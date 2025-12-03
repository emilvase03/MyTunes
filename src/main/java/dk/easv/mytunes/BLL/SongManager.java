package dk.easv.mytunes.BLL;

// Project imports
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.BLL.UTIL.SongSearcher;
import dk.easv.mytunes.DAL.DAO.SongDAO;
import dk.easv.mytunes.DAL.ISongDataAccess;

// Java imports
import java.io.IOException;
import java.util.List;

public class SongManager {
    private SongSearcher songSearcher = new SongSearcher();
    private ISongDataAccess songDAO;

    public SongManager() throws IOException {
        songDAO = new SongDAO();
    }
    public List<Song> searchSongs(String query) throws Exception {// retrieves all movies from data source.
        // Throws exception in case something goes wrong
        List<Song> allSongs = getAllSongs();//get all movies into a list
        List<Song> searchResult = songSearcher.search(allSongs, query);//filter movies based on query string
        return searchResult;
    }

    /**
     * @return allSongs
     * @throws Exception
     */
    public List<Song> getAllSongs() throws Exception {
        return songDAO.getAllSongs();
    }

    /**
     * Method for creating a song down through the layers
     * @param newSong
     * @return
     */
    public Song createSong(Song newSong) throws Exception {
        Song songCreated = songDAO.createSong(newSong);
        return songCreated;
    }

    /**
     * Method for updating a song down through the layers
     * @param songToBeUpdated
     * @throws Exception
     */
    public void updateSong(Song songToBeUpdated) throws Exception {
        songDAO.updateSong(songToBeUpdated);
    }

    /**
     *  Method for deleting a song down through the layers
     * @param selectedSong
     * @throws Exception
     */
    public void deleteSong(Song selectedSong) throws Exception {
        songDAO.deleteSong(selectedSong);
    }

}
