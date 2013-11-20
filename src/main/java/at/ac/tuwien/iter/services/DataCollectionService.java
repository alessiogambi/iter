package at.ac.tuwien.iter.services;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;

public interface DataCollectionService {

	/**
	 * 
	 * @param test
	 * @param testStartResponse
	 * @param testResult
	 */
	public void collectDataForExperiment(Test test, String testStartResponse,
			TestResult testResult);

}
