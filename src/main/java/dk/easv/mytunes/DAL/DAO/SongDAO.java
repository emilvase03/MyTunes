package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.ISongDataAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            String sql = "SELECT * FROM dbo.Song;";
            ResultSet rs = stmt.executeQuery(sql);

            // Loop through rows from the database result set
            while (rs.next()) {

                //Map DB row to Movie object

                String title = rs.getString("Title");
                String artist=rs.getString("Artist");
                String genre = rs.getString("Genre");
                String duration=rs.getString("Duration");

                Song song = new Song(title, artist, genre,duration);
                allSongs.add(song);
            }
            return allSongs;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get songs from database", ex);
        }
    }

}
