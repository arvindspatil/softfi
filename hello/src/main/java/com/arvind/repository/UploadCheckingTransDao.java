package com.arvind.repository;

import java.util.List;

import com.arvind.model.CheckingTransaction;

public interface UploadCheckingTransDao {
	public List<CheckingTransaction> findTransactionsByAcctId(int acctId);
	public List<CheckingTransaction> findTransactionsById(int transId);
	public void insert(CheckingTransaction trans);
	public void delete(int transId);
	public void updatePayeeMap(String inDesc, String stdDesc);
}
