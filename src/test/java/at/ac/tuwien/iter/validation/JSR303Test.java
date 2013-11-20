package at.ac.tuwien.iter.validation;

import java.io.File;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.gambi.tapestry5.cli.data.ApplicationConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.cloud.utils.CloudSymbolConstants;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class JSR303Test {

	private static Registry registry;

	@Before
	public void setupValidation() {

		// Overwrite default for files
		System.getProperties().put(
				IterSymbolsNames.TEST_RESULTS_FILE,
				(new File("src/test/resources/test-results-test.xml"))
						.getAbsolutePath());

		System.getProperties().put(
				IterSymbolsNames.BOOTSTRAP_FILE,
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

		// Build the registry
		RegistryBuilder builder = new RegistryBuilder();
		// TODO Because of the beanvalidaion module we canno just add all the
		// modules in the CP.
		// If you need that, just download the code from git, create another
		// "module" and remove the orginal tapestry-beanvalidator
		builder.add(IterModule.class);
		registry = builder.build();
		registry.performRegistryStartup();

	}

	@After
	public void tearDown() {
		// Leave matlab open
		registry.shutdown();
	}

	@Test
	public void validate() throws ParseException {

		/*
		 * String customerName = "tes"; String serviceName = "tes"; URL
		 * jmeterClientsURL = new URL(
		 * "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"
		 * ); URL manifestURL = new URL(
		 * "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml"
		 * ); int nParallelTests = 1; int nBestPredictions = 10; int
		 * nInitialTests = 1; URL joperaURL = new URL(
		 * "http://10.99.0.118:8080/rest/Autocles/Autocles/1.0/");
		 * 
		 * System.getProperties().put("arg:jmeter-clients-url",
		 * jmeterClientsURL.toString());
		 * System.getProperties().put("arg:service-manifest-url",
		 * manifestURL.toString());
		 * 
		 * NOTA Questo non deve essere fatto... Load gen dipende da questo ma //
		 * non e' bello che lo sia... // Forse meglio Factory con parametro ?
		 * Bho.
		 * 
		 * // TODO Where this is really done ?
		 * System.getProperties().put(IterSymbolsNames.PROBLEM_SIZE, "10"); //
		 * TODO THIS MUST BE DEFINED IN A BETTER WAY ...
		 * System.getProperties().put(IterSymbolsNames.LB, "10.0,0.0");
		 * System.getProperties().put(IterSymbolsNames.UB, "50.0,0.01 ");
		 */

		String[] arguments = new String[] { "-b", "-c", "tes", "-s", "service" };

		CommandLineParser parser = new BasicParser();
		// Contributions go directly into the CLIParser Object
		// CommandLine parsedOptions = parser.parse(optionSource.getOptions(),
		// arguments);
		// ApplicationConfiguration appConf = new IterApplicationConfiguration(
		// parsedOptions);

		// Do the validation of the bean/object
		// Validator validator = registry.getService(Validator.class);
		//
		// Set<ConstraintViolation<ApplicationConfiguration>> errors = validator
		// .validate(appConf);
		//
		// for (ConstraintViolation<ApplicationConfiguration> next : errors) {
		// System.out.println("JSR303Test.validate() " + next.getMessage());
		// }
		//
		// for (ConstraintViolation<ApplicationConfiguration> next : errors) {
		// System.out.println("JSR303Test.validate() " + next.getMessage());
		// }

	}
}
