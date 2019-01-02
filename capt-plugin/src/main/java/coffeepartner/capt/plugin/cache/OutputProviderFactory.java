package coffeepartner.capt.plugin.cache;

import coffeepartner.capt.plugin.api.OutputProvider;
import coffeepartner.capt.plugin.api.util.RelativeDirectoryProvider;

import java.io.File;
import java.io.IOException;

public class OutputProviderFactory {

    private final RelativeDirectoryProviderFactory provider;
    private final RootSelector selector;

    public OutputProviderFactory(RelativeDirectoryProviderFactory provider, RootSelector selector) {
        this.provider = provider;
        this.selector = selector;
    }

    public OutputProvider newProvider(String id) {
        return new OutputProviderImpl(id);
    }

    public interface RootSelector {
        File select(OutputProvider.Type type, String id);
    }


    class OutputProviderImpl implements OutputProvider {

        private final String id;

        public OutputProviderImpl(String id) {
            this.id = id;
        }

        @Override
        public RelativeDirectoryProvider getProvider(Type type) {
            File root = selector.select(type, id);
            return provider.newProvider(root);
        }

        @Override
        public void deleteAll() throws IOException {
            for (Type type : Type.values()) {
                getProvider(type).deleteAll();
            }
        }
    }
}
