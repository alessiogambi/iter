package at.ac.tuwien.iter.loadgenerators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.iter.services.impl.loadgenerators.LatinHypercubeInputSampler;

public class LHSTest {
	public static void main(String[] args) {

		Logger logger = LoggerFactory
				.getLogger(LatinHypercubeInputSampler.class);
		System.out.println("LHSTest.main() " + Math.pow(10, 3));
		LatinHypercubeInputSampler lhs = new LatinHypercubeInputSampler(logger);
		int nSamples = 10;

		List<Number[]> samples = lhs.sample(nSamples,
				new double[] { 0.0, 0.0 }, new double[] { 0.0, 100.0 });

		for (Number[] sample : samples) {
			for (int i = 0; i < sample.length; i++) {
				System.out.print(sample[i].doubleValue() + " ");
			}
			System.out.print("\n");
		}

	}
}
