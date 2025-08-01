package me.pan_truskawka045.injector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleTest {
    static class Dummy {}
    static class Dummy2 extends Dummy {}
    static class Dummy3 {}

    static class TestModule extends Module {
        @Override
        public void init() {}
    }

    @Test
    @DisplayName("register should store object for its class when registerBaseClass is true")
    void registerStoresObjectForBaseClass() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        module.register(dummy, true);
        assertSame(dummy, module.getInstances().get(Dummy.class));
    }

    @Test
    @DisplayName("register should store object for provided binding classes")
    void registerStoresObjectForBindingClasses() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        module.register(dummy, Dummy2.class, Dummy3.class);
        assertSame(dummy, module.getInstances().get(Dummy2.class));
        assertSame(dummy, module.getInstances().get(Dummy3.class));
    }

    @Test
    @DisplayName("register should store object for both base and binding classes when both are specified")
    void registerStoresObjectForBaseAndBindingClasses() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        module.register(dummy, true, Dummy2.class);
        assertSame(dummy, module.getInstances().get(Dummy.class));
        assertSame(dummy, module.getInstances().get(Dummy2.class));
    }

    @Test
    @DisplayName("register should handle empty binding classes array")
    void registerHandlesEmptyBindingClasses() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        module.register(dummy);
        assertNull(module.getInstances().get(Dummy2.class));
        assertNull(module.getInstances().get(Dummy3.class));
    }

    @Test
    @DisplayName("register should not store object for base class when registerBaseClass is false")
    void registerDoesNotStoreBaseClassWhenFalse() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        module.register(dummy, false, Dummy2.class);
        assertNull(module.getInstances().get(Dummy.class));
        assertSame(dummy, module.getInstances().get(Dummy2.class));
    }

    @Test
    @DisplayName("register with @Bind annotation should store object for annotation-specified classes")
    void registerWithBindAnnotationStoresObjectForSpecifiedClasses() {
        @Bind({Dummy2.class, Dummy3.class})
        class AnnotatedDummy {}

        Module module = new TestModule();
        AnnotatedDummy dummy = new AnnotatedDummy();
        module.register(dummy);

        assertSame(dummy, module.getInstances().get(AnnotatedDummy.class));
        assertSame(dummy, module.getInstances().get(Dummy2.class));
        assertSame(dummy, module.getInstances().get(Dummy3.class));
    }

    @Test
    @DisplayName("register with @Bind annotation and registerBaseClass false should not store base class")
    void registerWithBindAnnotationRegisterBaseClassFalseExcludesBaseClass() {
        @Bind(value = {Dummy2.class}, registerBaseClass = false)
        class AnnotatedDummy {}

        Module module = new TestModule();
        AnnotatedDummy dummy = new AnnotatedDummy();
        module.register(dummy);

        assertNull(module.getInstances().get(AnnotatedDummy.class));
        assertSame(dummy, module.getInstances().get(Dummy2.class));
    }

    @Test
    @DisplayName("register without @Bind annotation should store object for its own class only")
    void registerWithoutBindAnnotationStoresForOwnClassOnly() {
        class PlainDummy {}

        Module module = new TestModule();
        PlainDummy dummy = new PlainDummy();
        module.register(dummy);

        assertSame(dummy, module.getInstances().get(PlainDummy.class));
        assertNull(module.getInstances().get(Dummy.class));
    }

    @Test
    @DisplayName("register with @Bind annotation should combine annotation bindings with method parameters")
    void registerWithBindAnnotationCombinesWithMethodParameters() {
        @Bind({Dummy2.class})
        class AnnotatedDummy {}

        Module module = new TestModule();
        AnnotatedDummy dummy = new AnnotatedDummy();
        module.register(dummy, true, Dummy3.class);

        assertSame(dummy, module.getInstances().get(AnnotatedDummy.class));
        assertSame(dummy, module.getInstances().get(Dummy2.class));
        assertSame(dummy, module.getInstances().get(Dummy3.class));
    }

    @Test
    @DisplayName("register with @Bind annotation should override registerBaseClass parameter")
    void registerWithBindAnnotationOverridesRegisterBaseClassParameter() {
        @Bind(value = {Dummy2.class}, registerBaseClass = false)
        class AnnotatedDummy {}

        Module module = new TestModule();
        AnnotatedDummy dummy = new AnnotatedDummy();
        module.register(dummy, false, Dummy3.class);

        assertNull(module.getInstances().get(AnnotatedDummy.class));
        assertSame(dummy, module.getInstances().get(Dummy2.class));
        assertSame(dummy, module.getInstances().get(Dummy3.class));
    }

    @Test
    @DisplayName("register should return the same object that was passed in")
    void registerReturnsOriginalObject() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        Dummy result = module.register(dummy);

        assertSame(dummy, result);
    }

    @Test
    @DisplayName("register should handle null binding classes array gracefully")
    void registerHandlesNullBindingClasses() {
        Module module = new TestModule();
        Dummy dummy = new Dummy();
        Class<?>[] nullBindings = null;

        assertDoesNotThrow(() -> module.register(dummy, nullBindings));
        assertNull(module.getInstances().get(Dummy.class));
    }

    @Test
    @DisplayName("register should overwrite existing bindings for same class")
    void registerOverwritesExistingBindings() {
        Module module = new TestModule();
        Dummy firstDummy = new Dummy();
        Dummy secondDummy = new Dummy();

        module.register(firstDummy);
        module.register(secondDummy);

        assertSame(secondDummy, module.getInstances().get(Dummy.class));
        assertNotSame(firstDummy, module.getInstances().get(Dummy.class));
    }

    @Test
    @DisplayName("create should instantiate class with no-argument constructor")
    void createInstantiatesClassWithNoArgConstructor() {
        class SimpleClass {}

        TestModule module = new TestModule();
        module.setInjector(new Injector());

        SimpleClass result = module.create(SimpleClass.class);

        assertNotNull(result);
        assertInstanceOf(SimpleClass.class, result);
        assertSame(result, module.getInstances().get(SimpleClass.class));
    }

    @Test
    @DisplayName("create should instantiate class and resolve constructor dependencies")
    void createInstantiatesClassWithDependencies() {
        class Dependency {}
        class ClassWithDependency {
            final Dependency dependency;
            ClassWithDependency(Dependency dependency) {
                this.dependency = dependency;
            }
        }

        TestModule module = new TestModule();
        Injector injector = new Injector();
        module.setInjector(injector);

        Dependency dep = new Dependency();
        injector.register(dep);

        ClassWithDependency result = module.create(ClassWithDependency.class);

        assertNotNull(result);
        assertSame(dep, result.dependency);
        assertSame(result, module.getInstances().get(ClassWithDependency.class));
    }

    @Test
    @DisplayName("create should apply @Bind annotation when creating instance")
    void createAppliesBindAnnotationWhenCreating() {
        @Bind({Dummy2.class})
        class AnnotatedClass {}

        TestModule module = new TestModule();
        module.setInjector(new Injector());

        AnnotatedClass result = module.create(AnnotatedClass.class);

        assertSame(result, module.getInstances().get(AnnotatedClass.class));
        assertSame(result, module.getInstances().get(Dummy2.class));
    }
}
