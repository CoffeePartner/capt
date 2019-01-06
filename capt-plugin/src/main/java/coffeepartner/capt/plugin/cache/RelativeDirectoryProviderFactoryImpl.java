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
        public File root() {
            return root;
        }

        @Override
        public BufferedSource asSource(String path) throws IOException {
            File target = create(path);
            Files.createParentDirs(target);
            return Okio.buffer(Okio.source(target));
        }

        @Override
        public void deleteIfExists(String path) throws IOException {
            File target = create(path);
            if (target.exists() && !target.delete()) {
                throw new IOException("delete file " + target + " failed");
            }
        }

        @Override
        public BufferedSink asSink(String path) throws IOException {
            File target = create(path);
            Files.createParentDirs(target);
            return Okio.buffer(Okio.sink(target));
        }

        private File create(String path) {
            if ('/' != File.separatorChar) {
                path = path.replace('/', File.separatorChar);
            }
            return new File(root, path);
        }

        @Override
        public void deleteAll() throws IOException {
            FileUtils.deleteDirectory(root());
        }
    }
}
