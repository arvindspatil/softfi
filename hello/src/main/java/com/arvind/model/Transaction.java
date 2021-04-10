package com.arvind.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class Transaction extends BaseObject {
	
	private int transactionId;

	private int acctId;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate transDate;

	private String inDescription;
	
	private String description;
	
	private BigDecimal transAmt;

	private BigDecimal balanceAmt;

	private int transferAcctId;
	
	private String transferAcct;
	
	@Override
	public String toString() {
		return "transactionId: " + this.transactionId + Utility.NEWLINE
			+ "transDate: " + this.transDate + Utility.NEWLINE
			+ "inDescription: " + this.inDescription + Utility.NEWLINE
			+ "description: " + this.description + Utility.NEWLINE
			+ "balanceAmt: " + this.balanceAmt + Utility.NEWLINE
			+ "transAmt: " + this.transAmt + Utility.NEWLINE
			+ "transferAcctId: " + this.transferAcctId + Utility.NEWLINE
			+ "transferAcct: " + this.transferAcct;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getAcctId() {
		return acctId;
	}

	public void setAcctId(int acctId) {
		this.acctId = acctId;
	}

	public LocalDate getTransDate() {
		return transDate;
	}

	public void setTransDate(LocalDate transDate) {
		this.transDate = transDate;
	}

	public String getInDescription() {
		return inDescription;
	}

	public void setInDescription(String inDescription) {
		this.inDescription = inDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getTransAmt() {
		return transAmt;
	}

	public void setTransAmt(BigDecimal transAmt) {
		this.transAmt = transAmt;
	}

	public BigDecimal getBalanceAmt() {
		return balanceAmt;
	}

	public void setBalanceAmt(BigDecimal balanceAmt) {
		this.balanceAmt = balanceAmt;
	}

	public int getTransferAcctId() {
		return transferAcctId;
	}

	public void setTransferAcctId(int transferAcctId) {
		this.transferAcctId = transferAcctId;
	}

	public String getTransferAcct() {
		return transferAcct;
	}

	public void setTransferAcct(String transferAcct) {
		this.transferAcct = transferAcct;
	}

}
