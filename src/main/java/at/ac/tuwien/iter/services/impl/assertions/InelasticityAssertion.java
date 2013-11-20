package at.ac.tuwien.iter.services.impl.assertions;

import java.util.Arrays;
import java.util.List;

import matlabcontrol.MatlabInvocationException;

import org.slf4j.Logger;

import at.ac.tuwien.iter.data.TestReport;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.MathEngineDao;

public class InelasticityAssertion implements AssertionService {

	private Logger logger;
	private MathEngineDao dao;
	private final String assertionName = "inelasticity";

	public InelasticityAssertion(Logger logger, MathEngineDao dao) {
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
				logger.info(assertionName + " No transitions: FAILED.");
				testReport.setTestOutcome("FAILED");
				testReport.setReason("No transitions");
				return testReport;
			}

			int maxReachedState = 0;
			int minReachedState = Integer.MAX_VALUE;

			for (double[] transition : transitions) {
				if (transition[1] > maxReachedState) {
					maxReachedState = (int) transition[1];
				}

				if (transition[1] < minReachedState && transition[1] > 0) {
					minReachedState = (int) transition[1];
				}
			}

			logger.debug(assertionName + " InelasticityAssertion.check():"
					+ "Max Reached = " + maxReachedState);

			logger.debug(assertionName + " InelasticityAssertion.check():"
					+ "Min Reached = " + minReachedState);

			if (maxReachedState == minReachedState) {
				logger.info(assertionName
						+ " InelasticityAssertion.check(): FAILED");
				testReport.setTestOutcome("FAILED");
				testReport.setReason(String
						.format("The system did not move away from %d",
								minReachedState));
			} else {
				logger.info(assertionName
						+ " InelasticityAssertion.check(): PASSED");
				testReport.setTestOutcome("PASSED");
			}
		} catch (Throwable e) {
			logger.warn(assertionName
					+ " Exception during Inelasticity CHECK: ERROR !", e);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());
		}

		return testReport;
	}

	public void check(TestResult testResult) {
		try {

			List<double[]> transitions = null;
			transitions = dao.inferModel(testResult.getStates());
			testResult.addTestReport(check(transitions));
		} catch (MatlabInvocationException e) {
			logger.info(assertionName + " InelasticityAssertion.check():"
					+ "Cannot Infer a Markov Model from "
					+ Arrays.toString(testResult.getStates()));
			logger.warn(assertionName
					+ " Exception during Inelasticity CHECK: ERROR !", e);
			TestReport testReport = new TestReport();
			testReport.setTestedProperty(assertionName);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());

			testResult.addTestReport(testReport);
		} catch (Throwable e) {
			logger.info(assertionName + " InelasticityAssertion.check():");
			logger.warn(assertionName
					+ " Exception during Inelasticity CHECK: ERROR !", e);
			TestReport testReport = new TestReport();
			testReport.setTestedProperty(assertionName);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());

			testResult.addTestReport(testReport);
		}

	}
}
