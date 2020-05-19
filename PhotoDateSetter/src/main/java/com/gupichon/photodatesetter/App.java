package com.gupichon.photodatesetter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class App extends Application {

	private Scene scene;

	@Override
	public void start(Stage primaryStage) throws Exception {
		if (!ExiftoolExecutor.testExiftool()) {
			System.err.println("No exiftool");
			Alert alert = new Alert(AlertType.ERROR, "Exiftool manquant ou trop ancien", ButtonType.OK);
			alert.showAndWait();
			Platform.exit();
		}
		MainPane mp = new MainPane(primaryStage);
		scene = new Scene(mp);
		primaryStage.setHeight(300);
		primaryStage.setWidth(500);
		primaryStage.setResizable(false);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Applicateur de date de prise de vue");
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
			}
		});
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
