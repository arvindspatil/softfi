package com.arvind.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import com.arvind.model.AccountBal;
import com.arvind.model.LoanTransaction;

public interface LoanTransactionDao {
	public void insert(LoanTransaction trans);
	public void delete(int transId);
	public List<LoanTransaction> findTransactions();
	public List<LoanTransaction> findTransactionsByAcctId(int acctId);
	public List<LoanTransaction> findTransactionsByAcctName(String acctName);
	public TreeMap<LocalDate, BigDecimal> getAccountBalance(AccountBal bal, int acctId);
}
