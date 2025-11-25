package dk.easv.mytunes.DAL;

import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;

import java.util.List;

public interface IPlaylistDataAccess {

    List<Playlist> getAllPlaylists() throws Exception;

    Playlist createPlaylist(Playlist playlist) throws Exception;

    void updatePlaylist(Playlist playlist) throws Exception;

    void deletePlaylist(int playlistId) throws Exception;

    List<Song> getSongsInPlaylist(int playlistId) throws Exception;

    void addSongToPlaylist(int playlistId, int songId) throws Exception;

    void removeSongFromPlaylist(int playlistId, int songId) throws Exception;

    // optional to update song position in playlist
    //void updateSongPosition(int playlistId, int songId, int newPosition) throws Exception;
}
