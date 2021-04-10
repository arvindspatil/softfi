package com.arvind.repository;

import java.util.List;

import com.arvind.model.InvestmentTransaction;

public interface UploadInvestmentTransDao {
	public List<InvestmentTransaction> findTransactionsByAcctId(int acctId);
	public List<InvestmentTransaction> findTransactionsById(int transId);
	public void insert(InvestmentTransaction trans);
	public void delete(int transId);
}
