package io.split.qos.server;

import com.google.inject.Injector;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;
import org.junit.rules.RuleChain;

public class BaseCaseForTest {
    private final DropwizardAppRule<QOSServerConfigurationForTest> appRule =
        new DropwizardAppRule(QOSServerApplicationForTest.class, "conf/qos.test.server.yml");

    @Rule
    public RuleChain chain = RuleChain.outerRule(appRule);

    public Injector injector() {
        return QOSServerApplicationForTest.injector;
    }

}
