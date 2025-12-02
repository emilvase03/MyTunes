package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.PlaylistModel;
import dk.easv.mytunes.GUI.Models.SongModel;
import dk.easv.mytunes.GUI.UTIL.PlaybackManager;

// Java imports
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    private PlaylistModel playlistModel;
    private SongModel songModel;

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

        playbackManager.playingProperty().addListener((obs, wasPlaying, isPlaying) ->
                Platform.runLater(() -> playPauseButton.setText(isPlaying ? "Pause" : "Play"))
        );
        playPauseButton.setText("Play");

        songList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song song = getSelectedSong();
                if (song != null) {
                    playbackManager.playSong(song);
                }
            }
        });

        Platform.runLater(() -> {
            try {
                showRegisterPage();
            } catch (IOException e) {
                e.printStackTrace();
                Platform.exit();
            }
        });
    }

    private void setupTables() {
        // songs table
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("Duration"));

        // playlists table
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSongs.setCellValueFactory(cellData -> {
            Playlist playlist = cellData.getValue();
            int songCount = playlistModel.getSongCount(playlist);
            return new SimpleIntegerProperty(songCount).asObject();
        });
        colPlaylistTime.setCellValueFactory(new PropertyValueFactory<>("duration"));

        playlistView.setItems(playlistModel.getObservablePlaylists());
        songList.setItems(songModel.getObservableSongs());
    }

    private void bindVolumeSlider() {
        volumeBar.setMin(0.0);
        volumeBar.setMax(1.0);
        volumeBar.setValue(playbackManager.getVolume());

        volumeBar.valueProperty().addListener((obs, oldVal, newVal) ->
                playbackManager.setVolume(newVal.doubleValue())
        );
    }

    private void showRegisterPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
        Parent root = loader.load();
        RegisterViewController registerController = loader.getController();

        Stage registerStage = new Stage();
        registerStage.setTitle("Welcome to MyTunes");
        registerStage.initModality(Modality.APPLICATION_MODAL);
        registerStage.setScene(new Scene(root));
        registerStage.setResizable(false);
        registerStage.showAndWait();

        if (!registerController.isLoginSuccess()) {
            Platform.exit();
        }
    }

    @FXML
    private void onBtnClickPlayPause() {
        if (playlistSelected()) {
            // playbackManager.playPlaylist(getSelectedPlaylist());
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
                try {
                    int newIndex = SongModel.getInstance().getObservableSongs().size() - 1;
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
    private void onBtnDeleteSong() { }

    @FXML
    private void onBtnCloseProgram() {
        Platform.exit();
    }

    @FXML
    private void onBtnClickSearch() { }

    // Helper methods
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
}
