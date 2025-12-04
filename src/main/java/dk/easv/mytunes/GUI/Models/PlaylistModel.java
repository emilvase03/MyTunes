package dk.easv.mytunes.GUI.Models;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.BLL.PlaylistManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    public void addSongToPlaylist(int playlistId, int songId) throws Exception {
        playlistManager.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(int playlistId, int songId) throws Exception {
        playlistManager.removeSongFromPlaylist(playlistId, songId);
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
}