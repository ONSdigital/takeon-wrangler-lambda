package uk.gov.ons.validation.util;

import java.util.Map;

import uk.gov.ons.validation.exception.NoPropertyFoundException;

import static java.lang.String.format;

public final class PropertiesUtil {
    private static final String PROPERTY_NOT_FOUND_MESSAGE = "Unable to find the property %s";
    private static final Map<String, String> environmentVars = System.getenv();

    private PropertiesUtil() {
    }

    public static String getProperty(String key) {
        if (environmentVars.containsKey(key)) {
            return environmentVars.get(key);
        }
        throw new NoPropertyFoundException((format(PROPERTY_NOT_FOUND_MESSAGE, key)));
    }
}
