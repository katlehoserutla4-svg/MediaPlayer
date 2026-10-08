# Media Player

This is a JavaFX media player I made for a Software Engineering assignment. It can play audio and video files, and lets you keep a simple playlist while using the player.

## Technologies

- Java 17
- JavaFX 17 (Controls, FXML and Media)
- Maven

## Running the application

Make sure Java 17 and Maven are installed, then run this from the project folder:

```bash
mvn clean javafx:run
```

To compile without launching the application:

```bash
mvn clean compile
```

## Using the player

- **Open File** lets you choose one or more media files.
- **Open Folder** adds supported files from that folder (it does not search subfolders).
- Double-click an item in the playlist to play it.
- Use the player buttons to play, pause, stop, seek, change volume, mute, and skip tracks.
- Shuffle and the repeat button change what happens when a track finishes.
- Search filters the playlist. Select an item and use **Remove** to take it out.
- **Fullscreen** switches the window to fullscreen while playing video.

Keyboard shortcuts:

- Space: play or pause
- Left/Right: seek back or forward 10 seconds
- Up/Down: change volume
- Ctrl+O: open files
- Ctrl+Q: exit
- F11: toggle fullscreen

## Supported formats and limitations

The file chooser accepts MP3, WAV, M4A, AAC, MP4 and M4V files. Whether a file actually plays depends on the codecs supported by JavaFX Media on the computer. The player does not include its own codecs.

The audio visualization is a simple animation and does not analyse the sound. The player also does not read detailed track metadata.

## Project layout

```text
src/main/java/com/mediaplayer/
├── Main.java
├── MediaPlayerApplication.java
├── controller/MediaController.java
├── model/
│   ├── MediaItem.java
│   ├── MediaType.java
│   └── Playlist.java
├── service/
│   ├── FileManager.java
│   └── PlaybackManager.java
└── util/TimeFormatter.java

src/main/resources/
├── css/style.css
└── fxml/MediaPlayer.fxml
```
