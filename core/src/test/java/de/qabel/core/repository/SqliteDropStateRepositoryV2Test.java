package de.qabel.core.repository;

import de.qabel.core.repository.exception.EntityNotFoundException;
import de.qabel.core.repository.sqlite.ClientDatabase;
import de.qabel.core.repository.sqlite.SqliteDropStateRepository;
import de.qabel.core.repository.sqlite.SqliteDropStateRepositoryV2;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqliteDropStateRepositoryV2Test extends AbstractSqliteRepositoryTest<SqliteDropStateRepositoryV2> {

    @Override
    protected SqliteDropStateRepositoryV2 createRepo(ClientDatabase clientDatabase, EntityManager em) throws Exception {
        return new SqliteDropStateRepositoryV2(clientDatabase, em);
    }

    @Test(expected = EntityNotFoundException.class)
    public void throwsExceptionIfNoStateWasFound() throws Exception {
        repo.getDropState("not existing");
    }

    @Test
    public void knowsSavedDrops() throws Exception {
        repo.setDropState("drop", "state");
        assertEquals("state", repo.getDropState("drop"));
    }

    @Test
    public void replaceSavedDrops() throws Exception {
        repo.setDropState("drop", "state");
        assertEquals("state", repo.getDropState("drop"));
        repo.setDropState("drop", "state2");
        assertEquals("state", repo.getDropState("drop"));
    }


}
