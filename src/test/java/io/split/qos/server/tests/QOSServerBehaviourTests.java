package io.split.qos.server.tests;

import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.QOSTestsTracker;
import org.junit.Assert;
import org.junit.Test;

public class QOSServerBehaviourTests extends BaseCaseForTest {

    @Test
    public void testBehaviourCanBeCalledAndRunsTwoTests() throws Exception {
        QOSServerBehaviour behaviour = injector().getInstance(QOSServerBehaviour.class);
        behaviour.call();
        QOSTestsTracker tracker = injector().getInstance(QOSTestsTracker.class);
        Assert.assertEquals(2, tracker.getAll().size());
        behaviour.close();
    }
}
