package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import config.CustomDataSource;
import exception.GenericException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Tag("unit")
class AccountModelRepositoryImplTest {

    @Mock
    private CustomDataSource dataSource;
    @Mock
    private Statement stmt;
    @Mock
    private Connection connection;
    @Mock
    private ResultSet rs;
    @InjectMocks
    private AccountModelRepositoryImpl repository = new AccountModelRepositoryImpl();

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        when(stmt.executeQuery(anyString())).thenReturn(rs);
        when(dataSource.getDatabaseConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(stmt);
        when(stmt.getGeneratedKeys()).thenReturn(rs);
    }

    @Test
    void getShouldReturnAccountWhenIdPresentInDb() throws SQLException {
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getFloat("balance")).thenReturn(1000F);

        Optional<Account> account = repository.get(1L);

        assertTrue(account.isPresent());
        assertEquals(1L, account.get().getId());
        assertEquals(1000F, account.get().getBalance());
    }

    @Test
    void getShouldReturnEmptyWhenNoDataReturnedFromDb() throws SQLException {
        when(rs.next()).thenReturn(false);

        Optional<Account> account = repository.get(1L);

        assertTrue(account.isEmpty());
    }

    @Test
    void getShouldHandleAllExceptionAndRethrowGenericException() throws SQLException {
        String exceptionMessage = "Some Sql Exception";
        when(rs.next()).thenThrow(new SQLException(exceptionMessage));

        GenericException exception = assertThrows(GenericException.class, () -> repository.get(1L));

        assertEquals(exceptionMessage, exception.getMessage());
    }


    @Test
    void getAllShouldReturnAllAccountsFromDb() throws SQLException {
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getLong("id")).thenReturn(1L).thenReturn(2L);
        when(rs.getFloat("balance")).thenReturn(1000F).thenReturn(2000F);

        Set<Account> accounts = repository.getAll();

        Set<Account> expectedAccounts = Set.of(
            new Account(1L, 1000F),
            new Account(2L, 2000F)
        );
        assertEquals(2, accounts.size());
        assertEquals(expectedAccounts, accounts);
    }

    @Test
    void getAllShouldReturnEmptyCollectionWhenNoDataReturnedFromDb() throws SQLException {
        when(rs.next()).thenReturn(false);

        Set<Account> accounts = repository.getAll();

        assertTrue(accounts.isEmpty());
    }

    @Test
    void getAllShouldHandleAllExceptionAndRethrowGenericException() throws SQLException {
        String exceptionMessage = "Some Sql Exception";
        when(rs.next()).thenThrow(new SQLException(exceptionMessage));

        GenericException exception = assertThrows(GenericException.class, () -> repository.getAll());

        assertEquals(exceptionMessage, exception.getMessage());
    }


    @Test
    void getByIdsShouldReturnAccountsIfIdExitInDb() throws SQLException {
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getLong("id")).thenReturn(1L).thenReturn(2L);
        when(rs.getFloat("balance")).thenReturn(1000F).thenReturn(2000F);

        Set<Account> accounts = repository.getAccountsByIds(List.of(1L,2L));

        Set<Account> expectedAccounts = Set.of(
            new Account(1L, 1000F),
            new Account(2L, 2000F)
        );
        assertEquals(2, accounts.size());
        assertEquals(expectedAccounts, accounts);
    }

    @Test
    void getByIdsShouldReturnEmptyCollectionWhenNoDataReturnedFromDb() throws SQLException {
        when(rs.next()).thenReturn(false);

        Set<Account> accounts = repository.getAccountsByIds(List.of(1L,2L));

        assertTrue(accounts.isEmpty());
    }

    @Test
    void getByIdsShouldHandleAllExceptionAndRethrowGenericException() throws SQLException {
        String exceptionMessage = "Some Sql Exception";
        when(rs.next()).thenThrow(new SQLException(exceptionMessage));

        GenericException exception = assertThrows(GenericException.class, () -> repository.getAccountsByIds(List.of(1L,2L)));

        assertEquals(exceptionMessage, exception.getMessage());
    }


    @Test
    void saveShouldCreateAccount() throws SQLException {
        Account account = new Account(null, 1000F);
        when(rs.next()).thenReturn(true);
        when(rs.getLong(1)).thenReturn(1L);

        Account savedAccount = repository.save(account);

        assertNotNull(savedAccount);
        assertEquals(1L, savedAccount.getId());
        assertEquals(account.getBalance(), savedAccount.getBalance());
    }

    @Test
    void saveShouldThrowGenericExceptionWhenUniqueIdCannotBeObtained() throws SQLException {
        Account account = new Account(null, 1000F);
        when(rs.next()).thenReturn(false);

        GenericException exception = assertThrows(GenericException.class, () -> repository.save(account));
        assertEquals("Unable to create account, No Id obtained", exception.getMessage());
    }

    @Test
    void saveShouldHandleAllExceptionAndRethrowGenericException() throws SQLException {
        String exceptionMessage = "Some Sql Exception";
        when(rs.next()).thenThrow(new SQLException(exceptionMessage));

        GenericException exception = assertThrows(GenericException.class, () -> repository.get(1L));

        assertEquals(exceptionMessage, exception.getMessage());
    }

}
