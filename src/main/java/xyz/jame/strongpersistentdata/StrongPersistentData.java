package xyz.jame.strongpersistentdata;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Proxy;
import java.util.HashMap;

public class StrongPersistentData
{
    final Plugin plugin;
    final HashMap<Class<?>, PersistentDataType> persistentDataTypes = new HashMap<>();

    /**
     * Constructs a {@link StrongPersistentData}
     *
     * @param plugin the constructing plugin, which will own the generated {@link org.bukkit.NamespacedKey}
     */
    public StrongPersistentData(Plugin plugin)
    {
        this.plugin = plugin;
        // Primitive wrappers
        persistentDataTypes.put(Byte.class, PersistentDataType.BYTE);
        persistentDataTypes.put(Short.class, PersistentDataType.SHORT);
        persistentDataTypes.put(Integer.class, PersistentDataType.INTEGER);
        persistentDataTypes.put(Long.class, PersistentDataType.LONG);
        persistentDataTypes.put(Float.class, PersistentDataType.FLOAT);
        persistentDataTypes.put(Double.class, PersistentDataType.DOUBLE);

        // Primitives
        persistentDataTypes.put(Byte.TYPE, PersistentDataType.BYTE);
        persistentDataTypes.put(Short.TYPE, PersistentDataType.SHORT);
        persistentDataTypes.put(Integer.TYPE, PersistentDataType.INTEGER);
        persistentDataTypes.put(Long.TYPE, PersistentDataType.LONG);
        persistentDataTypes.put(Float.TYPE, PersistentDataType.FLOAT);
        persistentDataTypes.put(Double.TYPE, PersistentDataType.DOUBLE);

        // uhh, String
        persistentDataTypes.put(String.class, PersistentDataType.STRING);

        // Primitive arrays
        persistentDataTypes.put(byte[].class, PersistentDataType.BYTE_ARRAY);
        persistentDataTypes.put(int[].class, PersistentDataType.INTEGER_ARRAY);
        persistentDataTypes.put(long[].class, PersistentDataType.LONG_ARRAY);

        // Primitive wrapper arrays
        persistentDataTypes.put(Byte[].class, PersistentDataType.BYTE_ARRAY);
        persistentDataTypes.put(Integer[].class, PersistentDataType.INTEGER_ARRAY);
        persistentDataTypes.put(Long[].class, PersistentDataType.LONG_ARRAY);

        // Direct containers
        persistentDataTypes.put(PersistentDataContainer.class, PersistentDataType.TAG_CONTAINER);
        persistentDataTypes.put(PersistentDataContainer[].class, PersistentDataType.TAG_CONTAINER_ARRAY);
    }

    /**
     * Wrap a {@link PersistentDataContainer} to an interface, specified by {@code interfaceType}.
     * The methods on this interface will directly affect the provided {@link PersistentDataContainer}.
     *
     * @param container     the container to modify
     * @param interfaceType the type that represents this {@code container}
     * @return an object of type {@code interfaceType}
     */
    @NotNull
    public <T> T wrap(@NotNull PersistentDataContainer container, @NotNull Class<T> interfaceType)
    {
        return interfaceType.cast(Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, new PersistentDataContainerProxy(container, this)));
    }

    /**
     * <p>
     * Registers a {@link PersistentDataType} to be used when setting/getting data.
     * </p>
     *
     * <p>
     * Built-in types (those found within {@link PersistentDataType}) should <b>not</b> be passed to this method --
     * they are automatically registered. Overwriting them will <b>not</b> have any desired effects.
     * </p>
     *
     * @param persistentDataType the type to register.
     */
    public void registerPersistentDataType(PersistentDataType<?, ?> persistentDataType)
    {
        persistentDataTypes.put(persistentDataType.getComplexType(), persistentDataType);
    }
}
