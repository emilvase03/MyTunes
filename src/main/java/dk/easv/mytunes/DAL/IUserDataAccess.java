package dk.easv.mytunes.DAL;

// Project imports
import dk.easv.mytunes.BE.User;

// Java imports
import java.util.List;

public interface IUserDataAccess {

    List<User> getAllUsers() throws Exception;

    User createUser(User newUser) throws Exception;

    void updateUser(User user) throws Exception;

    void deleteUser(User user) throws Exception;

}
