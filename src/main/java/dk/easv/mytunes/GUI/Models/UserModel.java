package dk.easv.mytunes.GUI.Models;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UserManager;
import dk.easv.mytunes.DAL.DAO.UserDAO;

// Java imports
import java.util.List;


public class UserModel {

    private final UserManager userManager = new UserManager();


    public UserModel() {
    }


    public List<User> getAllUsers() throws Exception {
        return userManager.getAllUsers();
    }

    public User createUser(String username, String password) throws Exception {
        return userManager.createUser(username, password);
    }

    public User loginUser(String username, String password) {
        return userManager.loginUser(username, password);
    }

}
