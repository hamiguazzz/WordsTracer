package hamiguazzz.word.helper;

import hamiguazzz.database.Exception.FieldBindException;
import hamiguazzz.database.Exception.NoConverterException;
import hamiguazzz.database.Exception.NoEmptyConstructorException;
import hamiguazzz.database.Exception.XMLException;
import hamiguazzz.database.LinkProperty;
import hamiguazzz.database.utils.DataColumnHelper;
import hamiguazzz.utils.ThreadsPoolUtils;
import hamiguazzz.word.Word;
import hamiguazzz.word.WordTrace;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class WordTraceBuilder extends DataColumnHelper<WordTrace> {
	private static Logger logger = LogManager.getLogger(WordTraceBuilder.class.getName());

	//region Constructor
	public WordTraceBuilder(@NotNull LinkProperty property, @NotNull Class<? extends WordTrace> helpClass) throws XMLException, NoEmptyConstructorException, FieldBindException, NoConverterException {
		super(property, helpClass);
	}

	public WordTraceBuilder(@NotNull Class<? extends WordTrace> helpClass) throws XMLException, NoEmptyConstructorException, FieldBindException, NoConverterException {
		super(helpClass);
	}

	public WordTraceBuilder() throws NoConverterException, XMLException, FieldBindException, NoEmptyConstructorException {
		super(WordTrace.class);
	}
	//endregion

	@NotNull
	public WordTrace build(@NotNull String word, @NotNull WordBuilder builder) {
		return buildBySpecificConnection(word, builder, null);
	}

	@NotNull
	private WordTrace buildBySpecificConnection(@NotNull String word, @NotNull WordBuilder builder, @Nullable
			Connection connection) {
		WordTrace readWordTrace = read(word, connection);
		if (readWordTrace != null) {
			return readWordTrace;
		} else {
			Word readWord = builder.read(word, connection);
			if (readWord != null) return new WordTrace(readWord);
			try {
				@NotNull Word netWord = WordBuilder.buildWordFromNet(word);
				if (builder.write(netWord, connection))
					return new WordTrace(netWord);
				else {
					WordBuilder.deleteCache(word);
					throw new NullPointerException(word + " from net faced error");
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new NullPointerException(word + " from net faced IO error");
			}
		}
	}

	private final static int THREAD_COUNT = 4;

	public Pair<Map<String, WordTrace>, List<Thread>> buildAllToMap(@NotNull List<String> words, @NotNull WordBuilder builder) {
		if (words.size() == 0) return new Pair<>(new HashMap<>(), new ArrayList<>());
		var pool = ThreadsPoolUtils.balancePool(words, THREAD_COUNT);
		var re = new HashMap<String, WordTrace>(words.size());
		var threads = new ArrayList<Thread>(pool.size());
		for (int i = 0; i < pool.size(); i++) {
			int finalI = i;
			threads.add(new Thread(() -> {
				try (Connection con = createCon()) {
					pool.get(finalI).forEach(word -> re.put(word, buildBySpecificConnection(word, builder, con)));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}, "trace build " + "id = " + i));
		}
		threads.forEach(Thread::start);
		return new Pair<>(re, threads);
	}

	public Deque<WordTrace> buildAllToDeque(@NotNull List<String> words, @NotNull WordBuilder builder) {
		var pool = ThreadsPoolUtils.balancePool(words, THREAD_COUNT);
		var re = new ArrayDeque<WordTrace>(words.size());
		var threads = new ArrayList<Thread>(pool.size());
		for (int i = 0; i < pool.size(); i++) {
			int finalI = i;
			threads.add(new Thread(() -> {
				try (Connection con = createCon()) {
					pool.get(finalI).forEach(word -> re.add(buildBySpecificConnection(word, builder, con)));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}, "trace build " + "id = " + i));
		}
		threads.forEach(Thread::start);
		return re;
	}
}
