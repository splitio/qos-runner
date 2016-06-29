package io.split.qos.server.guice;

import com.google.inject.AbstractModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QOSGuiceModules {

    /**
     * The Guice Modules classes needed by the class under test.
     *
     * @return all the Module needed by guice to initialize.
     */
    Class<? extends AbstractModule>[] value();
}