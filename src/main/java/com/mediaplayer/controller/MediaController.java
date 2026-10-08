package com.mediaplayer.controller;

import com.mediaplayer.model.MediaItem;
import com.mediaplayer.model.MediaType;
import com.mediaplayer.model.Playlist;
import com.mediaplayer.service.FileManager;
import com.mediaplayer.service.PlaybackManager;
import com.mediaplayer.util.TimeFormatter;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

public class MediaController {
    @FXML private TextField searchField;
    @FXML private StackPane displayArea;
    @FXML private MediaView mediaView;
    @FXML private VBox audioView;
    @FXML private Label emptyLabel;
    @FXML private Label fileNameLabel;
    @FXML private Label metadataLabel;
    @FXML private Label nowPlayingLabel;
    @FXML private Label timeLabel;
    @FXML private Label typeLabel;
    @FXML private Label countLabel;
    @FXML private HBox visualizer;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Button playButton;
    @FXML private Button muteButton;
    @FXML private Button repeatButton;
    @FXML private ToggleButton shuffleButton;
    @FXML private ListView<MediaItem> playlistView;

    private final Playlist playlist = new Playlist();
    private PlaybackManager playback;
    private FilteredList<MediaItem> filtered;
    private boolean seeking;
    private boolean muted;
    private AnimationTimer visualizerTimer;

    @FXML
    public void initialize() {
        playback = new PlaybackManager();
        filtered = new FilteredList<>(playlist.getItems(), item -> true);
        playlistView.setItems(filtered);
        playlistView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(MediaItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String icon = item.getType() == MediaType.VIDEO ? "▸ " : "♫ ";
                    setText(icon + item.getFileName());
                }
            }
        });
        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                playSelected();
            }
        });

        searchField.textProperty().addListener((observable, oldText, newText) ->
                filtered.setPredicate(item ->
                        item.getFileName().toLowerCase().contains(newText.toLowerCase())
                )
        );
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                playback.setVolume(newValue.doubleValue())
        );
        progressSlider.setOnMousePressed(event -> seeking = true);
        progressSlider.setOnMouseReleased(event -> {
            seeking = false;
            if (playback.getPlayer() != null) {
                playback.getPlayer().seek(Duration.seconds(progressSlider.getValue()));
            }
        });

        setupShortcuts();
        updateCount();
    }

    private void setupShortcuts() {
        displayArea.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getTarget() instanceof TextInputControl && !event.isControlDown()) {
            return;
        }

        KeyCode key = event.getCode();
        if (key == KeyCode.SPACE) {
            togglePlay();
            event.consume();
        } else if (key == KeyCode.LEFT) {
            seekBackward();
        } else if (key == KeyCode.RIGHT) {
            seekForward();
        } else if (key == KeyCode.UP) {
            volumeSlider.setValue(Math.min(1, volumeSlider.getValue() + 0.05));
        } else if (key == KeyCode.DOWN) {
            volumeSlider.setValue(Math.max(0, volumeSlider.getValue() - 0.05));
        } else if (key == KeyCode.O && event.isControlDown()) {
            openFile();
        } else if (key == KeyCode.Q && event.isControlDown()) {
            exitApplication();
        } else if (key == KeyCode.F11) {
            fullscreen();
        }
    }

    @FXML
    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Media");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "All Media Files", "*.mp3", "*.wav", "*.m4a", "*.aac", "*.mp4", "*.m4v"
                ),
                new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav", "*.m4a", "*.aac"),
                new FileChooser.ExtensionFilter("Video", "*.mp4", "*.m4v")
        );

        List<File> files = chooser.showOpenMultipleDialog(displayArea.getScene().getWindow());
        if (files != null) {
            for (File file : files) {
                if (FileManager.supported(file.toPath())) {
                    playlist.add(new MediaItem(file.toPath()));
                }
            }
            updateCount();
        }
    }

    @FXML
    private void openFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open Media Folder");
        File folder = chooser.showDialog(displayArea.getScene().getWindow());
        if (folder != null) {
            try {
                FileManager.scan(folder).forEach(playlist::add);
                updateCount();
            } catch (IOException exception) {
                System.err.println("Could not read media folder: " + exception.getMessage());
                showAlert("Could not open folder", "The selected folder could not be read.");
            }
        }
    }

    private void updateCount() {
        int count = playlist.getItems().size();
        countLabel.setText(count + " item" + (count == 1 ? "" : "s"));
    }

    @FXML
    private void removeSelected() {
        MediaItem item = playlistView.getSelectionModel().getSelectedItem();
        if (item != null) {
            if (item == playlist.current()) {
                stop();
            }
            playlist.remove(item);
            updateCount();
        }
    }

    @FXML
    private void clearPlaylist() {
        stop();
        playlist.clear();
        updateCount();
    }

    private void playSelected() {
        MediaItem item = playlistView.getSelectionModel().getSelectedItem();
        if (item == null) {
            showAlert("Playlist is empty", "Select a media item first.");
            return;
        }
        playlist.setCurrentIndex(playlist.getItems().indexOf(item));
        loadCurrent(true);
    }

    private void loadCurrent(boolean autoPlay) {
        MediaItem item = playlist.current();
        if (item == null) {
            showAlert("Playlist is empty", "Add a media file to begin.");
            return;
        }
        if (!Files.exists(item.getPath())) {
            showAlert("File unavailable", "The selected file no longer exists.");
            return;
        }

        try {
            playback.load(item, this::handleEnd, () ->
                    Platform.runLater(() -> {
                        setupPlayerUI(item);
                        if (autoPlay) {
                            playback.play();
                        }
                    })
            );
        } catch (MediaException exception) {
            System.err.println("Media error: " + exception);
            showAlert("Playback error", "This media file could not be loaded.");
        }
    }

    private void setupPlayerUI(MediaItem item) {
        MediaPlayer player = playback.getPlayer();
        emptyLabel.setVisible(false);
        nowPlayingLabel.setText(item.getFileName());
        typeLabel.setText(item.getType().name());

        if (item.getType() == MediaType.VIDEO) {
            mediaView.setVisible(true);
            audioView.setVisible(false);
            mediaView.setMediaPlayer(player);
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            mediaView.fitWidthProperty().bind(displayArea.widthProperty().subtract(30));
            mediaView.fitHeightProperty().bind(displayArea.heightProperty().subtract(30));
        } else {
            mediaView.setVisible(false);
            audioView.setVisible(true);
            fileNameLabel.setText(item.getFileName());
            metadataLabel.setText("Audio file");
        }

        player.currentTimeProperty().addListener((observable, oldTime, currentTime) -> {
            if (!seeking) {
                progressSlider.setMax(Math.max(1, player.getTotalDuration().toSeconds()));
                progressSlider.setValue(currentTime.toSeconds());
            }
            timeLabel.setText(
                    TimeFormatter.format(currentTime) + " / "
                            + TimeFormatter.format(player.getTotalDuration())
            );
        });
        player.setOnPlaying(() -> {
            playButton.setText("Ⅱ");
            startVisualizer();
        });
        player.setOnPaused(() -> {
            playButton.setText("▶");
            stopVisualizer();
        });
        player.setOnStopped(() -> {
            playButton.setText("▶");
            stopVisualizer();
        });
        playlistView.getSelectionModel().select(item);
    }

    private void handleEnd() {
        PlaybackManager.RepeatMode repeatMode = playback.getRepeatMode();
        if (repeatMode == PlaybackManager.RepeatMode.CURRENT) {
            playback.getPlayer().seek(Duration.ZERO);
            playback.play();
            return;
        }
        if (playlist.getItems().isEmpty()) {
            return;
        }

        int nextIndex = playback.isShuffle()
                ? new Random().nextInt(playlist.getItems().size())
                : playlist.getCurrentIndex() + 1;
        if (nextIndex >= playlist.getItems().size()) {
            if (repeatMode == PlaybackManager.RepeatMode.PLAYLIST) {
                nextIndex = 0;
            } else {
                playback.stop();
                return;
            }
        }

        playlist.setCurrentIndex(nextIndex);
        loadCurrent(true);
    }

    @FXML
    private void togglePlay() {
        if (playback.getPlayer() == null) {
            if (playlist.current() != null) {
                loadCurrent(true);
            } else if (!playlist.getItems().isEmpty()) {
                playlist.setCurrentIndex(0);
                loadCurrent(true);
            } else {
                showAlert("Playlist is empty", "Add a media file to begin.");
            }
        } else if (playback.isPlaying()) {
            playback.pause();
        } else {
            playback.play();
        }
    }

    @FXML
    private void stop() {
        playback.stop();
        if (playback.getPlayer() != null) {
            playback.getPlayer().seek(Duration.ZERO);
        }
    }

    @FXML
    private void next() {
        if (playlist.getItems().isEmpty()) {
            return;
        }
        int index = playlist.getCurrentIndex() + 1;
        if (index >= playlist.getItems().size()) {
            index = 0;
        }
        playlist.setCurrentIndex(index);
        loadCurrent(true);
    }

    @FXML
    private void previous() {
        if (playlist.getItems().isEmpty()) {
            return;
        }
        int index = playlist.getCurrentIndex() - 1;
        if (index < 0) {
            index = playlist.getItems().size() - 1;
        }
        playlist.setCurrentIndex(index);
        loadCurrent(true);
    }

    @FXML
    private void seekBackward() {
        playback.seek(-10);
    }

    @FXML
    private void seekForward() {
        playback.seek(10);
    }

    @FXML
    private void toggleMute() {
        muted = !muted;
        playback.setMute(muted);
        muteButton.setText(muted ? "🔇" : "🔊");
    }

    @FXML
    private void toggleShuffle() {
        playback.setShuffle(shuffleButton.isSelected());
    }

    @FXML
    private void cycleRepeat() {
        PlaybackManager.RepeatMode currentMode = playback.getRepeatMode();
        PlaybackManager.RepeatMode nextMode;
        if (currentMode == PlaybackManager.RepeatMode.NONE) {
            nextMode = PlaybackManager.RepeatMode.CURRENT;
        } else if (currentMode == PlaybackManager.RepeatMode.CURRENT) {
            nextMode = PlaybackManager.RepeatMode.PLAYLIST;
        } else {
            nextMode = PlaybackManager.RepeatMode.NONE;
        }

        playback.setRepeatMode(nextMode);
        if (nextMode == PlaybackManager.RepeatMode.NONE) {
            repeatButton.setText("Repeat: Off");
        } else if (nextMode == PlaybackManager.RepeatMode.CURRENT) {
            repeatButton.setText("Repeat: One");
        } else {
            repeatButton.setText("Repeat: All");
        }
    }

    @FXML
    private void fullscreen() {
        Stage stage = (Stage) displayArea.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    private void exitApplication() {
        ((Stage) displayArea.getScene().getWindow()).close();
    }

    private void startVisualizer() {
        if (visualizerTimer != null || !audioView.isVisible()) {
            return;
        }

        for (int i = 0; i < 18; i++) {
            Region bar = new Region();
            bar.getStyleClass().add("viz-bar");
            bar.setPrefWidth(5);
            bar.setPrefHeight(15);
            visualizer.getChildren().add(bar);
        }

        visualizerTimer = new AnimationTimer() {
            private long lastUpdate;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 90_000_000) {
                    return;
                }
                lastUpdate = now;
                for (Node bar : visualizer.getChildren()) {
                    bar.setScaleY(0.3 + Math.random() * 2.2);
                }
            }
        };
        visualizerTimer.start();
    }

    private void stopVisualizer() {
        if (visualizerTimer != null) {
            visualizerTimer.stop();
            visualizerTimer = null;
        }
        visualizer.getChildren().clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public void shutdown() {
        stopVisualizer();
        playback.dispose();
    }
}
