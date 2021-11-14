package xyz.jame.strongpersistentdata;

import org.bukkit.persistence.PersistentDataContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * When used on a getter, this will retrieve a {@link org.bukkit.persistence.PersistentDataType#TAG_CONTAINER} from the
 * container at the key, and {@link StrongPersistentData#wrap(PersistentDataContainer, Class)} it to the return type.
 * </p>
 *
 * <p>
 * When used on a setter, this will retrieve a {@link org.bukkit.persistence.PersistentDataType#TAG_CONTAINER} from the
 * wrapped parameter, and set it on the container.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Wrap
{
}