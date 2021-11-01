package com.arvind.util;

public enum BillStatus {
	UNPAID(1, "Unpaid"),
    PAID(2, "Paid");

    private final int code;
    
    private final String desc;

    private BillStatus(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	public static BillStatus fromCode(int code) {
		for (BillStatus status : values()) {
			if (status.code == code) {
				return status;
			}
		}
		return null;
	}

}
