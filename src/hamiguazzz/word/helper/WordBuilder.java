package hamiguazzz.word.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.swjtu.lang.Lang;
import com.swjtu.querier.Querier;
import hamiguazzz.database.Exception.FieldBindException;
import hamiguazzz.database.Exception.NoConverterException;
import hamiguazzz.database.Exception.NoEmptyConstructorException;
import hamiguazzz.database.Exception.XMLException;
import hamiguazzz.database.LinkProperty;
import hamiguazzz.database.utils.DataColumnHelper;
import hamiguazzz.utils.StopWatch;
import hamiguazzz.word.Word;
import hamiguazzz.word.trans.WordTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static hamiguazzz.utils.ThreadsPoolUtils.*;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public final class WordBuilder extends DataColumnHelper<Word> {

	private static final String NetCacheDic = "./Temp/";
	private static Logger logger = LogManager.getLogger(WordBuilder.class.getName());

	//region Constructor
	public WordBuilder(@NotNull LinkProperty property, @NotNull Class<? extends Word> helpClass)
			throws XMLException, NoEmptyConstructorException, FieldBindException, NoConverterException {
		super(property, helpClass);
	}

	public WordBuilder(@NotNull Class<? extends Word> helpClass)
			throws XMLException, NoEmptyConstructorException, FieldBindException, NoConverterException {
		super(helpClass);
	}

	public WordBuilder() throws NoConverterException, XMLException, FieldBindException, NoEmptyConstructorException {
		super(Word.class);
	}
	//endregion

	//region Build Utils
	@NotNull
	public static Word buildWordFromNet(@NotNull String word) throws IOException {
		//region Get
		String cache = cacheWord(word);
		logger.trace("building word " + word + " from cache...");
		ObjectMapper mapper = new ObjectMapper();
		var root = mapper.readTree(cache);
		//endregion

		//region Base
		WordBase base = new WordBase();
		base.wordName = word;
		base.simple_meaning = root.path("trans_result").findPath("dst").asText();
		base.frequency = root.path("dict_result").path("collins").path("frequence").asInt(0);
		base.pronunciation_am = root.path("dict_result").path("simple_means").findPath("symbols").findPath("ph_am").asText();
		base.pronunciation_en = root.path("dict_result").path("simple_means").findPath("symbols").findPath("ph_en").asText();
		var col_temp = root.path("dict_result").path("simple_means").path("tags");
		var col = col_temp.getNodeType() == JsonNodeType.MISSING ? null : col_temp.withArray("core");
		if (col != null) {
			var list = new ArrayList<String>(col.size());
			col.forEach(e -> list.add(e.asText()));
			base.tags = list.toArray(new String[0]);
		} else {
			base.tags = null;
		}
		//endregion

		//region Exchanges
		WordExchange exchange = new WordExchange();
		var ex = root.path("dict_result").path("simple_means").path("exchange");
		exchange.word_done = toArray(ex.path("word_done").toString());
		exchange.word_pl = toArray(ex.path("word_pl").toString());
		exchange.word_er = toArray(ex.path("word_er").toString());
		exchange.word_est = toArray(ex.path("word_est").toString());
		exchange.word_ing = toArray(ex.path("word_ing").toString());
		exchange.word_past = toArray(ex.path("word_past").toString());
		exchange.word_third = toArray(ex.path("word_third").toString());
		//endregion

		//region Means
		var par = root.path("dict_result").path("simple_means").path("symbols").findPath("parts");
		var ml = new ArrayList<WordMeaning>();
		par.forEach(e -> {
			var m = new WordMeaning();
			m.part = e.path("part").asText();
			if (e.path("means").findPath("means").getNodeType() != JsonNodeType.MISSING)
				m.means = toArray(e.path("means").findPath("means").toString());
			else
				m.means = toArray(e.path("means").toString());
			ml.add(m);
		});
		WordMeaning[] means = ml.toArray(new WordMeaning[0]);
		//endregion

		//region Means_en
		var grs = root.path("dict_result").path("edict").findPath("item");
		var mle = new ArrayList<WordMeaningEn>();
		for (var gr : grs) {
			var ps = gr.path("pos").asText();
			var gg = gr.findPath("tr_group");
			for (var g : gg) {
				var em = new WordMeaningEn();
				em.part = ps;
				em.means = toArray(g.path("tr").toString());
				em.examples = toArray(g.path("example").toString().replace("\\\"", ""));
				em.similar_words = toArray(g.path("similar_word").toString());
				mle.add(em);
			}
		}
		WordMeaningEn[] means_en = mle.toArray(new WordMeaningEn[0]);
		//endregion

		logger.trace("succeed in building word " + word + " from network...");
		return new Word(base.wordName, base, exchange, means, means_en);
	}

	@Nullable
	public Word buildWordFromSQL(@NotNull String word) {
		logger.trace("building " + word + " from sql");
		Word read = read(word);
		logger.trace("succeed in building word " + word + " from sql...");
		return read;
	}
	//endregion

	//region SQL Utils
	public @NotNull List<String> getWordList() {
		//noinspection unchecked
		return searchAll();
	}

	public @NotNull List<String> getWordList(@NotNull String tag) {
		String sb = String.format("SELECT `%s` FROM `%s` WHERE `%s` LIKE  '%%%s%%'",
				nameMap.get("word"), tableName, nameMap.get("tags"), tag);
		var con = getCon();
		var list = new ArrayList<String>();
		try (PreparedStatement statement = con.prepareStatement(sb)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) list.add(resultSet.getString(nameMap.get("word")));
		} catch (SQLException e) {
			logger.error("can't get words tags contains " + tag);
			e.printStackTrace();
		}
		return list;
	}

	public LocalDateTime getLastUpdateTime(String word) {
		String st = String.format("SELECT `%s` FROM `%s` WHERE `%s`='%s'", nameMap.get("lastUpdateTime"),
				tableName, nameMap.get("word"), word);
		var con = getCon();
		try (PreparedStatement statement = con.prepareStatement(st)) {
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) return null;
			var lastUpdateTime = resultSet.getString(nameMap.get("lastUpdateTime"));
			return LocalDateTime.parse(lastUpdateTime, property.dateTimeFormatter);
		} catch (SQLException e) {
			logger.error("can't get update_time of " + word);
			e.printStackTrace();
		}
		return null;
	}
	//endregion

	//region Update Utils
	public final static int UPDATE_MAX_THREAD = 8;
	public final static long UPDATE_SLEEP_TIME = 1000;

	public void updateALLWordsFromNet(List<String> words) {
		in_updateALLWordsFromNet(words, new ArrayList<>());
	}

	private void in_updateALLWordsFromNet(List<String> words, ArrayList<Integer> tried) {
		//fixme too many requests will be limited
		if (words.size() == 0) {
			logger.info("all words is updated");
			return;
		}
		List<String> left_words = new ArrayList<>(words);
		List<Deque<String>> pool;
		List<Thread> threads;
		ArrayList<String> successful = new ArrayList<>();

		logger.info("left = " + left_words.size());
		pool = balancePool(left_words, UPDATE_MAX_THREAD);
		threads = poolToThreads(pool, "pool depth=" + tried.size(), UPDATE_SLEEP_TIME, (word) -> {
			try {
				logger.trace(word + " is possessing...");
				var obj_word = buildWordFromNet(word);
				if (write(obj_word)) {
					logger.trace("deal " + word + " successfully!");
					successful.add(word);
				} else {
					deleteCache(word);
				}
			} catch (Exception e) {
				logger.error("deal " + word + " failed!");
				e.printStackTrace();
			}
		});
		threads.forEach(Thread::start);
		threads.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error(thread.getName() + " can't join");
				e.printStackTrace();
			}
		});
		left_words.removeAll(successful);
		left_words.addAll(getAllEmptyString());
		tried.add(words.size() - left_words.size());
		logger.info("success = " + (words.size() - left_words.size()));
		int depth = tried.size();
		if (depth > 4) {
			if (tried.get(depth - 1) + tried.get(depth - 2) + tried.get(depth - 3) == 0) {
				logger.error("can't update " + left_words.size() + " words,they are\n" + left_words + "\n");
			} else in_updateALLWordsFromNet(left_words, tried);
		} else in_updateALLWordsFromNet(left_words, tried);
	}

	public List<String> getAllEmptyString() {
		ArrayList<String> all_words = new ArrayList<>();
		@SuppressWarnings("SqlDialectInspection")
		String sql = String.format("SELECT `%s` FROM `%s` WHERE `%s`=''",
				nameMap.get("word"), tableName, nameMap.get("simple_meaning"));
		try (PreparedStatement statement = getCon().prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			if (resultSet != null) {
				while (resultSet.next()) {
					all_words.add(resultSet.getString(1));
				}
			}
		} catch (SQLException e) {
			logger.error("fetch words list failed!");
			e.printStackTrace();
		}
		return all_words;
	}
	//endregion

	//region Words's caches Utils
	private static final int DELETE_SLEEP_TIME = 10;
	private static final int DELETE_MAX_THREADS = 16;
	private static final String ERROR_STRING = "{\"error\"";

	public static boolean deleteCache(String word) {
		File file = new File(getCacheFileName(word));
		return file.delete();
	}

	public static int deleteEmptyCache() {
		AtomicInteger re = new AtomicInteger(0);
		var files = getAllCaches();
		if (files == null || files.length == 0) return 0;
		var pool = balancePool(Arrays.asList(files), DELETE_MAX_THREADS);
		var threads = poolToThreads(pool, "cleaner", DELETE_SLEEP_TIME, (f -> {
			boolean isNeedDeleted = false;
			try (var is = new FileInputStream(f)) {
				if (new String(is.readAllBytes()).startsWith(ERROR_STRING)) {
					isNeedDeleted = true;
				}
			} catch (IOException e) {
				logger.error("can't read " + f.getName() + "'s cache!");
				e.printStackTrace();
			}
			if (isNeedDeleted) {
				if (f.delete()) {
					logger.trace("empty " + f.getName() + " cache is deleted");
					re.incrementAndGet();
				} else {
					logger.error("can't delete empty " + f.getName() + " cache");
				}
			}
		}));
		threads.forEach(Thread::start);
		joinAll(threads);
		logger.info(re.intValue() + " caches is deleted.");
		return re.intValue();
	}

	public List<String> getNotCachedWords() {
		List<String> allWords = getWordList();
		var caches = getAllCaches();
		Map<String, String> allWordsCache = new HashMap<>();
		allWords.forEach(e -> allWordsCache.put(e + ".cache", e));
		List<String> re = new ArrayList<>();
		for (var cache : caches) {
			if (allWordsCache.containsKey(cache.getName()))
				re.add(allWordsCache.get(cache.getName()));
		}
		allWords.removeAll(re);
		return allWords;
	}

	public static String cacheWord(String word) {
		String cache = readDataFromCache(word);
		if (cache != null) return cache;
		logger.trace("cache " + word + " from network...");
		String s = writeToCache(word, getDataFromNet(word));
		logger.trace("succeed in getting " + word + " from network...");
		return s;
	}

	public List<String> cacheAll(List<String> words) {
		List<String> failures = new Vector<>();
		var pool = balancePool(words, UPDATE_MAX_THREAD);
		var threads = poolToThreads(pool, "updateCache", UPDATE_SLEEP_TIME, word -> {
			String cache = cacheWord(word);
			if (cache.startsWith(ERROR_STRING)) {
				failures.add(word);
				logger.error("cache " + word + " error!");
			}
		});
		threads.forEach(Thread::start);
		joinAll(threads);
		return failures;
	}

	public void cacheNotCachedWords() {
		StopWatch watch = new StopWatch("caching use");
		watch.start();
		WordBuilder.deleteEmptyCache();
		List<String> notCachedWords = getNotCachedWords();
		logger.info("will cache " + notCachedWords.size() + " words");
		List<String> list = cacheAll(notCachedWords);
		watch.stop();
		logger.info("\nfailed count=" + list.size() +
				"\nsuccess count=" + (notCachedWords.size() - list.size()) +
				"\n" + watch);
	}

	private static File[] getAllCaches() {
		File file = new File(NetCacheDic);
		return file.listFiles();
	}

	private static String getCacheFileName(String word) {
		return NetCacheDic + word + ".cache";
	}

	private static String readDataFromCache(String word) {
		try (FileInputStream stream = new FileInputStream(getCacheFileName(word))) {
			return new String(stream.readAllBytes());
		} catch (IOException e) {
			return null;
		}
	}

	private static String writeToCache(String word, String dataFromNet) {
		File file = new File(getCacheFileName(word));
		if (!file.exists()) {
			try {
				//noinspection ResultOfMethodCallIgnored
				file.createNewFile();
				try (FileOutputStream stream = new FileOutputStream(file)) {
					stream.write(dataFromNet.getBytes());
				}
			} catch (IOException e) {
				logger.error("cache " + word + " failed!");
				e.printStackTrace();
			}
		}
		return dataFromNet;
	}
	//endregion

	//region Network Utils
	private static String getDataFromNet(String word) {
		logger.trace("getting word " + word + " from network...");
		Querier<WordTranslator> queriedTrans = new Querier<>();
		queriedTrans.setParams(Lang.EN, Lang.ZH, word);
		queriedTrans.attach(new WordTranslator());
		return queriedTrans.execute().get(0);
	}

	private static String[] toArray(String s) {
		if (s == null || s.equals("") || s.equals("\"\"")) {
			return new String[]{};
		}
		var re = s.split(",");
		if (re.length <= 1) {
			return new String[]{deal(s, 2, 2)};
		}
		re[0] = deal(re[0], 2, 1);
		for (int i = 1; i < re.length - 1; i++) {
			re[i] = deal(re[i], 1, 1);
		}
		re[re.length - 1] = deal(re[re.length - 1], 1, 2);
		return re;
	}

	private static String deal(String s, int left, int right) {
		if (s == null || s.equals("") || s.length() < left + right) return s;
		return new String(s.toCharArray(), left, s.length() - left - right);
	}
	//endregion

}