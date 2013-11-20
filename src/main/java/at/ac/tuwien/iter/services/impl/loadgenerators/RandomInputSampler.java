package at.ac.tuwien.iter.services.impl.loadgenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomInputSampler implements InputSampler {

	private Random randomGenerator;

	public RandomInputSampler() {
		randomGenerator = new Random(System.currentTimeMillis());
	}

	private double randomInRange(double min, double max) {
		return (min + randomGenerator.nextDouble() * (max - min));
	}

	public List<Number[]> sample(int nSamples, double[] lowerBounds,
			double[] upperBounds) {

		// Lower and Upper bounds must have the same dimensions!
		int size = lowerBounds.length;
		List<Number[]> samples = new ArrayList<Number[]>();
		for (int n = 0; n < nSamples; n++) {
			Number[] sample = new Number[size];
			for (int p = 0; p < size; p++) {
				sample[p] = randomInRange(lowerBounds[p], upperBounds[p]);
			}
		}
		return samples;
	}
}
