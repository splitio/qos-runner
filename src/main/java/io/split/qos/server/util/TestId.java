package io.split.qos.server.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.split.testrunner.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.runner.Description;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

public class TestId implements Comparable<TestId>{

    private final Optional<Description> description;
    private final Optional<Method> method;
    private final String testName;
    private final String shortenedClass;
    private final Class<?> clazz;

    public static TestId fromDescription(Description description) {
        return new TestId(description);
    }

    public static TestId fromMethod(Method method) {
        return new TestId(method);
    }

    private TestId(Description description) {
        this.description = Optional.of(Preconditions.checkNotNull(description));
        this.method = Optional.empty();
        this.clazz = description.getTestClass();
        this.shortenedClass = Util.shortenClass(clazz);
        this.testName = description.getMethodName();
    }

    private TestId(Method method) {
        this.description = Optional.empty();
        this.method = Optional.of(Preconditions.checkNotNull(method));
        this.clazz = method.getDeclaringClass();
        this.shortenedClass = Util.shortenClass(clazz);
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

    public String shortenedClass() {
        return shortenedClass;
    }

    private static final String SLACK_HTTPS_AUTH_URL = "https://slack.com/api/rtm.start?token=";

    public static void main(String [] args) throws IOException {
        String authToken = "xoxb-162119649299-2aAyDbgiP7QUXa585K4lskpn";
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(SLACK_HTTPS_AUTH_URL + authToken);
        HttpResponse response;
        response = httpClient.execute(request);
        System.out.print(response.getStatusLine().getStatusCode());
    }

    @Override
    public int compareTo(TestId o) {
        if (this.equals(o)) {
            return 0;
        }
        if (o == null) {
            return -1;
        }

        int compare = this.clazz.getSimpleName().compareTo(o.clazz.getSimpleName());
        if (compare != 0) {
            return compare;
        } else {
            return this.testName().compareTo(o.testName());
        }
    }
}
