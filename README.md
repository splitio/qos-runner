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

