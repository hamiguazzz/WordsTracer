package hamiguazzz.windows;

import hamiguazzz.database.Exception.FieldBindException;
import hamiguazzz.database.Exception.NoConverterException;
import hamiguazzz.database.Exception.NoEmptyConstructorException;
import hamiguazzz.database.Exception.XMLException;
import hamiguazzz.database.utils.DataColumnHelper;
import hamiguazzz.word.WordList;
import hamiguazzz.word.WordTrace;
import hamiguazzz.word.helper.WordBuilder;
import hamiguazzz.word.helper.WordTraceBuilder;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class SharedObjects {

	private static WordTraceBuilder wordTraceBuilder;
	private static WordBuilder wordBuilder;
	private static Map<String, WordTrace> wordTraceMap = new HashMap<>();
	private static DataColumnHelper<WordList> wordListHelper;

	@NotNull
	public static DataColumnHelper<WordList> getWordListHelper() {
		if (wordListHelper == null) {
			try {
				wordListHelper = new DataColumnHelper<>(WordList.class);
			} catch (XMLException | NoEmptyConstructorException | NoConverterException | FieldBindException e) {
				e.printStackTrace();
			}
		}
		return wordListHelper;
	}

	@NotNull
	public static WordBuilder getWordBuilder() {
		if (wordBuilder == null) {
			try {
				wordBuilder = new WordBuilder();
			} catch (NoConverterException | XMLException | NoEmptyConstructorException | FieldBindException e) {
				e.printStackTrace();
			}
		}
		return wordBuilder;
	}

	@NotNull
	public static WordTraceBuilder getWordTraceBuilder() {
		if (wordTraceBuilder == null) {
			try {
				wordTraceBuilder = new WordTraceBuilder();
			} catch (NoConverterException | XMLException | NoEmptyConstructorException | FieldBindException e) {
				e.printStackTrace();
			}
		}
		return wordTraceBuilder;
	}

	@NotNull
	public static Map<String, WordTrace> getWordTraceMap() {
		return wordTraceMap;
	}

	@NotNull
	public static WordTrace getTrace(@NotNull final String word) {
		if (getWordTraceMap().containsKey(word))
			return wordTraceMap.get(word);
		else {
			WordTrace trace = getWordTraceBuilder().build(word, getWordBuilder());
			wordTraceMap.put(word, trace);
			return trace;
		}
	}

	@NotNull
	public static Map<String, WordTrace> getAll(@NotNull final List<String> word) {
		List<String> collect = word.parallelStream().filter(trace -> !wordTraceMap.containsKey(trace)).collect(Collectors.toList());
		if (collect.size() > 0) {
			Pair<Map<String, WordTrace>, List<Thread>> mapListPair = getWordTraceBuilder().buildAllToMap(collect, getWordBuilder());
			mapListPair.getValue().forEach(e -> {
				try {
					e.join();
				} catch (InterruptedException err) {
					err.printStackTrace();
				}
			});
			wordTraceMap.putAll(mapListPair.getKey());
		}
		var re = new HashMap<String, WordTrace>(word.size());
		word.forEach(ele -> re.put(ele, wordTraceMap.get(ele)));
		return re;
	}

	public static List<WordTrace> getWordStartWith(@NotNull final String find) {
		preLoad();
		if (find.equals("*")) return new ArrayList<>(wordTraceMap.values());
		else if (find.endsWith("*") && find.startsWith("*")) {
			final String mid = find.substring(find.indexOf('*') + 1, find.lastIndexOf('*'));
			return wordTraceMap.values().parallelStream().filter(word -> word.getWordName().contains(mid)).collect
					(Collectors.toList());
		} else if (find.endsWith("*")) {
			final String start = find.substring(0, find.indexOf('*'));
			return wordTraceMap.values().parallelStream().filter(word -> word.getWordName().startsWith(start)).collect
					(Collectors.toList());
		} else if (find.startsWith("*")) {
			final String end = find.substring(find.lastIndexOf('*') + 1);
			return wordTraceMap.values().parallelStream().filter(word -> word.getWordName().endsWith(end)).collect
					(Collectors.toList());
		}
		return wordTraceMap.values().parallelStream().filter(word -> word.getWordName().equals(find)).collect
				(Collectors.toList());
	}

	public static List<WordTrace> getWordHasTag(@NotNull final String tag, final boolean useCustom) {
		preLoad();
		if (!useCustom) {
			return wordTraceMap.values().parallelStream().filter(word -> {
				String[] tags = word.getWordEntity().getTags();
				if (tags != null) {
					for (String s : tags) {
						if (tag.equals(s)) return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
		} else {
			return wordTraceMap.values().parallelStream().filter(word -> {
				String[] tags = word.getWordEntity().getTags();
				Set<String> tags2 = word.getTags();
				if (tags2 != null && tags2.contains(tag)) return true;
				if (tags != null) {
					for (String s : tags) {
						if (tag.equals(s)) return true;
					}
				}
				return false;
			}).collect(Collectors.toList());
		}
	}

	public static void preLoad() {
		List<String> wordList = getWordBuilder().getWordList();
		Map<String, WordTrace> traceMap = getWordTraceMap();
		List<String> leftList = wordList.parallelStream()
				.filter(word -> !traceMap.containsKey(word))
				.collect(Collectors.toList());
		if (leftList.size() == 0) return;
		Pair<Map<String, WordTrace>, List<Thread>> mapListPair = getWordTraceBuilder().buildAllToMap(leftList,
				getWordBuilder());
		mapListPair.getValue().forEach(e -> {
			try {
				e.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		});
		traceMap.putAll(mapListPair.getKey());
	}
}
