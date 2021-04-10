package com.arvind.model;

import java.util.List;

public class TransactionBook extends BaseObject {

	private List<Transaction> transactions;

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		for (Transaction t : transactions) {
			sbuf.append(t.toString());
		}
		return sbuf.toString();
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}
	
}
