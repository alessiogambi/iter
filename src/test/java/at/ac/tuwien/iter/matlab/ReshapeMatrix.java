package at.ac.tuwien.iter.matlab;

import java.util.ArrayList;
import java.util.List;

public class ReshapeMatrix {
	public static void main(String[] args) {
		Object[] result = new Object[1];
		result[0] = new double[] { 1.0, 1.0, 2.0, 2.0, 3.0, 0.0, 2.0, 1.0, 3.0,
				2.0, 0.5, 0.5, 0.5, 0.5, 1.0 };

		double[] _result = (double[]) (result[0]);

		// We know that each row will contains 4 columns
		int nColums = 5;
		int nRows = _result.length / nColums;

		System.out.println("ReshapeMatrix.main() " + nRows + " " + nColums);

		List<Double[]> theResult = new ArrayList<Double[]>(nRows);
		// Initialize the structure
		for (int row = 0; row < nRows; row++) {
			theResult.add(new Double[nColums]);
		}

		for (int i = 0; i < _result.length; i++) {
			System.out.println("ReshapeMatrix.main() " + (i % nRows) + " - "
					+ (i / nRows));
			theResult.get(i % nRows)[i / nRows] = _result[i];
		}

		// Print result
		for (Double[] row : theResult) {
			for (int col = 0; col < row.length; col++) {
				System.out.print(row[col] + " ");
			}
			System.out.print("\n");
		}

		// OLD VERSION
		// int entrySize = _result.length / n;
		// for (int i = 0; i < _result.length; i = i + entrySize) {
		// Double[] entry = new Double[entrySize];
		// // Skip the first value which is the Expected improvement
		// StringBuffer sb = new StringBuffer();
		// sb.append("Max E(I) = ");
		// sb.append(_result[i]);
		// sb.append(" : ");
		// for (int j = 0; j < entrySize - 1; j++) {
		// entry[j] = _result[i + 1 + j];
		// sb.append(_result[i + 1 + j]);
		// sb.append(", ");
		// }
		// theResult.add(entry);
		// sb.append("\n");
		// System.out.println("ReshapeMatrix.main() " + sb.toString());
		// }
		// java.lang.Object array, length = 1
		// index 0, double array, length = 16 n x [max, nparams]
	}
}
