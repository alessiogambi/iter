package at.ac.tuwien.iter.services.impl.matlab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import matlabcontrol.LoggingMatlabProxy;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.slf4j.Logger;

import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.MathEngineDao;

/**
 * This implementation uses matlabcontrol that must be put on the classpath.
 * 
 * TODO I still need to figure out how to call everything, and how to structure
 * organize the access to its objects.
 * 
 * It uses the concept of sessions, but it is not multithreading-prone we
 * declare this service as singleton ?
 * 
 * 
 * THe documentation is available here:https://code.google.com/p/matlabcontrol/
 * but they said also: Controlling the MATLAB GUI from Java This approach is for
 * those that want to control a MATLAB session (or multiple sessions) that can
 * also be used by a user. All of the solutions in this approach rely on the
 * Java MATLAB Interface (JMI). matlabcontrol was created to control either a
 * MATLAB session from within it, or one or more sessions of the MATLAB from a
 * Java program not launched from within MATLAB. Those launched from Java are
 * done without any need for user interaction: the user does not need to change
 * MATLAB configurations, launch MATLAB, or type any special commands into
 * MATLAB so that it can be controlled
 * 
 * 
 * Instead we need:Controlling the MATLAB engine
 * 
 * 
 * 
 * 
 * For the moment I kept the same structure as the OctaveImpl
 * 
 * @author alessiogambi
 * 
 */
public class MatlabControlImpl implements MathEngineDao {

	private Logger logger;
	// /*
	// * Various indices on the test executions
	// */
	// private Map<String, String> testExecutions; // test-execution-ID,
	// // matlab-variable-name
	//
	// private List<String> criticalTestCases; // test-execution-ID

	private LoggingMatlabProxy proxy;
	private MatlabProxyFactory factory;
	private MatlabTypeConverter processor;
	private Object lock;
	private int nParameters;

	// Need a queued access to the session otherwise it opens several matlab ?
	// FIXME Asking for the problem_size at this time is bad but I cannot think
	// about anything else right now
	public MatlabControlImpl(Logger logger, RegistryShutdownHub shutdownHub,
			String gpmlDir, String iterDir, int problemSize, double tol,
			double min_ei, double[] LB, double[] UB, int nBins,
			String matlabLogFile) throws MatlabConnectionException,
			MatlabInvocationException {
		this.logger = logger;

		logger.info("LB " + Arrays.toString(LB));
		logger.info("UB " + Arrays.toString(UB));

		MatlabProxyFactoryOptions.Builder builder = new MatlabProxyFactoryOptions.Builder();
		builder.setHidden(true);

		factory = new MatlabProxyFactory();
		proxy = new LoggingMatlabProxy(factory.getProxy());

		// Add shutdown hook to exit matlab once everything its over
		// Use @PostInjection
		shutdownHub.addRegistryShutdownListener(new Runnable() {
			public void run() {
				try {
					if (proxy.isConnected()) {
						proxy.exit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		// Instantiate utils and other objects
		processor = new MatlabTypeConverter(proxy);

		// Load and setup GPML libraries
		proxy.eval(String.format("addpath ('%s');", gpmlDir));
		proxy.eval("startup;");
		// Load and setup our libraries
		proxy.eval(String.format("addpath ('%s');", iterDir));

		double[][] _LB = new double[1][];
		_LB[0] = LB;
		processor.setNumericArray("LB", new MatlabNumericArray(_LB, null));
		double[][] _UB = new double[1][];
		_UB[0] = UB;
		processor.setNumericArray("UB", new MatlabNumericArray(_UB, null));

		// NOTE THAT STRINGs mist have ''
		proxy.eval(String.format("startup_iter(%d,%f,%f,LB,UB,%d, '%s');",
				problemSize, tol, min_ei, nBins, matlabLogFile));

		lock = new Object();

		nParameters = LB.length;

		// TODO Configure me to run only id debug mode!
		// listTestExecution();

	}

	public void addTestExecution(TestResult testResult)
			throws MatlabInvocationException {
		this.addTestExecution(testResult.getStates(),
				testResult.getParameters());
	}

	private void addTestExecution(double[] stateSequence, double... parameters)
			throws MatlabInvocationException {

		synchronized (lock) {

			try {

				logger.debug("MatlabControlImpl.addTestExecution(): "
						+ Arrays.toString(stateSequence));

				// We must convert the arrays into matlab vars and then pass
				// them by
				// name -> MAKE THIS A COLUMN VECTOR
				// double[][] _stateSequence = new double[1][];
				// _stateSequence[0] = stateSequence;

				// The vector MUST BE A COLUMN VECTOR !
				double[][] _stateSequence = new double[stateSequence.length][1];
				for (int i = 0; i < stateSequence.length; i++) {
					_stateSequence[i][0] = stateSequence[i];
				}
				processor.setNumericArray("stateSequence",
						new MatlabNumericArray(_stateSequence, null));

				double[][] _parameters = new double[1][];
				_parameters[0] = parameters;
				processor.setNumericArray("parameters", new MatlabNumericArray(
						_parameters, null));

				// I have big problems in passing arrays as inputs using feval
				// proxy.feval("update_training_data", "stateSequence",
				// "parameters");
				proxy.eval("update_training_data(stateSequence,parameters);");
			} catch (MatlabInvocationException e) {
				logger.error("", e);
				throw e;
			}
		}
	}

	// This is mainly for testing purposes
	Object listTestExecution() throws MatlabInvocationException {
		try {
			// Enable logging
			proxy.showInConsoleHandler();

			proxy.eval("global training_data");
			Object result = proxy.getVariable("training_data");
			logger.info("Training Data = " + result);
			/*
			 * result is an object that maps the cell array in matlab:
			 * result.lenght=nxn result[i]=object[2] result[i][0]=string[#of
			 * cell elements names] for us is 2 parameters, phi
			 * result[i][1]=object[#of test executions]
			 * result[i][1][t]=object[#of cell elements values] for us is 2
			 * result[i][1][t][0]=double[#of paramters] -> values of parameters
			 * result[i][1][t][1]=double[1] -> phi
			 */

			return result;
		} catch (MatlabInvocationException e) {
			logger.error("", e);
			throw e;
		}
	}

	// TODO Is this really needed?
	public void removeTestExecution(String ID) {
		// TODO Auto-generated method stub

	}

	// TODO Registered test cases... maybe this is better to have it
	// elsewhere... not in matlab
	public List<double[]> getCriticalTestCases() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Retrieve the (at most) n best parameters values according to the
	 * max(E[I]) formula. All the returned values have a max improvement that is
	 * greater or equals to minEI
	 * 
	 * @throws MatlabInvocationException
	 */
	public List<double[]> getBestPlasticityTests(int n)
			throws MatlabInvocationException {
		synchronized (lock) {

			/*
			 * IMPORTANT: Now that we use the discrete/integer problem we run
			 * into a non-termination issue, this results in the call possibly
			 * returning again the same tests. We need to deal with it: the
			 * heuristic is to take a random test 'far-away' from the repeated
			 * one and move on. If the same happens several time we are
			 * confident that the 'real' optimum is actually the one returned.
			 */
			logger.debug("MatlabControlImpl.getBestPlasticityTests() " + n);
			proxy.setVariable("n", n);
			Object[] result = proxy.returningEval(
					"get_best_expected_improvements(n);", 1);

			double[] _result = (double[]) (result[0]);

			/*
			 * We know that each row will contains p+2+1 columns, the first two
			 * are the i,j values that identify the transition, the third is
			 * max_ei and the others p columns are the parameters that define
			 * the test case
			 */
			int nColums = nParameters + 2 + 1;
			int nRows = _result.length / nColums;

			List<double[]> theResult = new ArrayList<double[]>(nRows);
			// Initialize the structure
			for (int row = 0; row < nRows; row++) {
				theResult.add(new double[nColums]);
			}

			for (int i = 0; i < _result.length; i++) {
				theResult.get(i % nRows)[i / nRows] = _result[i];
			}

			return theResult;
		}
	}

	public void exit() throws MatlabInvocationException {
		if (proxy.isConnected()) {
			proxy.exit();
		}
	}

	// I, J, Phi
	public List<double[]> inferModel(double[] stateSequence)
			throws MatlabInvocationException {

		logger.info("Infer Model from " + Arrays.toString(stateSequence));
		List<double[]> result = new ArrayList<double[]>();

		synchronized (lock) {
			try {
				// We must convert the arrays into matlab vars and then pass
				// them by name;

				// The vector MUST BE A COLUMN VECTOR !
				double[][] _stateSequence = new double[stateSequence.length][1];
				for (int i = 0; i < stateSequence.length; i++) {
					_stateSequence[i][0] = stateSequence[i];
				}

				processor.setNumericArray("stateSequence",
						new MatlabNumericArray(_stateSequence, null));

				Object[] theResult = proxy.returningEval(
						"infer_markov_model(stateSequence);", 1);

				double[] _result = (double[]) (theResult[0]);

				// We know that each row will contains 3 columns (i,j,phi_i,j)
				int nColums = 3;
				int nRows = _result.length / nColums;

				// logger.debug("MatlabControlImpl.inferModel() Col = "
				// + nColums);
				// logger.debug("MatlabControlImpl.inferModel() Row = "
				// + nRows);

				result = new ArrayList<double[]>(nRows);

				// Initialize the structure
				for (int row = 0; row < nRows; row++) {
					result.add(new double[nColums]);
				}

				for (int i = 0; i < _result.length; i++) {
					result.get(i % nRows)[i / nRows] = _result[i];
				}

				if (result.size() > 0) {
					logger.info("Inferred model as transition list: ");
					for (double[] transition : result) {
						logger.info(String.format("%f,%f -> %f", transition[0],
								transition[1], transition[2]));
					}
				} else {
					logger.info("No transitions were inferred !");
				}

			} catch (MatlabInvocationException e) {
				logger.error("", e);
				throw e;
			}
		}
		return result;
	}
}
