package coffeepartner.capt.plugin.api;

import com.android.build.gradle.BaseExtension;
import org.gradle.api.Project;

import java.net.URLClassLoader;

/**
 * You'd better use {@link Capt}.
 */
public interface CaptInternal extends Capt {

    Project getProject();

    BaseExtension getAndroid();

    URLClassLoader captLoader();
}
