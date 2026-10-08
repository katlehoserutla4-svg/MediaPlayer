package com.mediaplayer.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Playlist {
    private final ObservableList<MediaItem> items = FXCollections.observableArrayList();
    private int currentIndex = -1;

    public ObservableList<MediaItem> getItems() {
        return items;
    }

    public void add(MediaItem item) {
        if (item != null && items.stream().noneMatch(existing -> existing.getPath().equals(item.getPath()))) {
            items.add(item);
        }
    }

    public void remove(MediaItem item) {
        int index = items.indexOf(item);
        items.remove(item);
        if (currentIndex >= items.size()) {
            currentIndex = items.size() - 1;
        } else if (index >= 0 && index < currentIndex) {
            currentIndex--;
        }
    }

    public void clear() {
        items.clear();
        currentIndex = -1;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        currentIndex = index;
    }

    public MediaItem current() {
        if (currentIndex < 0 || currentIndex >= items.size()) {
            return null;
        }
        return items.get(currentIndex);
    }
}