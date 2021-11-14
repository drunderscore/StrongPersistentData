package xyz.jame.strongpersistentdata;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The action to take upon passing null to a setter.
 */
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface NullAction
{
    enum Rule
    {
        /**
         * Pass null as the value to {@link org.bukkit.persistence.PersistentDataContainer#set(NamespacedKey, PersistentDataType, Object)}
         */
        Pass,
        /**
         * Remove the key via {@link org.bukkit.persistence.PersistentDataContainer#remove(NamespacedKey)}
         */
        Remove
    }

    Rule value();
}
