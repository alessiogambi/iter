package at.ac.tuwien.iter.loadgenerators;

import javax.validation.ValidationException;

import org.apache.commons.cli.ParseException;
import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.gambi.tapestry5.cli.services.CLIParser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.impl.loadgenerators.InputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class LoadGeneratorTest {

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

	@Test
	public void generateTestCase() throws ValidationException, ParseException {
		String manifestURL = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml";
		String jmxURL = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx";
		String[] args = { "-c", "ite", "-s", "ite", "-m", manifestURL, "-j",
				jmxURL };

		registry.getService(CLIParser.class).parse(args);

		LoadGeneratorSource generatorSource = registry
				.getService(LoadGeneratorSource.class);
		LoadGenerator generator = generatorSource.getLoadGenerator("sine-lhs");

		at.ac.tuwien.iter.data.Test test = generator.generateRandomCase();
		System.out.println("LoadGeneratorTest.generateTestCase() test "
				+ test.getTraceURL());
	}

	@Test
	public void generateSinusTestCase() {
		generator = new SinusLoadGenerator(
				LoggerFactory.getLogger(LoadGenerator.class),
				"SinusLoadGenerator",
				registry.getService(TypeCoercer.class),
				registry.getService(SymbolSource.class).valueForSymbol(
						IterSymbolsNames.TRACEGENERATOR_URL),
				"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx",
				"", 1.0, 30.0, 0.0, 0.001, //
				100, // nBins
				registry.getService("RandomSinusLoadGenerator",
						InputSampler.class), 300);
		generator.generateRandomCase();
	}

	@AfterClass
	public static void stopAll() {
		registry.shutdown();
	}
}
