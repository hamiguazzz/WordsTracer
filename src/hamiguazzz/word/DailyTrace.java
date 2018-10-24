package hamiguazzz.word;

import hamiguazzz.database.annotation.DataColumn;
import hamiguazzz.database.annotation.DataColumnType;
import hamiguazzz.database.annotation.DataTable;
import hamiguazzz.database.annotation.EmptyConstructor;
import hamiguazzz.database.converter.DataConverterType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@DataTable(xmlPath = "./dataStructure/dailyTrace.xml", codeName = "dailyTrace", propertyPath = "" +
		"./dataStructure/LinkProperty.xml")
public class DailyTrace {

	@DataColumn(codeName = "id", type = DataColumnType.UNSIGNED_INT, key = true)
	private int id;
	@DataColumn(codeName = "count", type = DataColumnType.UNSIGNED_INT)
	private int count;
	@DataColumn(codeName = "timestamp", type = DataColumnType.DATETIME)
	private LocalDateTime timestamp;
	@DataColumn(codeName = "words", type = DataColumnType.VARCHAR, converter = DataConverterType.JSON)
	private ArrayList<String> words;

	private void calcID() {
		id = (timestamp.getYear() - 2000) * 100000000 + timestamp.getDayOfYear() * 100000 + timestamp.getHour() * 3600 +
				timestamp.getMinute() * 60 + timestamp.getSecond();
	}

	//region Bean Method
	@EmptyConstructor
	public DailyTrace() {
	}

	public DailyTrace(LocalDateTime timestamp, List<String> words) {
		this.count = words.size();
		this.timestamp = timestamp;
		this.words = new ArrayList<>(words);
		calcID();
	}

	public DailyTrace(List<String> words) {
		this(LocalDateTime.now(), words);
	}


	public int getId() {
		return id;
	}

	public int getCount() {
		return count;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public ArrayList<String> getWords() {
		return words;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DailyTrace that = (DailyTrace) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "DailyTrace{" +
				"id=" + id +
				", count=" + count +
				", timestamp=" + timestamp +
				", words=" + words +
				'}';
	}

	//endregion
}
