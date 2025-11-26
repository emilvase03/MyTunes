package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.MainViewModel;

// Java imports
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    private MainViewModel mainViewModel;
    private String currentUser;

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

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        songList.setItems(mainViewModel.getObservableSongs());

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
    private void onBtnCloseProgram() { }

    @FXML
    private void onBtnClickSearch() { }

    public MainViewController() {
        try {
            mainViewModel = new MainViewModel();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not instantiate MainViewModel");
        }
    }
}

