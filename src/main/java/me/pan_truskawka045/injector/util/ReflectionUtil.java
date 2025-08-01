package me.pan_truskawka045.injector.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class providing common reflection operations.
 * This class contains static methods for working with Java reflection API,
 * including field and method access, and generic type resolution.
 *
 * @author pan_truskawka045
 * @since 1.0
 */
@UtilityClass
public class ReflectionUtil {

    /**
     * Retrieves all fields from a class and its superclasses.
     * This method traverses the entire class hierarchy to collect all declared fields,
     * including private, protected, and public fields from the class and all its parent classes.
     *
     * @param clazz the class to retrieve fields from
     * @return a Set containing all fields from the class hierarchy, or empty set if no fields found
     * @throws NullPointerException if clazz is null
     */
    public Set<Field> getFields(Class<?> clazz) {
        Set<Field> fields = new HashSet<>();
        do {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        } while ((clazz = clazz.getSuperclass()) != null);

        return fields;
    }

    /**
     * Retrieves all methods from a class and its superclasses.
     * This method traverses the entire class hierarchy to collect all declared methods,
     * including private, protected, and public methods from the class and all its parent classes.
     *
     * @param clazz the class to retrieve methods from
     * @return a Set containing all methods from the class hierarchy, or empty set if no methods found
     * @throws NullPointerException if clazz is null
     */
    public Set<Method> getMethods(Class<?> clazz) {
        Set<Method> methods = new HashSet<>();
        do {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        } while ((clazz = clazz.getSuperclass()) != null);

        return methods;
    }
}