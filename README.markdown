# Headless JS Test

Headless JS Test is a tool for automating client side javascript testing and easily
integrating it into continuous integration systems.

It provides an Ant task for using in build scripts, or you can directly use the TestRunner
java class to build your own.

HtmlUnit is used to load html test pages, and it is agnostic about what testing framework is
used.

The included spec directory contains examples of how to integrate and use the Jasmine test framework.

## How to Use

The dependencies are the headless-js-test.jar, as well as htmlunit and its dependecies.

If you use Ant see the build.xml and ivy.xml files for how to easily pull in the jar dependencies.

The project structure for headless-js-test itself is a good example of how to setup a java project
to run your tests.

Reference the build.xml in the project for how to use the run-test-pages task to run test pages
from within Ant. Integration with a Hudson project is fairly trivial, and has been tested. Make
sure to preserve each build to get a graph of test history.

Note: Both Ant and HtmlUnit can be fairly noisy on the console, see the included conf/log4j.properties
for getting the console output to be more readable during test runs.

## TestRunner

Loads html testpages and waits for all javascript to execute.
Writes test results to the specified directory, in JUnit XML Report format.

Expects a global javascript variable called 'reporter' to be available, and contain
the following data structure for the TestRunner to generate and report results correctly.

var reporter = {
  finished: true,
  reports: [
    {
      name: "Test Suite Name",
      result: {
        passed: 3,
        failed: 0,
        total:  3
      },
      filename: "TEST-TestSuiteName.xml",
      text: "<testcases></testcases>"
    }
  ]
};

The included jasmine.junit_reporter.js in spec/js can be used with Jasmine. It should be fairly
easily to use as an example for something like qunit.

For more information on JUnit XML Report Format:

http://stackoverflow.com/questions/428553/unable-to-get-hudson-to-parse-junit-test-output-xml

## run-test-pages Ant Task

The RunTestPagesTask takes two optional arguments.

* reportsDir : defaults to an empty string, defines the directory to write the test reports to.
* browserVersion: defaults to firefox3. ie6, ie7, and ie8 are also supported.
* failonerror: set to true to fail the build if there is an error or a test fails.

Use a <fileset> to collect up all your test pages and execute them with the TestRunner.
 