package at.ac.tuwien.iter.exceptions;

public class TestExecutionException extends Exception {

	private static final long serialVersionUID = -1771695276788094290L;

	public TestExecutionException() {
		super();
	}

	public TestExecutionException(String message, Throwable t) {
		super(message, t);
	}

	public TestExecutionException(String message) {
		super(message);
	}

	public TestExecutionException(Throwable t) {
		super("Test execution failed due to the following exception:\n", t);
	}
}
