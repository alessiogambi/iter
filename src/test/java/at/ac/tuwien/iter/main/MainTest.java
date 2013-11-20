package at.ac.tuwien.iter.main;

import java.util.ArrayList;
import java.util.List;

public class MainTest {

	public static void main(String[] args) {
		double[] _result = new double[] { 1, 2, 3, 4, 5, 6 };

		// We know that each row will contains 3 columns (i,j,phi_i,j)
		int nColums = 3;
		int nRows = _result.length / nColums;

		System.out.println("MatlabControlImpl.inferModel() Col = " + nColums);
		System.out.println("MatlabControlImpl.inferModel() Row = " + nRows);

		List<double[]> result = new ArrayList<double[]>(nRows);

		System.out.println("MatlabControlImpl.inferModel() Result.size() = "
				+ result.size());

		// Initialize the structure
		for (int row = 0; row < nRows; row++) {
			result.add(new double[nColums]);
		}

		for (int i = 0; i < _result.length; i++) {
			System.out.println("MatlabControlImpl.inferModel() map " + i
					+ " to Col " + (i % nColums) + " row " + (i % nRows));
			result.get(i % nRows)[i % nColums] = _result[i];
		}

	}
}
