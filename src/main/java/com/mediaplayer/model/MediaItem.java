package com.mediaplayer.model;

import java.nio.file.Path;

public class MediaItem {
    private final Path path;
    private final MediaType type;

    public MediaItem(Path path) {
        this.path = path;
        this.type = detectType(path);
    }

    private static MediaType detectType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.matches(".*\\.(mp3|wav|m4a|aac)$")) {
            return MediaType.AUDIO;
        }
        if (name.matches(".*\\.(mp4|m4v)$")) {
            return MediaType.VIDEO;
        }
        return MediaType.UNKNOWN;
    }

    public Path getPath() {
        return path;
    }

    public MediaType getType() {
        return type;
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    @Override
    public String toString() {
        return getFileName();
    }
}