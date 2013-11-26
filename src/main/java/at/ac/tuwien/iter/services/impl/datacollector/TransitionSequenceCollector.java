package at.ac.tuwien.iter.services.impl.datacollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.gambi.tapestry5.cli.annotations.CLIOption;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.DataCollectionService;
import at.ac.tuwien.iter.utils.ServiceManifestUtils;

import com.google.common.base.Preconditions;
import com.google.inject.internal.Lists;

public class TransitionSequenceCollector implements DataCollectionService {

	private Logger logger;

	// FIXME: THIS IS A PATCH !!
	private String customerName;
	private String serviceName;

	public TransitionSequenceCollector(Logger logger,
			@CLIOption(longName = "customer-name") String customerName,
			@CLIOption(longName = "service-name") String serviceName) {
		this.logger = logger;
		this.customerName = customerName;
		this.serviceName = serviceName;
	}

	private String getURL(String url) {
		HttpClient client = new HttpClient();
		try {
			GetMethod getMethod = new GetMethod(url);
			int statusCode = client.executeMethod(getMethod);
			if (!(statusCode >= 200 && statusCode < 300)) {// !=
															// HttpStatus.SC_OK
				throw new RuntimeException("Method failed with code "
						+ statusCode + ": " + getMethod.getStatusLine()
						+ "\nURL: " + url);
			}
			return new String(getMethod.getResponseBody());
		} catch (HttpException e) {
			logger.error("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.error("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (Throwable e) {
			logger.error("Fatal generic error: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private String extractExperimentReportsUrl(String body) {
		String url = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;
			XPathExpression expr = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(body)));

			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

			expr = xpath.compile("//html/body/a/text()");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			url = nodes.item(0).getNodeValue();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url;
	}

	private String extractUrlOfControllerResults(String startTestResponse) {
		String url = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;
			XPathExpression expr = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(
					getURL(startTestResponse))));

			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

			// println("TOBEPARSED: " + startTestResponse);

			expr = xpath.compile("//experiment/controller/results/text()");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			url = nodes.item(0).getNodeValue();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url;
	}

	// TODO Centralize the data persistency layer somehow to easy the removal of
	// the direct dep on HSQLDB. Maybe it's enough to have a DBConnection
	// service/factory
	// Changed package level visibility for ease the testing
	protected double[] loadSeriesFromDBAndDropDB(String dumpUrl, Test test) {

		// TODO Use the Registry to inject the db connection, ...
		Connection conn = null;

		// With ;shutdown=true I think the in mem db is deleted after the last
		// connection is closed !
		String dbname = "jdbc:hsqldb:mem:testdb" + test.getId()
				+ ";shutdown=true";

		logger.debug("loadSeriesFromDBAndDropDB() " + dbname);

		try {
			// Is this needed ... I must check on the doc :) ?!
			Class.forName("org.hsqldb.jdbcDriver");

			conn = DriverManager.getConnection(dbname, "sa", "");

			BufferedReader reader = new BufferedReader(new StringReader(
					getURL(dumpUrl)));
			// TODO: all this method is just a quick and dirty patch. Mysql
			// should be completely removed because it is not portable

			// Loading mysql dump
			StringBuilder mysqlDump = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				mysqlDump.append(line);
				line = reader.readLine();
			}

			String[] commands = mysqlDump.toString().split(";");
			for (String command : commands) {
				if (command.contains(" SET ") || command.startsWith("--")
						|| command.startsWith("/*!")
						|| command.startsWith("UNLOCK")) {
					continue;
				}
				command = command.replaceAll("if not exists", "");
				command = command.replaceAll("IF NOT EXISTS", "");
				command = command.replaceAll("`", "");
				command = command.replaceAll(" TINYINT ", " boolean ");
				command = command.replaceAll(" tinyint ", " boolean ");

				command = command.replaceAll(" AUTO_INCREMENT",
						"GENERATED BY DEFAULT AS IDENTITY");
				command = command.replaceAll(" auto_increment",
						"GENERATED BY DEFAULT AS IDENTITY");
				command = command.replaceAll(" int ", " integer");
				command = command.replaceAll(" INT ", " integer");

				command = command
						.replaceAll(
								" NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
								"");
				command = command.replaceAll(" DEFAULT NULL", "");
				if (command.contains(" ENGINE=")) {
					command = command.substring(0, command.indexOf(" ENGINE="));
				}

				command = command + ';';

				// Executing command
				Statement statement = conn.createStatement();
				statement.executeUpdate(command);

			}

			// Finding the name of the table
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet rs = dbm.getTables(null, null, null, null);

			String tableName = null;
			String pattern = "(.+)_S(\\d+)";
			while (rs.next()) {
				String candidate = rs.getString(3);
				if (candidate.toLowerCase().startsWith("controller_activation")
						&& candidate.toLowerCase().contains(customerName)
						&& candidate.toLowerCase().contains(serviceName)) {
					tableName = candidate;
				}
			}

			if (tableName == null) {
				throw new RuntimeException("Cannot find the test result table");
			}

			logger.debug("Table name: " + dbname + "." + tableName);

			List<String> configurationDescriptors = ServiceManifestUtils
					.getColumnsFromManifest(test.getManifestURL());

			StringBuilder queryBuilder = new StringBuilder("SELECT ");
			for (String column : configurationDescriptors) {
				queryBuilder.append(column);
				queryBuilder.append(',');
			}
			queryBuilder.delete(queryBuilder.length() - 1,
					queryBuilder.length());

			queryBuilder.append(" FROM ");
			queryBuilder.append(tableName);
			queryBuilder.append(" ORDER BY time;");
			String query = queryBuilder.toString();

			// println("QUERY: " + query);

			Statement statement = conn.createStatement();
			ResultSet configurations = statement.executeQuery(query);

			ArrayList<Double> result = Lists.newArrayList();

			Preconditions.checkArgument(configurationDescriptors.size() == 1);
			while (configurations.next()) {
				result.add(configurations.getDouble(1));
			}

			double[] resultArray = new double[result.size()];
			int i = 0;
			for (Double state : result) {
				resultArray[i] = state;
				i = i + 1;
			}

			System.out
					.println("BasicRunner.loadSeriesFromDBAndDropDB() Query Result : "
							+ Arrays.toString(resultArray));
			return resultArray;

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getMessage().contains("Table not found: SYSTEM_TABLES")) {
				// This is really BAD!
				// See
				// http://sourceforge.net/tracker/index.php?func=detail&aid=2793732&group_id=23316&atid=378131

				// TODO Capture this inside a Typed Exception
				throw new RuntimeException(
						"FATAL: Database cannot be created or read !");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// dropTempDB(dbname);
		}
		return null;
	}

	// TODO Make it simple !
	protected void dropTempDB(String dbname) {
		Connection conn = null;
		try {
			// Is this needed ... I must check on the doc :) ?!
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection(dbname, "sa", "");
			// According to
			// http://stackoverflow.com/questions/4990410/how-can-i-wipe-data-from-my-hsqldb-after-every-test
			// This is the solution. TestCase DBCreateAndDestroy shows that !
			String query = "DROP SCHEMA PUBLIC CASCADE;";

			Statement statement = conn.createStatement();
			statement.execute(query);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void collectDataForExperiment(Test test, String testStartResponse,
			TestResult testResult) {
		try {
			String urlForCollectingResults = extractUrlOfControllerResults(extractExperimentReportsUrl(testStartResponse));

			logger.info("Results available at: " + urlForCollectingResults);

			double[] transitions = loadSeriesFromDBAndDropDB(
					urlForCollectingResults, test);

			logger.info("Collected the following transitions "
					+ Arrays.toString(transitions));

			testResult.setStates(transitions);
		} catch (Exception e) {
			logger.error("Got exception. Catch it to avoid breaking the chain !");
		}
	}
}
