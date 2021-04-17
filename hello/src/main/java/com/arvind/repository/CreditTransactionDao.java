package com.arvind.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import com.arvind.model.AccountBal;
import com.arvind.model.CreditTransaction;

public interface CreditTransactionDao {
	public void insert(CreditTransaction trans);
	public void delete(int transId);
	public List<CreditTransaction> findTransactions();
	public List<CreditTransaction> findTransactionsByAcctId(int acctId);
	public List<CreditTransaction> findTransactionsByAcctName(String acctName);
	public TreeMap<LocalDate, BigDecimal> getAccountBalance(AccountBal bal, int acctId);
}
