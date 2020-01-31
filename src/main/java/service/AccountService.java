package service;

import dao.AccountModelRepositoryImpl;
import dto.AccountDto;
import exception.RecordNotPresentException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static AccountService accountService;

    private AccountModelRepositoryImpl repository;

    private AccountService() {
        repository = new AccountModelRepositoryImpl();
    }

    public static AccountService getAccountService() {
        if (accountService == null) {
            accountService = new AccountService();
        }
        return accountService;
    }

    public Account get(Long accountId) {
        Optional<Account> account = repository.get(accountId);
        if (account.isEmpty()) {
            throw new RecordNotPresentException("Account with id " + accountId + " doesn't exist");
        }
        return account.get();
    }

    public Set<Account> getAll() {
        return repository.getAll();
    }

    public Account create(AccountDto accountDto) {
        Account account = toAccountFrom(accountDto);
        return repository.save(account);
    }

    public void update(Account account) {
        if (repository.get(account.getId()).isEmpty()) {
            logger.info("Account with id {} not present in database", account.getId());
            throw new RecordNotPresentException("Account with id " + account.getId() + " doesn't exist");
        }
        repository.update(account);
    }

    public void delete(Long accountId) {
        // todo check if balance is zero and their is no lock on the account just to mark is safe for deletion
        repository.delete(accountId);
    }

    void updateBalances(Account originatorAcc, Account beneficiaryAcc) {
        repository.updateBalances(originatorAcc, beneficiaryAcc);
    }

    Set<Account> getAccountsByIds(List<Long> ids) {
        return repository.getAccountsByIds(ids);
    }

    private Account toAccountFrom(AccountDto accountDto) {
        Account account = new Account();
        account.setBalance(accountDto.getBalance());
        return account;
    }
}
