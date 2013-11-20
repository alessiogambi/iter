package at.ac.tuwien.iter.services;

import java.util.List;

import at.ac.tuwien.iter.data.TestResult;

import matlabcontrol.MatlabInvocationException;

public interface MathEngineDao {

	/**
	 * Insert the results of a test execution represented as stateSequence
	 * 
	 * @param statesequence
	 * @param parameters
	 * @throws MatlabInvocationException
	 */
	public void addTestExecution(TestResult testResult)
			throws MatlabInvocationException;

	// public void addTestExecution(double[] stateSequence, double...
	// parameters)
	// throws MatlabInvocationException;

	/**
	 * Remove the corresponding test data from the system.
	 * 
	 * @param ID
	 */
	public void removeTestExecution(String ID);

	/**
	 * Return the list of critical test cases found so far. Each entry is a
	 * parameter assignment value
	 */
	public List<double[]> getCriticalTestCases();

	// TODO what if test case results in a critical test case but NOT in all its
	// executions? What is the semantic ? At least one execution returned in a
	// plastic behavior?

	/**
	 * Return a list of Double[], where Double[0] and Double[1] identifies a
	 * transition, and Double[2] corresponds to the EI. The size of the list is
	 * always less or equals to n, and all the EI have a value greater or equal
	 * than minEI.
	 * 
	 * The method return the actual n top-most transitions with respect to the
	 * EI measure. Additionally the List is ordered by desc EI values.
	 * 
	 * @throws MatlabInvocationException
	 */
	public List<double[]> getBestPlasticityTests(int n)
			throws MatlabInvocationException;

	/**
	 * Infer a model from the transition sequence
	 * 
	 * @param transitions
	 * @return
	 * @throws MatlabInvocationException
	 */
	public List<double[]> inferModel(double[] transitions)
			throws MatlabInvocationException;
}
