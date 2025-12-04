package dk.easv.mytunes.GUI.Controllers;

import dk.easv.mytunes.BE.CurrentUser;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.SongModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import java.io.File;

public class EditSongViewController {
    @FXML
    private TextField txtTitleEdit;
    @FXML
    private TextField txtArtistEdit;
    @FXML
    private TextField txtTimeEdit;
    @FXML
    private TextField txtFilePathEdit;
    @FXML
    private TextField txtGenreEdit;

    private SongModel songModel;
    private CurrentUser currentUser = CurrentUser.getInstance();
    private boolean songAdded = false;
    private Song song;

    public EditSongViewController() {
        try {
            songModel = SongModel.getInstance();
        } catch (Exception e) {
            showAlert("Error", "SongModel failed", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }
    private String getSongDuration(String filePath) {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            int totalSec = audioFile.getAudioHeader().getTrackLength();

            int minutes = totalSec / 60;
            int seconds = totalSec % 60;

            return String.format("%02d:%02d", minutes, seconds);

        } catch (Exception e) {
            showAlert("Error", "Failed to get song duration", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onBtnChooseEdit(ActionEvent actionEvent) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Audio Files (*.mp3, *.wav)", "*.mp3", "*.wav");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(txtFilePathEdit.getScene().getWindow());

        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            txtFilePathEdit.setText(filePath);

            String duration = getSongDuration(filePath);
            txtTimeEdit.setText(duration);
        }
    }

    @FXML
    private void onBtnCancelEdit(ActionEvent actionEvent) {
        closeStage();
    }

    @FXML
    private void onBtnSaveEdit(ActionEvent actionEvent) {

        try {
            String title = txtTitleEdit.getText();
            String artist = txtArtistEdit.getText();
            String duration = txtTimeEdit.getText();
            String filePath = txtFilePathEdit.getText();
            String genre = txtGenreEdit.getText();
            int userId = currentUser.getCurrentUser().getId();

            if (title.isBlank() || artist.isBlank() || duration.isBlank() ||filePath.isBlank() || genre.isBlank()) {
                showAlert("Could not edit song", "Please fill in every field", Alert.AlertType.ERROR);
                return;
            }



           if (!filePath.equals(song.getFilepath())) {
                String lowerPath = filePath.toLowerCase().trim();
                if (!(lowerPath.endsWith(".mp3") || lowerPath.endsWith(".wav"))) {
                    showAlert("Incorrect file type", "Please only use MP3 or WAV files", Alert.AlertType.ERROR);
                    return;
                }
                duration = getSongDuration(filePath);


          }
// Update song details
            song.setTitle(title);
            song.setArtist(artist);
            song.setDuration(duration);
            song.setFilepath(filePath);
            song.setGenre(genre);

            songModel.updateSong(song);

            songAdded = true;
            closeStage();

        } catch (Exception e) {
            showAlert("Error","Could not save song", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    private void closeStage() {
        Stage stage = (Stage) txtTitleEdit.getScene().getWindow();
        stage.close();
    }

    public boolean isSongAdded() {
        return songAdded;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
}

    public void setSong(Song song) {

        this.song = song;
        txtTitleEdit.setText(song.getTitle());
        txtArtistEdit.setText(song.getArtist());
        txtGenreEdit.setText(song.getGenre());
       txtTimeEdit.setText(song.getDuration());
       txtFilePathEdit.setText(song.getFilepath());

    }
}
