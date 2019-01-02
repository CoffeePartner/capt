package coffeepartner.capt.sample.safecatcher;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import coffeepartner.capt.plugin.api.graph.ClassInfo;
import coffeepartner.capt.plugin.api.log.Logger;
import coffeepartner.capt.plugin.api.process.AnnotationProcessor;
import coffeepartner.capt.plugin.api.process.ClassConsumer;
import coffeepartner.capt.sample.safecatcher.rt.Match;

public class MethodReplacer implements ClassConsumer {

    static final String DESC = Type.getDescriptor(Match.class);
    private final Logger logger;
    private final Map<String, List<MatchBean>> map = new ConcurrentHashMap<>();
    private Map<String, List<MatchBean>> matchMap;
    private final Set<String> extra = new HashSet<>();
    private boolean hasNew = false;

    public MethodReplacer(Logger logger) {
        this.logger = logger;
    }

    public boolean match(String className, String owner, String name, String desc, MethodVisitor next) {
        List<MatchBean> beans = matchMap.get(genKey(owner, name, desc));
        if (beans != null && !beans.isEmpty()) {
            beans.get(0).invoke(className, next);
            return true;
        }
        return false;
    }

    static String genKey(String owner, String name, String desc) {
        return owner + " " + name + " " + desc;
    }

    public void write(Writer writer) throws IOException {

        List<MatchBean> list = new ArrayList<>(map.size() * 2);
        for (List<MatchBean> l : map.values()) {
            list.addAll(l);
        }
        new Gson().toJson(list, writer);

        writer.close();
    }

    public void load(Reader reader) throws IOException {
        List<MatchBean> list = new Gson().fromJson(reader, new TypeToken<List<MatchBean>>() {
        }.getType());

        for (MatchBean m : list) {
            List<MatchBean> l = map.get(m.sourceOwner);
            if (l == null) {
                l = new ArrayList<>();
                map.put(m.sourceOwner, l);
            }
            l.add(m);
        }

        reader.close();
    }

    public AnnotationProcessor toAnnotationProcessor() {
        return new AnnotationProcessor() {
            @Override
            public void onAnnotationClassRemoved(ClassInfo info) {
                List<MatchBean> list = map.remove(info.name());
                if (list != null) {
                    for (MatchBean m : list) {
                        extra.addAll(m.affected);
                    }
                }
            }

            @Override
            public ClassConsumer onAnnotationChanged(ClassInfo info) {
                return MethodReplacer.this;
            }

            @Override
            public ClassConsumer onAnnotationClassAdded(ClassInfo info) {
                return MethodReplacer.this;
            }

            @Override
            public ClassConsumer onAnnotationClassNotChanged(ClassInfo info) {
                return MethodReplacer.this;
            }

            @Override
            public ClassConsumer onAnnotationMatched(ClassInfo info) {
                return MethodReplacer.this;
            }

            @Override
            public ClassConsumer onAnnotationMismatch(ClassInfo info) {
                onAnnotationClassRemoved(info);
                return null;
            }

            @Override
            public void onProcessEnd() {
                matchMap = new HashMap<>();
                for (List<MatchBean> l : map.values()) {
                    for (MatchBean b : l) {
                        String key = genKey(b.owner, b.name, b.desc);
                        List<MatchBean> beans = matchMap.get(key);
                        if (beans == null) {
                            beans = new ArrayList<>();
                            matchMap.put(key, beans);
                        }
                        beans.add(b);
                    }
                }
                System.out.println(map);
            }
        };
    }

    public Set<String> extra() {
        return extra;
    }

    public void onClassRemoved(String c) {
        // emm..., it's a example, so we keep it easy.
        for (List<MatchBean> list : map.values()) {
            for (MatchBean b : list) {
                b.affected.remove(c);
            }
        }
    }

    public boolean isMatchClass(String name) {
        return map.get(name) != null;
    }

    @Override
    public void accept(ClassNode node) {
        List<MatchBean> beans = new ArrayList<>();
        for (MethodNode m : node.methods) {
            if (m.invisibleAnnotations != null) {
                for (AnnotationNode a : m.invisibleAnnotations) {
                    if (a.desc.equals(DESC)) {
                        MatchBean bean = new MatchBean();
                        bean.sourceOwner = node.name;
                        bean.sourceName = m.name;
                        bean.sourceDesc = m.desc;
                        if ((m.access & Opcodes.ACC_STATIC) == 0) {
                            logger.warn("Found none static method {}.{} with @match", node.name, m.name);
                            continue;
                        }
                        for (int i = 0; i < a.values.size(); i += 2) {
                            switch ((String) a.values.get(i)) {
                                case "owner":
                                    bean.owner = (String) a.values.get(i + 1);
                                    break;
                                case "name":
                                    bean.name = (String) a.values.get(i + 1);
                                    break;
                                case "desc":
                                    bean.desc = (String) a.values.get(i + 1);
                            }
                        }
                        beans.add(bean);
                    }
                }
            }
        }
        if (!beans.isEmpty()) {
            map.put(node.name, beans);
            hasNew = true;
        }
    }

    public boolean hasNew() {
        return hasNew;
    }


    public static class MatchBean {
        public String owner;
        public String name;
        public String desc;
        public String sourceOwner;
        public String sourceName;
        public String sourceDesc;
        public Set<String> affected = Collections.synchronizedSet(new HashSet<String>());

        public void invoke(String className, MethodVisitor next) {
            next.visitMethodInsn(Opcodes.INVOKESTATIC, sourceOwner, sourceName, sourceDesc, false);
            affected.add(className);
        }

        @Override
        public String toString() {
            return "MatchBean{" +
                    "owner='" + owner + '\'' +
                    ", name='" + name + '\'' +
                    ", desc='" + desc + '\'' +
                    ", sourceOwner='" + sourceOwner + '\'' +
                    ", sourceName='" + sourceName + '\'' +
                    ", sourceDesc='" + sourceDesc + '\'' +
                    ", affected=" + affected +
                    '}';
        }
    }
}
