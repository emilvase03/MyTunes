package dk.easv.mytunes.GUI.Controllers;

import dk.easv.mytunes.BE.User;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterViewController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    private boolean loginSuccess = false;
    private UserModel userModel;

    public RegisterViewController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize register view
    }

    @FXML
    private void onBtnRegisterClick() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Registration Error", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }

        if (username.length() < 3) {
            showAlert("Registration Error", "Username must be at least 3 characters long", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 4) {
            showAlert("Registration Error", "Password must be at least 4 characters long", Alert.AlertType.ERROR);
            return;
        }

        userModel = new UserModel(-1, username, password);

        try {
            User createdUser = userModel.createUser(new User(-1, username, password));
            if (createdUser == null) {
                showAlert("Registration Error", "Username already exists. Choose another.", Alert.AlertType.ERROR);
                return;
            }
        } catch (Exception e) {
            showAlert("Registration Error", "Could not register user: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        showAlert("Success", "Account created successfully! Please login.", Alert.AlertType.INFORMATION);

        // Open login popup
        openLoginPopup();
    }


    @FXML
    private void onTxtLoginClick(MouseEvent event) {
        // User clicked "Already have an account?" - open login popup
        openLoginPopup();
    }

    private void openLoginPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent root = loader.load();
            LoginViewController loginController = loader.getController();

            Stage registerStage = (Stage) txtUsername.getScene().getWindow();

            // Disable the root pane instead of the stage
            rootPane.setDisable(true);

            // Create login stage
            Stage loginStage = new Stage();
            loginStage.setTitle("Login - MyTunes");
            loginStage.initOwner(registerStage);
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.setScene(new Scene(root));
            loginStage.setResizable(false);

            // Show login modally
            loginStage.showAndWait();

            // Re-enable register root pane after login closes
            rootPane.setDisable(false);

            if (loginController.isLoginSuccess()) {
                loginSuccess = true;
                registerStage.close(); // close register window
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login view", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    private void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}