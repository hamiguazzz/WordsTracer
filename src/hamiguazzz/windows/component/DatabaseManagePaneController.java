package hamiguazzz.windows.component;

import hamiguazzz.windows.SharedObjects;
import hamiguazzz.word.Word;
import hamiguazzz.word.WordTrace;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DatabaseManagePaneController {

	//region Field and Inti
	@FXML
	private TableColumn<WordTrace, String> wordNameColumn;
	@FXML
	private TableColumn<WordTrace, String> meanColumn;
	@FXML
	private TableColumn<WordTrace, Integer> progressColumn;
	@FXML
	private TableColumn<WordTrace, Integer> easyColumn;
	@FXML
	private TableColumn<WordTrace, String> lastColumn;
	@FXML
	private TableColumn<WordTrace, Integer> forgetColumn;
	@FXML
	private Button searchButton;
	@FXML
	private TextField searchTextField;
	@FXML
	private ToggleButton addModelButton;
	@FXML
	private TableView<WordTrace> dataTable;
	@FXML
	private ListView<DatabaseOperation> historyList;

	@FXML
	private void initialize() {
		var searchImage = new Image(String.valueOf(getClass().getResource("/search.png")));
		searchButton.setGraphic(new ImageView(searchImage));

		searchTextField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER)
				searchHandle();
		});

		historyList.setCellFactory(param -> new ListCell<>() {
			@Override
			protected void updateItem(DatabaseOperation item, boolean empty) {
				super.updateItem(item, empty);
				if (item != null && !empty) {
					this.setText(item.toInfoLabel());
					setOnMouseClicked(value -> {
						if (value.getClickCount() > 1)
							if (item.trace != null)
								Objects.requireNonNull(DetailsPaneController.createDetailsStage(item.trace)).show();
					});
				} else if (empty) {
					//if not add this branch,data will be duplicated.
					setText(null);
					setGraphic(null);
				}
			}
		});

		dataTable.setRowFactory(param -> {
			TableRow<WordTrace> tableRow = new TableRow<>();
			tableRow.setOnMouseClicked(event -> {
				if (event.getClickCount() > 1) {
					WordTrace selectedItem = dataTable.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						Objects.requireNonNull(DetailsPaneController.createDetailsStage(selectedItem)).show();
					}
				}
			});
			return tableRow;
		});

		historyList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		wordNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWordName()));
		meanColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getWordEntity()
				.getSimple_meaning()));
		progressColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getProgress()));
		forgetColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getForget()));
		easyColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getEasy()));
		lastColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()
				.getLastReadTime().equals(WordTrace.OLDEST_TIME_TAG) ? "" : param.getValue()
				.getLastReadTime().toString()));


		dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}
	//endregion

	//region Utils
	private boolean remove(WordTrace wordTrace) {
		return dataTable.getItems().remove(wordTrace);
	}

	private boolean add(WordTrace wordTrace) {
		return dataTable.getItems().add(wordTrace);
	}

	private DatabaseOperation popOperation() {
		if (historyList.getItems().size() < 1) return null;
		return historyList.getItems().get(0);
	}

	private void pushOperation(DatabaseOperation operation) {
		historyList.getItems().add(0, operation);
	}
	//endregion

	//region Handle
	@FXML
	private void searchHandle() {
		String wordName = searchTextField.getText();
		boolean adding = addModelButton.isSelected();
		if (wordName == null) return;
		List<WordTrace> traces = SharedObjects.getWordStartWith(wordName);
		if (adding) {
			traces.addAll(dataTable.getItems());
			List<WordTrace> collect = traces.stream().distinct().sorted(Comparator.comparing(WordTrace::getWordName)).collect(Collectors.toList());
			dataTable.setItems(FXCollections.observableList(collect));
		} else {
			dataTable.getItems().clear();
			dataTable.getItems().addAll(traces);
		}
	}

	@FXML
	private void revertHandle() {
		DatabaseOperation databaseOperation = popOperation();
		if (databaseOperation == null) return;
		databaseOperation.undoOperation();
		pushOperation(databaseOperation.revert());
	}

	@FXML
	private void deleteWordHandle() {
		ObservableList<WordTrace> selectedItems = dataTable.getSelectionModel().getSelectedItems();
		for (WordTrace selectedItem : selectedItems) {
			DatabaseOperation databaseOperation = new DatabaseOperation(selectedItem.getWordEntity(), selectedItem,
					OperationType.DELETE);
			databaseOperation.doOperation();
			pushOperation(databaseOperation);
		}
	}

	@FXML
	private void deleteTraceHandle() {
		ObservableList<WordTrace> selectedItems = dataTable.getSelectionModel().getSelectedItems();
		ArrayList<WordTrace> wordTraces = new ArrayList<>(selectedItems);
		for (WordTrace selectedItem : wordTraces) {
			DatabaseOperation databaseOperation = new DatabaseOperation(selectedItem.getWordEntity(), selectedItem, OperationType.CLEAR);
			databaseOperation.doOperation();
			pushOperation(databaseOperation);
		}
	}

	@FXML
	private void clearTableHandle() {
		dataTable.getItems().clear();
	}
	//endregion

	//region Operation
	private class DatabaseOperation {
		Word word;
		WordTrace trace;
		OperationType operationType;
		boolean reverted;

		DatabaseOperation(Word word, WordTrace trace, OperationType operationType) {
			this(word, trace, operationType, false);
		}

		DatabaseOperation(Word word, WordTrace trace, OperationType operationType, boolean reverted) {
			this.word = word;
			this.trace = trace;
			if (!trace.getWordEntity().equals(word)) {
				this.trace = SharedObjects.getTrace(word.getWord());
			}
			this.operationType = operationType;
			this.reverted = reverted;
		}

		DatabaseOperation revert() {
			return new DatabaseOperation(word, trace, operationType, !reverted);
		}

		boolean doOperation() {
			if (reverted) return undoOperation();
			switch (operationType) {
				case DELETE:
					return remove(trace) && SharedObjects.getWordBuilder().delete(word)
							&& SharedObjects.getWordTraceBuilder().delete(trace) && SharedObjects.reFresh(word.getWord());
				case CLEAR:
					return remove(trace) && SharedObjects.getWordTraceBuilder().delete(trace) && SharedObjects.reFresh(word.getWord());
			}
			throw new NullPointerException(operationType.name());
		}

		boolean undoOperation() {
			if (reverted) return doOperation();
			switch (operationType) {
				case DELETE:
					return add(trace) && SharedObjects.getWordBuilder().write(word)
							&& SharedObjects.getWordTraceBuilder().write(trace) && SharedObjects.reFresh(word.getWord());
				case CLEAR:
					return add(trace) && SharedObjects.getWordTraceBuilder().write(trace) && SharedObjects.reFresh(word
							.getWord());
			}
			throw new NullPointerException(operationType.name());
		}

		String toInfoLabel() {
			String pre;
			switch (operationType) {
				case CLEAR:
					pre = "删除记录";
					break;
				case DELETE:
					pre = "删除单词";
					break;
				default:
					pre = "";
					break;
			}
			return (reverted ? "撤销" : "") + pre + ":" + word.getWord();
		}
	}

	private enum OperationType {
		DELETE, CLEAR
	}
	//endregion

}



