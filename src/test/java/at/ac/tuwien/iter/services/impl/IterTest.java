package at.ac.tuwien.iter.services.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.utils.CloudSymbolConstants;
import at.ac.tuwien.iter.exceptions.TestExecutionException;
import at.ac.tuwien.iter.loadgenerators.SinusLoadGenerator;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.impl.evo.StopTestSuiteEvolution;
import at.ac.tuwien.iter.services.impl.loadgenerators.InputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class IterTest {

	private static final long DEFAULT_TIMEOUT = 10 * 60 * 1000l;

	public static void main(String[] args) throws MalformedURLException {

		System.getProperties().put(CloudSymbolConstants.CONFIGURATION_FILE,
				"src/test/resources/cloud.properties");

		// TODO Auto-generated constructor stub
		RegistryBuilder builder = new RegistryBuilder();
		// Load all the modules in the cp
		IOCUtilities.addDefaultModules(builder);
		// Load all the local modules
		builder.add(IterModule.class);
		Registry registry = builder.build();
		registry.performRegistryStartup();

		LoadGeneratorSource generatorSource = registry
				.getService(LoadGeneratorSource.class);
		// Setup
		LoadGenerator loadGenerator = new SinusLoadGenerator(
				LoggerFactory.getLogger(LoadGenerator.class),
				"SinusLoadGenerator",
				registry.getService(TypeCoercer.class),
				registry.getService(SymbolSource.class).valueForSymbol(
						IterSymbolsNames.TRACEGENERATOR_URL),
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx",
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml",
				0.0, 30.0, 0.0, 0.01, //
				100, // nBins

				registry.getService("RandomSinusLoadGenerator",
						InputSampler.class), 300);

		File bootstrapFile = new File(registry.getService(SymbolSource.class)
				.valueForSymbol(IterSymbolsNames.INPUT_FILE));
		File testResultsFile = new File(registry.getService(SymbolSource.class)
				.valueForSymbol(IterSymbolsNames.TEST_RESULTS_FILE));

		IterImpl iter = new IterImpl(
				// Resources
				LoggerFactory.getLogger(IterTest.class),
				// User inputs
				"ale",
				"bbv",
				// Experiment setup
				1,
				1,
				// Input-output
				testResultsFile,
				bootstrapFile,
				// Experimental Environment
				registry.getService(TypeCoercer.class).coerce(
						registry.getService(SymbolSource.class).valueForSymbol(
								IterSymbolsNames.JOPERA_URL), URL.class),
				// Experiment setup
				DEFAULT_TIMEOUT,
				true, // Bootstrap
				// Other services
				loadGenerator, registry.getService(RegistryShutdownHub.class),
				registry.getService(TypeCoercer.class),
				registry.getService(AssertionService.class),
				registry.getService(DataCollectionService.class),
				new StopTestSuiteEvolution(), generatorSource);

		try {
			iter.bootstrap();
		} catch (TestExecutionException e) {
			e.printStackTrace();
		}
		;
	}
}
