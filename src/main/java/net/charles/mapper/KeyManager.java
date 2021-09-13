package net.charles.mapper;

import net.charles.annotations.DataKey;
import net.charles.annotations.Duplicable;
import net.charles.exceptions.parser.DataKeyNotFoundException;

import java.lang.reflect.Field;
import java.util.UUID;

public final class KeyManager {
    /**
     * Defines if the class has a {@link DataKey}
     * @param clazz the class
     * @return {@code true} if the class has a {@link DataKey}, {@code false} otherwise
     */
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

    /**
     * Finds and returns the {@link DataKey} field value
     * @param t the object instance
     * @param <T> the object type
     * @return the field value
     * @throws IllegalAccessException
     */
    public static <T> String findKey(T t) throws IllegalAccessException {
        if (!hasDataKey(t.getClass())) {
            throw new DataKeyNotFoundException("no data key were found");
        }
        final boolean duplicable = t.getClass().getAnnotation(Duplicable.class) != null;
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.getAnnotation(DataKey.class) != null) {
                field.setAccessible(true);
                return (duplicable ? UUID.randomUUID() + "=" + field.get(t) : String.valueOf(field.get(t)));
            }
        }
        return null;
    }
}
