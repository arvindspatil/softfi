package com.arvind.util;

public enum SecurityType {
	STOCK(1, "Stock"),
    MUTUAL_FUND(2, "Mutual Fund"),
    OTHER(3, "Other");

    private final int code;
    
    private final String desc;

    private SecurityType(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	public static SecurityType fromCode(int code) {
		for (SecurityType status : values()) {
			if (status.code == code) {
				return status;
			}
		}
		return null;
	}

}
