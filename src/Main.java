import hamiguazzz.utils.StopWatch;
import hamiguazzz.word.helper.WordBuilder;

import java.sql.SQLException;

public class Main {
	public static void main(String[] args) throws SQLException {
		//System.out.println(WordBuilder.buildWordFromNet("link"));
//		File file = new File("./src/hamiguazzz/word/wordlist.xml");
//		try {
//			SQLXMLReader reader = new SQLXMLReader(file);
//			System.out.println(reader.getColumnsNameMap("words"));
//		} catch (ParserConfigurationException | SAXException e) {
//			e.printStackTrace();
//		}
//		for (int i=1;i<10000; i++) {
//			WordBuilder.isExist(String.valueOf(i));
//		}var timestamp =
		StopWatch watch = new StopWatch("updating use");
		watch.start();
		WordBuilder.updateALLWordsFromNet(WordBuilder.getWordList());
		watch.stop();
		System.out.println(watch);
	}
}
