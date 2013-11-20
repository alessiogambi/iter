package at.ac.tuwien.iter.matlab;

import java.util.List;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.executors.ConfigurationManager;
import at.ac.tuwien.iter.modules.IterModule;
import at.ac.tuwien.iter.services.impl.matlab.MatlabControlImpl;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

public class AddTestExecutionTest {

	private MatlabControlImpl dao;
	private static Registry registry;

	@BeforeClass
	public static void setupOctaveEngine() {
		RegistryBuilder builder = new RegistryBuilder();
		// IOCUtilities.addDefaultModules(builder);
		builder.add(IterModule.class);
		registry = builder.build();
	}

	@Before
	public void setup() throws MatlabConnectionException,
			MatlabInvocationException {

		// I suspect that this will open a new matlab !
		dao = new MatlabControlImpl(
				LoggerFactory.getLogger(MatlabControlImpl.class),
				registry.getService(RegistryShutdownHub.class), registry
						.getService(SymbolSource.class).valueForSymbol(
								IterSymbolsNames.GPML_DIR), registry
						.getService(SymbolSource.class).valueForSymbol(
								IterSymbolsNames.ITER_DIR), 10, 0.00001,
				0.0001, new double[] { 0.0, 0.0, 0.0, 0.0 }, new double[] {
						10.0, 10.0, 10.0, 10.0 }, 10,
				"src/test/resources/iter-matlab.log");
	}

	// @Test
	// public void addTestExecutions() throws MatlabInvocationException {
	// double[] stateSequence = new double[] { 1.0, 1.0, 1.0, 2.0, 3.0, 2.0,
	// 3.0, 4.0, 4.0, 4.0, 5.0, 6.0, 5.0, 5.0, 5.0, 5.0, 6.0, 5.0,
	// 6.0, 5.0, 4.0, 4.0, 3.0, 3.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0,
	// 1.0, 1.0 };
	// double[] parameters = new double[] { 10.0, 0.0, 5.0, 10.0 };
	//
	// dao.addTestExecution(stateSequence, parameters);
	//
	// dao.addTestExecution(stateSequence, parameters);
	//
	// }

	@Test
	public void getBestInputs() throws MatlabInvocationException {
		ConfigurationManager configurationManager = new ConfigurationManager(
				null, "bla", "bla", null);

		double[] stateSequence = new double[] { 1.0, 1.0, 1.0, 2.0, 3.0, 2.0,
				3.0, 4.0, 4.0, 4.0, 5.0, 6.0, 5.0, 5.0, 5.0, 5.0, 6.0, 5.0,
				6.0, 5.0, 4.0, 4.0, 3.0, 3.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0,
				1.0, 1.0 };

		double[] parameters = new double[] { 10.0, 0.0, 5.0, 0.1 };
		TypeCoercer typeCoercer = registry.getService(TypeCoercer.class);

		TestResult testResult = null;

		testResult = TestResult.newTestResult(
				at.ac.tuwien.iter.data.Test.newInstance("", "", "",
						"", typeCoercer.coerce(parameters, Number[].class)),
				configurationManager.getCustomerName(), configurationManager
						.getServiceName());
		testResult.setStates(stateSequence);
		dao.addTestExecution(testResult);

		stateSequence = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0,
				9.0, 10.0, 9.0, 8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0 };
		parameters = new double[] { 1.0, 1.0, 2.0, 0.2 };

		testResult = TestResult.newTestResult(
				at.ac.tuwien.iter.data.Test.newInstance("", "", "",
						"", typeCoercer.coerce(parameters, Number[].class)),
				configurationManager.getCustomerName(), configurationManager
						.getServiceName());
		testResult.setStates(stateSequence);
		dao.addTestExecution(testResult);

		stateSequence = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 4.0, 3.0,
				2.0, 1.0 };
		parameters = new double[] { 1.0, 0.0, 0.0, 0.9 };

		testResult = TestResult.newTestResult(
				at.ac.tuwien.iter.data.Test.newInstance("", "", "",
						"", typeCoercer.coerce(parameters, Number[].class)),
				configurationManager.getCustomerName(), configurationManager
						.getServiceName());
		testResult.setStates(stateSequence);
		dao.addTestExecution(testResult);

		List<double[]> results = dao.getBestPlasticityTests(4);
		for (double[] row : results) {
			for (int col = 0; col < row.length; col++) {
				System.out.print(row[col] + " ");
			}
			System.out.print("\n");
		}
	}

	@After
	public void closeMatlab() throws MatlabInvocationException {
		dao.exit();
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
}
