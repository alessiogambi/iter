package at.ac.tuwien.iter.services.impl.loadgenerators;

import jaea.optimization.sampling.SimpleLH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class LatinHypercubeInputSampler implements InputSampler {

	private Logger logger;

	public LatinHypercubeInputSampler(Logger logger) {
		this.logger = logger;
	}

	// Interval must be [0 - nIntervals-1] !!
	public double getMiddlePointInInterval(int _interval, int _nIntervals,
			double min, double max) {

		double interval = (double) _interval;
		double nIntervals = (double) _nIntervals;
		double intervalSize = (max - min) / nIntervals;
		return min + (interval + 0.5) * intervalSize;
	}

	public List<Number[]> sample(int nSamples, double[] lowerBounds,
			double[] upperBounds) {

		// Get number of dimensions
		int D = lowerBounds.length;

		// Compute the LHS over the indices
		SimpleLH LHGen = new SimpleLH();

		// lhs is nSamples X D
		int[][] lhs = LHGen.getSimpleLH(nSamples, D);

		List<Number[]> samples = new ArrayList<Number[]>();

		for (int n = 0; n < nSamples; n++) {
			Number[] sample = new Number[D];
			System.out.println("LatinHypercubeInputSampler.sample() "
					+ Arrays.toString(lhs[n]));
			for (int d = 0; d < D; d++) {

				sample[d] = getMiddlePointInInterval(lhs[n][d] - 1, nSamples,
						lowerBounds[d], upperBounds[d]);
			}
			samples.add(sample);
		}

		return samples;
	}

}
