package me.pan_truskawka045.injector;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for dependency injection modules.
 * Modules manage object registration, creation, and binding for the injector.
 * This class is thread-safe.
 */
@Log4j2
public abstract class Module {

    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    @Setter
    private Injector injector;

    /**
     * Called when the module is registered to perform any initialization logic.
     */
    public abstract void init();

    /**
     * Registers an object with optional base class and binding classes.
     *
     * @param object            the object to register
     * @param registerBaseClass whether to register the base class
     * @param bindClazz         additional classes to bind the object to
     * @param <T>               the type of the object
     * @return the registered object
     */
    public synchronized <T> T register(T object, boolean registerBaseClass, Class<?>... bindClazz) {
        if (object.getClass().isAnnotationPresent(Bind.class)) {
            Bind bind = object.getClass().getAnnotation(Bind.class);
            if (bind.registerBaseClass()) {
                instances.put(object.getClass(), object);
            }
            for (Class<?> clazz : bind.value()) {
                instances.put(clazz, object);
            }
        }
        if (bindClazz != null) {
            for (Class<?> clazz : bindClazz) {
                instances.put(clazz, object);
            }
        }
        if (registerBaseClass) {
            instances.put(object.getClass(), object);
        }
        return object;
    }

    /**
     * Registers an object with specific binding classes.
     *
     * @param object    the object to register
     * @param bindClazz additional classes to bind the object to
     * @param <T>       the type of the object
     * @return the registered object
     */
    public synchronized <T> T register(T object, Class<?>... bindClazz) {
        return register(object, false, bindClazz);
    }

    /**
     * Registers an object, using {@link Bind} annotation if present.
     *
     * @param object the object to register
     * @param <T>    the type of the object
     * @return the registered object
     */
    public synchronized <T> T register(T object) {
        if (object.getClass().isAnnotationPresent(Bind.class)) {
            Bind bind = object.getClass().getAnnotation(Bind.class);
            if (bind.registerBaseClass()) {
                instances.put(object.getClass(), object);
            }
            for (Class<?> clazz : bind.value()) {
                instances.put(clazz, object);
            }
            return object;
        }
        instances.put(object.getClass(), object);
        return object;
    }

    /**
     * Creates and registers an instance of the given class, resolving constructor dependencies.
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the object
     * @return the created and registered object
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public synchronized <T> T create(Class<T> clazz) {
        Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
        if (declaredConstructors.length == 0) {
            log.error("No constructors found for class {}", clazz.getName());
            return null;
        }
        Constructor<?> constructor = declaredConstructors[0];
        constructor.setAccessible(true);

        Object object;

        if (constructor.getParameterCount() == 0) {
            object = constructor.newInstance();
        } else {
            Object[] params = new Object[constructor.getParameterCount()];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                params[i] = injector.find(parameterTypes[i]);
            }
            object = constructor.newInstance(params);
        }

        return register((T) object);
    }

    /**
     * Registers a submodule with the parent injector.
     *
     * @param module the submodule to register
     */
    public synchronized void registerSubmodule(Module module) {
        injector.registerModule(module);
    }

    /**
     * Returns a thread-safe snapshot of all registered instances.
     * This method is safe to iterate over even while registrations are happening.
     *
     * @return a snapshot of the instances map
     */
    protected synchronized Map<Class<?>, Object> getInstances() {
        return Map.copyOf(instances);
    }
}