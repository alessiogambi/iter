package at.ac.tuwien.iter.services.impl.assertions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class PlasticityAssertionTest {

	private PlasticityAssertion assertion;

	@Before
	public void setup() {
		assertion = new PlasticityAssertion(
				LoggerFactory.getLogger(PlasticityAssertion.class), null);
	}

	@Test
	public void check1() {
		List<double[]> transitions = new ArrayList<double[]>();
		transitions.add(new double[] { 1.000000, 0.000000, 0.500000 });
		transitions.add(new double[] { 1.000000, 2.000000, 0.500000 });
		transitions.add(new double[] { 2.000000, 1.000000, 0.500000 });
		transitions.add(new double[] { 2.000000, 3.000000, 0.500000 });
		transitions.add(new double[] { 3.000000, 2.000000, 1.000000 });
		Assert.assertEquals("PASSED", assertion.check(transitions));
	}

	@Test
	public void check2() {
		List<double[]> transitions = new ArrayList<double[]>();
		transitions.add(new double[] { 1.000000, 2.000000, 1.000000 });
		transitions.add(new double[] { 2.000000, 1.000000, 1.000000 });
		Assert.assertEquals("PASSED", assertion.check(transitions));
	}

	@Test
	public void check3() {
		List<double[]> transitions = new ArrayList<double[]>();
		transitions.add(new double[] { 1.000000, 0.000000, 0.500000 });
		transitions.add(new double[] { 1.000000, 2.000000, 0.500000 });
		transitions.add(new double[] { 2.000000, 1.000000, 0.500000 });
		transitions.add(new double[] { 2.000000, 3.000000, 0.500000 });
		transitions.add(new double[] { 3.000000, 2.000000, 1.000000 });
		Assert.assertEquals("PASSED", assertion.check(transitions));

	}

	@Test
	public void check4() {
		List<double[]> transitions = new ArrayList<double[]>();
		Assert.assertEquals("SKIPPED", assertion.check(transitions));

	}

	@Test
	public void check5() {
		List<double[]> transitions = null;
		Assert.assertEquals("SKIPPED", assertion.check(transitions));

	}

	@Test
	public void check6() {
		List<double[]> transitions = new ArrayList<double[]>();
		transitions.add(new double[] { 1.000000, 2.000000, 1.00000 });
		transitions.add(new double[] { 2.000000, 3.000000, 1.00000 });
		transitions.add(new double[] { 3.000000, 2.000000, 1.000000 });
		Assert.assertEquals("FAILED", assertion.check(transitions));

	}
}
