package com.arvind.repository;

import java.util.List;

import com.arvind.model.AccountBal;
import com.arvind.model.SavingTransaction;

public interface SavingTransactionDao {
	public void insert(SavingTransaction trans);
	public void delete(int transId);
	public List<SavingTransaction> findTransactions();
	public List<SavingTransaction> findTransactionsByAcctId(int acctId);
	public List<SavingTransaction> findTransactionsByAcctName(String acctName);
	public void getAccountBalance(AccountBal bal, int acctId);
}
