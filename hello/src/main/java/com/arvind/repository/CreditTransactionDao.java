package com.arvind.repository;

import java.util.List;

import com.arvind.model.AccountBal;
import com.arvind.model.CreditTransaction;

public interface CreditTransactionDao {
	public void insert(CreditTransaction trans);
	public void delete(int transId);
	public List<CreditTransaction> findTransactions();
	public List<CreditTransaction> findTransactionsByAcctId(int acctId);
	public List<CreditTransaction> findTransactionsByAcctName(String acctName);
	public void getAccountBalance(AccountBal bal, int acctId);
}
