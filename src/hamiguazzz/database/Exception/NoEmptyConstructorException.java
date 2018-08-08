package hamiguazzz.database.Exception;

public class NoEmptyConstructorException extends Exception {
	public NoEmptyConstructorException() {
	}

	public NoEmptyConstructorException(String s) {
		super(s);
	}

	public NoEmptyConstructorException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoEmptyConstructorException(Throwable cause) {
		super(cause);
	}

}
