package at.ac.tuwien.iter.loadgenerators;

import java.util.ArrayList;
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
 * Binding for the traceloadgenerator web service. This class implements a sine
 * wave load
 * 
 * @author alessiogambi
 * 
 */
public class SinusLoadGenerator implements LoadGenerator {

	public static final String AMPLITUDE_LB = "at.ac.tuwien.loadgenerator.sinus.amplitude.lb";
	public static final String AMPLITUDE_UB = "at.ac.tuwien.loadgenerator.sinus.amplitude.ub";
	public static final String FREQUENCY_LB = "at.ac.tuwien.loadgenerator.sinus.frequency.lb";
	public static final String FREQUENCY_UB = "at.ac.tuwien.loadgenerator.sinus.frequency.ub";

	private String jmeterTestFile;
	private String serviceManifest;

	private final List<String> clientList;
	private final String traceGeneratorWebServiceURL;
	private Random random;

	private double[] amplitudeBounds;
	private double[] frequencyBounds;
	private int nBins;

	private InputSampler inputSampler;

	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public double[] getLowerBounds() {
		return new double[] { amplitudeBounds[0], frequencyBounds[0] };
	}

	public double[] getUpperBounds() {
		return new double[] { amplitudeBounds[1], frequencyBounds[1] };
	}

	private TypeCoercer coercer;

	public void setJmeterTestFile(String jmeterTestFile) {
		this.jmeterTestFile = jmeterTestFile;
	}

	public void setServiceManifest(String serviceManifest) {
		this.serviceManifest = serviceManifest;
	}

	private Logger logger;
	private String serviceID;

	private int duration;

	public SinusLoadGenerator(
			Logger logger,
			// THIS IS BAD!
			String serviceID,//
			TypeCoercer coercer, String traceGeneratorWebService,
			String jmeterTestFile, String manifest, double amplitudeLB,
			double amplitudeUB, double frequencyLB, double frequencyUB,
			int nBins, InputSampler inputSampler, int duration) {
		super();
		this.serviceID = serviceID;
		this.duration = duration;
		this.logger = logger;
		this.jmeterTestFile = jmeterTestFile;
		this.serviceManifest = manifest;
		this.coercer = coercer;
		this.traceGeneratorWebServiceURL = traceGeneratorWebService;
		this.clientList = JMeterUtils.getClientIDsFromTestFile(jmeterTestFile);
		this.random = new Random(System.currentTimeMillis());
		this.amplitudeBounds = new double[] { amplitudeLB, amplitudeUB };
		this.frequencyBounds = new double[] { frequencyLB, frequencyUB };
		this.inputSampler = inputSampler;
		this.nBins = nBins;
	}

	public int getNumberOfParameters() {
		return 2;
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
		List<SingleTraceSpecification> allClients = new ArrayList<SingleTraceSpecification>();
		int index = 0;
		for (String clientID : clientList) {

			if (index + getNumberOfParameters() <= pars.length) {

				// TODO Maybe there is a better way but I cannot find one.
				// FORCE Extra constraints on inputs, like digits and dependent
				// inputs

				// Force default values for additional 2 parameters
				double amplitude = Math.floor(pars[0].doubleValue()
						* Math.pow(10, 0))
						/ Math.pow(10, 0);
				double frequency = Math.floor(pars[1].doubleValue()
						* Math.pow(10, 3))
						/ Math.pow(10, 3);
				double verticalShift = amplitude;
				double phaseShift = 0.0;

				Number[] _pars = new Number[4];
				_pars[0] = amplitude;
				_pars[1] = frequency;
				_pars[2] = verticalShift;
				_pars[3] = phaseShift;

				SingleTraceSpecification client = new SingleTraceSpecification(
						clientID, "sine", _pars);
				allClients.add(client);
				index = index + getNumberOfParameters();
			} else {
				logger.info("SinusLoadGenerator.generateTest() More clients than parameters, skip "
						+ clientID);
			}
		}

		TraceSpecification _traceSpec = new TraceSpecification(
				duration, allClients);

		// Automatically convert to the right string !
		String traceSpec = coercer.coerce(_traceSpec, String.class);

		String workload = traceGeneratorWebServiceURL + "?tracespec="
				+ traceSpec;

		// Factory: each test is unique
		return Test.newInstance(jmeterTestFile, serviceManifest, workload,
				this.serviceID, pars);
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
		return generateTest(
		// Generate whole numbers for amplitude !
				randomInRange(random, amplitudeBounds[0], amplitudeBounds[1], 0),
				// Consider up to 3 digit after . for frequency !
				randomInRange(random, frequencyBounds[0], frequencyBounds[1], 3));

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

			logger.debug("IterImpl.randomRange() " + par + " --> "
					+ disc_x_max_ei);

			// Recovert the value back to real numbers

			result[i] = getLowerBounds()[i] + binSize * (disc_x_max_ei + 0.5);
		}

		return generateTest(result);
	}

}
