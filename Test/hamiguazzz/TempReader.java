package hamiguazzz;

import hamiguazzz.database.Exception.FieldBindException;
import hamiguazzz.database.Exception.NoConverterException;
import hamiguazzz.database.Exception.NoEmptyConstructorException;
import hamiguazzz.database.Exception.XMLException;
import hamiguazzz.database.LinkProperty;
import hamiguazzz.word.Word;
import hamiguazzz.word.helper.WordBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * read words from `waiting`.`word`,which are waiting to be built from network
 */
public class TempReader {

	private static final Logger logger = LogManager.getLogger(TempReader.class.getName());
	//match suitable words
	private static final String reg = "^[A-Z]??[a-z|A-Z .\\-]*?[a-z]$";

	public static void main(String[] args) {
	}

	/**
	 * running the getting method,
	 * and press 'q' to quit
	 * 'i' to get index
	 */
	private static void run() throws Exception {
		Thread thread = gets();
		thread.start();
		while (true) {
			int read = System.in.read();
			switch (read) {
				case 'q':
					running = false;
					thread.join();
					return;
				case 'i':
					logger.info("index=" + i);
					break;
			}
		}
	}

	/**
	 * clean wrong added word by moving their caches to ./wrong/ and run this method
	 */
	private static void cleanWrong() throws NoConverterException, XMLException, FieldBindException, NoEmptyConstructorException {
		File path = new File("./wrong/");
		File[] files = path.listFiles();
		WordBuilder builder = new WordBuilder();
		assert files != null;
		for (File file : files) {
			String[] split = file.getName().split("\\.");
			String wrongName = split[0];
			if (builder.deleteByKey(wrongName)) {
				logger.info("delete " + wrongName);
			} else {
				logger.error("failed " + wrongName);
			}
		}
	}

	private static volatile boolean running = true;

	private static volatile int i = 0;

	private static Thread gets() throws Exception {
		List<String> waiting = getWaiting();
		WordBuilder wordBuilder = new WordBuilder();
		List<String> collect = waiting.stream()
				.filter(word -> word.matches(reg))
				.filter(word -> !wordBuilder.isExistByKey(word))
				.distinct()
				.collect(Collectors.toList());
		logger.info("all count=" + collect.size());

		return new Thread(() -> {
			i = 0;
			List<String> failed = new ArrayList<>();
			for (String word : collect) {
				if (!running) return;
				//noinspection NonAtomicOperationOnVolatileField
				i++;
				logger.trace("building index=" + i + ",word=" + word);
				Word wordEntity = null;
				try {
					wordEntity = WordBuilder.buildWordFromNet(word);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (wordEntity == null || wordEntity.getSimple_meaning() == null || wordEntity.getSimple_meaning().equals
						("")) {
					failed.add(word);
					logger.error("failed build " + word + ",index=" + i);
					continue;
				}
				wordBuilder.write(wordEntity);
				logger.trace("built index=" + i + ",word=" + word);
				try {
					Thread.sleep((long) (Math.random() * 50));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.info("loaded " + collect.size());
			logger.info("failed " + failed.size());
		});
	}

	private static List<String> getWaiting() throws Exception {
		Connection createdConnection = getCon();
		ArrayList<String> words = new ArrayList<>();
		ResultSet resultSet;
		//noinspection SqlDialectInspection
		try (PreparedStatement preparedStatement = createdConnection.prepareStatement("SELECT * FROM `waiting`")) {
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				words.add(resultSet.getString("word"));
			}
		}
		createdConnection.close();
		return words;
	}

	private static Connection getCon() throws SQLException, ClassNotFoundException, XMLException {
		LinkProperty property = LinkProperty.get("./dataStructure/LinkProperty.xml");
		Class.forName(property.driverName);
		return DriverManager.getConnection(property.linkUrl + "wordlist" + property.linkSetting,
				property.userName, property.userPassword);
	}

}