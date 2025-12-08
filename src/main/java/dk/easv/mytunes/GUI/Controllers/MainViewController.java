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

    private final PlaybackManager playbackManager = new PlaybackManager();
    private PlaylistModel playlistModel;
    private SongModel songModel;
    private FilteredList<Song> filteredSongs;
    private boolean filterActive = false;

    @FXML private TableView<Song> songList;
    @FXML private TableColumn<Song, String> colTitle;
    @FXML private TableColumn<Song, String> colArtist;
    @FXML private TableColumn<Song, String> colGenre;
    @FXML private TableColumn<Song, String> colTime;
    @FXML private Button moveToPlaylist;
    @FXML private TableView<Playlist> playlistView;
    @FXML private TableColumn<Playlist, String> colName;
    @FXML private TableColumn<Playlist, Integer> colSongs;
    @FXML private TableColumn<Playlist, String> colPlaylistTime;
    @FXML private ListView<Song> songListInPlaylist;
    @FXML private Button playPauseButton;
    @FXML private Slider volumeBar;
    @FXML private TextField songSearcherTxtField;
    @FXML private Label lblCurrentSong;
    @FXML private Button searchBtn;
    @FXML private Button moveSongUp;
    @FXML private Button moveSongDown;

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
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("duration"));

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
            if (filterActive && isEmptyOrNull(newText)) {
                resetFilter();
            } else if (!filterActive && isEmptyOrNull(newText)) {
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
                songListInPlaylist.setItems(playlistModel.getObservableSongsInPlaylist(playlist));
            } else {
                songListInPlaylist.setItems(null);
            }
            updateMoveButtonsState();
        });

        songListInPlaylist.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) ->
                updateMoveButtonsState()
        );

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
        int size = songListInPlaylist.getItems().size();

        moveSongUp.setDisable(selectedPlaylist == null || idx <= 0);
        moveSongDown.setDisable(selectedPlaylist == null || idx < 0 || idx >= size - 1);
    }

    private void setupPlaybackListeners() {
        playbackManager.playingProperty().addListener((obs, wasPlaying, isPlaying) -> updatePlaybackUI());
        playbackManager.currentSongProperty().addListener((obs, oldSong, newSong) -> updatePlaybackUI());
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

        if (selectedPlaylistSongIndex >= 0) {
            playSongFromPlaylistAtIndex(selectedPlaylistSongIndex);
        } else if (selectedPlaylist != null && playlistModel.getSongCount(selectedPlaylist) > 0) {
            playEntirePlaylist();
        } else if (selectedSong != null) {
            playbackManager.playSong(selectedSong);
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
                selectAndScrollToLastItem(playlistView, playlistModel.getObservablePlaylists());
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

        if (!showConfirmation("Delete Playlist",
                "Are you sure you want to delete \"" + selectedPlaylist.getName() + "\"?")) {
            return;
        }

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
            songListInPlaylist.setItems(playlistModel.getObservableSongsInPlaylist(selectedPlaylist));
            showAlert("Success",
                    "\"" + selectedSong.getTitle() + "\" added to playlist \"" + selectedPlaylist.getName() + "\".",
                    Alert.AlertType.INFORMATION);
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
            refreshPlaylistSongList(playlist, index - 1);
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
            refreshPlaylistSongList(playlist, index + 1);
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

        if (!showConfirmation("Remove Song",
                "Remove \"" + selectedSongInPlaylist.getTitle() + "\" from \"" + selectedPlaylist.getName() + "\"?")) {
            return;
        }

        try {
            playlistModel.removeSongFromPlaylist(selectedPlaylist.getId(), selectedSongInPlaylist.getId());
            playlistView.refresh();
            songListInPlaylist.setItems(playlistModel.getObservableSongsInPlaylist(selectedPlaylist));
            showAlert("Success",
                    "\"" + selectedSongInPlaylist.getTitle() + "\" removed from playlist.",
                    Alert.AlertType.INFORMATION);
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
                selectAndScrollToLastItem(songList, songModel.getObservableSongs());
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

        if (!showConfirmation("Delete Song",
                "Are you sure you want to delete \"" + selectedSong.getTitle() + "\"?")) {
            return;
        }

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
            applySearchFilter();
        } else {
            resetFilter();
        }
    }

    private void applySearchFilter() {
        String query = songSearcherTxtField.getText();
        if (isEmptyOrNull(query)) {
            resetFilter();
            return;
        }

        String lowerQuery = query.trim().toLowerCase();
        filteredSongs.setPredicate(song -> {
            String title = song.getTitle() == null ? "" : song.getTitle().toLowerCase();
            String artist = song.getArtist() == null ? "" : song.getArtist().toLowerCase();
            return title.contains(lowerQuery) || artist.contains(lowerQuery);
        });

        filterActive = true;
        searchBtn.setText("Clear");
    }

    private void resetFilter() {
        filteredSongs.setPredicate(s -> true);
        filterActive = false;
        searchBtn.setText("Search");
        songSearcherTxtField.clear();
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
            if (songs.isEmpty() || index >= songs.size()) return;

            List<Song> reorderedList = new ArrayList<>();
            for (int i = index; i < songs.size(); i++) {
                reorderedList.add(songs.get(i));
            }
            for (int i = 0; i < index; i++) {
                reorderedList.add(songs.get(i));
            }

            playbackManager.playSongFromPlaylist(reorderedList, songs.get(index));
        } catch (Exception e) {
            showAlert("Error", "Could not play song:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshPlaylistSongList(Playlist playlist, int newIndex) {
        songListInPlaylist.setItems(playlistModel.getObservableSongsInPlaylist(playlist));
        int clampedIndex = Math.max(0, Math.min(newIndex, songListInPlaylist.getItems().size() - 1));
        songListInPlaylist.getSelectionModel().select(clampedIndex);
        songListInPlaylist.scrollTo(clampedIndex);
        playlistView.refresh();
        updateMoveButtonsState();
    }

    // generic type <T> allows this method to work with both Song and Playlist TableViews
    // avoids code duplication
    private <T> void selectAndScrollToLastItem(TableView<T> table, ObservableList<T> items) {
        int newIndex = items.size() - 1;
        if (newIndex >= 0) {
            table.getSelectionModel().select(newIndex);
            table.scrollTo(newIndex);
        }
    }

    private boolean isEmptyOrNull(String text) {
        return text == null || text.trim().isEmpty();
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