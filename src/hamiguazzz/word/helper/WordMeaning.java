package hamiguazzz.word.helper;

import java.util.Arrays;

public final class WordMeaning {
	String part;
	String[] means;

	//region Generated Codes
	public String getPart() {
		return part;
	}

	public String[] getMeans() {
		return means;
	}

	@Override
	public String toString() {
		return "{" +
				"part=" + part +
				", means=" + (means == null ? null : Arrays.asList(means)) +
				'}';
	}
	//endregion
}
