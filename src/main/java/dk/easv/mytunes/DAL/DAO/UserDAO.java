package dk.easv.mytunes.DAL.DAO;

import dk.easv.mytunes.BE.User;
import dk.easv.mytunes.DAL.DB.DBConnector;
import dk.easv.mytunes.DAL.IUserDataAccess;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            String sql = "SELECT * FROM " + "[" + "User" + "]";
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
}

