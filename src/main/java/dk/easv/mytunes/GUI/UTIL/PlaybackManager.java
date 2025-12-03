package dk.easv.mytunes.GUI.UTIL;

import dk.easv.mytunes.BE.Song;

import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class PlaybackManager {

    private MediaPlayer mediaPlayer;
    private String currentFilePath;

    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    public Song getCurrentSong() {
        return currentSong.get();
    }

    public void setCurrentSong(Song song) {
        currentSong.set(song);
    }

    public ObjectProperty<Song> currentSongProperty() {
        return currentSong;
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public void setPlaying(boolean state) {
        playing.set(state);
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public void playSong(Song song) {
        if (song == null || song.getFilepath() == null)
            return;

        String path = song.getFilepath();

        if (path.equals(currentFilePath) && mediaPlayer != null) {
            togglePause();
            return;
        }

        stopCurrentPlayer();

        currentFilePath = path;
        setCurrentSong(song);

        Media media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            setPlaying(newStatus == MediaPlayer.Status.PLAYING);
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            setPlaying(false);
            // next song automatically here.
        });

        mediaPlayer.play();
    }

    public void togglePause() {
        if (mediaPlayer == null)
            return;

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            setPlaying(false);
        } else {
            mediaPlayer.play();
            setPlaying(true);
        }
    }

    public void stop() {
        stopCurrentPlayer();
        currentFilePath = null;
        setCurrentSong(null);
        setPlaying(false);
    }

    private void stopCurrentPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {}
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void setVolume(double value) {
        if (mediaPlayer != null)
            mediaPlayer.setVolume(value);
    }

    public double getVolume() {
        return mediaPlayer != null ? mediaPlayer.getVolume() : 0.5;
    }

    public boolean isCurrentSong(Song song) {
        return song != null && song.equals(getCurrentSong()) && isPlaying();
    }

    public boolean isSongLoaded(Song song) {
        return song != null && song.equals(getCurrentSong());
    }

    public void next() {
        // implement next track logic here
        stop();
    }

    public void prev() {
        // implement previous track logic here
        stop();
    }
}

