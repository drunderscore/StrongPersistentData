# StrongPersistentData
Strongly-typed objects that operate on Bukkit persistent data.

Licensed under GNU GPL Version 3

## Capabilities
This is an API that will wrap `PersistentDataContainer` instances to an interface.
The methods on this interface directly modify the wrapped container, with no code
-- just method definitions, and sometimes annotations.

## Usage

#### Construction
An instance of `StrongPersistentData` is needed. This will allow us to
`wrap` a `PersistentDataContainer`, and register any custom `PersistentDataType`.
You must also pass the constructing plugin instance, which is required for
creating all the `NamespacedKey` for your container.

```java
final var strongPersistentData = new StrongPersistentData(this);
```

#### Example Interface of All Features
<details>
  <summary>TestType.java</summary>

```java
import org.bukkit.Location;
import xyz.jame.strongpersistentdata.ExplicitName;
import xyz.jame.strongpersistentdata.ExplicitType;
import xyz.jame.strongpersistentdata.NullAction;
import xyz.jame.strongpersistentdata.Wrap;

import java.util.Optional;
import java.util.OptionalInt;

public interface TestType
{
    // This is a getter. It has a non-void return value, and zero parameters.
    // This will correspond to the key "myplugin:cool"
    String cool();

    // This is a setter. It has a void return value, and one parameter.
    // This will correspond to the key "myplugin:cool"
    void cool(String value);

    // This is a getter with a longer, Java standard (camel case) method name.
    // A NamespacedKey cannot having capitalized characters, and the standard is snake case.
    // This will correspond to the key "myplugin:convert_my_camels_to_snakes"
    String convertMyCamelsToSnakes();

    // This is a setter with a longer, Java standard (camel case) method name.
    // A NamespacedKey cannot having capitalized characters, and the standard is snake case.
    // This will correspond to the key "myplugin:convert_my_camels_to_snakes"
    void convertMyCamelsToSnakes(String value);

    // This is a getter with a primitive return value.
    // Attempting to call this while it does not exist on the container will explicitly throw an exception.
    // A has method must be used to ensure this type exists.
    // A better solution would be to use an optional type, or a primitive wrapper (which is nullable)
    // This will correspond to the key "myplugin:health"
    int health();

    // This is a setter with a primitive parameter.
    // It would not be possible to pass null to the corresponding primitive data type,
    // so you would have to use a primitive wrapper instead.
    // This will correspond to the key "myplugin:health"
    void health(int value);

    // This is a has method. It starts with "has", has a primitive boolean return value, and zero parameters.
    // This will check the existence of the key, returning true if it exists, and false otherwise.
    // If this has no setter, this method _MUST_ be annotated with @ExplicitType.
    // This will correspond to the key "myplugin:health"
    boolean hasHealth();

    // This is a specialized Optional getter.
    // Such specialized getters exists for OptionalInt, OptionalLong, and OptionalDouble.
    // If the key does not exist on the container, the return value will be the empty. Otherwise, it will contain the
    // value.
    // This will correspond to the key "myplugin:better_health"
    OptionalInt betterHealth();

    // This is a setter with a primitive parameter.
    // There is no different between this and the above health getter.
    // This will correspond to the key "myplugin:better_health"
    void betterHealth(int value);

    // This is a remover. It starts with "remove", has a void return value, and zero parameters.
    // This will remove the key from the container.
    // This will correspond to the key "myplugin:better_health"
    void removeBetterHealth();

    // This is a getter, with a custom type.
    // The type must have been already registered with StrongPersistentData#registerPersistentDataType
    // This will correspond to the key "myplugin:destination"
    Location destination();

    // This is a setter, with a custom type.
    // The type must have been already registered with StrongPersistentData#registerPersistentDataType
    // This will correspond to the key "myplugin:destination"
    void destination(Location value);

    // This is an Optional getter, with a custom type.
    // The type must have been already registered with StrongPersistentData#registerPersistentDataType
    // If the key does not exist on the container, the return value will be the empty. Otherwise, it will return
    // an Optional containing the value.
    // Because of type-erasure in Java, there _MUST_ also be a setter to use this getter.
    // This will correspond to the key "myplugin:spawn"
    Optional<Location> spawn();

    // This is a setter.
    // There is no different between this and the above cool setter.
    // This will correspond to the key "myplugin:spawn"
    void spawn(Location value);

    // This is an Optional getter, without a setter.
    // In cases where you do not want a setter, but still use an Optional getter, you must annotate with @ExplicitType
    // This will correspond to the key "myplugin:no_setter"
    @ExplicitType(String.class)
    Optional<String> noSetter();

    // This is a getter, with an explicit name.
    // Instead of deducing the key name from the method name, it will instead use what is provided by the @ExplicitName
    // annotation.
    // This will correspond to the key "myplugin:my_name_does_not_suck", instead of "myplugin:my_name_sucks"
    @ExplicitName("my_name_does_not_suck")
    String myNameSucks();

    // This is a setter, with an explicit name.
    // Instead of deducing the key name from the method name, it will instead use what is provided by the @ExplicitName
    // annotation.
    // This will correspond to the key "myplugin:my_name_does_not_suck", instead of "myplugin:my_name_sucks"
    @ExplicitName("my_name_does_not_suck")
    void myNameSucks(String value);

    // This is a getter.
    // There is no different between this and the above cool getter.
    // This will correspond to the key "myplugin:player_owner"
    String playerOwner();

    // This is a setter, with a custom null action.
    // This setter is annotated with @NullAction.
    // If NullAction.Rule.Remove is specified, passing null to this setter will remove the key from the
    // container, instead of passing null as the value to a PersistentDataType
    // This will correspond to the key "myplugin:player_owner"
    @NullAction(NullAction.Rule.Remove)
    void playerOwner(String value);

    // This is a wrapped getter.
    // The type at the key is expected to be a container, and will automatically be wrapped to match the
    // return value.
    // This will correspond to the key "myplugin:tootsie_roll_wrapper"
    @Wrap
    TestType tootsieRollWrapper();

    // This is a wrapped setter.
    // The parameter should be a wrapped type, which will automatically be unwrapped (taking its underlying container),
    // and set it into this container at the key.
    // This will correspond to the key "myplugin:tootsie_roll_wrapper"
    @Wrap
    void tootsieRollWrapper(TestType value);
}
```
</details>

#### TODO
* Better caching
  * Once we execute a method, we shouldn't have to deduce and construct it's key
  (and a ton of other stuff) again.
* Container arrays
