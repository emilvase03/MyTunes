package dk.easv.mytunes.GUI.UTIL;

import dk.easv.mytunes.BE.Song;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages audio playback for songs and playlists.
 * Handles single song playback and playlist mode with next/previous navigation.
 */
public class PlaybackManager {

    // MediaPlayer state
    private MediaPlayer mediaPlayer;
    private double volume = 0.5;

    // Current playback state
    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);

    // Playlist context (only used when playing from a playlist)
    private final List<Song> playlistContext = new ArrayList<>();
    private int playlistIndex = -1;

    // Track the actual loaded file to distinguish between same song in different contexts
    private String currentLoadedFilePath = null;

    // Properties
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

    // Volume management
    public double getVolume() {
        return volume;
    }

    public void setVolume(double value) {
        this.volume = Math.max(0.0, Math.min(1.0, value));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
    }

    /**
     * Play a single song (standalone, not part of playlist context)
     */
    public void playSong(Song song) {
        if (song == null || !isValidSong(song)) return;

        // If the song is already loaded AND we're not switching playlists, just toggle
        if (!isInPlaylistMode() && isSameLoadedSong(song)) {
            togglePlayPause();
            return;
        }

        // If we are in playlist mode, or it's a different song, clear playlist context
        clearPlaylistContext();

        // Load and play the song
        loadAndPlay(song);
    }


    /**
     * Play a playlist starting from the first song
     */
    public void playPlaylist(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return;
        }

        // Set up playlist context
        setupPlaylistContext(songs, 0);

        // Play first song
        loadAndPlay(playlistContext.get(0));
    }

    /**
     * Play a specific song from a playlist, maintaining playlist context
     */
    public void playSongFromPlaylist(List<Song> songs, Song targetSong) {
        if (songs == null || songs.isEmpty() || targetSong == null) {
            return;
        }

        // Find target song index (first occurrence)
        int targetIndex = findSongIndex(songs, targetSong);
        if (targetIndex == -1) {
            targetIndex = 0; // Fallback to first song
        }

        // Check if we're already playing this exact position in this playlist
        if (isInPlaylistMode() &&
                playlistIndex == targetIndex &&
                playlistsMatch(songs) &&
                isSameLoadedSong(targetSong)) {
            // Same position in same playlist - just toggle
            togglePlayPause();
            return;
        }

        // Set up playlist context with the original list and index
        setupPlaylistContext(songs, targetIndex);

        // Play the target song
        loadAndPlay(playlistContext.get(playlistIndex));
    }

    /**
     * Toggle between play and pause for currently loaded song
     */
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

    /**
     * Play next song in playlist (only works in playlist mode)
     */
    public void next() {
        if (!hasNext()) {
            return;
        }

        playlistIndex++;
        loadAndPlay(playlistContext.get(playlistIndex));
    }

    /**
     * Play previous song in playlist (only works in playlist mode)
     */
    public void prev() {
        if (!hasPrev()) {
            return;
        }

        playlistIndex--;
        loadAndPlay(playlistContext.get(playlistIndex));
    }

    /**
     * Check if there's a next song available
     */
    public boolean hasNext() {
        return isInPlaylistMode() && playlistIndex < playlistContext.size() - 1;
    }

    /**
     * Check if there's a previous song available
     */
    public boolean hasPrev() {
        return isInPlaylistMode() && playlistIndex > 0;
    }

    /**
     * Check if currently in playlist mode
     */
    public boolean isPlayingPlaylistMode() {
        return isInPlaylistMode();
    }

    /**
     * Get current playlist (for UI display)
     */
    public ObservableList<Song> getCurrentPlaylist() {
        return FXCollections.observableArrayList(playlistContext);
    }

    /**
     * Get current index in playlist (for UI display)
     */
    public int getCurrentIndex() {
        return playlistIndex;
    }

    /**
     * Check if given song is currently playing (not paused)
     * This now checks the actual loaded filepath and playlist position
     */
    public boolean isCurrentSong(Song song) {
        if (song == null || getCurrentSong() == null) {
            return false;
        }

        // Must be same song ID, same filepath, and actually playing
        return song.getId() == getCurrentSong().getId()
                && song.getFilepath().equals(currentLoadedFilePath)
                && isPlaying();
    }

    /**
     * Stop all playback and clean up
     */
    public void stop() {
        disposeMediaPlayer();
        currentSong.set(null);
        currentLoadedFilePath = null;
        playing.set(false);
        clearPlaylistContext();
    }

    // ========== INTERNAL HELPER METHODS ==========

    /**
     * Core method to load and start playing a song
     */
    private void loadAndPlay(Song song) {
        if (!isValidSong(song)) {
            return;
        }

        // Clean up any existing player
        disposeMediaPlayer();

        // Update current song and filepath
        currentSong.set(song);
        currentLoadedFilePath = song.getFilepath();

        try {
            // Verify file exists
            File file = new File(song.getFilepath());
            if (!file.exists()) {
                System.err.println("Audio file not found: " + song.getFilepath());
                playing.set(false);
                return;
            }

            // Create new MediaPlayer
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume);

            // Set up end-of-media handler
            mediaPlayer.setOnEndOfMedia(this::handleEndOfMedia);

            // Set up error handler
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError());
                playing.set(false);
            });

            // Start playback
            mediaPlayer.play();
            playing.set(true);

        } catch (Exception e) {
            System.err.println("Error loading song: " + e.getMessage());
            e.printStackTrace();
            playing.set(false);
        }
    }

    /**
     * Handle what happens when a song finishes playing
     */
    private void handleEndOfMedia() {
        if (hasNext()) {
            // Auto-play next song in playlist
            next();
        } else {
            // End of playlist or single song
            playing.set(false);

            // In playlist mode, loop back to start (but don't auto-play)
            if (isInPlaylistMode() && !playlistContext.isEmpty()) {
                playlistIndex = 0;
                currentSong.set(playlistContext.get(0));
            }
        }
    }

    /**
     * Properly dispose of MediaPlayer to prevent resource leaks
     */
    private void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                // Ignore disposal errors
            }
            mediaPlayer = null;
        }
    }

    /**
     * Set up playlist context for playlist playback
     */
    private void setupPlaylistContext(List<Song> songs, int startIndex) {
        playlistContext.clear();
        playlistContext.addAll(songs);
        playlistIndex = startIndex;
    }

    /**
     * Clear playlist context (switches to single-song mode)
     */
    private void clearPlaylistContext() {
        playlistContext.clear();
        playlistIndex = -1;
    }

    /**
     * Check if currently in playlist mode
     */
    private boolean isInPlaylistMode() {
        return !playlistContext.isEmpty() && playlistIndex >= 0;
    }

    /**
     * Check if song has valid filepath
     */
    private boolean isValidSong(Song song) {
        return song != null
                && song.getFilepath() != null
                && !song.getFilepath().isBlank();
    }

    /**
     * Check if the given song is the same as currently loaded song
     * This checks both ID and filepath to handle duplicate songs
     */
    private boolean isSameLoadedSong(Song song) {
        Song current = getCurrentSong();
        return song != null
                && current != null
                && song.getId() == current.getId()
                && song.getFilepath().equals(currentLoadedFilePath);
    }

    /**
     * Check if two playlists contain the same songs in the same order
     */
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

    /**
     * Find index of song in list by ID
     * Note: This returns the FIRST occurrence if song appears multiple times
     */
    private int findSongIndex(List<Song> songs, Song targetSong) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getId() == targetSong.getId()) {
                return i;
            }
        }
        return -1;
    }
}