package com.dieyidezui.lancet.plugin.api;

import java.io.File;

public interface OutputProvider {

    /**
     * Ensure the dir exists
     *
     * @return dir
     */
    File getOutputClassRootDir();

    /**
     * Ensure the dir exists
     *
     * @return dir
     */
    File getTempDir();

    /**
     * Ensure the dir exists
     *
     * @return dir
     */
    File getCacheDir();
}
