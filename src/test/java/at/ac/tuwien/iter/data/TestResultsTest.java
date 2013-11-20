package at.ac.tuwien.iter.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestResultsTest {

	private Collection<TestResult> testResults;
	private Logger logger;
	private File bootstrapFile;

	@Before
	public void init() {
		bootstrapFile = new File("src/test/resources/bootstrap/input-file.xml");
		logger = LoggerFactory.getLogger(TestResultsTest.class);
		// Try Load all the cached executions if the file exists
		try {

			testResults = TestResultsCollector.loadFromFile(
					bootstrapFile.getAbsolutePath()).getTestResults();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@org.junit.Test
	public void compareTestResults() {

		Collection<TestResult> testResults = new ArrayList<TestResult>();
		try {
			testResults = TestResultsCollector.loadFromFile(
					bootstrapFile.getAbsolutePath()).getTestResults();
		} catch (Throwable e) {
		}

		int newTests = 0;
		// Store only the new testResults
		for (TestResult testResult : testResults) {
			if (this.testResults.contains(testResult)) {
				logger.debug("TestResult  " + testResult.getTestId()
						+ " is not new");
			} else {
				try {
					logger.debug("Storing " + testResult.getTestId());
					newTests++;
				} catch (Throwable e) {
					logger.warn("Cannot add " + testResult.getTestId()
							+ " to Matlab", e);
				}
			}
		}

		Assert.assertTrue(newTests == 0);
	}

}
