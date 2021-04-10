package com.arvind.util;

public enum Status {
	ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive");

    private final int code;
    
    private final String desc;

    private Status(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	public static Status fromCode(int code) {
		for (Status status : values()) {
			if (status.code == code) {
				return status;
			}
		}
		return null;
	}

}
