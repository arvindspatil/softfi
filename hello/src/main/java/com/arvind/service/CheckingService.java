package com.arvind.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.TreeMap;

public interface CheckingService {
	public void getAccountBalance(int acctId, TreeMap<LocalDate, HashMap<Integer, BigDecimal>> acctBalMap);
}
