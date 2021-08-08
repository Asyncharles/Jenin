package net.charles.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     The {@link DataKey} annotation defines the field that will be used as a key to access the value in redis
 *     The value of the field annotated will be the key linked to the stored data
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataKey {
    /**
     *
     * @return {@code true} if you wish for the field to be included in the serialized object, {@code false} otherwise
     */
    boolean include();
}
