# QOS-Runner

QOS-Runner is a JUnit Test runner for periodically running tests that has an interface with Slack.

This framework is extensively being used at [Split.io](http://www.split.io/):
* For running integration tests for all our different [SDK clients in all our languages](http://docs.split.io/docs/sdk-overview)
* For running integration tests for our different backend servers.
* For making sure that our different [integrations](http://docs.split.io/docs/integrations-overview) are working as expected.

In a nutshell, it will run a set of JUnit Tests periodically. It's possible to chat with a Slack Bot, asking which tests succeeded, which ones failed, when was the last cycle where all the tests succeeded, force run a particular test, force run all tests and more (check Commands Section). When a test fails, it will report the error on a slack channel.

The advantage of this approach is that everyone in your company can check the status of the running tests and be aware if there is a failure.

## Creating a Test

A JUnit test class simply has to extend [QOSTestCase](https://github.com/splitio/qos-runner/blob/438472cdc8b006ebcf8389266580d725f4299064/src/main/java/io/split/qos/server/testcase/QOSTestCase.java) and be marked with the [Suites](https://github.com/splitio/qos-runner/blob/2aab861af237e34a9c1009bd5b5ae1f98ad09bb5/src/main/java/io/split/testrunner/util/Suites.java) annotation. That's all.

```
@Suites("SMOKE_FOR_TEST")
public class SmokeExampleTest extends QOSTestCase {

    @Test
    public void testOne() {
    }
}
```
If you cannot extend _QOSTestCase_, simply in your base case declare the rules and the annotations that are defined in _QOSTestCase_.

## QOS Server Configuration

### Server YAML

QOS-Runner is a [DropWizard restful server](http://www.dropwizard.io/1.0.5/docs/), so it needs a _yml_ configuration file:
```
serverName: qos-server

config: conf/common.properties,conf/qos.test.properties

server:
  type: simple
  applicationContextPath: /api
```
* _serverName_: Name of the server. Used by the Slack Interface to interact.
* _config_: Comma separated list of properties file where the configuration of the server resides.

### Configuration Properties File

As stated above, QOS-Runner also needs a properties file with some predefined configuration:
```
# QOS Runner could eventually interface with other messaging systems, for now set to true
SLACK_INTEGRATION=true
# Create a [Slack Bot](https://api.slack.com/bot-users) and set the auth token here
SLACK_BOT_TOKEN=[THE_TOKEN]
# Channel were all the verbose communication will go, i.e. every time a test succeeds.
SLACK_VERBOSE_CHANNEL=web-e2e-verbose
# Channel were all the digest communication will go, i.e when a test fails or recovers.
SLACK_DIGEST_CHANNEL=web-e2e-digest
# If set to true, it will broadcast to the verbose channel every time a test succeeds.
BROADCAST_SUCCESS=true

# How mamy times a test has to fail consecutively to be considered failed.
CONSECUTIVE_FAILURES=2
# Timeout for waiting for a test to end.
TIMEOUT_IN_MINUTES=20
# How many tests the QOS-Runner will run in parallel.
PARALLEL_TESTS=10
# Wait time once the servers is shutting down for tests to finish.
SHUTDOWN_WAIT_IN_MINUTES=10
# Delay between a test finishes and the same test runs again. This determines the cycle time.
DELAY_BETWEEN_IN_SECONDS=300
# Delay between a failed test finishes and the same test runs again.
DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL=5
# If true, tests will be spread accross DELAY_BETWEEN_IN_SECONDS. i.e if you have 50 tests and cycle is 300 seconds, more or less every 6 seconds a test will run
SPREAD_TESTS=true

# Comma separated list of Suites to run (@Suites tag)
SUITES=SMOKE_FOR_TEST
# Base package where all the JUnit tests reside.
SUITES_PACKAGE=io.split
```

### Starting the Sever.

Simply run the Main class: _io.split.qos.server.QOSServerApplication_.  
With the program arguments: _server path_to_the_yml_

For example:
```
Main class: io.split.qos.server.QOSServerApplication
Program Arguments: server conf/qos.test.server.yml
```

## Slack Commands

QOS-Runner responds to commands in the Slack Console. For the examples, we will assume that the Slack Bot is names _@stagingbot_ and the _serverName_ defined in the yaml is _JAVA_.

### TEST SUCCEEDED

### INFO

```
@bot
