package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.MainViewModel;
import dk.easv.mytunes.GUI.Models.PlaylistModel;

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

import java.io.IOException;

public class MainViewController implements Initializable {

    private MainViewModel mainViewModel;
    private PlaylistModel playlistModel;
    private String currentUser;

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

        songList.setItems(mainViewModel.getObservableSongs());

        // playlist table setup
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSongs.setCellValueFactory((Callback<TableColumn.CellDataFeatures<Playlist, Integer>, ObservableValue<Integer>>) cellData -> {
            Playlist playlist = cellData.getValue();
            int songCount = playlistModel.getSongCount(playlist);
            return new SimpleIntegerProperty(songCount).asObject();
        });
        colPlaylistTime.setCellValueFactory(new PropertyValueFactory<>("duration"));

        playlistView.setItems(playlistModel.getObservablePlaylists());
      
        // Show register page when main view initializes
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
            mainViewModel = new MainViewModel();
            playlistModel = new PlaylistModel();
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
        if (registerController.isLoginSuccess()) {
            currentUser = registerController.getCurrentUser();
            System.out.println("User logged in: " + currentUser);
        } else {
            // User cancelled, close the application
            Platform.exit();
        }
    }

    public String getCurrentUser() {
        return currentUser;
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
    private void onBtnClickDeletePlaylist() { }

    @FXML
    private void onBtnClickAddToPlaylist() { }

    @FXML
    private void onBtnMoveSongUp() { }

    @FXML
    private void onBtnMoveSongDown() { }

    @FXML
    private void onBtnDeleteSongFromPlaylist() { }

    @FXML
    private void onBtnAddSong() { }

    @FXML
    private void onBtnEditSong() { }

    @FXML
    private void onBtnDeleteSong() { }

    @FXML
    private void onBtnCloseProgram() {
        Platform.exit();
    }

    @FXML
    private void onBtnClickSearch() { }
}

