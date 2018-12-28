package com.dieyidezui.lancet.plugin.util;

import com.dieyidezui.lancet.rt.annotations.Meta;
import com.dieyidezui.lancet.rt.annotations.RemoveWhenTransform;
import org.objectweb.asm.Type;

import java.nio.charset.Charset;

public interface Constants {

    String META = Type.getDescriptor(Meta.class);
    String REMOVE = Type.getDescriptor(RemoveWhenTransform.class);

    int OPT_SIZE = 8192;

    String NAME = "lancet";

    String CAPITALIZED_NAME = "Lancet";

    String PLUGIN_PATH = "META-INF/lancet-plugins/";

    String ANDROID_TEST = "AndroidTest";

    String TEST = "test";

    String PLUGIN_KEY = "implementation-class";

    Charset UTF8 = Charset.forName("utf-8");
}
