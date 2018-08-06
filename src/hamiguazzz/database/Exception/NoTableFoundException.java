package hamiguazzz.database.Exception;

public class NoTableFoundException extends RuntimeException {
	public NoTableFoundException() {
	}

	public NoTableFoundException(String message) {
		super(message);
	}

	public NoTableFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoTableFoundException(Throwable cause) {
		super(cause);
	}
}
