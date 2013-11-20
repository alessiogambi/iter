package at.ac.tuwien.iter.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.ServiceId;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.gambi.tapestry5.cli.data.CLIOption;
import org.gambi.tapestry5.cli.services.CLIValidator;
import org.gambi.tapestry5.cli.utils.CLISymbolConstants;
import org.slf4j.Logger;

import at.ac.tuwien.iter.data.IterApplication;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.Iter;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.services.TestSuiteEvolver;
import at.ac.tuwien.iter.services.TestSuiteEvolverSource;
import at.ac.tuwien.iter.services.impl.IterImpl;
import at.ac.tuwien.iter.services.impl.evo.GPMLBasedPlasticityEvolver;
import at.ac.tuwien.iter.services.impl.evo.StopTestSuiteEvolution;
import at.ac.tuwien.iter.services.impl.evo.TestSuiteEvolverSourceImpl;
import at.ac.tuwien.iter.services.impl.matlab.MatlabControlImpl;
import at.ac.tuwien.iter.services.impl.validators.TestEvolverCLIValidator;
import at.ac.tuwien.iter.utils.IterSymbolsNames;
import at.ac.tuwien.iter.utils.ServiceManifestUtils;

/**
 * This class contains the definition of the object managed by the tapestry-ioc
 * framework that implements the main functionalities of the tool.
 * 
 * 
 * 
 * @author alessiogambi
 * @category Module
 */
@SubModule({ AssertionModule.class, LoadGeneratorModule.class,
		DataCollectionModule.class })
public class IterModule {

	/**
	 * Define the CLI Options for ITER. Configurations for LOADGenerator are in
	 * the load generator module.
	 * 
	 * @category Contribution
	 */
	public void contributeCLIParser(SymbolSource symbolSource,
			Configuration<CLIOption> configuration) {

		configuration.add(new CLIOption("c", "customer-name", 1, true,
				"Customer name."));
		configuration.add(new CLIOption("s", "service-name", 1, true,
				"Service name"));

		configuration.add(new CLIOption("m", "service-manifest-URL", 1, true,
				"URL of the Service manifest file"));

		configuration.add(new CLIOption("j", "jmeter-clients-URL", 1, true,
				"URL of the JMeter file"));

		// Test Execution setting. With defaults
		CLIOption nParallelTests = new CLIOption("N", "n-parallel-tests", 1,
				false, "Maximum number of parallel test executions.");
		nParallelTests.setDefaultValue("1");
		configuration.add(nParallelTests);

		CLIOption nInitialRandomTests = new CLIOption("r",
				"n-initial-random-tests", 1, false,
				"Number of initial random tests.");
		nInitialRandomTests.setDefaultValue("0");
		configuration.add(nInitialRandomTests);

		// Search configuration
		CLIOption nBestTests = new CLIOption("n", "n-best-tests", 1, false,
				"Maximum number of best expected improvement");
		nBestTests.setDefaultValue("1");
		configuration.add(nBestTests);

		configuration.add(new CLIOption("b", "bootstrap", 0, false,
				"Enable boostrap from input file."));

		configuration.add(new CLIOption("i", "input-file", 1, false,
				"Boostrap file."));

		CLIOption outputFile = new CLIOption("o", "output-file", 1, false,
				"The result file produced as output.");
		outputFile.setDefaultValue(symbolSource
				.valueForSymbol(IterSymbolsNames.TEST_RESULTS_FILE));
		configuration.add(outputFile);

		CLIOption evolveWith = new CLIOption("e", "evolve-with", 1, false,
				"The name of the Strategy used to evolve the test suite.");
		evolveWith.setDefaultValue("default");
		configuration.add(evolveWith);

	}

	/**
	 * @category Contribution
	 * 
	 * @param contributions
	 * @param plasticity
	 */
	public void contributeTestSuiteEvolverSource(
			MappedConfiguration<String, TestSuiteEvolver> contributions,
			@InjectService("PlasticityTestSuiteEvolver") TestSuiteEvolver plasticity) {

		// Always there !
		contributions.addInstance("default", StopTestSuiteEvolution.class);
		// ESEC-FSE
		contributions.add("plasticity", plasticity);
	}

	// NOTE Filters should never be accessible directly !!
	/**
	 * @category Contribution
	 */
	public void contributeCLIValidator(final Logger logger,
			final TypeCoercer typeCoercer,
			OrderedConfiguration<CLIValidator> configuration) {

		configuration.addInstance("TestEvolverValidator",
				TestEvolverCLIValidator.class);

		configuration.add("BootstrapfileValidator", new CLIValidator() {

			public void validate(Map<CLIOption, String> options,
					List<String> inputs, List<String> accumulator) {
				try {

					String inputFile = null;
					// Default should already by there !
					String bootstrap = "false";

					for (CLIOption cliOption : options.keySet()) {
						if (cliOption.getLongOpt().equals("input-file")) {
							inputFile = cliOption.getValue();
						} else if (cliOption.getLongOpt().equals("bootstrap")) {
							bootstrap = cliOption.getValue();
						}
					}

					if (inputFile != null
							&& "false".equalsIgnoreCase(bootstrap)) {
						accumulator
								.add("Bootstrap file can be specified iff the bootstrap option (-i, --input-file) is specified!");

					}

					if (inputFile == null && "true".equalsIgnoreCase(bootstrap)) {
						accumulator.add("Bootstrap file is missing.");

					}

				} catch (Exception e) {
					logger.warn("Error during validation ", e);
					accumulator.add("BootstrapfileValidator Failed!");
				}
			};
		});

		// NOTE THIS IS QUITE A BIG HACK !!
		// Since the @PostConstructor and @PostInjection are not suitable
		// options I needed to resort to this code
		configuration.add("ProblemSpaceValidator", new CLIValidator() {

			public void validate(Map<CLIOption, String> options,
					List<String> inputs, List<String> accumulator) {

				int problemSize = 1;
				try {
					String manifestURL = null;
					for (CLIOption cliOption : options.keySet()) {
						if (cliOption.getLongOpt().equals(
								"service-manifest-URL")) {
							manifestURL = cliOption.getValue();
						}
					}

					// Note we could also @CLIOption("service-manifest-URL")
					// inject it !
					for (Integer dimension : ServiceManifestUtils
							.getVariabilitySpaceFromManifest(typeCoercer
									.coerce(manifestURL, URL.class))) {
						problemSize = problemSize * dimension;
					}
					logger.info("Problem size " + problemSize);
					System.getProperties().put(IterSymbolsNames.PROBLEM_SIZE,
							"" + problemSize);
				} catch (Exception e) {
					logger.error(
							"Error while checking/setting the problem size configuration",
							e);
					accumulator.add("Invalid Problem Size ");
				}

			}
		});

	}

	/*
	 * The IterApplication class is the bean that contains the JSR303 annotation
	 * to valid inputs passed via the command line.
	 */
	/**
	 * @category Contribution
	 */
	public void contributeApplicationConfigurationSource(
			MappedConfiguration<String, Object> configuration) {
		configuration.addInstance("iterApplication", IterApplication.class);
	}

	/**
	 * @category Contribution
	 * @param configuration
	 */
	public void contributeApplicationDefaults(
			MappedConfiguration<String, String> configuration) {

		// This is the command that we use to start the application. We need to
		// put that here for letting the CLI libraries know about the correct
		// syntax to invoke the application
		configuration.add(CLISymbolConstants.COMMAND_NAME, "iter");

		configuration.add(IterSymbolsNames.GPML_DIR, (new File(
				"./target/classes/gpml/")).getAbsolutePath());

		configuration.add(IterSymbolsNames.ITER_DIR, (new File(
				"./target/classes/at/ac/tuwien/iter/octave/"))
				.getAbsolutePath());

		// Default application values !
		configuration.add(IterSymbolsNames.TEST_RESULTS_FILE, (new File(
				"test-results.xml")).getAbsolutePath());
		// configuration
		// .add(IterSymbolsNames.BOOTSTRAP_FILE, "bootstrap-file.xml");

		configuration.add(IterSymbolsNames.TOLERANCE, "0.00001");
		configuration.add(IterSymbolsNames.MIN_EI, "0.0001");

		// Default experiment timeout:20 mins. Deploy + Run + Wait
		String defaultExperimentTimeout = "" + 20 * 60 * 1000l;
		configuration.add(IterSymbolsNames.EXPERIMENT_TIMEOUT,
				defaultExperimentTimeout);

		// NOT ALL THE NAMES ARE VALID !
		configuration.add(IterSymbolsNames.N_BINS, "100");
		configuration.add(IterSymbolsNames.MATLAB_LOG_FILE, (new File(
				"iterMatlab.log")).getAbsolutePath());
	}

	/**
	 * @category Contribution
	 * @param configuration
	 */
	@SuppressWarnings("rawtypes")
	public static void contributeTypeCoercer(
			Configuration<CoercionTuple> configuration) {

		Coercion<double[], Number[]> doubleArrayToNumberArray = new Coercion<double[], Number[]>() {

			public Number[] coerce(double[] arg0) {
				if (arg0 == null) {
					return null;
				}
				if (arg0.length == 0) {
					return new Number[0];
				} else {
					Number[] result = new Number[arg0.length];
					for (int i = 0; i < arg0.length; i++) {
						// result[i] = new Double(arg0[i]);
						result[i] = arg0[i];
					}
					return result;
				}
			}
		};

		configuration.add(new CoercionTuple<double[], Number[]>(double[].class,
				Number[].class, doubleArrayToNumberArray));

		Coercion<Number[], double[]> numberArrayToDoubleArray = new Coercion<Number[], double[]>() {

			public double[] coerce(Number[] arg0) {
				if (arg0 == null) {
					return null;
				}
				if (arg0.length == 0) {
					return new double[0];
				} else {
					double[] result = new double[arg0.length];
					// Maybe there is a smarter method for that
					for (int i = 0; i < arg0.length; i++) {
						result[i] = arg0[i].doubleValue();
					}
					return result;
				}
			}
		};

		configuration.add(new CoercionTuple<Number[], double[]>(Number[].class,
				double[].class, numberArrayToDoubleArray));

		Coercion<double[], String> doubleArrayToString = new Coercion<double[], String>() {

			public String coerce(double[] arg0) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < arg0.length; i++) {
					sb.append(arg0[i]);
					sb.append(",");
				}
				sb.deleteCharAt(sb.lastIndexOf(","));
				return sb.toString();
			}

		};
		configuration.add(new CoercionTuple<double[], String>(double[].class,
				String.class, doubleArrayToString));

		Coercion<String, double[]> stringToDoubleArray = new Coercion<String, double[]>() {
			public double[] coerce(String arg0) {
				// Arrays.toString => [x,x,x,x]
				String _array = arg0.trim().replaceAll("\\[", "")
						.replaceAll("\\]", "");

				String[] token = _array.split(",");
				double[] result = new double[token.length];
				for (int i = 0; i < token.length; i++) {
					result[i] = Double.parseDouble(token[i]);
				}
				return result;
			}
		};

		configuration.add(new CoercionTuple<String, double[]>(String.class,
				double[].class, stringToDoubleArray));

		Coercion<String, URL> stringToURL = new Coercion<String, URL>() {
			public URL coerce(String arg0) {
				try {
					return new URL(arg0);
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}
		};

		configuration.add(new CoercionTuple<String, URL>(String.class,
				URL.class, stringToURL));

		Coercion<URL, String> uRLtoString = new Coercion<URL, String>() {
			public String coerce(URL arg0) {
				if (arg0 != null) {
					return arg0.toString();
				} else {
					return null;
				}
			}
		};

		configuration.add(new CoercionTuple<URL, String>(URL.class,
				String.class, uRLtoString));

	}

	// TODO Note here that we access the input CLI and we force a default
	// This is an alternative method to get directly to the parsed input
	// We also contribute a validator to check if the provided name was also
	// contributed. THIS ASSUME A PROPER DEFAULT VALUE IS SET
	// Shall this be a Strategy or something else
	/**
	 * @category Build CommandLineTestSuiteEvolver
	 */
	@ServiceId("CommandLineTestSuiteEvolver")
	public TestSuiteEvolver buildCLITestSuiteEvolver(
			TestSuiteEvolverSource testSuiteEvolverSource,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "evolve-with") String evolverName) {

		return testSuiteEvolverSource.getTestSuiteEvolver(evolverName);
	}

	/**
	 * @category Build PlasticityTestSuiteEvolver
	 * @param logger
	 * @param mathEngineDao
	 * @param typeCoercer
	 * @param loadGenerator
	 * @param nBestPredictions
	 * @return
	 */
	@ServiceId("PlasticityTestSuiteEvolver")
	public TestSuiteEvolver buildPlasticityTestSuiteEvolver(
			Logger logger,
			MathEngineDao mathEngineDao,
			TypeCoercer typeCoercer,
			@InjectService("CommandLineLoadGenerator") LoadGenerator loadGenerator,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "n-best-tests") int nBestPredictions) {

		// TODO Make these parameters
		int limitCount = 2;
		int minDistance = 2;

		return new GPMLBasedPlasticityEvolver(logger, typeCoercer,
				loadGenerator, mathEngineDao, nBestPredictions, limitCount,
				minDistance);
	}

	/**
	 * @category Build Autobuild TestSuiteEvolverSource
	 * @param binder
	 */
	public static void bind(ServiceBinder binder) {
		binder.bind(TestSuiteEvolverSource.class,
				TestSuiteEvolverSourceImpl.class);
	}

	/**
	 * @category Build Iter
	 * 
	 * 
	 * @param logger
	 * @param registryShutdownHub
	 * @param typeCoercer
	 * @param assertionService
	 * @param dataCollectionService
	 * @param loadGeneratorSource
	 * @param loadGenerator
	 * @param testSuiteEvolver
	 * @param customerName
	 * @param serviceName
	 * @param nParallelTests
	 * @param nInitialTests
	 * @param joperaURL
	 * @param experimentTimetout
	 * @param testResultFile
	 * @param bootstrap
	 * @param bootstrapFile
	 * @return
	 */
	@Scope(ScopeConstants.PERTHREAD)
	public Iter build(
			Logger logger,
			// THis is not safe
			RegistryShutdownHub registryShutdownHub,
			TypeCoercer typeCoercer,
			AssertionService assertionService,
			DataCollectionService dataCollectionService,
			// This is needed for the Boostrap part as we generate on the fly
			// tests, and tests require a proper LoadGenerator
			LoadGeneratorSource loadGeneratorSource,
			// Is this really needed as we already inject the loadGenSource
			@InjectService("CommandLineLoadGenerator") LoadGenerator loadGenerator,
			@InjectService("CommandLineTestSuiteEvolver") TestSuiteEvolver testSuiteEvolver,

			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "customer-name") String customerName,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "service-name") String serviceName,

			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "n-parallel-tests") int nParallelTests,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "n-initial-random-tests") int nInitialTests,

			@Symbol(IterSymbolsNames.JOPERA_URL) URL joperaURL,
			@Symbol(IterSymbolsNames.EXPERIMENT_TIMEOUT) long experimentTimetout,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "output-file") File testResultFile,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "bootstrap") boolean bootstrap,
			@org.gambi.tapestry5.cli.annotations.CLIOption(longName = "input-file") File bootstrapFile) {

		System.out.println("IterModule.build() customerName " + customerName);
		System.out.println("IterModule.build() serviceName " + serviceName);

		return new IterImpl(logger, customerName, serviceName, nParallelTests,
				nInitialTests, testResultFile, bootstrapFile, joperaURL,
				experimentTimetout, bootstrap, loadGenerator,
				registryShutdownHub, typeCoercer, assertionService,
				dataCollectionService, testSuiteEvolver, loadGeneratorSource);
	}

	// THIS MUST BE A SINGLETON SERVICE !
	/**
	 * @category Build matlab Singleton
	 */
	@Scope(ScopeConstants.DEFAULT)
	@ServiceId("matlab")
	public MathEngineDao buildMatlabControl(
			Logger logger,
			// Force the LoadGenerator to be injected BEFORE the matlab is
			// constructed !!
			@InjectService("CommandLineLoadGenerator") LoadGenerator loadGenerator,
			RegistryShutdownHub registryShutdownHub, SymbolSource symbolSource,
			TypeCoercer typeCoercer,
			@Symbol(IterSymbolsNames.GPML_DIR) String gpmlDir,
			@Symbol(IterSymbolsNames.ITER_DIR) String iterDir,
			@Symbol(IterSymbolsNames.PROBLEM_SIZE) int problemSize,
			@Symbol(IterSymbolsNames.TOLERANCE) double tol,
			@Symbol(IterSymbolsNames.MIN_EI) double minEI,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(IterSymbolsNames.MATLAB_LOG_FILE) String matlabLogFile

	) throws MatlabConnectionException, MatlabInvocationException {

		// TODO We need this trick because load generator NEEDS a couple of
		// symbols BEFORE this service gets instantiated
		// so Injecting those symbols before hand won't work. Maybe a
		// ShadowProperty is the best approach, but for the moment we adopt the
		// following

		// 1) Force the proxy to instantiate the object by invoking some method
		// on it
		loadGenerator.generateRandomCase();
		// 2) Use the inject SymbolSource (not the SYMBOLS!) and TypeCoercer to
		// recover the values we are looking for

		double[] LB = typeCoercer.coerce(
				symbolSource.valueForSymbol(IterSymbolsNames.LB),
				double[].class);
		double[] UB = typeCoercer.coerce(
				symbolSource.valueForSymbol(IterSymbolsNames.UB),
				double[].class);

		// TODO Not sure this is really required... but we need to force the
		// proxy to
		// instantiate the object for real

		return new MatlabControlImpl(logger, registryShutdownHub, gpmlDir,
				iterDir, problemSize, tol, minEI, LB, UB, nBins, matlabLogFile);
	}
}
