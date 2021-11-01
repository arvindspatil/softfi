package com.arvind.repository;

import java.util.List;

import com.arvind.model.Bills;

public interface BillsDao {
	public List<Bills> getPendingBills();
	public List<Bills> getAllBills();
	public void insertBill(Bills bill);
	public void delete(int id);
}
