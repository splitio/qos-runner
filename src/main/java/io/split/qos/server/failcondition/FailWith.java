package io.split.qos.server.failcondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the Fail Condition.
 *
 * If not set it will default to SimpleFailCondition
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FailWith {
    Class<? extends FailCondition> value();
}
