package hamiguazzz.database;

import java.time.format.DateTimeFormatter;

public class LinkProperty {
	public String driverName = "com.mysql.cj.jdbc.Driver";
	public String linkurl = "jdbc:mysql://localhost:3306/";
	public String linkSetting = "?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false";
	public String userName = "testuser";
	public String userPassword = "123456";
	public DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
