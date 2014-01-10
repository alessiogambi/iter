package at.ac.tuwien.iter.services.impl;

import java.io.File;
import java.net.URL;

import javax.validation.ValidationException;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.gambi.tapestry5.cli.services.CLIOptionSource;
import org.gambi.tapestry5.cli.services.CLIParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import at.ac.tuwien.dsg.cloud.utils.CloudSymbolConstants;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.modules.TestAssertions;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.Iter;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.TestSuiteEvolver;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class BootstrapingTest {

	// Tapestry Registry for DI-IoC
	private static Registry registry;

	@Before
	public void setup() {

		// SETUP THE ENVIRONMENT: NOTE THAT THIS IS REALLY ANNOYNG !
		// NOTE THAT THIS SHOULD BE PASSED VIA COMMAND LINE TOO
		System.getProperties().put(
				IterSymbolsNames.INPUT_FILE,
				(new File("src/test/resources/bootstrap.xml"))
						.getAbsolutePath());

		// Options are specified via the cloud.properties file:
		System.getProperties().put(
				CloudSymbolConstants.CONFIGURATION_FILE,
				(new File("src/test/resources/cloud.properties"))
						.getAbsolutePath());

		RegistryBuilder builder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(builder);
		// Collection<Class> exclusionFilter = new ArrayList<Class>();
		// exclusionFilter.add(BeanValidatorModule.class);
		// ExtendedIOCUtilities.addDefaultModulesWithExclusion(builder,
		// exclusionFilter);
		builder.add(IterModule.class);

		// Add the testAssertion contributions
		builder.add(TestAssertions.class);

		registry = builder.build();
		registry.performRegistryStartup();
	}

	@After
	public void shutdown() {
		registry.shutdown();
	}

	@Test
	public void defaultTestSuiteEvolver1() {
		String[] args = new String[] {
				"-l",
				"triangle-lhs",
				"-n",
				"1",
				"-N",
				"1",
				"-c",
				"ale",
				"-s",
				"ale",
				"-m",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				"-j",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx" };

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}

		try {
			registry.getService(CLIOptionSource.class).valueForOption(
					"evolve-with");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("evolve-with")
					&& e.getMessage().contains("not defined"));
		}
	}

	@Test
	public void defaultTestSuiteEvolver2() {
		String[] args = new String[] {
				"-l",
				"triangle-lhs",
				"-c",
				"ale",
				"-s",
				"ale",
				"-m",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				"-j",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx" };

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}

	}

	// @Test
	public void plasticityTestSuiteEvolver() {
		String[] args = new String[] {
				"-e",
				"plasticity",
				"-l",
				"triangle-lhs",
				"-n",
				"1",
				"-N",
				"1",
				"-r",
				"0",
				"-c",
				"ale",
				"-s",
				"ale",
				"-m",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				"-j",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx" };

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}

	}

	@Test
	public void inputFile() {
		String inputFile = "bootstrap.xml";
		String outputFile = "triangle-test-result.xml";
		String[] args = new String[] {
				"-b",
				"--input-file",
				inputFile,
				"-l",
				"triangle-lhs",
				"--output-file",
				outputFile,
				"-n",
				"1",
				"-N",
				"1",
				"-r",
				"0",
				"-c",
				"ale",
				"-s",
				"ale",
				"-m",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				"-j",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx" };

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

			// Iter iter = registry.getService(Iter.class);
			// iter.start();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}

	}

	@Test
	public void parseCommandLine() {
		String[] args = "-b -l constant-lhs -n 5 -N 1 -r 10 -c ale -s ale -m http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml -j http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
				.split(" ");

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}
	}

	// @Test
	public void consistencyError() {
		String[] args = "-i input-file.xml -l constant-lhs -n 5 -N 1 -r 10 -c ale -s ale -m http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml -j http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
				.split(" ");

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);
		} catch (ValidationException e) {
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}
	}

	// @Test
	public void noConsistencyError() {
		String[] args = "-b -i input-file.xml -l constant-lhs -n 5 -N 1 -r 10 -c ale -s ale -m http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml -j http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
				.split(" ");

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}
		Assert.assertNotNull(registry.getService(CLIOptionSource.class)
				.valueForOption("bootstrap"));
	}

	@Test
	public void bootstrapDryrunNoOutput() {
		String[] args = {
				"-b",
				"-i",
				"src/test/resources/bootstrap/input-file.xml",
				"-l",
				"constant-lhs",
				"-n",
				"5",
				"-N",
				"1",
				"-c",
				"ale",
				"-s",
				"ale",
				"-m",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				"-j",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx" };
		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

			Iter iter = registry.getService(Iter.class);
			// Note that b is actually specified and by default the evolver is
			// NO evolution
			iter.start();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}

		CLIOptionSource optionSource = registry
				.getService(CLIOptionSource.class);
		Assert.assertTrue("true".equals(optionSource.valueForOption("b")));
	}

	@Test
	public void bannerStatsAndRepeat() {
		String[] args = {
				"-b",
				"-i",
				"src/test/resources/bootstrap/input-file.xml",
				"-l",
				"constant-lhs",
				"-n",
				"5",
				"-N",
				"1",
				"-r",
				"0",
				"-c",
				"ale",
				"-s",
				"ale",
				"-m",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				"-j",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx" };

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);

			Logger logger = org.slf4j.LoggerFactory.getLogger(Iter.class);
			// THis is not safe
			RegistryShutdownHub registryShutdownHub = registry
					.getService(RegistryShutdownHub.class);
			TypeCoercer typeCoercer = registry.getService(TypeCoercer.class);
			AssertionService assertionService = registry
					.getService(AssertionService.class);
			DataCollectionService dataCollectionService = registry
					.getService(DataCollectionService.class);
			// This is needed for the Boostrap part as we generate on the fly
			// tests, and tests require a proper LoadGenerator
			LoadGeneratorSource loadGeneratorSource = registry
					.getService(LoadGeneratorSource.class);
			// Is this really needed as we already inject the loadGenSource
			LoadGenerator loadGenerator = registry.getService(
					"CommandLineLoadGenerator", LoadGenerator.class);
			TestSuiteEvolver testSuiteEvolver = registry.getService(
					"CommandLineTestSuiteEvolver", TestSuiteEvolver.class);

			CLIOptionSource optionSource = registry
					.getService(CLIOptionSource.class);

			SymbolSource symbolSource = registry.getService(SymbolSource.class);

			String customerName = optionSource.valueForOption("customer-name");
			String serviceName = optionSource.valueForOption("service-name");

			int nParallelTests = typeCoercer.coerce(
					optionSource.valueForOption("n-parallel-tests"), int.class);
			int nInitialTests = typeCoercer.coerce(
					optionSource.valueForOption("n-initial-random-tests"),
					int.class);

			// NOTE THIS USES SymbolSource
			URL joperaURL = typeCoercer.coerce(
					symbolSource.valueForSymbol(IterSymbolsNames.JOPERA_URL),
					URL.class);
			// NOTE THIS USES SymbolSource
			long experimentTimetout = typeCoercer.coerce(symbolSource
					.valueForSymbol(IterSymbolsNames.EXPERIMENT_TIMEOUT),
					long.class);
			File testResultFile = typeCoercer.coerce(
					optionSource.valueForOption("output-file"), File.class);
			boolean bootstrap = typeCoercer.coerce(
					optionSource.valueForOption("bootstrap"), boolean.class);
			boolean regression = typeCoercer.coerce(
					optionSource.valueForOption("regression"), boolean.class);
			File bootstrapFile = typeCoercer.coerce(
					optionSource.valueForOption("input-file"), File.class);

			IterImpl iter = new IterImpl(logger, customerName, serviceName,
					nParallelTests, nInitialTests, testResultFile,
					bootstrapFile, joperaURL, experimentTimetout, bootstrap,
					regression, loadGenerator, registryShutdownHub,
					typeCoercer, assertionService, dataCollectionService,
					testSuiteEvolver, loadGeneratorSource);

			iter.bootstrap();

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("An Exception was generated");
		}

		Assert.assertNotNull("true".equals(registry.getService(
				CLIOptionSource.class).valueForOption("b")));
	}
}
