package at.ac.tuwien.iter.services.impl.evo;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.iter.services.TestSuiteEvolver;
import at.ac.tuwien.iter.services.TestSuiteEvolverSource;

public class TestSuiteEvolverSourceImpl implements TestSuiteEvolverSource {

	private Map<String, TestSuiteEvolver> contributions;

	public TestSuiteEvolverSourceImpl(
			Map<String, TestSuiteEvolver> contributions) {
		this.contributions = new HashMap<String, TestSuiteEvolver>();
		this.contributions.putAll(contributions);
	}

	public TestSuiteEvolver getTestSuiteEvolver(String id) {
		if (contributions.containsKey(id)) {
			return contributions.get(id);
		}
		throw new RuntimeException("The TestSuiteEvolver " + id
				+ " cannot be found !");
	}

}
