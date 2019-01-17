package coffeepartner.capt.plugin.api.logger;


import coffeepartner.capt.plugin.api.log.LogLevel;
import coffeepartner.capt.plugin.api.log.Logger;
import org.gradle.api.logging.Logging;

public class LoggerFactory {

    private LoggerFactory() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return new LoggerImpl(Logging.getLogger(clazz));
    }

    static class LoggerImpl implements Logger {

        private final org.gradle.api.logging.Logger logger;

        LoggerImpl(org.gradle.api.logging.Logger logger) {
            this.logger = logger;
        }

        @Override
        public boolean isEnabled(LogLevel level) {
            org.gradle.api.logging.LogLevel logLevel;
            switch (level) {
                case DEBUG:
                    logLevel = org.gradle.api.logging.LogLevel.DEBUG;
                    break;
                case INFO:
                    logLevel = org.gradle.api.logging.LogLevel.INFO;
                    break;
                case LIFECYCLE:
                    logLevel = org.gradle.api.logging.LogLevel.LIFECYCLE;
                    break;
                case WARN:
                    logLevel = org.gradle.api.logging.LogLevel.WARN;
                    break;
                case QUIET:
                    logLevel = org.gradle.api.logging.LogLevel.QUIET;
                    break;
                default:
                    logLevel = org.gradle.api.logging.LogLevel.ERROR;
                    break;
            }
            return logger.isEnabled(logLevel);
        }

        @Override
        public void debug(String message) {
            logger.debug(message);
        }

        @Override
        public void debug(String message, Object... objects) {
            logger.debug(message, objects);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            logger.debug(message, throwable);
        }

        @Override
        public void info(String message) {
            logger.info(message);
        }

        @Override
        public void info(String message, Object... objects) {
            logger.info(message, objects);
        }

        @Override
        public void info(String message, Throwable throwable) {
            logger.info(message, throwable);
        }

        @Override
        public void lifecycle(String message) {
            logger.lifecycle(message);
        }

        @Override
        public void lifecycle(String message, Object... objects) {
            logger.lifecycle(message, objects);
        }

        @Override
        public void lifecycle(String message, Throwable throwable) {
            logger.lifecycle(message, throwable);
        }

        @Override
        public void warn(String message) {
            logger.warn(message);
        }

        @Override
        public void warn(String message, Object... objects) {
            logger.warn(message, objects);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            logger.warn(message, throwable);
        }

        @Override
        public void quiet(String message) {
            logger.quiet(message);
        }

        @Override
        public void quiet(String message, Object... objects) {
            logger.quiet(message, objects);
        }

        @Override
        public void quiet(String message, Throwable throwable) {
            logger.quiet(message, throwable);
        }

        @Override
        public void error(String message) {
            logger.error(message);
        }

        @Override
        public void error(String message, Object... objects) {
            logger.error(message, objects);
        }

        @Override
        public void error(String message, Throwable throwable) {
            logger.error(message, throwable);
        }
    }
}
