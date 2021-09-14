package ua.org.code.persistence.dao.impl;

import lombok.extern.log4j.Log4j2;
import ua.org.code.persistence.dao.GenericJdbcDao;
import ua.org.code.persistence.dao.UserDao;
import ua.org.code.persistence.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcUserDao extends GenericJdbcDao<User> implements UserDao {
    private final JdbcRoleDao jdbcRoleDao;

    public JdbcUserDao(Connection connection) {
        super(connection, User.class);
        this.jdbcRoleDao = new JdbcRoleDao(connection);
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            Statement getAllUsersStatement = connection.createStatement();
            getAllUsersStatement.execute("select * from users");
            ResultSet getAllUsersResultSet = getAllUsersStatement.getResultSet();
            while (getAllUsersResultSet.next()) {
                User user = new User(
                getAllUsersResultSet.getLong(1),
                        getAllUsersResultSet.getString(2),
                        getAllUsersResultSet.getString(3),
                        getAllUsersResultSet.getString(4),
                        getAllUsersResultSet.getString(5),
                        getAllUsersResultSet.getString(6),
                        getAllUsersResultSet.getDate(7),
                        jdbcRoleDao.findById(getAllUsersResultSet.getLong(8)));
                users.add(user);
            }
            connection.commit();
        } catch (SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public User findById(Long id) {
        User user = new User();
        try (PreparedStatement findUserByIdStatement = connection.prepareStatement(
                     "select * from users where id=?")) {
            findUserByIdStatement.setMaxRows(1);
            findUserByIdStatement.setLong(1, id);
            if (!findUserByIdStatement.execute()) {
                throw new RuntimeException("No user with current id!");
            }

            ResultSet resultSet = findUserByIdStatement.getResultSet();
            resultSet.next();
            user.setId(id);
            user.setLogin(resultSet.getString(2));
            user.setPassword(resultSet.getString(3));
            user.setEmail(resultSet.getString(4));
            user.setFirstName(resultSet.getString(5));
            user.setLastName(resultSet.getString(6));
            user.setBirthday(resultSet.getDate(7));
            user.setRole(jdbcRoleDao.findById(resultSet.getLong(8)));
            connection.commit();
        } catch (SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public User findByLogin(String login) {
        User user = new User();
        try (PreparedStatement findUserByIdStatement = connection.prepareStatement(
                "select * from users where login=?")) {
            findUserByIdStatement.setMaxRows(1);
            findUserByIdStatement.setString(1, login);
            if (!findUserByIdStatement.execute()) {
                throw new RuntimeException("No user with current login!");
            }

            ResultSet resultSet = findUserByIdStatement.getResultSet();
            resultSet.next();
            user.setId(resultSet.getLong(1));
            user.setLogin(resultSet.getString(2));
            user.setPassword(resultSet.getString(3));
            user.setEmail(resultSet.getString(4));
            user.setFirstName(resultSet.getString(5));
            user.setLastName(resultSet.getString(6));
            user.setBirthday(resultSet.getDate(7));
            user.setRole(jdbcRoleDao.findById(resultSet.getLong(8)));
            connection.commit();
        } catch (SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
        return user;
    }

    @Override
    public User findByEmail(String email) {
        User user = new User();
        try (PreparedStatement findUserByIdStatement = connection.prepareStatement(
                "select * from users where email=?")) {
            findUserByIdStatement.setMaxRows(1);
            findUserByIdStatement.setString(1, email);
            if (!findUserByIdStatement.execute()) {
                throw new RuntimeException("No user with current email!");
            }

            ResultSet resultSet = findUserByIdStatement.getResultSet();
            resultSet.next();
            user.setId(resultSet.getLong(1));
            user.setLogin(resultSet.getString(2));
            user.setPassword(resultSet.getString(3));
            user.setEmail(resultSet.getString(4));
            user.setFirstName(resultSet.getString(5));
            user.setLastName(resultSet.getString(6));
            user.setBirthday(resultSet.getDate(7));
            user.setRole(jdbcRoleDao.findById(resultSet.getLong(8)));
            connection.commit();
        } catch (SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
        return user;
    }
}
