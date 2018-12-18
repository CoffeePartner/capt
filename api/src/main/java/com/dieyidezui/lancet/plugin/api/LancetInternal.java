package com.dieyidezui.lancet.plugin.api;

import com.android.build.gradle.BaseExtension;
import org.gradle.api.Project;

import java.net.URLClassLoader;

/**
 * You'd better use {@link Lancet}.
 */
public interface LancetInternal extends Lancet {

    Project getProject();

    BaseExtension getAndroid();

    URLClassLoader lancetLoader();
}
