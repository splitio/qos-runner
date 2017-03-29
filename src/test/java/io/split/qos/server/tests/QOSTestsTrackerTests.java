package io.split.qos.server.tests;

import com.google.common.util.concurrent.ListenableFuture;
import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.QOSTestsTracker;
import io.split.testrunner.junit.JUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class QOSTestsTrackerTests extends BaseCaseForTest {

    @Test
    public void testTrackingRuns() throws NoSuchMethodException {
        Method emptyMethod = this.getClass().getMethod("methodForTesting");
        Method emptyMethod2 = this.getClass().getMethod("methodForTesting2");

        JUnitRunner mockedRunner = Mockito.mock(JUnitRunner.class);
        Mockito.when(mockedRunner.isRunning()).thenReturn(true);

        JUnitRunner mockedRunner2 = Mockito.mock(JUnitRunner.class);
        Mockito.when(mockedRunner2.isRunning()).thenReturn(true);

        ListenableFuture mockedListener = Mockito.mock(ListenableFuture.class);

        QOSTestsTracker tracker = injector().getInstance(QOSTestsTracker.class);
        Assert.assertTrue(tracker.getAll().isEmpty());

        tracker.track(emptyMethod, mockedRunner, mockedListener);
        tracker.track(emptyMethod2, mockedRunner2, mockedListener);
        Assert.assertEquals(2, tracker.getAll().size());

        Mockito.when(mockedRunner.isRunning()).thenReturn(false);
        Assert.assertEquals(2, tracker.getAll().size());

        Mockito.when(mockedRunner2.isRunning()).thenReturn(false);
        Assert.assertEquals(2, tracker.getAll().size());
    }

    @Test
    public void addingOverTheSameMehtodKeepsTheCountToOne() throws NoSuchMethodException {
        Method emptyMethod = this.getClass().getMethod("methodForTesting");

        JUnitRunner mockedRunner = Mockito.mock(JUnitRunner.class);
        Mockito.when(mockedRunner.isRunning()).thenReturn(true);

        JUnitRunner mockedRunner2 = Mockito.mock(JUnitRunner.class);
        Mockito.when(mockedRunner2.isRunning()).thenReturn(true);

        ListenableFuture mockedListener = Mockito.mock(ListenableFuture.class);

        QOSTestsTracker tracker = injector().getInstance(QOSTestsTracker.class);

        tracker.track(emptyMethod, mockedRunner, mockedListener);
        tracker.track(emptyMethod, mockedRunner2, mockedListener);
        tracker.track(emptyMethod, mockedRunner, mockedListener);
        Assert.assertEquals(1, tracker.getAll().size());
    }

    public void methodForTesting() { }

    public void methodForTesting2() { }
}
