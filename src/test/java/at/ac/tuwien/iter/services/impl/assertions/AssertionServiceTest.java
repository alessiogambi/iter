package at.ac.tuwien.iter.services.impl.assertions;

import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.cloud.utils.CloudSymbolConstants;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.data.TestResultsCollector;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class AssertionServiceTest {

	private AssertionService assertionService;
	private Registry registry;
	private String singleTestResultFile;
	private String allTestResultFile;

	@Before
	public void setup() {

		System.getProperties().put("log4j.configuration",
				"file:/opt/iter/conf/log4j.properties");

		RegistryBuilder builder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);

		System.getProperties().put(CloudSymbolConstants.CONFIGURATION_FILE,
				"src/test/resources/cloud.properties");

		System.getProperties().put("arg:n-best-tests", "1");
		System.getProperties().put("arg:n-parallel-tests", "1");
		System.getProperties().put("arg:n-initial-random-tests", "50");
		System.getProperties().put("arg:customer-name", "ale");
		System.getProperties().put("arg:service-name", "ale");
		System.getProperties()
				.put("arg:service-manifest-url",
						"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml");
		System.getProperties()
				.put("arg:jmeter-clients-url",
						"http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx");

		registry = builder.build();
		registry.performRegistryStartup();

		assertionService = registry.getService(AssertionService.class);

		LoadGenerator loadGenerator = registry.getService(
				LoadGeneratorSource.class).getLoadGenerator("triangle-lhs");

		System.getProperties().put(IterSymbolsNames.LB,
				Arrays.toString(loadGenerator.getLowerBounds()));
		System.getProperties().put(IterSymbolsNames.UB,
				Arrays.toString(loadGenerator.getUpperBounds()));

		singleTestResultFile = "src/test/resources/test-result.xml";

		allTestResultFile = "src/test/resources/test-results.xml";
	}

	@Test
	public void check() {
		// Load some test results from file, and check them
		try {
			TestResultsCollector testResultsCollector = TestResultsCollector
					.loadFromFile(singleTestResultFile);

			for (TestResult testResult : testResultsCollector) {
				assertionService.check(testResult);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void checkAll() {
		// Load some test results from file, and check them
		try {
			TestResultsCollector testResultsCollector = TestResultsCollector
					.loadFromFile(allTestResultFile);

			for (TestResult testResult : testResultsCollector) {
				System.out.println("AssertionServiceTest.checkAll() checking "
						+ testResult.getLoadGeneratorID() + " with parameters "
						+ testResult.getParameters());
				assertionService.check(testResult);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void shutdown() {
		registry.shutdown();
	}
}
