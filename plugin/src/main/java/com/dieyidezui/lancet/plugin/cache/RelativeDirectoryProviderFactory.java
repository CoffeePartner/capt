package com.dieyidezui.lancet.plugin.cache;

import com.dieyidezui.lancet.plugin.api.util.RelativeDirectoryProvider;

import java.io.File;

public interface RelativeDirectoryProviderFactory {

    RelativeDirectoryProvider newProvider(File root);
}
