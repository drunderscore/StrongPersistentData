package xyz.jame.strongpersistentdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define an explicit setter/getter/has name, rather than trying to deduce it from the method name.
 */
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ExplicitName
{
    String value();
}