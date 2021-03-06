package hamiguazzz.windows;

import hamiguazzz.windows.component.WordManagePaneController;
import hamiguazzz.windows.component.WordPaneController;
import hamiguazzz.windows.component.WordSelectorPaneController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public final class MainWindowController {

	@FXML
	private TabPane tabPane;
	@FXML
	private Tab tabSelect;
	@FXML
	private Tab tabCard;
	@FXML
	private Tab tabManage;
	private Parent tabSelectPane;
	private Parent tabCardPane;
	private WordPaneController wordPaneController;
	private WordSelectorPaneController wordSelectorPaneController;

	@FXML
	private void initialize() throws IOException {
		var loader = new FXMLLoader();
		loader.setLocation(WordSelectorPaneController.class.getResource("WordSelectorPane.fxml"));
		tabSelectPane = loader.load();
		wordSelectorPaneController = loader.getController();
		loader = new FXMLLoader();
		loader.setLocation(WordPaneController.class.getResource("WordPane.fxml"));
		tabCardPane = loader.load();
		wordPaneController = loader.getController();
		wordSelectorPaneController.setCallBackAfterConfirm(wordList -> {
			wordPaneController.setWordList(wordList);
			tabPane.getSelectionModel().select(tabCard);
		});
		tabSelect.setContent(tabSelectPane);
		tabCard.setContent(tabCardPane);

		Accordion managePane = FXMLLoader.load(WordManagePaneController.class.getResource("WordManagePane.fxml"));
		managePane.setExpandedPane(managePane.getPanes().get(0));
		tabManage.setContent(managePane);
	}

	@FXML
	private void clickCardTabHandle() {
		resizeWindow(500, 400);
		if (tabPane.getScene() != null)
			tabPane.getScene().getWindow().setOnCloseRequest(p -> wordPaneController.stop());
		tabCardPane.requestFocus();
	}

	@FXML
	private void clickSelectTabHandle() {
		resizeWindow(800, 600);
		if (tabSelectPane != null) {
			SplitPane node = (SplitPane) tabSelectPane.getChildrenUnmodifiable().get(1);
			node.setDividerPosition(0, 0.2);
			node.setDividerPosition(1, 0.8);
		}
	}

	@FXML
	private void clickManageTabHandle() {
		resizeWindow(700, 750);
	}

	private void resizeWindow(double width, double height) {
		if (tabPane.getScene() != null) {
			tabPane.getScene().getWindow().setWidth(width);
			tabPane.getScene().getWindow().setHeight(height);
		}
	}
}
