package hamiguazzz.word;

import hamiguazzz.word.helper.WordBase;
import hamiguazzz.word.helper.WordExchange;
import hamiguazzz.word.helper.WordMeaning;
import hamiguazzz.word.helper.WordMeaningEn;

public class Word {

	WordBase word;
	WordExchange wordExchange;
	WordMeaning[] means;
	WordMeaningEn[] meaningEns;

	public Word(WordBase word, WordExchange wordExchange, WordMeaning[] means, WordMeaningEn[] meaningEns) {
		this.word = word;
		this.wordExchange = wordExchange;
		this.means = means;
		this.meaningEns = meaningEns;
	}


}
