package com.lucene.util.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class LogAppender {

    private final Logger logger;

    public LogAppender(String name) {
        this.logger = CustomLogger.getLogger(name);
    }

    public LogAppender(Logger logger) {
        this.logger = logger;
    }

    public static String log(LogLevel logLevel, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "[" + timestamp + "] [" + logLevel + "] " + message;
    }

    public String debug(String message) {
        logger.fine(message);
        return log(LogLevel.DEBUG, message);
    }

    public String info(String message) {
        logger.info(message);
        return log(LogLevel.INFO, message);
    }

    public String warning(String message) {
        logger.warning(message);
        return log(LogLevel.WARNING, message);
    }

    public String error(String message) {
        logger.severe(message);
        return log(LogLevel.ERROR, message);
    }
}
