package config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDataSource {

    private static final Logger logger = LoggerFactory.getLogger(CustomDataSource.class);

    private static CustomDataSource dataSource = null;
    private JdbcConnectionPool cp = null;
    private DatabaseConfig dbConfig = new DatabaseConfig();

    private CustomDataSource() {

    }

    public static CustomDataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        }
        dataSource = new CustomDataSource();
        return dataSource;
    }

    public void initializeJdbcConnectionPool() {
        try {
            Class.forName(dbConfig.getJDBC_DRIVER());
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        cp = JdbcConnectionPool.create(dbConfig.getDB_URL(), dbConfig.getUSER(), dbConfig.getPASS());
    }

    public Connection getDatabaseConnectionFromPool() {
        try {
            Connection connection = getConnectionPool().getConnection();
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JdbcConnectionPool getConnectionPool() {
        return cp;
    }

    public Connection getDatabaseConnection() {
        Connection conn = getDatabaseConnectionFromPool();
        if (conn == null) {
            System.out.println("Database connection is null");
            return null;
        }
        return conn;
    }

    public void cleanupResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            logger.error("Error in cleaning up the resources", e);
        }
    }
}
