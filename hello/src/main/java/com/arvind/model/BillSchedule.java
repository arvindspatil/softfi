package com.arvind.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.arvind.util.FrequencyType;
import com.arvind.util.Status;

public class BillSchedule extends BaseObject {

	private int scheduleId;
	
	private String payee;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
 
	private int gracePeriod;
	
	private FrequencyType frequencyType;
	
	private Status status;
	
	@Override
	public String toString() {
		return "scheduleId: " + this.scheduleId + Utility.NEWLINE
			+ "payee: " + this.payee + Utility.NEWLINE
			+ "startDate: " + this.startDate + Utility.NEWLINE
			+ "gracePeriod: " + this.gracePeriod + Utility.NEWLINE
			+ "frequencyType: " + this.frequencyType + Utility.NEWLINE;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public FrequencyType getFrequencyType() {
		return frequencyType;
	}

	public void setFrequencyType(FrequencyType frequencyType) {
		this.frequencyType = frequencyType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
