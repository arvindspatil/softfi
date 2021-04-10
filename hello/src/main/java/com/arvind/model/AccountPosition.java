package com.arvind.model;

import java.math.BigDecimal;

public class AccountPosition extends BaseObject {

	private int acctPositionId;
	
	private int acctId;
	
	private String ticker;
	
	private String description;
	
	private BigDecimal quantity;
	
	private BigDecimal currentQuote;
	
	private BigDecimal currentValue;

	private BigDecimal changeInPrice;

	private BigDecimal changeInValue;
	
	private BigDecimal costBasis;

	@Override
	public String toString() {
		return "acctPositionId: " + this.acctPositionId + Utility.NEWLINE
				+ "acctId: " + this.acctId + Utility.NEWLINE
				+ "ticker: " + this.ticker + Utility.NEWLINE
				+ "quantity: " + this.quantity + Utility.NEWLINE
				+ "currentQuote: " + this.currentQuote + Utility.NEWLINE
				+ "currentValue: " + this.currentValue + Utility.NEWLINE
				+ "changeInPrice: " + this.changeInPrice + Utility.NEWLINE
				+ "changeInValue: " + this.changeInValue + Utility.NEWLINE
				+ "costBasis: " + this.costBasis;
	}

	public int getAcctPositionId() {
		return acctPositionId;
	}

	public void setAcctPositionId(int acctPositionId) {
		this.acctPositionId = acctPositionId;
	}

	public int getAcctId() {
		return acctId;
	}

	public void setAcctId(int acctId) {
		this.acctId = acctId;
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

	public BigDecimal getCurrentQuote() {
		return currentQuote;
	}

	public void setCurrentQuote(BigDecimal currentQuote) {
		this.currentQuote = currentQuote;
	}

	public BigDecimal getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(BigDecimal currentValue) {
		this.currentValue = currentValue;
	}

	public BigDecimal getCostBasis() {
		return costBasis;
	}

	public void setCostBasis(BigDecimal costBasis) {
		this.costBasis = costBasis;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getChangeInValue() {
		return changeInValue;
	}

	public void setChangeInValue(BigDecimal changeInValue) {
		this.changeInValue = changeInValue;
	}

	public BigDecimal getChangeInPrice() {
		return changeInPrice;
	}

	public void setChangeInPrice(BigDecimal changeInPrice) {
		this.changeInPrice = changeInPrice;
	}

}
