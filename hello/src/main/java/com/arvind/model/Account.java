package com.arvind.model;

import com.arvind.util.AccountType;
import com.arvind.util.Status;

public class Account extends BaseObject {

	private int acctId;

	private String acctName;

	private AccountType acctType;
	
	private Status status;
	
	public int parentAcctId;
	
	public String parentAcctName;

	public int getAcctId() {
		return acctId;
	}

	public void setAcctId(int acctId) {
		this.acctId = acctId;
	}

	public String getAcctName() {
		return acctName;
	}

	public void setAcctName(String acctName) {
		this.acctName = acctName;
	}

	public AccountType getAcctType() {
		return acctType;
	}

	public void setAcctType(AccountType acctType) {
		this.acctType = acctType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getParentAcctId() {
		return parentAcctId;
	}

	public void setParentAcctId(int parentAcctId) {
		this.parentAcctId = parentAcctId;
	}

	@Override
	public String toString() {
		return "acctId: " + this.acctId + Utility.NEWLINE
			+ "acctName: " + this.acctName + Utility.NEWLINE
			+ "acctType: " + this.acctType + Utility.NEWLINE
			+ "status: " + this.status + Utility.NEWLINE
			+ "parentAcctId: " + this.parentAcctId;
	}

	public String getParentAcctName() {
		return parentAcctName;
	}

	public void setParentAcctName(String parentAcctName) {
		this.parentAcctName = parentAcctName;
	}
	
}
