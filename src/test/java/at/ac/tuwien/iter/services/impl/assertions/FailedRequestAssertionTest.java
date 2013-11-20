package at.ac.tuwien.iter.services.impl.assertions;

import java.io.File;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.data.TestReport;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;

public class FailedRequestAssertionTest {

	@Test
	public void checkNoFailedRequest() {
		FailedRequestAssertion assertion = new FailedRequestAssertion(
				LoggerFactory.getLogger(FailedRequestAssertion.class),
				new DatabaseManagerService(
						LoggerFactory.getLogger(FailedRequestAssertion.class),
						new File("src/test/resources/xml/")));

		TestReport tr = assertion.check("src/test/resources/xml/1-clients");
		System.out.println("checkNoFailedRequest() : " + tr.getTestOutcome()
				+ " " + tr.getReason());
	}

	@Test
	public void checkNoMore10FailedRequest() {
		AbsoluteFailedRequestAssertion assertion = new AbsoluteFailedRequestAssertion(
				LoggerFactory.getLogger(FailedRequestAssertion.class),
				new DatabaseManagerService(
						LoggerFactory.getLogger(FailedRequestAssertion.class),
						new File("src/test/resources/xml/")),
				// Threshold
				10);

		TestReport tr = assertion.check("src/test/resources/xml/1-clients");
		System.out.println("checkNoMore10FailedRequest() : "
				+ tr.getTestOutcome() + " " + tr.getReason());
	}

	@Test
	public void checkNoMore1PercFailedRequest() {
		RelativeFailedRequestAssertion assertion = new RelativeFailedRequestAssertion(
				LoggerFactory.getLogger(FailedRequestAssertion.class),
				new DatabaseManagerService(
						LoggerFactory.getLogger(FailedRequestAssertion.class),
						new File("src/test/resources/xml/")),
				// Threshold
				1.0 / 100.0);

		TestReport tr = assertion.check("src/test/resources/xml/1-clients");
		System.out.println("checkNoMore1PercFailedRequest() : "
				+ tr.getTestOutcome() + " " + tr.getReason());
	}

	@Test
	public void checkNoMore10PercFailedRequest() {
		RelativeFailedRequestAssertion assertion = new RelativeFailedRequestAssertion(
				LoggerFactory.getLogger(FailedRequestAssertion.class),
				new DatabaseManagerService(
						LoggerFactory.getLogger(FailedRequestAssertion.class),
						new File("src/test/resources/xml/")),
				// Threshold
				10.0 / 100.0);

		TestReport tr = assertion.check("src/test/resources/xml/1-clients");
		System.out.println("checkNoMore10PercFailedRequest() : "
				+ tr.getTestOutcome() + " " + tr.getReason());
	}
}
