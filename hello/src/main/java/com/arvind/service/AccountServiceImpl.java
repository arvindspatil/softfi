package com.arvind.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.arvind.model.Account;
import com.arvind.model.AccountBal;
import com.arvind.model.CheckingTransaction;
import com.arvind.model.CreditTransaction;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.LoanTransaction;
import com.arvind.model.Quote;
import com.arvind.model.SavingTransaction;
import com.arvind.model.Security;
import com.arvind.model.Transaction;
import com.arvind.model.TransactionBook;
import com.arvind.model.UploadCheckingTransaction;
import com.arvind.model.UploadCreditTransaction;
import com.arvind.model.UploadSavingTransaction;
import com.arvind.repository.AccountDao;
import com.arvind.repository.CheckingTransactionDao;
import com.arvind.repository.CreditTransactionDao;
import com.arvind.repository.InvestmentTransactionDao;
import com.arvind.repository.LoanTransactionDao;
import com.arvind.repository.PayeeMapDao;
import com.arvind.repository.QuoteDao;
import com.arvind.repository.SavingTransactionDao;
import com.arvind.repository.SecurityDao;
import com.arvind.repository.UploadCheckingTransDao;
import com.arvind.repository.UploadCreditTransDao;
import com.arvind.repository.UploadInvestmentTransDao;
import com.arvind.repository.UploadLoanTransDao;
import com.arvind.repository.UploadSavingTransDao;
import com.arvind.util.AccountType;
import com.arvind.util.TransactionType;
import com.arvind.util.Util;


@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	AccountDao accountDao;

	@Autowired
	PayeeMapDao payeeMapDao;

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
	UploadCreditTransDao uploadTransDao;
	
	@Autowired
	UploadCheckingTransDao uploadChkTransDao;

	@Autowired
	UploadSavingTransDao uploadSavTransDao;

	@Autowired
	UploadInvestmentTransDao uploadInvTransDao;

	@Autowired
	UploadLoanTransDao uploadLoanTransDao;
	
	@Autowired
	SecurityDao securityDao;

	@Autowired
	QuoteDao quoteDao;

	@Autowired
	InvestmentService investmentService;

	@Autowired
	UploadService uploadService;
	
//	@Autowired
//	private ServletContext servletContext;

	private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
	
	public Map<String, Object> readCreditTransactions(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
		
		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
		     String line;
		     InputStream is = file.getInputStream();
		     br = new BufferedReader(new InputStreamReader(is));
		     boolean hdrLine = true;
		     while ((line = br.readLine()) != null) {
		    	 if (hdrLine) {
		    		 hdrLine = false;
		    		 continue;
		    	 }

		    	 String[] fields = line.split(",");
		    	 CreditTransaction trans = new CreditTransaction();
		    	 LocalDate localDate = LocalDate.parse(fields[0], formatter);
		    	 trans.setTransDate(localDate);
		    	 trans.setInDescription(fields[2]);
		    	 trans.setDescription(payees.containsKey(fields[2]) ? payees.get(fields[2]) : fields[2]);
		    	 trans.setTransAmt(new BigDecimal(fields[4]));
		    	 trans.setAcctId(acctId);
		    	 uploadTransDao.insert(trans);
		    	 System.out.println(line);
		    	 result.add(trans);
		     }
		  } catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<CreditTransaction> savedTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new CreditTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	public Map<String, Object> readKembaChecking(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
		
		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
		     String line;
		     InputStream is = file.getInputStream();
		     br = new BufferedReader(new InputStreamReader(is));
		     boolean hdrLine = true;
		     while ((line = br.readLine()) != null) {
		    	 if (hdrLine) {
		    		 hdrLine = false;
		    		 continue;
		    	 }

		    	 String[] fields = line.split(",");
		    	 CheckingTransaction trans = new CheckingTransaction();
		    	 LocalDate localDate = LocalDate.parse(fields[1], formatter);
		    	 trans.setTransDate(localDate);
		    	 Integer chkNo = Integer.valueOf(fields[2]);
		    	 trans.setCheckNumber(chkNo);
		    	 trans.setInDescription(StringUtils.substring(Util.trimQuotes(fields[3]),0,78));
		    	 trans.setDescription(payees.containsKey(trans.getInDescription()) ? payees.get(trans.getInDescription()) : trans.getInDescription());
		    	 BigDecimal transAmt = new BigDecimal(fields[4]).abs();
		    	 if (StringUtils.equals(fields[5], "DR")) {
		    		 transAmt = transAmt.multiply(new BigDecimal(-1));
		    	 }
		    	 trans.setTransAmt(transAmt);
		    	 trans.setAcctId(acctId);
		    	 uploadChkTransDao.insert(trans);
		    	 System.out.println(line);
		    	 result.add(trans);
		     }
		  } catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<CheckingTransaction> savedTransactions = checkingTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new CheckingTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	
//	Amazon Card
//	Amex Cash
//	Amex Madhavi
//	Arvind JC Penney
//	Banana Republic
//	Basement Remodel
//	Best Buy
//	Birdwater Escrow
//	Birdwater Home Mortgage
//	Chase 0521
//	Chase 0797
//	Chase 9552
//	Checking - Madhavi
//	Cove View
//	Discover Arvind
//	Discover Checking
//	Discover Madhavi
//	Elan 6216
//	Elan 6257
//	FIA Credit Card
//	Fidelity Arvind IRA
//	Fifth Third Checking
//	Fifth Third Escrow
//	GTEFCU Checking
//	GTEFCU Credit Card
//	Home Depot Credit Card
//	Home Equity
//	India Home
//	Kemba Loan 2014
//	Kemba Savings
//	Key Checking
//	Key Madhavi
//	Kohls Charge
//	Kunal Fidelity IRA
//	Lowes
//	Madhavi JC Penney
//	New York and Company
//	Odyssey
//	Odyssey Loan
//	Old Navy Credit Card
//	Plantation Bay Home Mortgage
//	Simran 529 Plan
//	TD Ameritrade Arvind
//	TD Ameritrade Madhavi
//	Vanguard Arvind IRA
//	Vanguard Madhavi IRA
//	Wells Fargo
//	Wells Fargo Loan Escrow
//	Wells Fargo Principal Escrow

	

//	{ "Basement Remodel
//	{ "India Home
//	{ "Odyssey
//	{ "Kunal Fidelity IRA
	
	



	public static BigDecimal calculateTransAmt(TransactionType transType, BigDecimal inTransAmt) {
		BigDecimal transAmt = inTransAmt.abs();
		if (transType == TransactionType.BUY ||
				transType == TransactionType.XOUT ||
						transType == TransactionType.WITHDRAW) {
			transAmt = transAmt.multiply(new BigDecimal(-1));
		}
		return transAmt;
	}
	
	public static BigDecimal calculateFees(TransactionType transType, BigDecimal transAmt, BigDecimal quantity,
			BigDecimal quote) {
		BigDecimal fees = new BigDecimal(0);
		if (quantity == null || quote == null
				|| (transType != TransactionType.BUY && transType != TransactionType.SELL)) {
			return fees;
		}
		BigDecimal secCost = quantity.multiply(quote);
		if (transType == TransactionType.BUY) {
			fees = secCost.add(transAmt).abs();
		} else if (transType == TransactionType.SELL) {
			fees = secCost.subtract(transAmt).abs();
		}
		return fees;
	}


	
	public void updateAcctBalRecursive(HashMap<Integer, BigDecimal> acctIdBalMap, int acctId, BigDecimal acctVal, 
			HashMap<Integer, Account> idAcctMap) {
		BigDecimal currAcctVal = BigDecimal.ZERO;
		if (acctIdBalMap.containsKey(acctId)) {
			currAcctVal = acctIdBalMap.get(acctId);
		}
		currAcctVal = currAcctVal.add(acctVal);
		acctIdBalMap.put(acctId, currAcctVal);
		
		if (idAcctMap.containsKey(acctId)) {
			Account acct = idAcctMap.get(acctId);
			int parentId = acct.getParentAcctId();
			if (parentId == acctId) return;
			updateAcctBalRecursive(acctIdBalMap, parentId, acctVal, idAcctMap);
		}
	}
	
	public String getIndent(int depth) {
		String indentStr = StringUtils.EMPTY;
		for (int i = 0; i < depth; i++) {
			indentStr += "\t";
		}
		return indentStr;
	}
	
	@Override
	public CreditTransaction addCreditTransaction(CreditTransaction trans) {
		if (trans.getTransferAcctId() != 0) {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			Account transferAcct = accounts.get(0);
			trans.setTransferAcct(transferAcct.getAcctName());
		}
		creditTransactionDao.insert(trans);
		uploadService.updateCreditTransferTransaction(trans);
		return trans;
	}

	@Override
	public CheckingTransaction addCheckingTransaction(CheckingTransaction trans) {
		if (trans.getTransferAcctId() != 0) {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			Account transferAcct = accounts.get(0);
			trans.setTransferAcct(transferAcct.getAcctName());
		}
		checkingTransactionDao.insert(trans);
		uploadService.updateCheckingTransferTransaction(trans);
		return trans;
	}

	@Override
	public SavingTransaction addSavingsTransaction(SavingTransaction trans) {
		if (trans.getTransferAcctId() != 0) {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			Account transferAcct = accounts.get(0);
			trans.setTransferAcct(transferAcct.getAcctName());
		}
		savingTransactionDao.insert(trans);
		uploadService.updateSavingTransferTransaction(trans);
		return trans;
	}

	@Override
	public InvestmentTransaction addInvestmentTransaction(InvestmentTransaction trans) {
		if (trans.getTransferAcctId() != 0) {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			Account transferAcct = accounts.get(0);
			trans.setTransferAcct(transferAcct.getAcctName());
		}
		cleanInvTransaction(trans);
		investmentTransactionDao.insert(trans);
		investmentService.updatePrice(trans);
		uploadService.updateInvestmentTransferTransaction(trans);
		return trans;
	}

	@Override
	public LoanTransaction addLoanTransaction(LoanTransaction trans) {
		if (trans.getTransferAcctId() != 0) {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			Account transferAcct = accounts.get(0);
			trans.setTransferAcct(transferAcct.getAcctName());
		}
		loanTransactionDao.insert(trans);
		uploadService.updateLoanTransferTransaction(trans);
		return trans;
	}

	private void cleanInvTransaction(InvestmentTransaction trans) {
		Map<String, Security> securities = securityDao.getSecurities();
		trans.setTicker(StringUtils.stripToNull(trans.getTicker()));
		if (StringUtils.isNotBlank(trans.getTicker()) && securities.containsKey(trans.getTicker())) {
			trans.setDescription(securities.get(trans.getTicker()).getDescription());
		} else {
			trans.setDescription(trans.getTransactionType().getDesc());
		}
		trans.setInDescription(trans.getDescription());
		if (trans.getQuantity() != null && trans.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
			trans.setQuantity(null);
		}

		if (trans.getQuote() != null && trans.getQuote().compareTo(BigDecimal.ZERO) == 0) {
			trans.setQuote(null);
		}

		BigDecimal transAmt = calculateTransAmt(trans.getTransactionType(), String.valueOf(trans.getTransAmt()));
		BigDecimal fees = calculateFees(trans.getTransactionType(), transAmt, String.valueOf(trans.getQuantity()), String.valueOf(trans.getQuote()));

		trans.setTransAmt(transAmt);
		trans.setFees(fees);

		if (trans.getFees() != null && trans.getFees().compareTo(BigDecimal.ZERO) == 0) {
			trans.setFees(null);
		}

		if (trans.getTransferAcctId() == 0) {
			trans.setTransferAcct(null);
		} else {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			trans.setTransferAcct(accounts.get(0).getAcctName());
		}
	}
	
	private BigDecimal calculateTransAmt(TransactionType transType, String inTransAmt) {
		BigDecimal transAmt = new BigDecimal(inTransAmt).abs();
		if (transType == TransactionType.BUY ||
				transType == TransactionType.XOUT ||
						transType == TransactionType.WITHDRAW) {
			transAmt = transAmt.multiply(new BigDecimal(-1));
		}
		return transAmt;
	}
	
	private BigDecimal calculateFees(TransactionType transType, BigDecimal transAmt, String quantity, String quote) {
		BigDecimal fees = new BigDecimal(0);
		if (StringUtils.isBlank(quantity) ||
				StringUtils.isBlank(quote) || (transType != TransactionType.BUY && transType != TransactionType.SELL)) {
			return fees;
		}
		BigDecimal secCost = new BigDecimal(quantity).multiply(new BigDecimal(quote));
		if (transType == TransactionType.BUY) {
			fees = secCost.add(transAmt).abs();
		} else if (transType == TransactionType.SELL) {
			fees = secCost.subtract(transAmt).abs();
		}
		return fees;
	}

	@Override
	public Map<String, Object> fetchUploadedTransactions(Integer acctId) {
		
		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsById(acctId);
		Account acct = accounts.get(0);

	    switch (acct.getAcctType()) {
		case CREDIT:
			List<CreditTransaction> transactions = uploadTransDao.findTransactionsByAcctId(acctId);
			Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
			for (CreditTransaction trans : transactions) {
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}			
			modelMap.put("transactions", transactions);
			break;
		    
		case CHECKING:
			List<CheckingTransaction> upTransactions = uploadChkTransDao.findTransactionsByAcctId(acctId);
			Map<String, String> chkPayees = payeeMapDao.getPayeeMap(acctId);
			for (CheckingTransaction trans : upTransactions) {
				trans.setDescription(chkPayees.containsKey(trans.getDescription()) ? chkPayees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", upTransactions);
			break;
		    
		case SAVINGS:
			List<SavingTransaction> savUpTransactions = uploadSavTransDao.findTransactionsByAcctId(acctId);
			Map<String, String> savPayees = payeeMapDao.getPayeeMap(acctId);
			for (SavingTransaction trans : savUpTransactions) {
				trans.setDescription(savPayees.containsKey(trans.getDescription()) ? savPayees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}			
			modelMap.put("transactions", savUpTransactions);
			break;
		    
		case INVESTMENT:
			List<InvestmentTransaction> invUpTransactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
//			List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
//			TreeMap<LocalDate, TreeMap<String, BigDecimal>> shareBalanceMap = new TreeMap<>();
//			Util.updateInvestmentBalance(invTransactions);
			
//			Util.updateInvBalanceByMonth(invTransactions, shareBalanceMap);
			modelMap.put("transactions", invUpTransactions);
//			modelMap.put("invTransactions", invTransactions);
//			modelMap.put("view", "cash-inv-reconcile-upload");
			break;

		case AUTOLOAN:
		case MORTGAGE:
			List<LoanTransaction> loanUpTransactions = uploadLoanTransDao.findTransactionsByAcctId(acctId);
//			List<LoanTransaction> loanTransactions = loanTransactionDao.findTransactionsByAcctId(acctId);
//			Util.updateLoanBalance(loanTransactions);			
			modelMap.put("transactions", loanUpTransactions);
//			modelMap.put("loanTransactions", loanTransactions);
//			modelMap.put("view", "cash-loan-reconcile-upload");
			break;
		    
		default:
			break;
		}
		return modelMap;
	}

	private Map<String, ArrayList<Pair<LocalDate, LocalDate>>> tDates(List<InvestmentTransaction> transactions) {
		Map<String, ArrayList<Pair<LocalDate, LocalDate>>> modelMap = new HashMap<>();
		BigDecimal ignoreDecimal = new BigDecimal(0.5);
		Map<String, LocalDate> startDates = new HashMap<>();
		ReverseListIterator<InvestmentTransaction> riter = new ReverseListIterator<>(transactions);
		while (riter.hasNext()) {
			InvestmentTransaction trans = riter.next();
			if (StringUtils.isBlank(trans.getTicker())) {
				continue;
			}
			BigDecimal currentQty = trans.getBalanceQty();
			if (currentQty.compareTo(ignoreDecimal) < 0) {
				if (!startDates.containsKey(trans.getTicker())) {
					continue;
				}
				LocalDate startDate = startDates.get(trans.getTicker());
				Pair<LocalDate, LocalDate> timeWindow = Pair.of(startDate, trans.getTransDate());
				if (!modelMap.containsKey(trans.getTicker())) {
					modelMap.put(trans.getTicker(), new ArrayList<>());
				}
				modelMap.get(trans.getTicker()).add(timeWindow);
				startDates.remove(trans.getTicker());
			} else {
				if (startDates.containsKey(trans.getTicker())) {
					continue;
				}
				startDates.put(trans.getTicker(), trans.getTransDate());
			}
		}
		System.out.println("Old ones:");
		for (String ticker : modelMap.keySet()) {
			System.out.println("Ticker: " + ticker);
			for (Pair<LocalDate, LocalDate> windows : modelMap.get(ticker)) {
				System.out.println("Start Date :" + windows.getKey() + ": End Date :" + windows.getValue() + ":");
			}
		}
		LocalDate currentDate = LocalDate.now();
		System.out.println("Current ones:");
		for (String ticker : startDates.keySet()) {
			System.out.println("Ticker: " + ticker);
			System.out.println("Start Date :" + startDates.get(ticker) + ": End Date :" + currentDate + ":");
		}
		return modelMap;
	}
	
	@Override
	public Map<String, Object> fetchTransactions(Integer acctId) {

		Map<String, Object> modelMap = new HashMap<>();

		List<Account> accounts = accountDao.findAccountsById(acctId);
		Account acct = accounts.get(0);

		switch (acct.getAcctType()) {
		case CREDIT:
			List<CreditTransaction> cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCreditBalance(cardTransactions);
			modelMap.put("cardTransactions", cardTransactions);
			break;

		case CHECKING:
			List<CheckingTransaction> chkTransactions = checkingTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCheckingBalance(chkTransactions);
			modelMap.put("chkTransactions", chkTransactions);
			break;

		case SAVINGS:
			List<SavingTransaction> savTransactions = savingTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateSavingBalance(savTransactions);
			modelMap.put("savTransactions", savTransactions);
			break;

		case INVESTMENT:
			List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
			TreeMap<LocalDate, TreeMap<String, BigDecimal>> shareBalanceMap = new TreeMap<>();
			Util.updateInvestmentBalance(invTransactions);
			modelMap.put("invTransactions", invTransactions);
			tDates(invTransactions);
			break;

		case AUTOLOAN:
		case MORTGAGE:
//			List<LoanTransaction> loanUpTransactions = uploadLoanTransDao.findTransactionsByAcctId(acctId);
			List<LoanTransaction> loanTransactions = loanTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateLoanBalance(loanTransactions);
//			modelMap.put("transactions", loanUpTransactions);
			modelMap.put("loanTransactions", loanTransactions);
//			modelMap.put("view", "cash-loan-reconcile-upload");
			break;

		default:
			break;
		}
		return modelMap;
	}

	@Override
	public void updateAcceptUploadedCheckingTransaction(UploadCheckingTransaction trans) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		CheckingTransaction chkTrans = toCheckingTransaction(trans);
		checkingTransactionDao.insert(chkTrans);
		if (trans.isDecision()) {
			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());			
			uploadChkTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
		}
		uploadChkTransDao.delete(trans.getTransactionId());
		uploadService.updateCheckingTransferTransaction(chkTrans);
	}

	@Override
	public void updateAcceptUploadedSavingsTransaction(UploadSavingTransaction trans) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		SavingTransaction savTrans = toSavingTransaction(trans);
		savingTransactionDao.insert(savTrans);
		if (trans.isDecision()) {
			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());			
			uploadSavTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
		}
		uploadSavTransDao.delete(trans.getTransactionId());
		uploadService.updateSavingTransferTransaction(savTrans);
	}

	@Override
	public void updateAcceptUploadedCreditTransaction(UploadCreditTransaction trans) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		CreditTransaction creditTrans = toCreditTransaction(trans);
		creditTransactionDao.insert(creditTrans);
		if (trans.isDecision()) {
			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());			
			uploadTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
		}
		uploadTransDao.delete(trans.getTransactionId());
		uploadService.updateCreditTransferTransaction(creditTrans);
	}

	private CheckingTransaction toCheckingTransaction(UploadCheckingTransaction trans) {
		CheckingTransaction chkTrans = new CheckingTransaction();
		chkTrans.setAcctId(trans.getAcctId());
		chkTrans.setCheckNumber(trans.getCheckNumber());
		chkTrans.setDescription(trans.getDescription());
		chkTrans.setInDescription(trans.getInDescription());
		chkTrans.setTransactionId(trans.getTransactionId());
		chkTrans.setTransAmt(trans.getTransAmt());
		chkTrans.setTransDate(trans.getTransDate());
		chkTrans.setTransferAcct(trans.getTransferAcct());
		chkTrans.setTransferAcctId(trans.getTransferAcctId());
		return chkTrans;
	}

	private SavingTransaction toSavingTransaction(UploadSavingTransaction trans) {
		SavingTransaction savTrans = new SavingTransaction();
		savTrans.setAcctId(trans.getAcctId());
		savTrans.setDescription(trans.getDescription());
		savTrans.setInDescription(trans.getInDescription());
		savTrans.setTransactionId(trans.getTransactionId());
		savTrans.setTransAmt(trans.getTransAmt());
		savTrans.setTransDate(trans.getTransDate());
		savTrans.setTransferAcct(trans.getTransferAcct());
		savTrans.setTransferAcctId(trans.getTransferAcctId());
		return savTrans;
	}

	private CreditTransaction toCreditTransaction(UploadCreditTransaction trans) {
		CreditTransaction creditTrans = new CreditTransaction();
		creditTrans.setAcctId(trans.getAcctId());
		creditTrans.setDescription(trans.getDescription());
		creditTrans.setInDescription(trans.getInDescription());
		creditTrans.setTransactionId(trans.getTransactionId());
		creditTrans.setTransAmt(trans.getTransAmt());
		creditTrans.setTransDate(trans.getTransDate());
		creditTrans.setTransferAcct(trans.getTransferAcct());
		creditTrans.setTransferAcctId(trans.getTransferAcctId());
		return creditTrans;
	}

	@Override
	public void deleteUploadedCheckingTransaction(Integer transactionId) {
//		Map<String, Object> modelMap = new HashMap<>();
//		List<Account> accounts = accountDao.findAccountsById(acctId);
//		Account acct = accounts.get(0);
		
//		switch (acct.getAcctType()) {
//		case CREDIT:
//			uploadTransDao.delete(transactionId);
//			break;
//		    
//		case INVESTMENT:
//			uploadInvTransDao.delete(transactionId);
//			break;

//		case CHECKING:
			uploadChkTransDao.delete(transactionId);
//			break;
//		    
//		case SAVINGS:
//			uploadSavTransDao.delete(transactionId);
//			break;
//		    
//		default:
//			break;
//		}
//		modelMap.put("acctId", acctId);
//		modelMap.put("forward", "forward:/displayuploadedtransactions");
//		return modelMap;
    }

	@Override
	public void deleteUploadedSavingsTransaction(Integer transactionId) {
		uploadSavTransDao.delete(transactionId);
    }

	@Override
	public void deleteUploadedCreditTransaction(Integer transactionId) {
		uploadTransDao.delete(transactionId);
    }

	@Override
	public Map<String, BigDecimal> getAccountBalance(AccountType acctType) {
		BigDecimal ignoreDecimal = new BigDecimal(0.5);
		Map<String, BigDecimal> balanceMap = new TreeMap<>();
		List<Integer> acctTypes = new ArrayList<>();
		if (acctType == AccountType.AUTOLOAN ||
			acctType == AccountType.MORTGAGE) {
			acctTypes.add(AccountType.AUTOLOAN.getCode());
			acctTypes.add(AccountType.MORTGAGE.getCode());
		} else {
			acctTypes.add(acctType.getCode());
		}
		List<Account> filteredAccounts = accountDao.findAccountsByType(acctTypes);
		if (CollectionUtils.isEmpty(filteredAccounts)) {
			return balanceMap;
		}
		
		for (Account acct : filteredAccounts) {
			AccountBal bal = new AccountBal();
			bal.setAcctId(acct.getAcctId());
			bal.setAcctName(acct.getAcctName());
			bal.setAcctType(acct.getAcctType());
			bal.setParentAcctId(acct.getAcctId());
			bal.setParentAcctName(acct.getParentAcctName());
			bal.setStatus(acct.getStatus());
			if (acctType == AccountType.CHECKING) {
				checkingTransactionDao.getAccountBalance(bal, acct.getAcctId());
			} else if (acctType == AccountType.SAVINGS) {
				savingTransactionDao.getAccountBalance(bal, acct.getAcctId());
			} else if (acctType == AccountType.CREDIT) {
				creditTransactionDao.getAccountBalance(bal, acct.getAcctId());
			} else if (acct.getAcctType() == AccountType.AUTOLOAN ||
					acct.getAcctType() == AccountType.MORTGAGE) {
				loanTransactionDao.getAccountBalance(bal, acct.getAcctId());
			} else if (acct.getAcctType() == AccountType.INVESTMENT) {
				TreeMap<LocalDate, TreeMap<String, BigDecimal>> shareBalanceMap = new TreeMap<>();
				List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acct.getAcctId());
				TreeMap<LocalDate, BigDecimal> invDateBal = Util.updateInvBalanceByMonth(invTransactions, shareBalanceMap);
				TreeMap<LocalDate, BigDecimal> dateAcctBal = new TreeMap<>();
				for (LocalDate currentDt : invDateBal.keySet()) {
					BigDecimal cashBal = invDateBal.get(currentDt);
					BigDecimal dateVal = cashBal;
					if (!shareBalanceMap.containsKey(currentDt)) continue;
					TreeMap<String, BigDecimal> shareBal = shareBalanceMap.get(currentDt);
					if (shareBal == null) continue;
					for (String tckr : shareBal.keySet()) {
						BigDecimal currentQty = shareBal.get(tckr);
						BigDecimal currentQuote = BigDecimal.ONE;
						if (currentQty.compareTo(ignoreDecimal) < 0) continue;
						List<Quote> quotes = quoteDao.findNearestQuoteByTickerDate(tckr, currentDt);
						if (!CollectionUtils.isEmpty(quotes)) {
							currentQuote = quotes.get(0).getPricePs();
						}
						BigDecimal positionVal = currentQuote.multiply(currentQty);
						dateVal = dateVal.add(positionVal);
					}
					dateAcctBal.put(currentDt, dateVal);
//					System.out.println("Date 1 :" + currentDt + ": Account :" + acct.getAcctName() + ": Balance :" + dateVal + ":");
				}
				BigDecimal acctVal = BigDecimal.ZERO;
				if (!dateAcctBal.isEmpty()) {
					acctVal = dateAcctBal.lastEntry().getValue();
				}
				bal.setBalanceAmt(acctVal);
			}
			if (bal.getBalanceAmt().abs().compareTo(ignoreDecimal) < 0) {
				continue;
			}
			balanceMap.put(acct.getAcctName(), bal.getBalanceAmt().setScale(2, RoundingMode.HALF_UP));
		}
		return balanceMap;
	}

	@Override
	public void updateAccountRecon(Integer acctId, LocalDate reconDate) {
		List<Pair<Account, LocalDate>> reconDates = accountDao.findReconDateByAcctId(acctId);
		if (CollectionUtils.isEmpty(reconDates)) {
			return;
		}
		
		LocalDate acctReconDate = reconDates.get(0).getValue();
		if (acctReconDate == null) {
			accountDao.insertAccountRecon(acctId, reconDate);
		} else {
			accountDao.updateAccountRecon(acctId, reconDate);
		}
	}

}
