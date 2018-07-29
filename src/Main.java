import hamiguazzz.database.SQLXMLReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		//System.out.println(WordBuilder.buildWordFromNet("link"));
		File file = new File("./src/hamiguazzz/word/wordlist.xml");
		try {
			SQLXMLReader reader = new SQLXMLReader(file);
			System.out.println(reader.getColumnsNameMap("words"));
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}
}
