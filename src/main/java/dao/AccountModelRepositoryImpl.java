package dao;

import config.CustomDataSource;
import exception.GenericException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Todo make class method generic so that they can handle any creation and updates from any object
public class AccountModelRepositoryImpl implements AccountModelRepository {

    private static final Logger logger = LoggerFactory.getLogger(AccountModelRepositoryImpl.class);
    private CustomDataSource dataSource;

    public AccountModelRepositoryImpl() {
        dataSource = CustomDataSource.getDataSource();
    }

    @Override
    public Optional<Account> get(Long id) {
        String sql = "Select * from account where id = " + id;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setBalance(rs.getFloat("balance"));
                return Optional.of(account);
            }
        } catch (Exception ex) {
            logger.error("Exception Occurred in getting account \n", ex);
            throw new GenericException(ex.getMessage());
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
        return Optional.empty();
    }

    public Set<Account> getAccountsByIds(List<Long> ids) {
        StringBuilder querySql = new StringBuilder("Select * from account where id in ( ");
        for (int i = 0; i < ids.size() - 1; i++) {
            querySql.append(ids.get(i)).append(",");
        }
        querySql.append(ids.get(ids.size() - 1))
                .append(" );");

        Set<Account> accounts = new HashSet<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(querySql.toString());
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setBalance(rs.getFloat("balance"));
                accounts.add(account);
            }
            return accounts;
        } catch (Exception ex) {
            logger.error("Exception Occurred in get accounts \n", ex);
            throw new GenericException(ex.getMessage());
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
    }

    @Override
    public Set<Account> getAll() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String sql = "Select * from account ";
            Set<Account> accounts = new HashSet<>();
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setBalance(rs.getFloat("balance"));
                accounts.add(account);
            }
            return accounts;
        } catch (Exception ex) {
            logger.error("Exception Occurred in get all accounts \n", ex);
            throw new GenericException(ex.getMessage());
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
    }

    @Override
    public Account save(Account account) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            logger.info("Account to create is \n {}", account);
            String sql = new StringBuilder("insert into account (balance) values (")
                .append(account.getBalance())
                .append(");").toString();

            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                account.setId(rs.getLong(1));
                logger.info("Inserted account is \n{}", account);
                return account;
            }
            //Todo make a exception message class
            throw new GenericException("Unable to create account, No Id obtained");
        } catch (Exception ex) {
            logger.error("Exception Occurred in account creation \n", ex);
            throw new GenericException(ex.getMessage());
        } finally {
            dataSource.cleanupResources(conn, stmt, rs);
        }
    }

    @Override
    public void update(Account account) {
        logger.info("Account to update is \n {}", account);
        String updateSql = buildUpdateSql(account);
        updateDatabaseWith(updateSql);
    }

    public void updateBalances(Account originatorAcc, Account beneficiaryAcc) {
        logger.info("Accounts to updates are \n {} \n {}", originatorAcc, beneficiaryAcc);
        String updateSql = buildSqlForUpdatingBalances(originatorAcc, beneficiaryAcc);
        updateDatabaseWith(updateSql);
    }

    @Override
    // Todo u cannot delete the accocunt if it has some balances
    public boolean delete(Long id) {
        String sql = "delete from account where id = " + id;
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            int status = stmt.executeUpdate(sql);
            return status >= 0;
        } catch (Exception e) {
            logger.error("Exception Occurred in deleting accounts \n", e);
        } finally {
            dataSource.cleanupResources(conn, stmt, null);
        }
        return false;
    }

    private String buildUpdateSql(Account account) {
        return new StringBuilder("update account set ")
            .append("balance = ")
            .append(account.getBalance())
            .append("where id = ")
            .append(account.getId())
            .append(";").toString();
    }

    private String buildSqlForUpdatingBalances(Account originatorAcc, Account beneficiaryAcc) {
        return new StringBuilder("UPDATE account SET  balance = CASE WHEN  id = ")
            .append(originatorAcc.getId())
            .append(" THEN ")
            .append(originatorAcc.getBalance())
            .append(" WHEN id = ")
            .append(beneficiaryAcc.getId())
            .append(" THEN ")
            .append(beneficiaryAcc.getBalance())
            .append(" END WHERE id IN ( ")
            .append(originatorAcc.getId())
            .append(",")
            .append(beneficiaryAcc.getId())
            .append(");").toString();
    }

    private void updateDatabaseWith(String sql) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = dataSource.getDatabaseConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
            logger.error("Update Query {} executed successfully", sql);
        } catch (Exception e) {
            logger.error("Exception Occurred in updating using sql: \n {} exception is: \n", sql, e);
        } finally {
            dataSource.cleanupResources(conn, stmt, null);
        }
    }

}
