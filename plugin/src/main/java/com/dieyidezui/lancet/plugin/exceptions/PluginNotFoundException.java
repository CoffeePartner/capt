package com.dieyidezui.lancet.plugin.exceptions;

public class PluginNotFoundException extends RuntimeException {

    public PluginNotFoundException() {
    }

    public PluginNotFoundException(String message) {
        super(message);
    }

    public PluginNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginNotFoundException(Throwable cause) {
        super(cause);
    }
}
