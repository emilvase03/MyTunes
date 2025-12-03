package dk.easv.mytunes.GUI.Controllers;

import dk.easv.mytunes.BE.CurrentUser;
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.GUI.Models.PlaylistModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewPlaylistController {
    @FXML
    private TextField txtTitle;

    private PlaylistModel playlistModel;
    private boolean isPlaylistadded = false;

    public NewPlaylistController() {
        try {
            playlistModel = PlaylistModel.getInstance();
        } catch (Exception e) {
            showAlert("Error", "Could not instantiate PlaylistModel", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onBtnSave(ActionEvent actionEvent) {
        String title = txtTitle.getText();
        int userId = CurrentUser.getInstance().getCurrentUser().getId();
        if(!title.isBlank()) {

            Playlist newPlaylist = new Playlist(title, userId);

            try {
                playlistModel.createPlaylist(newPlaylist);
            } catch (Exception e) {
                showAlert("Error", "Could not create new playlist", Alert.AlertType.ERROR);
                throw new RuntimeException(e);
            }
            isPlaylistadded = true;
            closeStage();

        }
        else {
            showAlert("Error", "Please give your playlist a title", Alert.AlertType.ERROR);
            return;
        }

    }

    @FXML
    private void onBtnCancel(ActionEvent actionEvent) {
        closeStage();
    }

    public boolean isPlaylistAdded() {
        return isPlaylistadded;
    }

    private void closeStage() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
