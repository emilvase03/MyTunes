package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.CurrentUser;
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UserManager;

// Java imports
import dk.easv.mytunes.GUI.Models.UserModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    private boolean loginSuccess = false;
    private CurrentUser currentUser = CurrentUser.getInstance();

    private final UserModel userModel = new UserModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void handleLogin() throws IOException {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Validate
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login failed", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }
        User user = userModel.loginUser(username, password);

        if (user != null) {
            // SUCCESS
            showAlert("Login success", "Logged in successfully", Alert.AlertType.INFORMATION);
            loginSuccess = true;
            currentUser.setCurrentUser(user);

            // Open MyTunes MainView Window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            Stage mainStage = new Stage();
            mainStage.setTitle("MyTunes");
            mainStage.initModality(Modality.APPLICATION_MODAL);
            mainStage.setScene(new Scene(root));
            mainStage.setResizable(false);
            mainStage.show();

            // close loginUser window
            closeStage();
        } else {
            // FAIL
            showAlert("Login failed", "Invalid username or password", Alert.AlertType.ERROR);
            txtPassword.clear();
            loginSuccess = false;
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
    private void handleCancel() {
       closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    @FXML
    private void onBtnLoginClick() {
        try {
            handleLogin();
        } catch (IOException e) {
            showAlert("Error", "Login failed", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onTxtRegisterClick(MouseEvent mouseEvent) {
        try {
            showRegisterPage();
        } catch (IOException e) {
            showAlert("Error", "Could not open register window", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }
    }

    private void showRegisterPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterView.fxml"));
        Parent root = loader.load();
        RegisterViewController registerController = loader.getController();

        Stage registerStage = new Stage();
        registerStage.setTitle("Welcome to MyTunes");
        registerStage.initModality(Modality.APPLICATION_MODAL);
        registerStage.setScene(new Scene(root));
        registerStage.setResizable(false);
        registerStage.showAndWait();
    }

    public void onEnterLogin(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onBtnLoginClick();
        }
    }
}
