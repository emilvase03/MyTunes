package dk.easv.mytunes.BLL;

// Project imports
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.DAL.DAO.SongDAO;
import dk.easv.mytunes.DAL.ISongDataAccess;

// Java imports
import java.io.IOException;
import java.util.List;

public class SongManager {

    private ISongDataAccess songDAO;

    public SongManager() throws IOException {
        songDAO = new SongDAO();
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
