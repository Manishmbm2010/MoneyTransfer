package service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

import dao.AccountModelRepositoryImpl;
import dto.AccountDto;
import exception.RecordNotPresentException;
import java.util.HashSet;
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
class AccountServiceTest {

    @Mock
    private AccountModelRepositoryImpl repository;
    @InjectMocks
    private AccountService accountService = AccountService.getAccountService();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldReturnAccWhenIdExistInDb() {
        when(repository.get(anyLong()))
            .thenReturn(Optional.of(new Account(1L, 1000F)));

        Account account = accountService.get(1L);

        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals(1000F, account.getBalance());
    }

    @Test
    void shouldThrowExceptionAccWhenIdDoesnotExistInDb() {
        when(repository.get(anyLong()))
            .thenReturn(Optional.empty());
        RecordNotPresentException recordNotPresentException = assertThrows(RecordNotPresentException.class,
            () -> accountService.get(1L));
        assertEquals("Account with id " + 1 + " doesn't exist",
            recordNotPresentException.getMessage());
    }

    @Test
    void shouldReturnAllAccounts() {
        Set<Account> expectedAccounts = Set.of(
            new Account(1L, 1000F),
            new Account(2L, 500F));
        when(repository.getAll()).thenReturn(expectedAccounts);

        Set<Account> accounts = accountService.getAll();

        assertNotNull(accounts);
        assertEquals(2, accounts.size());
        assertTrue(accounts.containsAll(expectedAccounts));
    }

    @Test
    void shouldReturnEmptyCollectionOfAccount() {
        Set<Account> expectedAccounts = new HashSet<>();
        when(repository.getAll()).thenReturn(expectedAccounts);

        Set<Account> accounts = accountService.getAll();

        assertNotNull(accounts);
        assertEquals(0, accounts.size());
        assertTrue(accounts.containsAll(expectedAccounts));
    }

    @Test
    void shouldCreateAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setBalance(1000F);
        Account expectedAccount = new Account(1L, accountDto.getBalance());
        when(repository.save(any(Account.class)))
            .thenReturn(expectedAccount);

        Account actualAccount = accountService.create(accountDto);

        assertNotNull(actualAccount);
        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    void shouldUpdateAccount() {
        Account accountToUpdate = new Account(1L, 1000F);
        when(repository.get(anyLong())).thenReturn(Optional.of(accountToUpdate));

        assertDoesNotThrow(() -> accountService.update(accountToUpdate));
    }

    @Test
    void shouldThrowExceptionWhenIdNotPresentInDb() {
        Account accountToUpdate = new Account(1L, 1000F);
        when(repository.get(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecordNotPresentException.class, () -> accountService.update(accountToUpdate));
    }

    @Test
    void shouldDeleteAccount() {
        when(repository.delete(anyLong())).thenReturn(true);
        // todo decide whether delete should return something or not
    }
}
