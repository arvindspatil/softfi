package com.arvind.service;

import java.util.Map;

import com.arvind.model.AccountBal;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.Quote;

public interface InvestmentService {
	public Map<String, Object> updatePositions();
	public Map<String, Object> updateQuotes();
	public AccountBal getPositions(String acctName);
	public void updatePrice(InvestmentTransaction trans);
	public void updatePrice(Quote quote);
	public Map<String, Object> updateQuoteList();
}
