package io.split.methodrunner.commandline;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses command line arguments for tests to run.
 */
public class MethodCommandLineArguments {

    private static Class<?> hackClass;

    @Parameter(names = "-clazz", description = "Class to run", converter = ClassConverter.class, required = true)
    private Class<?> clazz = null;

    @Parameter(names = "-test", description = "Test to run", converter = MethodConverter.class, required = true)
    private Method test = null;

    @Parameter(names = "-quantity", description = "How many times the test is going to run")
    private Integer quantity = 1;

    @Parameter(names = "-parallel", description = "How many tests in parallel are going to run")
    private Integer parallel = 1;

    @Parameter(names = "-confs", description = "Path to the properties file if exists", required = true)
    private String confs = "";

    /**
     * @return the test to run.
     */
    public Method test() {
        return test;
    }

    /**
     * @return How many times the test will run. Default is 1.
     */
    public int quantity() {
        return quantity;
    }

    /**
     * @return How many tests in parallel will run. Default is 1.
     */
    public int parallel() {
        return parallel;
    }

    /**
     * @return Where the properties file resides.
     */
    public List<String> confs() {
        return Arrays
                .asList(confs.split(","))
                .stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .collect(Collectors.toList());
    }

    /**
     * Call this with the command line arguments to parse them.
     *
     * @param commandLineArgs the command line arguments
     * @return an instances with the arguments parsed.
     */
    public static MethodCommandLineArguments initialize(String[] commandLineArgs) {
        Preconditions.checkNotNull(commandLineArgs);

        MethodCommandLineArguments result = new MethodCommandLineArguments();
        JCommander jCommander = new JCommander();
        jCommander.setAcceptUnknownOptions(true);
        jCommander.addObject(result);
        jCommander.parse(commandLineArgs);
        return result;
    }

    /**
     * Converts an argument to a class.
     */
    public static class ClassConverter implements IStringConverter<Class<?>> {
        @Override
        public Class<?> convert(String value) {
            try {
                Class<?> theClass = Class.forName(value);
                MethodCommandLineArguments.hackClass = theClass;
                return theClass;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("Parameter -clazz should be a class, got : %s", value), e);
            }
        }
    }

    /**
     * Converts an argument to a method.
     */
    public static class MethodConverter implements IStringConverter<Method> {
        @Override
        public Method convert(String value) {
            try {
                Preconditions.checkArgument(hackClass != null, "Need to declare -clazz argument first");
                return hackClass.getMethod(value);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("Parameter -test should be a method of %s, got %s", hackClass.getCanonicalName(), value), e);
            }
        }
    }
}
