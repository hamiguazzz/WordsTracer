package hamiguazzz.windows.component;


import hamiguazzz.windows.SharedObjects;
import hamiguazzz.word.WordList;
import hamiguazzz.word.WordTrace;
import hamiguazzz.word.helper.WordMeaning;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

@SuppressWarnings("unused")
public final class WordPaneController {
	//region FXMLNodeAndInitialize
	@FXML
	private Label indexLabel;
	@FXML
	private Label wordNameLabel;
	@FXML
	private Label proEnLabel;
	@FXML
	private Label proAmLabel;
	@FXML
	private VBox meaningsBox;
	@FXML
	private Button easyButton;
	@FXML
	private Button forgetButton;
	@FXML
	private Button rememberButton;
	@FXML
	private Button detailsButton;

	@FXML
	private void initialize() {
		Thread updateThread = new Thread(this::updateThreadHandle, "appWordTraceUpdate");
		updateThread.setDaemon(true);
		updateThread.start();
	}
	//endregion

	//region Contract
	public void setWordList(@NotNull WordList wordList) {
		setTraces(new ArrayDeque<>(SharedObjects.getAll(wordList.getWordsArrayList()).values()));
		wordList.setLastReadTime(LocalDateTime.now());
		SharedObjects.getWordListHelper().write(wordList);
	}

	private void setTraces(@NotNull Deque<WordTrace> traces) {
		getNextWord();
		this.traceDeque = traces;
		index = 1;
		trace = this.traceDeque.pollFirst();
		while (trace == null) {
			trace = this.traceDeque.pollFirst();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		updateWordTrace();
	}

	public void stop() {
		running = false;
	}
	//endregion

	//region ButtonHandleGroup
	@FXML
	private void forgetWordHandle() {
		if (trace != null) {
			trace.setForget(trace.getForget() + 1);
		}
		progressWord();
	}

	@FXML
	private void easyWordHandle() {
		if (trace != null) {
			trace.setEasy(trace.getEasy() + 1);
		}
		progressWord();
	}

	@FXML
	private void rememberWordHandle() {
		progressWord();
	}

	@FXML
	private void showDetailsHandle() {
		Stage stage = DetailsPaneController.createDetailsStage(trace);
		if (stage != null) {
			stage.show();
		}
	}
	//endregion

	//region PrivatePartFunction
	private WordTrace trace = null;
	private Deque<WordTrace> traceDeque = null;
	private int index;
	private boolean hideWordMeaning = false;

	private void progressWord() {
		if (trace != null) {
			trace.setProgress(trace.getProgress() + 1);
			trace.updateLastRead();
			getNextWord();
			updateWordTrace();
		} else {
			Alert alert = new Alert(Alert.AlertType.INFORMATION, "没了！", ButtonType.OK);
			alert.setHeaderText("完成列表");
			alert.titleProperty().setValue("通知");
			alert.show();
		}
	}

	private void getNextWord() {
		if (traceDeque == null) {
			this.trace = null;
			return;
		}
		if (this.trace != null) {
			updateDeque.addLast(this.trace);
		}
		this.trace = traceDeque.pollFirst();
		if (this.trace != null) index++;
	}

	//Update Windows
	private void updateWordTrace() {
		if (this.trace == null) {
			return;
		}
		if (!hideWordMeaning) {
			Platform.runLater(() -> {
				indexLabel.setText(String.format("(%d/%d)", index, traceDeque.size() + index));
				wordNameLabel.setText(trace.getWordName());
				proEnLabel.setText("[" + trace.getWordEntity().getPronunciation_en() + "]");
				proAmLabel.setText("[" + trace.getWordEntity().getPronunciation_am() + "]");
				ObservableList<Node> children = meaningsBox.getChildren();
				children.clear();
				for (WordMeaning meaning : trace.getWordEntity().getMeaningsZH()) {
					StringBuilder builder = new StringBuilder();
					builder.append(meaning.getPart());
					String[] means = meaning.getMeans();
					if (means.length == 0) continue;
					for (int i = 0; i < means.length - 1; i++) {
						builder.append(means[i]).append(",");
					}
					builder.append(means[means.length - 1]);
					Text text = new Text(builder.toString());
					text.setWrappingWidth(400);
					children.add(text);
				}
			});
		} else {
			Platform.runLater(() -> {
				indexLabel.setText(String.format("(%d/%d)", index, traceDeque.size() + index));
				wordNameLabel.setText(trace.getWordName());
				proEnLabel.setText("[" + trace.getWordEntity().getPronunciation_en() + "]");
				proAmLabel.setText("[" + trace.getWordEntity().getPronunciation_am() + "]");
				meaningsBox.getChildren().clear();
			});
		}
	}

	private void setHideWordMeaning(boolean hideWordMeaning) {
		this.hideWordMeaning = hideWordMeaning;
		updateWordTrace();
	}
	//endregion

	//region Update DataBase
	private Deque<WordTrace> updateDeque = new ArrayDeque<>();
	private volatile boolean running = true;

	private void updateThreadHandle() {
		while (running) {
			while (!updateDeque.isEmpty()) {
				SharedObjects.getWordTraceBuilder().write(Objects.requireNonNull(updateDeque.pollFirst()));
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	//endregion

	//region KeyHandle
	@FXML
	private void keyEventHandle(KeyEvent event) {
		switch (event.getCharacter()) {
			case "e":
				easyWordHandle();
				break;
			case "r":
				progressWord();
				break;
			case "f":
				forgetWordHandle();
				break;
			case "h":
				setHideWordMeaning(!hideWordMeaning);
				break;
			case "d":
				showDetailsHandle();
				break;
		}
	}
	//endregion
}
