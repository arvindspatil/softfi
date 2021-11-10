package com.arvind.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arvind.model.Bills;
import com.arvind.repository.BillsDao;


@Service
public class BillServiceImpl implements BillService {

	@Autowired
	BillsDao billsDao;

	private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

	@Override
	public void updateBill(Bills bill) {
		billsDao.updateBill(bill);
	}
	
}
