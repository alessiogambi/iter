package at.ac.tuwien.iter.matlab;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

import org.apache.tapestry5.ioc.IOCUtilities;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.executors.ConfigurationManager;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class InferMarkovModelTest {

	private static Registry registry;
	private double[] testinput;
	private MathEngineDao dao;
	private ConfigurationManager configurationManager;
	private static final String jmxFileURL = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx";

	private TestResult testResult;

	@BeforeClass
	public static void setupOctaveEngine() {
		RegistryBuilder builder = new RegistryBuilder();
		IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);
		registry = builder.build();
		registry.performRegistryStartup();

		// Additional user provided informations
		// How many states do we have
		System.getProperties().put(IterSymbolsNames.PROBLEM_SIZE, "6");
		System.getProperties().put(IterSymbolsNames.LB, "0.0, 0.0, 0.0");
		System.getProperties().put(IterSymbolsNames.UB, "20.0,20.0,20.0");
		System.getProperties().put("at.ac.tuwien.dsg.cloud.configuration",
				"./conf/cloud.properties");

	}

	@Before
	public void loadTestSequence() {
		dao = registry.getService("matlab", MathEngineDao.class);

		configurationManager = new ConfigurationManager("", "als", "bls", null);

		testinput = new double[] { 10.0, 0.0, 5.0 };
		testResult = TestResult.newTestResult(
				at.ac.tuwien.iter.data.Test.newInstance(
						"",
						"",
						"",
						"",
						registry.getService(TypeCoercer.class).coerce(
								testinput, Number[].class)),
				configurationManager.getCustomerName(), configurationManager
						.getServiceName());
	}

	// @AfterClass
	// public static void tearDown() {
	// registry.shutdown();
	// }

	@Test
	public void addPlasticTestExecution() throws MatlabConnectionException,
			MatlabInvocationException {
		double[] testsequence = new double[] { 1.0, 1.0, 1.0, 2.0, 2.0, 2.0,
				2.0 };
		testResult.setStates(testsequence);
		dao.addTestExecution(testResult);
	}

	@Test
	public void addTestExecution() throws MatlabConnectionException,
			MatlabInvocationException {
		double[] testsequence = new double[] { 1.0, 1.0, 1.0, 2.0, 3.0, 2.0,
				3.0, 4.0, 4.0, 4.0, 5.0, 6.0, 5.0, 5.0, 5.0, 5.0, 6.0, 5.0,
				6.0, 5.0, 4.0, 4.0, 3.0, 3.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0 };
		testResult.setStates(testsequence);
		dao.addTestExecution(testResult);
	}
}
