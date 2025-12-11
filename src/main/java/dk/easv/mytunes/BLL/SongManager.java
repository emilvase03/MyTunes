package dk.easv.mytunes.BLL;

// Project imports
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.BLL.UTIL.SongSearcher;
import dk.easv.mytunes.DAL.DAO.SongDAO;
import dk.easv.mytunes.DAL.ISongDataAccess;

// Java imports
import java.util.List;

public class SongManager {
    private SongSearcher songSearcher = new SongSearcher();
    private ISongDataAccess songDAO;

    public SongManager() throws Exception {
        songDAO = new SongDAO();
    }
    public List<Song> searchSongs(String query) throws Exception {
        List<Song> allSongs = getAllSongs();
        return songSearcher.search(allSongs, query);
    }

    public List<Song> getAllSongs() throws Exception {
        return songDAO.getAllSongs();
    }

    public Song createSong(Song newSong) throws Exception {
        return songDAO.createSong(newSong);
    }

    public void updateSong(Song songToBeUpdated) throws Exception {
        songDAO.updateSong(songToBeUpdated);
    }

    public void deleteSong(Song selectedSong) throws Exception {
        songDAO.deleteSong(selectedSong);
    }

}
