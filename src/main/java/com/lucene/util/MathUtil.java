package com.lucene.util;

final public class MathUtil {

    public static float roundUpToHundredth(float value) {
        return (float) (Math.ceil(value * 100) / 100.0);
    }

    public static float foundUpToThousandth(float value) {
        return (float) (Math.ceil(value * 1000) / 1000.0);
    }

    private MathUtil() {
        // Prevent instantiation
    }
}
