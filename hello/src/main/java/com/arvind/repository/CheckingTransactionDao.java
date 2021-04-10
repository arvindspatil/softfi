package com.arvind.repository;

import java.util.List;

import com.arvind.model.AccountBal;
import com.arvind.model.CheckingTransaction;

public interface CheckingTransactionDao {
	public void insert(CheckingTransaction trans);
	public void delete(int transId);
	public List<CheckingTransaction> findTransactions();
	public List<CheckingTransaction> findTransactionsByAcctId(int acctId);
	public List<CheckingTransaction> findTransactionsByAcctName(String acctName);
	public void getAccountBalance(AccountBal bal, int acctId);
}
