package at.ac.tuwien.iter.exceptions;

public final class TestTimeoutException extends TestExecutionException {

	private static final long serialVersionUID = 8952212738565215344L;

	public TestTimeoutException() {
		super();
	}

	public TestTimeoutException(String message) {
		super(message);
	}

}
