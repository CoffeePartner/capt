package com.dieyidezui.lancet.plugin.api;

import com.dieyidezui.lancet.plugin.api.log.Logger;

public interface Context {

    String getVariantName();

    Logger getLogger(Class<?> clazz);
}
