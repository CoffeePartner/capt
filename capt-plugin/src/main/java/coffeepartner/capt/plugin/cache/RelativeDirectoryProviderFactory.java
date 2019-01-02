package coffeepartner.capt.plugin.cache;

import coffeepartner.capt.plugin.api.util.RelativeDirectoryProvider;

import java.io.File;

public interface RelativeDirectoryProviderFactory {

    RelativeDirectoryProvider newProvider(File root);
}
