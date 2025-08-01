package me.pan_truskawka045.injector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class for binding in a dependency injection context.
 * <p>
 * This annotation is intended to be used on types (classes or interfaces) that should be registered
 * for dependency injection. The {@code value} parameter specifies the types to which the annotated
 * class should be bound. The {@code registerBaseClass} parameter determines whether the base class
 * should also be registered.
 * </p>
 *
 * <pre>
 * Example usage:
 * {@code
 * @Bind({MyService.class})
 * public class MyServiceImpl implements MyService { ... }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Bind {

    /**
     * Specifies the types to which the annotated class should be bound.
     *
     * @return an array of classes to bind to this implementation
     */
    Class<?>[] value();

    /**
     * Indicates whether the base class should also be registered.
     * Defaults to {@code true}.
     *
     * @return {@code true} if the base class should be registered, {@code false} otherwise
     */
    boolean registerBaseClass() default true;

}