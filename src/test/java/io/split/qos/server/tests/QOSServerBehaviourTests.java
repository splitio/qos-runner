package io.split.qos.server.tests;

import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.QOSTestsTracker;
import io.split.testrunner.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class QOSServerBehaviourTests extends BaseCaseForTest {

    @Test
    public void testBehaviourCanBeCalledAndRunsTwoTests() throws Exception {
        QOSServerBehaviour behaviour = injector().getInstance(QOSServerBehaviour.class);
        behaviour.call();
        QOSTestsTracker tracker = injector().getInstance(QOSTestsTracker.class);
        Assert.assertEquals(2, tracker.getAll().size());
        behaviour.close();
    }

    @Test
    public void testRunAllSorting() throws Exception {
        QOSServerBehaviour behaviour = injector().getInstance(QOSServerBehaviour.class);
        QOSServerState state = injector().getInstance(QOSServerState.class);
        behaviour.scheduleTests();
        QOSTestsTracker tracker = injector().getInstance(QOSTestsTracker.class);
        Assert.assertEquals(2, tracker.getAll().size());

        List<Method> methods = behaviour.runAllNow();
        String testOneId = Util.id(methods.get(1));
        String testOneName = "testOne";
        String testTwoId = Util.id(methods.get(0));
        String testTwoName = "testTwo";

        assertThat(methods, testTwoName, testOneName);

        methods = succeed(state, behaviour, testTwoId);
        assertThat(methods, testOneName, testTwoName);

        methods = succeed(state, behaviour, testTwoId);
        assertThat(methods, testOneName, testTwoName);

        methods = succeed(state, behaviour, testOneId);
        assertThat(methods, testTwoName, testOneName);

        methods = fail(state, behaviour, testTwoId);
        assertThat(methods, testOneName, testTwoName);

        behaviour.close();
    }

    private void assertThat(List<Method> methods, String firstExpected, String secondExpected) {
        Assert.assertEquals(2, methods.size());
        Assert.assertEquals(firstExpected, methods.get(0).getName());
        Assert.assertEquals(secondExpected, methods.get(1).getName());
    }

    private List<Method> succeed(QOSServerState state, QOSServerBehaviour behaviour, String testId) {
        state.testSucceeded(testId);
        List<Method> methods = behaviour.runAllNow();
        Util.pause(1000);
        return methods;
    }

    private List<Method> fail(QOSServerState state, QOSServerBehaviour behaviour, String testId) {
        state.testFailed(testId);
        List<Method> methods = behaviour.runAllNow();
        Util.pause(1000);
        return methods;
    }
}
