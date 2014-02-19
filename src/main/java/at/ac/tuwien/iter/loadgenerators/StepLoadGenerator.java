package at.ac.tuwien.iter.loadgenerators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.slf4j.Logger;

import at.ac.tuwien.iter.data.Test;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.impl.loadgenerators.InputSampler;
import at.ac.tuwien.iter.utils.JMeterUtils;
import at.ac.tuwien.tracegenerator.data.SingleTraceSpecification;
import at.ac.tuwien.tracegenerator.data.TraceSpecification;

/**
 * Binding for the traceloadgenerator web service. This class implements a
 * step load
 * 
 * @author alessiogambi
 * 
 */
public class StepLoadGenerator implements LoadGenerator {
	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	private int duration;

	public static final String MIN_LB = "at.ac.tuwien.loadgenerator.step.min.lb";
	public static final String MIN_UB = "at.ac.tuwien.loadgenerator.step.min.ub";

	public static final String MAX_LB = "at.ac.tuwien.loadgenerator.step.max.lb";
	public static final String MAX_UB = "at.ac.tuwien.loadgenerator.step.max.ub";

	public static final String WHEN = "at.ac.tuwien.loadgenerator.step.when";

	private String jmeterTestFile;
	private String serviceManifest;

	private final List<String> clientList;
	private final String traceGeneratorWebServiceURL;
	private Random random;

	private double[] minBounds;
	private double[] maxBounds;
	private int whenInSecs;

	private int nBins;

	private InputSampler inputSampler;

	private Logger logger;

	public double[] getLowerBounds() {
		return new double[] { minBounds[0], maxBounds[0] };
	}

	public double[] getUpperBounds() {
		return new double[] { minBounds[1], maxBounds[1] };
	}

	private TypeCoercer coercer;

	public void setJmeterTestFile(String jmeterTestFile) {
		this.jmeterTestFile = jmeterTestFile;
	}

	public void setServiceManifest(String serviceManifest) {
		this.serviceManifest = serviceManifest;
	}

	private String serviceID;

	public StepLoadGenerator(
			Logger logger,
			// THIS IS BAD!
			String serviceID,//
			TypeCoercer coercer, String traceGeneratorWebService,
			String jmeterTestFile, String manifest, double minLB, double minUB,
			double maxLB, double maxUB, int whenInSecs, int nBins,
			InputSampler inputSampler, int duration) {

		this.logger = logger;
		this.jmeterTestFile = jmeterTestFile;
		this.serviceManifest = manifest;
		this.coercer = coercer;
		this.traceGeneratorWebServiceURL = traceGeneratorWebService;
		this.clientList = JMeterUtils.getClientIDsFromTestFile(jmeterTestFile);
		this.random = new Random(System.currentTimeMillis());

		this.minBounds = new double[] { minLB, minUB };
		this.maxBounds = new double[] { maxLB, maxUB };
		this.whenInSecs = whenInSecs;

		this.nBins = nBins;
		this.serviceID = serviceID;
		this.duration = duration;
		this.inputSampler = inputSampler;

		logger.debug("Input Bounds: " + Arrays.toString(getLowerBounds()) + " "
				+ Arrays.toString(getUpperBounds()));
	}

	public int getNumberOfParameters() {
		return 3;
	}

	// This generates an initial test suite according to the test suite
	// generator configured, which is an InputSampler
	public List<Test> generateInitialTestSuite(int testSuiteSize) {
		List<Test> initialTestSuite = new ArrayList<Test>();
		for (Number[] parameters : inputSampler.sample(testSuiteSize,
				getLowerBounds(), getUpperBounds())) {

			initialTestSuite.add(generateTest(parameters));

		}
		return initialTestSuite;
	}

	/*
	 * Pars must be compatible with the number of clients. No nPars >= clients *
	 * getParamaters()
	 */
	public Test generateTest(Number... pars) {
		// NOTE that the when parameter is fixed by default !
		logger.debug("generateTest() " + Arrays.toString(pars) + ", when "
				+ whenInSecs);
		List<SingleTraceSpecification> allClients = new ArrayList<SingleTraceSpecification>();
		int index = 0;
		for (String clientID : clientList) {
			if (index + getNumberOfParameters() <= pars.length) {

				// Force default values for additional 2 parameters
				double min = Math.floor(pars[0].doubleValue());
				double max = Math.floor(pars[1].doubleValue());

				Number[] _pars = new Number[1];
				_pars[0] = min;
				_pars[1] = max;
				_pars[2] = whenInSecs;

				SingleTraceSpecification client = new SingleTraceSpecification(
						clientID, "step", _pars);
				allClients.add(client);
				index = index + getNumberOfParameters();
			} else {
				logger.debug("generateTest() More clients than parameters, skip "
						+ clientID);
			}
		}

		TraceSpecification _traceSpec = new TraceSpecification(duration,
				allClients);

		// Automatically convert to the right string !
		String traceSpec = coercer.coerce(_traceSpec, String.class);

		String workload = traceGeneratorWebServiceURL + "?tracespec="
				+ traceSpec;

		logger.debug("generateTest() " + workload);

		// Factory: each test is unique
		return Test.newInstance(jmeterTestFile, serviceManifest, workload,
				serviceID, pars);
	}

	private double randomInRange(Random r, double min, double max, int digits) {

		// Take the random input
		double param = (min + random.nextDouble() * (max - min));

		// Find the corresponding bin
		double binSize = (min - max) / (double) nBins;

		int par = (int) Math.floor((param - min) / binSize);

		// Return the correspondind values

		param = min + binSize * (par + 0.5);

		// Finally, return with required digits
		return Math.floor(param * Math.pow(10, digits)) / Math.pow(10, digits);
	}

	public Test generateRandomCase() {
		// Random test cases make sense only at 0 digits for the number of clients !
		return generateTest(
				// min client
				randomInRange(random, 
				minBounds[0],
				minBounds[1], 
				0),
				// max client
				randomInRange(random, 
				maxBounds[0],
				maxBounds[1], 
				0)
				//
		);
	}

	public Test generatePseudoRandomTest(int distance, Number... params) {

		Number[] result = new Number[params.length];
		for (int i = 0; i < params.length; i++) {
			// Get a random BIN between minDist and nBins-minDist excluded
			int ran = distance + 1 + random.nextInt(nBins - 1 - (distance + 1));
			// Get the bin corresponding to the input
			double binSize = (getUpperBounds()[i] - getLowerBounds()[i])
					/ (double) nBins;
			int par = (int) Math
					.floor((params[i].doubleValue() - getLowerBounds()[i])
							/ binSize);
			// Make sure to be far away from this bin but withing the boundaries
			// defined by nBins
			int disc_x_max_ei = (par + ran) % nBins;

			logger.info("generatePseudoRandomTest " + par + " --> "
					+ disc_x_max_ei);

			// Recovert the value back to real numbers
			result[i] = getLowerBounds()[i] + binSize * (disc_x_max_ei + 0.5);
		}

		return generateTest(result);
	}

}
