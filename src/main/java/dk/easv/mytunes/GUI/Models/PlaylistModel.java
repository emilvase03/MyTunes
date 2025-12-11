package dk.easv.mytunes.GUI.Models;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.BLL.PlaylistManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;

public class PlaylistModel {
    private static PlaylistModel instance;
    private PlaylistManager playlistManager;
    private ObservableList<Playlist> playlistsToBeViewed;

    private PlaylistModel() throws Exception {
        playlistManager = new PlaylistManager();
        playlistsToBeViewed = FXCollections.observableArrayList();
        playlistsToBeViewed.addAll(playlistManager.getAllPlaylists());
    }

    public static PlaylistModel getInstance() throws Exception {
        if(instance == null)
            return instance = new PlaylistModel();
        return instance;
    }

    public ObservableList<Playlist> getObservablePlaylists() {
        return playlistsToBeViewed;
    }

    public Playlist createPlaylist(Playlist newPlaylist) throws Exception {
        Playlist playlistCreated = playlistManager.createPlaylist(newPlaylist);
        getObservablePlaylists().add(playlistCreated);
        return playlistCreated;
    }

    public void updatePlaylist(Playlist playlistToBeUpdated) throws Exception {
        playlistManager.updatePlaylist(playlistToBeUpdated);

        int index = -1;
        for (int i = 0; i < playlistsToBeViewed.size(); i++) {
            if (playlistsToBeViewed.get(i).getId() == playlistToBeUpdated.getId()) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            playlistsToBeViewed.set(index, playlistToBeUpdated);
        }
    }

    public void deletePlaylist(int selectedPlaylistId) throws Exception {
        playlistManager.deletePlaylist(selectedPlaylistId);
        playlistsToBeViewed.removeIf(playlist -> playlist.getId() == selectedPlaylistId);
    }

    public SequencedCollection<Song> getSongsInPlaylist(int playlistId) throws Exception {
        return playlistManager.getSongsInPlaylist(playlistId);
    }

    public void addSongToPlaylist(Playlist playlist, Song song) throws Exception {
        playlistManager.addSongToPlaylist(playlist.getId(), song.getId());
        refreshPlaylist(playlist.getId());
    }

    public ObservableList<Song> getObservableSongsInPlaylist(Playlist playlist) {
        try {
            return FXCollections.observableArrayList(
                    playlistManager.getSongsInPlaylist(playlist.getId())
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not load songs for playlist: " + playlist.getName(), e);
        }
    }

    public void removeSongFromPlaylist(int playlistId, int songId) throws Exception {
        playlistManager.removeSongFromPlaylist(playlistId, songId);
        refreshPlaylist(playlistId);
    }

    private void refreshPlaylist(int playlistId) throws Exception {
        SequencedCollection<Playlist> allPlaylists = playlistManager.getAllPlaylists();

        // find the updated playlist
        Playlist updatedPlaylist = null;
        for (Playlist p : allPlaylists) {
            if (p.getId() == playlistId) {
                updatedPlaylist = p;
                break;
            }
        }

        if (updatedPlaylist != null) {
            // find and update in observable list
            for (int i = 0; i < playlistsToBeViewed.size(); i++) {
                if (playlistsToBeViewed.get(i).getId() == playlistId) {
                    playlistsToBeViewed.set(i, updatedPlaylist);
                    break;
                }
            }
        }
    }

    // Reordering helpers --------------------------------------------------

    /**
     * Move the song at index `index` one position up (towards 0).
     * Persists change to DB and refreshes the playlist in the observable list.
     *
     * @param playlist the playlist to mutate
     * @param index    the current index of the song in the playlist (0-based)
     * @throws Exception on DB / IO errors
     */
    public void moveSongUp(Playlist playlist, int index) throws Exception {
        if (playlist == null) return;
        if (index <= 0) return;

        List<String> filepaths = parseFilepathsJson(playlist.getSongFilepaths());
        if (index >= filepaths.size()) return;

        Collections.swap(filepaths, index, index - 1);

        playlistManager.updatePlaylistFilepaths(playlist.getId(), filepaths);

        // refresh playlist in observable collection
        refreshPlaylist(playlist.getId());
    }

    /**
     * Move the song at index `index` one position down.
     * Persists change to DB and refreshes the playlist in the observable list.
     *
     * @param playlist the playlist to mutate
     * @param index    the current index of the song in the playlist (0-based)
     * @throws Exception on DB / IO errors
     */
    public void moveSongDown(Playlist playlist, int index) throws Exception {
        if (playlist == null) return;

        List<String> filepaths = parseFilepathsJson(playlist.getSongFilepaths());
        if (index < 0 || index >= filepaths.size() - 1) return;

        Collections.swap(filepaths, index, index + 1);

        playlistManager.updatePlaylistFilepaths(playlist.getId(), filepaths);

        // refresh playlist in observable collection
        refreshPlaylist(playlist.getId());
    }

    // helpers
    public int getSongCount(Playlist playlist) {
        String songFilepaths = playlist.getSongFilepaths();

        if (songFilepaths == null || songFilepaths.isBlank()) {
            return 0;
        }

        String cleaned = songFilepaths.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .trim();

        if (cleaned.isEmpty()) {
            return 0;
        }

        String[] filepaths = cleaned.split(",");
        int count = 0;
        for (String filepath : filepaths) {
            if (!filepath.trim().isEmpty()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Parse the DB JSON string (which is stored like ["path1","path2"]) into a List<String>.
     * Uses the same simple parsing rules as existing DAO (keeps behaviour stable).
     */
    private List<String> parseFilepathsJson(String json) {
        List<String> filepaths = new ArrayList<>();
        if (json == null || json.isBlank()) return filepaths;

        String cleaned = json.replace("[", "").replace("]", "").replace("\"", "").trim();
        if (cleaned.isEmpty()) return filepaths;

        String[] parts = cleaned.split(",");
        for (String p : parts) {
            if (!p.trim().isEmpty()) filepaths.add(p.trim());
        }

        return filepaths;
    }
}
