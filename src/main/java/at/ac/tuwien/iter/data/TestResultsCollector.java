package at.ac.tuwien.iter.data;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestResultsCollector implements Iterable<TestResult> {
	@XmlElement(name = "testResult")
	List<TestResult> tests;

	public TestResultsCollector() {
		super();
		tests = Lists.newLinkedList();
	}

	public void addTestResult(TestResult testResult) {
		this.tests.add(testResult);
	}

	public Iterator<TestResult> iterator() {
		return tests.iterator();
	}

	// This is meant only for reading!
	public Collection<TestResult> getTestResults() {
		return Collections.unmodifiableList(tests);
	}

	public static final TestResultsCollector loadFromFile(String file)
			throws JAXBException, IOException {
		JAXBContext contextObj = JAXBContext
				.newInstance(TestResultsCollector.class);
		Unmarshaller unMarshallerObj = contextObj.createUnmarshaller();
		TestResultsCollector testResultsCollector = (TestResultsCollector) unMarshallerObj
				.unmarshal(new BufferedReader(new FileReader(file)));
		return testResultsCollector;
	}

	public static final void saveToFile(String file,
			TestResultsCollector testResultsCollector) throws JAXBException,
			IOException {
		JAXBContext contextObj = JAXBContext
				.newInstance(TestResultsCollector.class);
		Marshaller marshallerObj = contextObj.createMarshaller();
		marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshallerObj.marshal(testResultsCollector, new PrintWriter(
				new FileOutputStream(file)));
	}

}
