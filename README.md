ITER
====

An extensible tool for search-based system testing of Cloud applications.
  
This tool implements an evolutionary process that is specifically designed to incrementally build test suites for system testing of applications that run in clouds under the *Infrastructure-as-a-Service (IaaS)* paradigm.
ITER leverages a powerful test driver, called [AUToCLES][1], that automates the deployment of cloud-based applications that are formally specified through a *service manifest* file, the generation of the workload that is specified by an [Apache JMeter][2] *test plan*, and the collection of the data inside a private *memcached* service.
AUToCLES exposes a Web user interface and a REST API that ITER binds to schedule test executions and to retrieve published test execution data.

The theory underlying ITER is described in some research papers (see the References Section at the end of this file) and is briefly summarized in the next Section.

The architecture of the tool, its main features, the intended users and the available extensions are described in a paper submitted to the Formal Demo track of the [International Conference on Software Engineering (ICSE'14)][3]. The next sections summarize these aspects, and we remand to the code for a detailed understanding of the tool's implementation.

A short video showcases how the ITER tool works, and it can be found <a href="http://www.youtube.com/watch?feature=player_embedded&v=Xk24Sf8Ucig" target="_blank">on YouTube</a>.
The video shows the main motivations for developing the tool, its main features. In the demo we use the implementation of the iterative test suite refinement methods that we proposed in [13].
The demo shows also how the tool can be extended by providing new (or customized) implementations of the different key elements that comprise its architecture.

Other videos, more focused on each of the main points of the tool, will be linked to this page in the future.

# Some Theory

System-testing is a wide topic, and it can be implemented in a multitude of ways.
We decided to adopt an automatic and iterative approach to generate and execute test cases.
We focus on a particular for of test cases that is suitable to test cloud-application *elasticity*.
In other words, we are mainly focused to test if the cloud-application are able to adapt to the input workload as their developer expect.
  
Test cases are composed of the following ingredients:  
1) A formal description of the system under test that specifies all the settings and data to automatically deploy the whole SUT. For example, the type and flavor of the virtual machines that comprise the elastic application, the software components that run on each of the as well as their customization and start-up parameters.  

2) A formal description of possible user sessions (sequences of requests issued from the clients). We assume the availability of a test dataset to be used during the test execution or the ability of the clients to generate data on the fly (for example, randomly).

3) A formal specification of workload that we will use to stress the application. We encode this as time depend functions that compute at each time the amount of user sessions (or clients) that are concurrently active.

4) A set of assertions over the collected test execution data. In particular, the assertions encodes conditions over the time varying variables that are collected during the run by the SUT and the platform. For this reason, we assume that SUT can monitor and expose the needed variables.

The test suite is then generated by instantiating any combination of these elements; however, in practice we fix 1, 2 and 4, and only let 3 to vary.
In this way, we generate easily different test cases that target the very same deployment for the SUT by applying different patterns of workload.

## A Simple Example

Imagine that we need to test an elastic n-tier Web application that for example implement and auction site.
The application is composed of various components that are run by different virtual machines. For example, the load balancer and the monitoring component run in a medium instance, the web server and the application server in a small instance and the database in a large instance. Web and application servers can dynamically added and removed. An additional small instance runs the control logic that decides when to add and remove the instances of these machines. All these information (and something more) are stored inside the service manifest (1). An example of such file can be downloaded from [here](doodle-service-manifest.xml).

To stress the application we define two types of user sessions, namely *buyers* and *sellers*, that defined (possibly randomized) sequences of users request. For example, `login`, `search product`, `bid`, `buy`, and so on. The logic of user sessions is captured inside a JMeter test plan, a widely known workload generator (2). An example of such file can be downloaded from [here](http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx), and it can be open with the JMeter tool (version 2.9 with GUI) after installing the extensions available [here](http://jmeter-plugins.org/wiki/StandardSet/).

We decide to mimic periodic trends for both the types of user sessions, so we model the amount of concurrent active users by two sine functions.
For example, we define that the amount of buyer sessions behaves according to a sine wave that passes from 0 to 100 users over a period of 30 mins, starting from 0. Similarly we define that seller sessions start from 10 and go up to 50 over a period of 1 hour. These workload are encoded in simple trace files that are obtained by evaluating the wave functions in each second (3). An example of such trace file can be downloaded from [here](http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-trace.csv).

Finally, we define two assertions the first is one the number of web/applications servers that must never become smaller than one, and the second is about the average response time of the user requests that should never exceed 2 seconds. Assuming the availability of these data we encode the assertion logic inside Java code (4). Available assertions can be found in the `at.ac.tuwien.iter.services.impl.assertions` package.

# Target Users

This tool targets all the persons that should/must/would evaluate the quality of software of cloud-based applications. In particular, we target software developers and testers.

# Usage Scenarios

## Test case generation (Main Scenario)

The main scenario is the one of incremental generation of test suite. A tester prepares the service manifest, user sessions definition and the assertions, and she configures the initial test case generation and evolution policy. Then the tool is started with additional configurations that define for example the space of the search, the amount of parallelism for executing the tests and test report file that must be produced. The tool starts by creating and executing some initial (random) tests, then evolves the test suite, and it runs until it completed the search or an exception is generated. During the run, partial results and executed test cases are stored in the test report file. At the end of the run, the test report contains the test suites and all the data about the test cases ran.

## Bootstrap

System-testing is an activity that may take a long time, so starting a new search every time may be tedious (note that we are not talking about regression testing!). Furthermore, many times the evolution process depends on previously collected data, and if those are available the process can be bootstrapped.
ITER allows search to be bootstrapped by providing a test report as input. When ITER uses the bootstrap, it does not repeat the test execution but it uses the available data to evaluate from all the provided assertions. This process speeds up the search algorithm, but it can also be used to run different search algorithms by starting all of them form the same state.

## Dry Run

ITER's default search policy is to stop without making any evolutionary step. This can be used in combination with the bootstrap when new assertions are defined. In this case, the new assertions can be evaluated against an input dataset to produce an updated test report without the need of re-executing the tests.

## Regression

ITER can be configured also to run an input set of tests.
By feeding the tool with the previously executed tests, it indeed implements a for of regression test.  

**Note:** Some of the features that are required to  implement this scenario are currently under-development.

# Tool Features

At the moment ITER offers several interesting features, and many more will come in the next future.
The most important are listed and briefly described below. 

- **Bootstrap** ITER can be bootstrapped with previously collected test executions. At the moment this is implemented by feeding the tool with a test report file in the XML format.  

- **Generation of the initial test suite**. Usually search algorithms are started by randomly selecting individuals.
ITER can be used to generate (pseudo-)randomly an initial set of test inputs.
Users can decide on the amount of individuals to generate and the strategy to generate them.
At the moment the are two implementations of this feature:
	- Random. The test set is generated randomly;  
	- LHS. The test set is generate by sampling the input space using the Latin Hypercube Sampling.  

- **Parallel Test Execution**. ITER enables testers to run in parallel (according to the users will) a variable number of test executions. In doing so, ITER assumes that users have enough spare capacity in the cloud to run correctly their tests. At the moment, test executions is implemented by AUToCLES, and ITER providers the bindings to its APIs. 

- **Data Collection**. ITER collects test execution data that are published after each test execution, and stores them locally. At the moment, we have provided several test data collectors that binds to the AUToCLES tool.

- **Assertion framework**. Users extend the framework by defining new assertions to check over the test execution data. ITER is designed to enable an easy access to the available data, and to store the result of each assertion inside the test report.

- **Test Report Generation**.

- **Evolutionary Policy**. ITER implements a generic evolutionary loop but the evolution logic, that is, the logic that evolves the test suites and decides when to stop the search. At the moment we have implemented two policies:
	- Stop. This is the simplest possible policy: it stops the search as soon as it is invoked.
	- Plasticity Search. This search creates new tests that maximize the probability of finding plasticity in the system starting from the data collected in the previous test executions (for more details see [13]).

# Implementation Details

ITER is implemented in Java and leverages several libraries; to mention few: [**tapestry-ioc**](http://tapestry.apache.org/ioc.html), a powerful framework for dependency injection and inversion of control by Apache, [**tapestry5-cli**](https://github.com/alessiogambi/tapestry5-cli), a library to manage user inputs provided on the command line, and [**matlabcontrol**](https://code.google.com/p/matlabcontrol/), a library to interface Java with Matlab that is required by the plasticity search.

The complete list of dependency can be easily retrieved by inspecting the [*pom.xml*](./pom.xml) file in the repository.

# Code and Releases Download

The code can be downloaded by forking this git repository, while the jar files can be downloaded with maven by adding the following dependency and repository entries inside your pom.xml file:
```xml
<dependency>  
	<groupId>at.ac.tuwien</groupId>  
	<artifactId>iter</artifactId>  
	<version>0.0.1-SNAPSHOT</version>  
</dependency>
```

```xml
<repository>  
	<name>Infosys Repo</name>  
	<id>infosys-repo</id>  
	<url>http://www.infosys.tuwien.ac.at/mvn</url>  
</repository>
```

```xml
<repository>  
	<id>matloacontrol</id>  
	<url>http://maven.inria.fr/artifactory/plasmalab-public-release</url>  
</repository>
```

# Configurations

ITER can be configured by defining several configurations inside a specific `cloud.properties` file.
[This file](./conf/cloud.properties) lists an example of the configuration file.

# Run the tool

**NOTE** to run the tool you need a running AUToCLES instance which can be problematic to build from scratch. For this reasons, we are developing (and maintaining) a virtual machine (Ubuntu, qcow2, and configured to run by OpenStack clouds) that contains all the required software and is ready to run.
That virtual machine is available only upon request to [Alessio Gambi](http://www.infosys.tuwien.ac.at/staff/agambi/blog/?page_id=2#contactform).

After cloning the repo, assuming the availability of the configuration file and AUToCLES, 
the default configuration of the tool can be run by issuing the following command:
```bash
mvn  
	-Dlog4j.configuration=file://<PATH-TO-ITER>/conf/log4j.properties \  
	-Dat.ac.tuwien.dsg.cloud.configuration=<PATH-TO-ITER>/conf/cloud.properties \  
 	exec:java \  
	-Dexec.args="--output-file output.xml -c <CNAME> -s <SNAME> -m <SERVICE-MANIFEST-URL> -j <JMETER-CLIENTS-URL>"  
```

**PATH-TO-ITER** points to where the git repo was cloned to;  
**CNAME** is a 3-char long id of the client;
**SNAME** is a 3-char long id of the service (SUT);  
**SERVICE-MANIFEST-URL** is a valid URL that points at the service manifest file;  
**JMETER-CLIENTS-URL** is a valid URL that points at the user sessions JMeter file.

# References

[11] A. Gambi, W. Hummer, H.L. Truong, and S. Dustdar.
*Testing Elastic Computing Systems*.
Submitted to IEEE Internet Computing, 2013

[12] A. Gambi, W. Hummer, and S. Dustdar.
*Automated Testing of Cloud-Based Elastic Systems with AUToCLES*.
In Proceedings of the 28th IEEE/ACM International Conference on Automated Software Engineering (ASE),
November 11-15, 2013, Palo Alto, California (USA)

[13] A. Gambi, A. Filieri, and S. Dustdar.
*Iterative Test Suites Refinement for Elastic Computing Systems*.
In Proceedings of the joint meeting of the European Software Engineering Conference and the ACM SIGSOFT Symposium on the Foundations of Software Engineering (ESEC/FSE), August 18-26, 2013, Saint Petersburg, Russia

[14] A. Gambi, W. Hummer, and S. Dustdar.
*Testing Elastic Systems with Surrogate Models*.
In Proceedings of the International Workshop on Combining Modelling and Search-Based Software Engineering (CMSBSE) (co-located with ICSE'13),
May 20, 2013, San Francisco, California, USA  

[1]: http://dsg.tuwien.ac.at/autocles/
[2]: http://jmeter.apache.org/
[3]: http://2014.icse-conferences.org/demo
