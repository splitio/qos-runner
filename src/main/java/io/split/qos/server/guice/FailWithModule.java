package io.split.qos.server.guice;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import io.split.qos.server.failcondition.FailCondition;

/**
 * Simple Module to inject the failed condition.
 */
public class FailWithModule extends AbstractModule {

    private final Class<? extends FailCondition> clazz;

    public FailWithModule(Class<? extends FailCondition> clazz) {
        this.clazz = Preconditions.checkNotNull(clazz);
    }

    @Override
    protected void configure() {
        bind(FailCondition.class).to(clazz).in(Singleton.class);
    }

}
