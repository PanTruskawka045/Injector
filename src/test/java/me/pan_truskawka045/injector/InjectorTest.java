package me.pan_truskawka045.injector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InjectorTest {
    private Injector injector;

    @BeforeEach
    void setUp() {
        injector = new Injector();
    }

    @Test
    @DisplayName("inject should inject dependencies into fields annotated with @Inject")
    void injectInjectsDependencies() {
        class Service {
        }
        class Client {
            @Inject
            Service service;
        }
        Service service = new Service();
        injector.registerModule(new Module() {
            @Override
            public void init() {
                register(service);
            }
        });
        Client client = new Client();
        injector.inject(client);
        assertSame(service, client.service);
    }

    @Test
    @DisplayName("inject should not inject into fields without @Inject annotation")
    void injectDoesNotInjectWithoutAnnotation() {
        class Service {
        }
        class Client {
            Service service;
        }
        Client client = new Client();
        injector.inject(client);
        assertNull(client.service);
    }

    @Test
    @DisplayName("inject should handle null object gracefully")
    void injectHandlesNullObject() {
        assertNull(injector.inject(null));
    }

    @Test
    @DisplayName("init should invoke methods annotated with @Init")
    void initInvokesInitMethods() {
        class Client {
            boolean initialized = false;

            @Init
            void initialize() {
                initialized = true;
            }
        }
        Client client = new Client();
        injector.init(client);
        assertTrue(client.initialized);
    }

    @Test
    @DisplayName("init should not invoke methods without @Init annotation")
    void initDoesNotInvokeNonInitMethods() {
        class Client {
            boolean called = false;

            void notInit() {
                called = true;
            }
        }
        Client client = new Client();
        injector.init(client);
        assertFalse(client.called);
    }

    @Test
    @DisplayName("init should return null when object is null")
    void initReturnsNullForNullObject() {
        assertNull(injector.init(null));
    }

    @Test
    @DisplayName("registerModule should add module and call its lifecycle methods")
    void registerModuleAddsModuleAndCallsLifecycle() {
        class TestModule extends Module {
            boolean setInjectorCalled = false;
            boolean initCalled = false;

            @Override
            public void setInjector(Injector injector) {
                setInjectorCalled = true;
            }

            @Override
            public void init() {
                initCalled = true;
            }
        }
        TestModule module = new TestModule();
        injector.registerModule(module);
        assertTrue(module.setInjectorCalled);
        assertTrue(module.initCalled);
    }
}

