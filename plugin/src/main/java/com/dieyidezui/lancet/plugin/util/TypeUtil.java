package com.dieyidezui.lancet.plugin.util;

public class TypeUtil {

    /**
     * Ljava/lang/Object; => java/lang/Object
     */
    public static String objDescToInternalName(String desc) {
        return desc.substring(1, desc.length() - 1);
    }
}
