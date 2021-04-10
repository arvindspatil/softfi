package com.arvind.model;

import java.math.BigDecimal;

public class ReportAccountBal extends BaseObject {
	private int depth;
	private String acctName;
	private BigDecimal accountValue;
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getAcctName() {
		return acctName;
	}
	public void setAcctName(String acctName) {
		this.acctName = acctName;
	}
	public BigDecimal getAccountValue() {
		return accountValue;
	}
	public void setAccountValue(BigDecimal accountValue) {
		this.accountValue = accountValue;
	}
	
	@Override
	public String toString() {
		return null;
	}
}
