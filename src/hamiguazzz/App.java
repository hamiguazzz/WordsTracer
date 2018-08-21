package hamiguazzz;

import hamiguazzz.windows.component.WordPaneController;
import hamiguazzz.word.WordTrace;
import hamiguazzz.word.helper.WordBuilder;
import hamiguazzz.word.helper.WordTraceBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

public class App extends Application {


	private WordPaneController controller;
	private Parent root;

	private Parent createContent() throws IOException {
		var loader = new FXMLLoader(getClass().getResource("windows/component/WordPane.fxml"));
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		root = loader.load();
		controller = loader.getController();
		return root;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		createContent();

		var wordTraceBuilder = new WordTraceBuilder();
		var wordBuilder = new WordBuilder();
		WordTrace test = wordTraceBuilder.build("test", wordBuilder);
		WordTrace next = wordTraceBuilder.build("next", wordBuilder);
		controller.setTraceBuilder(wordTraceBuilder);
		controller.setTraces(new ArrayDeque<>(List.of(test, next)));
		primaryStage.setTitle("WordsTracer");
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(root, 600, 400));
//		大表测试  result:per <0.8ms
//		StopWatch watch = new StopWatch("1600+words");
//		new Thread(() -> {
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			watch.start();
//			var list = wordTraceBuilder.buildAllToDeque(wordBuilder
//					.getWordList("高考"), wordBuilder);
//			System.out.println(list.size());
//			controller.setTraces(list);
//			watch.stop();
//			System.out.println(watch);
//		}).start();
		primaryStage.setOnCloseRequest(event -> controller.stop());
		primaryStage.show();
	}
}
