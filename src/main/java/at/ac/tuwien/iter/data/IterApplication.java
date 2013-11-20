package at.ac.tuwien.iter.data;

import java.net.URL;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gambi.tapestry5.cli.annotations.ValidURL;
import org.hibernate.validator.constraints.NotEmpty;

import at.ac.tuwien.iter.annotations.ValidLoadGenerator;

/**
 * This is the bean that represents the application configuration. It
 * centralizes all the command line validation of the application.
 * 
 * @author alessio
 * 
 */
public class IterApplication {

	@NotNull(message = "Customer Name Cannot be null")
	@Size(min = 1, max = 3, message = "Customer Name Validation Error")
	private String customerName;

	@NotNull(message = "Service Name Cannot be null")
	@Size(min = 1, max = 3, message = "Service Name Validation Error")
	private String serviceName;

	@NotNull(message = "Load Generator Name Cannot be null")
	@NotEmpty(message = "Load Generator Name Cannot be empty")
	@ValidLoadGenerator
	private String loadGeneratorName;

	@NotNull(message = "Service Manifest URL cannot be null")
	@ValidURL(message = "Service Manifest URL is not valid")
	private URL serviceManifestURL;

	@NotNull(message = "Jmeter Client URL cannot be null")
	@ValidURL(message = "JMX file URL is not valid")
	private URL jmeterClientsURL;

	@Min(value = 1, message = "Parallel Tests must be greater than 0")
	@Max(value = 5, message = "Parallel Tests must be smaller than 6")
	private Integer nParallelTests;

	@Min(value = 0, message = "Size of initial test must be equals to or greater than 0")
	private Integer nInitialRandomTests;

	@Min(value = 1, message = "Top expected improvement size must be greater than 0")
	private Integer nBestTests;

	private boolean bootstrap = false;

	public IterApplication() {
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getLoadGeneratorName() {
		return loadGeneratorName;
	}

	public void setLoadGeneratorName(String loadGeneratorName) {
		this.loadGeneratorName = loadGeneratorName;
	}

	public URL getServiceManifestURL() {
		return serviceManifestURL;
	}

	public void setServiceManifestURL(URL serviceManifestURL) {
		this.serviceManifestURL = serviceManifestURL;
	}

	public URL getJmeterClientsURL() {
		return jmeterClientsURL;
	}

	public void setJmeterClientsURL(URL jmeterClientsURL) {
		this.jmeterClientsURL = jmeterClientsURL;
	}

	public Integer getnParallelTests() {
		return nParallelTests;
	}

	public void setnParallelTests(Integer nParallelTests) {
		this.nParallelTests = nParallelTests;
	}

	public Integer getnInitialRandomTests() {
		return nInitialRandomTests;
	}

	public void setnInitialRandomTests(Integer nInitialRandomTests) {
		this.nInitialRandomTests = nInitialRandomTests;
	}

	public Integer getnBestTests() {
		return nBestTests;
	}

	public void setnBestTests(Integer nBestTests) {
		this.nBestTests = nBestTests;
	}

	public boolean isBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(boolean bootstrap) {
		this.bootstrap = bootstrap;
	}

}
