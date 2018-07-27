package hamiguazzz.word.helper;

public interface SQLData {
	String updateSQL();

	String insertSQL();

	String dropSQL();

	void searchFromSQL(String key);
}
