package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {
    private final CustomDataSource dataSource = CustomDataSource.getInstance();

    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String CREATE_USER = "INSERT INTO myusers(firstname, lastname, age) VALUES(?, ?, ?)";
    private static final String UPDATE_USER = "UPDATE myusers SET firstname = ?, lastname = ?, age = ? WHERE id = ?";
    private static final String DELETE_USER = "DELETE FROM myusers WHERE id = ?";
    private static final String FIND_USER_BY_ID = "SELECT * FROM myusers WHERE id = ?";
    private static final String FIND_USER_BY_NAME = "SELECT * FROM myusers WHERE firstname = ?";
    private static final String FIND_ALL_USERS = "SELECT * FROM myusers";

    public Long createUser(User user) {
        Long id = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(CREATE_USER, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, user.getFirstName());
            statement.setObject(2, user.getLastName());
            statement.setObject(3, user.getAge());
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    public User findUserById(Long userId) {
        User user = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(FIND_USER_BY_ID)) {
            statement.setLong(1, userId);
            statement.execute();
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = getAllUserParametersFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User findUserByName(String userName) {
        User user = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(FIND_USER_BY_NAME)) {
            statement.setString(1, userName);
            statement.execute();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                user = getAllUserParametersFromResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public List<User> findAllUser() {
        List<User> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(FIND_ALL_USERS)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(getAllUserParametersFromResultSet(resultSet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public User updateUser(User user) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(UPDATE_USER)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setInt(3, user.getAge());
            statement.setLong(4, user.getId());
            if (statement.executeUpdate() != 0) {
                return findUserById(user.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public void deleteUser(Long userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(DELETE_USER)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User getAllUserParametersFromResultSet(ResultSet rs)
            throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("firstname"))
                .lastName(rs.getString("lastname"))
                .age(rs.getInt("age"))
                .build();
    }
}
