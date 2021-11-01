package com.arvind.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.arvind.util.BillStatus;

public class Bills extends BaseObject {

	private int billId;
	
	private int scheduleId;
	
	private String payee;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate stmtDate;
 
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dueDate;
 
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate payDate;
 
	private BigDecimal amount;
	
	private BillStatus status;
	
	@Override
	public String toString() {
		return "billId: " + this.billId + Utility.NEWLINE
			+ "scheduleId: " + this.scheduleId + Utility.NEWLINE
			+ "payee: " + this.payee + Utility.NEWLINE
			+ "stmtDate: " + this.stmtDate + Utility.NEWLINE
			+ "dueDate: " + this.dueDate + Utility.NEWLINE
			+ "payDate: " + this.payDate + Utility.NEWLINE
			+ "amount: " + this.amount + Utility.NEWLINE
			+ "status: " + this.status + Utility.NEWLINE;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public int getBillId() {
		return billId;
	}

	public void setBillId(int billId) {
		this.billId = billId;
	}

	public LocalDate getStmtDate() {
		return stmtDate;
	}

	public void setStmtDate(LocalDate stmtDate) {
		this.stmtDate = stmtDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public LocalDate getPayDate() {
		return payDate;
	}

	public void setPayDate(LocalDate payDate) {
		this.payDate = payDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BillStatus getStatus() {
		return status;
	}

	public void setStatus(BillStatus status) {
		this.status = status;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

}
