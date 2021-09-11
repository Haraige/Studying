package ua.org.code;

import lombok.extern.log4j.Log4j2;
import ua.org.code.persistence.dao.impl.JdbcRoleDao;
import ua.org.code.persistence.dao.impl.JdbcUserDao;
import ua.org.code.persistence.entity.Role;
import ua.org.code.persistence.entity.User;
import ua.org.code.util.PooledDataSource;

import java.sql.Date;

@Log4j2
public class App {
    public static void main(String[] args) {
        JdbcRoleDao jdbcRoleDao = new JdbcRoleDao(PooledDataSource.getConnection());

        Role role = new Role(
                5L,
                "user"
        );

        jdbcRoleDao.create(role);
        jdbcRoleDao.findAll().forEach(System.out::println);
    }
}
