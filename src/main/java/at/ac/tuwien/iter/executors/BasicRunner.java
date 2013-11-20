package at.ac.tuwien.iter.executors;

import java.io.IOException;
import java.io.StringReader;

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
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;
import at.ac.tuwien.iter.exceptions.TestExecutionException;
import at.ac.tuwien.iter.exceptions.TestTimeoutException;
import at.ac.tuwien.iter.services.DataCollectionService;

/**
 * Basic workflow manager. It has the methods to execute a test, to load
 * execution trace from a mysql dump , to analyze results through octave.
 * 
 * Note that this implementation is specific to bind AUToCLES APIs
 * 
 * @author alessiogambi
 * 
 */
public class BasicRunner {

	// Refactoring and configuration:
	// Make this more robust for exceptions !
	// Store the experiment data somewhere / somehow -> bootstrap !

	private static final long POLLING_INTERVAL = 60 * 1000l;

	private final ConfigurationManager configurationManager;

	private final DataCollectionService dataCollector;
	private long timeoutMillis;

	private Logger logger;

	// TODO Use Registry to inject the configurationManager Object
	public BasicRunner(Logger logger,
			ConfigurationManager configurationManager, TypeCoercer typeCoercer,
			// MathEngineDao mathEnginedao,
			DataCollectionService dataCollector, long timeout) {

		this.logger = logger;
		this.configurationManager = configurationManager;
		this.dataCollector = dataCollector;
		this.timeoutMillis = timeout;

	}

	// NOTE When a timeout goes off there are two cases, the exeperiment is
	// still going, the experiment is in the tearing down phase.
	// In the latter case, forceUndeploy() will fail with an exception
	private void forceUndeploy(Test test, String startPage)
			throws TestExecutionException {

		logger.warn("BasicRunner.forceUndeploy()");
		logger.debug(startPage);
		int instanceId = -1;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;
			XPathExpression expr = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(startPage)));

			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

			expr = xpath.compile("//html/body/instance/text()");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			instanceId = Integer.parseInt(nodes.item(0).getNodeValue());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TestExecutionException(e);
		}

		String experimentURL = this.configurationManager.getUrlTester()
				+ instanceId + "/WaitEndOfTheRun/SystemInput/URI";

		logger.debug("RETRIEVE THE TARGT URL FROM EXPERIMENT URL: "
				+ experimentURL);

		String _endOfExperimentURL = getURL(experimentURL);

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;
			XPathExpression expr = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(
					_endOfExperimentURL)));
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();
			expr = xpath.compile("//HTML/BODY//text()");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			String endOfExperimentURL = nodes.item(0).getNodeValue();

			logger.debug("END EXPERIMENT URL IS: " + endOfExperimentURL);

			HttpClient client = new HttpClient();
			PostMethod formSubmission = new PostMethod(endOfExperimentURL);
			try {
				int statusCode = client.executeMethod(formSubmission);
				if (!(statusCode >= 200 && statusCode < 300)) {
					throw new RuntimeException("Method failed: "
							+ formSubmission.getStatusLine());
				}

			} catch (HttpException e) {
				logger.error("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("Fatal transport error: " + e.getMessage());
				e.printStackTrace();
			} finally {
				formSubmission.releaseConnection();
			}

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public TestResult executeTest(Test test) throws TestExecutionException {

		String testStartResponse = startTest(test);
		if (testStartResponse == null) {
			throw new TestExecutionException("Cannot start test " + test);
		}

		try {
			logger.info("BasicRunner.executeTest() with timeout "
					+ (timeoutMillis / (60 * 1000)) + " mins");
			waitForCompletion(test, testStartResponse);
		} catch (TestTimeoutException e) {
			e.printStackTrace();

			// This may fail simply because the experiment is alreay in the
			// undeployStage
			try {
				forceUndeploy(test, testStartResponse);
			} catch (Throwable ee) {
				logger.info("SILENT Exception. " + ee.getMessage());
			}

			throw new TestExecutionException(e);
		}

		TestResult testResult = TestResult.newTestResult(test,
				configurationManager.getCustomerName(),
				configurationManager.getServiceName());

		// Here we can INJECT the pipeline/chain of data collectors.
		// All the data are downloaded somewhere by each of the contributes
		// service
		// and assertions and other services will find them

		try {
			dataCollector.collectDataForExperiment(test, testStartResponse,
					testResult);
		} catch (RuntimeException e) {
			if (e.getMessage().contains(
					"FATAL: Database cannot be created or read !")) {
				// Rethrow this
				logger.error("BasicRunner.executeTest() FATAL !");
				throw e;
			} else {
				throw new TestExecutionException("While collecting data", e);
			}
		}
		return testResult;
	}

	private void waitForCompletion(Test test, String startPage)
			throws TestExecutionException {
		final long pollingStarted = System.currentTimeMillis();
		int instanceId = -1;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			Document doc = null;
			XPathExpression expr = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(startPage)));

			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();

			expr = xpath.compile("//html/body/instance/text()");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			instanceId = Integer.parseInt(nodes.item(0).getNodeValue());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TestExecutionException(e);
		}

		String stateUrl = this.configurationManager.getUrlTester() + instanceId
				+ "/0/System/STATE";
		logger.debug("STATE URL: " + stateUrl);

		long currentTime = System.currentTimeMillis();
		while ((currentTime - pollingStarted) <= timeoutMillis) {
			try {
				Thread.sleep(POLLING_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new TestExecutionException(e);
			}
			String currentStateAnswer = getURL(stateUrl);
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder;
				Document doc = null;
				XPathExpression expr = null;
				builder = factory.newDocumentBuilder();
				doc = builder.parse(new InputSource(new StringReader(
						currentStateAnswer)));

				XPathFactory xFactory = XPathFactory.newInstance();
				XPath xpath = xFactory.newXPath();

				expr = xpath.compile("//HTML/BODY//text()");
				Object result = expr.evaluate(doc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				int currentState = Integer.parseInt(nodes.item(0)
						.getNodeValue());

				if (currentState != 3) {
					logger.debug("STATE: " + currentState);
				}

				if (currentState == 4) {
					return;
				} else if (currentState == 8) {
					throw new TestExecutionException(
							"Test Failed with status 8. STOP THE TEST");
				} else if (currentState == 27) {
					throw new TestExecutionException(
							"Test Failed with status 27");
				}

				// Update the time
				currentTime = System.currentTimeMillis();

			} catch (Exception e) {
				e.printStackTrace();
				throw new TestExecutionException(e);
			}

		}
		throw new TestTimeoutException();
	}

	private String startTest(Test test) {
		HttpClient client = new HttpClient();
		// long currentTime = System.currentTimeMillis();
		PostMethod formSubmission = new PostMethod(
				configurationManager.getUrlTester());
		formSubmission.addParameter("Action", "run");
		formSubmission.addParameter("customerName",
				configurationManager.getCustomerName());

		// configurationManager.getServiceName() + "" + "" + (currentTime%1000)
		// formSubmission.addParameter("serviceName", "S" + (test.getId() %
		// 100));

		// TODO Use alsways the very same name otherwise we run out of security
		// groups!
		String serviceName = configurationManager.getServiceName();
		if (serviceName.length() > 3) {
			serviceName = serviceName.substring(0, 2);
		}
		formSubmission.addParameter("serviceName", serviceName);

		formSubmission.addParameter("traceURI", test.getTraceURL());
		formSubmission.addParameter("manifestURI", test.getManifestURL());

		formSubmission.addParameter("clientsURI", test.getClientsURL());
		try {
			int statusCode = client.executeMethod(formSubmission);
			if (!(statusCode >= 200 && statusCode < 300)) {
				throw new RuntimeException("Method failed: "
						+ formSubmission.getStatusLine());
			}
			byte[] responseBody = formSubmission.getResponseBody();

			return new String(responseBody);

		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			formSubmission.releaseConnection();
		}
		return null;
	}

	// Move to a service and inject it
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
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
