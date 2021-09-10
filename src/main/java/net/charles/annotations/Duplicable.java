package net.charles.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     The {@link Duplicable} annotation allows your java object to have a duplicate in the redis database
 *     This include the {@link DataKey} value that give access to your object in the database
 *     If there is a duplicate key in the redis database, the {@link DataKey} will be serialized
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Duplicable {
}
