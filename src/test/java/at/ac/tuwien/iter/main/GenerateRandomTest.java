package at.ac.tuwien.iter.main;

import java.util.Arrays;
import java.util.Random;

public class GenerateRandomTest {

	private static double randomInRange(Random r, double min, double max,
			int digits) {

		return Math.floor((min + r.nextDouble() * (max - min))
				* Math.pow(10, digits))
				/ Math.pow(10, digits);
	}

	public static void main(String[] args) {

		double[] amplitudeBounds = new double[] { 1.0, 20.0 };
		double[] frequencyBounds = new double[] { 0.0, 0.01 };
		double[] verticalShiftBounds = new double[] { 1.0, 30.0 };
		double[] params = new double[3];

		Random random = new Random(System.currentTimeMillis());

		params[0] = randomInRange(random, amplitudeBounds[0],
				amplitudeBounds[1], 1);
		params[1] = randomInRange(random, frequencyBounds[0],
				frequencyBounds[1], 3);
		params[2] = randomInRange(random, verticalShiftBounds[0],
				verticalShiftBounds[1], 1);

		System.out.println("GenerateRandomTest.main() "
				+ Arrays.toString(params));
	}
}
