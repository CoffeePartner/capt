package coffeepartner.capt.plugin;

import coffeepartner.capt.plugin.api.graph.ClassInfo;
import coffeepartner.capt.plugin.dsl.CaptPluginExtension;
import coffeepartner.capt.plugin.graph.ClassBean;
import coffeepartner.capt.plugin.resource.GlobalResource;
import coffeepartner.capt.plugin.util.CaptThreadFactory;
import coffeepartner.capt.plugin.util.Constants;
import coffeepartner.capt.plugin.variant.VariantManager;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryPlugin;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GradleCaptPlugin implements Plugin<Project>, Constants {

    private static final Logger LOGGER = Logging.getLogger(GradleCaptPlugin.class);

    @Override
    public void apply(Project project) {
        project.getPlugins().matching(p -> p instanceof AppPlugin || p instanceof LibraryPlugin)
                .all(c -> {

                    BaseExtension baseExtension = (BaseExtension) project.getExtensions().getByName("android");
                    project.getExtensions().create(NAME, GradleCaptExtension.class, project.container(CaptPluginExtension.class));

                    VariantManager variantManager = new VariantManager(createGlobalResource(project, baseExtension),
                            baseExtension, project);
                    // callCreate configurations for separate variant
                    variantManager.createConfigurationForVariant();

                    CaptTransform captTransform = new CaptTransform(variantManager);
                    baseExtension.registerTransform(captTransform);
                });
    }

    private static GlobalResource createGlobalResource(Project project, BaseExtension baseExtension) {

        int core = Runtime.getRuntime().availableProcessors();
        // use 20s instead if 60s to opt memory
        // 3 x core threads at most
        // Use it combine with ForkJoinPool
        ExecutorService io = new ThreadPoolExecutor(0, core * 3,
                20L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new CaptThreadFactory());

        // ForkJoinPool.common() just have core - 1, because it use the waiting thread,
        // But we just wait at IO threads, not computation, so we need core threads.
        ForkJoinPool computation = new ForkJoinPool(core);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                // optimize for List<ClassBean>, reduce array copy
                .registerTypeAdapter(new TypeToken<List<ClassBean>>() {
                }.getType(), (InstanceCreator) select -> new ArrayList<ClassInfo>(Constants.OPT_SIZE))
                .create();

        File root = new File(project.getBuildDir(), NAME);

        return new GlobalResource(project, root, computation, io, gson, (GradleCaptExtension) project.getExtensions().getByName(NAME), baseExtension);
    }
}
