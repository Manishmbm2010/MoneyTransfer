package service;

import dao.TransactionModelRepositoryImpl;
import dto.TransactionDto;
import exception.InvalidInputException;
import exception.RecordNotPresentException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Account;
import model.Transaction;
import model.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static TransactionService transactionService;
    private ConcurrentHashMap<Long,Long> lockMap = new ConcurrentHashMap<>();
    private TransactionModelRepositoryImpl repository;
    private ExecutorService executorService = Executors.newFixedThreadPool(100);
    private AccountService accountService;
    private TransactionService() {
        repository = TransactionModelRepositoryImpl.getTransactionRepository();
        accountService = AccountService.getAccountService();
    }

    public static TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new TransactionService();
        }
        return transactionService;
    }

    public Transaction getTransaction(long transactionId) {
        Optional<Transaction> transaction = repository.get(transactionId);
        if (transaction.isEmpty()) {
            throw new RecordNotPresentException("Transaction with id " + transactionId + " doesn't exist");
        }
        return transaction.get();
    }

    public Set<Transaction> getAllTransaction() {
        return repository.getAll();
    }

    public void updateTransactionStatus(Transaction transaction) {
        if (repository.get(transaction.getId()).isEmpty()) {
            throw new RecordNotPresentException("Transaction with id " + transaction.getId() + " doesn't exist");
        }
        repository.update(transaction);
    }


    // todo method from batch or async thread should not be sent out and thread local wont be used there
    public Transaction initiateTransaction(TransactionDto transactionDto) {
        Transaction transaction = toTransactionFrom(transactionDto);
        //todo check if originator and beneficary exists in database
        //todo precheck before inserting transaction in database

        // 1.  fetch both the accounts and check if oginator has suffiecnt balance at that time
        // if yes then insert the transaction else throws exception say insufficient balance
        Set<Account> accounts = accountService.getAccountsByIds(
            Arrays.asList(transaction.getOriginatorId(), transaction.getBeneficiaryId())
        );
        boolean isExist = checkIfOriginatorAndBeneficaryAccountExist(accounts, transaction);
        if (!isExist) {
            // todo throw some exception
        }
        Transaction insertedTransaction = repository.save(transaction);
        executorService.execute(() -> {
            try {
                process(insertedTransaction);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return insertedTransaction;
    }

    private Boolean checkIfOriginatorAndBeneficaryAccountExist(Set<Account> accounts, Transaction transaction) {
        Account originatorAcc = null;
        Account beneficiaryAcc = null;
        for (Account account : accounts) {
            if (account.getId().equals(transaction.getOriginatorId())) {
                originatorAcc = account;
                continue;
            }
            if (account.getId().equals(transaction.getBeneficiaryId())) {
                beneficiaryAcc = account;
            }
        }
        if (originatorAcc == null) {
            logger.error("Error fetching originator account or originator account doesn't exist");
            throw new InvalidInputException("Originator Doesn't exist");
        }
        if (beneficiaryAcc == null) {
            logger.error("Error fetching beneficiary account or beneficiary account doesn't exist");
            throw new InvalidInputException("beneficiary Doesn't exist");
        }
        return true;
    }

    private void process(Transaction transaction) throws InterruptedException {
        // This block make sure we get lock and retry at least 10 times. After that I
        int retryAvailable = 10;
        boolean isLockObtainedOnAccounts = false;
        while (retryAvailable > 0) {
            retryAvailable--;
            Long lockedByTransactionId = lockMap.putIfAbsent(transaction.getOriginatorId(), transaction.getId());
            if (lockedByTransactionId != null && !lockedByTransactionId.equals(transaction.getId())) {
                logger.info("Originator account is locked by transaction id {}", lockedByTransactionId);
                Thread.sleep(10);
                continue;
            }
            lockedByTransactionId = lockMap.putIfAbsent(transaction.getBeneficiaryId(), transaction.getId());
            if (lockedByTransactionId != null && !lockedByTransactionId.equals(transaction.getId())) {
                logger.info("Beneficiary account is locked by transaction id {}", lockedByTransactionId);
                lockMap.remove(transaction.getOriginatorId());
                Thread.sleep(10);
                continue;
            }
            isLockObtainedOnAccounts = true;
            break;
        }

        if (!isLockObtainedOnAccounts) {
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setReason("Lock not obtained in all retry attempts, should be processed in some batch");
            repository.save(transaction);
            return;
        }
        transaction.setStatus(TransactionStatus.IN_PROGRESS);
        repository.update(transaction);

        //checkOriginatorBalance();
        // Todo merge 2 get calls on accountRepo to one

        // todo transaction originatorids and beneficary ids should be object account
        Set<Account> accounts = accountService.getAccountsByIds(
            Arrays.asList(transaction.getOriginatorId(), transaction.getBeneficiaryId())
        );

        Account originatorAcc = null;
        Account beneficiaryAcc = null;
        for (Account account : accounts) {
            if (account.getId().equals(transaction.getOriginatorId())) {
                originatorAcc = account;
                continue;
            }
            if (account.getId().equals(transaction.getBeneficiaryId())) {
                beneficiaryAcc = account;
            }
        }
        if (originatorAcc == null || beneficiaryAcc == null) {
            logger.error("Error fetching account details from database");
        }

        if (originatorAcc.getBalance() < transaction.getTransferAmount()) {
            logger.info("Insufficient balance !!");
            logger.info("Transfer amount {} exceeded from available balance {}", transaction.getTransferAmount(),
                originatorAcc.getBalance());
            transaction.setStatus(TransactionStatus.REJECTED);
            transaction.setReason("Insufficient Balance !!!!");
            repository.save(transaction);
            return;
        }

        originatorAcc.setBalance(originatorAcc.getBalance() - transaction.getTransferAmount());
        beneficiaryAcc.setBalance(beneficiaryAcc.getBalance() + transaction.getTransferAmount());
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        accountService.updateBalances(originatorAcc, beneficiaryAcc);

        //unlock account for another transaction
        //Todo should be in finally block and must be executed if lock is granted successfully
        lockMap.remove(transaction.getOriginatorId());
        lockMap.remove(transaction.getBeneficiaryId());

        // Update transaction status
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        repository.update(transaction);
    }

    private Transaction toTransactionFrom(TransactionDto transactionDto) {
        Transaction transaction = new Transaction();
        transaction.setOriginatorId(transactionDto.getOriginatorId());
        transaction.setBeneficiaryId(transactionDto.getBeneficiaryId());
        transaction.setTransferAmount(transactionDto.getTransferAmount());
        transaction.setStatus(TransactionStatus.SUBMITTED);
        return transaction;
    }

}
