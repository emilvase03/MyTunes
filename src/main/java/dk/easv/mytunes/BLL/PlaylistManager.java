package dk.easv.mytunes.BLL;

import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.DAL.DAO.PlaylistDAO;
import dk.easv.mytunes.DAL.IPlaylistDataAccess;

import java.io.IOException;
import java.util.List;

public class PlaylistManager {

    private final IPlaylistDataAccess playlistDAO;

    public PlaylistManager() throws IOException {
        playlistDAO = new PlaylistDAO();
    }

    public List<Playlist> getAllPlaylists() throws Exception {
        return playlistDAO.getAllPlaylists();
    }

    public Playlist createPlaylist(Playlist playlist) throws Exception {
        return playlistDAO.createPlaylist(playlist);
    }

    public void updatePlaylist(Playlist playlist) throws Exception {
        playlistDAO.updatePlaylist(playlist);
    }

    public void deletePlaylist(int playlistId) throws Exception {
        playlistDAO.deletePlaylist(playlistId);
    }

    public List<Song> getSongsInPlaylist(int playlistId) throws Exception {
        return playlistDAO.getSongsInPlaylist(playlistId);
    }

    public void addSongToPlaylist(int playlistId, int songId) throws Exception {
        playlistDAO.addSongToPlaylist(playlistId, songId);
    }

    public void removeSongFromPlaylist(int playlistId, int songId) throws Exception {
        playlistDAO.removeSongFromPlaylist(playlistId, songId);
    }
}
