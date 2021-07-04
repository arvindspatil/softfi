package com.arvind.service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.arvind.model.CheckingTransaction;
import com.arvind.model.CreditTransaction;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.LoanTransaction;
import com.arvind.model.SavingTransaction;
import com.arvind.util.AccountType;

public interface UploadService {
	public Map<String, Object> uploadTransactions(MultipartFile file, String acctName);
	public Map<String, Object> uploadTransactions(MultipartFile file, int acctId);
	public Map<String, Object> uploadTransactionsQuicken(MultipartFile file, String acctName);
	public Map<String, Object> fetchUploadedTransactions(String acctName);
	public Map<String, Object> displayUploadedTransactions(Integer acctId);
	public Map<String, Object> deleteTransaction(Integer transactionId, Integer acctId);
	public Map<String, Object> editTransaction(Integer transactionId, Integer acctId);
	public Map<String, Object> acceptTransaction(Integer transactionId, Integer acctId);
	public void cleanInvTransaction(InvestmentTransaction trans);
	public void updateCheckingTransferTransaction(CheckingTransaction trans);
	public void updateSavingTransferTransaction(SavingTransaction trans);
	public void updateCreditTransferTransaction(CreditTransaction trans);
	public void updateInvestmentTransferTransaction(InvestmentTransaction trans);
	public void updateLoanTransferTransaction(LoanTransaction trans);
	public Map<String, Object> downloadAccount(String acctName);
	public Map<String, Object> addTransaction(String acctName);
	public Map<String, Object> displayAccountBalance(Integer acctId);
	public Map<String, Object> updateAccountBalance(String acctName) ;
//	public Map<String, Object> getAccountBalance(String acctName) ;
	public Map<String, Object> addQuote();
	public Map<String, Object> getNetBalance();
	public Map<AccountType, Object> getHistoricalBalance();
	public void doNetBalance(HttpServletResponse resonse) throws IOException;
}
