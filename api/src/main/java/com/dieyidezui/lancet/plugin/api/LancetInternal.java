package com.dieyidezui.lancet.plugin.api;

import com.android.build.gradle.BaseExtension;
import org.gradle.api.Project;

public interface LancetInternal extends Lancet {

    Project getProject();

    BaseExtension getAndroid();
}
