package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.CurrentUser;
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.SongModel;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;

import java.io.File;

public class NewSongController {
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtArtist;
    @FXML
    private TextField txtTime;
    @FXML
    private TextField txtFilePath;
    @FXML
    private TextField txtGenre;

    private SongModel songModel;
    private CurrentUser currentUser = CurrentUser.getInstance();
    private boolean songAdded = false;

    public NewSongController() {
        try {
            songModel = SongModel.getInstance();
        } catch (Exception e) {
            showAlert("Error", "SongModel failed", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onBtnChoose(ActionEvent actionEvent) {
        String filePath = txtFilePath.getText();
        String duration;

        if (!filePath.isBlank()) {
            duration = getSongDuration(filePath);
            txtTime.setText(duration);
        }
    }

    @FXML
    private void onBtnCancel(ActionEvent actionEvent) {
        closeStage();
    }

    @FXML
    private void onBtnSave(ActionEvent actionEvent) {
        try {
            String title = txtTitle.getText();
            String artist = txtArtist.getText();
            String duration = txtTime.getText();
            String filePath = txtFilePath.getText();
            String genre = txtGenre.getText();
            int userId = currentUser.getCurrentUser().getId();

            if (title.isBlank() || artist.isBlank() || duration.isBlank() || filePath.isBlank() || genre.isBlank()) {
                showAlert("Could not add song", "Please fill in every field", Alert.AlertType.ERROR);
                return;
            }

            if (!filePath.endsWith("mp3") && !filePath.endsWith("wav")) {
                showAlert("Incorrect file type", "Please only use MP3 or WAV files", Alert.AlertType.ERROR);
                return;
            }


            Song newSong = new Song(-1, userId, filePath, title, artist, genre, duration);
            songModel.createSong(newSong);

            songAdded = true;
            closeStage();

        } catch (Exception e) {
            showAlert("Error","Could not save song", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    private void closeStage() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    public boolean isSongAdded() {
        return songAdded;
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
