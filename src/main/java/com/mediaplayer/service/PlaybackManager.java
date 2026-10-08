package com.mediaplayer.service;

import com.mediaplayer.model.MediaItem;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class PlaybackManager {
    public enum RepeatMode {
        NONE,
        CURRENT,
        PLAYLIST
    }

    private MediaPlayer player;
    private RepeatMode repeatMode = RepeatMode.NONE;
    private boolean shuffle;

    public MediaPlayer getPlayer() {
        return player;
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void load(MediaItem item, Runnable onEnd, Runnable onReady) {
        dispose();
        if (item == null) {
            return;
        }

        Media media = new Media(new File(item.getPath().toString()).toURI().toString());
        player = new MediaPlayer(media);
        player.setOnReady(onReady);
        player.setOnEndOfMedia(onEnd);
    }

    public void play() {
        if (player != null) {
            player.play();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    public void seek(double seconds) {
        if (player != null) {
            player.seek(player.getCurrentTime().add(Duration.seconds(seconds)));
        }
    }

    public void setVolume(double volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    public void setMute(boolean mute) {
        if (player != null) {
            player.setMute(mute);
        }
    }

    public boolean isPlaying() {
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public void dispose() {
        if (player != null) {
            player.dispose();
            player = null;
        }
    }
}