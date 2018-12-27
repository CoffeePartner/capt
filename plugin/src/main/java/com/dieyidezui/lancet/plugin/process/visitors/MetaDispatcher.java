package com.dieyidezui.lancet.plugin.process.visitors;

import com.dieyidezui.lancet.plugin.api.process.MetaProcessor;
import com.dieyidezui.lancet.plugin.resource.GlobalResource;
import com.dieyidezui.lancet.plugin.resource.VariantResource;
import com.dieyidezui.lancet.plugin.util.ConcurrentHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MetaDispatcher {


    private Set<String> preMetas = Collections.emptySet();
    private Set<String> metas = new ConcurrentHashSet<>();

    private final GlobalResource global;

    public MetaDispatcher(GlobalResource global) {
        this.global = global;
    }


    @SuppressWarnings("Convert2Lambda")
    public Consumer<MetaClasses> readPreMetas() {
        return new Consumer<MetaClasses>() {
            @Override
            public void accept(MetaClasses m) {
                preMetas = m.classes;
            }
        };
    }

    @SuppressWarnings("Convert2Lambda")
    public Supplier<MetaClasses> writeMetas() {
        return new Supplier<MetaClasses>() {
            @Override
            public MetaClasses get() {
                return new MetaClasses(metas);
            }
        };
    }

    public void addMeta(String meta) {
        metas.add(meta);
    }

    public static class MetaClasses {

        public MetaClasses(Set<String> classes) {
            this.classes = classes;
        }

        public Set<String> classes;
    }


    public void dispatchMetas(boolean incremental, VariantResource resource, MetaProcessorFactory factory) {

    }

    public interface MetaProcessorFactory {

        List<MetaProcessor> support(Set<String> annotation);
    }
}
