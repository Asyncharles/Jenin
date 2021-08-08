package net.charles.parser;

import net.charles.annotations.DataKey;
import net.charles.exceptions.DataKeyNotFoundException;

import java.lang.reflect.Field;

public final class KeyManager {
    public static boolean hasDataKey(Class<?> clazz) {
        boolean valid = false;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(DataKey.class) != null) {
                if (valid) return false;
                valid = true;
            }
        }
        return valid;
    }

    public static <T> String findKey(T t) throws IllegalAccessException {
        if (!hasDataKey(t.getClass())) {
            throw new DataKeyNotFoundException("no data key were found");
        }
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.getAnnotation(DataKey.class) != null) {
                field.setAccessible(true);
                return String.valueOf(field.get(t));
            }
        }
        return null;
    }
}
