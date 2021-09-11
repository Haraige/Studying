package ua.org.code.persistence.dao;

import annotation.Table;
import com.google.common.base.CaseFormat;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2
public abstract class GenericJdbcDao<E> implements Dao<E> {
    protected Connection connection;
    public GenericJdbcDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(E entity) {
        List<String> fieldsNames = fieldNamesToUnderscore(entity.getClass().getDeclaredFields());
        StringBuilder sql = new StringBuilder(
                "insert into " + getTableName(entity) + " (");
        for (int i = 0; i < fieldsNames.size() - 1; i++) {
            sql.append(fieldsNames.get(i)).append(", ");
        }
        sql.append(fieldsNames.get(fieldsNames.size() - 1)).append(") values(");
        sql.append("?, ".repeat(Math.max(0, fieldsNames.size() - 1)));
        sql.append("?)");

        List<Class<?>> entityTypes = getEntityFieldsClasses(entity);
        List<Object> entityValues = getEntityFieldsValues(entity);

        try (PreparedStatement createStatement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < fieldsNames.size(); i++) {
                setStatementValue(createStatement,
                        i + 1,
                        entityTypes.get(i),
                        entityValues.get(i));
            }
            createStatement.execute();
        } catch(SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
    }


    @Override
    public void update(E entity) {
        List<String> fieldsNames = fieldNamesToUnderscore(entity.getClass().getDeclaredFields());
        StringBuilder sql = new StringBuilder(
                "update " + entity.getClass().getSimpleName() + " set ");
        for (int i = 0; i < fieldsNames.size() - 1; i++) {
            sql.append(fieldsNames.get(i)).append("=?, ");
        }
        sql.append(fieldsNames.get(fieldsNames.size() - 1)).append("=? ");
        sql.append("where id=?");

        List<Class<?>> entityTypes = getEntityFieldsClasses(entity);
        List<Object> entityValues = getEntityFieldsValues(entity);

        try (PreparedStatement updateStatement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < fieldsNames.size(); i++) {
                setStatementValue(updateStatement,
                        i + 1,
                        entityTypes.get(i),
                        entityValues.get(i));
            }
            updateStatement.setLong(fieldsNames.size() + 1, getEntityId(entity));
            updateStatement.execute();
        } catch(SQLException e) {
            logSqlError(e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error rollback", e);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(E entity) {
        List<String> fieldsNames = fieldNamesToUnderscore(entity.getClass().getDeclaredFields());
        StringBuilder sql = new StringBuilder(
                "delete from " + entity.getClass().getSimpleName() + " where id=? ");
        try (PreparedStatement removeStatement = connection.prepareStatement(sql.toString())) {
            connection.setAutoCommit(false);
            removeStatement.setLong(1, getEntityId(entity));
            removeStatement.execute();
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
    }

    @Override
    public List<E> findAll() {
        return null;
    }

    @Override
    public E findById(Long id) {
        return null;
    }

    private String getTableName(E entity) {
        Table tableAnnotation = entity.getClass().getDeclaredAnnotation(Table.class);
        String tableName;
        if (tableAnnotation == null) {
            tableName = entity.getClass().getSimpleName();
        } else {
            tableName = tableAnnotation.name();
        }
        return tableName;
    }

    private List<String> fieldNamesToUnderscore(Field[] fields) {
        List<String> fieldsNames = new ArrayList<>();
        Arrays.stream(fields).forEach(field -> {
            fieldsNames.add(
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, field.getName()));
            field.setAccessible(true);
        });
        return fieldsNames;
    }

    private List<Class<?>> getEntityFieldsClasses(Object entity) {
        List<Class<?>> entityFieldsClasses = new ArrayList<>();
        for (Field field: entity.getClass().getDeclaredFields()) {
            entityFieldsClasses.add(field.getType());
        }
        return entityFieldsClasses;
    }

    private List<Object> getEntityFieldsValues(Object entity) {
        List<Object> entityFieldsValues = new ArrayList<>();
        for (Field field: entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                entityFieldsValues.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error getting field value!");
            }
        }
        return entityFieldsValues;
    }


    private Long getEntityId(Object obj) {
        Field entityIdField;
        Long id;
        try {
            entityIdField = obj.getClass().getDeclaredField("id");
            entityIdField.setAccessible(true);
            id = (Long) entityIdField.get(obj);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException("No id field in entity!");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("No access to field!");
        }
        return id;
    }

    private void setStatementValue(PreparedStatement statement, int index, Class<?> valueType, Object value) {
        try {
            switch (valueType.getSimpleName()) {
            case "Integer" -> statement.setInt(index,
                    (Integer) value);
            case "Long" -> statement.setLong(index, (Long) value);
            case "String" -> statement.setString(index,
                    (String) value);
            case "Double" -> statement.setDouble(index, (Double) value);
            case "Date" -> statement.setDate(index, (Date) value);
            default -> statement.setLong(index, getEntityId(value));
            }
        } catch (SQLException e) {
            log.error(e);
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
        }
    }

    protected void logSqlError(SQLException e) {
        log.error("SQLState: " + e.getSQLState() + '\n' +
                "Error Code: " + e.getErrorCode() + '\n' +
                "Message: " + e.getMessage());
    }
}
