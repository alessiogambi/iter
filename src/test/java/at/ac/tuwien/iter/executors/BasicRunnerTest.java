package at.ac.tuwien.iter.executors;

import java.util.Arrays;
import java.util.List;

import matlabcontrol.MatlabInvocationException;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.hsqldb.Collation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.exceptions.TestExecutionException;
import at.ac.tuwien.iter.loadgenerators.SinusLoadGenerator;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.services.impl.loadgenerators.InputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class BasicRunnerTest {

	private BasicRunner runner;
	private ConfigurationManager configurationManager;
	private static Registry registry;

	private final String customerName = "tAF";
	private final String serviceName = "S01";
	private final String urlTester = "http://10.99.0.118:8080/rest/Autocles/Autocles/1.0/";
	private final static String testFile = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx";
	private final static String manifestFile = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml";
	private final String dumpUrl = "http://10.99.0.118:8081/memcached/autocles-experiment1051827766860757-controllerResults";
	private final String manifestUrl = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml";

	private static SinusLoadGenerator loadGenerator;

	private static TypeCoercer typeCoercer;
	private static String traceGeneratorWebService;
	private static DataCollectionService dataCollectionService;
	private static MathEngineDao mathEngineDao;

	private static long DEFAULT_TIMEOUT = 10 * 60 * 1000l;

	@BeforeClass
	public static void setupOctaveEngine() {
		RegistryBuilder builder = new RegistryBuilder();
		// IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);
		registry = builder.build();
		registry.performRegistryStartup();
		//

		typeCoercer = registry.getService(TypeCoercer.class);
		traceGeneratorWebService = registry.getService(SymbolSource.class)
				.valueForSymbol(IterSymbolsNames.TRACEGENERATOR_URL);

		loadGenerator = new SinusLoadGenerator(
				LoggerFactory.getLogger(LoadGenerator.class),
				"SinusLoadGenerator", typeCoercer, traceGeneratorWebService,
				testFile, manifestFile, 0.0, 30.0, 0.0, 0.01, //
				100, // nBins
				registry.getService("RandomSinusLoadGenerator",
						InputSampler.class));
		dataCollectionService = registry
				.getService(DataCollectionService.class);

		mathEngineDao = registry.getService("matlab", MathEngineDao.class);
	}

	@Before
	public void createRunner() {
		// TODO Inject ConfManager as dep via the registry
		runner = new BasicRunner(
				LoggerFactory.getLogger(BasicRunnerTest.class),
				configurationManager, typeCoercer, dataCollectionService,
				DEFAULT_TIMEOUT);
	}

	@After
	public void cleanUpRunner() {
		// Clean up the DB somehow... A dao object should be used here.
		runner = null;
	}

	@AfterClass
	public static void shutdownRegistry() {
		registry.shutdown();
	}

	// // TODO Until a clear db implementation is not ready, keep this commented
	// // @Test
	// public void extractData() {
	//
	// Test test = loadGenerator.generateTest(typeCoercer.coerce(new double[] {
	// 1.0, 1.0 }, Number[].class));
	// double[] result = runner.loadSeriesFromDBAndDropDB(dumpUrl, test);
	//
	// System.out.println("BasicRunnerTest.extractData() " + result.length);
	// for (int i = 0; i < result.length; i++) {
	// System.out.println(result[i]);
	// }
	// }

	// // TODO Until a clear db implementation is not ready, keep this commented
	// // @Test
	// public void addDataToMatlab() throws TestExecutionException {
	// double[] params = { 10d, 0.01, 10d, 0d };
	//
	// SinusLoadGenerator loadGenerator = new SinusLoadGenerator(
	// LoggerFactory.getLogger(LoadGenerator.class), typeCoercer,
	// traceGeneratorWebService, testFile, manifestFile, 0.0, 30.0,
	// 0.0, 0.01,//
	// 100, // nBins
	// registry.getService("RandomSinusLoadGenerator",
	// InputSampler.class));
	//
	// at.ac.tuwien.iter.loadgenerators.Test initialTest = loadGenerator
	// .generateTest(typeCoercer.coerce(params, Number[].class));
	//
	// Test test = loadGenerator.generateTest(typeCoercer.coerce(new double[] {
	// 1.0, 1.0 }, Number[].class));
	//
	// double[] transitions = runner.loadSeriesFromDBAndDropDB(dumpUrl, test);
	// MathEngineDao dao = registry.getService("matlab", MathEngineDao.class);
	//
	// try {
	// System.out.println("BasicRunnerTest.addDataToMatlab() :\n"
	// + "Input parameters :"
	// + Arrays.toString(initialTest.getParameters()) + "\n"
	// + "Transitions : " + Arrays.toString(transitions));
	//
	// TestResult testResult = TestResult.newTestResult(test,
	// configurationManager.getCustomerName(),
	// configurationManager.getServiceName(), transitions);
	//
	// dao.addTestExecution(testResult);
	// } catch (MatlabInvocationException e) {
	// e.printStackTrace();
	// throw new TestExecutionException(e);
	// }
	// }

	// @Test
	public void getBestImprovement() throws TestExecutionException {
		double[] params = { 10d, 0.01, 10d, 0d };

		// Do a bootstrap or use a Mock DataCollector service or test the
		// TransitionCollectorService

		SinusLoadGenerator loadGenerator = new SinusLoadGenerator(
				LoggerFactory.getLogger(LoadGenerator.class),
				"SinusLoadGenerator", typeCoercer, traceGeneratorWebService,
				testFile, manifestFile, 0.0, 30.0, 0.0, 0.01, //
				100, // nBins
				registry.getService("RandomSinusLoadGenerator",
						InputSampler.class));

		// at.ac.tuwien.iter.loadgenerators.Test initialTest = loadGenerator
		// .generateTest(typeCoercer.coerce(params, Number[].class));
		//
		// Test test = loadGenerator.generateTest(typeCoercer.coerce(new
		// double[] {
		// 1.0, 1.0 }, Number[].class));
		// double[] transitions = runner.loadSeriesFromDBAndDropDB(dumpUrl,
		// test);
		// MathEngineDao dao = registry.getService("matlab",
		// MathEngineDao.class);
		//
		// try {
		// TestResult testResult = TestResult.newTestResult(test,
		// configurationManager.getCustomerName(),
		// configurationManager.getServiceName(), transitions);
		//
		// dao.addTestExecution(testResult);
		// // Not sure this will work with only 1 data sample !!
		// List<double[]> results = dao.getBestPlasticityTests(4);
		// System.out.println("Best Improvements: " + results);
		// } catch (MatlabInvocationException e) {
		// e.printStackTrace();
		// throw new TestExecutionException(e);
		// }
	}
}
