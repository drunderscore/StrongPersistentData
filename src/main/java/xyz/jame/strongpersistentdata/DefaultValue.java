package xyz.jame.strongpersistentdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The value to use when a getter returns null.
 */
public class DefaultValue
{
    private DefaultValue()
    {
    }

    /**
     * Use this annotation if the return type is {@link Byte#TYPE}, {@link Short#TYPE}, {@link Integer#TYPE}, or
     * {@link Long#TYPE}.
     */
    @Target(ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Numeric
    {
        long value();
    }

    /**
     * Use this annotation if the return type is {@link Float#TYPE}, or {@link Double#TYPE}.
     */
    @Target(ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface NumericFloating
    {
        double value();
    }

    @Target(ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface String
    {
        java.lang.String value();
    }
}