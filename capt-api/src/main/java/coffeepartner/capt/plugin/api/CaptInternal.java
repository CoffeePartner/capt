package coffeepartner.capt.plugin.api;

import com.android.build.gradle.BaseExtension;
import org.gradle.api.Project;

import java.net.URLClassLoader;

/**
 * You'd better use {@link Capt}.
 * If you create capt plugin by java/groovy plugin with Gradle runtime, you can use CaptInternal.
 */
public interface CaptInternal extends Capt {

    /**
     * @return this project
     */
    Project getProject();

    /**
     * @return android extension
     */
    BaseExtension getAndroid();

    /**
     * @return the class loader contains all cpat configuration files correspond to variant
     */
    URLClassLoader captLoader();
}
