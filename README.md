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
If you cannot extend _QOSTestCase_, simply in your base class declare the rules and the annotations that are defined in _QOSTestCase_.

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
# Create a [Slack Bot](https://api.slack.com/bot-users) and set the auth token here
SLACK_BOT_TOKEN=[THE_TOKEN]
# Channel were all the verbose communication will go, i.e. every time a test succeeds.
SLACK_VERBOSE_CHANNEL=web-e2e-verbose
# Channel were all the digest communication will go, i.e when a test fails or recovers.
SLACK_DIGEST_CHANNEL=web-e2e-digest

# How mamy times a test has to fail consecutively to be considered failed.
# Optional. Default 2
CONSECUTIVE_FAILURES=2
# Timeout for waiting for a test to end.
# Optional. Default 20
TIMEOUT_IN_MINUTES=20
# How many tests the QOS-Runner will run in parallel.
# Optional. Default 10
PARALLEL_TESTS=10
# Delay between a test finishes and the same test runs again. This determines the cycle time.
# Optional. Default 300
DELAY_BETWEEN_IN_SECONDS=300
# Delay between a failed test finishes and the same test runs again.
# Optional. Default 60
DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL=60
# When a test fails and keeps failing, it wont keep pushing notifications, instead will wait for this time until rebroadcasting
# Optional. Default 60
RE_BROADCAST_FAILURE_IN_MINUTES=60

# Comma separated list of Suites to run (@Suites tag)
SUITES=SMOKE_FOR_TEST
# Base package where all the JUnit tests reside.
SUITES_PACKAGE=io.split
# Description to show up in INFO Command
# Optional. Default -
DESCRIPTION=-
# Timezone for displaying the times on the commands.
# Optional. Default America/Los_Angeles
TIME_ZONE=America/Los_Angeles
```

### Starting the Sever.

Simply run the Main class: _io.split.qos.server.QOSServerApplication_.  
With the program arguments: _server path_to_the_yml_

For example:
```
Main class: io.split.qos.server.QOSServerApplication
Program Arguments: server conf/qos.test.server.yml
```

## [Slack Notifications] (https://github.com/splitio/qos-runner/wiki/Slack-Notifications)

QOS-Runner pushes notifications to channels when tests fail/succeeds/recovers.

## [Slack Commands] (https://github.com/splitio/qos-runner/wiki/Slack-Commands)

QOS-Runner can receive commands for pausing/resuming the tests, listing the tests, etc.

## [Configuration] (https://github.com/splitio/qos-runner/wiki/Configuration)

Using the properties files for configuring your QOS-Runner.

## [Green Command] (https://github.com/splitio/qos-runner/wiki/Green-Command)

In depth explanation of how the green command works.

## [Story] (https://github.com/splitio/qos-runner/wiki/Story)

In depth explanation of how to add stories to your tests for debugging and logging purposes.
