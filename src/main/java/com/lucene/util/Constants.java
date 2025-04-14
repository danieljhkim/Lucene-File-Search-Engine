package com.lucene.util;

import java.util.Set;

public class Constants {

    public static final String[] SUPPORTED_FILE_TYPES = {"all", "txt", "py", "java", "md", "docx", "doc", "pptx", "js", "json", "html", "xml", "csv"};
    public static final Set<String> FILE_TYPES_SET = CollectionsUtil.arrayToSet(SUPPORTED_FILE_TYPES);

    private Constants() {
        // Prevent instantiation
    }
}
