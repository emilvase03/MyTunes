package dk.easv.mytunes.GUI.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainViewController implements Initializable {

    private String currentUser;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
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