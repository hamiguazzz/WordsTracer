package hamiguazzz.database.Exception;

public class ConvertFailedException extends RuntimeException {
	public ConvertFailedException() {
	}

	public ConvertFailedException(String message) {
		super(message);
	}

	public ConvertFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConvertFailedException(Throwable cause) {
		super(cause);
	}
}
