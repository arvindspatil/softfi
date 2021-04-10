package com.arvind.repository;

import java.util.List;

import com.arvind.model.Account;

public interface AccountDao {
	public List<Account> findAccounts();
	public void insertAccount(Account acct);
	public List<Account> findAccountsByType(List<Integer> acctTypes);
	public List<Account> findAccountsByName(String acctName);
	public List<Account> findAccountsById(int acctId);
}
