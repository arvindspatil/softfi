package com.arvind.repository;

import java.util.List;

import com.arvind.model.AccountBal;
import com.arvind.model.LoanTransaction;

public interface LoanTransactionDao {
	public void insert(LoanTransaction trans);
	public void delete(int transId);
	public List<LoanTransaction> findTransactions();
	public List<LoanTransaction> findTransactionsByAcctId(int acctId);
	public List<LoanTransaction> findTransactionsByAcctName(String acctName);
	public void getAccountBalance(AccountBal bal, int acctId);
}
