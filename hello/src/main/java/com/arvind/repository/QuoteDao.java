package com.arvind.repository;

import java.time.LocalDate;
import java.util.List;

import com.arvind.model.Quote;

public interface QuoteDao {
	public void insert(Quote quote);
	public void delete(int quoteId);
	public List<Quote> findQuotes();
	public List<Quote> findRecentQuotes();
	public List<Quote> findQuotesByTicker(String ticker);
	public void update(Quote quote);
	public List<Quote> findLatestQuotes();
	public List<Quote> findQuoteByTickerDate(String ticker, LocalDate quoteDate);
	public List<Quote> findNearestQuoteByTickerDate(String ticker, LocalDate quoteDate);
}
