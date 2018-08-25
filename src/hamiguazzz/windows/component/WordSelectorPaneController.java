package hamiguazzz.windows.component;

import hamiguazzz.word.WordList;
import hamiguazzz.word.WordTrace;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class WordSelectorPaneController {

	//region FXMLNodeAndInitialize
	@FXML
	private TextField wordSearchField;
	@FXML
	private TextField tagSearchField;
	@FXML
	private Button wordSearchButton;
	@FXML
	private Button tagSearchButton;
	@FXML
	private CheckBox useCustomCheck;
	@FXML
	private ToggleButton addWordMode;
	@FXML
	private ChoiceBox<Integer> wordsAddCountChoice;
	@FXML
	private ChoiceBox<String> columnControlChoice;
	@FXML
	private TableView<WordTrace> wordsTable;
	@FXML
	private ListView<WordTrace> wordListView;
	@FXML
	private TextField wordListNameField;

	@NotNull
	private Map<ColumnContentType, TableColumn<WordTrace, ?>> columnMap = new HashMap<>();

	@FXML
	private void initialize() {
		//region assert notnull
		assert wordListView != null : "fx:id=\"wordListView\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert wordsAddCountChoice != null : "fx:id=\"wordsAddCountChoice\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert wordSearchButton != null : "fx:id=\"wordSearchButton\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert tagSearchButton != null : "fx:id=\"tagSearchButton\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert addWordMode != null : "fx:id=\"addWordMode\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert useCustomCheck != null : "fx:id=\"useCustomCheck\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert tagSearchField != null : "fx:id=\"tagSearchField\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert columnControlChoice != null : "fx:id=\"columnControlChoice\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert wordsTable != null : "fx:id=\"wordsTable\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert wordListNameField != null : "fx:id=\"wordListNameField\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		assert wordSearchField != null : "fx:id=\"wordSearchField\" was not injected: check your FXML file 'WordSelectorPane.fxml'.";
		//endregion
		//region search
		wordSearchField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
				searchByWordHandle();
		});
		tagSearchField.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
				searchByTagHandle();
		});
		var searchImage = new Image(String.valueOf(getClass().getResource("/search.png")));
		wordSearchButton.setGraphic(new ImageView(searchImage));
		tagSearchButton.setGraphic(new ImageView(searchImage));
		wordSearchButton.setOnAction(p -> this.searchByWordHandle());
		tagSearchButton.setOnAction(p -> this.searchByTagHandle());
		//endregion

		wordListView.setCellFactory((ListView<WordTrace> l) -> new ListCell<>() {
			@Override
			protected void updateItem(WordTrace item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null && !empty) {
					this.setText(item.getWordName());
					setOnMouseClicked(value -> {
						if (value.getClickCount() > 1) {
							List<WordTrace> collect = wordsTable.getItems().parallelStream().filter(w -> w.equals(item))
									.limit(1).collect(Collectors.toList());
							if (collect.size() == 0) {
								wordsTable.getItems().add(item);
								collect.add(item);
							}
							wordsTable.getSelectionModel().select(collect.get(0));
							wordsTable.scrollTo(collect.get(0));
						}
					});
				} else if (empty) {
					//if not add this branch,data will be duplicated.
					setText(null);
					setGraphic(null);
				}
			}
		});

		for (ColumnContentType type : ColumnContentType.values()) {
			columnMap.put(type, null);
		}

		columnAddFun(ColumnContentType.word);
		columnAddFun(ColumnContentType.meaning);
		columnAddFun(ColumnContentType.progress);

		wordsAddCountChoice.setItems(FXCollections.observableList(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
		columnControlChoice.setItems(FXCollections.observableList(ColumnContentType.getValues()));

		wordsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		wordListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	//endregion

	//region SearchButtonHandleGroup
	private void searchByWordHandle() {
		String value = wordSearchField.getText();
		if (value == null || value.equals("")) return;
		List<WordTrace> wordStartWith = SharedObjects.getWordStartWith(value);
		if (isAddingMode()) {
			wordStartWith.addAll(wordsTable.getItems());
			wordsTable.setItems(FXCollections.observableList(
					wordStartWith.parallelStream().distinct().collect(Collectors.toList())));
		} else {
			wordsTable.setItems(FXCollections.observableList(wordStartWith));
		}
	}

	private void searchByTagHandle() {
		String value = tagSearchField.getText();
		if (value == null || value.equals("")) return;
		List<WordTrace> hasTagsWord = SharedObjects.getWordHasTag(value, useCustomCheck.isSelected());
		if (isAddingMode()) {
			hasTagsWord.addAll(wordsTable.getItems());
			wordsTable.setItems(FXCollections.observableList(
					hasTagsWord.parallelStream().distinct().collect(Collectors.toList())));
		} else {
			wordsTable.setItems(FXCollections.observableList(hasTagsWord));
		}
	}

	private boolean isAddingMode() {
		return addWordMode.isSelected();
	}
	//endregion

	//region ColumnControlHandleGroup
	@FXML
	private void columnAddHandle() {
		var value = columnControlChoice.getValue();
		columnAddFun(ColumnContentType.nameMap.get(value));
	}

	@FXML
	private void columnDeleteHandle() {
		var value = ColumnContentType.nameMap.get(columnControlChoice.getValue());
		var tableColumn = columnMap.get(value);
		if (tableColumn != null) {
			columnMap.replace(value, null);
			wordsTable.getColumns().remove(tableColumn);
		}
	}

	@SuppressWarnings("unchecked")
	private void columnAddFun(@NotNull ColumnContentType type) {
		TableColumn tableColumn = columnMap.get(type);
		if (tableColumn == null) {
			tableColumn = new TableColumn<>(type.name);
			var factory = ColumnContentType.bindFactory(type);
			tableColumn.setCellValueFactory(factory);
			columnMap.replace(type, tableColumn);
			wordsTable.getColumns().add(tableColumn);
		}
	}
	//endregion

	//region AddToListButtonHandleGroup
	private int getSelectSize() {
		return wordsAddCountChoice.getValue() != null ? wordsAddCountChoice.getValue() : 0;
	}

	@FXML
	private void addByRandomHandle() {
		if (getSelectSize() == 0) return;
		ArrayList<WordTrace> wordTraces = new ArrayList<>(wordsTable.getItems());
		wordTraces.removeAll(wordListView.getItems());
		Collections.shuffle(wordTraces);
		List<WordTrace> collect = wordTraces.stream().limit(getSelectSize()).collect(Collectors.toList());
		wordListView.getItems().addAll(collect);
	}

	@FXML
	private void addByMaxForgetHandle() {
		if (getSelectSize() == 0) return;
		ArrayList<WordTrace> wordTraces = new ArrayList<>(wordsTable.getItems());
		wordTraces.removeAll(wordListView.getItems());
		Collections.shuffle(wordTraces);
		List<WordTrace> collect = wordTraces.stream()
				.sorted(Comparator.comparingInt(WordTrace::getForget).reversed())
				.limit(getSelectSize()).collect(Collectors.toList());
		wordListView.getItems().addAll(collect);
	}

	@FXML
	private void addByMaxIntervalTimeHandle() {
		if (getSelectSize() == 0) return;
		ArrayList<WordTrace> wordTraces = new ArrayList<>(wordsTable.getItems());
		wordTraces.removeAll(wordListView.getItems());
		Collections.shuffle(wordTraces);
		List<WordTrace> collect = wordTraces.stream()
				.sorted((o1, o2) ->
				{
					if (o1.getLastReadTime().isBefore(o2.getLastReadTime()))
						return 1;
					if (o1 == o2) return 0;
					else return -1;
				})
				.limit(getSelectSize()).collect(Collectors.toList());
		wordListView.getItems().addAll(collect);
	}

	@FXML
	private void addByMinProgressHandle() {
		if (getSelectSize() == 0) return;
		ArrayList<WordTrace> wordTraces = new ArrayList<>(wordsTable.getItems());
		wordTraces.removeAll(wordListView.getItems());
		Collections.shuffle(wordTraces);
		List<WordTrace> collect = wordTraces.stream()
				.sorted(Comparator.comparingInt(WordTrace::getProgress))
				.limit(getSelectSize()).collect(Collectors.toList());
		wordListView.getItems().addAll(collect);
	}

	@FXML
	private void addByMaxFrequencyHandle() {
		if (getSelectSize() == 0) return;
		ArrayList<WordTrace> wordTraces = new ArrayList<>(wordsTable.getItems());
		wordTraces.removeAll(wordListView.getItems());
		Collections.shuffle(wordTraces);
		List<WordTrace> collect = wordTraces.stream()
				.sorted(((o1, o2) -> o2.getWordEntity().getFrequency() - o1.getWordEntity().getFrequency()))
				.limit(getSelectSize()).collect(Collectors.toList());
		wordListView.getItems().addAll(collect);
	}
	//endregion

	//region BelowWordListViewButtonHandleGroup
	@FXML
	private void saveWordListHandle() {
		String listName = wordListNameField.getText();
		if (listName.equals("")) {
			new Alert(Alert.AlertType.WARNING, "没有表名", ButtonType.OK).showAndWait();
			return;
		}
		boolean exist = SharedObjects.getWordListHelper().isExistByKey(listName);
		WordList wordList;
		if (exist) {
			wordList = SharedObjects.getWordListHelper().read(listName);
			if (wordList == null) {
				new Alert(Alert.AlertType.ERROR, "读表失败").show();
				return;
			}
			List<String> collect = wordListView.getItems().parallelStream().map(WordTrace::getWordName).collect(Collectors
					.toList());
			wordList.setWordsArrayList(new ArrayList<>(collect));
		} else {
			wordList = new WordList(listName, wordListView.getItems().parallelStream().map(WordTrace::getWordName).collect
					(Collectors.toList()));
		}
		if (SharedObjects.getWordListHelper().write(wordList)) {
			new Alert(Alert.AlertType.INFORMATION, "保存" + listName + "成功!", ButtonType.OK).showAndWait();
		} else {
			new Alert(Alert.AlertType.ERROR, "保存" + listName + "失败!", ButtonType.OK).showAndWait();
		}
	}

	@FXML
	private void clearWordListHandle() {
		wordListNameField.setText("");
		wordListView.getItems().clear();
	}

	@FXML
	private void selectWordListHandle() {
		var tempWordList = getViewWordList();

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.getDialogPane().setPrefWidth(250);
		alert.titleProperty().setValue("选择表");
		@SuppressWarnings("unchecked")
		List<String> list = Collections.checkedList(SharedObjects.getWordListHelper().searchAll(), String.class);
		ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableList(list));
		Label nameLabel = new Label();
		Label createTimeLabel = new Label();
		Label lastReadTimeLabel = new Label();

		HBox hBox1 = new HBox(new Label("组名:"), nameLabel);
		HBox hBox2 = new HBox(new Label("创建时间:"), createTimeLabel);
		HBox hBox3 = new HBox(new Label("最后阅读时间:"), lastReadTimeLabel);
		VBox group = new VBox(choiceBox, hBox1, hBox2, hBox3);
		group.maxWidth(Double.MAX_VALUE);
		choiceBox.setMaxWidth(Double.MAX_VALUE);
		alert.getDialogPane().setContent(group);

		choiceBox.setOnAction(event -> {
			String value = choiceBox.getValue();
			if (value != null) {
				nameLabel.setText(value);
				WordList wordList = SharedObjects.getWordListHelper().read(value);
				if (wordList != null) {
					createTimeLabel.setText(wordList.getCreateTime().toString());
					lastReadTimeLabel.setText(wordList.getLastReadTime().toString());
					setViewWordList(wordList);
				}
			}
		});
		alert.showAndWait().filter(buttonType -> buttonType == ButtonType.CANCEL).ifPresent(re -> setViewWordList(tempWordList));
	}

	@FXML
	void deleteWordListHandle() {
		Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK, ButtonType.CANCEL);
		alert.getDialogPane().setPrefWidth(250);
		alert.titleProperty().setValue("选择要删除的表");
		@SuppressWarnings("unchecked")
		List<String> list = Collections.checkedList(SharedObjects.getWordListHelper().searchAll(), String.class);
		ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableList(list));
		Label nameLabel = new Label();
		Label createTimeLabel = new Label();
		Label lastReadTimeLabel = new Label();
		HBox hBox1 = new HBox(new Label("组名"), nameLabel);
		HBox hBox2 = new HBox(new Label("创建时间"), createTimeLabel);
		HBox hBox3 = new HBox(new Label("最后阅读时间"), lastReadTimeLabel);
		VBox group = new VBox(choiceBox, hBox1, hBox2, hBox3);
		group.maxWidth(Double.MAX_VALUE);
		choiceBox.setMaxWidth(Double.MAX_VALUE);
		choiceBox.setOnAction(event -> {
			String value = choiceBox.getValue();
			if (value != null) {
				nameLabel.setText(value);
				WordList wordList = SharedObjects.getWordListHelper().read(value);
				if (wordList != null) {
					createTimeLabel.setText(wordList.getCreateTime().toString());
					lastReadTimeLabel.setText(wordList.getLastReadTime().toString());
				}
			}
		});
		alert.getDialogPane().setContent(group);
		alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).ifPresent(re -> {
			if (choiceBox.getValue() != null) {
				if (SharedObjects.getWordListHelper().deleteByKey(choiceBox.getValue()))
					new Alert(Alert.AlertType.INFORMATION, "删除" + choiceBox.getValue() + "成功", ButtonType.OK).showAndWait();
				else
					new Alert(Alert.AlertType.ERROR, "删除" + choiceBox.getValue() + "失败", ButtonType.OK).showAndWait();
			} else {
				new Alert(Alert.AlertType.WARNING, "没有选择", ButtonType.OK).showAndWait();
			}
		});
	}

	@FXML
	private void confirmWordListHandle() {
		WordList viewWordList = getViewWordList();
		if (viewWordList.getWordsArrayList().size() > 0)
			callBackAfterConfirm.accept(viewWordList);
	}

	private void setViewWordList(WordList wordList) {
		wordListNameField.setText(wordList.getListName());
		wordListView.setItems(FXCollections.observableList(new ArrayList<>(SharedObjects.getAll(wordList.getWordsArrayList()).values())));
	}

	private WordList getViewWordList() {
		WordList wordList = new WordList(wordListNameField.getText(),
				wordListView.getItems().parallelStream().map(WordTrace::getWordName).collect(Collectors.toList()));
		WordList read = SharedObjects.getWordListHelper().read(wordList.getListName());
		if (read != null) {
			wordList.setCreateTime(read.getCreateTime());
		}
		return wordList;
	}
	//endregion

	//region BelowTableButtonHandleGroup
	@FXML
	private void addFTableTListHandle() {
		distinctAddToList(wordsTable.getItems());
	}

	@FXML
	private void addFListTTableHandle() {
		distinctAddToTable(wordListView.getItems());
	}

	@FXML
	private void deleteFTableTListHandle() {
		wordListView.getItems().removeAll(wordsTable.getItems());
	}

	@FXML
	private void deleteFListTTableHandle() {
		wordsTable.getItems().removeAll(wordListView.getItems());
	}

	private void distinctAddToTable(final Collection<WordTrace> wordTraces) {
		ArrayList<WordTrace> traces = new ArrayList<>(wordTraces);
		traces.addAll(wordsTable.getItems());
		wordsTable.setItems(FXCollections.observableList(traces.parallelStream().distinct()
				.collect(Collectors.toList())));
	}

	private void distinctAddToList(final Collection<WordTrace> wordTraces) {
		ArrayList<WordTrace> traces = new ArrayList<>(wordTraces);
		traces.addAll(wordListView.getItems());
		wordListView.setItems(FXCollections.observableList(traces.parallelStream().distinct()
				.collect(Collectors.toList())));
	}
	//endregion

	//region ListContextMenuHandle
	@FXML
	void listRemoveItemHandle() {
		wordListView.getItems().removeAll(getListSelectedTraces());
	}

	@FXML
	void listAddItemHandle() {
		distinctAddToTable(getListSelectedTraces());
	}

	@FXML
	void listRefreshItemHandle() {
		ArrayList<WordTrace> list = new ArrayList<>(wordListView.getItems());
		wordListView.getItems().clear();
		wordListView.setItems(FXCollections.observableList(list));
	}

	private List<WordTrace> getListSelectedTraces() {
		return wordListView.getSelectionModel().getSelectedItems();
	}

	//endregion

	//region TableContextHandle
	@FXML
	void tableAddItemHandle() {
		distinctAddToList(getTableSelectedTraces());
	}

	@FXML
	void tableRemoveItemHandle() {
		wordsTable.getItems().removeAll(getTableSelectedTraces());
	}

	@FXML
	void tableRefreshItemHandle() {
		ArrayList<WordTrace> list = new ArrayList<>(wordsTable.getItems());
		wordsTable.getItems().clear();
		wordsTable.setItems(FXCollections.observableList(list));
	}

	@FXML
	void tableClearItemHandle() {
		wordsTable.getItems().clear();
	}

	private List<WordTrace> getTableSelectedTraces() {
		return wordsTable.getSelectionModel().getSelectedItems();
	}
	//endregion

	//region Contract
	private Consumer<WordList> callBackAfterConfirm;

	void setCallBackAfterConfirm(@NotNull Consumer<WordList> callBackAfterConfirm) {
		this.callBackAfterConfirm = callBackAfterConfirm;
	}
	//endregion
}

enum ColumnContentType {
	word("单词"), meaning("释义"), frequency("出现频率"), progress("记忆次数"),
	lastReadTime("最后阅读时间"), forget("忘记次数"), easy("容易次数"), value("价值");

	public final String name;

	ColumnContentType(String s) {
		name = s;
	}

	public static Map<String, ColumnContentType> nameMap = new HashMap<>();

	static {
		for (ColumnContentType type : ColumnContentType.values()) {
			nameMap.put(type.name, type);
		}
	}

	public static Callback<TableColumn.CellDataFeatures<WordTrace, ?>, ObservableValue<?>>
	bindFactory(ColumnContentType type) {
		switch (type) {
			case word:
				return param -> new SimpleObjectProperty<>(param.getValue().getWordName());
			case meaning:
				return param -> new SimpleStringProperty(param.getValue().getWordEntity().getSimple_meaning());
			case easy:
				return param -> new SimpleIntegerProperty(param.getValue().getEasy());
			case forget:
				return param -> new SimpleIntegerProperty(param.getValue().getForget());
			case progress:
				return param -> new SimpleIntegerProperty(param.getValue().getProgress());
			case frequency:
				return param -> new SimpleIntegerProperty(param.getValue().getWordEntity().getFrequency());
			case lastReadTime:
				return param -> new SimpleStringProperty(param.getValue().getLastReadTime().toString());
			default:
				return param -> null;
		}
	}

	public static List<String> getValues() {
		var list = new ArrayList<String>();
		for (ColumnContentType type : ColumnContentType.values()) {
			list.add(type.name);
		}
		return list;
	}

}
