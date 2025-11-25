package dk.easv.mytunes.GUI.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private boolean loginSuccess = false;
    private String currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize login view
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password", false);
            return;
        }

        // Simple authentication (replace with real authentication)
        if (username.equals("admin") && password.equals("password")) {
            showMessage("Login successful!", true);
            currentUser = username;
            loginSuccess = true;

            // Close dialog after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        } else {
            showMessage("Invalid username or password", false);
            passwordField.clear();
            loginSuccess = false;
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message, boolean success) {
        messageLabel.setText(message);
        messageLabel.setTextFill(success ? Color.GREEN : Color.RED);
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    @FXML
    private void onTxtRegisterClick(MouseEvent mouseEvent) {
    }

    @FXML
    private void onBtnLoginClick(ActionEvent actionEvent) {

    }
}