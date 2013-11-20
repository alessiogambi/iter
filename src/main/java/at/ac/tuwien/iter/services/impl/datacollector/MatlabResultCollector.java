package at.ac.tuwien.iter.services.impl.datacollector;

import org.slf4j.Logger;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.services.MathEngineDao;

/**
 * At the moment this is contributed to no one as the test evolution part is
 * carried out by the plasticity class that in turns contains the call to matlab
 * 
 * @author alessiogambi
 * 
 */
public class MatlabResultCollector implements DataCollectionService {

	private MathEngineDao mathEngineDao;
	private Logger logger;

	public MatlabResultCollector(Logger logger, MathEngineDao mathEngineDao) {
		this.logger = logger;
		this.mathEngineDao = mathEngineDao;
	}

	public void collectDataForExperiment(Test test, String testStartResponse,
			TestResult testResult) {
		try {
			mathEngineDao.addTestExecution(testResult);
		} catch (Exception e) {
			logger.error("Error while collecting data,", e);
		}
	}

}
