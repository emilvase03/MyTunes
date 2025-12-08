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
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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

    public Button moveSongUp;
    public Button moveSongDown;
    private PlaylistModel playlistModel;
    private SongModel songModel;
    private FilteredList<Song> filteredSongs;
    private boolean filterActive=false;

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
    @FXML
    private Button moveToPlaylist;

    // playlists
    @FXML
    private TableView<Playlist> playlistView;
    @FXML
    private TableColumn<Playlist, String> colName;
    @FXML
    private TableColumn<Playlist, Integer> colSongs;
    @FXML
    private TableColumn<Playlist, String> colPlaylistTime;
    @FXML
    private ListView<Song> songListInPlaylist;

    // playing songs
    @FXML
    private Button playPauseButton;
    @FXML
    private Slider volumeBar;
    @FXML
    private TextField songSearcherTxtField;
    @FXML
    private Label lblCurrentSong;
    @FXML
    private Button searchBtn;

    public MainViewController() {
        try {
            playlistModel = PlaylistModel.getInstance();
            songModel = SongModel.getInstance();
        } catch (Exception e) {
            showAlert("Error", "Could not get instance from PlaylistModel or SongModel", Alert.AlertType.ERROR);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        bindVolumeSlider();
        setupSearch();
        setupPlaylistSelection();
        setupPlaybackListeners();
        setupDoubleClickHandlers();
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

        filteredSongs = new FilteredList<>(songModel.getObservableSongs(), s -> true);
        songList.setItems(filteredSongs);
    }

    private void bindVolumeSlider() {
        volumeBar.setMin(0.0);
        volumeBar.setMax(1.0);
        volumeBar.setValue(playbackManager.getVolume());

        volumeBar.valueProperty().addListener((obs, oldVal, newVal) ->
                playbackManager.setVolume(newVal.doubleValue())
        );
    }

    private void setupSearch() {
        songSearcherTxtField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                filteredSongs.setPredicate(s -> true);
            }
        });

        moveToPlaylist.disableProperty().bind(
                songList.getSelectionModel().selectedItemProperty().isNull()
                        .or(playlistView.getSelectionModel().selectedItemProperty().isNull())
        );
    }

    private void setupPlaylistSelection() {
        playlistView.getSelectionModel().selectedItemProperty().addListener((obs, old, playlist) -> {
            if (playlist != null) {
                songListInPlaylist.setItems(
                        playlistModel.getObservableSongsInPlaylist(playlist)
                );
            } else {
                songListInPlaylist.setItems(null);
            }

            // update up/down buttons when playlist changes
            updateMoveButtonsState();
        });

        // when playlist list selection changes, update move button states
        songListInPlaylist.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            updateMoveButtonsState();
        });

        songListInPlaylist.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                setText(empty || song == null ? null : song.getTitle());
            }
        });
    }

    private void updateMoveButtonsState() {
        Playlist selectedPlaylist = getSelectedPlaylist();
        int idx = songListInPlaylist.getSelectionModel().getSelectedIndex();

        boolean disableUp = (selectedPlaylist == null) || (idx <= 0);
        boolean disableDown = (selectedPlaylist == null) || (idx < 0) || (idx >= songListInPlaylist.getItems().size() - 1);

        moveSongUp.setDisable(disableUp);
        moveSongDown.setDisable(disableDown);
    }

    private void setupPlaybackListeners() {
        playbackManager.playingProperty().addListener((obs, wasPlaying, isPlaying) ->
                updatePlaybackUI()
        );

        playbackManager.currentSongProperty().addListener((obs, oldSong, newSong) ->
                updatePlaybackUI()
        );
    }

    private void updatePlaybackUI() {
        Song currentSong = playbackManager.getCurrentSong();
        boolean isPlaying = playbackManager.isPlaying();

        playPauseButton.setText(isPlaying ? "Pause" : "Play");

        if (currentSong == null) {
            lblCurrentSong.setText("nothing is playing...");
            return;
        }

        String statusText = isPlaying ? " ... is playing" : " ... is paused";

        String playlistInfo = "";
        if (playbackManager.isPlayingPlaylistMode()) {
            int current = playbackManager.getCurrentIndex() + 1;
            int total = playbackManager.getCurrentPlaylist().size();
            playlistInfo = " [Playlist: " + current + "/" + total + "]";
        }

        lblCurrentSong.setText(currentSong.getTitle() + statusText + playlistInfo);
    }

    private void setupDoubleClickHandlers() {
        songList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song song = getSelectedSong();
                if (song != null) {
                    playbackManager.playSong(song);
                }
            }
        });

        playlistView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Playlist playlist = getSelectedPlaylist();
                if (playlist != null) {
                    playEntirePlaylist();
                }
            }
        });

        songListInPlaylist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedIndex = songListInPlaylist.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    playSongFromPlaylistAtIndex(selectedIndex);
                }
            }
        });
    }

    @FXML
    private void onBtnClickPlayPause() {
        int selectedPlaylistSongIndex = songListInPlaylist.getSelectionModel().getSelectedIndex();
        Playlist selectedPlaylist = getSelectedPlaylist();
        Song selectedSong = getSelectedSong();

        // priority order:
        // 1. if a song is selected in playlist view -> play that song from playlist at that index
        if (selectedPlaylistSongIndex >= 0) {
            playSongFromPlaylistAtIndex(selectedPlaylistSongIndex);

            // 2. if a playlist is selected and has songs -> play entire playlist
        } else if (selectedPlaylist != null && playlistModel.getSongCount(selectedPlaylist) > 0) {
            playEntirePlaylist();

            // 3. if a song is selected in the main table -> play as single song
        } else if (selectedSong != null) {
            // this will now toggle pause if the song is already playing
            playbackManager.playSong(selectedSong);

            // 4. otherwise -> toggle current playback
        } else {
            playbackManager.togglePlayPause();
        }
    }

    @FXML
    private void onBtnClickPreviousSong() {
        playbackManager.prev();
    }

    @FXML
    private void onBtnClickNextSong() {
        playbackManager.next();
    }

    @FXML
    private void onBtnClickNewPlaylist() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/NewPlaylistView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("New Playlist");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            NewPlaylistController controller = fxmlLoader.getController();
            if (controller.isPlaylistAdded()) {
                int newIndex = playlistModel.getObservablePlaylists().size() - 1;
                if (newIndex >= 0) {
                    playlistView.getSelectionModel().select(newIndex);
                    playlistView.scrollTo(newIndex);
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Could not open New Playlist window:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnClickEditPlaylist() {
        Playlist selectedPlaylist = getSelectedPlaylist();

        if (selectedPlaylist == null) {
            showAlert("No Selection", "Please select a playlist to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditPlaylistView.fxml"));
            Parent root = fxmlLoader.load();

            EditPlaylistViewController controller = fxmlLoader.getController();
            controller.setData(selectedPlaylist, playlistModel);

            Stage stage = new Stage();
            stage.setTitle("Edit Playlist");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            playlistView.refresh();

        } catch (IOException e) {
            showAlert("Error", "Could not open Edit Playlist window:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

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
        }
    }

    @FXML
    private void onBtnClickAddToPlaylist() {
        Playlist selectedPlaylist = getSelectedPlaylist();
        Song selectedSong = getSelectedSong();

        if (selectedPlaylist == null) {
            showAlert("No Playlist Selected", "Please select a playlist first.", Alert.AlertType.WARNING);
            return;
        }

        if (selectedSong == null) {
            showAlert("No Song Selected", "Please select a song to add.", Alert.AlertType.WARNING);
            return;
        }

        // --- Check for duplicates ---
        boolean alreadyInPlaylist = playlistModel.getObservableSongsInPlaylist(selectedPlaylist)
                .stream()
                .anyMatch(song -> song.getId() == selectedSong.getId());

        if (alreadyInPlaylist) {
            showAlert("Duplicate Song",
                    "\"" + selectedSong.getTitle() + "\" is already in the playlist \"" + selectedPlaylist.getName() + "\".",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            playlistModel.addSongToPlaylist(selectedPlaylist, selectedSong);

            playlistView.refresh();
            songListInPlaylist.setItems(
                    playlistModel.getObservableSongsInPlaylist(selectedPlaylist)
            );

            showAlert("Success",
                    "\"" + selectedSong.getTitle() + "\" added to playlist \"" + selectedPlaylist.getName() + "\".",
                    Alert.AlertType.INFORMATION
            );

        } catch (Exception e) {
            showAlert("Error", "Could not add song:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnMoveSongUp() {
        Playlist playlist = getSelectedPlaylist();
        int index = songListInPlaylist.getSelectionModel().getSelectedIndex();

        if (playlist == null || index <= 0) return;

        try {
            playlistModel.moveSongUp(playlist, index);

            // refresh UI list and keep moved item selected
            songListInPlaylist.setItems(playlistModel.getObservableSongsInPlaylist(playlist));
            int newIndex = Math.max(0, index - 1);
            songListInPlaylist.getSelectionModel().select(newIndex);
            songListInPlaylist.scrollTo(newIndex);

            playlistView.refresh();
            updateMoveButtonsState();

        } catch (Exception e) {
            showAlert("Error", "Could not move song:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnMoveSongDown() {
        Playlist playlist = getSelectedPlaylist();
        int index = songListInPlaylist.getSelectionModel().getSelectedIndex();

        if (playlist == null) return;

        try {
            playlistModel.moveSongDown(playlist, index);

            // refresh UI list and keep moved item selected
            songListInPlaylist.setItems(playlistModel.getObservableSongsInPlaylist(playlist));
            int newIndex = Math.min(songListInPlaylist.getItems().size() - 1, index + 1);
            songListInPlaylist.getSelectionModel().select(newIndex);
            songListInPlaylist.scrollTo(newIndex);

            playlistView.refresh();
            updateMoveButtonsState();

        } catch (Exception e) {
            showAlert("Error", "Could not move song:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onBtnDeleteSongFromPlaylist() {
        Playlist selectedPlaylist = getSelectedPlaylist();
        Song selectedSongInPlaylist = songListInPlaylist.getSelectionModel().getSelectedItem();

        if (selectedPlaylist == null) {
            showAlert("No Playlist Selected", "Please select a playlist first.", Alert.AlertType.WARNING);
            return;
        }

        if (selectedSongInPlaylist == null) {
            showAlert("No Song Selected", "Please select a song from the playlist to remove.", Alert.AlertType.WARNING);
            return;
        }

        boolean confirmed = showConfirmation(
                "Remove Song",
                "Remove \"" + selectedSongInPlaylist.getTitle() + "\" from \"" + selectedPlaylist.getName() + "\"?"
        );

        if (!confirmed) return;

        try {
            playlistModel.removeSongFromPlaylist(selectedPlaylist.getId(), selectedSongInPlaylist.getId());

            playlistView.refresh();
            songListInPlaylist.setItems(
                    playlistModel.getObservableSongsInPlaylist(selectedPlaylist)
            );

            showAlert("Success",
                    "\"" + selectedSongInPlaylist.getTitle() + "\" removed from playlist.",
                    Alert.AlertType.INFORMATION
            );

        } catch (Exception e) {
            showAlert("Error", "Could not remove song:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

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
        Song selectedSong = getSelectedSong();

        if (selectedSong == null) {
            showAlert("No Selection", "Please select a song to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/EditSongView.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            EditSongViewController controller = fxmlLoader.getController();
            controller.setSong(selectedSong);

            Stage stage = new Stage();
            stage.setTitle("Edit Song");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.showAndWait();

            songList.refresh();

        } catch (Exception e) {
            showAlert("Error", "Could not open Edit Song window:\n" + e.getMessage(), Alert.AlertType.ERROR);
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
            showAlert("Cannot Delete", "Please stop playback before deleting this song.", Alert.AlertType.WARNING);
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
        }
    }

    @FXML
    private void onBtnCloseProgram() {
        playbackManager.stop();
        Platform.exit();
    }

    @FXML
    private void onBtnClickSearch() {
        if (!filterActive) {
            String q = songSearcherTxtField.getText();
            final String query = (q == null) ? "" : q.trim().toLowerCase();

            if (query.isEmpty()) {
                if (filteredSongs != null) {
                    filteredSongs.setPredicate(s -> true);
                }

                filterActive = false;
                if (searchBtn != null) searchBtn.setText("Search");
                return;
            }

            if (filteredSongs != null) {
                filteredSongs.setPredicate(song -> {
                    String title  = song.getTitle()  == null ? "" : song.getTitle().toLowerCase();
                    String artist = song.getArtist() == null ? "" : song.getArtist().toLowerCase();


                    return title.contains(query) || artist.contains(query);
                });
            }

            // Switch button to "Clear"
            filterActive = true;
            if (searchBtn != null) searchBtn.setText("Clear");

        } else {

            if (filteredSongs != null) {
                filteredSongs.setPredicate(s -> true);  // show all songs
            }
            filterActive = false;
            if (searchBtn != null) searchBtn.setText("Search");
            if (songSearcherTxtField != null) songSearcherTxtField.clear();
        }
    }


    private void playEntirePlaylist() {
        Playlist playlist = getSelectedPlaylist();
        if (playlist == null) return;

        try {
            ObservableList<Song> songs = playlistModel.getObservableSongsInPlaylist(playlist);
            if (songs.isEmpty()) {
                showAlert("Empty Playlist", "This playlist has no songs.", Alert.AlertType.INFORMATION);
                return;
            }

            playbackManager.playPlaylist(songs);

        } catch (Exception e) {
            showAlert("Error", "Could not play playlist:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void playSongFromPlaylistAtIndex(int index) {
        Playlist playlist = getSelectedPlaylist();
        if (playlist == null || index < 0) return;

        try {
            ObservableList<Song> songs = playlistModel.getObservableSongsInPlaylist(playlist);
            if (songs.isEmpty() || index >= songs.size()) {
                return;
            }

            Song targetSong = songs.get(index);

            List<Song> reorderedList = new ArrayList<>();

            for (int i = index; i < songs.size(); i++) {
                reorderedList.add(songs.get(i));
            }
            for (int i = 0; i < index; i++) {
                reorderedList.add(songs.get(i));
            }

            playbackManager.playSongFromPlaylist(reorderedList, targetSong);

        } catch (Exception e) {
            showAlert("Error", "Could not play song:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Song getSelectedSong() {
        return songList.getSelectionModel().getSelectedItem();
    }

    private Playlist getSelectedPlaylist() {
        return playlistView.getSelectionModel().getSelectedItem();
    }

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
