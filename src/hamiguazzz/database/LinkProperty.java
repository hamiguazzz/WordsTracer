package hamiguazzz.database;

import hamiguazzz.database.Exception.XMLException;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class LinkProperty {
	@NotNull
	public String driverName = "com.mysql.cj.jdbc.Driver";
	@NotNull
	public String linkUrl = "jdbc:mysql://localhost:3306/";
	@NotNull
	public String linkSetting = "?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false";
	@NotNull
	public String userName = "root";
	@NotNull
	public String userPassword = "";
	@NotNull
	public DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	@NotNull
	public DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public LinkProperty(@NotNull String userName, @NotNull String userPassword, @NotNull String linkUrl, @NotNull String linkSetting) {
		this.linkUrl = linkUrl;
		this.linkSetting = linkSetting;
		this.userName = userName;
		this.userPassword = userPassword;
	}

	public LinkProperty(@NotNull String userName, @NotNull String userPassword) {
		this.userName = userName;
		this.userPassword = userPassword;
	}

	public LinkProperty() {
	}

	@NotNull
	public static LinkProperty get(@NotNull String path) throws XMLException {
		return new PropertyXmlReader(path).getProperty();
	}

	@NotNull
	public static LinkProperty get(@NotNull File file) throws XMLException {
		return new PropertyXmlReader(file).getProperty();
	}

}

class PropertyXmlReader {

	private Document document;

	PropertyXmlReader(@NotNull File file) throws XMLException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			this.document = db.parse(file);
		} catch (@NotNull ParserConfigurationException | SAXException | IOException e) {
			throw new XMLException("can't read xml at " + file.getName(), e);
		}
	}

	PropertyXmlReader(@NotNull String path) throws XMLException {
		this(new File(path));
	}

	@NotNull LinkProperty getProperty() {
		var re = new LinkProperty();
		re.driverName = document.getElementsByTagName("driverName").item(0).getTextContent();
		re.linkUrl = document.getElementsByTagName("linkUrl").item(0).getTextContent();
		re.linkSetting = document.getElementsByTagName("linkSetting").item(0).getTextContent();
		re.userName = document.getElementsByTagName("userName").item(0).getTextContent();
		re.userPassword = document.getElementsByTagName("userPassword").item(0).getTextContent();
		re.dateTimeFormatter = DateTimeFormatter.ofPattern(document.getElementsByTagName("dateTimeFormatter").item(0)
				.getTextContent());
		re.dateFormatter = DateTimeFormatter.ofPattern(document.getElementsByTagName("dateFormatter").item(0).getTextContent());
		return re;
	}

}