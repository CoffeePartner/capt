package coffeepartner.capt.plugin.api.util;

import okio.BufferedSink;
import okio.BufferedSource;

import java.io.File;
import java.io.IOException;

public interface RelativeDirectoryProvider {

    /**
     * @return get the root directory, may not exists
     */
    File root();

    /**
     * Use '/' as separator, even if windows.
     *
     * @param path the relative path
     * @return source
     * @throws IOException If create file failed.
     */
    BufferedSource asSource(String path) throws IOException;


    /**
     * Use '/' as separator, even if windows.
     *
     * @param path the relative path
     * @throws IOException if delete failed
     */
    void deleteIfExists(String path) throws IOException;

    /**
     * Use '/' as separator, even if windows.
     *
     * @param path the relative path
     * @return sink
     * @throws IOException If file not exists.
     */
    BufferedSink asSink(String path) throws IOException;

    /**
     * Delete all content in {@link #root()}. This is useful when running in non-incremental mode
     *
     * @throws IOException if deleting the output failed.
     */
    void deleteAll() throws IOException;
}
