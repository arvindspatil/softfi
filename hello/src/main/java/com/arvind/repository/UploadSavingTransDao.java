package com.arvind.repository;

import java.util.List;

import com.arvind.model.SavingTransaction;

public interface UploadSavingTransDao {
	public List<SavingTransaction> findTransactionsByAcctId(int acctId);
	public List<SavingTransaction> findTransactionsById(int transId);
	public void insert(SavingTransaction trans);
	public void delete(int transId);
	public void updatePayeeMap(String inDesc, String stdDesc);
}
