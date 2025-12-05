package dk.easv.mytunes.BLL;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.BLL.UTIL.Encrypter;
import dk.easv.mytunes.GUI.Models.UserModel;


public class UserManager {

    private final UserModel userModel = new UserModel();

    public User loginUser(String username, String password) {
        try {
            for (User u : userModel.getAllUsers()) {
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
            return userModel.createUser(new User(-1, username, hashedPassword));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
