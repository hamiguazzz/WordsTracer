package hamiguazzz.database;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLXMLReader {

	private Document document;

	//region Constructor
	public SQLXMLReader(File file) throws ParserConfigurationException, SAXException, IOException {
		this.document = getDocument(file);
	}

	public SQLXMLReader(String file) throws IOException, SAXException, ParserConfigurationException {
		this(new File(file));
	}
	//endregion

	public String getDatabaseName() {
		return document.getElementsByTagName("database").item(0).getAttributes().getNamedItem("name").getNodeValue();
	}

	public String[] getTableNames() {
		var tables = document.getElementsByTagName("table");
		List<String> ls = new ArrayList<>();
		for (int i = 0; i < tables.getLength(); i++) {
			ls.add(tables.item(i).getAttributes().getNamedItem("name").getNodeValue());
		}
		return ls.toArray(new String[0]);
	}

	public Map<String, String> getColumnsNameMap(String tableName) {
		var tables = document.getElementsByTagName("table");
		Node table = null;
		for (int i = 0; i < tables.getLength(); i++) {
			if (tables.item(i).getAttributes().getNamedItem("name").getNodeValue().equals(tableName)) {
				table = tables.item(i);
				break;
			}
		}
		if (table == null) throw new NullPointerException(tableName + "is not found!");
		HashMap<String, String> map = new HashMap<>();
		var nl = table.getChildNodes();

		for (int i = 0; i < nl.getLength(); i++) {
			var atts = nl.item(i).getAttributes();
			if (atts != null) {
				var name = atts.getNamedItem("name").getNodeValue();
				var cnn = atts.getNamedItem("codename");
				var codename = cnn == null ? name : cnn.getNodeValue();
				map.put(codename, name);
			}
		}
		return map;
	}

	public Document getDocument() {
		return document;
	}

	//region Utils
	private static Document getDocument(File file) throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(file);
	}
	//endregion
}
