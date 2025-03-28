package com.lucene.util.logging;

import java.util.logging.LogRecord;
import java.util.logging.Formatter;

public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return record.getLevel() + ": " + formatMessage(record) + "\n";
    }
}


