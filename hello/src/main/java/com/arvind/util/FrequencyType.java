package com.arvind.util;

import org.apache.commons.lang3.StringUtils;

public enum FrequencyType {
	MONTHLY(1, "Monthly"),
    QUARTERLY(2, "Quarterly"),
    BIMONTHLY(3, "Bi-Monthly"),
	SIXMONTHLY(4, "Six-Monthly"),
    YEARLY(5, "Yearly");

    private final int code;
    
    private final String desc;

    private FrequencyType(int code, String desc) {
    	this.code = code;
    	this.desc = desc;
    }

	public int getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
	
	public static FrequencyType fromCode(int code) {
		for (FrequencyType acctType : values()) {
			if (acctType.code == code) {
				return acctType;
			}
		}
		return null;
	}

	public static FrequencyType fromDesc(String desc) {
		for (FrequencyType acctType : values()) {
			if (StringUtils.equals(desc, acctType.desc)) {
				return acctType;
			}
		}
		return null;
	}
}
