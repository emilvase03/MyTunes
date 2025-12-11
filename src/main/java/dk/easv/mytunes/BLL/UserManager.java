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

    public List<User> getAllUsers() throws Exception {
        return userDAO.getAllUsers();
    }

    public User loginUser(String username, String password) {
        try {
            for (User u : userDAO.getAllUsers()) {
                if (u.getUsername().equals(username)
                        && Encrypter.verifyPassword(password, u.getPassword_hash())) {
                    return u;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public User createUser(String username, String password) {
        try {
            String hashedPassword = Encrypter.hashPassword(password); // hash the raw password
            return userDAO.createUser(new User(-1, username, hashedPassword));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
