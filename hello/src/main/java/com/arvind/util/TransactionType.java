package com.arvind.util;

import org.apache.commons.lang3.StringUtils;

public enum TransactionType {
	OTHER(1, "Other"),
    ADD(2, "Add"),
    BUY(3, "Buy"),
	DEPOSIT(4, "Deposit"),
    DIVIDEND(5, "Dividend"),
    ENDING_BALANCE(6, "Ending Balance"),
    INTEREST(7, "Interest"),
    OPENING_BALANCE(8, "Opening Balance"),
    REINVEST(9, "Reinvest"),
	REMOVE(10, "Remove"),
    SELL(11, "Sell"),
    XIN(12, "Transfer In"),
    XOUT(13, "Transfer Out"),
	WITHDRAW(14, "Withdraw");

    private final int code;
    
    private final String desc;

    private TransactionType(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	public static TransactionType fromCode(int code) {
		for (TransactionType acctType : values()) {
			if (acctType.code == code) {
				return acctType;
			}
		}
		return null;
	}

	public static TransactionType fromDesc(String desc) {
		for (TransactionType acctType : values()) {
			if (StringUtils.equals(desc, acctType.desc)) {
				return acctType;
			}
		}
		return null;
	}
}
