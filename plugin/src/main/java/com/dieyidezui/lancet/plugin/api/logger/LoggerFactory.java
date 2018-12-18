package com.dieyidezui.lancet.plugin.api.logger;


import com.dieyidezui.lancet.plugin.api.log.Logger;
import org.gradle.api.logging.Logging;

public class LoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return new LoggerImpl(Logging.getLogger(clazz));
    }

    static class LoggerImpl implements Logger {

        private final org.gradle.api.logging.Logger logger;

        public LoggerImpl(org.gradle.api.logging.Logger logger) {
            this.logger = logger;
        }

    }

}
