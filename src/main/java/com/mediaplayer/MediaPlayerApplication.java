package com.mediaplayer;

import com.mediaplayer.controller.MediaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MediaPlayerApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MediaPlayer.fxml"));
        Scene scene = new Scene(loader.load(), 1400, 767);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        stage.setTitle("KAT! Media Player");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> loader.<MediaController>getController().shutdown());
    }
}