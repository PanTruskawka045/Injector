package me.pan_truskawka045.injector;

import lombok.extern.log4j.Log4j2;
import me.pan_truskawka045.injector.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thread-safe dependency injection container for managing object creation, injection, and initialization.
 * Dependency injection container for managing object creation, injection, and initialization.
 */
@SuppressWarnings("unused")
@Log4j2
public class Injector {

    private final List<Module> modules = Collections.synchronizedList(new LinkedList<>());
    private final Module module = new DefaultModule();

    public Injector() {
        registerModule(module);
        // Register the injector AFTER construction is complete
        // This prevents 'this' reference escape during construction
        synchronized(this) {
            module.register(this);
        }
    }

    /**
     * Registers a module with this injector.
     *
     * @param module the module to register
     */
    public void registerModule(Module module) {
        synchronized(this) {
            modules.add(module);
            module.setInjector(this);
        }
        // Call init() outside synchronized block to prevent deadlock
        // if init() calls back into injector methods
        module.init();
    }

    /**
     * Injects dependencies into fields annotated with {@link Inject} in the given object.
     *
     * @param object the object to inject dependencies into
     * @param <T>    the type of the object
     * @return the injected object
     */
    public synchronized <T> T inject(T object) {
        if (object == null) {
            return null;
        }
        Set<Field> fields = ReflectionUtil.getFields(object.getClass());
        fields.forEach(field -> {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    field.set(object, find(field.getType()));
                } catch (IllegalAccessException e) {
                    log.error("Failed to inject field {} in class {}", field.getName(), object.getClass().getName());
                }
            }
        });
        return object;
    }

    /**
     * Invokes methods annotated with {@link Init} on the given object.
     *
     * @param object the object to initialize
     * @param <T>    the type of the object
     * @return the initialized object
     */
    public synchronized <T> T init(T object) {
        if (object == null) {
            return null;
        }
        Set<Method> methods = ReflectionUtil.getMethods(object.getClass());
        methods.forEach(method -> {
            if (method.isAnnotationPresent(Init.class)) {
                method.setAccessible(true);
                try {
                    method.invoke(object);
                } catch (Exception e) {
                    log.error("Failed to invoke method {} in class {}", method.getName(), object.getClass().getName(), e);
                }
            }
        });

        return object;
    }

    /**
     * Injects dependencies into all registered objects.
     */
    public synchronized void injectAll() {
        // Create a safe copy of modules to avoid ConcurrentModificationException
        List<Module> modulesCopy;
        synchronized(modules) {
            modulesCopy = new ArrayList<>(modules);
        }

        modulesCopy.forEach(module -> {
            if (module instanceof DefaultModule) {
                return;
            }
            module.getInstances().forEach((aClass, o) -> {
                inject(o);
            });
        });

        module.getInstances().forEach((aClass, o) -> {
            inject(o);
        });
    }

    /**
     * Calls all {@link Init}-annotated methods on all registered objects.
     */
    public synchronized void initAll() {
        // Create a safe copy of modules to avoid ConcurrentModificationException
        List<Module> modulesCopy;
        synchronized(modules) {
            modulesCopy = new ArrayList<>(modules);
        }

        modulesCopy.forEach(module -> {
            if (module instanceof DefaultModule) {
                return;
            }
            module.getInstances().forEach((aClass, o) -> {
                try {
                    init(o);
                } catch (Exception exception) {
                    log.error("Failed to init object", exception);
                }
            });
        });

        module.getInstances().forEach((aClass, o) -> {
            try {
                init(o);
            } catch (Exception exception) {
                log.error("Failed to init object", exception);
            }
        });
    }

    /**
     * Registers an object in the default module.
     *
     * @param object the object to register
     * @param <T>    the type of the object
     * @return the registered object
     */
    public synchronized <T> T register(T object) {
        return module.register(object);
    }

    /**
     * Registers an object in the default module with specific binding classes.
     *
     * @param object    the object to register
     * @param bindClazz the classes to bind the object to
     * @param <T>       the type of the object
     * @return the registered object
     */
    public synchronized <T> T register(T object, Class<?>... bindClazz) {
        return module.register(object, bindClazz);
    }

    /**
     * Registers an object in the default module with options for base class registration and binding classes.
     *
     * @param object            the object to register
     * @param registerBaseClass whether to register the base class
     * @param bindClazz         the classes to bind the object to
     * @param <T>               the type of the object
     * @return the registered object
     */
    public synchronized <T> T register(T object, boolean registerBaseClass, Class<?>... bindClazz) {
        return module.register(object, registerBaseClass, bindClazz);
    }

    /**
     * Creates and registers an instance of the given class.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the object
     * @return the created and registered object
     */
    public synchronized <T> T create(Class<T> clazz) {
        return module.create(clazz);
    }

    /**
     * Finds a registered instance by its class.
     *
     * @param clazz the class to find
     * @param <T>   the type of the object
     * @return the found instance or null if not found
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T find(Class<T> clazz) {
        for (Module module1 : modules) {
            Map<Class<?>, Object> instances = module1.getInstances();
            Object instance = instances.get(clazz);
            if (instance != null) {
                return (T) instance;
            }
        }
        return null;
    }

    private static final class DefaultModule extends Module {

        @Override
        public void init() {

        }
    }

}