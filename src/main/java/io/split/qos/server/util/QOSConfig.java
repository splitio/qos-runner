package io.split.qos.server.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for running tests from the IDE.
 *
 * Annotate the class with this, pointing to the properties file.
 * i.e @QOSConfig("conf/qos.java.properties")
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QOSConfig {
    String value();
}
