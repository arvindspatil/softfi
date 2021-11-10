package com.arvind.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import com.arvind.model.AccountBal;
import com.arvind.model.OtherTransaction;

public interface OtherTransactionDao {
	public void insert(OtherTransaction trans);
	public void delete(int transId);
	public List<OtherTransaction> findTransactions();
	public List<OtherTransaction> findTransactionsByAcctId(int acctId);
	public List<OtherTransaction> findTransactionsByAcctName(String acctName);
	public TreeMap<LocalDate, BigDecimal> getAccountBalance(AccountBal bal, int acctId);
}
