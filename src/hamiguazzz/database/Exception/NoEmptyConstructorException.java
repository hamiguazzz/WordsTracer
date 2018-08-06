package hamiguazzz.database.Exception;

public class NoEmptyConstructorException extends NoSuchMethodException {
	public NoEmptyConstructorException() {
	}

	public NoEmptyConstructorException(String s) {
		super(s);
	}
}
