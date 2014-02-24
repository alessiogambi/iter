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
 * Binding for the traceloadgenerator web service. This class implements an
 * impulse load
 * 
 * @author alessiogambi
 * 
 */
public class ImpulseLoadGenerator implements LoadGenerator {
	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

	public static final String INTENSITY_LB = "at.ac.tuwien.loadgenerator.impulse.intensity.lb";
	public static final String INTENSITY_UB = "at.ac.tuwien.loadgenerator.impulse.intensity.ub";

	public static final String WHEN = "at.ac.tuwien.loadgenerator.impulse.when";

	public static final String IMPULSE_DURATION_LB = "at.ac.tuwien.loadgenerator.impulse.duration.lb";
	public static final String IMPULSE_DURATION_UB = "at.ac.tuwien.loadgenerator.impulse.duration.ub";

	private String jmeterTestFile;
	private String serviceManifest;

	private final List<String> clientList;
	private final String traceGeneratorWebServiceURL;
	private Random random;

	private int duration;

	private double[] intensityBounds;
	private int whenInSecs;
	private double[] impulseDurationBounds;

	private int nBins;

	private InputSampler inputSampler;

	private Logger logger;

	public double[] getLowerBounds() {
		return new double[] { intensityBounds[0], impulseDurationBounds[0] };
	}

	public double[] getUpperBounds() {
		return new double[] { intensityBounds[1], impulseDurationBounds[1] };
	}

	private TypeCoercer coercer;

	public void setJmeterTestFile(String jmeterTestFile) {
		this.jmeterTestFile = jmeterTestFile;
	}

	public void setServiceManifest(String serviceManifest) {
		this.serviceManifest = serviceManifest;
	}

	private String serviceID;

	public ImpulseLoadGenerator(
			Logger logger,
			// THIS IS BAD!
			String serviceID,//
			TypeCoercer coercer, String traceGeneratorWebService,
			String jmeterTestFile, String manifest, //
			double intensityLB, double intensityUB,//
			int whenInSecs, //
			int impulseDurationLB, double impulseDurationUB, //
			int nBins, InputSampler inputSampler, int duration) {

		this.logger = logger;
		this.jmeterTestFile = jmeterTestFile;
		this.serviceManifest = manifest;
		this.coercer = coercer;
		this.traceGeneratorWebServiceURL = traceGeneratorWebService;
		this.clientList = JMeterUtils.getClientIDsFromTestFile(jmeterTestFile);
		this.random = new Random(System.currentTimeMillis());

		this.intensityBounds = new double[] { intensityLB, intensityUB };
		this.whenInSecs = whenInSecs;
		// Automatic casting to double from integer
		this.impulseDurationBounds = new double[] { impulseDurationLB,
				impulseDurationUB };

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

			// There's When parameter
			Number[] _parameters = new Number[parameters.length + 1];

			_parameters[0] = parameters[0];
			// Inject the WHEN parameter (it's fixed)
			_parameters[1] = whenInSecs;
			_parameters[2] = parameters[1];

			initialTestSuite.add(generateTest(_parameters));

		}
		return initialTestSuite;
	}

	/*
	 * Pars must be compatible with the number of clients. No nPars >= clients *
	 * getParamaters()
	 */
	public Test generateTest(Number... pars) {
		// NOTE that the when parameter is fixed by default !
		logger.debug("generateTest() " + Arrays.toString(pars));
		List<SingleTraceSpecification> allClients = new ArrayList<SingleTraceSpecification>();
		int index = 0;
		for (String clientID : clientList) {
			if (index + getNumberOfParameters() <= pars.length) {

				// Force default values for additional 2 parameters
				double intensity = Math.floor(pars[0].doubleValue());
				int when = pars[1].intValue();
				// Whatever number this is kust get the int value
				int impulseDuration = pars[2].intValue();

				Number[] _pars = new Number[getNumberOfParameters()];
				_pars[0] = intensity;
				_pars[1] = when;
				_pars[2] = impulseDuration;

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
		// Random test cases make sense only at 0 digits for the number of
		// clients !
		return generateTest(
		// Intensity of impulse
				randomInRange(random, intensityBounds[0], intensityBounds[1], 0),
				// When
				whenInSecs,
				// max client
				randomInRange(random, impulseDurationBounds[0],
						impulseDurationBounds[1], 0));
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

		Number[] _parameters = new Number[result.length + 1];
		System.arraycopy(result, 0, _parameters, 0, result.length);
		_parameters[result.length] = whenInSecs;

		return generateTest(_parameters);

	}

}
