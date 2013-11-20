package at.ac.tuwien.iter.loadgenerators;

import java.util.Arrays;

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
import at.ac.tuwien.iter.services.impl.loadgenerators.RandomInputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class TriangleLoadGeneratorTest {

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
		generator = new TriangleLoadGenerator(
				LoggerFactory.getLogger(TriangleLoadGenerator.class),
				"TriangleLoadGenerator",
				registry.getService(TypeCoercer.class),
				registry.getService(SymbolSource.class).valueForSymbol(
						IterSymbolsNames.TRACEGENERATOR_URL),
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx",
				"", // ampliBounds - 50 to 100 clients
				50.0, 100.0,
				// period Bounds - 1 to 5 minutes
				60.0, 300, //
				100, // nBins
				new RandomInputSampler());
	}

	@Test
	public void generateRandomTestCase() {
		at.ac.tuwien.iter.data.Test test = generator.generateRandomCase();
		System.out.println("TriangleLoadGeneratorTest.generateTestCase() "
				+ test.getTraceURL());
		System.out
				.println("TriangleLoadGeneratorTest.generateRandomTestCase() "
						+ Arrays.toString(test.getParameters()));
	}

	@Test
	public void generateTestCase() {
		at.ac.tuwien.iter.data.Test test = generator.generateTest(0.94, 85.0);
		System.out.println("TriangleLoadGeneratorTest.generateTestCase() "
				+ test.getTraceURL());
		System.out
				.println("TriangleLoadGeneratorTest.generateRandomTestCase() "
						+ Arrays.toString(test.getParameters()));
	}

	@AfterClass
	public static void stopAll() {
		registry.shutdown();
	}
}
