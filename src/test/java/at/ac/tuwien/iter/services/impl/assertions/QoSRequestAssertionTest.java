package at.ac.tuwien.iter.services.impl.assertions;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.data.TestReport;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;

public class QoSRequestAssertionTest {

	@Test
	public void checkAvgRT() {

		// Columns/KPI that already contains the AvgRT as collected by the
		// service itself. Can be they NULL ? What to do in that case ?
		//
		// kpi_CREATE_POLL_AvgRT,kpi_DELETE_POLL_AvgRT,kpi_GET_POLLS_AvgRT,kpi_GET_POLL_AvgRT,
		// kpi_VOTE_AvgRT
		double[] avgRtMaxSlaMillis = new double[] { 2000.0, 1500.0, 2000.0,
				500.0, 1000.0 };

		DoodleWebServiceAvgResponseTimeAssertion assertion = new DoodleWebServiceAvgResponseTimeAssertion(
				LoggerFactory.getLogger(FailedRequestAssertion.class),
				new DatabaseManagerService(
						LoggerFactory
								.getLogger(DoodleWebServiceAvgResponseTimeAssertion.class),
						new File("src/test/resources/db")),
				// Add the Max RT
				avgRtMaxSlaMillis);

		String dbName = "jdbc:hsqldb:file:/Users/alessiogambi/Documents/TUWien/OngoingWorkNotDropBox/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/test/resources/db/datacollection1-service";
		TestReport result = assertion.check(dbName);
		System.out.println("checkAvgRT() : " + result.getTestOutcome());

		Assert.assertEquals("FAILED", result.getTestOutcome());
	}

	@Test
	public void checkAvgRT2() {

		// Columns/KPI that already contains the AvgRT as collected by the
		// service itself. Can be they NULL ? What to do in that case ?
		//
		// kpi_CREATE_POLL_AvgRT,kpi_DELETE_POLL_AvgRT,kpi_GET_POLLS_AvgRT,kpi_GET_POLL_AvgRT,
		// kpi_VOTE_AvgRT
		double[] avgRtMaxSlaMillis = new double[] { 2000.0, 1500.0, 2000.0,
				500.0, 1000.0 };

		DoodleWebServiceAvgResponseTimeAssertion assertion = new DoodleWebServiceAvgResponseTimeAssertion(
				LoggerFactory.getLogger(FailedRequestAssertion.class),
				new DatabaseManagerService(
						LoggerFactory
								.getLogger(DoodleWebServiceAvgResponseTimeAssertion.class),
						new File("src/test/resources/db")),
				// Add the Max RT
				avgRtMaxSlaMillis);

		String dbName = "jdbc:hsqldb:file:/Users/alessiogambi/Documents/TUWien/OngoingWorkNotDropBox/elasticTest/ICSE-2014-with-Antonio/code/workspace/iter/src/test/resources/db/datacollection11-service";
		TestReport result = assertion.check(dbName);
		System.out.println("checkAvgRT() : " + result.getTestOutcome());

		Assert.assertEquals("FAILED", result.getTestOutcome());
	}
}
