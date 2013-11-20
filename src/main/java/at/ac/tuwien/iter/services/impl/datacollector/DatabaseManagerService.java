package at.ac.tuwien.iter.services.impl.datacollector;

import java.io.File;

import org.slf4j.Logger;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.data.TestResult;

public class DatabaseManagerService {

	private Logger logger;
	private File baseDir;

	public DatabaseManagerService(Logger logger, File baseDir) {
		this.logger = logger;
		this.baseDir = baseDir;

		if (!baseDir.exists()) {
			// Create the structure of dirs if needed
			this.logger.info("Create the base dir for storing test results"
					+ baseDir.getAbsolutePath());
			boolean mkdirs = baseDir.mkdirs();
			if (!mkdirs) {
				this.logger.error("The base dir was NOT created !");
				throw new RuntimeException("Cannot create dir structure: "
						+ baseDir.getAbsolutePath());
			}
		}

	}

	public String getControllerDBnameForTest(Test test) {
		return String.format("jdbc:hsqldb:file:%s/iter-%d-controller",
				baseDir.getAbsolutePath(), test.getId());
	}

	public String getServiceDBnameForTest(Test test) {
		return String.format("jdbc:hsqldb:file:%s/iter-%d-service",
				baseDir.getAbsolutePath(), test.getId());
	}

	public String getClientDBnameForTest(Test test) {
		return String.format("jdbc:hsqldb:file:%s/iter-%d-clients",
				baseDir.getAbsolutePath(), test.getId());
	}

	public String getControllerDBnameForTest(TestResult testResult) {
		return String.format("jdbc:hsqldb:file:%s/iter-%d-controller",
				baseDir.getAbsolutePath(), testResult.getTestId());
	}

	public String getClientDBnameForTest(TestResult test) {
		return String.format("jdbc:hsqldb:file:%s/iter-%d-clients",
				baseDir.getAbsolutePath(), test.getTestId());
	}

	public String getServiceDBnameForTest(TestResult testResult) {
		return String.format("jdbc:hsqldb:file:%s/iter-%d-service",
				baseDir.getAbsolutePath(), testResult.getTestId());
	}
}
