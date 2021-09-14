package ua.org.code.persistence.dao;

import ua.org.code.persistence.entity.User;

public interface UserDao extends Dao<User> {
    User findByLogin(String login);
    User findByEmail(String email);
}
