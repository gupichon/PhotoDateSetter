package com.gupichon.photodatesetter;

import java.io.File;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainPane extends VBox {
	private DatePicker datePicker;
	private TextField dirTxtFld;
	private TextField commentTxtFld;
	private ProgressBar progressBar;
	private ExiftoolService service;

	public MainPane(Stage primaryStage) {
		service = new ExiftoolService();
		setSpacing(10);
		Label dirLbl = new Label("Répertoire");
		dirLbl.setMaxHeight(Double.MAX_VALUE);
		dirTxtFld = new TextField();
		dirTxtFld.setMaxWidth(Double.MAX_VALUE);
		dirTxtFld.setEditable(false);
		HBox.setHgrow(dirTxtFld, Priority.ALWAYS);
		dirTxtFld.setPromptText("<Répertoire des images>");
		Button dirBtn = new Button("...");
		dirBtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser dirChooser = new DirectoryChooser();
				dirChooser.setTitle("Choisir un dossier d'images");
				File dir = dirChooser.showDialog(primaryStage);
				if (dir != null && dir.isDirectory()) {
					dirTxtFld.setText(dir.getAbsolutePath());
				}
			}
		});
		HBox dirHBox = new HBox(2, dirLbl, dirTxtFld, dirBtn);

		Label dateLbl = new Label("Date");
		dateLbl.setMaxHeight(Double.MAX_VALUE);
		datePicker = new DatePicker();
		datePicker.setShowWeekNumbers(true);
		HBox dateHBox = new HBox(2, dateLbl, datePicker);

		Button launchBtn = new Button("Lancer");
		launchBtn.disableProperty().bind(dirTxtFld.textProperty().isEmpty().or(datePicker.valueProperty().isNull())
				.or(service.runningProperty()));
		launchBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		VBox.setVgrow(launchBtn, Priority.ALWAYS);
		launchBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				service.setValues(datePicker.getValue(), commentTxtFld.getText(), dirTxtFld.getText());
				service.restart();
			}
		});

		Label commentLbl = new Label("Description");
		commentLbl.setMaxHeight(Double.MAX_VALUE);
		commentTxtFld = new TextField();
		commentTxtFld.setMaxWidth(Double.MAX_VALUE);
		commentTxtFld.setPromptText("<Description optionnelle>");
		HBox.setHgrow(commentTxtFld, Priority.ALWAYS);
		HBox commentHBox = new HBox(2, commentLbl, commentTxtFld);

		progressBar = new ProgressBar(0.0);
		progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		progressBar.progressProperty().bind(service.progressProperty());
		Label message = new Label();
		message.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		message.textProperty().bind(service.messageProperty());

		datePicker.disableProperty().bind(service.runningProperty());
		dirTxtFld.disableProperty().bind(service.runningProperty());
		commentTxtFld.disableProperty().bind(service.runningProperty());
		dirBtn.disableProperty().bind(service.runningProperty());

		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				dirTxtFld.setText(null);
				datePicker.setValue(null);
				commentTxtFld.setText(null);
			}
		});
		this.getChildren().addAll(dirHBox, dateHBox, commentHBox, launchBtn, progressBar, message);

	}
}
