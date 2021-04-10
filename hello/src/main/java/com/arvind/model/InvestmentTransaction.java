package com.arvind.model;

import java.math.BigDecimal;

import com.arvind.util.TransactionType;

public class InvestmentTransaction extends Transaction {

	private TransactionType transactionType;
	
	private String ticker;
	
	private BigDecimal quantity;
	
	private BigDecimal fees;
	
	private BigDecimal quote;
	
	private BigDecimal balanceQty;

	@Override
	public String toString() {
		return super.toString() + Utility.NEWLINE
				+ "transactionType: " + this.transactionType + Utility.NEWLINE
				+ "ticker: " + this.ticker + Utility.NEWLINE
				+ "quantity: " + this.quantity + Utility.NEWLINE
				+ "fees: " + this.fees + Utility.NEWLINE
				+ "quote: " + this.quote;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public void setFees(BigDecimal fees) {
		this.fees = fees;
	}

	public BigDecimal getQuote() {
		return quote;
	}

	public void setQuote(BigDecimal quote) {
		this.quote = quote;
	}

	public BigDecimal getBalanceQty() {
		return balanceQty;
	}

	public void setBalanceQty(BigDecimal balanceQty) {
		this.balanceQty = balanceQty;
	}

}
