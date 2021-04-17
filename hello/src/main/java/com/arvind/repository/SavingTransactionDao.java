package com.arvind.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import com.arvind.model.AccountBal;
import com.arvind.model.SavingTransaction;

public interface SavingTransactionDao {
	public void insert(SavingTransaction trans);
	public void delete(int transId);
	public List<SavingTransaction> findTransactions();
	public List<SavingTransaction> findTransactionsByAcctId(int acctId);
	public List<SavingTransaction> findTransactionsByAcctName(String acctName);
	public TreeMap<LocalDate, BigDecimal> getAccountBalance(AccountBal bal, int acctId);
}
