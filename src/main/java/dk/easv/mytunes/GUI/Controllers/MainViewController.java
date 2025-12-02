package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.PlaylistModel;
import dk.easv.mytunes.GUI.Models.SongModel;

// Java imports
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;

public class MainViewController implements Initializable {

    private PlaylistModel playlistModel;
    private SongModel songModel;

    // songs
    @FXML
    private TableView<Song> songList;
    @FXML
    private TableColumn colTitle;
    @FXML
    private TableColumn colArtist;
    @FXML
    private TableColumn colGenre;
    @FXML
    private TableColumn colTime;

    // playlists
    @FXML
    private TableView<Playlist> playlistView;
    @FXML
    private TableColumn colName;
    @FXML
    private TableColumn colSongs;
    @FXML
    private TableColumn colPlaylistTime;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {

        // songs table setup
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("Duration"));

        // playlist table setup
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSongs.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Playlist, Integer>, ObservableValue<Integer>>) cellData -> {
            Playlist playlist = cellData.getValue();
            int songCount = playlistModel.getSongCount(playlist);
            return new SimpleIntegerProperty(songCount).asObject();
        });
        colPlaylistTime.setCellValueFactory(new PropertyValueFactory<>("duration"));

        playlistView.setItems(playlistModel.getObservablePlaylists());
        songList.setItems(songModel.getObservableSongs());
      
        Platform.runLater(() -> {
            try {
                showRegisterPage();
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        });
    }
  
    public MainViewController() {
        try {
            playlistModel = new PlaylistModel();
            songModel = SongModel.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not instantiate MainViewModel");
        }
    }

    private void showRegisterPage() throws IOException {
        // Load the register view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
        Parent root = loader.load();
        RegisterViewController registerController = loader.getController();

        // Create and show register stage
        Stage registerStage = new Stage();
        registerStage.setTitle("Welcome to MyTunes");
        registerStage.initModality(Modality.APPLICATION_MODAL);
        registerStage.setScene(new Scene(root));
        registerStage.setResizable(false);
        registerStage.showAndWait();

        // Check if login was successful
        if (!registerController.isLoginSuccess()) {
            // User cancelled, close the application
            Platform.exit();
        }
    }

    @FXML
    private void onBtnClickPlayPause() { }

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

        Playlist selectedPlaylist = playlistView.getSelectionModel().getSelectedItem();

        if (selectedPlaylist == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Playlist Selected");
            alert.setContentText("Please select a playlist to delete.");
            alert.showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Playlist");
        confirmAlert.setHeaderText("Delete " + selectedPlaylist.getName() + "?");
        confirmAlert.setContentText("Are you sure you want to delete this playlist? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {

                    playlistModel.deletePlaylist(selectedPlaylist.getId());

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Playlist deleted successfully.");
                    successAlert.showAndWait();

                } catch (Exception e) {

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Could not delete playlist");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }


    @FXML
    private void onBtnClickAddToPlaylist() { }

    @FXML
    private void onBtnMoveSongUp() { }

    @FXML
    private void onBtnMoveSongDown() { }

    @FXML
    private void onBtnDeleteSongFromPlaylist() {  }

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

            //CSS
            //scene.getStylesheets().add(getClass().getResource("FilePath").toExternalForm());

            stage.showAndWait();

            songList.setItems(songModel.getObservableSongs());

            // Auto-select/-scroll to the newly added movie
            NewSongController controller = fxmlLoader.getController();
            if (controller.isSongAdded()) {
                try {
                    int newIndex = SongModel.getInstance().getObservableSongs().size() -1;
                    if (newIndex >= 0) {
                        songList.getSelectionModel().select(newIndex);
                        songList.scrollTo(newIndex);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onBtnEditSong() { }

    @FXML
    private void onBtnDeleteSong() {
        // Get the selected song from the table
        Song selectedSong = songList.getSelectionModel().getSelectedItem();

        // Check if a song is selected
        if (selectedSong == null) {
            // Show alert if no song is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Song Selected");
            alert.setContentText("Please select a song to delete.");
            alert.showAndWait();
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Song");
        confirmAlert.setHeaderText("Delete " + selectedSong.getTitle() + "?");
        confirmAlert.setContentText("Are you sure you want to delete this song? This action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete the song through the model
                    songModel.deleteSong(selectedSong);

                    // Optional: Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Song deleted successfully.");
                    successAlert.showAndWait();

                } catch (Exception e) {
                    // Show error alert
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Could not delete song");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void onBtnCloseProgram() {
        Platform.exit();
    }

    @FXML
    private void onBtnClickSearch() { }
}

