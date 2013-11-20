package at.ac.tuwien.iter.services.impl.evo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import matlabcontrol.MatlabInvocationException;

import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.slf4j.Logger;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.MathEngineDao;
import at.ac.tuwien.iter.services.TestSuiteEvolver;

/**
 * 
 * This class evolve the test suites by exploiting GPML models as describe in
 * the ESEC/FSE 2013 NIER-PAPER
 * 
 * @author alessiogambi
 * 
 */
public class GPMLBasedPlasticityEvolver implements TestSuiteEvolver {

	private Logger logger;

	private TypeCoercer typeCoercer;

	private LoadGenerator loadGenerator;
	private MathEngineDao mathEngineDao;

	private int nBestPredictions;

	private int limitCount;
	private int minDistance;
	private Map<Test, Integer> hitCount;

	private Collection<TestResult> testResults;

	public GPMLBasedPlasticityEvolver(Logger logger, TypeCoercer typeCoercer,
			LoadGenerator loadGenerator, MathEngineDao mathEngineDao,
			int nBestPredictions, int limitCount, int minDistance) {
		super();
		this.logger = logger;
		this.typeCoercer = typeCoercer;
		this.loadGenerator = loadGenerator;
		this.mathEngineDao = mathEngineDao;
		this.nBestPredictions = nBestPredictions;
		this.limitCount = limitCount;
		this.minDistance = minDistance;
		// ?
		this.hitCount = new HashMap<Test, Integer>();
		this.testResults = new ArrayList<TestResult>();
	}

	public Collection<Test> evolveTestSuite(Collection<Test> testSuite,
			Collection<TestResult> testResults) {

		int newTests = 0;
		// Store only the new testResults
		for (TestResult testResult : testResults) {
			if (this.testResults.contains(testResult)) {
				logger.debug("TestResult  " + testResult.getTestId()
						+ " is not new");
			} else {
				try {
					logger.debug("Storing " + testResult.getTestId());
					mathEngineDao.addTestExecution(testResult);
					this.testResults.add( testResult );
					newTests++;
				} catch (Throwable e) {
					logger.warn("Cannot add " + testResult.getTestId()
							+ " to Matlab", e);
				}
			}
		}

		logger.info(String.format("Added %d new tests to the search !",
				newTests));

		logger.info("Try to get the first " + nBestPredictions
				+ " best expected improvements");

		Collection<Test> newExperiments = new ArrayList<Test>();

		try {
			// NOTE HERE We must make the search generic !
			/*
			 * IMPORTANT: Now that we use the discrete/integer problem we run
			 * into a non-termination issue, this results in the call possibly
			 * returning again the same tests. We need to deal with it: the
			 * heuristic is to take a random test 'far-away' from the repeated
			 * one and move on. If the same happens several time we are
			 * confident that the 'real' optimum is actually the one returned.
			 */

			List<double[]> newParameters = mathEngineDao
					.getBestPlasticityTests(nBestPredictions);

			for (double[] _params : newParameters) {
				int i = (int) _params[0];
				int j = (int) _params[1];
				double maxEI = _params[2];
				logger.info("Max(E[I]) = " + maxEI + " for Transition (" + i
						+ "->" + j + ")");

				double[] params = new double[_params.length - (2 + 1)];
				System.arraycopy(_params, 3, params, 0, params.length);

				// Do the check !
				Test newTest = loadGenerator.generateTest(typeCoercer.coerce(
						params, Number[].class));

				if (testSuite.contains(newTest)) {
					// TODO How this reacts to our test repetitions?
					logger.info("IterImpl.evolveTestSuite() CHECK FAILED WE ALREADY COLLECTED THIS TEST !");

					int hitcount = hitCount.get(newTest) + 1;

					if (hitcount < limitCount) {

						newTest = loadGenerator.generatePseudoRandomTest(
								minDistance,
								typeCoercer.coerce(params, Number[].class));
					} else {

						logger.warn("IterImpl.evolveTestSuite() Max hit count reached. Do not add the test ! ");
						continue;
					}
				}

				newExperiments.add(newTest);
			}
		} catch (MatlabInvocationException e) {

			// TODO : Here we need to understand if the problem is due to the
			// impossibility of training the interpolator or other,
			// in one case we must add new data/random in other case not

			logger.error(
					"Iter.start() Something went wrong, try to add some more random experiment",
					e);

			// TODO This should be configurable as well, isn;t it ?
			// Generate random n tests !
			for (int experiment = 0; experiment < nBestPredictions; experiment++) {
				newExperiments.add(loadGenerator.generateRandomCase());

			}
		} catch (Exception e) {
			// e.printStackTrace();
			logger.error(" Generic Error!!! Exit", e);
		}

		return newExperiments;
	}
}