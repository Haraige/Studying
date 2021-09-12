package ua.org.code.persistence.dao.impl;

import lombok.extern.log4j.Log4j2;
import ua.org.code.persistence.dao.GenericJdbcDao;
import ua.org.code.persistence.dao.RoleDao;
import ua.org.code.persistence.entity.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcRoleDao extends GenericJdbcDao<Role> implements RoleDao {
    public JdbcRoleDao(Connection connection) {
        super(connection, Role.class);
    }

    @Override
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            Statement getAllRolesStatement = connection.createStatement();
            getAllRolesStatement.execute("select * from roles");
            ResultSet getAllRolesResultSet = getAllRolesStatement.getResultSet();
            while (getAllRolesResultSet.next()) {
                Role role = new Role(
                        getAllRolesResultSet.getLong(1),
                        getAllRolesResultSet.getString(2)
                );
                roles.add(role);
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
        return roles;
    }

    @Override
    public Role findById(Long id) {
        Role resultRole = new Role();
        try (PreparedStatement getRoleByIdStatement = connection.prepareStatement(
                     "select * from roles where id=?")) {
            getRoleByIdStatement.setMaxRows(1);
            getRoleByIdStatement.setLong(1, id);
            if (!getRoleByIdStatement.execute()) {
                throw new RuntimeException("No role with current id!");
            }

            ResultSet resultSet = getRoleByIdStatement.getResultSet();

            resultSet.next();
            resultRole.setId(resultSet.getLong(1));
            resultRole.setName(resultSet.getString(2));
        } catch (SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
        return resultRole;
    }

    @Override
    public Role findByName(String name) {
        Role resultRole = new Role();
        try (PreparedStatement getRoleByNameStatement = connection.prepareStatement(
                     "select * from roles where name=?")) {
            connection.setAutoCommit(false);
            getRoleByNameStatement.setMaxRows(1);
            getRoleByNameStatement.setString(1, name);
            if (!getRoleByNameStatement.execute()) {
                throw new RuntimeException("No role with current name!");
            }

            ResultSet resultSet = getRoleByNameStatement.getResultSet();

            resultSet.next();
            resultRole.setId(resultSet.getLong(1));
            resultRole.setName(resultSet.getString(2));
        } catch (SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);

        }
        return resultRole;
    }
}
