package com.arvind.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.arvind.model.Account;

public interface AccountDao {
	public List<Account> findAccounts();
	public void insertAccount(Account acct);
	public void insertAccountRecon(int acctId, LocalDate reconDate);
	public List<Account> findAccountsByType(List<Integer> acctTypes);
	public List<Account> findAccountsByName(String acctName);
	public List<Account> findAccountsById(int acctId);
	public List<Pair<Account, LocalDate>> findAllReconDates();
	public List<Pair<Account, LocalDate>> findReconDateByAcctId(int acctId);
	public void updateAccountRecon(int acctId, LocalDate reconDate);
}
