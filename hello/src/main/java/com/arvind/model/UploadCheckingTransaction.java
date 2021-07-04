package com.arvind.model;

public class UploadCheckingTransaction extends CheckingTransaction {

	private boolean decision;
	
	@Override
	public String toString() {
		return super.toString() + Utility.NEWLINE
				+ "decision" + this.decision;
	}

	public boolean isDecision() {
		return decision;
	}

	public void setDecision(boolean decision) {
		this.decision = decision;
	}
	
}
