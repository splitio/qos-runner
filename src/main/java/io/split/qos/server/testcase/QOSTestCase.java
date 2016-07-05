package io.split.qos.server.testcase;

import com.google.inject.Inject;
import io.split.qos.server.failcondition.FailWith;
import io.split.qos.server.failcondition.SimpleFailCondition;
import io.split.testrunner.guice.GuiceModules;
import io.split.testrunner.TestRunner;
import io.split.qos.server.modules.QOSClientsModule;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.util.BroadcasterTestWatcher;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Base test case. All the classes should extend this.
 *
 * <p>
 *     If you feel adventurous, in your base case you could add the annotations and also add the rule
 *     and it should work. IT SHOULD.
 * </p>
 */
@RunWith(TestRunner.class)
@GuiceModules({QOSPropertiesModule.class, QOSClientsModule.class})
@FailWith(SimpleFailCondition.class)
public class QOSTestCase {

    @Inject
    @Rule
    public BroadcasterTestWatcher broadcasterTestWatcher;
}
