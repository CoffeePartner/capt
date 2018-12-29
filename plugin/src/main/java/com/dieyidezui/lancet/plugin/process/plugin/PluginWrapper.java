package com.dieyidezui.lancet.plugin.process.plugin;

import com.dieyidezui.lancet.plugin.api.Arguments;
import com.dieyidezui.lancet.plugin.api.LancetInternal;
import com.dieyidezui.lancet.plugin.api.OutputProvider;
import com.dieyidezui.lancet.plugin.api.Plugin;
import com.dieyidezui.lancet.plugin.api.asm.LancetClassVisitor;
import com.dieyidezui.lancet.plugin.api.graph.ClassInfo;
import com.dieyidezui.lancet.plugin.api.process.AnnotationProcessor;
import com.dieyidezui.lancet.plugin.api.transform.ClassRequest;
import com.dieyidezui.lancet.plugin.api.transform.ClassTransformer;
import com.dieyidezui.lancet.plugin.process.PluginBean;
import com.dieyidezui.lancet.plugin.process.visitors.AnnotationClassDispatcher;
import com.dieyidezui.lancet.plugin.process.visitors.ThirdRound;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.ConcurrentHashSet;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("unchecked")
public class PluginWrapper extends ForwardingLancet {

    private final boolean incremental;
    private final Plugin plugin;
    private final Arguments args;
    private final String id;
    private final VariantResource resource;
    private final Set<String> affected = new ConcurrentHashSet<>();
    private Set<String> preAffected = Collections.emptySet();

    public PluginWrapper(boolean incremental, Plugin plugin,
                         Arguments args,
                         String id, VariantResource resource, LancetInternal delegate) {
        super(delegate);
        this.incremental = incremental;
        this.plugin = plugin;
        this.args = args;
        this.id = id;
        this.resource = resource;
    }

    public String id() {
        return id;
    }

    public void callBeforeCreate() {
        plugin.beforeCreate(this);
    }

    public void callOnCreate() {
        plugin.onCreate(this);
    }

    public void callOnDestroy() {
        plugin.onDestroy(this);
    }

    public Set<String> getSupportedAnnotations() {
        return plugin.getSupportedAnnotations();
    }

    @Nullable
    public ThirdRound.TransformProvider newTransformProvider() {
        ClassTransformer transformer = new ClassTransformWrapper(plugin.onTransformClass());
        return new ThirdRound.TransformProvider() {

            @Override
            public void onClassAffected(String className) {
                affected.add(className);
            }

            @Override
            public ClassTransformer transformer() {
                return transformer;
            }
        };
    }

    @Nullable
    public AnnotationClassDispatcher.AnnotationProcessorProvider newAnnotationProvider() {
        AnnotationProcessor processor = plugin.onProcessAnnotations();
        if (processor != null) {
            return new AnnotationClassDispatcher.AnnotationProcessorProvider() {
                Set<String> supported = plugin.getSupportedAnnotations();

                @Override
                public Set<String> supports() {
                    return supported;
                }

                @Override
                public AnnotationProcessor processor() {
                    return processor;
                }
            };
        }
        return null;
    }

    @Override
    public boolean isIncremental() {
        return incremental;
    }

    @Override
    public Arguments getArgs() {
        return args;
    }

    @Override
    public OutputProvider outputs() {
        return resource.provider(id);
    }

    public PluginBean toBean() {
        return new PluginBean(id, Sets.union(affected, preAffected));
    }

    public void combinePre(PluginBean pre) {
        preAffected = pre.getAffectedClasses();
    }

    private static final ClassTransformer NOOP = new ClassTransformer() {
        @Override
        public ClassRequest beforeTransform() {
            return new ClassRequest() {
            };
        }

        @Nullable
        @Override
        public LancetClassVisitor onTransform(ClassInfo classInfo, boolean required) {
            return null;
        }
    };

    class ClassTransformWrapper extends ClassTransformer {
        private final ClassTransformer classTransformer;

        ClassTransformWrapper(@Nullable ClassTransformer classTransformer) {
            this.classTransformer = classTransformer == null ? NOOP : classTransformer;
        }

        @Override
        public ClassRequest beforeTransform() {
            return classTransformer.beforeTransform();
        }

        @Nullable
        @Override
        public LancetClassVisitor onTransform(ClassInfo classInfo, boolean required) {
            preAffected.remove(classInfo.name());
            return classTransformer.onTransform(classInfo, required);
        }

        @Override
        public void afterTransform() {
            classTransformer.afterTransform();
        }
    }
}
