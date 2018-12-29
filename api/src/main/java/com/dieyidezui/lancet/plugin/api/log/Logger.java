package com.dieyidezui.lancet.plugin.api.log;

public interface Logger {

    /**
     * @param level level
     * @return true if the given log level is enabled for this logger.
     */
    boolean isEnabled(LogLevel level);

    /**
     * Logs the given message at debug log level.
     *
     * @param message the log message.
     */
    void debug(String message);

    /**
     * Logs the given message at debug log level.
     *
     * @param message the log message.
     * @param objects the log message parameters.
     */
    void debug(String message, Object... objects);

    /**
     * Logs the given message at debug log level.
     *
     * @param message   the log message.
     * @param throwable the exception to log.
     */
    void debug(String message, Throwable throwable);

    /**
     * Logs the given message at info log level.
     *
     * @param message the log message.
     */
    void info(String message);

    /**
     * Logs the given message at info log level.
     *
     * @param message the log message.
     * @param objects the log message parameters.
     */
    void info(String message, Object... objects);

    /**
     * Logs the given message at info log level.
     *
     * @param message   the log message.
     * @param throwable the exception to log.
     */
    void info(String message, Throwable throwable);

    /**
     * Logs the given message at lifecycle log level.
     *
     * @param message the log message.
     */
    void lifecycle(String message);

    /**
     * Logs the given message at lifecycle log level.
     *
     * @param message the log message.
     * @param objects the log message parameters.
     */
    void lifecycle(String message, Object... objects);

    /**
     * Logs the given message at lifecycle log level.
     *
     * @param message   the log message.
     * @param throwable the exception to log.
     */
    void lifecycle(String message, Throwable throwable);

    /**
     * Logs the given message at warn log level.
     *
     * @param message the log message.
     */
    void warn(String message);

    /**
     * Logs the given message at warn log level.
     *
     * @param message the log message.
     * @param objects the log message parameters.
     */
    void warn(String message, Object... objects);

    /**
     * Logs the given message at warn log level.
     *
     * @param message   the log message.
     * @param throwable the exception to log.
     */
    void warn(String message, Throwable throwable);

    /**
     * Logs the given message at quiet log level.
     *
     * @param message the log message.
     */
    void quiet(String message);

    /**
     * Logs the given message at quiet log level.
     *
     * @param message the log message.
     * @param objects the log message parameters.
     */
    void quiet(String message, Object... objects);

    /**
     * Logs the given message at quiet log level.
     *
     * @param message   the log message.
     * @param throwable the exception to log.
     */
    void quiet(String message, Throwable throwable);

    /**
     * Logs the given message at error log level.
     *
     * @param message the log message.
     */
    void error(String message);

    /**
     * Logs the given message at error log level.
     *
     * @param message the log message.
     * @param objects the log message parameters.
     */
    void error(String message, Object... objects);

    /**
     * Logs the given message at error log level.
     *
     * @param message   the log message.
     * @param throwable the exception to log.
     */
    void error(String message, Throwable throwable);
}
