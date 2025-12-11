package dk.easv.mytunes.GUI.Controllers;

// Project imports
import dk.easv.mytunes.BE.CurrentUser;
import dk.easv.mytunes.BE.User;

// Java imports
import dk.easv.mytunes.GUI.Models.UserModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class LoginViewController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    private boolean loginSuccess = false;
    private CurrentUser currentUser = CurrentUser.getInstance();

    private final UserModel userModel = new UserModel();

    @FXML
    private void handleLogin(){
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login failed", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }

        try {
            User user = userModel.loginUser(username, password);
            if (user != null) {
                showAlert("Login success", "Logged in successfully", Alert.AlertType.INFORMATION);
                loginSuccess = true;
                currentUser.setCurrentUser(user);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
                Parent root = loader.load();

                Stage mainStage = new Stage();
                mainStage.setTitle("MyTunes");
                mainStage.initModality(Modality.APPLICATION_MODAL);
                mainStage.setScene(new Scene(root));
                mainStage.setResizable(false);
                mainStage.show();

                closeStage();
            } else {
                showAlert("Login failed", "Invalid username or password", Alert.AlertType.ERROR);
                txtPassword.clear();
                loginSuccess = false;
            }
        } catch (Exception e) {
            showAlert("Error", "Could not login", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }

    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeStage() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onBtnLoginClick() {
        try {
            handleLogin();
        } catch (Exception e) {
            showAlert("Error", "Login failed", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onTxtRegisterClick(MouseEvent mouseEvent) {
        try {
            showRegisterPage();
        } catch (Exception e) {
            showAlert("Error", "Could not open register window", Alert.AlertType.ERROR);
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
