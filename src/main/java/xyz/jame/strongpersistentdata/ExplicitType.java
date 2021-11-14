package xyz.jame.strongpersistentdata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define an explicit type. This has different implications depending on the targeted method.
 *
 * <ul>
 *
 * <li>
 * If the method is a has method (starts with "has", return type {@link Boolean#TYPE}, zero parameters),
 * this will define the expected type that should be there, rather than attempting to deduce it from  it's setter.
 * </li>
 *
 * <li>
 * If the method is an {@link java.util.Optional} get method, (return type {@link java.util.Optional}, zero parameters),
 * this will define the parameterized type of {@link java.util.Optional}, rather than attempting to deduce it from it's
 * setter.
 * </li>
 * </ul>
 *
 * <p> Otherwise, this has no effect.
 */
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ExplicitType
{
    Class<?> value();
}