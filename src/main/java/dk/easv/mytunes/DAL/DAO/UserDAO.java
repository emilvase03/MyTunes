package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.IUserDataAccess;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDataAccess
{

    private DBConnector databaseConnector;

    public UserDAO() {
        try {
            databaseConnector = new DBConnector();
        } catch (IOException e) {
            throw new RuntimeException("DatabaseConnecter failed");
        }
    }

    /**
     * @return List of type User queried from DB
     */
    public List<User> getAllUsers() throws Exception {
        // Create return data structure
        ArrayList allUsers = new ArrayList<>();

        // Create a connection // 'try-with-resources'
        try (Connection connection = databaseConnector.getConnection()) {
            // Create SQL command
            String sql = "SELECT * FROM " + "[" + "user" + "]";
            // Create a statement that we later can send to the database
            Statement statement = connection.createStatement();

            // Executes the sql-command // Sends the sql-command to the database
            if (statement.execute(sql)) {
                ResultSet resultSet = statement.getResultSet();
                while(resultSet.next()) {
                    // Map DB row to User object
                    int id = resultSet.getInt("id");
                    String username = resultSet.getString("username");
                    String passwordHash = resultSet.getString("password_hash");

                    User newUser = new User(id, username, passwordHash);
                    allUsers.add(newUser);
                }
            }
            return allUsers;

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not get users from database");
        }
    }

    /**
     * Creates new Movie object in DB
     * @param newUser
     * @return createdMovie
     * @throws Exception
     */
    public User createUser(User newUser) throws Exception{
        String sql = "INSERT INTO dbo.user (username, password_hash) VALUES (?,?);";

        try (Connection connection = databaseConnector.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Bind parameters
            stmt.setString(1, newUser.getUsername());
            stmt.setString(2, newUser.getPassword_hash());

            // Run the specified SQL statement
            stmt.executeUpdate();

            // Get the generated ID from DB
            ResultSet rs = stmt.getGeneratedKeys();
            int id = -1;

            if (rs.next())
                id = rs.getInt(1);

            // Create movie object and send up the layers
            User createdUser = new User(id, newUser.getUsername(), newUser.getPassword_hash());

            return createdUser;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new Exception("Could not create user", ex);
        }

    }

    /**
     * Updates title and year of specified Movie in DB
     * @param user
     * @throws Exception
     */
    public void updateUser(User user) throws Exception{

        try (Connection connection = databaseConnector.getConnection()) {
            String sql = "UPDATE user SET username = ?, password_hash = ? WHERE Id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPassword_hash());
                statement.setInt(3, user.getId());

                statement.executeUpdate();
            }

        }
        catch (SQLException ex) {
            throw new Exception("Could not update user", ex);
        }

    }

    /**
     * Deletes Movie from DB
     * @param user
     * @throws Exception
     */
    public void deleteUser(User user) throws Exception{
        try (Connection connection = databaseConnector.getConnection()) {
            String sql = "DELETE FROM user WHERE Id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, user.getId());

                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new Exception("Could not delete user", ex);
        }
    }

}

