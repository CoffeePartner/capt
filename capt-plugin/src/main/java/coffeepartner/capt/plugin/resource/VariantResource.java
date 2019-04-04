package coffeepartner.capt.plugin.resource;

import coffeepartner.capt.plugin.api.OutputProvider;
import coffeepartner.capt.plugin.api.Plugin;
import coffeepartner.capt.plugin.cache.FileManager;
import coffeepartner.capt.plugin.cache.OutputProviderFactory;
import coffeepartner.capt.plugin.util.Constants;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.TransformInvocation;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Stream;

public class VariantResource implements Constants {

    private static final Logger LOGGER = Logging.getLogger(VariantResource.class);

    private final Loader loader = new Loader();
    private final String variant;
    private final FileManager files;
    private final OutputProviderFactory factory;
    private boolean incremental;

    public VariantResource(String variant, FileManager files, OutputProviderFactory factory) {
        this.variant = variant;
        this.files = files;
        this.factory = factory;
    }

    public String variant() {
        return variant;
    }

    public void init(TransformInvocation invocation, List<File> bootClasspath, Configuration target) {
        this.loader.initClassLoader(invocation, bootClasspath, target);
        this.files.attachContext(invocation);
    }

    public void setIncremental(boolean incremental) throws IOException {
        this.incremental = incremental;
        if (!incremental) {
            files.clearForFullMode();
        }
    }

    public InputStream openStream(String className) throws IOException {
        URL is = loader.runtimeLoader.getResource(className + ".class");
        if (is == null) {
            throw new IOException("open class failed: " + className);
        }
        URLConnection connection = is.openConnection();
        connection.setUseCaches(false); // ignore jar entry cache, or else you will die
        return connection.getInputStream();
    }

    public Class<?> loadApkClass(String className) throws ClassNotFoundException {
        return loader.runtimeLoader.loadClass(className);
    }

    public URLClassLoader getFullAndroidLoader() {
        return loader.fullAndroidLoader;
    }

    public Class<?> loadPluginClass(String className) throws ClassNotFoundException {
        return loader.runnerLoader.loadClass(className);
    }

    public URL loadPluginOnCapt(String pluginName) {
        return loader.loadPluginOnCapt(pluginName);
    }

    public boolean isIncremental() {
        return incremental;
    }

    public OutputProvider provider(String id) {
        return factory.newProvider(id);
    }

    public URLClassLoader loader() {
        return loader.runnerLoader;
    }

    static class Loader {

        private URLClassLoader runnerLoader;
        private URLClassLoader runtimeLoader;
        private URLClassLoader fullAndroidLoader;

        void initClassLoader(TransformInvocation invocation, List<File> bootClasspath, Configuration target) {

            URL[] runnerUrls = target.resolve().stream()
                    .map(f -> {
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw new AssertionError(e);
                        }
                    }).toArray(URL[]::new);
            URLClassLoader captDependencies = new URLClassLoader(runnerUrls, Plugin.class.getClassLoader());

            URL[] runtimeUrls = invocation.getInputs().stream()
                    .flatMap(s -> Stream.concat(s.getDirectoryInputs().stream(), s.getJarInputs().stream()))
                    .map(QualifiedContent::getFile)
                    .map(f -> {
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw new AssertionError(e);
                        }
                    })
                    .toArray(URL[]::new);

            URL[] fullAndroidUrls = Stream.concat(
                    Stream.concat(invocation.getInputs().stream(),
                            invocation.getReferencedInputs().stream())
                            .flatMap(s -> Stream.concat(s.getDirectoryInputs().stream(), s.getJarInputs().stream()))
                            .map(QualifiedContent::getFile),
                    bootClasspath.stream())
                    .map(f -> {
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw new AssertionError(e);
                        }
                    }).toArray(URL[]::new);

            // runner doesn't have runtime
            this.runnerLoader = captDependencies;
            this.runtimeLoader = new URLClassLoader(runtimeUrls);
            this.fullAndroidLoader = new URLClassLoader(fullAndroidUrls);
        }

        public URL loadPluginOnCapt(String pluginName) {
            return runnerLoader.getResource(PLUGIN_PATH + pluginName + ".properties");
        }
    }

}
