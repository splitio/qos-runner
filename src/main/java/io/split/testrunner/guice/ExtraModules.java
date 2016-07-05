package io.split.testrunner.guice;

import com.google.inject.AbstractModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtraModules {

    /**
     * The Extra Modules classes needed by the class under test.
     *
     * @return all the extra custom modules needed by Guice.
     */
    Class<? extends AbstractModule>[] value();
}