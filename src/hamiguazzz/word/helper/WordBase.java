package hamiguazzz.word.helper;

import hamiguazzz.database.annotation.DataColumn;
import hamiguazzz.database.annotation.DataColumnType;
import hamiguazzz.database.annotation.DataTable;
import hamiguazzz.database.annotation.EmptyConstructor;
import hamiguazzz.database.converter.DataConverterType;

import java.util.Arrays;

@DataTable(xmlPath = "./dataStructure/words.xml", codeName = "words", propertyPath = "./dataStructure/LinkProperty.xml")
public final class WordBase {
	@DataColumn(codeName = "word", key = true)
	String wordName;
	@DataColumn(codeName = "frequency", type = DataColumnType.UNSIGNED_INT)
	int frequency;
	@DataColumn(codeName = "simple_meaning")
	String simple_meaning;
	@DataColumn(codeName = "pronunciation_en")
	String pronunciation_en;
	@DataColumn(codeName = "pronunciation_am")
	String pronunciation_am;
	@DataColumn(codeName = "tags", converter = DataConverterType.JSON)
	String[] tags;

	@EmptyConstructor
	public WordBase() {
	}

	//region Generated Codes
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
	public String toString() {
		return "Base{" +
				"zh=" + simple_meaning +
				", frequency=" + frequency +
				", pronunciation_en=" + pronunciation_en +
				", pronunciation_am=" + pronunciation_am +
				", tags=" + (tags == null ? null : Arrays.asList(tags)) +
				'}';
	}
	//endregion
}
