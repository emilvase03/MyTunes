package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.Song;
import dk.easv.mytunes.GUI.Models.MainViewModel;

// Java imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

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

    private MainViewModel mainViewModel;


    public MainViewController() {
        try {
            mainViewModel = new MainViewModel();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not instantiate MainViewModel");
        }
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        songList.setItems(mainViewModel.getObservableSongs());
    }

}

