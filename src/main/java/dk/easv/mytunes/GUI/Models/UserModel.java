package dk.easv.mytunes.GUI.Models;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UserManager;

// Java imports
import java.util.List;

public class UserModel {

    private final UserManager userManager;


    public UserModel() {
        try {
            userManager = new UserManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<User> getAllUsers() throws Exception {
        return userManager.getAllUsers();
    }

    public User createUser(String username, String password) throws Exception {
        return userManager.createUser(username, password);
    }

    public User loginUser(String username, String password) throws Exception {
        return userManager.loginUser(username, password);
    }

}
