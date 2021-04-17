package com.arvind.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

public class AccountBal extends Account {

	private BigDecimal balanceAmt;
	
	private List<AccountPosition> positions;

	private BigDecimal accountValue;

	private BigDecimal changeInPositionsValue;
	
	@Override
	public String toString() {
		return super.toString() + Utility.NEWLINE
				+ "balanceAmt: " + this.balanceAmt + Utility.NEWLINE;
	}

	public BigDecimal getBalanceAmt() {
		return balanceAmt;
	}

	public void setBalanceAmt(BigDecimal balanceAmt) {
		this.balanceAmt = balanceAmt;
	}

	public List<AccountPosition> getPositions() {
		return positions;
	}

	public void setPositions(List<AccountPosition> positions) {
		this.positions = positions;
	}

	public BigDecimal getAccountValue() {
		return accountValue;
	}

	public void setAccountValue(BigDecimal accountValue) {
		this.accountValue = accountValue;
	}

	public BigDecimal getChangeInPositionsValue() {
		return changeInPositionsValue;
	}

	public void setChangeInPositionsValue(BigDecimal changeInPositionsValue) {
		this.changeInPositionsValue = changeInPositionsValue;
	}
	
}
