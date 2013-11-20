package at.ac.tuwien.iter.services.impl.assertions;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;

import at.ac.tuwien.iter.data.TestReport;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;

public class DoodleWebServiceAvgResponseTimeAssertion implements
		AssertionService {

	private Logger logger;
	private DatabaseManagerService dbService;
	private final String assertionName = "avg-response-time";

	private double[] thresholds;

	/**
	 * 
	 * TODO: This is just an example, very basic, not clear code ;)
	 * 
	 * 
	 * 
	 * 
	 * CREATE TABLE `monitoring_dsg_customers_ale_services_ale` ( `time`
	 * timestamp, `date` datetime `kpi_CREATE_POLL_AvgRT` double DEFAULT NULL,
	 * `kpi_CREATE_POLL_AvgTX` double DEFAULT NULL, `kpi_CREATE_POLL_QL` double
	 * DEFAULT NULL, `kpi_CREATE_POLL_RC` double DEFAULT NULL,
	 * `kpi_CREATE_POLL_RD` double DEFAULT NULL, `kpi_DELETE_POLL_AvgRT` double
	 * DEFAULT NULL, `kpi_DELETE_POLL_AvgTX` double DEFAULT NULL,
	 * `kpi_DELETE_POLL_QL` double DEFAULT NULL, `kpi_DELETE_POLL_RC` double
	 * DEFAULT NULL, `kpi_DELETE_POLL_RD` double DEFAULT NULL,
	 * `kpi_GET_POLLS_AvgRT` double DEFAULT NULL, `kpi_GET_POLLS_AvgTX` double
	 * DEFAULT NULL, `kpi_GET_POLLS_QL` double DEFAULT NULL, `kpi_GET_POLLS_RC`
	 * double DEFAULT NULL, `kpi_GET_POLLS_RD` double DEFAULT NULL,
	 * `kpi_GET_POLL_AvgRT` double DEFAULT NULL, `kpi_GET_POLL_AvgTX` double
	 * DEFAULT NULL, `kpi_GET_POLL_QL` double DEFAULT NULL, `kpi_GET_POLL_RC`
	 * double DEFAULT NULL, `kpi_GET_POLL_RD` double DEFAULT NULL,
	 * `kpi_VOTE_AvgRT` double DEFAULT NULL, `kpi_VOTE_AvgTX` double DEFAULT
	 * NULL, `kpi_VOTE_QL` double DEFAULT NULL, `kpi_VOTE_RC` double DEFAULT
	 * NULL, `kpi_VOTE_RD` double DEFAULT NULL)
	 * 
	 * @param logger
	 * @param dbService
	 * @param threshold
	 * 
	 *            TODO This can be made parametric, just add a conf file with
	 *            inside something like a message.catalog with keys:
	 *            KPI_NAME_LIST and MAX_VALUE or even CONDITION on KPI
	 */
	public DoodleWebServiceAvgResponseTimeAssertion(Logger logger,
			DatabaseManagerService dbService, double... thresholds) {
		this.logger = logger;
		this.dbService = dbService;
		this.thresholds = thresholds;
	}

	private String getTableName(Connection conn) throws SQLException {
		// Finding the name of the table
		DatabaseMetaData dbm = conn.getMetaData();
		ResultSet rs = dbm.getTables(null, null, null, null);

		String tableName = null;
		while (rs.next()) {
			String candidate = rs.getString(3);
			if (candidate.toLowerCase().startsWith("monitoring_")) {
				tableName = candidate;
				break;
			}
		}

		if (tableName == null) {
			throw new RuntimeException("Cannot find the name of the table !");
		}

		return tableName;
	}

	// Better testability
	protected TestReport check(String dbname) {

		TestReport testReport = new TestReport();
		testReport.setTestedProperty(assertionName);

		Connection conn = null;
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection(dbname, "sa", "");

			// Get table name
			String tableName = getTableName(conn);

			// Select the values that are bigger than the thresholds
			StringBuffer query = new StringBuffer();
			// query.append("SELECT COUNT(*)");
			query.append("SELECT time,  kpi_CREATE_POLL_AvgRT, kpi_DELETE_POLL_AvgRT, kpi_GET_POLLS_AvgRT, kpi_GET_POLL_AvgRT, kpi_VOTE_AvgRT ");
			query.append("FROM ").append(tableName).append(" ");
			query.append("WHERE ");
			query.append("kpi_CREATE_POLL_AvgRT > ").append(thresholds[0])
					.append(" OR ").append("kpi_DELETE_POLL_AvgRT > ")
					.append(thresholds[1]).append(" OR ")
					.append("kpi_GET_POLLS_AvgRT > ").append(thresholds[2])
					.append(" OR ").append("kpi_GET_POLL_AvgRT > ")
					.append(thresholds[3]).append(" OR ")
					.append("kpi_VOTE_AvgRT > ").append(thresholds[4]);

			// Execute the Query
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query.toString());

			int n = 0;
			while (rs.next()) {
				n = n + 1;

				logger.debug("DoodleWebServiceAvgResponseTimeAssertion.check() Failing entry: ");
				logger.debug("\t" + rs.getString("time"));

				if (rs.getDouble("kpi_CREATE_POLL_AvgRT") > thresholds[0]) {
					logger.debug(String.format("%s was (%f>%f)",
							"kpi_CREATE_POLL_AvgRT",
							rs.getDouble("kpi_CREATE_POLL_AvgRT"),
							thresholds[0]));
				}

				if (rs.getDouble("kpi_DELETE_POLL_AvgRT") > thresholds[1]) {

					logger.debug("DoodleWebServiceAvgResponseTimeAssertion.check()");
					logger.debug(String.format("\t%s was (%f>%f)",
							"kpi_DELETE_POLL_AvgRT",
							rs.getDouble("kpi_DELETE_POLL_AvgRT"),
							thresholds[1]));
				}
				if (rs.getDouble("kpi_GET_POLLS_AvgRT") > thresholds[2]) {

					logger.debug("DoodleWebServiceAvgResponseTimeAssertion.check()");

					logger.debug(String.format("\t%s was (%f>%f)",
							"kpi_GET_POLLS_AvgRT",
							rs.getDouble("kpi_GET_POLLS_AvgRT"), thresholds[2]));
				}
				if (rs.getDouble("kpi_GET_POLL_AvgRT") > thresholds[3]) {

					logger.debug("DoodleWebServiceAvgResponseTimeAssertion.check()");
					logger.debug(String.format("\t%s was (%f>%f)",
							"kpi_GET_POLL_AvgRT",
							rs.getDouble("kpi_GET_POLL_AvgRT"), thresholds[3]));
				}

				if (rs.getDouble("kpi_VOTE_AvgRT") > thresholds[4]) {

					logger.debug("DoodleWebServiceAvgResponseTimeAssertion.check()");
					logger.debug(String.format("\t%s was (%f>%f)",
							"kpi_VOTE_AvgRT", rs.getDouble("kpi_VOTE_AvgRT"),
							thresholds[4]));
				}

			}

			logger.info(assertionName + " Elements over the threshold :" + n);

			if (n == 0) {
				logger.info(assertionName + " PASSED");
				testReport.setTestOutcome("PASSED");
			} else {
				logger.info(assertionName + " FAILED");
				testReport.setTestOutcome("FAILED");
				testReport
						.setReason(String
								.format("The number of failed requests %d is greater than 0",
										n));
			}
		} catch (ClassNotFoundException e) {
			logger.warn(assertionName + " ERROR");
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());
		} catch (SQLException e) {
			logger.warn(assertionName + " ERROR");
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());
		} catch (Throwable e) {
			logger.warn(assertionName + " ERROR");
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return testReport;
	}

	public void check(TestResult testResult) {
		try {
			String serviceMonitoringDB = null;

			if (testResult.getServiceDB() != null) {
				serviceMonitoringDB = testResult.getServiceDB();
			} else {
				serviceMonitoringDB = dbService
						.getServiceDBnameForTest(testResult);
			}

			testResult.addTestReport(check(serviceMonitoringDB));

		} catch (Exception e) {
			logger.error(
					"Got exception. Catch it to avoid breaking the chain !", e);
			TestReport testReport = new TestReport();
			testReport.setTestedProperty(assertionName);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());

			testResult.addTestReport(testReport);
		}

	}
}
