package dk.easv.mytunes.GUI.UTIL;

// Project imports
import dk.easv.mytunes.BE.Song;

// Java imports
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaybackManager {

    private MediaPlayer mediaPlayer;
    private double volume = 0.5;

    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    private final List<Song> playlistContext = new ArrayList<>();
    private int playlistIndex = -1;

    private String currentLoadedFilePath = null;

    public ObjectProperty<Song> currentSongProperty() {
        return currentSong;
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public Song getCurrentSong() {
        return currentSong.get();
    }

    public boolean isPlaying() {
        return playing.get();
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double value) {
        this.volume = Math.max(0.0, Math.min(1.0, value));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
    }

    // standalone, non-playlist song playback
    public void playSong(Song song) {
        if (song == null || !isValidSong(song)) return;

        if (!isInPlaylistMode() && isSameLoadedSong(song)) {
            togglePlayPause();
            return;
        }

        clearPlaylistContext();

        loadAndPlay(song);
    }

    // play a full playlist from the start
    public void playPlaylist(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return;
        }

        setupPlaylistContext(songs, 0);

        loadAndPlay(playlistContext.get(0));
    }

    // play a specific song from a playlist, maintain context
    public void playSongFromPlaylist(List<Song> songs, Song targetSong) {
        if (songs == null || songs.isEmpty() || targetSong == null) {
            return;
        }

        int targetIndex = findSongIndex(songs, targetSong);
        if (targetIndex == -1) {
            targetIndex = 0;
        }

        if (isInPlaylistMode() &&
                playlistIndex == targetIndex &&
                playlistsMatch(songs) &&
                isSameLoadedSong(targetSong)) {
            togglePlayPause();
            return;
        }

        // setup context
        setupPlaylistContext(songs, targetIndex);

        loadAndPlay(playlistContext.get(playlistIndex));
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) {
            return;
        }

        MediaPlayer.Status status = mediaPlayer.getStatus();

        if (status == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playing.set(false);
        } else if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY) {
            mediaPlayer.play();
            playing.set(true);
        }
    }

    // next song in playlist context
    public void next() {
        if (!hasNext()) {
            return;
        }

        playlistIndex++;
        loadAndPlay(playlistContext.get(playlistIndex));
    }

    // previous song in playlist context
    public void prev() {
        if (!hasPrev()) {
            return;
        }

        playlistIndex--;
        loadAndPlay(playlistContext.get(playlistIndex));
    }

    public boolean hasNext() {
        return isInPlaylistMode() && playlistIndex < playlistContext.size() - 1;
    }

    public boolean hasPrev() {
        return isInPlaylistMode() && playlistIndex > 0;
    }

    public boolean isPlayingPlaylistMode() {
        return isInPlaylistMode();
    }

    public ObservableList<Song> getCurrentPlaylist() {
        return FXCollections.observableArrayList(playlistContext);
    }

    public int getCurrentIndex() {
        return playlistIndex;
    }

    // check if given song is currently playing
    // includes id, filepath, and playing status
    public boolean isCurrentSong(Song song) {
        if (song == null || getCurrentSong() == null) {
            return false;
        }

        return song.getId() == getCurrentSong().getId()
                && song.getFilepath().equals(currentLoadedFilePath)
                && isPlaying();
    }

    // stop playback and clear state
    public void stop() {
        disposeMediaPlayer();
        currentSong.set(null);
        currentLoadedFilePath = null;
        playing.set(false);
        clearPlaylistContext();
    }

    // core helpers
    private void loadAndPlay(Song song) {
        if (!isValidSong(song)) {
            return;
        }

        disposeMediaPlayer();

        currentSong.set(song);
        currentLoadedFilePath = song.getFilepath();

        try {
            File file = new File(song.getFilepath());
            if (!file.exists()) {
                System.err.println("Audio file not found: " + song.getFilepath());
                playing.set(false);
                return;
            }

            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume);

            mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);

            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError());
                playing.set(false);
            });

            mediaPlayer.play();
            playing.set(true);

        } catch (Exception e) {
            System.err.println("Error loading song: " + e.getMessage());
            e.printStackTrace();
            playing.set(false);
        }
    }

    private void handleEndOfMedia() {
        if (hasNext()) {
            next();
        } else {
            playing.set(false);

            if (isInPlaylistMode() && !playlistContext.isEmpty()) {
                playlistIndex = 0;
                currentSong.set(playlistContext.get(0));
            }
        }
    }

    // clean up to prevent resource leaks
    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                // ignore errors during disposal
            }
            mediaPlayer = null;
        }
    }

    // playlist context management
    private void setupPlaylistContext(List<Song> songs, int startIndex) {
        playlistContext.clear();
        playlistContext.addAll(songs);
        playlistIndex = startIndex;
    }

    // clear playlist context
    private void clearPlaylistContext() {
        playlistContext.clear();
        playlistIndex = -1;
    }

    private boolean isInPlaylistMode() {
        return !playlistContext.isEmpty() && playlistIndex >= 0;
    }

    private boolean isValidSong(Song song) {
        return song != null
                && song.getFilepath() != null
                && !song.getFilepath().isBlank();
    }

    private boolean isSameLoadedSong(Song song) {
        Song current = getCurrentSong();
        return song != null
                && current != null
                && song.getId() == current.getId()
                && song.getFilepath().equals(currentLoadedFilePath);
    }

    // compare current playlist context with new playlist
    private boolean playlistsMatch(List<Song> newPlaylist) {
        if (newPlaylist.size() != playlistContext.size()) {
            return false;
        }

        for (int i = 0; i < newPlaylist.size(); i++) {
            if (newPlaylist.get(i).getId() != playlistContext.get(i).getId()) {
                return false;
            }
        }

        return true;
    }

    // find index of target song in given list
    private int findSongIndex(List<Song> songs, Song targetSong) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId() == targetSong.getId()) {
                return i;
            }
        }
        return -1;
    }
}