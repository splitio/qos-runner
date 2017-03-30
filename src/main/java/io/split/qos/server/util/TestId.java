package io.split.qos.server.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.split.testrunner.util.Util;
import org.junit.runner.Description;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public class TestId {

    private final Optional<Description> description;
    private final Optional<Method> method;
    private final String testName;
    private final String shortenedClass;

    public static TestId fromDescription(Description description) {
        return new TestId(description);
    }

    public static TestId fromMethod(Method method) {
        return new TestId(method);
    }

    private TestId(Description description) {
        this.description = Optional.of(Preconditions.checkNotNull(description));
        this.method = Optional.empty();
        this.shortenedClass = Util.shortenClass(description.getTestClass());
        this.testName = description.getMethodName();
    }

    private TestId(Method method) {
        this.description = Optional.empty();
        this.method = Optional.of(Preconditions.checkNotNull(method));
        this.shortenedClass = Util.shortenClass(method.getDeclaringClass());
        this.testName = method.getName();
    }

    @Override
    public String toString() {
        return String.format("%s#%s", shortenedClass, testName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortenedClass, testName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final TestId other = (TestId) obj;
        return Objects.equals(this.shortenedClass, other.shortenedClass)
                && Objects.equals(this.testName, other.testName);
    }

    public boolean contains(String fuzzyClassOrName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyClassOrName));
        return shortenedClass.toLowerCase().contains(fuzzyClassOrName.toLowerCase()) ||
                testName.toLowerCase().contains(fuzzyClassOrName.toLowerCase());
    }

    public boolean contains(String fuzzyClass, String fuzzyName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyClass));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyName));
        return shortenedClass.toLowerCase().contains(fuzzyClass.toLowerCase()) &&
                testName.toLowerCase().contains(fuzzyName.toLowerCase());
    }

    public String testName() {
        return testName;
    }
}
