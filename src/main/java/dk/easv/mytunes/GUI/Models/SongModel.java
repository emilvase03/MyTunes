package dk.easv.mytunes.GUI.Models;

// Project imports
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.BLL.SongManager;

// Java imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class SongModel {

    private SongManager songManager = new SongManager();
    private ObservableList<Song> songsToBeViewed;


    public SongModel() throws Exception {
        songsToBeViewed = FXCollections.observableArrayList();
        songsToBeViewed.addAll(songManager.getAllSongs());

    }

    public ObservableList<Song> getObservableSongs() {
        return songsToBeViewed;
    }

    /**
     * Method for creating a song down through the layers
     * @param newSong
     * @return
     */
    public Song createSong(Song newSong) throws Exception {
        Song songCreated = songManager.createSong(newSong);
        getObservableSongs().add(songCreated);
        return songCreated;
    }

    /**
     * Method for updating a song down through the layers
     * @param songToBeUpdated
     * @throws Exception
     */
    public void updateSong(Song songToBeUpdated) throws Exception {
        // update song in DAL layer (Through the layers)
        songManager.updateSong(songToBeUpdated);

        int index = songsToBeViewed.indexOf(songsToBeViewed);
        songsToBeViewed.set(index, songToBeUpdated);
    }

    /**
     * Method for deleting a song down through the layers
     * @param selectedSong
     * @throws Exception
     */
    public void deleteSong(Song selectedSong) throws Exception {
        // Removes song in DAL layer (through the layers)
        songManager.deleteSong(selectedSong);

        // Update observable list
        songsToBeViewed.remove(selectedSong);
    }


}
