package com.arvind.model;

public class CheckingTransaction extends Transaction {

	private int checkNumber;
	
	@Override
	public String toString() {
		return super.toString() + Utility.NEWLINE
				+ "checkNumber: " + this.checkNumber;
	}

	public int getCheckNumber() {
		return checkNumber;
	}

	public void setCheckNumber(int checkNumber) {
		this.checkNumber = checkNumber;
	}
	
}
