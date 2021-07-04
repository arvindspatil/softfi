package com.arvind.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.arvind.model.CheckingTransaction;
import com.arvind.repository.CheckingTransactionDao;
import com.arvind.util.Util;


@Service
public class CheckingServiceImpl implements CheckingService {

	@Autowired
	CheckingTransactionDao checkingTransactionDao;

	private static final Logger log = LoggerFactory.getLogger(CheckingServiceImpl.class);
	
	@Override
	public void getAccountBalance(int acctId, TreeMap<LocalDate, HashMap<Integer, BigDecimal>> acctBalMap) {
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		List<CheckingTransaction> transactions = checkingTransactionDao.findTransactionsByAcctId(acctId);
		if (CollectionUtils.isEmpty(transactions)) {
			return;
		}
		balanceMap = Util.updateCheckingBalanceByMonth(transactions);
		Util.updateMissingBalanceHistory(balanceMap);
		for (Map.Entry<LocalDate, BigDecimal> entry : balanceMap.entrySet()) {
			LocalDate keyDt = entry.getKey();
			BigDecimal value = entry.getValue();
			if (!acctBalMap.containsKey(keyDt)) {
				acctBalMap.put(keyDt, new HashMap<Integer, BigDecimal>());
			}
			HashMap<Integer, BigDecimal> valueMap = acctBalMap.get(keyDt);
			valueMap.put(acctId, value);
		}
	}

}
