package net.charles.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     The {@link Exclude} annotation defines whether the field or the class will be included in the stored data
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Exclude {
    /**
     *
     * @return {@code true} if you wish to serialize the type or field as a null value, {@code false} if you wish to remove the type or the field from the serialization
     */
    boolean serializeAsNull() default true;
}
