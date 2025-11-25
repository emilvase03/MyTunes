package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.IPlaylistDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO implements IPlaylistDataAccess {

    private final DBConnector db = new DBConnector();

    public PlaylistDAO() throws IOException {}

    // ======================================================
    // Playlists
    // ======================================================

    @Override
    public List<Playlist> getAllPlaylists() throws Exception {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT id, user_id, name, song_filepaths, duration FROM playlists ORDER BY name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    @Override
    public Playlist createPlaylist(Playlist playlist) throws Exception {
        String sql = "INSERT INTO playlists (user_id, name, song_filepaths, duration) VALUES (?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, playlist.getUserId());
            stmt.setString(2, playlist.getName());
            stmt.setString(3, playlist.getSongFilepaths());
            stmt.setString(4, playlist.getDuration());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                playlist.setId(keys.getInt(1));
            }
        }
        return playlist;
    }

    @Override
    public void updatePlaylist(Playlist playlist) throws Exception {
        String sql = "UPDATE playlists SET name = ?, song_filepaths = ?, duration = ? WHERE id = ? AND user_id = ?";

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

    @Override
    public void deletePlaylist(int playlistId) throws Exception {
        String sql = "DELETE FROM playlists WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            stmt.executeUpdate();
        }
    }

    // ======================================================
    // Songs in playlist (JSON filepaths)
    // ======================================================

    @Override
    public List<Song> getSongsInPlaylist(int playlistId) throws Exception {
        List<Song> songs = new ArrayList<>();

        String sql = "SELECT song_filepaths FROM playlists WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return songs;

            String json = rs.getString("song_filepaths");
            if (json == null || json.isBlank()) return songs;

            // Convert JSON to list of filepaths
            json = json.replace("[", "").replace("]", "").replace("\"", "");
            String[] filepaths = json.split(",");

            String songQuery = "SELECT id, title, artist, category, duration, filepath FROM songs WHERE filepath = ?";

            for (String path : filepaths) {
                path = path.trim();
                if (path.isEmpty()) continue;

                try (PreparedStatement sStmt = conn.prepareStatement(songQuery)) {
                    sStmt.setString(1, path);
                    ResultSet srs = sStmt.executeQuery();

                    if (srs.next()) {
                        Song s = new Song(
                                srs.getInt("id"),
                                srs.getString("title"),
                                srs.getString("artist"),
                                srs.getString("category"),
                                srs.getString("duration")
                        );
                        s.setId(srs.getInt("id"));      // if Song has id
                        s.setFilepath(path);            // store filepath
                        songs.add(s);
                    }
                }
            }
        }
        return songs;
    }

    @Override
    public void addSongToPlaylist(int playlistId, int songId) throws Exception {
        String fetchSong = "SELECT filepath FROM songs WHERE id = ?";
        String filepath;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(fetchSong)) {

            stmt.setInt(1, songId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return;
            filepath = rs.getString("filepath");
        }

        // Add filepath to JSON list
        updatePlaylistJson(playlistId, filepath, true);
    }

    @Override
    public void removeSongFromPlaylist(int playlistId, int songId) throws Exception {
        String fetchSong = "SELECT filepath FROM songs WHERE id = ?";
        String filepath;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(fetchSong)) {

            stmt.setInt(1, songId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) return;
            filepath = rs.getString("filepath");
        }

        // Remove filepath from JSON list
        updatePlaylistJson(playlistId, filepath, false);
    }

    // ======================================================
    // Helper: Update JSON array in DB
    // ======================================================

    private void updatePlaylistJson(int playlistId, String filepath, boolean add) throws Exception {
        String sql = "SELECT song_filepaths FROM playlists WHERE id = ?";
        String json = "[]";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) json = rs.getString("song_filepaths");
        }

        // Convert JSON to list
        json = json.replace("[", "").replace("]", "").replace("\"", "");
        List<String> filepaths = new ArrayList<>();

        if (!json.isBlank()) {
            for (String fp : json.split(",")) {
                if (!fp.trim().isEmpty()) filepaths.add(fp.trim());
            }
        }

        if (add) {
            filepaths.add(filepath);
        } else {
            filepaths.remove(filepath);
        }

        // Back to JSON format
        String newJson = "[\"" + String.join("\",\"", filepaths) + "\"]";

        String update = "UPDATE playlists SET song_filepaths = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {

            stmt.setString(1, newJson);
            stmt.setInt(2, playlistId);
            stmt.executeUpdate();
        }
    }
}
