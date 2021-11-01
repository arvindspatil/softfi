package com.arvind.repository;

import java.util.List;

import com.arvind.model.BillSchedule;

public interface BillScheduleDao {
	public List<BillSchedule> findBillSchedules();
	public void insertBillSchedule(BillSchedule sched);
	public void delete(int id);
}
