package dk.easv.mytunes.GUI.UTIL;

import dk.easv.mytunes.BE.Song;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class PlaybackManager {

    private MediaPlayer mediaPlayer;
    private String currentFilePath;

    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    private double volume = 0.5;

    public BooleanProperty playingProperty() {
        return playing;
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public void setVolume(double volume) {
        this.volume = clamp(volume, 0.0, 1.0);
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
    }

    public double getVolume() {
        return volume;
    }

    public void playSong(Song song) {
        String path = song.getFilepath();
        if (path == null) return;

        if (path.equals(currentFilePath) && mediaPlayer != null) {
            togglePause();
        } else {
            stopIfNeeded();
            mediaPlayer = createPlayer(path);
            currentFilePath = path;

            mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) ->
                    playing.set(newStatus == MediaPlayer.Status.PLAYING)
            );

            mediaPlayer.setOnEndOfMedia(() -> playing.set(false));

            mediaPlayer.play();
        }
    }

    public void togglePause() {
        if (mediaPlayer == null) return;

        switch (mediaPlayer.getStatus()) {
            case PLAYING -> mediaPlayer.pause();
            case PAUSED, STOPPED, READY -> mediaPlayer.play();
            default -> mediaPlayer.play();
        }

        if (mediaPlayer != null) {
            playing.set(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING);
        }
    }

    private void stopIfNeeded() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        playing.set(false);
    }

    private MediaPlayer createPlayer(String path) {
        Media media = new Media(new File(path).toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setVolume(volume); // apply current volume
        return player;
    }

    public boolean isCurrentSong(Song song) {
        if (song == null || currentFilePath == null) return false;
        return song.getFilepath().equals(currentFilePath) && isPlaying();
    }

    // Helper: clamp value to min/max
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
