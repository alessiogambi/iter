package at.ac.tuwien.iter.main;

import javax.validation.ValidationException;

import org.apache.commons.cli.ParseException;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.gambi.tapestry5.cli.services.CLIParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.iter.modules.IterModule;

public class CLITest {
	// Tapestry Registry for DI-IoC
	private static Registry registry;

	@Before
	public void setup() {
		RegistryBuilder builder = new RegistryBuilder();
		// NO ! Load all the modules in the cp
		// IOCUtilities.addDefaultModules(builder);
		// Load all the local modules
		builder.add(IterModule.class);
		registry = builder.build();
		registry.performRegistryStartup();
	}

	@After
	public void shutdown() {
		registry.shutdown();
	}

	@Test
	public void doTheTest() {
		String[] args = "-l constant-lhs -n 5 -N 1 -r 10 -c ale -s ale -m http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml -j http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
				.split(" ");

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);

			parser.parse(args);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void failTheTest() {
		String[] args = "-l not-a-valid-load-gen  -n 5 -N 1 -r 10 -c ale -s ale -m http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml -j http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
				.split(" ");

		try {
			// This can generate exception if parsing or validation fail !
			CLIParser parser = registry.getService(CLIParser.class);
			parser.parse(args);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e instanceof ValidationException
					|| e instanceof ParseException);
		}
	}

	// @Test
	// public void failTheTest() {
	// try {
	// String[] args =
	// "-n a -r 10 -c ale -s ale -m http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml -j http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
	// .split(" ");
	//
	// // This can generate exception if parsing or validation fail !
	// ApplicationConfiguration configuration = registry.getService(
	// ApplicationConfigurationProvider.class).provide(args);
	//
	// Iter iter = registry
	// .getService(at.ac.tuwien.iter.services.Iter.class);
	// } catch (RuntimeException e) {
	// System.out.println("IterTest.failTheTest() Ok ");
	// e.printStackTrace();
	// }
	//
	// }
}
