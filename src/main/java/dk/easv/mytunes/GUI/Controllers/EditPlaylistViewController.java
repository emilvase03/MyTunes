package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.GUI.Models.PlaylistModel;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class EditPlaylistViewController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField txtPlaylistName;

    private Playlist playlistToEdit;
    private PlaylistModel playlistModel;

    public void setData(Playlist playlist, PlaylistModel model) {
        this.playlistToEdit = playlist;
        this.playlistModel = model;

        txtPlaylistName.setText(playlist.getName());
    }

    @FXML
    private void onBtnSaveChanges(ActionEvent actionEvent) {
        try {
            String newName = txtPlaylistName.getText().trim();
            if (newName.isEmpty()) return;

            playlistToEdit.setName(newName);
            playlistModel.updatePlaylist(playlistToEdit);

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert("Error", "Could not save changes:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onBtnCancelChanges(ActionEvent actionEvent) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}
