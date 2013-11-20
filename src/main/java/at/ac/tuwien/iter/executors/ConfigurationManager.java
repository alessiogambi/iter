package at.ac.tuwien.iter.executors;

import at.ac.tuwien.iter.services.LoadGenerator;

/**
 * TODO: This class is deprecated and will be removed in the next release
 * 
 * @author alessiogambi
 * 
 */
@Deprecated
public class ConfigurationManager {
	private final String urlTester;
	private final String customerName;
	private final String serviceName;
	private final LoadGenerator loadGenerator;

	public ConfigurationManager(String urlTester, String customerName,
			String serviceName, LoadGenerator loadGenerator) {
		super();
		this.urlTester = urlTester;
		this.customerName = customerName;
		this.serviceName = serviceName;
		this.loadGenerator = loadGenerator;
	}

	public final String getUrlTester() {
		return urlTester;
	}

	public final String getCustomerName() {
		return customerName;
	}

	public final String getServiceName() {
		return serviceName;
	}

	public final LoadGenerator getLoadGenerator() {
		return loadGenerator;
	}

}
