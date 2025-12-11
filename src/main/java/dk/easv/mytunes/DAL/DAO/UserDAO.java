package dk.easv.mytunes.DAL.DAO;

// Project imports
import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.IUserDataAccess;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDataAccess {

    private final DBConnector databaseConnector;

    public UserDAO() throws Exception {
        databaseConnector = new DBConnector();
    }

    public List<User> getAllUsers() throws Exception {
        List<User> allUsers = new ArrayList<>();

        try (Connection conn = databaseConnector.getConnection()) {
            String sql = "SELECT * FROM [users]";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                allUsers.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                ));
            }
            return allUsers;
        }
    }

    public boolean usernameExists(String username) throws Exception {
        String sql = "SELECT COUNT(*) FROM [users] WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /** Create a new user */
    public User createUser(User newUser) throws Exception {
        // Check if username already exists
        if (usernameExists(newUser.getUsername())) {
            return null;
        }

        String sql = "INSERT INTO [users] (username, password_hash) VALUES (?, ?)";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, newUser.getUsername());
            stmt.setString(2, newUser.getPassword_hash());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) id = rs.getInt(1);

            return new User(id, newUser.getUsername(), newUser.getPassword_hash());
        }
    }

    /** Update a user */
    public void updateUser(User user) throws Exception {
        String sql = "UPDATE [users] SET username = ?, password_hash = ? WHERE id = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword_hash());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();
        }
    }

    /** Delete a user */
    public void deleteUser(User user) throws Exception {
        String sql = "DELETE FROM [users] WHERE id = ?";

        try (Connection conn = databaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }
    }
}
