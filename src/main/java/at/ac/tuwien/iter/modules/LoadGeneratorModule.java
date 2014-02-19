package at.ac.tuwien.iter.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.ServiceId;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.gambi.tapestry5.cli.annotations.CLIOption;
import org.slf4j.Logger;

import at.ac.tuwien.iter.loadgenerators.ConstantLoadGenerator;
import at.ac.tuwien.iter.loadgenerators.RampLoadGenerator;
import at.ac.tuwien.iter.loadgenerators.SawToothLoadGenerator;
import at.ac.tuwien.iter.loadgenerators.SinusLoadGenerator;
import at.ac.tuwien.iter.loadgenerators.SquareLoadGenerator;
import at.ac.tuwien.iter.loadgenerators.StepLoadGenerator;
import at.ac.tuwien.iter.loadgenerators.TriangleLoadGenerator;
import at.ac.tuwien.iter.services.LoadGenerator;
import at.ac.tuwien.iter.services.LoadGeneratorSource;
import at.ac.tuwien.iter.services.impl.RuntimeSymbolProvider;
import at.ac.tuwien.iter.services.impl.loadgenerators.InputSampler;
import at.ac.tuwien.iter.services.impl.loadgenerators.LatinHypercubeInputSampler;
import at.ac.tuwien.iter.services.impl.loadgenerators.LoadGeneratorSourceImpl;
import at.ac.tuwien.iter.services.impl.loadgenerators.RandomInputSampler;
import at.ac.tuwien.iter.utils.IterSymbolsNames;

/**
 * The load generator module contains the tapestry-ioc managed classes that
 * define the bindings to the trace generator web service
 * 
 * This module is a SubModule of {@link IterModule}
 * 
 * @author alessiogambi
 * 
 */
public class LoadGeneratorModule {

	public static final String DURATION_IN_SEC = "at.ac.tuwien.iter.experiment.duration";

	public static void contributeFactoryDefaults(
			MappedConfiguration<String, Object> configuration) {

		configuration.add(LoadGeneratorModule.DURATION_IN_SEC, "300");
	}

	/**
	 * Load generator need some user configurations. We specify them as
	 * contributions to the CLIParser that is the service that interprets the
	 * command line, validates the input, and offers the input values to the
	 * other objects managed by tapestry-ioc
	 * 
	 * We define an option (-l, --load-generator-name) to let the user specify
	 * her preference about the service to create.
	 * 
	 * @param configuration
	 * 
	 * @category UserContribution CLIParser
	 */
	public void contributeCLIParser(
			Configuration<org.gambi.tapestry5.cli.data.CLIOption> configuration) {
		org.gambi.tapestry5.cli.data.CLIOption loadGeneratorName = new org.gambi.tapestry5.cli.data.CLIOption(
				"l", "load-generator-name", 1, false,
				"Symbolic name of the load generator.");

		// This can be a plain symbol as well;
		loadGeneratorName.setDefaultValue("sine-lhs");
		configuration.add(loadGeneratorName);
	}

	/**
	 * Build the LoadGeneratorSource service that contains and instantiate the
	 * LoadGenerator classes
	 * 
	 * @param logger
	 * @param configurations
	 * @return
	 * 
	 * @category Build LoadGeneratorSource
	 */
	public static LoadGeneratorSource build(Logger logger,
			Map<String, LoadGenerator> configurations) {
		return new LoadGeneratorSourceImpl(logger, configurations);
	}

	/*
	 * TODO Not sure this is ok !
	 */
	public void contributeSymbolSource(
			@InjectService("LoadGeneratorSymbolProvider") SymbolProvider symbolProvider,
			OrderedConfiguration<SymbolProvider> configuration) {

		configuration.add("LoadGeneratorRuntimeValues", symbolProvider);

	}

	/*
	 * TODO Not sure this is ok !
	 */
	public RuntimeSymbolProvider buildLoadGeneratorSymbolProvider() {
		RuntimeSymbolProvider loadGeneratorRuntimeValues = new RuntimeSymbolProvider() {
			Map<String, String> symbols = new HashMap<String, String>();

			public String valueForSymbol(String symbolName) {
				return symbols.get(symbolName);
			}

			public void addSymbols(Map<String, String> arg0) {
				symbols.putAll(arg0);
			}
		};
		return loadGeneratorRuntimeValues;
	}

	@ServiceId("CommandLineLoadGenerator")
	public LoadGenerator buildCLILoadGenerator(
			/*
			 * TODO Not sure this is ok !
			 */
			@InjectService("LoadGeneratorSymbolProvider") RuntimeSymbolProvider symbolProvider,
			LoadGeneratorSource loadGeneratorSource,
			@CLIOption(longName = "load-generator-name") String loadGeneratorId) {

		LoadGenerator loadGenerator = loadGeneratorSource
				.getLoadGenerator(loadGeneratorId);

		Map<String, String> loadGeneratorSymbols = new HashMap<String, String>();
		loadGeneratorSymbols.put(IterSymbolsNames.LB,
				Arrays.toString(loadGenerator.getLowerBounds()));
		loadGeneratorSymbols.put(IterSymbolsNames.UB,
				Arrays.toString(loadGenerator.getUpperBounds()));

		symbolProvider.addSymbols(loadGeneratorSymbols);

		return loadGenerator;
	}

	public LoadGenerator buildRandomSinusLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,

			@Symbol(IterSymbolsNames.N_BINS) int nBins,

			@Symbol(SinusLoadGenerator.AMPLITUDE_LB) double amplitudeLB,
			@Symbol(SinusLoadGenerator.AMPLITUDE_UB) double amplitudeUB,
			@Symbol(SinusLoadGenerator.FREQUENCY_LB) double frequencyLB,
			@Symbol(SinusLoadGenerator.FREQUENCY_UB) double frequencyUB,

			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration

	) {

		InputSampler randomSampler = new RandomInputSampler();

		return new SinusLoadGenerator(logger, "RandomSinusLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, amplitudeLB, amplitudeUB, frequencyLB,
				frequencyUB, nBins, randomSampler, duration);
	}

	public LoadGenerator buildLHSSinusLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			// I do not like this approach... This is dependend on the Input
			// args ...Options
			// So make it dependent on them !!
			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(SinusLoadGenerator.AMPLITUDE_LB) double amplitudeLB,
			@Symbol(SinusLoadGenerator.AMPLITUDE_UB) double amplitudeUB,
			@Symbol(SinusLoadGenerator.FREQUENCY_LB) double frequencyLB,
			@Symbol(SinusLoadGenerator.FREQUENCY_UB) double frequencyUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new SinusLoadGenerator(logger, "LHSSinusLoadGenerator", coercer,
				traceGeneratorWebService, jmeterClientsURL, manifestURL,
				amplitudeLB, amplitudeUB, frequencyLB, frequencyUB, nBins,
				lhsSampler, duration);
	}

	public LoadGenerator buildRandomTriangleLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(TriangleLoadGenerator.AMPLITUDE_LB) double amplitudeLB,
			@Symbol(TriangleLoadGenerator.AMPLITUDE_UB) double amplitudeUB,
			@Symbol(TriangleLoadGenerator.PERIOD_LB) double periodLB,
			@Symbol(TriangleLoadGenerator.PERIOD_UB) double periodUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler randomSampler = new RandomInputSampler();

		return new TriangleLoadGenerator(logger, "RandomTriangleLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, amplitudeLB, amplitudeUB, periodLB, periodUB,
				nBins, randomSampler, duration);
	}

	public LoadGenerator buildLHSTriangleLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(TriangleLoadGenerator.AMPLITUDE_LB) double amplitudeLB,
			@Symbol(TriangleLoadGenerator.AMPLITUDE_UB) double amplitudeUB,
			@Symbol(TriangleLoadGenerator.PERIOD_LB) double periodLB,
			@Symbol(TriangleLoadGenerator.PERIOD_UB) double periodUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new TriangleLoadGenerator(logger, "LHSTriangleLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, amplitudeLB, amplitudeUB, periodLB, periodUB,
				nBins, lhsSampler, duration);
	}

	public LoadGenerator buildRandomSawToothLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(SawToothLoadGenerator.STEP_LB) double stepLB,
			@Symbol(SawToothLoadGenerator.STEP_UB) double stepUB,
			@Symbol(SawToothLoadGenerator.PERIOD_LB) double periodLB,
			@Symbol(SawToothLoadGenerator.PERIOD_UB) double periodUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler randomSampler = new RandomInputSampler();

		return new SawToothLoadGenerator(logger, "RandomSawToothLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, stepLB, stepUB, periodLB, periodUB, nBins,
				randomSampler, duration);
	}

	public LoadGenerator buildLHSSawToothLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(SawToothLoadGenerator.STEP_LB) double stepLB,
			@Symbol(SawToothLoadGenerator.STEP_UB) double stepUB,
			@Symbol(SawToothLoadGenerator.PERIOD_LB) double periodLB,
			@Symbol(SawToothLoadGenerator.PERIOD_UB) double periodUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new SawToothLoadGenerator(logger, "LHSSawToothLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, stepLB, stepUB, periodLB, periodUB, nBins,
				lhsSampler, duration);
	}

	public LoadGenerator buildRandomSquareLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(SquareLoadGenerator.AMPLITUDE_LB) double amplitudeLB,
			@Symbol(SquareLoadGenerator.AMPLITUDE_UB) double amplitudeUB,
			@Symbol(SquareLoadGenerator.FREQUENCY_LB) double frequencyLB,
			@Symbol(SquareLoadGenerator.FREQUENCY_UB) double frequencyUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler randomSampler = new RandomInputSampler();

		return new SquareLoadGenerator(logger, "RandomSquareLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, amplitudeLB, amplitudeUB, frequencyLB,
				frequencyUB, nBins, randomSampler, duration);
	}

	public LoadGenerator buildLHSSquareLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(SquareLoadGenerator.AMPLITUDE_LB) double amplitudeLB,
			@Symbol(SquareLoadGenerator.AMPLITUDE_UB) double amplitudeUB,
			@Symbol(SquareLoadGenerator.FREQUENCY_LB) double frequencyLB,
			@Symbol(SquareLoadGenerator.FREQUENCY_UB) double frequencyUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new SquareLoadGenerator(logger, "LHSSquareLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, amplitudeLB, amplitudeUB, frequencyLB,
				frequencyUB, nBins, lhsSampler, duration);
	}

	public LoadGenerator buildRandomConstantLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(ConstantLoadGenerator.INTENSITY_LB) double intensityLB,
			@Symbol(ConstantLoadGenerator.INTENSITY_UB) double intensityUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {
		InputSampler randomSampler = new RandomInputSampler();

		return new ConstantLoadGenerator(logger, "RandomConstantLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, intensityLB, intensityUB, nBins, randomSampler,
				duration);
	}

	public LoadGenerator buildLHSConstantLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,

			@Symbol(ConstantLoadGenerator.INTENSITY_LB) double intensityLB,
			@Symbol(ConstantLoadGenerator.INTENSITY_UB) double intensityUB,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new ConstantLoadGenerator(logger, "LHSConstantLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, intensityLB, intensityUB, nBins, lhsSampler,
				duration);
	}

	public LoadGenerator buildRandomStepLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(StepLoadGenerator.MIN_LB) double minLB,
			@Symbol(StepLoadGenerator.MIN_UB) double minUB,
			@Symbol(StepLoadGenerator.MAX_LB) double maxLB,
			@Symbol(StepLoadGenerator.MAX_UB) double maxUB,
			@Symbol(StepLoadGenerator.WHEN) int when,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {
		InputSampler randomSampler = new RandomInputSampler();

		return new StepLoadGenerator(logger, "RandomStepLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, minLB, minUB, maxLB, maxUB, when, nBins,
				randomSampler, duration);
	}

	public LoadGenerator buildLHSStepLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,

			@Symbol(StepLoadGenerator.MIN_LB) double minLB,
			@Symbol(StepLoadGenerator.MIN_UB) double minUB,
			@Symbol(StepLoadGenerator.MAX_LB) double maxLB,
			@Symbol(StepLoadGenerator.MAX_UB) double maxUB,
			@Symbol(StepLoadGenerator.WHEN) int when,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new StepLoadGenerator(logger, "LHSStepLoadGenerator", coercer,
				traceGeneratorWebService, jmeterClientsURL, manifestURL, minLB,
				minUB, maxLB, maxUB, when, nBins, lhsSampler, duration);
	}

	public LoadGenerator buildRandomRampLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,
			@Symbol(RampLoadGenerator.MIN_LB) double minLB,
			@Symbol(RampLoadGenerator.MIN_UB) double minUB,
			@Symbol(RampLoadGenerator.STEP_LB) double stepLB,
			@Symbol(RampLoadGenerator.STEP_UB) double stepUB,
			@Symbol(RampLoadGenerator.STEP_DURATION) int stepDuration,
			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {
		InputSampler randomSampler = new RandomInputSampler();

		return new RampLoadGenerator(logger, "RandomRampLoadGenerator",
				coercer, traceGeneratorWebService, jmeterClientsURL,
				manifestURL, minLB, minUB, stepLB, stepUB, stepDuration, nBins,
				randomSampler, duration);
	}

	public LoadGenerator buildLHSRampLoadGenerator(
			Logger logger,
			TypeCoercer coercer,
			@Symbol(IterSymbolsNames.TRACEGENERATOR_URL) String traceGeneratorWebService,

			@CLIOption(longName = "jmeter-clients-URL") String jmeterClientsURL,
			@CLIOption(longName = "service-manifest-URL") String manifestURL,
			@Symbol(IterSymbolsNames.N_BINS) int nBins,

			@Symbol(RampLoadGenerator.MIN_LB) double minLB,
			@Symbol(RampLoadGenerator.MIN_UB) double minUB,
			@Symbol(RampLoadGenerator.STEP_LB) double stepLB,
			@Symbol(RampLoadGenerator.STEP_UB) double stepUB,
			@Symbol(RampLoadGenerator.STEP_DURATION) int stepDuration,

			@Symbol(LoadGeneratorModule.DURATION_IN_SEC) int duration) {

		InputSampler lhsSampler = new LatinHypercubeInputSampler();

		return new RampLoadGenerator(logger, "LHSRampLoadGenerator", coercer,
				traceGeneratorWebService, jmeterClientsURL, manifestURL, minLB,
				minUB, stepLB, stepUB, stepDuration, nBins, lhsSampler,
				duration);
	}

	@Contribute(LoadGeneratorSource.class)
	public static void addLoadGenerators(
			TypeCoercer coercer,
			@InjectService("RandomSinusLoadGenerator") LoadGenerator randomSine,
			@InjectService("LHSSinusLoadGenerator") LoadGenerator lhsSine,

			@InjectService("RandomTriangleLoadGenerator") LoadGenerator randomTriangle,
			@InjectService("LHSTriangleLoadGenerator") LoadGenerator lhsTriangle,

			@InjectService("RandomSawToothLoadGenerator") LoadGenerator randomSawTooth,
			@InjectService("LHSSawToothLoadGenerator") LoadGenerator lhsSawTooth,

			@InjectService("RandomSquareLoadGenerator") LoadGenerator randomSquare,
			@InjectService("LHSSquareLoadGenerator") LoadGenerator lhsSquare,

			@InjectService("RandomConstantLoadGenerator") LoadGenerator randomConstant,
			@InjectService("LHSConstantLoadGenerator") LoadGenerator lhsConstant,

			@InjectService("RandomStepLoadGenerator") LoadGenerator randomStep,
			@InjectService("LHSStepLoadGenerator") LoadGenerator lhsStep,

			@InjectService("RandomRampLoadGenerator") LoadGenerator randomRamp,
			@InjectService("LHSRampLoadGenerator") LoadGenerator lhsRamp,

			MappedConfiguration<String, LoadGenerator> configuration) {

		configuration.add("sine", randomSine);
		configuration.add("sine-rand", randomSine);
		configuration.add("sine-lhs", lhsSine);

		configuration.add("triangle", randomTriangle);
		configuration.add("triangle-rand", randomTriangle);
		configuration.add("triangle-lhs", lhsTriangle);

		configuration.add("sawtooth", randomSawTooth);
		configuration.add("sawtooth-rand", randomSawTooth);
		configuration.add("sawtooth-lhs", lhsSawTooth);

		configuration.add("square", randomSquare);
		configuration.add("square-rand", randomSquare);
		configuration.add("square-lhs", lhsSquare);

		configuration.add("constant", randomConstant);
		configuration.add("constant-rand", randomConstant);
		configuration.add("constant-lhs", lhsConstant);

		configuration.add("step", randomStep);
		configuration.add("step-rand", randomStep);
		configuration.add("step-lhs", lhsStep);

		configuration.add("ramp", randomRamp);
		configuration.add("ramp-rand", randomRamp);
		configuration.add("ramp-lhs", lhsRamp);

	}
}
