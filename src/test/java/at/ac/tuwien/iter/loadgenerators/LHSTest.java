package at.ac.tuwien.iter.loadgenerators;

import java.util.List;

import at.ac.tuwien.iter.services.impl.loadgenerators.LatinHypercubeInputSampler;

public class LHSTest {
	public static void main(String[] args) {

		System.out.println("LHSTest.main() " + Math.pow(10, 3));
		LatinHypercubeInputSampler lhs = new LatinHypercubeInputSampler();
		List<Number[]> samples = lhs.sample(10, new double[] { 1.0, 0.0 },
				new double[] { 50.0, 0.01 });
		for (Number[] sample : samples) {
			for (int i = 0; i < sample.length; i++) {
				System.out.print(sample[i].doubleValue() + " ");
			}
			System.out.print("\n");
		}

	}
}
