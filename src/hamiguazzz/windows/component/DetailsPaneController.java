package hamiguazzz.windows.component;

import hamiguazzz.App;
import hamiguazzz.word.WordTrace;
import hamiguazzz.word.helper.WordExchange;
import hamiguazzz.word.helper.WordMeaning;
import hamiguazzz.word.helper.WordMeaningEn;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.css.Size;
import javafx.css.SizeUnits;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DetailsPaneController {
	//region FXMLNodeAndInitialize
	@FXML
	private Label wordNameLabel;

	@FXML
	private Label proEnLabel;

	@FXML
	private Label proAmLabel;

	@FXML
	private VBox meanEnBox;

	@FXML
	private VBox meanZhBox;

	@FXML
	private VBox exchangeBox;

	@FXML
	private Label meanEnLabel;

	@SuppressWarnings("unused")
	@FXML
	private Label meanZhLabel;

	@FXML
	private Label exchangeLabel;

	@FXML
	private void initialize() {
		assert wordNameLabel != null : "fx:id=\"wordNameLabel\" was not injected: check your FXML file 'DetailsPane.fxml'.";
		assert proEnLabel != null : "fx:id=\"proEnLabel\" was not injected: check your FXML file 'DetailsPane.fxml'.";
		assert proAmLabel != null : "fx:id=\"proAmLabel\" was not injected: check your FXML file 'DetailsPane.fxml'.";
		assert meanEnBox != null : "fx:id=\"meanEnBox\" was not injected: check your FXML file 'DetailsPane.fxml'.";
		assert meanZhBox != null : "fx:id=\"meanZhBox\" was not injected: check your FXML file 'DetailsPane.fxml'.";
		assert exchangeBox != null : "fx:id=\"exchangeBox\" was not injected: check your FXML file 'DetailsPane.fxml'.";
	}

	//endregion
	//region Contract
	@Nullable
	static Parent createDetailsPane(@NotNull final WordTrace trace) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(DetailsPaneController.class.getResource("DetailsPane.fxml"));
			Parent load = fxmlLoader.load();
			DetailsPaneController controller = fxmlLoader.getController();
			controller.setWordOnShown(trace);
			return load;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static Stage createDetailsStage(@NotNull final WordTrace trace) {
		Parent detailsPane = DetailsPaneController.createDetailsPane(trace);
		if (detailsPane == null) return null;
		Stage stage = new Stage();
		stage.setTitle(trace.getWordName());
		stage.setScene(new Scene(detailsPane, 600, 800));
		stage.getIcons().add(new Image(String.valueOf(App.class.getResource("/AppIcon.png"))));
		return stage;
	}

	//endregion
	//region Private Realization
	private WordTrace wordOnShown;

	private void setWordOnShown(WordTrace wordOnShown) {
		this.wordOnShown = wordOnShown;
		clear();
		update();
	}

	private void update() {
		Platform.runLater(() -> {
			if (wordOnShown == null) return;
			wordNameLabel.setText(wordOnShown.getWordName());
			proEnLabel.setText("[" + wordOnShown.getWordEntity().getPronunciation_en() + "]");
			proAmLabel.setText("[" + wordOnShown.getWordEntity().getPronunciation_am() + "]");
			generateMeanZhContent();
			generateExchangeContent();
			generateMeanEnContent();
		});
	}

	private void generateExchangeContent() {
		assert wordOnShown != null;
		WordExchange wordExchange = wordOnShown.getWordEntity().getWordExchange();
		partGenerateExchangeContent("复数:", wordExchange.getWord_pl());
		partGenerateExchangeContent("过去式:", wordExchange.getWord_past());
		partGenerateExchangeContent("过去分词:", wordExchange.getWord_done());
		partGenerateExchangeContent("动名词:", wordExchange.getWord_ing());
		partGenerateExchangeContent("单三:", wordExchange.getWord_third());
		partGenerateExchangeContent("比较级:", wordExchange.getWord_er());
		partGenerateExchangeContent("最高级:", wordExchange.getWord_est());
		if (exchangeBox.getChildren().size() == 0) {
			this.exchangeLabel.setText("");
		}
	}

	private void partGenerateExchangeContent(String partName, String[] parts) {
		if (parts != null && parts.length > 0) {
			final String content;
			if (parts.length == 1) content = parts[0];
			else {
				final StringBuilder builder = new StringBuilder();
				for (int i = 0; i < parts.length - 1; i++) {
					builder.append(parts[i]).append(",");
				}
				builder.append(parts.length - 1);
				content = builder.toString();
			}
			exchangeBox.getChildren().add(getLabelPair(partName, content));
		}
	}

	private void generateMeanZhContent() {
		assert wordOnShown != null;
		ObservableList<Node> children = meanZhBox.getChildren();
		children.clear();
		for (WordMeaning meaning : wordOnShown.getWordEntity().getMeaningsZH()) {
			StringBuilder builder = new StringBuilder();
			builder.append(meaning.getPart());
			String[] means = meaning.getMeans();
			if (means.length == 0) continue;
			for (int i = 0; i < means.length - 1; i++) {
				builder.append(means[i]).append(",");
			}
			builder.append(means[means.length - 1]);
			Label label = new Label(builder.toString());
			label.autosize();
			children.add(label);
		}
		if (children.size() == 0) {
			children.add(new Label(wordOnShown.getWordEntity().getSimple_meaning()));
		}
	}

	private void generateMeanEnContent() {
		assert wordOnShown != null;
		WordMeaningEn[] meaningsEN = wordOnShown.getWordEntity().getMeaningsEN();
		List<String> collect = Arrays.stream(meaningsEN).parallel().map(WordMeaningEn::getPart).distinct().collect(Collectors
				.toList());
		for (String part : collect) {
			Label label = new Label(part);
			label.setMaxWidth(Double.MAX_VALUE);
			label.setAlignment(Pos.CENTER);
			label.setPadding(new Insets(3, 0, 0, 3));
			Font font = Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, 18);
			label.fontProperty().set(font);
			meanEnBox.getChildren().add(label);
			var pane = new VBox();
			Arrays.stream(meaningsEN).filter(mean -> mean.getPart().equals(part))
					.forEach(e -> partGenerateMeanEnContent(pane, e));
			meanEnBox.getChildren().add(pane);
		}
	}

	private final static double fitLength = new Size(80, SizeUnits.PX).getValue();
	private final static double wrapTextLength = 500;

	private void partGenerateMeanEnContent(VBox pane, WordMeaningEn wordMeaningEn) {
		ObservableList<Node> children = pane.getChildren();
		//region Labels
		Label labelMean = new Label("Meanings:");
		Label labelRelative = new Label("Relatives:");
		Label labelExample = new Label("Examples:");
		final var labels = new Label[]{labelMean, labelRelative, labelExample};
		final Font font = Font.font("System", FontWeight.LIGHT, FontPosture.ITALIC, 12);
		for (Label label : labels) {
			label.setPrefWidth(fitLength);
			label.setFont(font);
			label.setTextFill(Color.GRAY);
			label.setPadding(new Insets(3, 0, 3, 0));
		}
		//endregion
		//region separator
		Separator separator = new Separator();
		separator.setOrientation(Orientation.HORIZONTAL);
		separator.setMaxWidth(Double.MAX_VALUE);
		separator.setPadding(new Insets(3, 0, 3, 0));
		//endregion
		children.add(separator);
		if (wordMeaningEn.getMeans().length != 0
				&& !wordMeaningEn.getMeans()[0].equals("[]")) {
			children.add(labelMean);
			for (String s : wordMeaningEn.getMeans()) {
				Text t = new Text(s);
				t.setWrappingWidth(wrapTextLength);
				children.add(t);
			}
		}

		if (wordMeaningEn.getExamples().length != 0
				&& !wordMeaningEn.getExamples()[0].equals("[]")) {
			children.add(labelExample);
			for (String s : wordMeaningEn.getExamples()) {
				Text t = new Text(s);
				t.setWrappingWidth(wrapTextLength);
				children.add(t);
			}
		}

		String[] similar_words = wordMeaningEn.getSimilar_words();
		if (similar_words.length != 0 && !similar_words[0].equals("[]")) {
			children.add(labelRelative);
			String re_Content;
			if (similar_words.length == 1) {
				re_Content = similar_words[0];
			} else {
				StringBuilder buffer = new StringBuilder();
				for (int i = 0; i < similar_words.length - 1; i++) {
					buffer.append(similar_words[i]).append(",");
				}
				buffer.append(similar_words[similar_words.length - 1]);
				re_Content = buffer.toString();
			}
			Text relativeText = new Text(re_Content);
			relativeText.setWrappingWidth(wrapTextLength);
			children.add(relativeText);
		}
	}

	private void clear() {
		Platform.runLater(() -> {
			this.wordNameLabel.setText("");
			this.proAmLabel.setText("");
			this.proEnLabel.setText("");
			this.exchangeBox.getChildren().clear();
			this.meanEnBox.getChildren().clear();
			this.meanZhBox.getChildren().clear();
		});
	}

	private HBox getLabelPair(String label1, String label2) {
		HBox re = new HBox();
		re.setMaxWidth(Double.MAX_VALUE);
		Label l1 = new Label(label1);
		Label l2 = new Label(label2);
		l1.setLabelFor(l2);
		l1.setPadding(new Insets(0, 5, 0, 0));
		re.getChildren().addAll(l1, l2);
		return re;
	}
	//endregion
}