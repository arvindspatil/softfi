package com.arvind.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import com.arvind.model.AccountBal;
import com.arvind.model.CheckingTransaction;

public interface CheckingTransactionDao {
	public void insert(CheckingTransaction trans);
	public void delete(int transId);
	public List<CheckingTransaction> findTransactions();
	public List<CheckingTransaction> findTransactionsByAcctId(int acctId);
	public List<CheckingTransaction> findTransactionsByAcctName(String acctName);
	public TreeMap<LocalDate, BigDecimal> getAccountBalance(AccountBal bal, int acctId);
}
