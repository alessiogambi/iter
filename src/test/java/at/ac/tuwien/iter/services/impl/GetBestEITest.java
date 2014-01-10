package at.ac.tuwien.iter.services.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.utils.CloudSymbolConstants;
import at.ac.tuwien.iter.exceptions.TestExecutionException;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.Iter;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.services.TestSuiteEvolver;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class GetBestEITest {

	private static Registry registry;
	private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000l;

	@Before
	public void setup() {
		// Overwrite default for files
		System.getProperties().put(
				IterSymbolsNames.TEST_RESULTS_FILE,
				(new File("src/test/resources/test-results-test.xml"))
						.getAbsolutePath());

		System.getProperties().put(
				IterSymbolsNames.INPUT_FILE,
				(new File("src/test/resources/bootstrap.xml"))
						.getAbsolutePath());

		// Options are specified via the cloud.properties file:
		System.getProperties().put(
				CloudSymbolConstants.CONFIGURATION_FILE,
				(new File("src/test/resources/cloud.properties"))
						.getAbsolutePath());

		// Use the development files for doing the test !!
		System.getProperties().put(
				IterSymbolsNames.ITER_DIR,
				(new File("src/main/resources/at/ac/tuwien/iter/octave/"))
						.getAbsolutePath());

		// Remove the test-result file if any !
		File testResultFile = new File(
				"src/test/resources/test-results-test.xml");
		if (testResultFile.exists()) {
			testResultFile.renameTo(new File(String.format(
					"src/test/resources/test-results-test.xml.bkp.%f",
					Math.random())));
		}

	}

	@After
	public void tearDown() {
		// Leave matlab open
		// registry.shutdown();
	}

	@Test
	public void bootstrapAndGetBestValues() throws MalformedURLException {

		String customerName = "tes";
		String serviceName = "tes";
		URL jmeterClientsURL = new URL(
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx");
		URL manifestURL = new URL(
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml");
		int nParallelTests = 1;
		int nBestPredictions = 10;
		int nInitialTests = 1;
		URL joperaURL = new URL(
				"http://10.99.0.118:8080/rest/Autocles/Autocles/1.0/");

		// NOTA Questo non deve essere fatto... Load gen dipende da questo ma
		// non e' bello che lo sia...
		// Forse meglio Factory con parametro ? Bho.

		System.getProperties().put("arg:jmeter-clients-url",
				jmeterClientsURL.toString());
		System.getProperties().put("arg:service-manifest-url",
				manifestURL.toString());

		// TODO Where this is really done ?
		System.getProperties().put(IterSymbolsNames.PROBLEM_SIZE, "10");
		// TODO THIS MUST BE DEFINED IN A BETTER WAY ...
		System.getProperties().put(IterSymbolsNames.LB, "10.0,0.0");
		System.getProperties().put(IterSymbolsNames.UB, "50.0,0.01 ");

		// Build the registry
		RegistryBuilder builder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);
		registry = builder.build();
		registry.performRegistryStartup();

		LoadGenerator loadGenerator = registry.getService(
				LoadGeneratorSource.class).getLoadGenerator("sine");

		RegistryShutdownHub registryShutdownHub = registry
				.getService(RegistryShutdownHub.class);
		MathEngineDao mathEngineDao = registry.getService(MathEngineDao.class);
		TypeCoercer typeCoercer = registry.getService(TypeCoercer.class);
		AssertionService assertionService = registry
				.getService(AssertionService.class);
		SymbolSource symbolSource = registry.getService(SymbolSource.class);

		LoadGeneratorSource loadGeneratorSource = registry
				.getService(LoadGeneratorSource.class);

		DataCollectionService dataCollectionService = registry
				.getService(DataCollectionService.class);

		// If this is not define we must stop everything
		File testResultFile = new File(
				symbolSource.valueForSymbol(IterSymbolsNames.TEST_RESULTS_FILE));

		// This may fails
		File bootstrapFile = null;

		try {
			bootstrapFile = new File(
					symbolSource.valueForSymbol(IterSymbolsNames.INPUT_FILE));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		TestSuiteEvolver plasticityEvolver = registry.getService(
				"PlasticityEvolver", TestSuiteEvolver.class);

		IterImpl iter = new IterImpl(
				LoggerFactory.getLogger(Iter.class), //
				customerName,
				serviceName, //
				nParallelTests,
				nInitialTests, //
				testResultFile,
				bootstrapFile, //
				joperaURL,//
				DEFAULT_TIMEOUT,
				true,// Bootstrap
				false, // Regression
				false, // DryRun
				// Services
				loadGenerator, registryShutdownHub, typeCoercer,
				assertionService, dataCollectionService, plasticityEvolver,
				loadGeneratorSource);

		// Bootstrap from file
		try {
			iter.bootstrap();
		} catch (TestExecutionException e) {
			e.printStackTrace();
		}

		// Get Best Improvements = Plasticity evolver do not consider the input
		// test suite, as that is directly loaded inside matlab on the shared
		// dao !
		Collection<at.ac.tuwien.iter.data.Test> newTestCases = plasticityEvolver
				.evolveTestSuite(iter.getTestSuite(), iter.getTestResults());

		for (at.ac.tuwien.iter.data.Test test : newTestCases) {
			System.out.println("GetBestEITest.bootstrapAndGetBestValues() : "
					+ Arrays.toString(test.getParameters()));
		}
	}
}
