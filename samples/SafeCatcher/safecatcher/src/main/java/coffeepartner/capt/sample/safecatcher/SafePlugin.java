package coffeepartner.capt.sample.safecatcher;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import coffeepartner.capt.plugin.api.Capt;
import coffeepartner.capt.plugin.api.OutputProvider;
import coffeepartner.capt.plugin.api.Plugin;
import coffeepartner.capt.plugin.api.annotations.Def;
import coffeepartner.capt.plugin.api.asm.CaptClassVisitor;
import coffeepartner.capt.plugin.api.graph.ClassInfo;
import coffeepartner.capt.plugin.api.graph.Status;
import coffeepartner.capt.plugin.api.process.AnnotationProcessor;
import coffeepartner.capt.plugin.api.transform.ClassRequest;
import coffeepartner.capt.plugin.api.transform.ClassTransformer;
import coffeepartner.capt.plugin.api.util.RelativeDirectoryProvider;

@Def(supportedAnnotationTypes = "coffeepartner.capt.sample.safecatcher.rt.Match")
public class SafePlugin extends Plugin<Capt> {
    static final String CACHE_NAME = "match.json";
    private MethodReplacer replacer;
    RelativeDirectoryProvider provider;
    private Capt capt;

    @Override
    public void onCreate(Capt capt) throws IOException {
        this.capt = capt;
        provider = capt.getOutputs().getProvider(OutputProvider.Type.CACHE);
        replacer = new MethodReplacer(capt.getContext().getLogger(MethodReplacer.class));
        if (capt.isIncremental()) {
            replacer.read(new InputStreamReader(provider.asSource(CACHE_NAME).inputStream()));
        }

        Map<String, Object> args = capt.getArgs().getMyArguments().arguments();
        if(!args.get("plugin_defined_args1").equals(121322)) {
            throw new AssertionError();
        }
        if(!((Map)args.get("plugin_defined_args2")).isEmpty()) {
            throw new AssertionError();
        }
    }

    @Override
    public AnnotationProcessor onProcessAnnotations() {
        return replacer.toAnnotationProcessor();
    }

    @Override
    public ClassTransformer onTransformClass() {
        return new ClassTransformer() {
            @Override
            public ClassRequest beforeTransform() {
                return new ClassRequest() {
                    @Override
                    public Set<String> extraSpecified() {
                        return replacer.extra();
                    }

                    @Override
                    public Scope scope() {
                        return capt.isIncremental() && replacer.hasNew() ? Scope.ALL : Scope.CHANGED;
                    }
                };
            }

            @Override
            public CaptClassVisitor onTransform(ClassInfo classInfo, boolean required) {
                if (classInfo.exists() && !replacer.isMatchClass(classInfo.name())) {
                    return new SafeClassVisitor();
                } else if (classInfo.status() == Status.REMOVED && capt.isIncremental()) {
                    replacer.onClassRemoved(classInfo.name());
                }
                return null;
            }

            @Override
            public void afterTransform() throws IOException {
                Writer writer = new OutputStreamWriter(provider.asSink(SafePlugin.CACHE_NAME).outputStream(), Charset.defaultCharset());
                replacer.write(writer);
            }
        };
    }


    class SafeClassVisitor extends CaptClassVisitor {

        private String className;

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    if (!replacer.match(className, owner, name, desc, mv)) {
                        mv.visitMethodInsn(opcode, owner, name, desc, itf);
                    } else {
                        context().notifyChanged();
                    }
                }
            };
        }
    }
}
