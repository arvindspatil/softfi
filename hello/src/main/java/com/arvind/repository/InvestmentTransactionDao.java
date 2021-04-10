package com.arvind.repository;

import java.util.List;

import com.arvind.model.InvestmentTransaction;

public interface InvestmentTransactionDao {
	public void insert(InvestmentTransaction trans);
	public void delete(int transId);
	public List<InvestmentTransaction> findTransactions();
	public List<InvestmentTransaction> findTransactionsByAcctId(int acctId);
	public List<InvestmentTransaction> findTransactionsByAcctName(String acctName);
}
