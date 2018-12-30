package coffeepartner.capt.plugin.cache;

import coffeepartner.capt.plugin.api.util.RelativeDirectoryProvider;
import com.google.common.io.Files;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class RelativeDirectoryProviderFactoryImpl implements RelativeDirectoryProviderFactory {
    @Override
    public RelativeDirectoryProvider newProvider(File root) {
        return new RelativeDirectoryProviderImpl(root);
    }

    static class RelativeDirectoryProviderImpl implements RelativeDirectoryProvider {

        private final File root;

        RelativeDirectoryProviderImpl(File root) {
            this.root = root;
        }

        @Override
        public File root() throws IOException {
            root.mkdirs();
            if (!root.isDirectory()) {
                throw new IOException("Unable to callCreate directories of " + root);
            }
            return root;
        }

        @Override
        public BufferedSource asSource(String path) throws IOException {
            return Okio.buffer(Okio.source(ensure(path)));
        }

        @Override
        public BufferedSink asSink(String path) throws IOException {
            return Okio.buffer(Okio.sink(ensure(path)));
        }

        private File ensure(String path) throws IOException {
            if ('/' != File.separatorChar) {
                path = path.replace('/', File.separatorChar);
            }
            File target = new File(root, path);
            Files.createParentDirs(target);
            return target;
        }

        @Override
        public void deleteAll() throws IOException {
            FileUtils.cleanDirectory(root());
        }
    }
}
