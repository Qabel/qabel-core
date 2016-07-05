package de.qabel.core.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.qabel.core.repository.DropStateRepository;
import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.exception.PersistenceException;

public class SqliteDropStateRepository implements DropStateRepository {
    public static final String TABLE_NAME = "drop_state";
    private ClientDatabase database;

    public SqliteDropStateRepository(ClientDatabase database) {
        this.database = database;
    }

    @Override
    public String getDropState(String drop) throws EntityNotFoundException, PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "SELECT `last_request_stamp` FROM " + TABLE_NAME + " WHERE `drop` = ? LIMIT 1"
        )) {
            statement.setString(1, drop);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntityNotFoundException("no state found for drop '" + drop + "'");
                }

                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new PersistenceException("failed to load drop state: " + e.getMessage(), e);
        }
    }

    @Override
    public void setDropState(String drop, String state) throws PersistenceException {
        try (PreparedStatement statement = database.prepare(
            "INSERT INTO " + TABLE_NAME + " (`drop`, `last_request_stamp`) VALUES (?, ?)"
        )) {
            statement.setString(1, drop);
            statement.setString(2, state);
            statement.execute();
        } catch (SQLException e) {
            throw new PersistenceException("failed to save drop " + e.getMessage(), e);
        }
    }
}
