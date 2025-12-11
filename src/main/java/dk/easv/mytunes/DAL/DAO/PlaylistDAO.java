package dk.easv.mytunes.DAL.DAO;

// Project imports
import dk.easv.mytunes.BE.CurrentUser;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.IPlaylistDataAccess;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO implements IPlaylistDataAccess {

    private final DBConnector db = new DBConnector();

    public PlaylistDAO() throws IOException {}

    @Override
    public List<Playlist> getAllPlaylists() throws Exception {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT id, user_id, name, song_filepaths, duration " +
                "FROM playlists WHERE user_id = ? ORDER BY name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, CurrentUser.getInstance().getCurrentUser().getId());
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
             PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        String sql = "UPDATE playlists SET name = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playlist.getName());
            stmt.setInt(2, playlist.getId());
            stmt.setInt(3, playlist.getUserId());

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

            // convert json to list of filepaths (simple parsing to match existing behaviour)
            json = json.replace("[", "").replace("]", "").replace("\"", "");
            String[] filepaths = json.split(",");

            String songQuery = "SELECT id, user_id, filepath, title, artist, genre, duration " +
                    "FROM songs WHERE filepath = ?";

            for (String path : filepaths) {
                path = path.trim();
                if (path.isEmpty()) continue;

                try (PreparedStatement sStmt = conn.prepareStatement(songQuery)) {
                    sStmt.setString(1, path);
                    ResultSet srs = sStmt.executeQuery();

                    if (srs.next()) {
                        Song s = new Song(
                                srs.getInt("id"),
                                srs.getInt("user_id"),
                                srs.getString("filepath"),
                                srs.getString("title"),
                                srs.getString("artist"),
                                srs.getString("genre"),
                                srs.getString("duration")
                        );
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

        updatePlaylistJson(playlistId, filepath, true);
        updatePlaylistDuration(playlistId);
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

        updatePlaylistJson(playlistId, filepath, false);
        updatePlaylistDuration(playlistId);
    }

    // public method to update the json filepaths directly (used for reordering)
    public void updatePlaylistFilepaths(int playlistId, List<String> filepaths) throws Exception {
        if (filepaths == null) filepaths = new ArrayList<>();

        // build json string: ["p1","p2","p3"]
        String newJson;
        if (filepaths.isEmpty()) {
            newJson = "[]";
        } else {
            // escape any existing double-quotes in filepaths (basic)
            List<String> escaped = new ArrayList<>();
            for (String fp : filepaths) {
                if (fp == null) fp = "";
                String safe = fp.replace("\"", "\\\"");
                escaped.add(safe);
            }
            newJson = "[\"" + String.join("\",\"", escaped) + "\"]";
        }

        String update = "UPDATE playlists SET song_filepaths = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {

            stmt.setString(1, newJson);
            stmt.setInt(2, playlistId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new Exception("Could not update playlist filepaths", e);
        }

        // update duration after filepaths changed
        updatePlaylistDuration(playlistId);
    }

    // helper used by add/removeSongFromPlaylist
    private void updatePlaylistJson(int playlistId, String filepath, boolean add) throws Exception {
        String sql = "SELECT song_filepaths FROM playlists WHERE id = ?";
        String json = "[]";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) json = rs.getString("song_filepaths");
        }

        // convert json to list
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

        // back to json
        String newJson = "[\"" + String.join("\",\"", filepaths) + "\"]";

        String update = "UPDATE playlists SET song_filepaths = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(update)) {

            stmt.setString(1, newJson);
            stmt.setInt(2, playlistId);
            stmt.executeUpdate();
        }
    }

    private void updatePlaylistDuration(int playlistId) throws Exception {
        List<Song> songs = getSongsInPlaylist(playlistId);

        int totalSeconds = 0;

        for (Song song : songs) {
            totalSeconds += parseDurationToSeconds(song.getDuration());
        }

        String totalDuration = formatSecondsToTime(totalSeconds);

        String sql = "UPDATE playlists SET duration = ? WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, totalDuration);
            stmt.setInt(2, playlistId);
            stmt.executeUpdate();
        }
    }

    private int parseDurationToSeconds(String duration) {
        if (duration == null || duration.isBlank()) {
            return 0;
        }

        String[] parts = duration.split(":");
        if (parts.length < 2 || parts.length > 3) {
            return 0;
        }

        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].trim().matches("\\d+")) {
                return 0;
            }
        }

        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            nums[i] = Integer.parseInt(parts[i].trim());
        }

        if (nums.length == 2) {
            // mm:ss
            return nums[0] * 60 + nums[1];
        } else {
            // hh:mm:ss
            return nums[0] * 3600 + nums[1] * 60 + nums[2];
        }
    }

    private String formatSecondsToTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
