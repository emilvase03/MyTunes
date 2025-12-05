package dk.easv.mytunes.GUI.Models;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.DAL.DAO.UserDAO;

// Java imports
import java.util.List;


public class UserModel {

    private final UserDAO userDAO = new UserDAO();


    public UserModel() {
    }


    public List<User> getAllUsers() throws Exception {
        return userDAO.getAllUsers();
    }

    public User createUser(User newUser) throws Exception {
        return userDAO.createUser(newUser);
    }

}
