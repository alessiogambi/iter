package at.ac.tuwien.iter.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestReport {

	private String testedProperty;
	private String testOutcome;
	private String reason;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TestReport) {

			if (this.testedProperty == null) {

				if (((TestReport) obj).testedProperty == null) {
					return false;
				} else {
					return ((TestReport) obj).testedProperty
							.equals(testedProperty);
				}

			} else {
				return testedProperty.equals(((TestReport) obj).testedProperty);
			}
		}
		return super.equals(obj);
	}

	public TestReport() {
		super();
	}

	public TestReport(String property, String result) {
		super();
		testedProperty = property;
		testOutcome = result;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String description) {
		this.reason = description;
	}

	public String getTestedProperty() {
		return testedProperty;
	}

	public boolean isFailed() {
		return "FAILED".equalsIgnoreCase(getTestOutcome());
	}

	public String getTestOutcome() {
		return testOutcome;
	}

	public void setTestedProperty(String testedProperty) {
		this.testedProperty = testedProperty;
	}

	public void setTestOutcome(String testOutcome) {
		this.testOutcome = testOutcome;
	}
}
