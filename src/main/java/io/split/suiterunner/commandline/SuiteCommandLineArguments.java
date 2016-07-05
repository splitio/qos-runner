package io.split.suiterunner.commandline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

/**
 * Parses command line arguments for the suites to run.
 */
public class SuiteCommandLineArguments {

    /**
     * @return Where the properties file resides.
     */
    public String conf() {
        return conf;
    }

    /**
     * @return Suites to run (for running suites).
     */
    public List<String> suites() {
        return Arrays.asList(suites.split(","));
    }

    /**
     * @return How many tests in parallel can be run.
     */
    public int parallel() {
        return parallel;
    }

    /**
     * @return root package.
     */
    public String suitesPackage() {
        return suitesPackage;
    }

    @Parameter(names = "-suites", description = "Comma delimited suites to run", required = true)
    private String suites = "";

    @Parameter(names = "-suitesPackage", description = "Base package to grab the tests", required = true)
    private String suitesPackage = "";

    @Parameter(names = "-parallel", description = "How many tests in parallel are going to run")
    private Integer parallel = 1;

    @Parameter(names = "-conf", description = "Path to the properties file if exists", required = true)
    private String conf = "";

    /**
     * Call this with the command line arguments to parse them.
     *
     * @param commandLineArgs the command line arguments
     * @return an instances with the arguments parsed.
     */
    public static SuiteCommandLineArguments initialize(String[] commandLineArgs) {
        Preconditions.checkNotNull(commandLineArgs);

        SuiteCommandLineArguments result = new SuiteCommandLineArguments();
        JCommander jCommander = new JCommander();
        jCommander.setAcceptUnknownOptions(true);
        jCommander.addObject(result);
        jCommander.parse(commandLineArgs);
        return result;
    }
}
