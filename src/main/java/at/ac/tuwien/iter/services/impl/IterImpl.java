package at.ac.tuwien.iter.services.impl;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBException;

import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.data.TestResultsCollector;
import at.ac.tuwien.iter.exceptions.TestExecutionException;
import at.ac.tuwien.iter.executors.BasicRunner;
import at.ac.tuwien.iter.executors.ConfigurationManager;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.Iter;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.TestSuiteEvolver;

/**
 * This is the main class that manages the test suite generation guided by the
 * model. Each test suite generation process is (and must be independent!) from
 * the others.
 * 
 * 
 * @author alessiogambi
 * 
 */
public class IterImpl implements Iter {

	// TODO To run the tests -> Check if Tapestry already provide one or we can
	// just
	// add to it
	private ExecutorService executor;

	private Logger logger;

	// Inputs
	private String customerName;
	private String serviceName;

	// This must be injected
	private LoadGenerator loadGenerator;

	// This is the result !
	private Set<Test> testSuite;
	private Map<Test, Integer> hitCount;
	private TestResultsCollector testResultsCollector;

	private List<Test> experimentAgenda;

	// / VERY BAD !
	private ConfigurationManager configurationManager;

	private TypeCoercer typeCoercer;
	private AssertionService assertionService;
	private DataCollectionService dataCollectionService;

	// Test Execution
	private int nParallelTests;
	private int nInitialTests;

	private long experimentTimeout;

	private boolean bootstrap;
	private boolean regression;
	private boolean dryrun;

	private File inputFile;
	private File testResultFile;

	private TestSuiteEvolver testSuiteEvolver;

	private LoadGeneratorSource loadGeneratorSource;

	public IterImpl(
	// Resources
			Logger logger,
			// User inputs - Do we really need this here ? Those are used only
			// because AUToCLES need that, maybe we can move them there - TODO
			// Move this into Test execution framework
			String customerName, String serviceName,
			// Test Execution - TODO Move this into Test execution framework
			int nParallelTests,
			// Test Suite Initialization.
			int nInitialTests,
			// Input-output
			final File testResultFile, final File inputFile,
			// Experimental Environment - TODO Move this into Test execution
			// framework
			URL autoclesURL,
			// Experiment setup - Pre/Post conditions ?
			long experimentTimeout, //
			// Flags
			boolean bootstrap, boolean regression, boolean dryrun,
			// Other services - Why we need this here ?
			LoadGenerator loadGenerator,
			// This should be avoided, but @PostInjection does not work fine
			// apparently...
			RegistryShutdownHub registryShutdownHub, //
			TypeCoercer typeCoercer, // Infrastructure service
			AssertionService assertionService, // OK
			DataCollectionService dataCollectionService, // Not sure
			TestSuiteEvolver testSuiteEvolver, // OK
			LoadGeneratorSource loadGeneratorSource// Not sure
	) {

		this.bootstrap = bootstrap;
		this.regression = regression;
		this.dryrun = dryrun;

		this.logger = logger;

		this.customerName = customerName;
		this.serviceName = serviceName;
		this.loadGenerator = loadGenerator;
		this.assertionService = assertionService;
		this.dataCollectionService = dataCollectionService;
		this.loadGeneratorSource = loadGeneratorSource;
		this.nParallelTests = nParallelTests;
		this.nInitialTests = nInitialTests;

		this.experimentTimeout = experimentTimeout;

		this.testResultsCollector = new TestResultsCollector();

		this.testSuite = new HashSet<Test>();
		this.hitCount = new Hashtable<Test, Integer>();

		this.testResultFile = testResultFile;
		this.inputFile = inputFile;

		// this.mathEngineDao = mathEngineDao;
		this.typeCoercer = typeCoercer;
		this.testSuiteEvolver = testSuiteEvolver;

		// TODO This must be removed, use a service instead and provide symbols
		// !!!
		this.configurationManager = new ConfigurationManager(
				autoclesURL.toString(), this.customerName, this.serviceName,
				this.loadGenerator);
		// TODO EXECUTOR SERVICE MUST BE ACCESSED/INJECTED AS DEPENDENCY
		// Register hook for shutdown
		// Use the @PostInjection annotation, see the ref manual
		registryShutdownHub.addRegistryShutdownListener(new Runnable() {
			public void run() {
				if (executor != null) {
					executor.shutdown();
				}
			}
		});

		// TODO IS IT REALLY WORKING ?!
		// Register hook for shutdown: STORE ALL THE RESULTS !
		// Use the @PostInjection annotation, see the ref manual
		registryShutdownHub.addRegistryShutdownListener(new Runnable() {
			public void run() {
				if (testResultsCollector != null) {
					try {
						TestResultsCollector.saveToFile(
								testResultFile.getAbsolutePath(),
								testResultsCollector);
					} catch (JAXBException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	/**
	 * Try to bootstrap from the given file. If something goes wrong the
	 * bootstrap may add some (previously) test to the Agenda
	 */
	void bootstrap() throws TestExecutionException {

		// Stats
		int total = 0;
		int failed = 0;
		int rescheduled = 0;
		String bFile = inputFile.getAbsolutePath();
		long startTime = System.currentTimeMillis();

		logger.info("Iter.bootstrapAndStart() BootStraping from "
				+ inputFile.getAbsolutePath());

		if (inputFile.exists()) {

			// Try Load all the cached executions if the file exists
			try {
				testResultsCollector = TestResultsCollector
						.loadFromFile(inputFile.getAbsolutePath());
			} catch (Throwable e) {
				logger.error(
						String.format(
								"Error. Cannot load the boostrap file %s. Skip the bootstrap process.",
								inputFile.getAbsolutePath()), e);
				throw new TestExecutionException(
						"Cannot load the bootstrap file");
			}

			// Collect some data
			total = testResultsCollector.getTestResults().size();

			try {

				// Run the assertion again
				for (TestResult testResult : testResultsCollector) {
					logger.info("Processing testResult: " + testResult);

					Test newTest = loadGeneratorSource.getLoadGenerator(
							testResult.getLoadGeneratorID()).generateTest(
							testResult.getParametersAsNumbers());

					try {
						logger.info("Registering test in testsuite: " + newTest);
						testSuite.add(newTest);
					} catch (Throwable e) {

						logger.warn(
								"Cannot store the test in the test suite file. Run again test "
										+ newTest, e);
						// TODO Check if this will eventually override the one
						// stored in the boostraped file/
						experimentAgenda.add(newTest);
						rescheduled++;
						failed++;
						continue;
					}

					try {
						logger.info("Checking Assertions for test: " + newTest);
						assertionService.check(testResult);
					} catch (Throwable e) {
						logger.error(
								"Cannot assert test results in bootstraping file. Skip  !",
								e);
						failed++;
						continue;
					}
				}
			} catch (Throwable e) {
				// TODO Not sure if really needed anymore
				logger.error("Cannot complete the bootstrap!", e);
				throw new TestExecutionException(
						"Cannot complete the bootstrap");
			}

		} else {
			logger.warn(String
					.format("The specified boostraping file %s does not exists. Continue with no bootstrap.",
							inputFile.getAbsolutePath()));
		}

		long endTime = System.currentTimeMillis();
		/*
		 * Print Bootstrap statistics
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("\n\n").append("=======================================\n")
				.append("\tBootstrap summary\n")
				.append("=======================================\n");

		sb.append("   Elaborated ").append(total).append(" test results\n");
		sb.append("   Input file ").append(bFile).append("\n");

		sb.append("   Result:\n");
		sb.append("    - Ok: ").append((total - failed)).append("\n");
		sb.append("    - Failed: ").append(failed).append("\n");
		sb.append("        - Rescheduled: ").append(rescheduled).append("\n");
		sb.append("   Elaboration time was ")
				.append(String.format("%.2f",
						(double) ((endTime - startTime) / 1000l)))
				.append(" secs\n");
		sb.append("=======================================\n");
		logger.info(sb.toString());

	}

	// Return a list of tests to be executed taken from the input file
	List<Test> regression() throws TestExecutionException {

		List<Test> tests = new ArrayList<Test>();

		// Stats
		int total = 0;

		String bFile = inputFile.getAbsolutePath();
		long startTime = System.currentTimeMillis();

		logger.info("Iter.regression() Regression from "
				+ inputFile.getAbsolutePath());

		if (inputFile.exists()) {

			// Try Load all the cached executions if the file exists
			TestResultsCollector inputTests = null;
			try {
				inputTests = TestResultsCollector.loadFromFile(inputFile
						.getAbsolutePath());
			} catch (Throwable e) {
				logger.error(
						String.format(
								"Error. Cannot load the input file %s. Abort the regression",
								inputFile.getAbsolutePath()), e);
				throw new TestExecutionException(
						"Cannot load the bootstrap file", e);
			}

			// TODO Not sure we will not have problems
			// Create a new tests !
			for (TestResult testResult : inputTests) {
				Test newTest = loadGeneratorSource.getLoadGenerator(
						testResult.getLoadGeneratorID()).generateTest(
						testResult.getParametersAsNumbers());

				logger.info("Creating Test :" + newTest);
				tests.add(newTest);

				total++;
			}
		} else {
			logger.warn(String
					.format("The specified regression file %s does not exists. Continue with no bootstrap.",
							inputFile.getAbsolutePath()));
		}

		long endTime = System.currentTimeMillis();
		/*
		 * Print Statistics
		 */
		StringBuffer sb = new StringBuffer();
		sb.append("\n\n").append("=======================================\n")
				.append("\tRegression summary\n")
				.append("=======================================\n");

		sb.append("   Elaborated ").append(total).append(" previous tests\n");
		sb.append("   Input file ").append(bFile).append("\n");
		sb.append("   Result:\n");
		sb.append("        - Rescheduled: ").append(total).append("\n");
		sb.append("   Elaboration time was ")
				.append(String.format("%.2f",
						(double) ((endTime - startTime) / 1000l)))
				.append(" secs\n");
		sb.append("=======================================\n");
		logger.info(sb.toString());

		// eventually return
		return tests;

	}

	private List<Test> createRandomTests() {
		List<Test> randomExperiments = new ArrayList<Test>();
		for (int experiment = 0; experiment < nInitialTests; experiment++) {
			randomExperiments.add(loadGenerator.generateRandomCase());
		}

		return randomExperiments;
	}

	// Mainly for unit testing
	protected Collection<TestResult> getTestResults() {
		return testResultsCollector.getTestResults();
	}

	protected Collection<Test> getTestSuite() {
		return testSuite;
	}

	/*
	 * Schedule the experiments over the set of executors and then blocks until
	 * all the experiments ran. Experiments that run fine are removed from the
	 * list, if there is some failures in the execution the test must be
	 * repeated
	 * 
	 * @param experiments
	 */
	private void scheduleAndRunExperiments(final List<Test> experiments)
			throws InterruptedException {

		final AtomicBoolean stopTest = new AtomicBoolean(false);
		final UncaughtExceptionHandler uncaughtExceptionHandler = new UncaughtExceptionHandler() {

			public void uncaughtException(Thread thread, Throwable throwable) {
				logger.error(" uncaughtException " + throwable.getMessage()
						+ " from Thread " + thread);

				if (throwable.getMessage().contains("STOP THE TEST")) {
					stopTest.set(true);
				}

			}
		};

		// TODO Try to exploit the parallel executor provided by the framework
		// itself. Note that we need to capture any exception generated by the
		// worker threads !
		final ThreadFactory factory = new ThreadFactory() {

			public Thread newThread(Runnable runnable) {
				final Thread thread = new Thread(runnable);
				// Force our generated Handler here
				thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
				return thread;
			}
		};

		executor = Executors.newFixedThreadPool(nParallelTests, factory);

		for (final Test test : experiments) {

			// Should I USED FUTURE<?> here ?
			/*
			 * Something like: ExecutorService executor =
			 * Executors.newSingleThreadExecutor(); Runnable task = new
			 * Runnable() { public void run() { throw new
			 * RuntimeException("foo"); } };
			 * 
			 * Future<?> future = executor.submit(task); try { future.get(); }
			 * catch (ExecutionException e) { Exception rootException =
			 * e.getCause(); }
			 */

			executor.execute(new Runnable() {

				private void saveResultsToFile() throws JAXBException,
						IOException {
					// Store the result
					String fileName = testResultFile.getAbsolutePath();
					logger.debug("Basic Runner: Storing results to  "
							+ fileName);

					TestResultsCollector.saveToFile(fileName,
							testResultsCollector);

				}

				public void run() {
					Logger _logger = LoggerFactory.getLogger(logger.getName()
							+ "-Test-" + test.getId());

					BasicRunner runner = new BasicRunner(_logger,
							configurationManager, typeCoercer,
							dataCollectionService, experimentTimeout);

					TestResult testResult = null;
					try {
						testResult = runner.executeTest(test);
					} catch (TestExecutionException e) {
						e.printStackTrace();
						if (e.getMessage().contains("STOP THE TEST")) {
							logger.error("STOP THE TEST SUITE !");
							throw new RuntimeException(e);

						}

					} catch (Exception e) {
						logger.error("Error while executing the test");
						throw new RuntimeException(e);
					}

					try {
						// Store the execution in the TestResultCollectorFile -
						// Only if there are not exceptions
						testResultsCollector.addTestResult(testResult);
					} catch (Exception e) {
						logger.warn("Error while add test result to collector",
								e);

					}

					// TODO Not sure this belongs HERE... maybe it's part of the
					// "main" process.
					// Increase our testsuite
					synchronized (testSuite) {
						testSuite.add(test);

						// This is specific for our iter search !!
						hitCount.put(test, 0);
					}
					logger.info("Added the test " + test.getId()
							+ "to the test suite file");

					// Run all the assertions: What if multiple threads run
					// this at the same time ? MathDao should be synch and
					// thread safe...
					try {
						assertionService.check(testResult);
					} catch (Exception e) {
						logger.warn(
								"Failed to store assertion for " + test.getId()
										+ "in the XML file !", e);
					}

					/*
					 * TODO: This fails always if there are no plastiicty check
					 * at all. For the moment we simply comment the code out !
					 * TODO Shall we capture this some how in a configurable way
					 * ? Maybe in the future we may implement somthing like: if
					 * fails the check then repeat
					 */

					// try {
					//
					// if (testResult.getTestReport("plasticity") != null
					// && !testResult.getTestReport("plasticity")
					// .isFailed()) {

					// If the experiment was fine remove form the agenda
					synchronized (experiments) {
						logger.info("Removing " + test
								+ " from the list of experiments to run");
						if (!experiments.remove(test)) {

							logger.warn("IterImpl.scheduleExperiments() ERROR while removing "
									+ test);
						}
					}
					//
					// } else {
					// logger.info("Plasticity check failed, so we keep the experiment for another round !");
					// }
					// } catch (Exception e) {
					// logger.warn("Error while checink test reports", e);
					// }

					try {
						saveResultsToFile();
					} catch (Exception e) {
						logger.warn(
								"Failed to store result for " + test.getId()
										+ "in the XML file !", e);
						e.printStackTrace();
					}

				}
			});

		}
		// Follow the common pattern for temporary thread pools and wait
		// the end of all the experiments
		executor.shutdown();

		try {
			// Wait until this is over or the process gets interrupted
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.warn(" Timeout on Wait for termination. Stop the test");
			throw new RuntimeException("STOP THE TEST", e);
		}

		logger.info("Experiments ROUND finished !");
	}

	public void start() {
		// State Variables
		// Search status
		boolean running = true;
		// the Agenda contains the list of experiments to run.
		// This is a "global" var in the class
		experimentAgenda = new ArrayList<Test>();

		if (regression) {
			try {
				experimentAgenda.addAll(regression());
			} catch (TestExecutionException e) {
				logger.warn("Problems during the regression", e);
			}
		}
		// If the -R/--regression flag is specified we read from the input file
		// and create (not random) tests

		// If the r option is zero or not specified, we will not create random
		// tests
		experimentAgenda.addAll(createRandomTests());

		// Dry-Run option

		if (!dryrun) {
			// This is not side effect free so we cannot be used with dry-run !
			if (bootstrap) {
				// Shall we move exc inside the private method ?
				try {
					bootstrap();
				} catch (TestExecutionException e) {
					logger.warn("Problems during the bootstrap", e);
				}
			}
			while (running) {
				try {
					logger.info("IterImpl.start() Scheduling : "
							+ experimentAgenda.size() + " experiments to run.");

					// Schedule all the experiments over the N executors
					// This can be esily become a service as well.
					// This will block until all the experiments ran
					scheduleAndRunExperiments(experimentAgenda);

					// If for some reasons some experiment failed or must be
					// repeated in the next round, we will keep it inside the
					// agenda

				} catch (InterruptedException e) {
					logger.warn("Interrupted execution. Exit");
					running = false;
					break;
				}

				// Maybe the assertions should be here !?
				logger.info("IterImpl.start() Experiments that remains to run or must be repeated: "
						+ experimentAgenda.size());
				/*
				 * - Make this a configurable setting -
				 */

				Collection<Test> newExperiments = null;
				try {
					// Evolve the test suite starting from the current one, plus
					// the
					// results obtained from it
					newExperiments = testSuiteEvolver.evolveTestSuite(
							testSuite, testResultsCollector.getTestResults());
				} catch (Exception e) {
					logger.warn("Error during test suite evolution. Continue",
							e);
				}

				// THIS IS SPECIFIC FOR OUR TEST SUITE. CAN BE A CONFIGURABLE
				// SERVICE IN CHAIN/PIPELINE with constraints (on the number of
				// test
				// for example). no need for update result one we have
				// everything
				// inside the testSuiteObject !
				experimentAgenda.addAll(newExperiments);

				// Check for termination. No more experiments means we are done
				// !
				if (experimentAgenda.size() == 0) {
					logger.info("There are no more tests to run !");
					running = false;
				}
			}
			// Store to file
			try {
				TestResultsCollector.saveToFile(
						testResultFile.getAbsolutePath(), testResultsCollector);
				logger.info("Results stored to "
						+ testResultFile.getAbsolutePath());
			} catch (Exception e) {
				logger.error("Results cannot be stored to "
						+ testResultFile.getAbsolutePath());
				e.printStackTrace();
			}
		} else {
			logger.info("DryRun option active");
		}
	}
}
