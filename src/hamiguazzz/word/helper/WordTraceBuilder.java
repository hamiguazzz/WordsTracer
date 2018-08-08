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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		if (this.isExistByKey(word)) {
			return read(word);
		} else {
			if (builder.isExistByKey(word)) {
				return new WordTrace(builder.read(word));
			} else {
				try {
					@NotNull Word netWord = WordBuilder.buildWordFromNet(word);
					if (builder.write(netWord))
						return new WordTrace(netWord);
					else {
						WordBuilder.deleteCache(word);
						throw new NullPointerException(word + " from net faced error");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				throw new NullPointerException(word + " from net faced IO error");
			}
		}
	}

	private final static int THREAD_COUNT = 4;
	private final static long THREAD_SLEEP = 20;

	public Pair<Map<String, WordTrace>, List<Thread>> buildAll(@NotNull List<String> words, @NotNull WordBuilder builder) {
		var pool = ThreadsPoolUtils.balancePool(words, 4);
		var re = new HashMap<String, WordTrace>(words.size());
		var threads = ThreadsPoolUtils.poolToThreads(pool, "trace build", THREAD_SLEEP,
				word -> re.put(word, build(word, builder)));
		threads.forEach(Thread::start);
		return new Pair<>(re, threads);
	}

}
