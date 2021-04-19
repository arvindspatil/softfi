package com.arvind.util;

public enum AccountType {
	CHECKING(1, "Checking"),
    SAVINGS(2, "Savings"),
    CREDIT(3, "Credit Card"),
    INVESTMENT(4, "Investment"),
    OTHER(5, "Other"),
    AUTOLOAN(6, "Auto Loan"),
    MORTGAGE(7, "Mortgage"),
	NET(8, "Net");
    private final int code;
    
    private final String desc;

    private AccountType(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	public static AccountType fromCode(int code) {
		for (AccountType acctType : values()) {
			if (acctType.code == code) {
				return acctType;
			}
		}
		return null;
	}
}
