package hamiguazzz.word;

import hamiguazzz.database.annotation.*;
import hamiguazzz.database.converter.DataConverterType;
import hamiguazzz.database.converter.DataToString;
import hamiguazzz.database.converter.StringToData;
import hamiguazzz.word.helper.WordBase;
import hamiguazzz.word.helper.WordExchange;
import hamiguazzz.word.helper.WordMeaning;
import hamiguazzz.word.helper.WordMeaningEn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

@DataTable(xmlPath = "./dataStructure/words.xml", codeName = "words")
public class Word {
	@DataColumn(codeName = "wordEntity", key = true)
	private String wordName;
	@DataBean(keyField = "wordName", write = true)
	private WordBase word;
	@DataColumn(codeName = "exchanges", converter = DataConverterType.JSON)
	private WordExchange wordExchange;
	@DataColumn(codeName = "means", converter = DataConverterType.JSON)
	private WordMeaning[] means;
	@DataColumn(codeName = "meaningEns", converter = DataConverterType.JSON)
	private WordMeaningEn[] meaningEns;
	@DataColumn(codeName = "lastUpdateTime", converter = DataConverterType.BIND_METHOD,
			customConverter = @ConvertMethod(stringToData = "lastUpdateTimeFrom", dataToString = "lastUpdateTimeTo"))
	private LocalDateTime lastUpdateTime;

	@NotNull
	private static DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@NotNull
	@Converter
	private static DataToString lastUpdateTimeTo = data -> DataToString.standardDataTimeConverter.dataToString
			(LocalDateTime.now());
	@NotNull
	@Converter
	private static StringToData<LocalDateTime> lastUpdateTimeFrom = data -> LocalDateTime.parse(data, formatter);

	public Word(String wordName, WordBase word, WordExchange wordExchange, WordMeaning[] means, WordMeaningEn[] meaningEns) {
		this.wordName = wordName;
		this.word = word;
		this.wordExchange = wordExchange;
		this.means = means;
		this.meaningEns = meaningEns;
	}

	@EmptyConstructor
	public Word() {
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
	public boolean equals(@Nullable Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Word word = (Word) o;
		return Objects.equals(wordName, word.wordName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(wordName);
	}

	@NotNull
	@Override
	public String toString() {
		return "hamiguazzz.wordEntity.Word{" +
				wordName + "," +
				word + "," +
				wordExchange +
				", meanings_zh=" + (means == null ? null : Arrays.asList(means)) +
				", meanings_en=" + (meaningEns == null ? null : Arrays.asList(meaningEns)) +
				'}';
	}
	//endregion

	public String getWord() {
		return wordName;
	}
	//region From WordBase

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
