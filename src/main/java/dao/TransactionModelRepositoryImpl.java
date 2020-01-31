package dao;

import config.CustomDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import model.Transaction;
import model.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionModelRepositoryImpl implements TransactionModelRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionModelRepositoryImpl.class);
    private static TransactionModelRepositoryImpl transactionRepository;
    private CustomDataSource dataSource;

    private TransactionModelRepositoryImpl() {
        dataSource = CustomDataSource.getDataSource();
    }

    public static TransactionModelRepositoryImpl getTransactionRepository() {
        if (transactionRepository != null) {
            return transactionRepository;
        }
        transactionRepository = new TransactionModelRepositoryImpl();
        return transactionRepository;
    }

    @Override
    public Optional<Transaction> get(Long id) {
        String sql = "Select * from Transaction where id = " + id;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Transaction transaction = getTransaction(rs);
                return Optional.of(transaction);
            }
        } catch (Exception e) {
            logger.error("Exception Occurred in get transaction \n", e);
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
        return Optional.empty();
    }

    @Override
    public Set<Transaction> getAll() {
        String sql = "Select * from TRANSACTION ;";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Set<Transaction> transactions = new HashSet<>();
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Transaction transaction = getTransaction(rs);
                transactions.add(transaction);
            }
        } catch (Exception e) {
            logger.error("Exception Occurred in getting all transactions \n", e);
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
        return transactions;
    }

    @Override
    public Transaction save(Transaction transaction) {
        logger.info("Transaction to insert is \n {}", transaction);
        String sql = buildInsertSql(transaction);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                transaction.setId(rs.getLong(1));
                logger.info("Inserted transaction is \n{}", transaction);
                return transaction;
            }
            throw new SQLException("Transaction insertion failed, No id obtained");
        } catch (Exception e) {
            logger.error("Exception Occurred in saving transaction details \n", e);
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
        return null;
    }

    @Override
    public void update(Transaction transaction) {
        logger.info("Transaction to update is \n {}", transaction);
        String sql = buildUpdateSql(transaction);
        logger.debug("update sql is {}", sql);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
            logger.info("Transaction id {} ,status updated successfully ", transaction.getId());
        } catch (Exception e) {
            logger.error("Exception Occurred in updating transaction status \n", e);
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
    }

    @Override
    public boolean delete(Long id) {
        //Todo transaction once inserted should not be deleted
        // Throw not implemented exception or Method not allowed something
        return false;
    }

    private String buildInsertSql(Transaction transaction) {
        return new StringBuilder(
            "insert into transaction (originator_Id, beneficiary_Id,transfer_amount,status) values (")
            .append(transaction.getOriginatorId())
            .append(",")
            .append(transaction.getBeneficiaryId())
            .append(",")
            .append(transaction.getTransferAmount())
            .append(",'")
            .append(transaction.getStatus().toString())
            .append("');").toString();
    }

    private String buildUpdateSql(Transaction transaction) {
        return new StringBuilder("update transaction set ")
            .append("status = '")
            .append(transaction.getStatus().toString())
            .append("' ")
            .append("where id = ")
            .append(transaction.getId())
            .append(";").toString();
    }

    private Transaction getTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setBeneficiaryId(rs.getLong("beneficiary_Id"));
        transaction.setOriginatorId(rs.getLong("originator_Id"));
        transaction.setTransferAmount(rs.getFloat("transfer_amount"));
        transaction.setStatus(TransactionStatus.valueOf(rs.getString("status")));
        return transaction;
    }
}
