package io.split.qos.server.tests;

import com.google.common.util.concurrent.ListenableFuture;
import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.QOSTestsTracker;
import io.split.qos.server.testrunner.QOSTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class QOSTestsTrackerTests extends BaseCaseForTest {

    @Test
    public void testTrackingRuns() throws NoSuchMethodException {
        Method emptyMethod = this.getClass().getMethod("methodForTesting");
        Method emptyMethod2 = this.getClass().getMethod("methodForTesting2");

        QOSTestRunner mockedRunner = Mockito.mock(QOSTestRunner.class);
        Mockito.when(mockedRunner.isRunning()).thenReturn(true);

        QOSTestRunner mockedRunner2 = Mockito.mock(QOSTestRunner.class);
        Mockito.when(mockedRunner2.isRunning()).thenReturn(true);

        ListenableFuture mockedListener = Mockito.mock(ListenableFuture.class);

        QOSTestsTracker tracker = injector().getInstance(QOSTestsTracker.class);
        Assert.assertTrue(tracker.getAll().isEmpty());
        Assert.assertTrue(tracker.getNotRunning().isEmpty());

        tracker.track(emptyMethod, mockedRunner, mockedListener);
        tracker.track(emptyMethod2, mockedRunner2, mockedListener);
        Assert.assertEquals(2, tracker.getAll().size());
        Assert.assertTrue(tracker.getNotRunning().isEmpty());

        Mockito.when(mockedRunner.isRunning()).thenReturn(false);
        Assert.assertEquals(2, tracker.getAll().size());
        Assert.assertEquals(1, tracker.getNotRunning().size());

        Mockito.when(mockedRunner2.isRunning()).thenReturn(false);
        Assert.assertEquals(2, tracker.getAll().size());
        Assert.assertEquals(2, tracker.getNotRunning().size());
    }

    @Test
    public void addingOverTheSameMehtodKeepsTheCountToOne() throws NoSuchMethodException {
        Method emptyMethod = this.getClass().getMethod("methodForTesting");

        QOSTestRunner mockedRunner = Mockito.mock(QOSTestRunner.class);
        Mockito.when(mockedRunner.isRunning()).thenReturn(true);

        QOSTestRunner mockedRunner2 = Mockito.mock(QOSTestRunner.class);
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
