package at.ac.tuwien.iter.services.impl.assertions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import matlabcontrol.MatlabInvocationException;

import org.slf4j.Logger;

import at.ac.tuwien.iter.data.TestReport;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.MathEngineDao;

public class PlasticityAssertion implements AssertionService {

	private Logger logger;
	private MathEngineDao dao;
	private final String assertionName = "plasticity";

	public PlasticityAssertion(Logger logger, MathEngineDao dao) {
		this.logger = logger;
		this.dao = dao;
	}

	// Better testability
	protected TestReport check(List<double[]> transitions) {
		TestReport testReport = new TestReport();
		testReport.setTestedProperty(assertionName);
		try {

			if (transitions == null) {
				logger.info(assertionName + " Null transitions: SKIPPED.");
				testReport.setTestOutcome("SKIPPED");
				testReport.setReason("Null transitions");
				return testReport;
			}

			if (transitions.size() == 0) {
				logger.info(assertionName + " No transitions: SKIPPED.");
				testReport.setTestOutcome("SKIPPED");
				testReport.setReason("No transitions");
				return testReport;
			}

			Set<Integer> reachedStates = new HashSet<Integer>();

			for (double[] transition : transitions) {
				if (transition[1] > 1) {
					reachedStates.add((int) transition[1]);
				}
			}

			logger.debug(assertionName + " Reached States (over 1) = "
					+ reachedStates);

			// For all states s such that s > 1 and s <= maxReachedState
			// If s,s-1 not observed Then plastic

			for (Integer s : reachedStates) {

				logger.debug(assertionName
						+ "  Search scaling down transition: " + s + ","
						+ (s - 1));

				boolean plastic = true;
				for (double[] transition : transitions) {
					if ((int) transition[0] == s
							&& (int) transition[1] == (s - 1)) {

						logger.debug(assertionName + " Found transition " + s
								+ "," + (s - 1));
						plastic = false;
						break;
					}
				}

				if (plastic) {
					logger.info("Found Plasticity in the System: FAILED !");
					logger.info(String
							.format("Transition %d->%d was never observed ", s,
									(s - 1)));
					testReport.setTestOutcome("FAILED");
					testReport
							.setReason(String.format(
									"Transition %d,%d was never observed ", s,
									(s - 1)));
					return testReport;
				}
			}

			logger.info(assertionName + " check ok : PASSED");
			testReport.setTestOutcome("PASSED");
		} catch (Throwable ee) {
			logger.warn(assertionName + " Exception during check: ERROR !");
			testReport.setTestOutcome("ERROR");
			testReport.setReason(ee.getMessage());
		}

		return testReport;
	}

	public void check(TestResult testResult) {
		try {
			List<double[]> transitions = null;

			transitions = dao.inferModel(testResult.getStates());

			testResult.addTestReport(check(transitions));
		} catch (MatlabInvocationException e) {
			logger.info(assertionName + " Cannot Infer a Markov Model from "
					+ Arrays.toString(testResult.getStates()));

			TestReport testReport = new TestReport();
			testReport.setTestedProperty(assertionName);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());

			testResult.addTestReport(testReport);

			logger.warn(assertionName
					+ " Exception during Plasticity CHECK: ERROR !");
		} catch (Throwable e) {
			// e.printStackTrace();
			logger.info(assertionName + " Cannot Infer a Markov Model from "
					+ Arrays.toString(testResult.getStates()));

			TestReport testReport = new TestReport();
			testReport.setTestedProperty(assertionName);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());

			testResult.addTestReport(testReport);

			logger.warn(assertionName
					+ " Exception during Plasticity CHECK: ERROR !");
		}

	}
}
