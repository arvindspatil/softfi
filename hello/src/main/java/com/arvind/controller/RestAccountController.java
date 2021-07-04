package com.arvind.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arvind.model.Account;
import com.arvind.model.AccountRecon;
import com.arvind.model.CheckingTransaction;
import com.arvind.model.CreditTransaction;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.LoanTransaction;
import com.arvind.model.SavingTransaction;
import com.arvind.model.Security;
import com.arvind.model.UploadCheckingTransaction;
import com.arvind.model.UploadCreditTransaction;
import com.arvind.model.UploadSavingTransaction;
import com.arvind.model.json.AllData;
import com.arvind.model.json.ChartData;
import com.arvind.model.json.ChartDataset;
import com.arvind.repository.AccountDao;
import com.arvind.repository.CheckingTransactionDao;
import com.arvind.repository.CreditTransactionDao;
import com.arvind.repository.InvestmentTransactionDao;
import com.arvind.repository.LoanTransactionDao;
import com.arvind.repository.SavingTransactionDao;
import com.arvind.repository.SecurityDao;
import com.arvind.service.AccountService;
import com.arvind.service.UploadService;
import com.arvind.util.AccountType;
import com.arvind.util.Util;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/")
public class RestAccountController {

	@Autowired
	AccountDao accountDao;

	@Autowired
	SecurityDao securityDao;

	@Autowired
	UploadService uploadService;

	@Autowired
	CreditTransactionDao creditTransactionDao;

	@Autowired
	CheckingTransactionDao checkingTransactionDao;

	@Autowired
	SavingTransactionDao savingTransactionDao;

	@Autowired
	InvestmentTransactionDao investmentTransactionDao;

	@Autowired
	LoanTransactionDao loanTransactionDao;

	@Autowired
	AccountService accountService;

	@GetMapping("/allaccounts")
	public List<Account> handleAllAccounts(Model model) {
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
		Predicate<Account> isValidAccount = acct -> (acct.getAcctType() == AccountType.AUTOLOAN
				|| acct.getAcctType() == AccountType.CHECKING || acct.getAcctType() == AccountType.CREDIT
				|| acct.getAcctType() == AccountType.MORTGAGE || acct.getAcctType() == AccountType.SAVINGS
				|| acct.getAcctType() == AccountType.INVESTMENT);
		return allAccounts.stream().filter(isValidAccount).collect(Collectors.toList());
	}

	@GetMapping("/account-recon")
	public List<AccountRecon> fetchAccountRecons() {
		List<Pair<Account, LocalDate>> lst = accountDao.findAllReconDates();
		List<AccountRecon> response = new ArrayList<>();
		for (Pair<Account, LocalDate> iter : lst) {
			AccountRecon acct = new AccountRecon();
			acct.setAcctId(iter.getKey().getAcctId());
			acct.setAcctName(iter.getKey().getAcctName());
			acct.setAcctType(iter.getKey().getAcctType());
			acct.setStatus(iter.getKey().getStatus());
			LocalDate reconDate = (iter.getValue() == null ? LocalDate.of(2000, 1, 1) : iter.getValue());
			acct.setReconDate(reconDate);
			response.add(acct);
		}
		return response;
	}

	@GetMapping("/parent-accounts")
	public List<Account> fetchParentAccounts() {
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
		Predicate<Account> isValidAccount = acct -> (acct.getAcctType() == AccountType.OTHER);
		return allAccounts.stream().filter(isValidAccount).collect(Collectors.toList());
	}

	@RequestMapping(value = "/transactions/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getTransactionsByAcctId(@PathVariable("id") int id) {
		Map<String, Object> transactions = accountService.fetchTransactions(id);
		return transactions;
	}

	@RequestMapping(value = "/uploaded-transactions/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getUploadedTransactionsByAcctId(@PathVariable("id") int id) {
		Map<String, Object> transactions = accountService.fetchUploadedTransactions(id);
		return transactions;
	}
	
	@RequestMapping(value = "/credit-upload-transactions", method = RequestMethod.POST)
	public Map<String, Object> handleUploadAccount(@RequestParam("file") MultipartFile file,
			@RequestParam(name="acctId") int acctId) {
		uploadService.uploadTransactions(file, acctId);
		Map<String, Object> resp = getUploadedTransactionsByAcctId(acctId);
		return resp;
	}

	@RequestMapping(value = "/savings-upload-transactions", method = RequestMethod.POST)
	public Map<String, Object> handleSavingsUploadAccount(@RequestParam("file") MultipartFile file,
			@RequestParam(name="acctId") int acctId) {
		uploadService.uploadTransactions(file, acctId);
		Map<String, Object> resp = getUploadedTransactionsByAcctId(acctId);
		return resp;
	}

	@RequestMapping(value = "/checking-upload-transactions", method = RequestMethod.POST)
	public Map<String, Object> handleCheckingUploadAccount(@RequestParam("file") MultipartFile file,
			@RequestParam(name="acctId") int acctId) {
		uploadService.uploadTransactions(file, acctId);
		Map<String, Object> resp = getUploadedTransactionsByAcctId(acctId);
		return resp;
	}

	@DeleteMapping("/delete-credit-trans/{acctid}/{id}")
	public void deleteCreditTrans(@PathVariable int id, @PathVariable int acctid) {
		creditTransactionDao.delete(id);
	}

	@DeleteMapping("/delete-checking-trans/{acctid}/{id}")
	public void deleteCheckingTrans(@PathVariable int id, @PathVariable int acctid) {
		checkingTransactionDao.delete(id);
	}

	@DeleteMapping("/delete-savings-trans/{acctid}/{id}")
	public void deleteSavingsTrans(@PathVariable int id, @PathVariable int acctid) {
		savingTransactionDao.delete(id);
	}

	@DeleteMapping("/delete-investment-trans/{acctid}/{id}")
	public void deleteInvestmentTrans(@PathVariable int id, @PathVariable int acctid) {
		investmentTransactionDao.delete(id);
	}

	@DeleteMapping("/delete-loan-trans/{acctid}/{id}")
	public void deleteLoanTrans(@PathVariable int id, @PathVariable int acctid) {
		loanTransactionDao.delete(id);
	}

	@DeleteMapping("/delete-uploaded-checking-trans/{acctid}/{id}")
	public void deleteUploadedCheckingTrans(@PathVariable int id, @PathVariable int acctid) {
		accountService.deleteUploadedCheckingTransaction(id);
	}

	@DeleteMapping("/delete-uploaded-savings-trans/{acctid}/{id}")
	public void deleteUploadedSavingsTrans(@PathVariable int id, @PathVariable int acctid) {
		accountService.deleteUploadedSavingsTransaction(id);
	}

	@DeleteMapping("/delete-uploaded-credit-trans/{acctid}/{id}")
	public void deleteUploadedCreditTrans(@PathVariable int id, @PathVariable int acctid) {
		accountService.deleteUploadedCreditTransaction(id);
	}

	@PostMapping("/add-credit-trans")
	public void addCreditTrans(@RequestBody CreditTransaction trans) {
		accountService.addCreditTransaction(trans);
	}

	@PostMapping("/add-checking-trans")
	public void addCheckingTrans(@RequestBody CheckingTransaction trans) {
		accountService.addCheckingTransaction(trans);
	}

	@PostMapping("/add-savings-trans")
	public void addSavingsTrans(@RequestBody SavingTransaction trans) {
		accountService.addSavingsTransaction(trans);
	}

	@PostMapping("/add-investment-trans")
	public void addInvestmentTrans(@RequestBody InvestmentTransaction trans) {
		System.out.println("Uncomment this line");
		accountService.addInvestmentTransaction(trans);
	}

	@PostMapping("/add-loan-trans")
	public void addLoanTrans(@RequestBody LoanTransaction trans) {
		accountService.addLoanTransaction(trans);
	}

	@PostMapping("/add-account")
	public void addAccount(@RequestBody Account acct) {
		System.out.println("Here");
		accountDao.insertAccount(acct);
	}

	@PostMapping("/update-checking-trans")
	public void updateCheckingTransaction(@RequestBody CheckingTransaction trans) {
		System.out.println("Here");
		deleteCheckingTrans(trans.getTransactionId(), trans.getAcctId());
		addCheckingTrans(trans);
//		accountService.addCheckingTransaction(trans);
	}

	@PostMapping("/update-savings-trans")
	public void updateSavingsTransaction(@RequestBody SavingTransaction trans) {
		deleteSavingsTrans(trans.getTransactionId(), trans.getAcctId());
		addSavingsTrans(trans);
	}

	@PostMapping("/update-credit-trans")
	public void updateCreditTransaction(@RequestBody CreditTransaction trans) {
		deleteCreditTrans(trans.getTransactionId(), trans.getAcctId());
		addCreditTrans(trans);
	}

	@PostMapping("/update-investment-trans")
	public void updateInvestmentTransaction(@RequestBody InvestmentTransaction trans) {
		deleteInvestmentTrans(trans.getTransactionId(), trans.getAcctId());
		addInvestmentTrans(trans);
	}

	@PostMapping("/update-loan-trans")
	public void updateLoanTransaction(@RequestBody LoanTransaction trans) {
		deleteLoanTrans(trans.getTransactionId(), trans.getAcctId());
		addLoanTrans(trans);
	}

	@PostMapping("/upd-accept-checking-trans")
	public void updateAcceptUploadedCheckingTransaction(@RequestBody UploadCheckingTransaction transaction) {
		accountService.updateAcceptUploadedCheckingTransaction(transaction);
	}

	@PostMapping("/upd-accept-savings-trans")
	public void updateAcceptUploadedSavingsTransaction(@RequestBody UploadSavingTransaction transaction) {
		accountService.updateAcceptUploadedSavingsTransaction(transaction);
	}

	@PostMapping("/upd-accept-credit-trans")
	public void updateAcceptUploadedCreditTransaction(@RequestBody UploadCreditTransaction transaction) {
		accountService.updateAcceptUploadedCreditTransaction(transaction);
	}

	@PostMapping("/update-acct-recon")
	public void updateAccountRecon(@RequestBody AccountRecon account) {
		System.out.println("AccountRecon print");
		accountService.updateAccountRecon(account.getAcctId(), account.getReconDate());
//		deleteCreditTrans(trans.getTransactionId(), trans.getAcctId());
//		addCreditTrans(trans);
	}
	
	@GetMapping("/list-transactions")
	public List<CreditTransaction> listTransactions() {
		List<CreditTransaction> transactions = new ArrayList<>();
		return transactions;
	}

	private ChartData getChartDataByType(Map<AccountType, Object> histMap, List<String> labelList, AccountType acctType,
			String color) {
		ChartData chartData = new ChartData();

		chartData.setLabels(labelList);
		List<ChartDataset> datasets = new ArrayList<>();
		List<BigDecimal> dataValues = Util.filterByType(acctType, histMap);

		ChartDataset acctDataset = new ChartDataset();
		acctDataset.setData(dataValues);
		acctDataset.setLabel(acctType.name());
		acctDataset.setLineTension(0);
//		acctDataset.setBackgroundColor("transparent");
		acctDataset.setBackgroundColor(color);
		acctDataset.setBorderColor(color);
		acctDataset.setPointBackgroundColor(color);
		acctDataset.setBorderWidth(1);
		datasets.add(acctDataset);
		chartData.setDatasets(datasets);

		return chartData;
	}

	@GetMapping("/chart-data")
	public AllData getChartData(Model model) {

		AllData allData = new AllData();
		Map<AccountType, ChartData> chartData = new HashMap<>();

		List<String> labelList = Util.getChartKeys();
		Map<AccountType, Object> histMap = uploadService.getHistoricalBalance();

		ChartData checkingChartData = getChartDataByType(histMap, labelList, AccountType.CHECKING, "blue");
		ChartData creditChartData = getChartDataByType(histMap, labelList, AccountType.CREDIT, "red");
		ChartData loanChartData = getChartDataByType(histMap, labelList, AccountType.MORTGAGE, "green");
		ChartData savingChartData = getChartDataByType(histMap, labelList, AccountType.SAVINGS, "#20f08b");
		ChartData invChartData = getChartDataByType(histMap, labelList, AccountType.INVESTMENT, "cyan");
		ChartData netChartData = getChartDataByType(histMap, labelList, AccountType.NET, "indigo");

		allData.setCheckingData(checkingChartData);
		allData.setCreditData(creditChartData);
		allData.setSavingsData(savingChartData);
		allData.setInvData(invChartData);
		allData.setLoanData(loanChartData);
		allData.setNetData(netChartData);

		return allData;
	}

	@GetMapping("/chart-checking-data")
	public ChartData getChartCheckingData() {

		List<String> labelList = new ArrayList<>();
		List<BigDecimal> dataValues = new ArrayList<>(); // Util.filterByType(acctType, histMap);
		Map<String, BigDecimal> balanceMap = accountService.getAccountBalance(AccountType.CHECKING);
		for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
			labelList.add(entry.getKey());
			dataValues.add(entry.getValue());
		}

		ChartData checkingChartData = new ChartData();

		checkingChartData.setLabels(labelList);
		List<ChartDataset> datasets = new ArrayList<>();

		ChartDataset acctDataset = new ChartDataset();
		acctDataset.setData(dataValues);
		acctDataset.setLabel(AccountType.CHECKING.getDesc());
		acctDataset.setLineTension(0);
		acctDataset.setBackgroundColor("blue");
		acctDataset.setBorderColor("blue");
		acctDataset.setPointBackgroundColor("blue");
		acctDataset.setBorderWidth(1);
		datasets.add(acctDataset);
		checkingChartData.setDatasets(datasets);

		return checkingChartData;
	}

	@GetMapping("/checking-bal")
	public Map<String, BigDecimal> getCheckingBalances() {
		return accountService.getAccountBalance(AccountType.CHECKING);
	}

	@GetMapping("/saving-bal")
	public Map<String, BigDecimal> getSavingBalances() {
		return accountService.getAccountBalance(AccountType.SAVINGS);
	}

	@GetMapping("/credit-bal")
	public Map<String, BigDecimal> getCreditBalances() {
		return accountService.getAccountBalance(AccountType.CREDIT);
	}

	@GetMapping("/loan-bal")
	public Map<String, BigDecimal> getLoanBalances() {
		return accountService.getAccountBalance(AccountType.AUTOLOAN);
	}

	@GetMapping("/investment-bal")
	public Map<String, BigDecimal> getInvestmentBalances() {
		return accountService.getAccountBalance(AccountType.INVESTMENT);
	}

	@GetMapping("/chart-savings-data")
	public ChartData getChartSavingsData() {

		List<String> labelList = new ArrayList<>();
		List<BigDecimal> dataValues = new ArrayList<>();
		Map<String, BigDecimal> balanceMap = accountService.getAccountBalance(AccountType.SAVINGS);
		for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
			labelList.add(entry.getKey());
			dataValues.add(entry.getValue());
		}

		ChartData savingsChartData = new ChartData();

		savingsChartData.setLabels(labelList);
		List<ChartDataset> datasets = new ArrayList<>();

		ChartDataset acctDataset = new ChartDataset();
		acctDataset.setData(dataValues);
		acctDataset.setLabel(AccountType.SAVINGS.getDesc());
		acctDataset.setLineTension(0);
		acctDataset.setBackgroundColor("green");
		acctDataset.setBorderColor("green");
		acctDataset.setPointBackgroundColor("green");
		acctDataset.setBorderWidth(1);
		datasets.add(acctDataset);
		savingsChartData.setDatasets(datasets);

		return savingsChartData;
	}

	@GetMapping("/chart-credit-data")
	public ChartData getChartCreditData() {

		List<String> labelList = new ArrayList<>();
		List<BigDecimal> dataValues = new ArrayList<>();
		Map<String, BigDecimal> balanceMap = accountService.getAccountBalance(AccountType.CREDIT);
		for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
			labelList.add(entry.getKey());
			dataValues.add(entry.getValue());
		}

		ChartData creditChartData = new ChartData();

		creditChartData.setLabels(labelList);
		List<ChartDataset> datasets = new ArrayList<>();

		ChartDataset acctDataset = new ChartDataset();
		acctDataset.setData(dataValues);
		acctDataset.setLabel(AccountType.CREDIT.getDesc());
		acctDataset.setLineTension(0);
		acctDataset.setBackgroundColor("red");
		acctDataset.setBorderColor("red");
		acctDataset.setPointBackgroundColor("red");
		acctDataset.setBorderWidth(1);
		datasets.add(acctDataset);
		creditChartData.setDatasets(datasets);

		return creditChartData;
	}

	@GetMapping("/chart-investment-data")
	public ChartData getChartInvestmentData() {

		List<String> labelList = new ArrayList<>();
		List<BigDecimal> dataValues = new ArrayList<>();
		Map<String, BigDecimal> balanceMap = accountService.getAccountBalance(AccountType.INVESTMENT);
		for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
			labelList.add(entry.getKey());
			dataValues.add(entry.getValue());
		}

		ChartData investmentChartData = new ChartData();

		investmentChartData.setLabels(labelList);
		List<ChartDataset> datasets = new ArrayList<>();

		ChartDataset acctDataset = new ChartDataset();
		acctDataset.setData(dataValues);
		acctDataset.setLabel(AccountType.INVESTMENT.getDesc());
		acctDataset.setLineTension(0);
		acctDataset.setBackgroundColor("blue");
		acctDataset.setBorderColor("blue");
		acctDataset.setPointBackgroundColor("blue");
		acctDataset.setBorderWidth(1);
		datasets.add(acctDataset);
		investmentChartData.setDatasets(datasets);

		return investmentChartData;
	}

	@GetMapping("/chart-loans-data")
	public ChartData getChartLoanData() {

		List<String> labelList = new ArrayList<>();
		List<BigDecimal> dataValues = new ArrayList<>();
		Map<String, BigDecimal> balanceMap = accountService.getAccountBalance(AccountType.MORTGAGE);
		for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
			labelList.add(entry.getKey());
			dataValues.add(entry.getValue());
		}

		ChartData loanChartData = new ChartData();

		loanChartData.setLabels(labelList);
		List<ChartDataset> datasets = new ArrayList<>();

		ChartDataset acctDataset = new ChartDataset();
		acctDataset.setData(dataValues);
		acctDataset.setLabel(AccountType.MORTGAGE.getDesc());
		acctDataset.setLineTension(0);
		acctDataset.setBackgroundColor("red");
		acctDataset.setBorderColor("red");
		acctDataset.setPointBackgroundColor("red");
		acctDataset.setBorderWidth(1);
		datasets.add(acctDataset);
		loanChartData.setDatasets(datasets);

		return loanChartData;
	}

	@GetMapping("/fetch-securities")
	public List<Security> getAllSecurities(Model model) {
		List<Security> allSecurities = securityDao.findSecurities();
		return allSecurities;
	}

	@PostMapping("/add-security")
	public void addSecurity(@RequestBody Security security) {
		securityDao.insertSecurity(security);
	}

}
