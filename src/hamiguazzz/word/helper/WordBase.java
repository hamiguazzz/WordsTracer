package hamiguazzz.word.helper;

import java.util.Arrays;
import java.util.Objects;

public final class WordBase {
	String word;
	int frequency;
	String simple_meaning;
	String pronunciation_en;
	String pronunciation_am;
	String[] tags;

	//region Generated Codes
	public String getWord() {
		return word;
	}

	public String getSimple_meaning() {
		return simple_meaning;
	}

	public int getFrequency() {
		return frequency;
	}

	public String getPronunciation_en() {
		return pronunciation_en;
	}

	public String getPronunciation_am() {
		return pronunciation_am;
	}

	public String[] getTags() {
		return tags;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WordBase wordBase = (WordBase) o;
		return Objects.equals(word, wordBase.word);
	}

	@Override
	public int hashCode() {
		return Objects.hash(word);
	}

	@Override
	public String toString() {
		return "Base{" +
				"word=" + word +
				",zh=" + simple_meaning +
				", frequency=" + frequency +
				", pronunciation_en=" + pronunciation_en +
				", pronunciation_am=" + pronunciation_am +
				", tags=" + (tags == null ? null : Arrays.asList(tags)) +
				'}';
	}
	//endregion
}
