package hamiguazzz.windows.component;


import hamiguazzz.word.WordTrace;
import hamiguazzz.word.helper.WordMeaning;
import hamiguazzz.word.helper.WordTraceBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

@SuppressWarnings("unused")
public class WordPaneController {
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

	private WordTrace trace = null;

	private Deque<WordTrace> traceDeque = null;

	private Deque<WordTrace> updateDeque;

	private WordTraceBuilder traceBuilder;
	private volatile boolean running = true;
	private int index;

	@FXML
	private void initialize() {
		updateDeque = new ArrayDeque<>();
		Thread updateThread = new Thread(this::updateThreadHandle, "appWordTraceUpdate");
		updateThread.setDaemon(true);
		updateThread.start();
	}

	public void setTraces(@NotNull Deque<WordTrace> traces) {
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

	public void setTraceBuilder(@NotNull WordTraceBuilder traceBuilder) {
		this.traceBuilder = traceBuilder;
	}

	public void stop() {
		running = false;
	}

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
		//todo showDetailsHandle
	}

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

	//Update Windows
	private void updateWordTrace() {
		if (this.trace != null) {
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
					children.add(new Label(builder.toString()));
				}
			});
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

	//Update DataBase
	private void updateThreadHandle() {
		while (running) {
			while (!updateDeque.isEmpty()) {
				traceBuilder.write(Objects.requireNonNull(updateDeque.pollFirst()));
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
