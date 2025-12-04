package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Playlist;
import dk.easv.mytunes.GUI.Models.PlaylistModel;

// Java imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
            if (newName.isEmpty()) return; // optional: show an error popup

            playlistToEdit.setName(newName);   // update object
            playlistModel.updatePlaylist(playlistToEdit); // save to DB + ObservableList

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBtnCancelChanges(ActionEvent actionEvent) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}
