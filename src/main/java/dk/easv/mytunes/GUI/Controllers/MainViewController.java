package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.BLL.UTIL.SongSearcher;
import dk.easv.mytunes.GUI.Models.PlaylistModel;
import dk.easv.mytunes.GUI.Models.SongModel;
import dk.easv.mytunes.GUI.UTIL.PlaybackManager;

// Java imports
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    private PlaylistModel playlistModel;
    private SongModel songModel;
    private FilteredList<Song> filteredSongs;

    private final PlaybackManager playbackManager = new PlaybackManager();

    // songs
    @FXML
    private TableView<Song> songList;
    @FXML
    private TableColumn<Song, String> colTitle;
    @FXML
    private TableColumn<Song, String> colArtist;
    @FXML
    private TableColumn<Song, String> colGenre;
    @FXML
    private TableColumn<Song, String> colTime;

    // playlists
    @FXML
    private TableView<Playlist> playlistView;
    @FXML
    private TableColumn<Playlist, String> colName;
    @FXML
    private TableColumn<Playlist, Integer> colSongs;
    @FXML
    private TableColumn<Playlist, String> colPlaylistTime;

    // playing songs
    @FXML
    private Button playPauseButton;
    @FXML
    private Slider volumeBar;
    @FXML
    private TextField songSearcherTxtField;
    private Label lblCurrentSong;

    public MainViewController() {
        try {
            playlistModel = new PlaylistModel();
            songModel = SongModel.getInstance();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not instantiate MainViewModel");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setupTables();
        bindVolumeSlider();
 //
       filteredSongs = new FilteredList<>(songModel.getObservableSongs(), s -> true);
        songList.setItems(filteredSongs);

//Only react when search becomes empty -> show all automatically


        songSearcherTxtField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                filteredSongs.setPredicate(s -> true); // show everything
            }

        });



        // handle playback UI updates
        playbackManager.playingProperty().addListener((obs, oldVal, newVal) -> {
            playPauseButton.setText(newVal ? "Pause" : "Play");

            Song song = playbackManager.getCurrentSong();
            if (song == null) {
                lblCurrentSong.setText("nothing is playing..");
                return;
            }

            lblCurrentSong.setText(
                    song.getTitle() + (newVal ? " ... is playing" : " ... is paused")
            );
        });

        // double click to play
        songList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song song = getSelectedSong();
                if (song != null) {
                    playbackManager.playSong(song);
                }
            }
        });
    }

    private void setupTables() {
        // songs table
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("duration"));

        // playlists table
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSongs.setCellValueFactory(cellData -> {
            Playlist playlist = cellData.getValue();
            int songCount = playlistModel.getSongCount(playlist);
            return new SimpleIntegerProperty(songCount).asObject();
        });
        colPlaylistTime.setCellValueFactory(new PropertyValueFactory<>("duration"));

        playlistView.setItems(playlistModel.getObservablePlaylists());


       // songList.setItems(songModel.getObservableSongs());
    }

    private void bindVolumeSlider() {
        volumeBar.setMin(0.0);
        volumeBar.setMax(1.0);
        volumeBar.setValue(playbackManager.getVolume());

        volumeBar.valueProperty().addListener((obs, oldVal, newVal) ->
                playbackManager.setVolume(newVal.doubleValue())
        );
    }

    // button actions

    @FXML
    private void onBtnClickPlayPause() {
        if (playlistSelected()) {
            // play playlist
        } else if (songSelected()) {
            playbackManager.playSong(getSelectedSong());
        } else {
            playbackManager.togglePause();
        }
    }

    @FXML
    private void onBtnClickPreviousSong() { }

    @FXML
    private void onBtnClickNextSong() { }

    @FXML
    private void onBtnClickNewPlaylist() { }

    @FXML
    private void onBtnClickEditPlaylist() { }

    @FXML
    private void onBtnClickDeletePlaylist() {

        Playlist selectedPlaylist = getSelectedPlaylist();

        if (selectedPlaylist == null) {
            showAlert("No Selection", "Please select a playlist to delete.", Alert.AlertType.WARNING);
            return;
        }

        boolean confirmed = showConfirmation(
                "Delete Playlist",
                "Are you sure you want to delete \"" + selectedPlaylist.getName() + "\"?"
        );

        if (!confirmed) return;

        try {
            playlistModel.deletePlaylist(selectedPlaylist.getId());
            showAlert("Success", "Playlist deleted successfully.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Could not delete playlist:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void onBtnClickAddToPlaylist() { }

    @FXML
    private void onBtnMoveSongUp() { }

    @FXML
    private void onBtnMoveSongDown() { }

    @FXML
    private void onBtnDeleteSongFromPlaylist() { }

    @FXML
    private void onBtnAddSong() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/NewSongView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("New Song");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            songList.setItems(songModel.getObservableSongs());

            NewSongController controller = fxmlLoader.getController();
            if (controller.isSongAdded()) {
                int newIndex = songModel.getObservableSongs().size() - 1;
                if (newIndex >= 0) {
                    songList.getSelectionModel().select(newIndex);
                    songList.scrollTo(newIndex);
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Could not load window:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnEditSong() {

        Song selectedSong = songList.getSelectionModel().getSelectedItem();

        if (selectedSong == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Song Selected");
            alert.setContentText("Please select a song to edit.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSongView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Edit Song");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);


            songList.setItems(songModel.getObservableSongs());

            EditSongController controller = fxmlLoader.getController();
            controller.setSong(selectedSong);


            stage.showAndWait();

            // Refresh table after editing
            songList.setItems(songModel.getObservableSongs());


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

    }

    @FXML
    private void onBtnDeleteSong() {
        Song selectedSong = getSelectedSong();

        if (selectedSong == null) {
            showAlert("No Selection", "Please select a song to delete.", Alert.AlertType.WARNING);
            return;
        }

        if (playbackManager.isCurrentSong(selectedSong)) {
            showAlert("Cannot Delete", "Please stop playback before deleting.", Alert.AlertType.WARNING);
            return;
        }

        boolean confirmed = showConfirmation(
                "Delete Song",
                "Are you sure you want to delete \"" + selectedSong.getTitle() + "\"?"
        );

        if (!confirmed) return;

        try {
            songModel.deleteSong(selectedSong);
            showAlert("Success", "Song deleted successfully.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Could not delete song:\n" + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void onBtnCloseProgram() {
        Platform.exit();
    }

    @FXML
    private void onBtnClickSearch() {

        String query = songSearcherTxtField.getText();

        String q = songSearcherTxtField.getText();

        if (q != null && !q.trim().isEmpty()) {
            final String qu = q.trim().toLowerCase();

            // Apply filtering when the button is pressed
            filteredSongs.setPredicate(song -> {
                String title = song.getTitle() == null ? "" : song.getTitle().toLowerCase();
                String artist = song.getArtist() == null ? "" : song.getArtist().toLowerCase();
                String genre = song.getGenre() == null ? "" : song.getGenre().toLowerCase();
                String durStr = song.getDuration() == null ? "" : song.getDuration().toString();


            //Match  fields
                return title.contains(query)
                        || artist.contains(query)
                        || genre.contains(query)
                        || durStr.contains(query);
            });
        } else {
            // Search pressed with empty text -> show all

            filteredSongs.setPredicate(s -> true);
        }


                    }

    // helpers

    private boolean songSelected() {
        return songList.getSelectionModel().getSelectedItem() != null;
    }

    private boolean playlistSelected() {
        return playlistView.getSelectionModel().getSelectedItem() != null;
    }

    private Song getSelectedSong() {
        return songList.getSelectionModel().getSelectedItem();
    }

    private Playlist getSelectedPlaylist() {
        return playlistView.getSelectionModel().getSelectedItem();
    }

    // alert helpers

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }
}
