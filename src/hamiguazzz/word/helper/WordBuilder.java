package hamiguazzz.word.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.swjtu.lang.Lang;
import com.swjtu.querier.Querier;
import hamiguazzz.database.SQLXMLReader;
import hamiguazzz.utils.StopWatch;
import hamiguazzz.word.Word;
import hamiguazzz.word.trans.WordTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@SuppressWarnings({"WeakerAccess", "SqlDialectInspection"})
public final class WordBuilder {

	private static Logger logger = LogManager.getLogger(WordBuilder.class.getName());

	//region Hardcode
	private static final String NetCacheDic = "./Temp/";

	private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

	private static final String DATABASE_URL =
			"jdbc:mysql://localhost:3306/wordlist" +
					"?useUnicode=true&characterEncoding=utf-8" +
					"&serverTimezone=GMT%2B8&useSSL=false";

	private static final String USER_NAME = "testuser";

	private static final String USER_PASSWORD = "123456";

	private static final String DATA_XML_PATH = "./src/hamiguazzz/word/wordlist.xml";

	private static final String TABLE_NAME = "words";
	//endregion

	//region Build Utils
	public static Word buildWordFromNet(String word) throws IOException {
		//region Get
		String cache = cacheWord(word);
		logger.trace("building word " + word + " from cache...");
		ObjectMapper mapper = new ObjectMapper();
		var root = mapper.readTree(cache);
		//endregion

		//region Base
		WordBase base = new WordBase();
		base.word = word;
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
				em.examples = toArray(g.path("example").toString());
				em.similar_words = toArray(g.path("similar_word").toString());
				mle.add(em);
			}
		}
		WordMeaningEn[] means_en = mle.toArray(new WordMeaningEn[0]);
		//endregion

		logger.trace("succeed in building word " + word + " from network...");
		return new Word(base, exchange, means, means_en);
	}

	public static Word buildWordFromSQL(String word) {
		logger.trace("building " + word + " from sql");
		var con = getCon();
		try (var ste = con.prepareStatement("SELECT * FROM `words` WHERE word='" + word + "'")) {
			ResultSet resultSet = ste.executeQuery();
			return dataToWord(resultSet);
		} catch (SQLException e) {
			logger.error("can't build " + word + " from sql!");
			e.printStackTrace();
			return null;
		}
	}
	//endregion

	//region SQL Utils
	public static void insertOrUpdate(Word word) {
		boolean exist = isExist(word.getWord());
		String sql;
		if (!exist) {
			sql = insert(word);
		} else {
			sql = update(word);
		}
		try (PreparedStatement p = con.prepareStatement(sql)) {
			p.execute();
		} catch (SQLException e) {
			logger.error("sql error when insert/update " + word);
			e.printStackTrace();
		}
		logger.trace(word.getWord() + " to sql successfully!");
	}

	public static boolean isExist(String word) {
		Connection con = getCon();
		String builder = String.format("SELECT %s from %s where %s='%s'", getMap().get("word"), TABLE_NAME, getMap().get("word"), word);
		try (var p = con.prepareStatement(builder)) {
			ResultSet resultSet = p.executeQuery();
			return resultSet.next();
		} catch (SQLException e) {
			logger.error("sql error when judging if " + word + " exists");
			e.printStackTrace();
		}
		return false;
	}

	public static boolean deleteWord(String word) {
		var con = getCon();
		try (PreparedStatement statement = con.prepareStatement("DELETE FROM words WHERE word='" + word + "'")) {
			return statement.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("can't delete " + word + " from sql");
			e.printStackTrace();
			return false;
		}
	}

	public static List<String> getWordList() {
		ArrayList<String> all_words = new ArrayList<>();
		try (PreparedStatement statement = getCon().prepareStatement("SELECT word FROM words")) {
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

	//region Update Utils
	public final static int UPDATE_MAX_THREAD = 8;
	public final static long UPDATE_SLEEP_TIME = 1000;

	public static void updateALLWordsFromNet(List<String> words) {
		in_updateALLWordsFromNet(words, new ArrayList<>());
	}

	private static void in_updateALLWordsFromNet(List<String> words, ArrayList<Integer> tried) {
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
				insertOrUpdate(obj_word);
				logger.trace("deal " + word + " successfully!");
				successful.add(word);
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

	public static List<String> getAllEmptyString() {
		ArrayList<String> all_words = new ArrayList<>();
		try (PreparedStatement statement = getCon().prepareStatement("SELECT word FROM words WHERE simple_meaning=''")) {
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

	public static List<String> getNotCachedWords() {
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

	private static File[] getAllCaches() {
		File file = new File(NetCacheDic);
		return file.listFiles();
	}

	public static String cacheWord(String word) {
		String cache = readDataFromCache(word);
		if (cache != null) return cache;
		logger.trace("cache " + word + " from network...");
		String s = writeToCache(word, getDataFromNet(word));
		logger.trace("succeed in getting " + word + " from network...");
		return s;
	}

	public static List<String> cacheAll(List<String> words) {
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

	public static void cacheNotCachedWords() {
		StopWatch watch = new StopWatch("caching use");
		watch.start();
		WordBuilder.deleteEmptyCache();
		List<String> notCachedWords = WordBuilder.getNotCachedWords();
		logger.info("will cache " + notCachedWords.size() + " words");
		List<String> list = WordBuilder.cacheAll(notCachedWords);
		watch.stop();
		logger.info("\nfailed count=" + list.size() +
				"\nsuccess count=" + (notCachedWords.size() - list.size()) +
				"\n" + watch);
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

	//region Multi Thread Utils
	private static <T> List<Thread> poolToThreads(List<Deque<T>> pool, String poolName, long sleepTime, Consumer<T>
			target) {
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < pool.size(); i++) {
			int finalI = i;
			threads.add(new Thread(() -> {
				pool.get(finalI).forEach(target);
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}, poolName + "id = " + i));
		}
		return threads;
	}

	private static <T> List<Deque<T>> balancePool(List<T> objs, int threadsCount) {
		ArrayList<Deque<T>> pool = new ArrayList<>(threadsCount);
		for (int i = 0; i < threadsCount; i++) {
			pool.add(new ArrayDeque<>());
		}
		for (int i = 0; i < objs.size(); i += threadsCount) {
			for (int j = 0; j < threadsCount; j++) {
				if (i + j < objs.size())
					pool.get(j).add(objs.get(i + j));
			}
		}
		return pool;
	}

	private static void joinAll(List<Thread> threads) {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error(thread.getName() + " can't join");
				e.printStackTrace();
			}
		}
	}
	//endregion

	//region Database Utils

	private static String insert(Word word) {
		logger.trace("inserting " + word.getWord());
		var sb = new StringBuilder();
		var t1 = new StringBuilder();
		var t2 = new StringBuilder();
		var map = getMap();
		for (var e : map.entrySet()) {
			t1.append(e.getValue()).append(",");
			t2.append("'").append(wordToData(word, e.getKey())).append("'").append(",");
		}
		t1.setLength(t1.length() - 1);
		t2.setLength(t2.length() - 1);
		sb.append("INSERT INTO ").append(TABLE_NAME)
				.append(" (").append(t1).append(")")
				.append(" VALUES ")
				.append("(").append(t2).append(")");
		return sb.toString();
	}

	private static String update(Word word) {
		var sb = new StringBuilder();
		logger.trace("updating " + word.getWord());
		sb.append("UPDATE ").append(TABLE_NAME).append(" SET ");
		var m = getMap();
		var mv = m.entrySet();
		for (var e : mv) {
			sb.append(e.getValue()).append("=").append("'").append(wordToData(word, e.getKey())).append("'").append
					(",");
		}
		sb.setLength(sb.length() - 1);
		sb.append(" WHERE ").append(m.get("word")).append("=").append("'").append(word.getWord()).append("'");
		return sb.toString();
	}

	private static String wordToData(Word word, String tab) {
		switch (tab) {
			case "word":
				return word.getWord();
			case "simple_meaning":
				return word.getSimple_meaning();
			case "frequency":
				return String.valueOf(word.getFrequency());
			case "pronunciation_en":
				var spe = word.getPronunciation_en();
				return spe == null ? null : spe.replace("'", "''");
			case "pronunciation_am":
				var spa = word.getPronunciation_am();
				return spa == null ? null : spa.replace("'", "''");
			case "meanings_zh":
				return convertObjToJson(word.getMeaningsZH());
			case "meanings_en":
				String s = convertObjToJson(word.getMeaningsEN());
				return s == null ? null : s.replace("'", "''");
			case "exchanges":
				return convertObjToJson(word.getWordExchange());
			case "tags":
				return convertObjToJson(word.getTags());
			case "relative":
				return null;
			case "last_update_time":
				return now();
		}
		return null;
	}

	private static Word dataToWord(ResultSet resultSet) throws SQLException {
		if (!resultSet.next()) return null;
		WordBase base = new WordBase();
		base.word = resultSet.getString("word");
		base.frequency = resultSet.getInt("frequency");
		base.pronunciation_am = resultSet.getString("pronunciation_am");
		base.pronunciation_en = resultSet.getString("pronunciation_en");
		base.simple_meaning = resultSet.getString("simple_meaning");
		base.tags = convertJsonToObj(resultSet.getString("tags"), String[].class);
		WordMeaning[] wordMeanings = convertJsonToObj(resultSet.getString("meanings_zh"), WordMeaning[].class);
		WordMeaningEn[] wordMeaningEns = convertJsonToObj(resultSet.getString("meanings_en"), WordMeaningEn[].class);
		WordExchange exchange = convertJsonToObj(resultSet.getString("exchanges"), WordExchange.class);
		return new Word(base, exchange, wordMeanings, wordMeaningEns);
	}

	private static String now() {
		return Timestamp.valueOf(LocalDateTime.now()).toString().substring(0, 19);
	}

	private static <T> String convertObjToJson(T object) {
		var mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("can't convert" + object.getClass().getName() + "to json");
			e.printStackTrace();
		}
		return null;
	}

	private static <T> T convertJsonToObj(String json, Class<T> type) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, type);
		} catch (IOException e) {
			logger.error("can't convert json to obj type=" + type.getName());
			e.printStackTrace();
		}
		return null;
	}

	private static Connection con = null;

	private static Connection getCon() {
		// FIXME: 2018/7/29 HARDCORE
		if (con != null) return con;
		else {
			try {
				Class.forName(DRIVER_NAME); //classLoader,加载对应驱动
				con = DriverManager.getConnection(DATABASE_URL, USER_NAME, USER_PASSWORD);
			} catch (ClassNotFoundException | SQLException e) {
				logger.error("sql connection error!");
				e.printStackTrace();
			}
		}
		return con;
	}

	private static Map<String, String> nameMap = null;

	private static Map<String, String> getMap() {
		if (nameMap != null) return nameMap;
		else {
			try {
				var reader = new SQLXMLReader(DATA_XML_PATH);
				nameMap = reader.getColumnsNameMap(reader.getTableNames()[0]);
			} catch (IOException | SAXException | ParserConfigurationException e) {
				logger.error("read xml error");
				e.printStackTrace();
			}
		}
		return nameMap;
	}
	//endregion

	//region Network query
	private static String getDataFromNet(String word) {
		logger.trace("getting word " + word + " from network...");
		Querier<WordTranslator> querierTrans = new Querier<>();
		querierTrans.setParams(Lang.EN, Lang.ZH, word);
		querierTrans.attach(new WordTranslator());
		return querierTrans.execute().get(0);
	}
	//endregion

	//region Network Utils
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