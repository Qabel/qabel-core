package de.qabel.core;

import java.util.List;

public class StringUtils {
    public static String join(String separator, String[] parts) {
        return org.apache.commons.lang3.StringUtils.join(parts, separator);
    }
    public static <T> String join(String separator, List<T> parts) {
        return org.apache.commons.lang3.StringUtils.join(parts, separator);
    }
}
