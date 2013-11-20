package at.ac.tuwien.iter.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;


import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class TestResult {
	private long testId;
	private Integer hashCode = null;

	private String customerName;
	private String serviceName;

	private String loadGeneratorID;

	private String traceURL;

	private long recordedAt;
	@XmlList
	private double[] states;
	@XmlList
	private double[] parameters;

	private String controllerDB;
	private String serviceDB;
	private String clientsDB;

	@XmlElementWrapper(name = "testReports")
	@XmlElement(name = "testReport")
	private List<TestReport> testReports;

	protected TestResult() {
		super();
		this.testReports = new ArrayList<TestReport>();
	}

	private TestResult(Test test, String customerName, String serviceName,
			long recordedAt) {
		super();

		// Why this ?
		// this.testId = test.getId() + test.hashCode() * 79;
		this.testId = test.getId();

		this.customerName = customerName;
		this.serviceName = serviceName;
		this.recordedAt = recordedAt;
		this.loadGeneratorID = test.getLoadGeneratorID();
		this.traceURL = test.getTraceURL();
		this.setParametersFromNumbers(test.getParameters());
		this.testReports = new ArrayList<TestReport>();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof TestResult) {
			TestResult that = (TestResult) arg0;
			// The Id should be enough as it is supposed to be unique...
			return (this.testId == that.testId
					&& this.customerName.equals(that.customerName) && this.serviceName
					.equals(that.serviceName));
		} else {
			return super.equals(arg0);
		}
	}

	@Override
	public int hashCode() {
		if (this.hashCode == null) {
			HashFunction hashFunction = Hashing.sha512();
			Hasher hasher = hashFunction.newHasher();
			hasher = hasher.putLong(testId).putString(customerName)
					.putString(serviceName);
			for (Number number : parameters) {
				hasher = hasher.putString(number.toString());
			}
			this.hashCode = hasher.hash().asInt();
		}
		return this.hashCode;
	}

	/*
	 * Number[] statesAsNumber = new Number[states.length]; for (int i = 0; i <
	 * states.length; i++) { statesAsNumber[i] = states[i]; }
	 */

	public static final synchronized TestResult newTestResult(Test test,
			String customerName, String serviceName) {
		return new TestResult(test, customerName, serviceName,
				System.currentTimeMillis());
	}

	public final long getTestId() {
		return testId;
	}

	public final String getCustomerName() {
		return customerName;
	}

	public final String getServiceName() {
		return serviceName;
	}

	public final long getRecordedAt() {
		return recordedAt;
	}

	public final String getLoadGeneratorID() {
		return loadGeneratorID;
	}

	public final void setLoadGeneratorID(String loadGeneratorID) {
		this.loadGeneratorID = loadGeneratorID;
	}

	public final String getTraceURL() {
		return traceURL;
	}

	// public void addTestReport(String property, String result) {
	// // Force the SET semantic
	// TestReport testReport = new TestReport(property, result);
	//
	// if (testReports.contains(testReport)) {
	// boolean res = testReports.remove(testReport);
	// // System.out
	// // .println("TestResult.addTestReport() test Reports containt already "
	// // + property + " it will be overridden (" + res + ")");
	// }
	// testReports.add(testReport);
	// }

	public void addTestReport(TestReport testReport) {
		// // Force the SET semantic
		// TestReport testReport = new TestReport(property, result);

		if (testReports.contains(testReport)) {
			testReports.remove(testReport);
		}
		testReports.add(testReport);
	}

	public final void setTraceURL(String traceURL) {
		this.traceURL = traceURL;
	}

	public final Number[] getStatesAsNumbers() {
		Number[] statesToReturn = new Number[this.states.length];
		for (int i = 0; i < this.states.length; i++) {
			statesToReturn[i] = this.states[i];
		}
		return statesToReturn;
	}

	public final Number[] getParametersAsNumbers() {
		if (this.parameters.length == 0) {
			return null;
		}
		Number[] parametersToReturn = new Number[this.parameters.length];
		for (int i = 0; i < this.parameters.length; i++) {
			parametersToReturn[i] = parameters[i];
		}
		return parametersToReturn;
	}

	protected final void setTestId(long testId) {
		this.testId = testId;
	}

	protected final void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	protected final void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	protected final void setRecordedAt(long recordedAt) {
		this.recordedAt = recordedAt;
	}

	public final double[] getStates() {
		return states;
	}

	public final void setStates(double[] states) {
		this.states = states;
	}

	public final double[] getParameters() {
		return parameters;
	}

	protected final void setTestReports(List<TestReport> testReports) {
		this.testReports = testReports;
	}

	public final List<TestReport> getTestReports() {
		return testReports;
	}

	public final TestReport getTestReport(String property) {
		// Force the SET semantic
		TestReport testReport = new TestReport(property, "");

		if (testReports.contains(testReport)) {
			return testReports.get(testReports.indexOf(testReport));
		}

		return null;
	}

	public final void setParameters(double[] parameters) {
		this.parameters = parameters;
	}

	protected final void setStatesFromNumbers(Number[] states) {
		this.states = new double[states.length];
		for (int i = 0; i < states.length; i++) {
			this.states[i] = new Double(states[i].toString());
		}
	}

	protected final void setParametersFromNumbers(Number[] parameters) {
		this.parameters = new double[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			this.parameters[i] = new Double(parameters[i].toString());
		}
	}

	public void setControllerDB(String dbName) {
		this.controllerDB = dbName;
	}

	public final String getControllerDB() {
		return controllerDB;
	}

	public void setServiceDB(String dbName) {
		this.serviceDB = dbName;
	}

	public final String getServiceDB() {
		return serviceDB;
	}

	public void setClientsDB(String fileName) {
		this.clientsDB = fileName;
	}

	public final String getClientsDB() {
		return clientsDB;
	}
}
