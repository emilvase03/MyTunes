package dk.easv.mytunes.GUI.Controllers;

import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UserManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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
    private String currentUser;

    private final UserManager userManager = new UserManager();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Validate
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login failed", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }
        User user = userManager.loginUser(username, password);

        if (user != null) {
            // SUCCESS
            showAlert("Login success", "Logged in successfully", Alert.AlertType.INFORMATION);
            loginSuccess = true;
            currentUser = username;

            // close loginUser window
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();

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
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    @FXML
    private void onBtnLoginClick() {
        handleLogin();
    }

    @FXML
    private void onTxtRegisterClick(MouseEvent mouseEvent) {
        handleCancel();
    }
}
