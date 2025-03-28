package com.lucene.util.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;

public class CustomLogger {

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.FINE);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);
        consoleHandler.setFormatter(new LogFormatter());

        logger.setUseParentHandlers(false);
        logger.addHandler(consoleHandler);
        return logger;
    }
}