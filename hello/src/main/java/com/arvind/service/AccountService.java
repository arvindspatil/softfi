package com.arvind.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import com.arvind.model.CheckingTransaction;
import com.arvind.model.CreditTransaction;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.LoanTransaction;
import com.arvind.model.SavingTransaction;
import com.arvind.model.UploadCheckingTransaction;
import com.arvind.model.UploadCreditTransaction;
import com.arvind.model.UploadSavingTransaction;
import com.arvind.util.AccountType;

public interface AccountService {
	public Map<String, Object> fetchTransactions(Integer acctId);
	public CreditTransaction addCreditTransaction(CreditTransaction trans);
	public CheckingTransaction addCheckingTransaction(CheckingTransaction trans);
	public SavingTransaction addSavingsTransaction(SavingTransaction trans);
	public InvestmentTransaction addInvestmentTransaction(InvestmentTransaction trans);
	public LoanTransaction addLoanTransaction(LoanTransaction trans);
	public Map<String, Object> fetchUploadedTransactions(Integer acctId);
	public void updateAcceptUploadedCheckingTransaction(UploadCheckingTransaction trans);
	public void updateAcceptUploadedSavingsTransaction(UploadSavingTransaction trans);
	public void updateAcceptUploadedCreditTransaction(UploadCreditTransaction trans);
	public void deleteUploadedCheckingTransaction(Integer transactionId);
	public void deleteUploadedSavingsTransaction(Integer transactionId);
	public void deleteUploadedCreditTransaction(Integer transactionId);
	public Map<String, BigDecimal> getAccountBalance(AccountType acctType);
	public void updateAccountRecon(Integer acctId, LocalDate reconDate);
}
