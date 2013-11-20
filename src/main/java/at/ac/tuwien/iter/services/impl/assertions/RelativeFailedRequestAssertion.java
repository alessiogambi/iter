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

public class RelativeFailedRequestAssertion implements AssertionService {

	private Logger logger;
	private DatabaseManagerService dbService;
	private String assertionName = "relative-failed-requests";
	private double threshold;

	public RelativeFailedRequestAssertion(Logger logger,
			DatabaseManagerService dbService, double threshold) {
		this.logger = logger;
		this.dbService = dbService;
		if (threshold > 1.0 || threshold < 0.0) {
			throw new IllegalArgumentException(
					"Relative threshold must be between 0.0 and 1.0!");
		}
		this.threshold = threshold;
		assertionName = assertionName + "-" + (threshold * 100.0) + "%";

	}

	private double getFailedOverTotalRatio(File xmlFile) {
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

			expr = xpath.compile("//httpSample");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			int total = nodes.getLength();

			expr = xpath.compile("//httpSample[@s='false']");
			result = expr.evaluate(doc, XPathConstants.NODESET);
			nodes = (NodeList) result;
			int failed = nodes.getLength();

			if (total != 0) {
				return ((double) failed / (double) total);
			} else {
				throw new RuntimeException(
						"Cannot compute ratio failed/total requests: No requests at all!");
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Cannot compute ratio failed/total requests");
	}

	// Better testability
	protected TestReport check(String _xmlFile) {
		TestReport testReport = new TestReport();
		testReport.setTestedProperty(assertionName);
		try {
			File xmlFile = new File(_xmlFile);
			// Read the file into a string a pass it to the method

			double ratio = getFailedOverTotalRatio(xmlFile);

			if (ratio < threshold) {
				logger.info(assertionName + " PASSED");
				testReport.setTestOutcome("PASSED");
			} else {
				logger.info(assertionName + " FAILED");
				testReport.setTestOutcome("FAILED");
				testReport.setReason(String.format(
						"%.2f%% of requests failed. Max is %.2f%%",
						(ratio * 100), (threshold * 100)));
			}
		} catch (Throwable e) {
			logger.warn("Exception while checking" + assertionName
					+ " : ERROR !", e);
			testReport.setTestOutcome("ERROR");
			testReport.setReason(e.getMessage());
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
