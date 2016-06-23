package io.split.qos.server.tests;

import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.util.Util;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.mockito.Mockito;

public class QOSServerStateTests extends BaseCaseForTest {

    @Test
    public void pauseAndResume() {
        QOSServerState state = injector().getInstance(QOSServerState.class);
        Assert.assertFalse(state.isActive());
        Assert.assertTrue(state.isPaused());
        Assert.assertNotNull(state.pausedSince());
        Assert.assertNull(state.activeSince());

        state.resume("Me");
        Assert.assertFalse(state.isPaused());
        Assert.assertTrue(state.isActive());
        Assert.assertEquals("Me", state.who());
        Assert.assertNull(state.pausedSince());
        Assert.assertNotNull(state.activeSince());

        state.pause("Me2");
        Assert.assertFalse(state.isActive());
        Assert.assertTrue(state.isPaused());
        Assert.assertEquals("Me2", state.who());
        Assert.assertNotNull(state.pausedSince());
        Assert.assertNull(state.activeSince());
    }

    @Test
    public void registerTests() throws NoSuchMethodException {
        long now = System.currentTimeMillis();
        Util.pause(500);
        Description mock = Mockito.mock(Description.class);
        Mockito.when(mock.getTestClass()).thenAnswer(invocation -> QOSServerState.class);
        Mockito.when(mock.getMethodName()).thenReturn("mock");
        QOSServerState state = injector().getInstance(QOSServerState.class);
        state.registerTest(mock);

        Assert.assertEquals(1, state.tests().size());
        state.testSucceeded(mock);

        QOSServerState.TestStatus status = state.tests().get(Util.id(mock));
        Assert.assertTrue(status.succeeded());
        Assert.assertTrue(status.when() > now);
        Assert.assertTrue(state.lastGreen() > now);

        state.testFailed(mock);
        status = state.tests().get(Util.id(mock));
        Assert.assertFalse(status.succeeded());
        Assert.assertNull(state.lastGreen());

    }
}
