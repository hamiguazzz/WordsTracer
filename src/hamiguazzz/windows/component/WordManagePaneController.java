package hamiguazzz.windows.component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;

import java.io.IOException;

public class WordManagePaneController {

	@FXML
	private TitledPane cacheManagePane;

	@FXML
	private TitledPane aboutPane;

	@FXML
	private TitledPane searchNetPane;

	@FXML
	private TitledPane databaseManagePane;

	@FXML
	private TitledPane settingPane;

	@FXML
	void initialize() throws IOException {
		assert cacheManagePane != null : "fx:id=\"cacheManagePane\" was not injected: check your FXML file 'WordManagePane.fxml'.";
		assert aboutPane != null : "fx:id=\"aboutPane\" was not injected: check your FXML file 'WordManagePane.fxml'.";
		assert searchNetPane != null : "fx:id=\"searchNetPane\" was not injected: check your FXML file 'WordManagePane.fxml'.";
		assert databaseManagePane != null : "fx:id=\"databaseManagePane\" was not injected: check your FXML file 'WordManagePane.fxml'.";
		assert settingPane != null : "fx:id=\"settingPane\" was not injected: check your FXML file 'WordManagePane.fxml'.";

		Parent searchNetContentPane = FXMLLoader.load(SearchFromNetPaneController.class.getResource("SearchFromNetPane.fxml"));
		searchNetPane.setContent(searchNetContentPane);
		Parent databaseManageContentPane = FXMLLoader.load(DatabaseManagePaneController.class.getResource
				("DatabaseManagePane.fxml"));
		databaseManagePane.setContent(databaseManageContentPane);
	}
}
