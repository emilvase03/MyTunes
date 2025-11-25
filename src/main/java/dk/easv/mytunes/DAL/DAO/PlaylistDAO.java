package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
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

    public List<Song> getSongsFromPlaylist(int playlistId) throws Exception {
        List<Song> songs = new ArrayList<>();

        String sql = "SELECT song_filepaths FROM playlists WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return songs;

            String json = rs.getString("song_filepaths");
            if (json == null || json.isEmpty()) return songs;

            // Remove brackets and split into filepaths
            json = json.replace("[", "").replace("]", "").replace("\"", "");
            String[] filepaths = json.split(",");

            // Fetch song objects
            String songQuery = "SELECT title, artist, category, duration, filepath " +
                    "FROM songs WHERE filepath = ?";

            for (String path : filepaths) {
                path = path.trim();
                if (path.isEmpty()) continue;

                try (PreparedStatement songStmt = conn.prepareStatement(songQuery)) {
                    songStmt.setString(1, path);
                    ResultSet songRS = songStmt.executeQuery();

                    if (songRS.next()) {
                        Song s = new Song(
                                songRS.getString("title"),
                                songRS.getString("artist"),
                                songRS.getString("category"),
                                songRS.getString("duration")
                        );
                        s.setFilepath(songRS.getString("filepath")); // if your Song class has this
                        songs.add(s);
                    }
                }
            }
        }

        return songs;
    }

    public void addSongToPlaylist(int playlistId, Song song) throws Exception {
        String sql = "SELECT song_filepaths FROM playlists WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            String json = "[]";
            if (rs.next()) {
                json = rs.getString("song_filepaths");
            }

            // Convert JSON array → List
            json = json.replace("[", "").replace("]", "").replace("\"", "");
            List<String> filepaths = new ArrayList<>();

            if (!json.trim().isEmpty()) {
                for (String fp : json.split(",")) {
                    if (!fp.trim().isEmpty()) filepaths.add(fp.trim());
                }
            }

            // Add new song filepath
            filepaths.add(song.getFilepath());

            // Convert back to JSON
            String newJson = "[\"" + String.join("\",\"", filepaths) + "\"]";

            // Update playlist
            String update = "UPDATE playlists SET song_filepaths = ? WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                updateStmt.setString(1, newJson);
                updateStmt.setInt(2, playlistId);
                updateStmt.executeUpdate();
            }
        }
    }

    public void removeSongFromPlaylist(int playlistId, Song song) throws Exception {
        String sql = "SELECT song_filepaths FROM playlists WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return;

            String json = rs.getString("song_filepaths");
            json = json.replace("[", "").replace("]", "").replace("\"", "");

            List<String> filepaths = new ArrayList<>();
            for (String fp : json.split(",")) {
                if (!fp.trim().equals(song.getFilepath()))
                    filepaths.add(fp.trim());
            }

            // Convert back to JSON
            String newJson = "[\"" + String.join("\",\"", filepaths) + "\"]";

            // Update playlist
            String update = "UPDATE playlists SET song_filepaths = ? WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                updateStmt.setString(1, newJson);
                updateStmt.setInt(2, playlistId);
                updateStmt.executeUpdate();
            }
        }
    }

}
