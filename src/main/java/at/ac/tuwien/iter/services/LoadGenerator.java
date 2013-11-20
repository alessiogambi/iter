package at.ac.tuwien.iter.services;

import java.util.List;

import at.ac.tuwien.iter.data.Test;

// Load generator interface. There will be a concrete implementation for any shape of parametric workload (e.g. SinusLoadGenerator)
public interface LoadGenerator {

	public int getNumberOfParameters();

	public Test generateTest(Number... pars);

	public Test generateRandomCase();

	public Test generatePseudoRandomTest(int distance, Number... pars);

	public List<Test> generateInitialTestSuite(int nTests);

	public double[] getLowerBounds();

	public double[] getUpperBounds();

	public void setJmeterTestFile(String jmeterTestFile);

	public void setServiceManifest(String serviceManifest);

	public String getServiceID();
}
