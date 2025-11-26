package dk.easv.mytunes.GUI.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPassword;

    @FXML
    private Label messageLabel;

    private boolean loginSuccess = false;
    private String currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize login view
//        if (messageLabel != null) {
//            messageLabel.setText("");
//        }
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login failed", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }

        // Simple authentication (replace with real authentication)
        if (username.equals("admin") && password.equals("password")) {
            showAlert("Login success", "Logged in successfully", Alert.AlertType.INFORMATION);
            currentUser = username;
            loginSuccess = true;

            // Close dialog after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) rootPane.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } else {
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
    private void onTxtRegisterClick(MouseEvent mouseEvent) {
        handleCancel();
    }

    @FXML
    private void onBtnLoginClick(ActionEvent actionEvent) {
        handleLogin();
    }
}