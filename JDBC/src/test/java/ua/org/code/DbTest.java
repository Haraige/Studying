package ua.org.code;

import org.dbunit.Assertion;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.org.code.persistence.dao.impl.JdbcRoleDao;
import ua.org.code.persistence.dao.impl.JdbcUserDao;
import ua.org.code.persistence.entity.Role;
import ua.org.code.persistence.entity.User;

import javax.sql.DataSource;
import java.awt.image.LookupTable;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DbTest extends DataSourceBasedDBTestCase {
    private Connection connection;
    private JdbcRoleDao jdbcRoleDao;
    private JdbcUserDao jdbcUserDao;

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(getClass().getClassLoader().getResourceAsStream(
                "dataset/users.xml"));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        jdbcRoleDao = new JdbcRoleDao(getDataSource().getConnection());
        jdbcUserDao = new JdbcUserDao(getDataSource().getConnection());
        connection = getConnection().getConnection();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void givenDataSetEmptySchemaRole_whenDataSetCreated_thenTablesAreEqual() throws Exception {
        IDataSet expectedDataSet = getDataSet();
        ITable expectedTable = expectedDataSet.getTable("ROLE");
        IDataSet databaseDataSet = getConnection().createDataSet();
        ITable actualTable = databaseDataSet.getTable("ROLE");
        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void givenDataSetEmptySchemaUser_whenDataSetCreated_thenTablesAreEqual() throws Exception {
        IDataSet expectedDataSet = getDataSet();
        ITable expectedTable = expectedDataSet.getTable("USER");
        IDataSet databaseDataSet = getConnection().createDataSet();
        ITable actualTable = databaseDataSet.getTable("USER");
        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenSelectFromRole_thenFirstNameIsAdmin() throws
            SQLException {
        ResultSet rs = connection.createStatement().executeQuery("select * from Role where id = 1");

        assertThat(rs.next()).isTrue();
        assertThat(rs.getString("name")).isEqualTo("admin");
    }

    @Test
    public void whenSelectFromUser_thenFirstLoginIsPeter() throws
            SQLException {
        ResultSet rs = connection.createStatement().executeQuery("select * from User where id = 1");

        assertThat(rs.next()).isTrue();
        assertThat(rs.getString("login")).isEqualTo("peter");
    }

    @Test
    public void whenInsertIntoRole_thenCreateNewRole() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-role.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("ROLE");

        jdbcRoleDao.create(new Role(
                3L,
                "moderator"
        ));
        ITable actualTable = getConnection().createQueryTable("result_name",
                "select * from ROLE where name='moderator'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenInsertIntoUser_thenCreateNewUser() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-user.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("USER");

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
                "select * from USER where login='denis'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenUpdateRole_thenUpdateRoleInDb() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-updated-role.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("ROLE");

        Role updatableUser = jdbcRoleDao.findById(2L);
        updatableUser.setName("player");
        jdbcRoleDao.update(updatableUser);
        ITable actualTable = getConnection().createQueryTable("result_name",
                "select * from ROLE where name='player'");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    @Test
    public void whenUpdateUser_thenUpdateUserInDb() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("dataset/expected-updated-user.xml");
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(is);
        ITable expectedTable = expectedDataSet.getTable("USER");

        User updatedUser = jdbcUserDao.findById(2L);
        updatedUser.setLogin("liza");
        jdbcUserDao.update(updatedUser);
        ITable actualTable = getConnection().createQueryTable("result_login",
                "select * from USER where login='liza'");

        Assertion.assertEquals(expectedTable, actualTable);
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
