package io.split.methodrunner.commandline;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Preconditions;

import java.lang.reflect.Method;

public class MethodCommandLineArguments {

    private static Class<?> hackClass;

    @Parameter(names = "-clazz", description = "Class to run", converter = ClassConverter.class, required = true)
    private Class<?> clazz;

    @Parameter(names = "-test", description = "Test to run", converter = MethodConverter.class, required = true)
    private Method test;

    @Parameter(names = "-quantity", description = "How many times the test is going to run")
    private Integer quantity = 1;

    @Parameter(names = "-parallel", description = "How many tests in parallel are going to run")
    private Integer parallel = 1;

    @Parameter(names = "-conf", description = "Path to the properties file if exists", required = true)
    private String conf = "";

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
    public String conf() {
        return conf;
    }

    public static MethodCommandLineArguments initialize(String[] commandLineArgs) {
        Preconditions.checkNotNull(commandLineArgs);

        MethodCommandLineArguments result = new MethodCommandLineArguments();
        JCommander jCommander = new JCommander();
        jCommander.setAcceptUnknownOptions(true);
        jCommander.addObject(result);
        jCommander.parse(commandLineArgs);
        return result;
    }

    public static class ClassConverter implements IStringConverter<Class<?>> {
        @Override
        public Class<?> convert(String value) {
            try {
                Class<?> theClass = Class.forName(value);
                MethodCommandLineArguments.hackClass = theClass;
                return theClass;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(String.format("Parameter -clazz should be a class, got : %s", value));
            }
        }
    }

    public static class MethodConverter implements IStringConverter<Method> {
        @Override
        public Method convert(String value) {
            try {
                Preconditions.checkArgument(hackClass != null, "Need to declare -clazz argument first");
                return hackClass.getMethod(value);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("Parameter -test should be a method of %s, got %s", hackClass.getCanonicalName(), value));
            }
        }
    }
}
