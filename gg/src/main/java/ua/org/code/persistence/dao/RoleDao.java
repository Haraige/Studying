package ua.org.code.persistence.dao;

import ua.org.code.persistence.entity.Role;

public interface RoleDao extends Dao<Role> {
    Role findByName(String name);
}
