package at.ac.tuwien.iter.services.impl.datacollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.DataCollectionService;

public class ControllerResultsCollector implements DataCollectionService {

	private Logger logger;
	private DatabaseManagerService dbmanager;

	// TODO The results will be saved somewhere else...

	public ControllerResultsCollector(Logger logger,
			DatabaseManagerService dbmanager) {
		this.logger = logger;
		this.dbmanager = dbmanager;
	}

	// Move to some UTIL or service package as this code replicated a lot of
	// times
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

			// ("TOBEPARSED: " + startTestResponse);

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

	// Changed package level visibility for ease the testing
	protected String storeDataInDB(String dumpUrl, Test test) {

		// TODO Use the Registry to inject the db connection, ...
		Connection conn = null;

		String dbname = dbmanager.getControllerDBnameForTest(test);

		logger.info("storeDataInDB() " + dbname);

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

			return dbname;

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
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
		}
		return null;
	}

	public void collectDataForExperiment(Test test, String testStartResponse,
			TestResult testResult) {
		try {
			String urlForCollectingResults = extractUrlOfControllerResults(extractExperimentReportsUrl(testStartResponse));

			logger.info("Results available at: " + urlForCollectingResults);

			String dbName = storeDataInDB(urlForCollectingResults, test);

			testResult.setControllerDB(dbName);
		} catch (Exception e) {
			logger.error("Got exception. Catch it to avoid breaking the chain !");
		}
	}
}
