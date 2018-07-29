package hamiguazzz.word;

import hamiguazzz.word.helper.WordBase;
import hamiguazzz.word.helper.WordExchange;
import hamiguazzz.word.helper.WordMeaning;
import hamiguazzz.word.helper.WordMeaningEn;

import java.util.Arrays;
import java.util.Objects;

public class Word {

	private WordBase word;
	private WordExchange wordExchange;
	private WordMeaning[] means;
	private WordMeaningEn[] meaningEns;

	public Word(WordBase word, WordExchange wordExchange, WordMeaning[] means, WordMeaningEn[] meaningEns) {
		this.word = word;
		this.wordExchange = wordExchange;
		this.means = means;
		this.meaningEns = meaningEns;
	}

	//region Generated Codes
	public WordBase getWordBase() {
		return word;
	}

	public WordExchange getWordExchange() {
		return wordExchange;
	}

	public WordMeaning[] getMeaningsZH() {
		return means;
	}

	public WordMeaningEn[] getMeaningsEN() {
		return meaningEns;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Word word1 = (Word) o;
		return Objects.equals(word, word1.word);
	}

	@Override
	public int hashCode() {
		return Objects.hash(word);
	}

	@Override
	public String toString() {
		return "Word{" +
				word + "," +
				wordExchange +
				", meanings_zh=" + (means == null ? null : Arrays.asList(means)) +
				", meanings_en=" + (meaningEns == null ? null : Arrays.asList(meaningEns)) +
				'}';
	}
	//endregion

	//region From WordBase
	public String getWord() {
		return word.getWord();
	}

	public String getSimple_meaning() {
		return word.getSimple_meaning();
	}

	public String[] getTags() {
		return word.getTags();
	}

	public int getFrequency() {
		return word.getFrequency();
	}

	public String getPronunciation_en() {
		return word.getPronunciation_en();
	}

	public String getPronunciation_am() {
		return word.getPronunciation_am();
	}
	//endregion
}
