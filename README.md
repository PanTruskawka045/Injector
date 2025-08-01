# 🚀 Java Dependency Injection Framework

A lightweight, annotation-based dependency injection container for Java applications. This framework provides a simple yet powerful way to manage object dependencies and lifecycle in your applications.

## ✨ Features

- 🎯 **Field Injection** - Inject dependencies using `@Inject` annotation
- 🔧 **Constructor Injection** - Automatic constructor parameter resolution
- 📦 **Module System** - Organize dependencies with custom modules
- 🏷️ **Interface Binding** - Bind implementations to interfaces using `@Bind`
- 🎉 **Post-Construction Initialization** - Execute methods after injection with `@Init`
- 🔄 **Lifecycle Management** - Automatic injection and initialization of all registered objects
- 🌳 **Hierarchical Modules** - Support for submodules and complex dependency graphs

## 📋 Requirements

- Java 8 or higher
- Lombok (for annotation processing)
- Log4j2 (for logging)

## 🏗️ Installation

Download the repository, and place the code in your project.

## 🚀 Quick Start

### Basic Usage

```java
    private final Injector injector = new Injector();

    //Some kind of initialization method
    @Override
    public void onEnable() {
        
        //Register a plugin class
        injector.register(this);
        
        //Register an object and bind it to a specific class
        injector.register(Bukkit.getWorlds().iterator().next(), World.class);

        //Register a module
        injector.registerModule(new SomeKindOfModule());
        
        //Create an instance of a class and register it
        //This will inject dependencies in the constructor
        injector.create(MyService.class);

        injector.injectAll();
        injector.initAll();
    }
    
    class SomeKindOfModule extends Module {
        
        @Override
        public void init(){
            create(Database.class);
        }
        
    }
    
    //Class will be registered as `MyService.class` and `MyServiceImpl.class`
    @Bind({MyService.class})
    class MyServiceImpl implements MyService {
        
        //This will be injected with constructor injection
        private final World world;
        
        //This will be injected with field injection
        @Inject
        private Database database;
        
        public MyService(World world) {
            this.world = world;
        }
        
    }

```

## 📚 API Reference

### Core Classes

#### `Injector`
The main dependency injection container.

- `register(T object)` - Register an object instance
- `register(T object, Class<?>... bindClazz)` - Register with specific bindings
- `create(Class<T> clazz)` - Create and register a new instance
- `inject(T object)` - Inject dependencies into an object
- `init(T object)` - Call @Init methods on an object
- `find(Class<T> clazz)` - Find a registered instance
- `registerModule(Module module)` - Register a module
- `injectAll()` - Inject dependencies into all registered objects
- `initAll()` - Initialize all registered objects

#### `Module`
Abstract base class for organizing related dependencies.

- `register(T object)` - Register an object in this module
- `create(Class<T> clazz)` - Create and register an instance
- `registerSubmodule(Module module)` - Register a child module

### Annotations

#### `@Inject`
Mark fields for dependency injection.

```java
@Inject
private MyService service;
```

#### `@Bind`
Bind a class to specific interfaces or parent classes.

```java
@Bind({ServiceInterface.class, BaseService.class})
public class ServiceImpl implements ServiceInterface extends BaseService {
    // Implementation
}
```

Parameters:
- `value()` - Array of classes to bind to
- `registerBaseClass()` - Whether to also register the implementing class (default: true)

#### `@Init`
Mark methods to be called after dependency injection.

```java
@Init
public void initialize() {
    // Post-injection initialization
}
```

## 🧪 Testing

The framework includes comprehensive unit tests demonstrating various usage patterns:

```bash
./gradlew test
```

## 📝 License

This project is licensed under the MIT License.

## 🔍 Examples Repository

For more comprehensive examples and use cases, check out the test files in `src/test/java/` which demonstrate:

- Basic field injection
- Constructor injection with dependencies
- Module registration and organization
- Interface binding scenarios
- Lifecycle management with @Init methods
- Complex dependency graphs

---

Made with ❤️ by pan_truskawka045
