package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.ISongDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO implements ISongDataAccess {
    private DBConnector databaseConnector;

    public SongDAO() throws IOException {//introduces the path to database,where we get connection from
        databaseConnector = new DBConnector();
    }

    public List<Song> getAllSongs() throws Exception {
        ArrayList<Song> allSongs = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection();//try with resources.The connection should be closed after so it is in () with try.
             Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM dbo.songs;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {

                //Map DB row to Movie object
                int id=rs.getInt("id");
                String title = rs.getString("Title");
                String artist=rs.getString("Artist");
                String genre = rs.getString("Genre");
                String duration=rs.getString("Duration");

                Song song = new Song(id,title, artist, genre,duration);
                allSongs.add(song);
            }
            return allSongs;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get songs from database", ex);
        }
    }
    @Override
    public Song createSong(Song newSong) throws Exception {
        String sql = "INSERT INTO dbo.songs (id,title,artist,genre,duration) VALUES (?,?,?,?,?);";

        // try-with-resources makes sure we close db connection etc.
        try (Connection conn = databaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Bind parameters
            stmt.setInt   (1, newSong.getId());
            stmt.setString(2, newSong.getTitle());
            stmt.setString(3, newSong.getArtist());
            stmt.setString(4, newSong.getGenre());
            stmt.setString(5, newSong.getDuration());

            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from the DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = 0;

            if (rs.next()) {
                id = rs.getInt(1);
            }


            Song createdSong = new Song(id, newSong.getTitle(), newSong.getArtist(),newSong.getGenre(),newSong.getDuration());


            return createdSong;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not create song", ex);
        }
    }


    @Override
    public void updateSong(Song song) throws Exception {
        String sql = "UPDATE dbo.songs SET title = ?,artist = ?,genre = ?,duration = ? WHERE id = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Bind parameters
            stmt.setString(1, song.getTitle());
            stmt.setString(2, song.getArtist());
            stmt.setString(3, song.getGenre());
            stmt.setString(4,song.getDuration());
            stmt.setInt   (5,song.getId());




            // Run the specified SQL statement
            stmt.executeUpdate();
        } catch (SQLException ex) {

            throw new Exception("Could not get movies from database.", ex);
        }
    }


    @Override
    public void deleteSong(Song song) throws Exception {
        // SQL command
        String sql = "DELETE FROM dbo.songs WHERE id = ?;";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, song.getId());

            // Run the specified SQL statement
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Could not get movies from database.", ex);

        }

}}
