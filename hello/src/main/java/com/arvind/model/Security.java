package com.arvind.model;

import com.arvind.util.SecurityType;

public class Security extends BaseObject {

	private String ticker;

	private String description;

	private SecurityType securityType;
	
	@Override
	public String toString() {
		return "ticker: " + this.ticker + Utility.NEWLINE
			+ "description: " + this.description + Utility.NEWLINE
			+ "securityType: " + this.securityType;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SecurityType getSecurityType() {
		return securityType;
	}

	public void setSecurityType(SecurityType securityType) {
		this.securityType = securityType;
	}
	
}
