package at.ac.tuwien.iter.services.impl.loadgenerators;

import java.util.List;

public interface InputSampler {

	public List<Number[]> sample(int samples, double[] lowerBounds,
			double[] upperBounds);
}
