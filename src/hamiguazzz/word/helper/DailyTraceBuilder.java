package hamiguazzz.word.helper;

import hamiguazzz.database.Exception.FieldBindException;
import hamiguazzz.database.Exception.NoConverterException;
import hamiguazzz.database.Exception.NoEmptyConstructorException;
import hamiguazzz.database.Exception.XMLException;
import hamiguazzz.database.utils.DataColumnHelper;
import hamiguazzz.word.DailyTrace;
import hamiguazzz.word.WordTrace;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DailyTraceBuilder {

	private DataColumnHelper<DailyTrace> helper;

	public DailyTraceBuilder() throws NoConverterException, XMLException, FieldBindException, NoEmptyConstructorException {
		helper = new DataColumnHelper<>(DailyTrace.class);
	}

	@NotNull
	public DailyTrace build(@NotNull String... words) {
		List<String> collect = Arrays.stream(words).distinct().collect(Collectors.toList());
		return new DailyTrace(collect);
	}

	@NotNull
	public DailyTrace build(@NotNull WordTrace... words) {
		List<String> collect = Arrays.stream(words).map(WordTrace::getWordName).distinct().collect(Collectors.toList());
		return new DailyTrace(collect);
	}

	public boolean write(@NotNull DailyTrace obj, Connection... connection) {
		return helper.write(obj, connection);
	}

	@NotNull
	public List<DailyTrace> readAll(@NotNull LocalDate date) {
		ArrayList<DailyTrace> list = new ArrayList<>();
		var ids = searchAll(date);
		ids.forEach(id -> list.add(helper.read(id)));
		return list;
	}

	public boolean deleteAll(@NotNull LocalDate date) {
		List<Integer> list = searchAll(date);
		boolean re = true;
		for (Integer id : list) {
			if (!helper.deleteByKey(id)) {
				re = false;
				break;
			}
		}
		return re;
	}

	private List<Integer> searchAll(@NotNull LocalDate date) {
		int from = (date.getYear() - 2000) * 100000000 + date.getDayOfYear() * 100000;
		int to = from + 100000;
		return searchAll().stream().filter(id -> id <= to && id >= from).collect(Collectors.toList());
	}

	@NotNull
	private List<Integer> searchAll() {
		return helper.searchAll();
	}

	public boolean delete(@NotNull DailyTrace obj) {
		return helper.delete(obj);
	}

	public int calcAllCount(@NotNull LocalDate date) {
		return (int) readAll(date).stream().flatMap(trace -> trace.getWords().stream()).distinct().count();
	}

	public List<String> getAllStudiedWords(@NotNull LocalDate date) {
		return readAll(date).stream().flatMap(trace -> trace.getWords().stream()).distinct().collect(Collectors.toList());
	}

	public void close() {
		helper.close();
	}
}
