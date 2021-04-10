package com.arvind.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {

	private String ticker;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate quoteDate;
	
	private BigDecimal pricePs;

	public Quote() {
	}

	@Override
	public String toString() {
		return "Quote{" + "ticker='" + ticker + '\'' + ", date=" + quoteDate + ", pricePs=" + pricePs + '}';
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public LocalDate getQuoteDate() {
		return quoteDate;
	}

	public void setQuoteDate(LocalDate quoteDate) {
		this.quoteDate = quoteDate;
	}

	public BigDecimal getPricePs() {
		return pricePs;
	}

	public void setPricePs(BigDecimal pricePs) {
		this.pricePs = pricePs;
	}
}