package io.split.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;

import java.util.Map;

public class EnvVarModule extends AbstractModule {

    private final String _prefix;

    public EnvVarModule(String prefix) {
        _prefix = prefix;
    }

    @Override
    protected void configure() {
        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
            if (entry.getKey().startsWith(_prefix)) {
                String key = entry.getKey().replace(_prefix, "");
                bind(Key.get(String.class, Names.named(key))).toInstance(entry.getValue());
            }
        }
    }

}