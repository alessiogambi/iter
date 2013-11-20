package at.ac.tuwien.iter.services.impl.evo;

import java.util.ArrayList;
import java.util.Collection;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.TestSuiteEvolver;

/**
 * Return an empty collection of new tests to be run.
 * 
 * @author alessiogambi
 * 
 */
public class StopTestSuiteEvolution implements TestSuiteEvolver {

	public Collection<Test> evolveTestSuite(Collection<Test> testSuite,
			Collection<TestResult> testResults) {
		return new ArrayList<Test>();
	}
}
