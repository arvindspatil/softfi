package com.arvind.repository;

import java.util.List;

import com.arvind.model.LoanTransaction;

public interface UploadLoanTransDao {
	public List<LoanTransaction> findTransactionsByAcctId(int acctId);
	public List<LoanTransaction> findTransactionsById(int transId);
	public void insert(LoanTransaction trans);
	public void delete(int transId);
}
