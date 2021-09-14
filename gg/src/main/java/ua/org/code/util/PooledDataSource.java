package ua.org.code.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class PooledDataSource {

    private final static BasicDataSource dataSource = new BasicDataSource();

    static {
        Properties properties = LoadProperties.load();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        dataSource.setInitialSize(3);
        dataSource.setMaxTotal(15);
    }

    public static Connection getConnection() {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting connection", e);
        }
        return connection;
    }

}
