package dk.easv.mytunes.DAL.DB;

// Project imports
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

// Java imports
import java.io.*;
import java.sql.Connection;
import java.util.Properties;

public class DBConnector {

    private static final String PROP_FILE = ".idea/config/database.settings";
    private SQLServerDataSource dataSource;

    public DBConnector() throws IOException {
        Properties databaseProperties = new Properties();
        databaseProperties.load(new FileInputStream(new File(PROP_FILE)));

        dataSource = new SQLServerDataSource();

        dataSource.setServerName(databaseProperties.getProperty("Server"));
        dataSource.setDatabaseName(databaseProperties.getProperty("Database"));
        dataSource.setUser(databaseProperties.getProperty("User"));
        dataSource.setPassword(databaseProperties.getProperty("Password"));

        dataSource.setTrustServerCertificate(true);
    }

    public Connection getConnection() throws SQLServerException {
        return dataSource.getConnection();
    }

    public static void main(String[] args) throws Exception {
        DBConnector dbConnector = new DBConnector();

        try (Connection connection = dbConnector.getConnection()) {
            System.out.println("Is it open? " + !connection.isClosed());
        } // Connection gets closed here
    }

}
