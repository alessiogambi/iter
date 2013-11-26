package at.ac.tuwien.iter.loadgenerators;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.impl.loadgenerators.InputSampler;
import at.ac.tuwien.iter.services.impl.loadgenerators.RandomInputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class SinusLoadGeneratorTest {

	private static Registry registry;
	private LoadGenerator generator;

	@BeforeClass
	public static void setup() {
		System.getProperties().put("at.ac.tuwien.dsg.cloud.configuration",
				"./conf/cloud.properties");
		RegistryBuilder builder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);
		registry = builder.build();
		registry.performRegistryStartup();
	}

	@Before
	public void createGenerator() {// Setup
		generator = new SinusLoadGenerator(
				LoggerFactory.getLogger(SinusLoadGenerator.class),
				"SinusLoadGenerator",
				registry.getService(TypeCoercer.class),
				registry.getService(SymbolSource.class).valueForSymbol(
						IterSymbolsNames.TRACEGENERATOR_URL),
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx",
				"", 1.0, 30.0, 0.0, 0.001, //
				100, // nBins
				new RandomInputSampler(), 300);
	}

	@Test
	public void generateTestCase() {
		generator.generateRandomCase();
	}

	@AfterClass
	public static void stopAll() {
		registry.shutdown();
	}
}
