package dk.easv.mytunes.DAL;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;

// Java imports
import java.util.List;

public interface IPlaylistDataAccess {
    List<Playlist> getAllPlaylists() throws Exception;

    Playlist createPlaylist(Playlist playlist) throws Exception;

    void updatePlaylist(Playlist playlist) throws Exception;

    void deletePlaylist(int playlistId) throws Exception;

    List<Song> getSongsInPlaylist(int playlistId) throws Exception;

    void addSongToPlaylist(int playlistId, int songId) throws Exception;

    void removeSongFromPlaylist(int playlistId, int songId) throws Exception;
}
