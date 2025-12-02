package dk.easv.mytunes.BLL;

import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.DAL.DAO.UserDAO;
import dk.easv.mytunes.BLL.UTIL.Encrypter;

public class UserManager {

    private final UserDAO userDAO = new UserDAO();

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
