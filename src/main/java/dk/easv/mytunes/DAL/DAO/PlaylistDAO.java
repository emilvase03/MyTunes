package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.DAL.DB.DBConnector;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {

    private final DBConnector db = new DBConnector();

    public PlaylistDAO() throws IOException {
    }

    public List<Playlist> getAllPlaylists(int userId) throws Exception {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT id, user_id, name, song_filepaths, duration " +
                "FROM playlists WHERE user_id = ? ORDER BY name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                playlists.add(new Playlist(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("song_filepaths"),
                        rs.getString("duration")
                ));
            }
        }

        return playlists;
    }

    public Playlist createPlaylist(Playlist playlist) throws Exception {
        String sql = "INSERT INTO playlists (user_id, name, song_filepaths, duration) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, playlist.getUserId());
            stmt.setString(2, playlist.getName());
            stmt.setString(3, playlist.getSongFilepaths());
            stmt.setString(4, playlist.getDuration());

            stmt.executeUpdate();

            // set generated playlist id
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                playlist.setId(keys.getInt(1));
            }
        }

        return playlist;
    }

    public void updatePlaylist(Playlist playlist) throws Exception {
        String sql = "UPDATE playlists SET name = ?, song_filepaths = ?, duration = ? " +
                "WHERE id = ? AND user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playlist.getName());
            stmt.setString(2, playlist.getSongFilepaths());
            stmt.setString(3, playlist.getDuration());
            stmt.setInt(4, playlist.getId());
            stmt.setInt(5, playlist.getUserId());

            stmt.executeUpdate();
        }
    }

    public void deletePlaylist(int playlistId) throws Exception {
        String sql = "DELETE FROM playlists WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.executeUpdate();
        }
    }

    // requires song dao
    /*
    public List<Song> getSongsFromPlaylist(int playlistId) throws Exception {
        return null;
    }

    public void addSongToPlaylist(int playlistId, Song song) throws Exception {}

    public void removeSongFromPlaylist(int playlistId, Song song) throws Exception {}
    */
}
