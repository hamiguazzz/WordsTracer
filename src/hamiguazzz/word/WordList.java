package hamiguazzz.word;

import hamiguazzz.database.annotation.DataColumn;
import hamiguazzz.database.annotation.DataColumnType;
import hamiguazzz.database.annotation.DataTable;
import hamiguazzz.database.annotation.EmptyConstructor;
import hamiguazzz.database.converter.DataConverterType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"WeakerAccess", "unused"})
@DataTable(xmlPath = "./dataStructure/wordLists.xml", codeName = "wordList", propertyPath = "./dataStructure/LinkProperty.xml")
public class WordList {

	@DataColumn(codeName = "listName", type = DataColumnType.VARCHAR, key = true)
	private String listName;
	@DataColumn(codeName = "createTime", type = DataColumnType.DATETIME)
	private LocalDateTime createTime;
	@DataColumn(codeName = "lastReadTime", type = DataColumnType.DATETIME)
	private LocalDateTime lastReadTime;
	@DataColumn(codeName = "wordsArrayList", type = DataColumnType.VARCHAR, converter = DataConverterType.JSON)
	private ArrayList<String> wordsArrayList;

	private static final LocalDateTime OLDEST_TIME_TAG = LocalDateTime.of(1900, 1, 1, 0, 0, 0);

	//region Constructor
	@EmptyConstructor
	public WordList() {
	}

	public WordList(String listName, List<String> words) {
		this(listName, new ArrayList<>(words), LocalDateTime.now(), OLDEST_TIME_TAG);
	}

	public WordList(String listName, ArrayList<String> wordsArrayList, LocalDateTime createTime, LocalDateTime
			lastReadTime) {
		this.listName = listName;
		this.createTime = createTime;
		this.lastReadTime = lastReadTime;
		this.wordsArrayList = wordsArrayList;
	}
	//endregion

	//region Generated Codes
	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public LocalDateTime getLastReadTime() {
		return lastReadTime;
	}

	public void setLastReadTime(LocalDateTime lastReadTime) {
		this.lastReadTime = lastReadTime;
	}

	public List<String> getWordsArrayList() {
		return wordsArrayList;
	}

	public void setWordsArrayList(ArrayList<String> wordsArrayList) {
		this.wordsArrayList = wordsArrayList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WordList wordList = (WordList) o;
		return Objects.equals(listName, wordList.listName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(listName);
	}

	@Override
	public String toString() {
		return "WordList{" +
				"listName=" + listName +
				", createTime=" + createTime +
				", lastReadTime=" + lastReadTime +
				", wordsArrayList=" + wordsArrayList +
				'}';
	}
	//endregion
}
