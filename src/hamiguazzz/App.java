package hamiguazzz;

import hamiguazzz.windows.component.SharedObjects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

	@Override
	public void start(Stage primaryStage) throws IOException {
		Thread preLoadThread = new Thread(SharedObjects::preLoad, "preLoad");
		preLoadThread.setDaemon(true);
		preLoadThread.start();
		TabPane root = FXMLLoader.load(App.class.getResource("/hamiguazzz/windows/component/MainWindow.fxml"));
		primaryStage.getIcons().add(new Image(String.valueOf(App.class.getResource("/AppIcon.png"))));
		primaryStage.setTitle("WordsTracer");
		Scene scene = new Scene(root, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
