package com.dieyidezui.capt.plugin.cache;

import com.dieyidezui.capt.plugin.api.util.RelativeDirectoryProvider;

import java.io.File;

public interface RelativeDirectoryProviderFactory {

    RelativeDirectoryProvider newProvider(File root);
}
