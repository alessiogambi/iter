package at.ac.tuwien.iter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class JMeterUtils {

	private static String getURL(String url) {
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

	private static List<String> extractClientIDs(String body) {
		List<String> result = new ArrayList<String>();
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
			expr = xpath
					.compile("//kg.apc.jmeter.threads.UltimateThreadGroup/@testname");
			Object _result = expr.evaluate(doc, XPathConstants.NODESET);
			NodeList nodes = (NodeList) _result;
			for (int n = 0; n < nodes.getLength(); n++) {
				result.add(nodes.item(n).getNodeValue());
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
		return result;
	}

	public static List<String> getClientIDsFromTestFile(String jmxFileURL) {

		// Get the file and extract the ThreadGroups Content from it !
		List<String> result = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new StringReader(
				getURL(jmxFileURL)));
		// Loading JMX FILE
		StringBuilder jmxFile = new StringBuilder();
		String line;
		try {
			line = reader.readLine();
			while (line != null) {
				jmxFile.append(line);
				line = reader.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			result.addAll(extractClientIDs(jmxFile.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}
}
