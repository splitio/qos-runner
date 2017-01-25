package io.split.testrunner.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.inject.Singleton;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Used to find the classes that are annotated with the suite.
 */
@Singleton
public class TestsFinder {

    private final CacheLoader<CacheId, List<Method>> loader = new CacheLoader<CacheId, List<Method>>() {
        @Override
        public List<Method> load(CacheId id) throws Exception {
            List<Method> result = Lists.newArrayList();
            List<Class> classesToTest = getTestClassesOfPackage(id.suites, id.suitesPackage);
            for (Class clazz : classesToTest) {
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(Test.class)
                            && !method.isAnnotationPresent(Ignore.class)) {
                        result.add(method);
                    }
                }
            }
            return result;
        }
    };

    private static final ClassLoader CLASS_LOADER = TestsFinder.class.getClassLoader();

    /**
     * Finds the classes to test, given a list of suites and the root package.
     *
     * @param suites the Suites used to find classes (classes should be annotated with the Suites annotation)
     * @param suitesPackage the root package.
     * @return the List of classes that are annotated with any of the suites.
     * @throws IOException if failed to initialize the class loader.
     */
    @SuppressWarnings("unchecked")
    public List<Class> getTestClassesOfPackage(List<String> suites, String suitesPackage) throws IOException {
        Preconditions.checkNotNull(suites);
        Preconditions.checkArgument(!suites.isEmpty());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(suitesPackage));

        List<String> upperSuites = ImmutableList.copyOf(Lists.transform(suites, String::toUpperCase));
        return ImmutableList.copyOf(Iterables.filter(getAllClassesOfPackage(suitesPackage), clazz -> {
            if (clazz.isAnnotationPresent(Suites.class)) {
                Suites suitesAnnotation = (Suites) clazz.getAnnotation(Suites.class);
                return Lists.transform(
                        Lists.newArrayList(suitesAnnotation.value()), String::toUpperCase)
                        .stream()
                        .anyMatch(upperSuites::contains);
            }
            return false;
        }));
    }

    public List<Method> getTestMethodsOfPackage(List<String> suites, String suitesPackage) throws Exception {
        Preconditions.checkNotNull(suites);
        Preconditions.checkArgument(!suites.isEmpty());
        Preconditions.checkArgument(!Strings.isNullOrEmpty(suitesPackage));
        return loader.load(new CacheId(suites, suitesPackage));
    }

    private final static class CacheId {
        private final List<String> suites;
        private final String suitesPackage;

        private CacheId(List<String> suites, String suitesPackage) {
            this.suites = suites;
            this.suitesPackage = suitesPackage;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheId))
                return false;
            if (obj == this)
                return true;

            CacheId rhs = (CacheId) obj;
            return new EqualsBuilder().
                            append(suites, rhs.suites).
                            append(suitesPackage, rhs.suitesPackage).
                            isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                    // if deriving: appendSuper(super.hashCode()).
                            append(suites).
                            append(suitesPackage).
                            toHashCode();
        }
    }

    private String id(List<String> suites, String suitesPackage) {
        return String.format("%s-%s", suitesPackage, String.join("-", suites));
    }

    private static List<Class> getAllClassesOfPackage(String suitesPackage) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(suitesPackage));

        ClassPath classPath = ClassPath.from(CLASS_LOADER);
        return ImmutableList.copyOf(Iterables.transform(classPath.getTopLevelClassesRecursive(suitesPackage), TO_CLASS));
    }

    private static final Function<ClassPath.ClassInfo, Class> TO_CLASS = classInfo -> {
        try {
            return CLASS_LOADER.loadClass(classInfo.getName());
        } catch (ClassNotFoundException e) {
            // Should never happen.... i think.
            throw new IllegalStateException(e);
        }
    };
}
