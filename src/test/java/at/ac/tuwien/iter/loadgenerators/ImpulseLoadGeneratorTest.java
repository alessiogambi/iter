package at.ac.tuwien.iter.loadgenerators;

import java.util.List;

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
import at.ac.tuwien.iter.services.impl.loadgenerators.LatinHypercubeInputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class ImpulseLoadGeneratorTest {

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
		generator = new ImpulseLoadGenerator(
				LoggerFactory.getLogger(ImpulseLoadGenerator.class),
				"RampLoadGenerator",
				registry.getService(TypeCoercer.class),
				registry.getService(SymbolSource.class).valueForSymbol(
						IterSymbolsNames.TRACEGENERATOR_URL),
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx",
				"", //
				0.0, 400.0,//
				120,//
				30, 30,//
				20, // nBins
				new LatinHypercubeInputSampler(LoggerFactory
						.getLogger(LatinHypercubeInputSampler.class)), 300);
	}

	@Test
	public void generateRandomeTestCase() {
		at.ac.tuwien.iter.data.Test test = generator.generateRandomCase();
		System.out.println("RampLoadGeneratorTest.generateRandomeTestCase() "
				+ test.getTraceURL());
	}

	@Test
	public void generateLHSTestCase() {
		List<at.ac.tuwien.iter.data.Test> suite = generator
				.generateInitialTestSuite(10);
		for (at.ac.tuwien.iter.data.Test test : suite) {
			System.out.println(test.getTraceURL());
		}
	}

	@AfterClass
	public static void stopAll() {
		registry.shutdown();
	}
}
