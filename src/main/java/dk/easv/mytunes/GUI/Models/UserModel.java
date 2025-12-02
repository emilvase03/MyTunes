package dk.easv.mytunes.GUI.Models;

import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UserManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class UserModel {

    private final UserManager userManager = new UserManager(); // initialize it!
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty passwordHash;

    // Constructor from raw values
    public UserModel(int id, String username, String passwordHash) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.passwordHash = new SimpleStringProperty(passwordHash);
    }

    public UserModel(User user) {
        this(user.getId(), user.getUsername(), user.getPassword_hash());
    }

    public User createUser(User newUser) throws Exception {
        return userManager.createUser(newUser.getUsername(), newUser.getPassword_hash());
    }

    // ID property
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Username property
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    // PasswordHash property
    public String getPasswordHash() {
        return passwordHash.get();
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash.set(passwordHash);
    }

    public StringProperty passwordHashProperty() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                '}';
        // Do NOT print password
    }
}
