package ua.org.code;

import org.dbunit.Assertion;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.org.code.persistence.dao.impl.JdbcRoleDao;
import ua.org.code.persistence.dao.impl.JdbcUserDao;
import ua.org.code.persistence.entity.Role;
import ua.org.code.persistence.entity.User;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DbTest extends DataSourceBasedDBTestCase {
    private Connection connection;
    private JdbcRoleDao jdbcRoleDao;
    private JdbcUserDao jdbcUserDao;

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(getClass().getClassLoader().getResourceAsStream(
                "dataset/tables.xml"));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        connection = getConnection().getConnection();
        jdbcRoleDao = new JdbcRoleDao(getDataSource().getConnection());
        jdbcUserDao = new JdbcUserDao(getDataSource().getConnection());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @Order(1)
    public void whenSelectFromRole_thenFirstNameIsAdmin() {
        List<Role> roles = jdbcRoleDao.findAll();
        assertThat(roles.isEmpty()).isFalse();
        assertThat(roles.get(0).getName()).isEqualTo("admin");
    }

    @Test
    @Order(2)
    public void whenSelectFromUser_thenFirstLoginIsPeter() {
        List<User> users = jdbcUserDao.findAll();
        assertThat(users.isEmpty()).isFalse();
        assertThat(users.get(0).getLogin()).isEqualTo("peter");
    }

    @Test
    public void whenInsertIntoRole_thenCreateNewRole() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-role.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("roles");

        jdbcRoleDao.create(new Role(
                3L,
                "moderator"
        ));
        ITable actualTable = getConnection().createQueryTable("result_name",
                "select * from roles where name='moderator'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenInsertIntoUser_thenCreateNewUser() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-user.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("users");

        jdbcUserDao.create(new User(
                3L,
                "denis",
                "pass",
                "denis@email.com",
                "Denis",
                "Red",
                Date.valueOf("2000-10-11"),
                jdbcRoleDao.findById(2L)
        ));
        ITable actualTable = getConnection().createQueryTable("result_login",
                "select * from users where login='denis'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenUpdateRole_thenUpdateRoleInDb() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-updated-role.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("roles");

        Role updatableUser = jdbcRoleDao.findById(2L);
        updatableUser.setName("player");
        jdbcRoleDao.update(updatableUser);
        ITable actualTable = getConnection().createQueryTable("result_name",
                "select * from roles where name='player'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenUpdateUser_thenUpdateUserInDb() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-updated-user.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("users");

        User updatedUser = jdbcUserDao.findById(2L);
        updatedUser.setLogin("liza");
        jdbcUserDao.update(updatedUser);
        ITable actualTable = getConnection().createQueryTable("result_login",
                "select * from users where login='liza'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenDeleteRole_thenShouldReturnEmptyRowForSelectByName() throws Exception{
        jdbcRoleDao.remove(jdbcRoleDao.findById(4L));

        ITable actualTable = getConnection().createQueryTable("result_remove",
                "select * from roles where name='waiter'");
        assertThat(actualTable.getRowCount()).isEqualTo(0);
    }

    @Test
    public void whenDeleteUser_thenReturnEmptyRowForSelectByLogin() throws Exception{
        jdbcUserDao.remove(jdbcUserDao.findById(1L));

        ITable actualTable = getConnection().createQueryTable("result_remove",
                "select * from users where login='peter'");
        assertThat(actualTable.getRowCount()).isEqualTo(0);
    }

    @Test
    public void whenFindUserById_thenLoginIsVova() {
        User user = jdbcUserDao.findById(4L);
        assertThat(user.getLogin()).isEqualTo("vova");
    }

    @Test
    public void whenFindRoleById_thenNameIsAdmin() {
        Role role = jdbcRoleDao.findById(1L);
        assertThat(role.getName()).isEqualTo("admin");
    }

    @Test
    public void whenFindRoleByName_thenNameIdIsOne() {
        Role role = jdbcRoleDao.findByName("admin");
        assertThat(role.getId()).isEqualTo(1L);
    }

    @Test
    public void whenFindUserByLogin_thenFirstNameIsVova() {
        User user = jdbcUserDao.findByLogin("vova");
        assertThat(user.getFirstName()).isEqualTo("Vova");
    }

    @Test
    public void whenFindUserByEmail_thenFirstNameIsVova() {
        User user = jdbcUserDao.findByEmail("vova@email.com");
        assertThat(user.getFirstName()).isEqualTo("Vova");
    }

    @Override
    protected DataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;init=runscript from 'classpath:sql/create.sql'");
        dataSource.setUser("admin");
        dataSource.setPassword("admin");
        return dataSource;
    }

    @Override
    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.REFRESH;
    }

    @Override
    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.DELETE_ALL;
    }
}
