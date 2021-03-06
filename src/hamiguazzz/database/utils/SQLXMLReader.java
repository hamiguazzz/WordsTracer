package hamiguazzz.database.utils;

import hamiguazzz.database.Exception.NoTableFoundException;
import hamiguazzz.database.Exception.XMLException;
import hamiguazzz.database.annotation.DataColumnType;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class SQLXMLReader {

	private Document document;

	//region Constructor
	public SQLXMLReader(@NotNull File file) throws XMLException {
		try {
			this.document = getDocument(file);
		} catch (@NotNull IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			throw new XMLException("can't read xml at " + file.getName(), e);
		}
	}

	public SQLXMLReader(@NotNull String file) throws XMLException {
		this(new File(file));
	}
	//endregion

	public String getDatabaseName() {
		return document.getElementsByTagName("database").item(0).getAttributes().getNamedItem("name").getNodeValue();
	}

	@NotNull
	public String getTableName(@NotNull String codeTableName) {
		var tables = document.getElementsByTagName("table");
		for (int i = 0; i < tables.getLength(); i++) {
			if (tables.item(i).getAttributes().getNamedItem("codename").getNodeValue().equals(codeTableName))
				return tables.item(i).getAttributes().getNamedItem("name").getNodeValue();
		}
		throw new NoTableFoundException();
	}

	//codename=name
	@NotNull
	public Map<String, String> getColumnsNameMap(String codeTableName) {
		var table = getTableNode(codeTableName);
		HashMap<String, String> map = new HashMap<>();
		var columnNodeList = table.getChildNodes();

		for (int i = 0; i < columnNodeList.getLength(); i++) {
			var attributes = columnNodeList.item(i).getAttributes();
			if (attributes != null) {
				var name = attributes.getNamedItem("name").getNodeValue();
				var cnn = attributes.getNamedItem("codename").getNodeValue();
				map.put(cnn, name);
			}
		}
		return map;
	}

	//codename=type
	@NotNull
	public Map<String, DataColumnType> getColumnsTypeMap(String codeTableName) {
		var table = getTableNode(codeTableName);
		HashMap<String, DataColumnType> map = new HashMap<>();
		var columnNodeList = table.getChildNodes();
		for (int i = 0; i < columnNodeList.getLength(); i++) {
			var attributes = columnNodeList.item(i).getAttributes();
			if (attributes != null) {
				var typeName = attributes.getNamedItem("type").getNodeValue();
				var cnn = attributes.getNamedItem("codename").getNodeValue();
				map.put(cnn, DataColumnType.get(typeName));
			}
		}
		return map;
	}

	//region Utils
	protected Document getDocument() {
		return document;
	}

	private static Document getDocument(@NotNull File file) throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(file);
	}

	@NotNull
	private Node getTableNode(String codeTableName) {
		var tables = document.getElementsByTagName("table");
		Node table = null;
		for (int i = 0; i < tables.getLength(); i++) {
			if (tables.item(i).getAttributes().getNamedItem("codename").getNodeValue().equals(codeTableName)) {
				table = tables.item(i);
				break;
			}
		}
		if (table == null) throw new NoTableFoundException(codeTableName + "is not found!");
		return table;
	}

	//endregion
}
