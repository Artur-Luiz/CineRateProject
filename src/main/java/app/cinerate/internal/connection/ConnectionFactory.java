package app.cinerate.internal.connection;

import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class ConnectionFactory {



    public ConnectionFactory() {
    }

    public Connection createConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        var sqliteFileUrl = "jdbc:sqlite:" + new File("cinerate.db");
        return DriverManager.getConnection(sqliteFileUrl);
    }

}
