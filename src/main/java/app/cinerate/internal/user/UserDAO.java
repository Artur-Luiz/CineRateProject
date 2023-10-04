package app.cinerate.internal.user;

import app.cinerate.internal.connection.ConnectionFactory;
import app.cinerate.internal.dao.GenericDAO;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserDAO implements GenericDAO<User> {

    private final ConnectionFactory connectionFactory;

    @Autowired
    public UserDAO(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;

        createTable();
    }


    @Override
    public void createTable() {
        var userTable = """
                CREATE TABLE IF NOT EXISTS user
                (
                    id       INTEGER PRIMARY KEY,
                    nickname VARCHAR(255) UNIQUE                                    NOT NULL,
                    password VARCHAR(100)                                           NOT NULL,
                    role     VARCHAR(10) CHECK ( role IN ('USER', 'ADMIN', 'MOD') ) NOT NULL DEFAULT 'USER'
                )
                """;

        var ratingTable = """
                CREATE TABLE IF NOT EXISTS rating
                (
                    movie_id   INTEGER                                                                    NOT NULL,
                    user_id    INTEGER                                                                    NOT NULL,
                    commentary TEXT,
                    favorite   BOOLEAN                                                                    NOT NULL,
                    rating     INT(10),
                    status     VARCHAR(20) CHECK ( status IN ('PENDING', 'PLAN_TO_WATCH', 'COMPLETED') )  NOT NULL,
                    PRIMARY KEY (movie_id, user_id),
                    FOREIGN KEY (user_id) REFERENCES user (id)
                )
                """;

        try (var connection = connectionFactory.createConnection()) {
            var queries = List.of(userTable, ratingTable);
            for (String query : queries) {
                try (var statement = connection.prepareStatement(query)) {
                    statement.execute();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long insert(User entity) {

        try (var connection = connectionFactory.createConnection();
             var userStatement = connection.prepareStatement("INSERT INTO user (id, nickname, password, role) VALUES (null, ?, ?, ?)")) {

            userStatement.setString(1, entity.getNickname());
            userStatement.setString(2, entity.getEncryptedPassword());
            userStatement.setString(3, entity.getRole().name());

            userStatement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    @Override
    public void update(User entity) {
        if (entity.getId() == null)
            throw new IllegalStateException("User id cannot be null, please insert a user before updating it");

        try (var connection = connectionFactory.createConnection();
             var userStatement = connection.prepareStatement("UPDATE user SET password = ?, role = ? WHERE id = ?");
             var ratingStatement = connection.prepareStatement("INSERT OR REPLACE INTO rating (movie_id, user_id, commentary, favorite, rating, status) VALUES (?, ?, ?, ?, ?, ?)")
        ) {

            userStatement.setString(1, entity.getEncryptedPassword());
            userStatement.setString(2, entity.getRole().name());
            userStatement.setLong(3, entity.getId());

            userStatement.executeUpdate();

            for (Rating rating : entity.getRatings()) {
                ratingStatement.setLong(1, rating.getMovieId());
                ratingStatement.setLong(2, entity.getId());
                ratingStatement.setString(3, rating.getCommentary());
                ratingStatement.setBoolean(4, rating.isFavorite());
                ratingStatement.setInt(5, rating.getRating());
                ratingStatement.setString(6, rating.getStatus().name());

                ratingStatement.addBatch();
            }

            ratingStatement.executeLargeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(User entity) {
        if (entity.getId() == null)
            throw new IllegalStateException("User id cannot be null, please insert a user before deleting it");

        try (var connection = connectionFactory.createConnection();
             var userStatement = connection.prepareStatement("DELETE FROM user WHERE id = ?");
             var ratingStatement = connection.prepareStatement("DELETE FROM rating WHERE user_id = ?")
        ) {

            userStatement.setLong(1, entity.getId());
            ratingStatement.setLong(1, entity.getId());

            userStatement.executeUpdate();
            ratingStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<User> findById(Long id) {

        try (var connection = connectionFactory.createConnection();
             var statement = connection.prepareStatement(
                     """
                             SELECT *
                             FROM user
                                 LEFT JOIN rating on user.id = rating.user_id AND rating.status != 'PENDING'
                             WHERE user.id = ?
                             """
             );
        ) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                return Optional.ofNullable(getUserFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<User> findByNickname(String nickname) {
        try (var connection = connectionFactory.createConnection();
             var statement = connection.prepareStatement(
                     """
                             SELECT *
                             FROM user
                                 LEFT JOIN rating on user.id = rating.user_id AND rating.status != 'PENDING'
                             WHERE user.nickname = ?
                             """
             );
        ) {
            statement.setString(1, nickname);

            try (var resultSet = statement.executeQuery()) {
                return Optional.ofNullable(getUserFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Nullable
    private User getUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = null;

        var ratings = new ArrayList<Rating>();

        while (resultSet.next()) {
            if (user == null) {
                user = new User(
                        resultSet.getLong("id"),
                        resultSet.getString("nickname"),
                        ratings,
                        resultSet.getString("password"),
                        Role.valueOf(resultSet.getString("role"))
                );
            }

            var movieId = resultSet.getLong("movie_id");
            if (movieId == 0) {
                continue;
            }

            var rating = new Rating(
                    resultSet.getInt("movie_id"),
                    resultSet.getString("commentary"),
                    resultSet.getInt("rating"),
                    resultSet.getBoolean("favorite"),
                    Rating.Status.valueOf(resultSet.getString("status"))
            );

            ratings.add(rating);

        }
        return user;
    }

}
