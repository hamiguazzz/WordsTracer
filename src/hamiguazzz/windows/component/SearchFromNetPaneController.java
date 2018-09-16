package hamiguazzz.windows.component;

import hamiguazzz.windows.SharedObjects;
import hamiguazzz.word.Word;
import hamiguazzz.word.WordTrace;
import hamiguazzz.word.helper.WordBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFromNetPaneController {
	//match suitable words
	private static final String reg = "^[A-Z]??[a-z|A-Z .\\-]*?[a-z]$";

	//region FXMLNodeAndInitialize
	@FXML
	private TextField addWordTextField;

	@FXML
	private ListView<String> waitingAddListView;

	@FXML
	private TableView<Word> searchedTableView;

	@FXML
	private TableColumn<Word, String> wordNameColumn;

	@FXML
	private TableColumn<Word, String> simpleMeanColumn;

	@FXML
	private void initialize() {
		assert simpleMeanColumn != null : "fx:id=\"simpleMeanColumn\" was not injected: check your FXML file 'SearchFromNetPane.fxml'.";
		assert searchedTableView != null : "fx:id=\"searchedTableView\" was not injected: check your FXML file 'SearchFromNetPane.fxml'.";
		assert addWordTextField != null : "fx:id=\"addWordTextField\" was not injected: check your FXML file 'SearchFromNetPane.fxml'.";
		assert waitingAddListView != null : "fx:id=\"waitingAddListView\" was not injected: check your FXML file 'SearchFromNetPane.fxml'.";
		assert wordNameColumn != null : "fx:id=\"wordNameColumn\" was not injected: check your FXML file 'SearchFromNetPane.fxml'.";

		addWordTextField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER) {
				if (keyEvent.isShiftDown()) {
					searchWaitingListHandle();
					if (addWordTextField.getText().equals("")) return;
				}
				addWordHandle();
				addWordTextField.clear();
			}
		});

		waitingAddListView.setOnKeyPressed(keyEvent -> {
			switch (keyEvent.getCode()) {
				case ENTER:
					searchFromNetHandle();
					break;
				case DELETE:
					waitingWordRemoveHandle();
					break;
				case I:
					importWaitingListHandle();
					break;
				case E:
					exportWaitingListHandle();
					break;
			}
		});

		searchedTableView.setOnKeyPressed(keyEvent -> {
			switch (keyEvent.getCode()) {
				case ENTER:
					saveToDatabaseHandel();
					break;
				case DELETE:
					if (keyEvent.isShiftDown()) {
						removeCacheHandle();
					} else {
						removeHandle();
					}
					break;
				case F5:
					refreshHandle();
					break;
			}
		});

		wordNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWord()));
		simpleMeanColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSimple_meaning()));

		searchedTableView.setRowFactory(param -> {
			TableRow<Word> tableRow = new TableRow<>();
			tableRow.setOnMouseClicked(event -> {
				if (event.getClickCount() >= 2) {
					Word selectedItem = searchedTableView.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						Stage stage = DetailsPaneController.createDetailsStage(new WordTrace(selectedItem));
						if (stage != null) {
							stage.show();
						}
					}
				}
			});
			return tableRow;
		});

		waitingAddListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		searchedTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	//endregion

	//region WaitingList
	@FXML
	private void addWordHandle() {
		String word = this.addWordTextField.getText();
		if (word.matches(reg)) {
			if (!waitingAddListView.getItems().contains(word))
				waitingAddListView.getItems().add(word);
		} else
			new Alert(Alert.AlertType.WARNING, "不匹配的单词拼写", ButtonType.OK).show();
	}

	@FXML
	private void searchFromNetHandle() {
		Platform.runLater(() -> {
			Collection<String> collect = waitingAddListView.getSelectionModel().getSelectedItems().stream().distinct().collect(Collectors.toList());
			for (String s : collect) {
				final Word word = searchSpecificWord(s);
				if (word == null) return;
				if (searchedTableView.getItems().contains(word))
					searchedTableView.getItems().stream().filter(w -> w.getWord().equals(s)).findAny().ifPresent(e ->
							searchedTableView.getItems().remove(e));
				searchedTableView.getItems().add(searchSpecificWord(s));
				try {
					Thread.sleep(getSleepTime(collect.size()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private Word searchSpecificWord(@NotNull String word) {
		try {
			return WordBuilder.buildWordFromNet(word);
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "网络异常", ButtonType.OK).show();
			e.printStackTrace();
		}
		return null;
	}

	@FXML
	private void waitingWordRemoveHandle() {
		waitingAddListView.getItems().removeAll(waitingAddListView.getSelectionModel().getSelectedItems());
	}

	@FXML
	private void importWaitingListHandle() {
		var fileChooser = new FileChooser();
		fileChooser.setTitle("选择要导入的单词表");
		fileChooser.setInitialDirectory(new File("./"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("单词列表", "*.txt", "*.wordList"));
		File file = fileChooser.showOpenDialog(waitingAddListView.getScene().getWindow());
		if (file == null) return;
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {
				Collection<String> collect = bufferedReader.lines().filter(e -> e.matches(reg)).distinct().collect(Collectors.toList());
				waitingAddListView.getItems().addAll(collect);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void clearWaitingListHandle() {
		waitingAddListView.getItems().clear();
	}

	@FXML
	private void exportWaitingListHandle() {
		var fileChooser = new FileChooser();
		fileChooser.setTitle("选择要导出的单词表");
		fileChooser.setInitialDirectory(new File("./"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("文本", "*.txt"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("单词表 ", "*.wordList"));
		File file = fileChooser.showSaveDialog(waitingAddListView.getScene().getWindow());
		if (file == null) return;
		try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream))) {
				for (String s : this.waitingAddListView.getItems()) {
					bufferedWriter.write(s);
					bufferedWriter.newLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void searchWaitingListHandle() {
		final Thread thread = new Thread(() -> {
			final List<String> wordNames = new ArrayList<>();
			final Collection<String> collect = searchedTableView.getItems().stream().map(Word::getWord).collect(Collectors
					.toList());
			for (String s : this.waitingAddListView.getItems()) {
				if (!collect.contains(s))
					wordNames.add(s);
			}
			final int sleepTime = getSleepTime(wordNames.size());
			for (String s : wordNames) {
				System.out.println(s);
				if (s == null || s.equals("") || s.equals("null") || !s.matches(reg)) return;
				final Word word = searchSpecificWord(s);
				if (word == null) return;

				if (searchedTableView.getItems().contains(word)) {
					searchedTableView.getItems().stream().filter(w -> w.getWord().equals(s)).findAny()
							.ifPresent(e -> searchedTableView.getItems().remove(e));
				}
				searchedTableView.getItems().add(word);

				try {
					Thread.sleep(getSleepTime(sleepTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "searchListFromNet");
		thread.setDaemon(true);
		thread.start();
	}

	private int getSleepTime(int listSize) {
		if (listSize < 3) return 0;
		else if (listSize < 10) return 20;
		else if (listSize < 100) return 30;
		else if (listSize < 500) return 50;
		else return 60;
	}
	//endregion

	//region TempTable
	@FXML
	private void saveToDatabaseHandel() {
		saveSomeToDatabase(searchedTableView.getSelectionModel().getSelectedItems());
	}

	private void saveSomeToDatabase(Collection<Word> collection) {
		int success = 0;
		for (Word word : collection) {
			if (SharedObjects.getWordBuilder().write(word))
				success++;
		}
		new Alert(Alert.AlertType.INFORMATION,
				String.format("导入数据库，成功%d个，失败%d个", success, collection.size() - success))
				.show();
	}

	@FXML
	private void refreshHandle() {
		ObservableList<Word> selectedItems = searchedTableView.getSelectionModel().getSelectedItems();
		refreshSome(selectedItems);
	}

	private void refreshSome(Collection<Word> collection) {
		Thread thread = new Thread(() -> Platform.runLater(() -> {
			var words = new ArrayList<>(collection);
			int success = 0;
			for (var word : words) {
				searchedTableView.getItems().remove(word);
				var t = searchSpecificWord(word.getWord());
				if (t != null) {
					searchedTableView.getItems().add(t);
					success++;
				}
				try {
					Thread.sleep(getSleepTime(words.size()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			new Alert(Alert.AlertType.INFORMATION, String.format("刷新成功%d个,失败%d个", success, words.size() - success), ButtonType.OK)
					.show();
		}), "refreshThread");
		thread.setDaemon(true);
		thread.start();
	}

	@FXML
	private void removeHandle() {
		searchedTableView.getItems().removeAll(searchedTableView.getSelectionModel().getSelectedItems());
	}

	@FXML
	private void removeCacheHandle() {
		removeSomeCache(searchedTableView.getSelectionModel().getSelectedItems());
	}

	private void removeSomeCache(Collection<Word> collection) {
		int i = 0;
		for (Word word : collection) {
			if (WordBuilder.deleteCache(word.getWord()))
				i++;
		}
		new Alert(Alert.AlertType.INFORMATION, String.format("删除缓存，成功%d个，失败%d个", i, collection.size() - i)).show();
	}

	@FXML
	private void writeAllHandle() {
		var words = new ArrayList<>(searchedTableView.getItems());
		saveSomeToDatabase(words);
	}

	@FXML
	private void clearAllHandle() {
		searchedTableView.getItems().clear();
	}

	@FXML
	private void refreshAllHandle() {
		refreshSome(new ArrayList<>(searchedTableView.getItems()));
	}

	@FXML
	private void clearAllCacheHandle() {
		var words = new ArrayList<>(searchedTableView.getItems());
		removeSomeCache(words);
	}
	//endregion
}