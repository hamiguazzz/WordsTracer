package hamiguazzz.word.helper;

import java.util.Arrays;

public final class WordMeaningEn {
	String[] means;
	String[] examples;
	String[] similar_words;
	String part;

	//region Generated Codes
	public String[] getMeans() {
		return means;
	}

	public String[] getExamples() {
		return examples;
	}

	public String[] getSimilar_words() {
		return similar_words;
	}

	public String getPart() {
		return part;
	}

	@Override
	public String toString() {
		return "{" +
				"means=" + (means == null ? null : Arrays.asList(means)) +
				", examples=" + (examples == null ? null : Arrays.asList(examples)) +
				", similar_words=" + (similar_words == null ? null : Arrays.asList(similar_words)) +
				", part=" + part +
				'}';
	}
	//endregion
}
