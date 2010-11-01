# Headless JS Test

Headless JS Test is a tool for automating client side javascript testing and easily
integrating it into continuous integration systems.

It provides an Ant task for using in build scripts, or you can directly use the TestRunner
java class to build your own.

HtmlUnit is used to load html test pages, and it is agnostic about what testing framework is
used.

## How to Use

The only dependencies are the headless-js-test.jar, as well as all the included jars for htmlunit.

The project structure for this project itself is a good example of how to setup a java project
to run your tests.

Reference the build.xml in the project for how to use the run-test-pages task to run test pages
from within Ant. Integration with a Hudson project is fairly trivial, and has been tested. Make
sure to preserve each build to get a graph of test history.

## run-test-pages Ant Task

The RunTestPagesTask takes two optional arguments.

* reportsDir : defaults to an empty string, defines the directory to write the test reports to.
* browserVersion: defaults to firefox3. ie6, ie7, and ie8 are also supported.
* failonerror: set to true to fail the build if there is an error or a test fails.

Right now the way to define was test pages are run is to select them with a fileset.
 