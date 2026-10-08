package com.mediaplayer.service;

import com.mediaplayer.model.MediaItem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileManager {
    public static boolean supported(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.matches(".*\\.(mp3|wav|m4a|aac|mp4|m4v)$");
    }

    public static List<MediaItem> scan(File directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory.toPath())) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(FileManager::supported)
                    .sorted()
                    .map(MediaItem::new)
                    .toList();
        }
    }
}