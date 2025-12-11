package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UserManager;

// Java imports
import dk.easv.mytunes.GUI.Models.UserModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class RegisterViewController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    private UserModel userModel;

    @FXML
    private void onBtnRegisterClick() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        userModel = new UserModel();

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


        try {
            User createdUser = userModel.createUser(username, password);
            if (createdUser == null) {
                showAlert("Registration Error", "Username already exists. Choose another.", Alert.AlertType.ERROR);
                return;
            }
        } catch (Exception e) {
            showAlert("Registration Error", "Could not register user: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        showAlert("Success", "Account created successfully! Please loginUser.", Alert.AlertType.INFORMATION);

        // Open loginUser popup
        closeStage();
    }


    @FXML
    private void onTxtLoginClick(MouseEvent event) {
        // User clicked "Already have an account?"
        closeStage();
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    public void onEnterRegister(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onBtnRegisterClick();
        }
    }
}