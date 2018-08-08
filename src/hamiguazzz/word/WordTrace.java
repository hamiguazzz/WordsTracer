package hamiguazzz.word;

import hamiguazzz.database.annotation.*;
import hamiguazzz.database.converter.DataConverterType;
import hamiguazzz.database.converter.DataToString;
import hamiguazzz.database.converter.JsonConverter;
import hamiguazzz.database.converter.StringToData;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@DataTable(codeName = "trace", xmlPath = "./dataStructure/traces.xml")
public class WordTrace {

	//region beans
	@DataColumn(codeName = "word", key = true)
	private String wordName;
	@DataBean(keyField = "wordName")
	private Word wordEntity;
	@DataColumn(codeName = "progress", type = DataColumnType.UNSIGNED_INT)
	private int progress;
	@DataColumn(codeName = "easy", type = DataColumnType.UNSIGNED_INT)
	private int easy;
	@DataColumn(codeName = "forget", type = DataColumnType.UNSIGNED_INT)
	private int forget;
	@DataColumn(codeName = "forget", type = DataColumnType.UNSIGNED_INT, converter = DataConverterType.BIND_METHOD,
			customConverter = @ConvertMethod(dataToString = "tagsTo", stringToData = "tagsFrom"))
	private Set<String> tags;
	@DataColumn(codeName = "lastReadTime", type = DataColumnType.DATETIME)
	private LocalDateTime lastReadTime;
	@DataColumn(codeName = "firstReadTime", type = DataColumnType.DATETIME)
	private LocalDateTime firstReadTime;

	@Converter
	private static DataToString tagsTo = data -> {
		@SuppressWarnings("unchecked") Set<String> tags = (Set<String>) data;
		String[] objects = tags.toArray(new String[0]);
		return JsonConverter.to().dataToString(objects);
	};

	@Converter
	private static StringToData<Set<String>> tagsFrom = data -> {
		String[] objects = JsonConverter.from(String[].class).stringToData(data);
		return new HashSet<>(Arrays.asList(objects != null ? objects : new String[0]));
	};
	//endregion

	//region Construction
	public WordTrace(Word word, int progress, int easy, int forget, Set<String> tags, LocalDateTime last_read_time,
	                 LocalDateTime first_read_time) {
		this.wordEntity = word;
		this.progress = progress;
		this.easy = easy;
		this.forget = forget;
		this.tags = tags;
		this.lastReadTime = last_read_time;
		this.firstReadTime = first_read_time;
	}

	@EmptyConstructor
	public WordTrace() {
	}
	//endregion

	//region Generate Codes


	public String getWordName() {
		return wordName;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setEasy(int easy) {
		this.easy = easy;
	}

	public void setForget(int forget) {
		this.forget = forget;
	}

	public void updateLastRead() {
		if (firstReadTime == null) firstReadTime = LocalDateTime.now();
		lastReadTime = LocalDateTime.now();
	}

	public boolean addTag(String tag) {
		return tags.add(tag);
	}

	public Word getWordEntity() {
		return wordEntity;
	}

	public int getProgress() {
		return progress;
	}

	public int getEasy() {
		return easy;
	}

	public int getForget() {
		return forget;
	}

	public Set<String> getTags() {
		return tags;
	}

	public LocalDateTime getLastReadTime() {
		return lastReadTime;
	}

	public LocalDateTime getFirstReadTime() {
		return firstReadTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WordTrace wordTrace = (WordTrace) o;
		return Objects.equals(wordName, wordTrace.wordName);
	}

	@Override
	public int hashCode() {

		return Objects.hash(wordName);
	}

	@Override
	public String toString() {
		return "WordTrace{" +
				"wordEntity=" + wordEntity.getWord() +
				", progress=" + progress +
				", easy=" + easy +
				", forget=" + forget +
				", tags=" + tags +
				", lastReadTime=" + lastReadTime +
				", firstReadTime=" + firstReadTime +
				'}';
	}
	//endregion
}
