package dao;

import java.util.List;
import java.util.Set;
import model.Account;

interface AccountModelRepository extends ModelRepository<Account, Long> {

    void updateBalances(Account originatorAcc, Account beneficiaryAcc);

    Set<Account> getAccountsByIds(List<Long> ids);
}
