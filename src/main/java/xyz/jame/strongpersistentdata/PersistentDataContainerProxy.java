package xyz.jame.strongpersistentdata;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

class PersistentDataContainerProxy implements InvocationHandler
{
    private static final Method HASHCODE_METHOD;
    private static final Method EQUALS_METHOD;
    private static final Method TOSTRING_METHOD;
    private final PersistentDataContainer container;
    private final StrongPersistentData strongPersistentData;

    public PersistentDataContainerProxy(PersistentDataContainer container, StrongPersistentData strongPersistentData)
    {
        this.container = container;
        this.strongPersistentData = strongPersistentData;
    }

    static
    {
        try
        {
            HASHCODE_METHOD = Object.class.getMethod("hashCode");
            EQUALS_METHOD = Object.class.getMethod("equals", Object.class);
            TOSTRING_METHOD = Object.class.getMethod("toString");
        }
        catch (NoSuchMethodException wtf)
        {
            // If you are here, your JVM implementation sucks.
            throw new IllegalStateException("Impossible situation where either hashCode, equals, or toString methods do not exist", wtf);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
    {
        if (EQUALS_METHOD.equals(method))
            return proxy == args[0];
        else if (HASHCODE_METHOD.equals(method))
            return System.identityHashCode(proxy);
        else if (TOSTRING_METHOD.equals(method))
            return "proxy object@" + Integer.toHexString(System.identityHashCode(proxy)); // FIXME: it should be ClassName@HexHashCode, right?

        String name;
        String effectiveMethodName;
        boolean isRemoveMethod = method.getName().startsWith("remove") && method.getReturnType() == Void.TYPE && method.getParameterCount() == 0;
        boolean isHasMethod = method.getName().startsWith("has") && method.getReturnType() == Boolean.TYPE && method.getParameterCount() == 0;
        var explicitNameAnnotation = method.getAnnotation(ExplicitName.class);
        var explicitTypeAnnotation = method.getAnnotation(ExplicitType.class);
        var shouldWrap = method.getAnnotation(Wrap.class) != null;

        if (isRemoveMethod)
            effectiveMethodName = Character.toLowerCase(method.getName().charAt(6)) + method.getName().substring(7);
        else if (isHasMethod)
            effectiveMethodName = Character.toLowerCase(method.getName().charAt(3)) + method.getName().substring(4);
        else
            effectiveMethodName = method.getName();

        if (explicitNameAnnotation != null)
            name = explicitNameAnnotation.value();
        else
            name = camelCaseToSnakeCase(effectiveMethodName);

        var key = new NamespacedKey(strongPersistentData.plugin, name);

        if (isRemoveMethod)
        {
            container.remove(key);
            return null;
        }

        if (isHasMethod)
        {
            Class<?> type;

            // If we are annotated with @ExplicitType, then it's super easy to know our type
            if (explicitTypeAnnotation != null)
                type = explicitTypeAnnotation.value();
            else
            {
                // Otherwise, we can _try_ to deduce it by finding its setter method.
                Method maybeSetter = null;

                for (var m : proxy.getClass().getMethods())
                {
                    if (m.getName().equals(effectiveMethodName) && m.getReturnType() == Void.TYPE && m.getParameterCount() == 1)
                    {
                        maybeSetter = m;
                        break;
                    }
                }

                if (maybeSetter == null)
                    throw new IllegalStateException("Couldn't deduce type for has method");

                type = maybeSetter.getParameters()[0].getType();
            }

            // Let's not try to deduce based on its getter method. It might be an Optional (or specialized), and
            // it's not worth trying to distinguish

            var persistentType = strongPersistentData.persistentDataTypes.get(type);
            if (persistentType == null)
                throw createNoPersistentTypeException(method.getName(), type);

            return container.has(key, persistentType);
        }

        // This should be a setter
        if (method.getReturnType() == Void.TYPE)
        {
            if (method.getParameterCount() != 1)
                throw new IllegalStateException("Setter with more than one parameter");

            var type = method.getParameters()[0].getType();
            var nullActionAnnotation = method.getAnnotation(NullAction.class);

            if (nullActionAnnotation != null && nullActionAnnotation.value() == NullAction.Rule.Remove && args[0] == null)
            {
                container.remove(key);
                return null;
            }

            if (shouldWrap)
            {
                if (!Proxy.isProxyClass(args[0].getClass()))
                    throw new IllegalStateException("Non-proxy object passed to @Wrap setter");

                var proxyObject = Proxy.getInvocationHandler(args[0]);
                if (!(proxyObject instanceof PersistentDataContainerProxy))
                    throw new IllegalStateException("Incorrect Proxy object passed to @Wrap setter");

                var containerProxyObject = (PersistentDataContainerProxy) proxyObject;
                container.set(key, PersistentDataType.TAG_CONTAINER, containerProxyObject.container);

                return null;
            }

            var persistentType = strongPersistentData.persistentDataTypes.get(type);
            if (persistentType == null)
                throw createNoPersistentTypeException(method.getName(), type);

            container.set(key, persistentType, persistentType.getComplexType().cast(args[0]));

            return null;
        }
        // This should be a getter
        else
        {
            if (method.getParameterCount() != 0)
                throw new IllegalStateException("Getter with more than 0 parameters");

            var type = method.getReturnType();

            if (type == OptionalInt.class)
            {
                var maybeValue = container.get(key, PersistentDataType.INTEGER);
                if (maybeValue == null)
                    return OptionalInt.empty();

                return OptionalInt.of(maybeValue);
            }
            else if (type == OptionalLong.class)
            {
                var maybeValue = container.get(key, PersistentDataType.LONG);
                if (maybeValue == null)
                    return OptionalLong.empty();

                return OptionalLong.of(maybeValue);
            }
            else if (type == OptionalDouble.class)
            {
                var maybeValue = container.get(key, PersistentDataType.DOUBLE);
                if (maybeValue == null)
                    return OptionalDouble.empty();

                return OptionalDouble.of(maybeValue);
            }
            else if (type == Optional.class)
            {
                // Type erasure means we cannot deduce the class type from the type parameter.

                Class<?> optionalParameterizedType;

                // If we are annotated with @ExplicitType, then it's super easy to know our type
                if (explicitTypeAnnotation != null)
                    optionalParameterizedType = explicitTypeAnnotation.value();
                else
                {
                    // Otherwise, we can _try_ to deduce it by finding its setter method.
                    Method maybeSetter = null;

                    for (var m : proxy.getClass().getMethods())
                    {
                        if (m.getName().equals(effectiveMethodName) && m.getReturnType() == Void.TYPE && m.getParameterCount() == 1)
                        {
                            maybeSetter = m;
                            break;
                        }
                    }

                    if (maybeSetter == null)
                        throw new IllegalStateException("Couldn't deduce parameterized type for Optional getter");

                    optionalParameterizedType = maybeSetter.getParameters()[0].getType();
                }

                if (shouldWrap)
                {
                    // TODO: container array
                    var value = container.get(key, PersistentDataType.TAG_CONTAINER);
                    if (value == null)
                        return Optional.empty();

                    return Optional.of(strongPersistentData.wrap(value, optionalParameterizedType));
                }

                var persistentType = strongPersistentData.persistentDataTypes.get(optionalParameterizedType);
                if (persistentType == null)
                    throw createNoPersistentTypeException(method.getName(), optionalParameterizedType);

                return Optional.ofNullable(container.get(key, persistentType));
            }

            if (shouldWrap)
            {
                var value = container.get(key, PersistentDataType.TAG_CONTAINER);
                if (value == null)
                    return null;

                return strongPersistentData.wrap(value, type);
            }

            var persistentType = strongPersistentData.persistentDataTypes.get(type);
            if (persistentType == null)
                throw createNoPersistentTypeException(method.getName(), type);

            var value = container.get(key, persistentType);

            if (value == null)
            {
                if (type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE || type == Long.TYPE)
                {
                    var defaultValueAnnotation = method.getAnnotation(DefaultValue.Numeric.class);
                    if (defaultValueAnnotation != null)
                    {
                        if (type == Byte.TYPE)
                            return (byte) defaultValueAnnotation.value();
                        if (type == Short.TYPE)
                            return (short) defaultValueAnnotation.value();
                        if (type == Integer.TYPE)
                            return (int) defaultValueAnnotation.value();
                        if (type == Long.TYPE)
                            return defaultValueAnnotation.value();
                    }
                }
                else if (type == Float.TYPE || type == Double.TYPE)
                {
                    var defaultValueAnnotation = method.getAnnotation(DefaultValue.NumericFloating.class);
                    if (defaultValueAnnotation != null)
                    {
                        if (type == Float.TYPE)
                            return (float) defaultValueAnnotation.value();
                        if (type == Double.TYPE)
                            return defaultValueAnnotation.value();

                    }
                }
                else if (type == String.class)
                {
                    var defaultValueAnnotation = method.getAnnotation(DefaultValue.String.class);
                    if (defaultValueAnnotation != null)
                        return defaultValueAnnotation.value();
                }

                if (type.isPrimitive())
                    throw new IllegalStateException("Getter with primitive type cannot return null");
            }

            return value;
        }
    }

    private static String camelCaseToSnakeCase(String camelCase)
    {
        var builder = new StringBuilder(camelCase.length());

        for (var c : camelCase.toCharArray())
        {
            if (Character.isUpperCase(c))
            {
                builder.append('_');
                builder.append(Character.toLowerCase(c));
            }
            else
            {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static IllegalStateException createNoPersistentTypeException(String key, Class<?> type)
    {
        return new IllegalStateException("No persistent type exists for " + key + " of type " + type);
    }
}
