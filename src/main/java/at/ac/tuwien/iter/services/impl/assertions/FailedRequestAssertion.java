package at.ac.tuwien.iter.services.impl.assertions;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.ac.tuwien.iter.data.TestReport;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.services.AssertionService;
import at.ac.tuwien.iter.services.impl.datacollector.DatabaseManagerService;

public class FailedRequestAssertion implements AssertionService {

	private Logger logger;
	private DatabaseManagerService dbService;
	private String assertionName;
	private int threshold;

	public FailedRequestAssertion(Logger logger,
			DatabaseManagerService dbService) {
		this.logger = logger;
		this.dbService = dbService;
		this.threshold = 0;
		this.assertionName = "no-failed-request";
	}

	private int countFailedRequests(File xmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;
			XPathExpression expr = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(xmlFile);

			// Count the requests
			// <testResults version="1.2">
			// <httpSample
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

			expr = xpath.compile("//httpSample[@s='false']");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			return nodes.getLength();

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new RuntimeException(
				"Cannot compute the number of failed requests ");
	}

	// Better testability
	protected TestReport check(String _xmlFile) {
		TestReport testReport = new TestReport();
		testReport.setTestedProperty(assertionName);
		try {
			File xmlFile = new File(_xmlFile);
			// Read the file into a string a pass it to the method

			int requestCounts = countFailedRequests(xmlFile);

			if (requestCounts <= threshold) {
				logger.info(assertionName + " PASSED");
				testReport.setTestOutcome("PASSED");
			} else {
				logger.info(assertionName + " FAILED");
				logger.info(assertionName + " Found " + requestCounts
						+ " failed requests. Max is " + threshold + "");
				testReport.setTestOutcome("FAILED");
				testReport.setReason(String.format(
						"%d requests failed. Max is %d", requestCounts,
						threshold));
			}
		} catch (Throwable ee) {
			// NEVER BLOCK THE CHAIN !!
			logger.warn("Exception while checking" + assertionName
					+ " : ERROR !", ee);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(ee.getMessage());
		}
		return testReport;
	}

	public void check(TestResult testResult) {
		try {
			// TODO There is something wrong while doing the bootstrap !
			String xmlFile = null;
			if (testResult.getClientsDB() != null) {
				xmlFile = testResult.getClientsDB();
			} else {
				xmlFile = dbService.getClientDBnameForTest(testResult);
			}

			testResult.addTestReport(check(xmlFile));

		} catch (Throwable e) {
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
