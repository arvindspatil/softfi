package com.arvind.model;

import java.time.LocalDate;

public class AccountRecon extends Account {

	private LocalDate reconDate;

	@Override
	public String toString() {
		return super.toString() + Utility.NEWLINE 
			+ "reconDate: " + this.reconDate;
	}

	public LocalDate getReconDate() {
		return reconDate;
	}

	public void setReconDate(LocalDate reconDate) {
		this.reconDate = reconDate;
	}
	
}
