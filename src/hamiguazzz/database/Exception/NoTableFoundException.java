package hamiguazzz.database.Exception;

public class NoTableFoundException extends RuntimeException {
	public NoTableFoundException() {
	}

	public NoTableFoundException(String message) {
		super(message);
	}
}
