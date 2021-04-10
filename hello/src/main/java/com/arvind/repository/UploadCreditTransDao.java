package com.arvind.repository;

import java.util.List;

import com.arvind.model.CreditTransaction;

public interface UploadCreditTransDao {
	public List<CreditTransaction> findTransactionsByAcctId(int acctId);
	public List<CreditTransaction> findTransactionsById(int transId);
	public void insert(CreditTransaction trans);
	public void delete(int transId);
	public void updatePayeeMap(String inDesc, String stdDesc);
}
