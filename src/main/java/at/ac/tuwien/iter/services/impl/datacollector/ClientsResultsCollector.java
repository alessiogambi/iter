package at.ac.tuwien.iter.services.impl.datacollector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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

public class ClientsResultsCollector implements DataCollectionService {

	private Logger logger;
	private DatabaseManagerService dbmanager;

	// TODO The results will be saved somewhere else...

	public ClientsResultsCollector(Logger logger,
			DatabaseManagerService dbmanager) {
		this.logger = logger;
		this.dbmanager = dbmanager;
	}

	void downloadFromUrl(URL url, String localFilename) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;

		try {
			URLConnection urlConn = url.openConnection();// connect

			is = urlConn.getInputStream(); // get connection inputstream
			fos = new FileOutputStream(localFilename); // open outputstream to
														// local file

			byte[] buffer = new byte[4096]; // declare 4KB buffer
			int len;

			// while we have availble data, continue downloading and storing to
			// local file
			while ((len = is.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
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

	private String extractUrlOfClientsResults(String startTestResponse) {
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

			expr = xpath.compile("//experiment/client/results/text()");
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
	protected String storeDataInFile(String dumpUrl, Test test)
			throws MalformedURLException, IOException {

		// TODO THIS SMELLS BAD !!
		String filename = dbmanager.getClientDBnameForTest(test);
		filename = filename.split(":")[filename.split(":").length - 1];
		logger.debug("storeDataInDB() " + filename);

		// This should be a reliable thing...
		downloadFromUrl(new URL(dumpUrl), filename);
		return filename;
	}

	public void collectDataForExperiment(Test test, String testStartResponse,
			TestResult testResult) {
		try {
			String urlForCollectingResults = extractUrlOfClientsResults(extractExperimentReportsUrl(testStartResponse));

			logger.info("Results available at: " + urlForCollectingResults);

			String fileName = storeDataInFile(urlForCollectingResults, test);

			testResult.setClientsDB(fileName);
		} catch (Exception e) {
			logger.error(
					"Got exception. Catch it to avoid breaking the chain !", e);
		}
	}
}
