package dk.easv.mytunes.BLL;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UTIL.Encrypter;
import dk.easv.mytunes.DAL.DAO.UserDAO;
import dk.easv.mytunes.DAL.IUserDataAccess;

// Java imports
import java.util.List;

public class UserManager {

    private IUserDataAccess userDAO = new UserDAO();

    public UserManager() throws Exception {
    }

    public List<User> getAllUsers() throws Exception {
        return userDAO.getAllUsers();
    }

    public User loginUser(String username, String password) throws Exception {
        if (username == null || password == null) {
            return null;
        }

        List<User> users = userDAO.getAllUsers();
        if (users == null || users.isEmpty()) {
            return null;
        }

        for (User u : users) {
            if (u.getUsername().equals(username)
                    && Encrypter.verifyPassword(password, u.getPassword_hash())) {
                return u;
            }
        }

        return null;
    }

    public User createUser(String username, String password) throws Exception {
        String hashedPassword = Encrypter.hashPassword(password); // may throw
        return userDAO.createUser(new User(-1, username, hashedPassword)); // may throw
    }
}
