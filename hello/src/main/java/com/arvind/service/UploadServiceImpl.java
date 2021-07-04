package com.arvind.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import com.arvind.model.ReportAccountBal;
import com.arvind.model.SavingTransaction;
import com.arvind.model.Security;
import com.arvind.model.Transaction;
import com.arvind.model.TransactionBook;
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
import com.arvind.util.MediaTypeUtils;
import com.arvind.util.TransactionType;
import com.arvind.util.Util;


@Service
public class UploadServiceImpl implements UploadService {

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
	private ServletContext servletContext;

	private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);
	
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
	
	public Map<String, Object> readKembaCheckingQuicken(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

		Map<String, Integer> acctQuickenMap = Stream.of(new Object[][] { 
		     { "Amazon Card", 25 },
		     {	"Amex Cash", 26 },
		     { "Amex Madhavi", 27 },
		     { "Arvind JC Penney", 35 },
		     { "Banana Republic", 28 },
		     { "Best Buy", 78 },
		     { "Birdwater Escrow", 70 },
		     { "Birdwater Home Mortgage", 69 },
		     { "Chase 0521", 72 },
		     { "Chase 0797", 29 },
		     { "Chase 9552", 71 },
		     { "Checking - Madhavi", 14 },
		     { "Cove View", 20 },
		     { "Discover Arvind", 30 },
		     { "Discover Checking", 24 },
		     { "Discover Madhavi", 31 },
		     { "Elan 6216", 33 },
		     { "Elan 6257", 32 },
		     { "FIA Credit Card", 77 },
		     { "Fidelity Arvind IRA", 46 },
		     { "Fifth Third Checking", 11 },
		     { "Fifth Third Escrow", 80 },
		     { "GTEFCU Checking", 73 },
		     { "GTEFCU Credit Card", 76 },
		     { "Home Depot Credit Card", 34 },
		     { "Home Equity", 21 },
		     { "Kemba Loan 2014", 67 },
		     { "Kemba Savings", 13 },
		     { "Key Checking", 22 },
		     { "Key Madhavi", 23 },
		     { "Kohls Charge", 37 },
		     { "Lowes", 38 },
		     { "Madhavi JC Penney", 36 },
		     { "New York and Company", 39 },
		     { "Odyssey Loan", 17 },
		     { "Old Navy Credit Card", 40 },
		     { "Plantation Bay Home Mortgage", 19 },
		     { "Simran 529 Plan", 65 },
		     { "TD Ameritrade Arvind", 50 },
		     { "TD Ameritrade Madhavi", 51 },
		     { "Vanguard Arvind IRA", 53 },
		     { "Vanguard Madhavi IRA", 54 },
		     { "Wells Fargo", 41 },
		     { "Wells Fargo Loan Escrow", 79 },
		     { "Wells Fargo Principal Escrow", 79 },
		 }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

		List<String> kembaPre = Stream.of(
				"GTEFCU Checking",
				"GTEFCU Credit Card",
				"Fifth Third Checking"
			).collect(Collectors.toList());
		
//		Stream.of(new Object[][] { 
//		     { "Amazon Card", 25 },
//		     {	"Amex Cash", 26 },
//		     { "Amex Madhavi", 27 },
//		     { "Arvind JC Penney", 35 },
//		     { "Banana Republic", 28 },
//		     { "Best Buy", 78 },
//		     { "Birdwater Escrow", 70 },
//		     { "Birdwater Home Mortgage", 69 },
//		     { "Chase 0521", 72 },
//		     { "Chase 0797", 29 },
//		     { "Chase 9552", 71 },
//		     { "Checking - Madhavi", 14 },
//		     { "Cove View", 20 },
//		     { "Discover Arvind", 30 },
//		     { "Discover Checking", 24 },
//		     { "Discover Madhavi", 31 },
//		     { "Elan 6216", 33 },
//		     { "Elan 6257", 32 },
//		     { "FIA Credit Card", 77 },
//		     { "Fidelity Arvind IRA", 46 },
//		     { "Fifth Third Escrow", 80 },
//		     { "Home Depot Credit Card", 34 },
//		     { "Home Equity", 21 },
//		     { "Kemba Loan 2014", 67 },
//		     { "Kemba Savings", 13 },
//		     { "Key Checking", 22 },
//		     { "Key Madhavi", 23 },
//		     { "Kohls Charge", 37 },
//		     { "Lowes", 38 },
//		     { "Madhavi JC Penney", 36 },
//		     { "New York and Company", 39 },
//		     { "Odyssey Loan", 17 },
//		     { "Old Navy Credit Card", 40 },
//		     { "Plantation Bay Home Mortgage", 19 },
//		     { "Simran 529 Plan", 65 },
//		     { "TD Ameritrade Arvind", 50 },
//		     { "TD Ameritrade Madhavi", 51 },
//		     { "Vanguard Arvind IRA", 53 },
//		     { "Vanguard Madhavi IRA", 54 },
//		     { "Wells Fargo", 41 },
//		     { "Wells Fargo Loan Escrow", 79 },
//		     { "Wells Fargo Principal Escrow", 79 },
//		 }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\t");
				CheckingTransaction trans = new CheckingTransaction();
				LocalDate localDate = LocalDate.parse(fields[1], formatter);
				trans.setTransDate(localDate);
				trans.setCheckNumber(
						StringUtils.isNumeric(fields[2]) ? Integer.valueOf(fields[2]) : NumberUtils.INTEGER_ZERO);
				trans.setInDescription(StringUtils.substring(fields[3], 0, 78));
				trans.setDescription(payees.containsKey(trans.getInDescription()) ? payees.get(trans.getInDescription())
						: trans.getInDescription());
				String transAcctStr = StringUtils.substringBetween(fields[4], "[", "]");
				if (StringUtils.isNotBlank(transAcctStr)) {
					if (acctQuickenMap.containsKey(transAcctStr)) {
						trans.setTransferAcctId(acctQuickenMap.get(transAcctStr));
						List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
						Account transferAcct = accounts.get(0);
						trans.setTransferAcct(transferAcct.getAcctName());
					}
				}
				BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[5]));
				trans.setTransAmt(transAmt);
				trans.setAcctId(acctId);
				// uploadChkTransDao.insert(trans);
				// System.out.println(line);
				checkingTransactionDao.insert(trans);
				updateCheckingTransferTransaction(trans);
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

	public Map<String, Object> readQuickenBuyFid(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int noOfLines = 0;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\t");
				if (StringUtils.isBlank(fields[3]) || !StringUtils.equals(fields[3], "Bought")) {
					continue;
				}
				System.out.println(++noOfLines + " buy records inserted");
				InvestmentTransaction invTrans = new InvestmentTransaction();
				invTrans.setAcctId(acctId);
				invTrans.setQuantity(new BigDecimal(Util.trimCommas(fields[9])));
				invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
				invTrans.setTicker(fields[5]);
				invTrans.setTransactionId(0);
				invTrans.setTransactionType(TransactionType.BUY);
				BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
				invTrans.setTransAmt(transAmt);
				LocalDate localDate = LocalDate.parse(fields[1], formatter);
				invTrans.setTransDate(localDate);
				invTrans.setTransferAcct(null);
				invTrans.setTransferAcctId(0);
				cleanInvTransaction(invTrans);
				investmentTransactionDao.insert(invTrans);
			}
		} catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<InvestmentTransaction> savedTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new InvestmentTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	public Map<String, Object> readQuickenCredit(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
			
		Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

	    Pattern pattern = Pattern.compile("(?<=\\[).+?(?=\\])", Pattern.CASE_INSENSITIVE);

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int noOfLines = 0;
			while ((line = br.readLine()) != null) {
				System.out.println("Adding " + ++noOfLines + " credit trans");
				String[] fields = line.split("\t");
				
				if (StringUtils.isNotBlank(fields[4])) {
					Matcher matcher = pattern.matcher(fields[4]);
					boolean matchFound = matcher.find();
					if(matchFound) {
//						System.out.println("Skipping line " + noOfLines);
						continue;
					}
				}
				
				CreditTransaction trans = new CreditTransaction();
				LocalDate localDate = LocalDate.parse(fields[1], formatter);
				trans.setTransDate(localDate);
				trans.setInDescription(fields[3]);
				trans.setDescription(fields[3]);
				trans.setTransAmt(new BigDecimal(Util.trimCommas(fields[5])));
				trans.setAcctId(acctId);
				creditTransactionDao.insert(trans);
//				System.out.println(line);
				result.add(trans);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());       
		}

		List<CreditTransaction> savedTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

//		modelMap.put("tbook", book);
//		modelMap.put("transactions", result);
//		modelMap.put("transtoupdate", new CreditTransaction());
//		modelMap.put("savedTransactions", savedTransactions);

		List<Account> allAccounts = accountDao.findAccounts();

	    modelMap.put("accounts", allAccounts);
		modelMap.put("acctId", acctId);
		modelMap.put("transactions", new ArrayList<CreditTransaction>());
		modelMap.put("cardTransactions", savedTransactions);
		modelMap.put("view", "cash-reconcile-upload");

		return modelMap;
	}
	
	public Map<String, Object> readQuickenInterestTest(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int noOfBuys = 0;
			int noOfSells = 0;
			int noOfDivs = 0;
			int noOfInts = 0;
			int noOfDeps = 0;
			while (true) {
				line = br.readLine();
				if (line == null) break;
				String[] fields = line.split("\t");
				if (StringUtils.equals(fields[3], "Sold")) {
					System.out.println(++noOfSells + " sell records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.SELL);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[11]));

					String gainLine = br.readLine();
					if (gainLine == null) break;
					String[] gainFields = gainLine.split("\t");
					BigDecimal gainAmt = new BigDecimal(Util.trimCommas(gainFields[11]));
					transAmt = transAmt.add(gainAmt);
					
					invTrans.setTransAmt(transAmt);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "ReinvDiv") ||
						StringUtils.equals(fields[3], "ReinvLg")) {
					System.out.println(++noOfBuys + " reinv div records inserted");
					InvestmentTransaction divTrans = new InvestmentTransaction();
					divTrans.setAcctId(acctId);
					divTrans.setTicker(fields[5]);
					divTrans.setTransactionId(0);
					divTrans.setTransactionType(TransactionType.DIVIDEND);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					divTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					divTrans.setTransDate(localDate);
					divTrans.setTransferAcct(null);
					divTrans.setTransferAcctId(0);
					cleanInvTransaction(divTrans);
					investmentTransactionDao.insert(divTrans);

					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity(new BigDecimal(Util.trimCommas(fields[9])));
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.BUY);
					invTrans.setTransAmt(transAmt);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "Div")) {
					System.out.println(++noOfDivs + " dividend records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.DIVIDEND);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);

					String gainLine = br.readLine();
					if (gainLine == null)
						break;
					String[] gainFields = gainLine.split("\t");
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(gainFields[11]));

					invTrans.setTransAmt(transAmt);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "IntInc")) {
					System.out.println(++noOfInts + " interest records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setTicker(StringUtils.EMPTY);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.INTEREST);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					
					String gainLine = br.readLine();
					if (gainLine == null)
						break;
					String[] gainFields = gainLine.split("\t");
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(gainFields[11]));

					invTrans.setTransAmt(transAmt);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "Cash")) {
					System.out.println(++noOfDeps + " deposit records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setTicker(StringUtils.EMPTY);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.DEPOSIT);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);					
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[11]));
					invTrans.setTransAmt(transAmt);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "Bought")) {
					System.out.println(++noOfBuys + " buy records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity(new BigDecimal(Util.trimCommas(fields[9])));
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.BUY);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				}				
			}
//			while ((line = br.readLine()) != null) {
//				String[] fields = line.split(",");
//				if (StringUtils.isBlank(fields[3])) {
//					continue;
//				}
//				if (StringUtils.equals(fields[3], "Sold")) {
//					System.out.println(++noOfSells + " sell records inserted");
//					InvestmentTransaction invTrans = new InvestmentTransaction();
//					invTrans.setAcctId(acctId);
//					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
//					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
//					invTrans.setTicker(fields[5]);
//					invTrans.setTransactionId(0);
//					invTrans.setTransactionType(TransactionType.SELL);
//					LocalDate localDate = LocalDate.parse(fields[1], formatter);
//					invTrans.setTransDate(localDate);
//					invTrans.setTransferAcct(null);
//					invTrans.setTransferAcctId(0);
//					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
//
//					String gainLine = br.readLine();
//					if (gainLine == null) break;
//					BigDecimal gainAmt = new BigDecimal(Util.trimCommas(fields[11]));
//					invTrans.setTransAmt(transAmt);
//					transAmt = transAmt.add(gainAmt);
//					
//					invTrans.setTransAmt(transAmt);
//					cleanInvTransaction(invTrans);
//					investmentTransactionDao.insert(invTrans);
//				}
//			}
		} catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<InvestmentTransaction> savedTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new InvestmentTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	public Map<String, Object> readQuickenBuyTab(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int noOfBuys = 0;
			int noOfSells = 0;
			int noOfDivs = 0;
			int noOfInts = 0;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(",");
				if (StringUtils.isBlank(fields[3])) {
					continue;
				}
				if (StringUtils.equals(fields[3], "Sold")) {
					System.out.println(++noOfSells + " sell records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.SELL);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "Bought")) {
					System.out.println(++noOfBuys + " buy records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity(new BigDecimal(Util.trimCommas(fields[9])));
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.BUY);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "Div")) {
					System.out.println(++noOfDivs + " dividend records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
//					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
//					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.DIVIDEND);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "IntInc")) {
					System.out.println(++noOfInts + " interest records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
//					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
//					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(StringUtils.EMPTY);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.INTEREST);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				}
			}
		} catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<InvestmentTransaction> savedTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new InvestmentTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	public Map<String, Object> readQuickenVanguard(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int noOfBuys = 0;
			int noOfSells = 0;
			int noOfDivs = 0;
			int noOfInts = 0;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\t");
				if (StringUtils.isBlank(fields[3])) {
					continue;
				}
//				if (StringUtils.equals(fields[3], "Sold")) {
//					System.out.println(++noOfSells + " sell records inserted");
//					InvestmentTransaction invTrans = new InvestmentTransaction();
//					invTrans.setAcctId(acctId);
//					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
//					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
//					invTrans.setTicker(fields[5]);
//					invTrans.setTransactionId(0);
//					invTrans.setTransactionType(TransactionType.SELL);
//					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
//					invTrans.setTransAmt(transAmt);
//					LocalDate localDate = LocalDate.parse(fields[1], formatter);
//					invTrans.setTransDate(localDate);
//					invTrans.setTransferAcct(null);
//					invTrans.setTransferAcctId(0);
//					cleanInvTransaction(invTrans);
//					investmentTransactionDao.insert(invTrans);
//				} else 
				if (StringUtils.equals(fields[3], "Bought")) {
					System.out.println(++noOfBuys + " buy records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity(new BigDecimal(Util.trimCommas(fields[9])));
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.BUY);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "ReinvDiv") ||
						StringUtils.equals(fields[3], "ReinvLg")) {
					System.out.println(++noOfBuys + " reinv div records inserted");
					InvestmentTransaction divTrans = new InvestmentTransaction();
					divTrans.setAcctId(acctId);
					divTrans.setTicker(fields[5]);
					divTrans.setTransactionId(0);
					divTrans.setTransactionType(TransactionType.DIVIDEND);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					divTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					divTrans.setTransDate(localDate);
					divTrans.setTransferAcct(null);
					divTrans.setTransferAcctId(0);
					cleanInvTransaction(divTrans);
					investmentTransactionDao.insert(divTrans);

					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity(new BigDecimal(Util.trimCommas(fields[9])));
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.BUY);
					invTrans.setTransAmt(transAmt);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "IntInc")) {
					System.out.println(++noOfInts + " interest records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setTicker(StringUtils.EMPTY);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.INTEREST);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[11]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);

//				} else if (StringUtils.equals(fields[3], "Div")) {
//					System.out.println(++noOfDivs + " dividend records inserted");
//					InvestmentTransaction invTrans = new InvestmentTransaction();
//					invTrans.setAcctId(acctId);
//					invTrans.setTicker(fields[5]);
//					invTrans.setTransactionId(0);
//					invTrans.setTransactionType(TransactionType.DIVIDEND);
//					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
//					invTrans.setTransAmt(transAmt);
//					LocalDate localDate = LocalDate.parse(fields[1], formatter);
//					invTrans.setTransDate(localDate);
//					invTrans.setTransferAcct(null);
//					invTrans.setTransferAcctId(0);
//					cleanInvTransaction(invTrans);
//					investmentTransactionDao.insert(invTrans);
				}
			}
		} catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<InvestmentTransaction> savedTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new InvestmentTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	public Map<String, Object> readQuickenSellFid(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
			String line;
			InputStream is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			int noOfSells = 0;
			int noOfInts = 0;
			int noOfDivs = 0;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\t");
				if (StringUtils.isBlank(fields[3])) {
					continue;
				}
				if (StringUtils.equals(fields[3], "Sold")) {
					System.out.println(++noOfSells + " sell records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.SELL);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "IntInc")) {
					System.out.println(++noOfInts + " interest records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
//					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
//					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(StringUtils.EMPTY);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.INTEREST);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				} else if (StringUtils.equals(fields[3], "Div")) {
					System.out.println(++noOfDivs + " dividend records inserted");
					InvestmentTransaction invTrans = new InvestmentTransaction();
					invTrans.setAcctId(acctId);
//					invTrans.setQuantity((new BigDecimal(Util.trimCommas(fields[9]))).abs());
//					invTrans.setQuote(new BigDecimal(Util.trimCommas(fields[8])));
					invTrans.setTicker(fields[5]);
					invTrans.setTransactionId(0);
					invTrans.setTransactionType(TransactionType.DIVIDEND);
					BigDecimal transAmt = new BigDecimal(Util.trimCommas(fields[12]));
					invTrans.setTransAmt(transAmt);
					LocalDate localDate = LocalDate.parse(fields[1], formatter);
					invTrans.setTransDate(localDate);
					invTrans.setTransferAcct(null);
					invTrans.setTransferAcctId(0);
					cleanInvTransaction(invTrans);
					investmentTransactionDao.insert(invTrans);
				}
			}
		} catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<InvestmentTransaction> savedTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new InvestmentTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	
	public Map<String, Object> readKeyChecking(MultipartFile file, int acctId) {
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
		    	 String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // line.split(",");
		    	 if (fields.length < 3) {
		    		 continue;
		    	 }
		    	 
		    	 if (hdrLine) {
		    		 hdrLine = false;
		    		 continue;
		    	 }

		    	 CheckingTransaction trans = new CheckingTransaction();
		    	 LocalDate localDate = LocalDate.parse(fields[0], formatter);
		    	 trans.setTransDate(localDate);
		    	 
		    	 String amtStr = Util.trimQuotes(fields[1]);
		    	 BigDecimal transAmt = new BigDecimal(amtStr);
		    	 trans.setTransAmt(transAmt);

		    	 trans.setInDescription(Util.trimQuotes(fields[2]));
		    	 trans.setDescription(payees.containsKey(trans.getInDescription()) ? payees.get(trans.getInDescription()) : trans.getInDescription());

		    	 Integer chkNo = fields.length == 4 ? Integer.valueOf(Util.trimQuotes(fields[3])) : 0;
		    	 trans.setCheckNumber(chkNo);

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

	public Map<String, Object> readCitiCard(MultipartFile file, int acctId) {
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

		    	 String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // line.split(",");

 		    	 CreditTransaction trans = new CreditTransaction();
 		    	 LocalDate localDate = LocalDate.parse(fields[1], formatter);
 		    	 trans.setTransDate(localDate);
 		    	 trans.setInDescription(Util.trimQuotes(fields[2]));
 		    	 trans.setDescription(payees.containsKey(trans.getInDescription()) ? payees.get(trans.getInDescription()) : trans.getInDescription());
 		    	 String amtStr = Util.trimCommas(fields[3]);
 		    	 if (fields.length > 4) {
 		    		 amtStr = Util.trimCommas(fields[4]);
 		    		 trans.setTransAmt(new BigDecimal(amtStr).abs());
 		    	 } else {
 		    		trans.setTransAmt(new BigDecimal(amtStr).abs().multiply(new BigDecimal(-1)));
 		    	 }
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

	public Map<String, Object> readChaseCredit(MultipartFile file, int acctId) {
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
		    	 trans.setTransAmt(new BigDecimal(fields[5]));
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

	public Map<String, Object> readDiscoverCredit(MultipartFile file, int acctId) {
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
		    	 trans.setTransAmt(new BigDecimal(fields[3]).multiply(new BigDecimal(-1)));
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

	public Map<String, Object> readAmex(MultipartFile file, int acctId) {
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
		    	 trans.setInDescription(fields[1]);
		    	 trans.setDescription(payees.containsKey(fields[1]) ? payees.get(fields[1]) : fields[1]);
		    	 trans.setTransAmt(new BigDecimal(fields[4]).multiply(new BigDecimal(-1)));
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

	public Map<String, Object> readOldNavyBanana(MultipartFile file, int acctId) {
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
		    	 trans.setInDescription(fields[4]);
		    	 trans.setDescription(payees.containsKey(fields[4]) ? payees.get(fields[4]) : fields[4]);
		    	 trans.setTransAmt(new BigDecimal(fields[3]));
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

	public Map<String, Object> readWellsFargoCard(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
		
		BufferedReader br;
		List<Transaction> result = new ArrayList<>();
		try {
		     String line;
		     InputStream is = file.getInputStream();
		     br = new BufferedReader(new InputStreamReader(is));
//		     boolean hdrLine = true;
		     while ((line = br.readLine()) != null) {
//		    	 if (hdrLine) {
//		    		 hdrLine = false;
//		    		 continue;
//		    	 }

		    	 String[] fields = line.split(",");
		    	 CreditTransaction trans = new CreditTransaction();
		    	 String dateStr = fields[0].replaceAll("\"", StringUtils.EMPTY);
		    	 LocalDate localDate = LocalDate.parse(dateStr, formatter);
		    	 trans.setTransDate(localDate);
		    	 
		    	 trans.setInDescription(fields[4]);
		    	 trans.setDescription(payees.containsKey(fields[4]) ? payees.get(fields[4]) : fields[4]);
		    	 
		    	 String amtStr = fields[1].replaceAll("\"", StringUtils.EMPTY);
		    	 trans.setTransAmt(new BigDecimal(amtStr));
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

	public Map<String, Object> readFidelityCreditTransactions(MultipartFile file, int acctId) {
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

	public Map<String, Object> readFifthThirdChecking(MultipartFile file, int acctId) {
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
		    	 CheckingTransaction trans = new CheckingTransaction();
		    	 LocalDate localDate = LocalDate.parse(fields[0], formatter);
		    	 trans.setTransDate(localDate);
		    	 trans.setInDescription(Util.trimQuotes(fields[1]));
		    	 trans.setDescription(payees.containsKey(trans.getInDescription()) ? payees.get(trans.getInDescription()) : trans.getInDescription());
		    	 Integer chkNo = StringUtils.isNumeric(fields[2]) ? Integer.valueOf(fields[2]) : 0;
		    	 trans.setCheckNumber(chkNo);
		    	 BigDecimal transAmt = new BigDecimal(fields[3]);
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

	public Map<String, Object> readKembaSavings(MultipartFile file, int acctId) {
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
		    	 SavingTransaction trans = new SavingTransaction();
		    	 LocalDate localDate = LocalDate.parse(fields[1], formatter);
		    	 trans.setTransDate(localDate);
		    	 trans.setInDescription(Util.trimQuotes(fields[3]));
		    	 trans.setDescription(payees.containsKey(trans.getInDescription()) ? payees.get(trans.getInDescription()) : trans.getInDescription());
		    	 BigDecimal transAmt = new BigDecimal(fields[4]).abs();
		    	 if (StringUtils.equals(fields[5], "DR")) {
		    		 transAmt = transAmt.multiply(new BigDecimal(-1));
		    	 }
		    	 trans.setTransAmt(transAmt);
		    	 trans.setAcctId(acctId);
		    	 uploadSavTransDao.insert(trans);
		    	 System.out.println(line);
		    	 result.add(trans);
		     }
		  } catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<SavingTransaction> savedTransactions = savingTransactionDao.findTransactionsByAcctId(acctId);

		TransactionBook book = new TransactionBook();
		book.setTransactions(result);

		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new CheckingTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}

	public int debitCreditMultiplier(TransactionType transType) {
		int rcVal = 1;
		switch (transType) {
		case BUY:
		case ENDING_BALANCE:
		case REMOVE:
		case WITHDRAW:
			rcVal = -1;
			break;
			
		case DEPOSIT:
		case ADD:
		case DIVIDEND:
		case INTEREST:
		case OPENING_BALANCE:
		case SELL:
			rcVal = 1;
			break;

		default:
			break;
		}
		return rcVal;
	}
	
	public Map<String, Object> readInvestmentTransactions(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		Map<String, Account> accountMap = new HashMap<>();
		List<Account> allAccounts = accountDao.findAccounts();	
		for (Account acct : allAccounts) {
			accountMap.put(acct.getAcctName(), acct);
		}
		
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

		    	 InvestmentTransaction trans = new InvestmentTransaction();
		    	 LocalDate localDate = LocalDate.parse(fields[0], formatter);
		    	 trans.setTransDate(localDate);
		    	 trans.setTransactionType(TransactionType.fromDesc(fields[2]));
//		    	 int multiplier = debitCreditMultiplier(trans.getTransactionType());
		    	 trans.setTicker(fields[3]);
		    	 trans.setInDescription(fields[4]);
		    	 trans.setDescription(fields[4]);
		    	 if (StringUtils.isNotBlank(fields[5]))
		    		 trans.setQuantity(new BigDecimal(fields[5]));
		    	 if (StringUtils.isNotBlank(fields[6]))
		    		 trans.setQuote(new BigDecimal(fields[6]));
		    	 if (StringUtils.isNotBlank(fields[7]))
		    		 trans.setFees(new BigDecimal(fields[7]));
		    	 trans.setTransAmt(new BigDecimal(fields[8]));
		    	 if (StringUtils.isNotBlank(fields[9]) && accountMap.containsKey(fields[9])) {
		    		 Account acct = accountMap.get(fields[9]);
			    	 trans.setTransferAcctId(acct.getAcctId());		    		 
		    	 }

		    	 trans.setAcctId(acctId);
		    	 uploadInvTransDao.insert(trans);
		    	 System.out.println(line);
		    	 result.add(trans);
		     }
		  } catch (IOException e) {
		    System.err.println(e.getMessage());       
		  }

		List<InvestmentTransaction> savedTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);

//		TransactionBook book = new TransactionBook();
//		book.setTransactions(result);
//
//		modelMap.put("tbook", book);
		modelMap.put("transactions", result);
		modelMap.put("transtoupdate", new InvestmentTransaction());
		modelMap.put("savedTransactions", savedTransactions);

		return modelMap;
	}
	
	
	@Override
	public Map<String, Object> uploadTransactionsQuicken(MultipartFile file, String acctName) {
		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		
//		switch (acct.getAcctType()) {
//
//		case INVESTMENT:
//			modelMap = readInvestmentTransactions(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-inv-trans-list");
//			break;
//
//		default:
//			break;
//		}

		if (acct.getAcctType() == AccountType.CREDIT) {
			modelMap = readQuickenCredit(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("selectedAcctName", acctName);
			modelMap.put("view", "cash-reconcile-upload");			
		} else if (acct.getAcctType() == AccountType.INVESTMENT) {
//			modelMap = readQuickenBuyFid(file, acct.getAcctId());
//			modelMap = readQuickenSellFid(file, acct.getAcctId());
//			modelMap = readQuickenBuyTab(file, acct.getAcctId());
			modelMap = readQuickenInterestTest(file, acct.getAcctId());
//			modelMap = readQuickenVanguard(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-inv-trans-list");			
		} else if (StringUtils.equals("Kemba Joint Checking", acct.getAcctName()) ||
			StringUtils.equals("Kemba Madhavi Checking", acct.getAcctName()) ||
			StringUtils.equals("GTEFCU Checking", acct.getAcctName()) ||
			StringUtils.equals("Fifth Third Checking", acct.getAcctName())) {
			modelMap = readKembaCheckingQuicken(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-checking-uploaded-trans-list");
//		} else if (StringUtils.equals("Kemba Joint Savings", acct.getAcctName()) ||
//				StringUtils.equals("Kemba Madhavi Savings", acct.getAcctName())) {
//			modelMap = readKembaSavings(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-saving-uploaded-trans-list");
//		} else if (StringUtils.equals("Fifth Third Checking", acct.getAcctName())) {
//			modelMap = readFifthThirdChecking(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-checking-uploaded-trans-list");
//		} else if (StringUtils.equals("Chase Arvind", acct.getAcctName()) ||
//				StringUtils.equals("Amazon Card", acct.getAcctName())) {
//			modelMap = readChaseCredit(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-trans-list");
//		} else if (StringUtils.equals("Fidelity 6257", acct.getAcctName()) ||
//				StringUtils.equals("Fidelity 6216", acct.getAcctName())) {
//			modelMap = readFidelityCreditTransactions(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-trans-list");
//		} else if (StringUtils.equals("Discover Arvind Card", acct.getAcctName()) ||
//				StringUtils.equals("Discover Madhavi Card", acct.getAcctName())) {
//			modelMap = readDiscoverCredit(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-trans-list");
//		} else if (StringUtils.equals("Amex Arvind", acct.getAcctName()) ||
//				StringUtils.equals("Amex Madhavi", acct.getAcctName())) {
//			modelMap = readAmex(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-trans-list");
//		} else if (StringUtils.equals("Old Navy Madhavi Card", acct.getAcctName()) ||
//				StringUtils.equals("Banana Republic", acct.getAcctName())) {
//			modelMap = readOldNavyBanana(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-trans-list");
//		} else if (StringUtils.equals("Wells Fargo Arvind Card", acct.getAcctName())) {
//			modelMap = readWellsFargoCard(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-card-uploaded-trans-list");
//		} else if (StringUtils.equals("Key Arvind Checking", acct.getAcctName()) ||
//				StringUtils.equals("Key Madhavi Checking", acct.getAcctName())) {
//			modelMap = readKeyChecking(file, acct.getAcctId());
//			modelMap.put("acctName", acctName);
//			modelMap.put("view", "cash-checking-uploaded-trans-list");
		}		
		return modelMap;
	}

	private Map<String, Object> uploadTransactions(MultipartFile file, Account acct) {
		Map<String, Object> modelMap = new HashMap<>();
		String acctName = acct.getAcctName();
		
		switch (acct.getAcctType()) {

		case INVESTMENT:
			modelMap = readInvestmentTransactions(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-inv-trans-list");
			break;
		default:
			break;
		}
		
		if (StringUtils.equals("Kemba Joint Checking", acct.getAcctName()) ||
			StringUtils.equals("Kemba Madhavi Checking", acct.getAcctName())) {
			modelMap = readKembaChecking(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-checking-uploaded-trans-list");
		} else if (StringUtils.equals("Kemba Joint Savings", acct.getAcctName()) ||
				StringUtils.equals("Kemba Madhavi Savings", acct.getAcctName())) {
			modelMap = readKembaSavings(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-saving-uploaded-trans-list");
		} else if (StringUtils.equals("Fifth Third Checking", acct.getAcctName())) {
			modelMap = readFifthThirdChecking(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-checking-uploaded-trans-list");
		} else if (StringUtils.equals("Chase Arvind", acct.getAcctName()) ||
				StringUtils.equals("Amazon Card", acct.getAcctName())) {
			modelMap = readChaseCredit(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		} else if (StringUtils.equals("Fidelity 6257", acct.getAcctName()) ||
				StringUtils.equals("Fidelity 6216", acct.getAcctName())) {
			modelMap = readFidelityCreditTransactions(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		} else if (StringUtils.equals("Discover Arvind Card", acct.getAcctName()) ||
				StringUtils.equals("Discover Madhavi Card", acct.getAcctName())) {
			modelMap = readDiscoverCredit(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		} else if (StringUtils.equals("Amex Arvind", acct.getAcctName()) ||
				StringUtils.equals("Amex Madhavi", acct.getAcctName())) {
			modelMap = readAmex(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		} else if (StringUtils.equals("Old Navy Madhavi Card", acct.getAcctName()) ||
				StringUtils.equals("Banana Republic", acct.getAcctName())) {
			modelMap = readOldNavyBanana(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		} else if (StringUtils.equals("Wells Fargo Arvind Card", acct.getAcctName())) {
			modelMap = readWellsFargoCard(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		} else if (StringUtils.equals("Key Arvind Checking", acct.getAcctName()) ||
				StringUtils.equals("Key Madhavi Checking", acct.getAcctName())) {
			modelMap = readKeyChecking(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-checking-uploaded-trans-list");
		} else if (StringUtils.equals("Citi Card", acct.getAcctName())) {
			modelMap = readCitiCard(file, acct.getAcctId());
			modelMap.put("acctName", acctName);
			modelMap.put("view", "cash-card-uploaded-trans-list");
		}
		return modelMap;
	}
	
	@Override
	public Map<String, Object> uploadTransactions(MultipartFile file, String acctName) {
		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		return uploadTransactions(file, acct);
	}	

	@Override
	public Map<String, Object> uploadTransactions(MultipartFile file, int acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsById(acctId);
		Account acct = accounts.get(0);
		return uploadTransactions(file, acct);
	}	

	@Override
	public Map<String, Object> fetchUploadedTransactions(String acctName) {

		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		int acctId = acct.getAcctId();
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
	    modelMap.put("accounts", allAccounts);
		modelMap.put("acctId", acctId);
		modelMap.put("selectedAcctName", acctName);

	    switch (acct.getAcctType()) {
		case CREDIT:
			List<CreditTransaction> transactions = uploadTransDao.findTransactionsByAcctId(acctId);
			List<CreditTransaction> cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCreditBalance(cardTransactions);
			Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
			for (CreditTransaction trans : transactions) {
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", transactions);
			modelMap.put("cardTransactions", cardTransactions);
			modelMap.put("view", "cash-reconcile-upload");
			break;
		    
		case CHECKING:
			List<CheckingTransaction> upTransactions = uploadChkTransDao.findTransactionsByAcctId(acctId);
			List<CheckingTransaction> chkTransactions = checkingTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCheckingBalance(chkTransactions);
			Map<String, String> chkPayees = payeeMapDao.getPayeeMap(acctId);
			for (CheckingTransaction trans : upTransactions) {
				trans.setDescription(chkPayees.containsKey(trans.getDescription()) ? chkPayees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", upTransactions);
			modelMap.put("chkTransactions", chkTransactions);
			modelMap.put("view", "cash-chk-reconcile-upload");
			break;
		    
		case SAVINGS:
			List<SavingTransaction> savUpTransactions = uploadSavTransDao.findTransactionsByAcctId(acctId);
			List<SavingTransaction> savTransactions = savingTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateSavingBalance(savTransactions);
			Map<String, String> savPayees = payeeMapDao.getPayeeMap(acctId);
			for (SavingTransaction trans : savUpTransactions) {
				trans.setDescription(savPayees.containsKey(trans.getDescription()) ? savPayees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", savUpTransactions);
			modelMap.put("savTransactions", savTransactions);
			modelMap.put("view", "cash-sav-reconcile-upload");
			break;
		    
		case INVESTMENT:
			List<InvestmentTransaction> invUpTransactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
			List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
			TreeMap<LocalDate, TreeMap<String, BigDecimal>> shareBalanceMap = new TreeMap<>();
			Util.updateInvestmentBalance(invTransactions);
			
//			Util.updateInvBalanceByMonth(invTransactions, shareBalanceMap);
			modelMap.put("transactions", invUpTransactions);
			modelMap.put("invTransactions", invTransactions);
			modelMap.put("view", "cash-inv-reconcile-upload");
			break;

		case AUTOLOAN:
		case MORTGAGE:
			List<LoanTransaction> loanUpTransactions = uploadLoanTransDao.findTransactionsByAcctId(acctId);
			List<LoanTransaction> loanTransactions = loanTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateLoanBalance(loanTransactions);			
			modelMap.put("transactions", loanUpTransactions);
			modelMap.put("loanTransactions", loanTransactions);
			modelMap.put("view", "cash-loan-reconcile-upload");
			break;
		    
		default:
			break;
		}
		return modelMap;
	}

	@Override
	public Map<String, Object> updateAccountBalance(String acctName) {

		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		int acctId = acct.getAcctId();
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
	    modelMap.put("accounts", allAccounts);
		modelMap.put("acctId", acctId);
		modelMap.put("selectedAcctName", acctName);

	    switch (acct.getAcctType()) {
		case CREDIT:
			List<CreditTransaction> transactions = uploadTransDao.findTransactionsByAcctId(acctId);
			List<CreditTransaction> cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCreditBalance(cardTransactions);
			Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
			for (CreditTransaction trans : transactions) {
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", transactions);
			modelMap.put("cardTransactions", cardTransactions);
			modelMap.put("view", "cash-reconcile-upload");
			break;
		    
		case CHECKING:
			List<CheckingTransaction> upTransactions = uploadChkTransDao.findTransactionsByAcctId(acctId);
			List<CheckingTransaction> chkTransactions = checkingTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCheckingBalance(chkTransactions);
			Map<String, String> chkPayees = payeeMapDao.getPayeeMap(acctId);
			for (CheckingTransaction trans : upTransactions) {
				trans.setDescription(chkPayees.containsKey(trans.getDescription()) ? chkPayees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", upTransactions);
			modelMap.put("chkTransactions", chkTransactions);
			modelMap.put("view", "cash-chk-reconcile-upload");
			break;
		    
		case SAVINGS:
			List<SavingTransaction> savUpTransactions = uploadSavTransDao.findTransactionsByAcctId(acctId);
			List<SavingTransaction> savTransactions = savingTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateSavingBalance(savTransactions);
			Map<String, String> savPayees = payeeMapDao.getPayeeMap(acctId);
			for (SavingTransaction trans : savUpTransactions) {
				trans.setDescription(savPayees.containsKey(trans.getDescription()) ? savPayees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}
			
			modelMap.put("transactions", savUpTransactions);
			modelMap.put("savTransactions", savTransactions);
			modelMap.put("view", "cash-sav-reconcile-upload");
			break;
		    
		case INVESTMENT:
			List<InvestmentTransaction> invUpTransactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
			List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
			HashMap<String, BigDecimal> balMap = Util.updateInvestmentBalance(invTransactions);

//			Util.updateInvestmentBalance(invTransactions);
			
			modelMap.put("transactions", invUpTransactions);
			modelMap.put("invTransactions", invTransactions);
			modelMap.put("view", "cash-inv-reconcile-upload");
			break;

		default:
			break;
		}
		return modelMap;
	}

	@Override
	public Map<String, Object> displayAccountBalance(Integer acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		String acctName = StringUtils.EMPTY;
		if (acctId != null) {
			List<Account> accounts = accountDao.findAccountsById(acctId);
			Account acct = accounts.get(0);
			acctName = acct.getAcctName();
			
			switch (acct.getAcctType()) {
			case CREDIT:
				List<CreditTransaction> transactions = uploadTransDao.findTransactionsByAcctId(acctId);
				List<CreditTransaction> cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateCreditBalance(cardTransactions);
				Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
				for (CreditTransaction trans : transactions) {
					trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription())
							: trans.getDescription());
					trans.setInDescription(trans.getDescription());
				}
				modelMap.put("transactions", transactions);
				modelMap.put("cardTransactions", cardTransactions);
				modelMap.put("view", "cash-reconcile-upload");
				break;
			    
			case CHECKING:
				List<CheckingTransaction> chkUptransactions = uploadChkTransDao.findTransactionsByAcctId(acctId);
				List<CheckingTransaction> chkTransactions = checkingTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateCheckingBalance(chkTransactions);
				Map<String, String> chkPayees = payeeMapDao.getPayeeMap(acctId);
				for (CheckingTransaction trans : chkUptransactions) {
					trans.setDescription(chkPayees.containsKey(trans.getDescription()) ? chkPayees.get(trans.getDescription())
							: trans.getDescription());
					trans.setInDescription(trans.getDescription());
				}
				modelMap.put("transactions", chkUptransactions);
				modelMap.put("chkTransactions", chkTransactions);
				modelMap.put("view", "cash-chk-reconcile-upload");
				break;
			    
			case SAVINGS:
				List<SavingTransaction> savUptransactions = uploadSavTransDao.findTransactionsByAcctId(acctId);
				List<SavingTransaction> savTransactions = savingTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateSavingBalance(savTransactions);
				Map<String, String> savPayees = payeeMapDao.getPayeeMap(acctId);
				for (SavingTransaction trans : savUptransactions) {
					trans.setDescription(savPayees.containsKey(trans.getDescription()) ? savPayees.get(trans.getDescription())
							: trans.getDescription());
					trans.setInDescription(trans.getDescription());
				}
				modelMap.put("transactions", savUptransactions);
				modelMap.put("savTransactions", savTransactions);
				modelMap.put("view", "cash-sav-reconcile-upload");
				break;
			    
			case INVESTMENT:
				List<InvestmentTransaction> invUpTransactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
				List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateInvestmentBalance(invTransactions);

				modelMap.put("transactions", invUpTransactions);
				modelMap.put("invTransactions", invTransactions);
				modelMap.put("view", "cash-inv-reconcile-upload");
				break;

			default:
				break;
			}
		} else {
			modelMap.put("transactions", new ArrayList<CreditTransaction>());
			modelMap.put("cardTransactions", new ArrayList<CreditTransaction>());
			modelMap.put("view", "cash-reconcile-upload");			
		}
		modelMap.put("selectedAcctName", acctName);
		modelMap.put("acctName", acctName);
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
		
		Predicate<Account> isValidAccount = acct -> (acct.getAcctType() == AccountType.AUTOLOAN
				|| acct.getAcctType() == AccountType.CHECKING || acct.getAcctType() == AccountType.CREDIT
				|| acct.getAcctType() == AccountType.MORTGAGE || acct.getAcctType() == AccountType.SAVINGS
				|| acct.getAcctType() == AccountType.INVESTMENT);
		List<Account> displayAccounts = allAccounts.stream().filter(isValidAccount).collect(Collectors.toList());
		modelMap.put("accounts", displayAccounts);
		return modelMap;
	}	

	
	
	@Override
	public Map<String, Object> displayUploadedTransactions(Integer acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		
		String acctName = StringUtils.EMPTY;
		if (acctId != null) {
			List<Account> accounts = accountDao.findAccountsById(acctId);
			Account acct = accounts.get(0);
			acctName = acct.getAcctName();
			
			switch (acct.getAcctType()) {
			case CREDIT:
				List<CreditTransaction> transactions = uploadTransDao.findTransactionsByAcctId(acctId);
				List<CreditTransaction> cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateCreditBalance(cardTransactions);
				Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
				for (CreditTransaction trans : transactions) {
					trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription())
							: trans.getDescription());
					trans.setInDescription(trans.getDescription());
				}
				modelMap.put("transactions", transactions);
				modelMap.put("cardTransactions", cardTransactions);
				modelMap.put("view", "cash-reconcile-upload");
				break;
			    
			case CHECKING:
				List<CheckingTransaction> chkUptransactions = uploadChkTransDao.findTransactionsByAcctId(acctId);
				List<CheckingTransaction> chkTransactions = checkingTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateCheckingBalance(chkTransactions);
				Map<String, String> chkPayees = payeeMapDao.getPayeeMap(acctId);
				for (CheckingTransaction trans : chkUptransactions) {
					trans.setDescription(chkPayees.containsKey(trans.getDescription()) ? chkPayees.get(trans.getDescription())
							: trans.getDescription());
					trans.setInDescription(trans.getDescription());
				}
				modelMap.put("transactions", chkUptransactions);
				modelMap.put("chkTransactions", chkTransactions);
				modelMap.put("view", "cash-chk-reconcile-upload");
				break;
			    
			case SAVINGS:
				List<SavingTransaction> savUptransactions = uploadSavTransDao.findTransactionsByAcctId(acctId);
				List<SavingTransaction> savTransactions = savingTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateSavingBalance(savTransactions);
				Map<String, String> savPayees = payeeMapDao.getPayeeMap(acctId);
				for (SavingTransaction trans : savUptransactions) {
					trans.setDescription(savPayees.containsKey(trans.getDescription()) ? savPayees.get(trans.getDescription())
							: trans.getDescription());
					trans.setInDescription(trans.getDescription());
				}
				modelMap.put("transactions", savUptransactions);
				modelMap.put("savTransactions", savTransactions);
				modelMap.put("view", "cash-sav-reconcile-upload");
				break;
			    
			case INVESTMENT:
				List<InvestmentTransaction> invUpTransactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
				List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateInvestmentBalance(invTransactions);

				modelMap.put("transactions", invUpTransactions);
				modelMap.put("invTransactions", invTransactions);
				modelMap.put("view", "cash-inv-reconcile-upload");
				break;

			case AUTOLOAN:
			case MORTGAGE:
				List<LoanTransaction> loanUpTransactions = uploadLoanTransDao.findTransactionsByAcctId(acctId);
				List<LoanTransaction> loanTransactions = loanTransactionDao.findTransactionsByAcctId(acctId);
				Util.updateLoanBalance(loanTransactions);
				modelMap.put("transactions", loanUpTransactions);
				modelMap.put("loanTransactions", loanTransactions);
				modelMap.put("view", "cash-loan-reconcile-upload");
				break;
			    
			default:
				break;
			}
		} else {
			modelMap.put("transactions", new ArrayList<CreditTransaction>());
			modelMap.put("cardTransactions", new ArrayList<CreditTransaction>());
			modelMap.put("view", "cash-reconcile-upload");			
		}
		modelMap.put("selectedAcctName", acctName);
		modelMap.put("acctName", acctName);
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
		
		Predicate<Account> isValidAccount = acct -> (acct.getAcctType() == AccountType.AUTOLOAN
				|| acct.getAcctType() == AccountType.CHECKING || acct.getAcctType() == AccountType.CREDIT
				|| acct.getAcctType() == AccountType.MORTGAGE || acct.getAcctType() == AccountType.SAVINGS
				|| acct.getAcctType() == AccountType.INVESTMENT);
		List<Account> displayAccounts = allAccounts.stream().filter(isValidAccount).collect(Collectors.toList());
		modelMap.put("accounts", displayAccounts);
		return modelMap;
	}	

	@Override
	public Map<String, Object> deleteTransaction(Integer transactionId, Integer acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		List<Account> accounts = accountDao.findAccountsById(acctId);
		Account acct = accounts.get(0);
		
		switch (acct.getAcctType()) {
		case CREDIT:
			uploadTransDao.delete(transactionId);
			break;
		    
		case INVESTMENT:
			uploadInvTransDao.delete(transactionId);
			break;

		case CHECKING:
			uploadChkTransDao.delete(transactionId);
			break;
		    
		case SAVINGS:
			uploadSavTransDao.delete(transactionId);
			break;
		    
		default:
			break;
		}
		modelMap.put("acctId", acctId);
		modelMap.put("forward", "forward:/displayuploadedtransactions");
		return modelMap;
    }

	@Override
	public Map<String, Object> acceptTransaction(Integer transactionId, Integer acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		List<Account> accounts = accountDao.findAccountsById(acctId);
		Account acct = accounts.get(0);
		
		switch (acct.getAcctType()) {
		case CREDIT:
			List<CreditTransaction> transactions = uploadTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(transactions)) {
				CreditTransaction trans = transactions.get(0);
				Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
				creditTransactionDao.insert(trans);
				uploadTransDao.delete(transactionId);
			}
			modelMap.put("acctId", acctId);
			break;
			
		case CHECKING:
			List<CheckingTransaction> chkTransactions = uploadChkTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(chkTransactions)) {
				CheckingTransaction trans = chkTransactions.get(0);
				Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
				checkingTransactionDao.insert(trans);
				uploadChkTransDao.delete(transactionId);
				updateCheckingTransferTransaction(trans);
			}
			modelMap.put("acctId", acctId);
			break;
			
		case SAVINGS:
			List<SavingTransaction> savTransactions = uploadSavTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(savTransactions)) {
				SavingTransaction trans = savTransactions.get(0);
				Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
				savingTransactionDao.insert(trans);
				uploadSavTransDao.delete(transactionId);
			}
			modelMap.put("acctId", acctId);
			break;
			
		case INVESTMENT:
			List<InvestmentTransaction> invTransactions = uploadInvTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(invTransactions)) {
				InvestmentTransaction trans = invTransactions.get(0);
				cleanInvTransaction(trans);
				investmentTransactionDao.insert(trans);
				uploadInvTransDao.delete(transactionId);
			}
			modelMap.put("acctId", acctId);
			break;

		default:
			break;
		}
		return modelMap;
	}
	
	
	@Override
	public Map<String, Object> editTransaction(Integer transactionId, Integer acctId) {
		Map<String, Object> modelMap = new HashMap<>();
		List<Account> accounts = accountDao.findAccountsById(acctId);
		Account acct = accounts.get(0);
		
		List<Account> allAccounts = accountDao.findAccounts();
	    modelMap.put("accounts", allAccounts);

		switch (acct.getAcctType()) {
		case CREDIT:
			List<CreditTransaction> transactions = uploadTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(transactions)) {
				modelMap.put("trans", transactions.get(0));
			} else {
				modelMap.put("trans", new CreditTransaction());
			}
			modelMap.put("view", "cash-edit-card-trans");
			break;

		case CHECKING:
			List<CheckingTransaction> chkTransactions = uploadChkTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(chkTransactions)) {
				modelMap.put("trans", chkTransactions.get(0));
			} else {
				modelMap.put("trans", new CheckingTransaction());
			}
			modelMap.put("view", "cash-edit-chk-trans");
			break;
			
		case SAVINGS:
			List<SavingTransaction> savTransactions = uploadSavTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(savTransactions)) {
				modelMap.put("trans", savTransactions.get(0));
			} else {
				modelMap.put("trans", new SavingTransaction());
			}
			modelMap.put("view", "cash-edit-sav-trans");
			break;
			
		case INVESTMENT:
			List<InvestmentTransaction> invTransactions = uploadInvTransDao.findTransactionsById(transactionId);
			if (!CollectionUtils.isEmpty(invTransactions)) {
				modelMap.put("trans", invTransactions.get(0));
			} else {
				modelMap.put("trans", new InvestmentTransaction());
			}
			modelMap.put("view", "cash-edit-inv-trans");
			break;

		default:
			break;
		}
		return modelMap;
	}

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

	@Override
	public void cleanInvTransaction(InvestmentTransaction trans) {

		Map<String, Security> securities = securityDao.getSecurities();
		trans.setTicker(StringUtils.stripToNull(trans.getTicker()));
		if (StringUtils.isNotBlank(trans.getTicker()) && securities.containsKey(trans.getTicker())) {
			trans.setDescription(securities.get(trans.getTicker()).getDescription());
		} else {
			trans.setDescription(trans.getTransactionType().getDesc());
		}
		trans.setInDescription(trans.getDescription());

		BigDecimal transAmt = calculateTransAmt(trans.getTransactionType(), trans.getTransAmt());
		trans.setTransAmt(transAmt);

		BigDecimal fees = calculateFees(trans.getTransactionType(), transAmt, trans.getQuantity(), trans.getQuote());
		trans.setFees(fees);

		if (trans.getQuantity() != null && trans.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
			trans.setQuantity(null);
		}

		if (trans.getFees() != null && trans.getFees().compareTo(BigDecimal.ZERO) == 0) {
			trans.setFees(null);
		}

		if (trans.getQuote() != null && trans.getQuote().compareTo(BigDecimal.ZERO) == 0) {
			trans.setQuote(null);
		}

		if (trans.getTransferAcctId() == 0) {
			trans.setTransferAcct(null);
		} else {
			List<Account> accounts = accountDao.findAccountsById(trans.getTransferAcctId());
			trans.setTransferAcct(accounts.get(0).getAcctName());
		}
	}

	@Override
	public void updateCheckingTransferTransaction(CheckingTransaction trans) {
		if (StringUtils.isBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			return;
		}
		
		String acctName = trans.getTransferAcct();
		List<Account> accounts = new ArrayList();
		if (StringUtils.isBlank(acctName)) {
			accounts = accountDao.findAccountsById(trans.getTransferAcctId());
		} else {
			accounts = accountDao.findAccountsByName(acctName);
		}
		Account transferAcct = accounts.get(0);
		if (transferAcct.getAcctId() == trans.getAcctId()) {
			return;
		}

		switch (transferAcct.getAcctType()) {
		case CHECKING: {
			CheckingTransaction xferTrans = new CheckingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setCheckNumber(trans.getCheckNumber());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			checkingTransactionDao.insert(xferTrans);
		}
			break;

		case SAVINGS: {
			SavingTransaction xferTrans = new SavingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			savingTransactionDao.insert(xferTrans);
		}
			break;

		case CREDIT: {
			CreditTransaction xferTrans = new CreditTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			creditTransactionDao.insert(xferTrans);
		}
			break;

		case INVESTMENT: {
			InvestmentTransaction xferTrans = new InvestmentTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransactionType(
					trans.getTransAmt().compareTo(new BigDecimal(0)) < 0 ? TransactionType.XIN : TransactionType.XOUT);
			xferTrans.setTransferAcctId(trans.getAcctId());
			cleanInvTransaction(xferTrans);
			investmentTransactionDao.insert(xferTrans);
		}
			break;

		case AUTOLOAN:
		case MORTGAGE: {
			LoanTransaction xferTrans = new LoanTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			loanTransactionDao.insert(xferTrans);
		}
			break;

		default:
			break;
		}
	}

	@Override
	public void updateSavingTransferTransaction(SavingTransaction trans) {
		if (StringUtils.isBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			return;
		}
		
		String acctName = trans.getTransferAcct();
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account transferAcct = accounts.get(0);
		if (transferAcct.getAcctId() == trans.getAcctId()) {
			return;
		}

		switch (transferAcct.getAcctType()) {
		case CHECKING: {
			CheckingTransaction xferTrans = new CheckingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			checkingTransactionDao.insert(xferTrans);
		}
			break;

		case SAVINGS: {
			SavingTransaction xferTrans = new SavingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			savingTransactionDao.insert(xferTrans);
		}
			break;

		case CREDIT: {
			CreditTransaction xferTrans = new CreditTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			creditTransactionDao.insert(xferTrans);
		}
			break;

		case INVESTMENT: {
			InvestmentTransaction xferTrans = new InvestmentTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransactionType(
					trans.getTransAmt().compareTo(new BigDecimal(0)) < 0 ? TransactionType.XIN : TransactionType.XOUT);
			xferTrans.setTransferAcctId(trans.getAcctId());
			cleanInvTransaction(xferTrans);
			investmentTransactionDao.insert(xferTrans);
		}
			break;

		case AUTOLOAN:
		case MORTGAGE: {
			LoanTransaction xferTrans = new LoanTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			loanTransactionDao.insert(xferTrans);
		}
			break;

		default:
			break;
		}
	}

	@Override
	public void updateCreditTransferTransaction(CreditTransaction trans) {
		if (StringUtils.isBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			return;
		}

		String acctName = trans.getTransferAcct();
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account transferAcct = accounts.get(0);
		if (transferAcct.getAcctId() == trans.getAcctId()) {
			return;
		}

		switch (transferAcct.getAcctType()) {
		case CHECKING: {
			CheckingTransaction xferTrans = new CheckingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			checkingTransactionDao.insert(xferTrans);
		}
			break;

		case SAVINGS: {
			SavingTransaction xferTrans = new SavingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			savingTransactionDao.insert(xferTrans);
		}
			break;

		case CREDIT: {
			CreditTransaction xferTrans = new CreditTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			creditTransactionDao.insert(xferTrans);
		}
			break;

		case INVESTMENT: {
			InvestmentTransaction xferTrans = new InvestmentTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransactionType(
					trans.getTransAmt().compareTo(new BigDecimal(0)) < 0 ? TransactionType.XIN : TransactionType.XOUT);
			xferTrans.setTransferAcctId(trans.getAcctId());
			cleanInvTransaction(xferTrans);
			investmentTransactionDao.insert(xferTrans);
		}
			break;

		case AUTOLOAN:
		case MORTGAGE: {
			LoanTransaction xferTrans = new LoanTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			loanTransactionDao.insert(xferTrans);
		}
			break;

		default:
			break;
		}
	}

	@Override
	public void updateInvestmentTransferTransaction(InvestmentTransaction trans) {
		if (StringUtils.isBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			return;
		}
		
		String acctName = trans.getTransferAcct();
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account transferAcct = accounts.get(0);
		if (transferAcct.getAcctId() == trans.getAcctId()) {
			return;
		}

		switch (transferAcct.getAcctType()) {
		case CHECKING: {
			CheckingTransaction xferTrans = new CheckingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription("Transfer");
			xferTrans.setInDescription("Transfer");
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			checkingTransactionDao.insert(xferTrans);
		}
			break;

		case SAVINGS: {
			SavingTransaction xferTrans = new SavingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription("Transfer");
			xferTrans.setInDescription("Transfer");
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			savingTransactionDao.insert(xferTrans);
		}
			break;

		case CREDIT: {
			CreditTransaction xferTrans = new CreditTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription("Transfer");
			xferTrans.setInDescription("Transfer");
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			creditTransactionDao.insert(xferTrans);
		}
			break;

		case INVESTMENT: {
			InvestmentTransaction xferTrans = new InvestmentTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription("Transfer");
			xferTrans.setInDescription("Transfer");
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransactionType(
					trans.getTransAmt().compareTo(new BigDecimal(0)) < 0 ? TransactionType.XIN : TransactionType.XOUT);
			xferTrans.setTransferAcctId(trans.getAcctId());
			investmentTransactionDao.insert(xferTrans);
		}
			break;

		case AUTOLOAN:
		case MORTGAGE: {
			LoanTransaction xferTrans = new LoanTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			loanTransactionDao.insert(xferTrans);
		}
			break;

		default:
			break;
		}
	}

	@Override
	public void updateLoanTransferTransaction(LoanTransaction trans) {
		if (StringUtils.isBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			return;
		}

		String acctName = trans.getTransferAcct();
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account transferAcct = accounts.get(0);
		if (transferAcct.getAcctId() == trans.getAcctId()) {
			return;
		}

		switch (transferAcct.getAcctType()) {
		case CHECKING: {
			CheckingTransaction xferTrans = new CheckingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			checkingTransactionDao.insert(xferTrans);
		}
			break;

		case SAVINGS: {
			SavingTransaction xferTrans = new SavingTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			savingTransactionDao.insert(xferTrans);
		}
			break;

		case CREDIT: {
			CreditTransaction xferTrans = new CreditTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransferAcctId(trans.getAcctId());
			creditTransactionDao.insert(xferTrans);
		}
			break;

		case INVESTMENT: {
			InvestmentTransaction xferTrans = new InvestmentTransaction();
			xferTrans.setAcctId(transferAcct.getAcctId());
			xferTrans.setTransDate(trans.getTransDate());
			xferTrans.setDescription(trans.getDescription());
			xferTrans.setInDescription(trans.getInDescription());
			xferTrans.setTransAmt(trans.getTransAmt().multiply(new BigDecimal(-1)));
			xferTrans.setTransactionType(
					trans.getTransAmt().compareTo(new BigDecimal(0)) < 0 ? TransactionType.XIN : TransactionType.XOUT);
			xferTrans.setTransferAcctId(trans.getAcctId());
			cleanInvTransaction(xferTrans);
			investmentTransactionDao.insert(xferTrans);
		}
			break;

		default:
			break;
		}
	}

	@Override
	public Map<String, Object> downloadAccount(String acctName) {
		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		int acctId = acct.getAcctId();
		
		String archieveDir = "C:\\Users\\Arvind\\Documents\\QuickenText";
		String fieldSeparator = "|";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
		
		modelMap.put("acctId",  acctId);
		
		switch (acct.getAcctType()) {
		case CREDIT: {
			String fileName = archieveDir + "\\" + acctName + ".txt";
			List<CreditTransaction> creditTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
			FileWriter fileWriter = null;
			BufferedWriter bufferedWriter = null;
			try {
				fileWriter = new FileWriter(fileName);
				bufferedWriter = new BufferedWriter(fileWriter);
				for (CreditTransaction trans : creditTransactions) {
					StringBuffer lineBuffer = new StringBuffer();
					lineBuffer.append(trans.getTransactionId()).append(fieldSeparator)
					.append(trans.getAcctId()).append(fieldSeparator)
					.append(trans.getTransDate().format(formatter)).append(fieldSeparator)
					.append(trans.getDescription()).append(fieldSeparator)
					.append(trans.getTransAmt()).append(fieldSeparator)
					.append(trans.getTransferAcct()).append(fieldSeparator)
					.append(System.lineSeparator());
					bufferedWriter.write(lineBuffer.toString());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	            try {
	                if (bufferedWriter != null)
	                    bufferedWriter.close();

	                if (fileWriter != null)
	                    fileWriter.close();
	            } catch (IOException ex) {
	                System.err.format("IOException: %s%n", ex);
	            }
	        }
		}
			break;
		    
		default:
			break;
		}
		return modelMap;
	}

	@Override
	public Map<String, Object> addTransaction(String acctName) {
		Map<String, Object> modelMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		int acctId = acct.getAcctId();

		List<Account> allAccounts = accountDao.findAccounts();
	    modelMap.put("accounts", allAccounts);

		switch (acct.getAcctType()) {
		case CREDIT:
		{
			CreditTransaction cardTransaction = new CreditTransaction();
			cardTransaction.setAcctId(acctId);
			modelMap.put("trans", cardTransaction);
			modelMap.put("view", "cash-add-card-trans");
		}
			break;

		case CHECKING:
		{
			CheckingTransaction chkTransaction = new CheckingTransaction();
			chkTransaction.setAcctId(acctId);
			modelMap.put("trans", chkTransaction);
			modelMap.put("view", "cash-add-chk-trans");
		}
			break;
			
		case SAVINGS:		
		{
			SavingTransaction savTransaction = new SavingTransaction();
			savTransaction.setAcctId(acctId);
			modelMap.put("trans", savTransaction);
			modelMap.put("view", "cash-add-sav-trans");
		}
			break;

		case INVESTMENT:
		{
			List<Integer> acctTypes = new ArrayList<>();
			acctTypes.add(AccountType.INVESTMENT.getCode());
			List<Account> invAccounts = accountDao.findAccountsByType(acctTypes);
			
			acctTypes.add(AccountType.CHECKING.getCode());
			acctTypes.add(AccountType.SAVINGS.getCode());
			acctTypes.add(AccountType.INVESTMENT.getCode());
			List<Account> filteredAccounts = accountDao.findAccountsByType(acctTypes);

			InvestmentTransaction trans = new InvestmentTransaction();
		    modelMap.put("trans", trans);
		    modelMap.put("transTypeList", TransactionType.values());
		    modelMap.put("invAcctList", invAccounts);
		    modelMap.put("acctList", filteredAccounts);
		    modelMap.put("view", "cash-add-inv-trans");
		}
			break;

		case AUTOLOAN:
		case MORTGAGE:
		{
			LoanTransaction loanTransaction = new LoanTransaction();
			loanTransaction.setAcctId(acctId);
			modelMap.put("trans", loanTransaction);
			modelMap.put("view", "cash-add-loan-trans");
		}
			break;

		default:
			break;
		}
		return modelMap;
	}

	@Override
	public Map<String, Object> addQuote() {
		Map<String, Object> modelMap = new HashMap<>();
		List<Security> securities = securityDao.findSecurities();
		Quote quote = new Quote();
	    modelMap.put("quote", quote);
	    modelMap.put("securities", securities);
	    modelMap.put("view", "cash-add-quote");
		return modelMap;
	}

	@Override
	public Map<String, Object> getNetBalance() {
		Map<String, Object> netBalance = new HashMap<>();
		
		List<AccountBal> accounts = new ArrayList<>();

		List<AccountBal> chkAccounts = new ArrayList<>();
		List<AccountBal> savAccounts = new ArrayList<>();
		List<AccountBal> invAccounts = new ArrayList<>();
		List<AccountBal> cardAccounts = new ArrayList<>();
		List<AccountBal> loanAccounts = new ArrayList<>();
		List<AccountBal> mtgAccounts = new ArrayList<>();

		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.CHECKING.getCode());
		acctTypes.add(AccountType.CREDIT.getCode());
		acctTypes.add(AccountType.SAVINGS.getCode());
		acctTypes.add(AccountType.INVESTMENT.getCode());
		acctTypes.add(AccountType.AUTOLOAN.getCode());
		acctTypes.add(AccountType.MORTGAGE.getCode());
		List<Account> filteredAccounts = accountDao.findAccountsByType(acctTypes);
		if (CollectionUtils.isEmpty(filteredAccounts)) {
			return netBalance;
		}
		
		BigDecimal netBal = BigDecimal.ZERO;
		BigDecimal invBal = BigDecimal.ZERO;
		BigDecimal chkBal = BigDecimal.ZERO;
		BigDecimal savBal = BigDecimal.ZERO;
		BigDecimal creditBal = BigDecimal.ZERO;
		BigDecimal loanBal = BigDecimal.ZERO;
		TreeMap<LocalDate, BigDecimal> creditBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> checkingBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> savingsBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> loanBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> invBalHist = new TreeMap<>();
		
		for (Account acct : filteredAccounts) {
			AccountBal bal = new AccountBal();
			bal.setAcctId(acct.getAcctId());
			bal.setAcctName(acct.getAcctName());
			bal.setAcctType(acct.getAcctType());
			bal.setParentAcctId(acct.getAcctId());
			bal.setParentAcctName(acct.getParentAcctName());
			bal.setStatus(acct.getStatus());
			if (acct.getAcctType() == AccountType.INVESTMENT) {
				bal = investmentService.getPositions(acct.getAcctName());
				netBal = netBal.add(bal.getAccountValue());
				invBal = invBal.add(bal.getAccountValue());
				invAccounts.add(bal);
			} else if (acct.getAcctType() == AccountType.CHECKING) {
				Util.updateBalanceHistory(checkingBalHist, checkingTransactionDao.getAccountBalance(bal, acct.getAcctId()));
				netBal = netBal.add(bal.getAccountValue());
				chkBal = chkBal.add(bal.getAccountValue());
				chkAccounts.add(bal);
			} else if (acct.getAcctType() == AccountType.CREDIT) {
				Util.updateBalanceHistory(creditBalHist, creditTransactionDao.getAccountBalance(bal, acct.getAcctId()));
				netBal = netBal.add(bal.getAccountValue());
				creditBal = creditBal.add(bal.getAccountValue());
				cardAccounts.add(bal);
			} else if (acct.getAcctType() == AccountType.SAVINGS) {
				Util.updateBalanceHistory(savingsBalHist, savingTransactionDao.getAccountBalance(bal, acct.getAcctId()));
				netBal = netBal.add(bal.getAccountValue());
				savBal = savBal.add(bal.getAccountValue());
				savAccounts.add(bal);
			} else if (acct.getAcctType() == AccountType.AUTOLOAN ||
					acct.getAcctType() == AccountType.MORTGAGE) {
				Util.updateBalanceHistory(loanBalHist, loanTransactionDao.getAccountBalance(bal, acct.getAcctId()));
				netBal = netBal.add(bal.getAccountValue());
				loanBal = loanBal.add(bal.getAccountValue());
				loanAccounts.add(bal);
			}
			System.out.println("Account : " + acct.getAcctName() + " Cash : " + bal.getBalanceAmt() + " Acct Val : " + bal.getAccountValue());
		}
		netBalance.put("chkAccounts", chkAccounts);
		netBalance.put("chkBal", chkBal);
		netBalance.put("savAccounts", savAccounts);
		netBalance.put("savBal", savBal);
		netBalance.put("invAccounts", invAccounts);
		netBalance.put("invBal", invBal);
		netBalance.put("cardAccounts", cardAccounts);
		netBalance.put("creditBal", creditBal);
		netBalance.put("loanAccounts", loanAccounts);
		netBalance.put("loanBal", loanBal);
		netBalance.put("netBalance", netBal);
		return netBalance;
	}

	
	@Override
	public Map<AccountType, Object> getHistoricalBalance() {
		Map<AccountType, Object> netBalance = new HashMap<>();
		
		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.CHECKING.getCode());
		acctTypes.add(AccountType.CREDIT.getCode());
		acctTypes.add(AccountType.SAVINGS.getCode());
		acctTypes.add(AccountType.INVESTMENT.getCode());
		acctTypes.add(AccountType.AUTOLOAN.getCode());
		acctTypes.add(AccountType.MORTGAGE.getCode());
		List<Account> filteredAccounts = accountDao.findAccountsByType(acctTypes);
		if (CollectionUtils.isEmpty(filteredAccounts)) {
			return netBalance;
		}
		
		TreeMap<LocalDate, BigDecimal> creditBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> checkingBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> savingsBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> loanBalHist = new TreeMap<>();
		TreeMap<LocalDate, BigDecimal> invBalHist = new TreeMap<>();
		LocalDate aprilDt = LocalDate.now().withDayOfMonth(1).plusMonths(1);
		for (Account acct : filteredAccounts) {
//			if (!StringUtils.equals(acct.getAcctName(), "Amazon Card")) {
//				continue;
//			}
			AccountBal bal = new AccountBal();
			bal.setAcctId(acct.getAcctId());
			bal.setAcctName(acct.getAcctName());
			bal.setAcctType(acct.getAcctType());
			bal.setParentAcctId(acct.getAcctId());
			bal.setParentAcctName(acct.getParentAcctName());
			bal.setStatus(acct.getStatus());
			if (acct.getAcctType() == AccountType.INVESTMENT) {
//				if (acct.getAcctId() != 46) continue;
				TreeMap<LocalDate, TreeMap<String, BigDecimal>> shareBalanceMap = new TreeMap<>();
				List<InvestmentTransaction> invTransactions = investmentTransactionDao.findTransactionsByAcctId(acct.getAcctId());
				TreeMap<LocalDate, BigDecimal> invDateBal = Util.updateInvBalanceByMonth(invTransactions, shareBalanceMap);
				BigDecimal ignoreDecimal = new BigDecimal(0.5);
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
				Util.updateBalanceHistory(invBalHist, dateAcctBal);
			} else if (acct.getAcctType() == AccountType.CHECKING) {
				Util.updateBalanceHistory(checkingBalHist, checkingTransactionDao.getAccountBalance(bal, acct.getAcctId()));
			} else if (acct.getAcctType() == AccountType.CREDIT) {
				if (StringUtils.equals(acct.getAcctName(), "Amazon Card")) {
					System.out.println("Printing AcctBalances for :" + acct.getAcctName());
				}
				Util.updateBalanceHistory(creditBalHist, creditTransactionDao.getAccountBalance(bal, acct.getAcctId()));
			} else if (acct.getAcctType() == AccountType.SAVINGS) {
				Util.updateBalanceHistory(savingsBalHist, savingTransactionDao.getAccountBalance(bal, acct.getAcctId()));
			} else if (acct.getAcctType() == AccountType.AUTOLOAN ||
					acct.getAcctType() == AccountType.MORTGAGE) {
				Util.updateBalanceHistory(loanBalHist, loanTransactionDao.getAccountBalance(bal, acct.getAcctId()));
			}
		}
		TreeMap<LocalDate, BigDecimal> netBalHist = Util.updateNetBalanceHistory(checkingBalHist, savingsBalHist, creditBalHist, loanBalHist, invBalHist);

		Util.normalizeBalanceHistory(AccountType.CHECKING, checkingBalHist);
		Util.normalizeBalanceHistory(AccountType.CREDIT, creditBalHist);
		Util.normalizeBalanceHistory(AccountType.SAVINGS, savingsBalHist);
		Util.normalizeBalanceHistory(AccountType.MORTGAGE, loanBalHist);
		Util.normalizeBalanceHistory(AccountType.INVESTMENT, invBalHist);
		Util.normalizeBalanceHistory(AccountType.NET, netBalHist);

		netBalance.put(AccountType.CHECKING, checkingBalHist);
		netBalance.put(AccountType.CREDIT, creditBalHist);
		netBalance.put(AccountType.SAVINGS, savingsBalHist);
		netBalance.put(AccountType.MORTGAGE, loanBalHist);
		netBalance.put(AccountType.INVESTMENT, invBalHist);
		netBalance.put(AccountType.NET, netBalHist);

		return netBalance;
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
	
	public void printChildren(int acctId, int depth, HashMap<Integer, Set<Integer>> acctTree, HashMap<Integer, BigDecimal> acctIdBalMap, 
			ArrayList<String> acctBalStrs, ArrayList<ReportAccountBal> acctBals, HashMap<Integer, Account> idAcctMap) {
		String acctBalStr = StringUtils.EMPTY;
		acctBalStr += getIndent(depth);
		acctBalStr += idAcctMap.get(acctId).getAcctName();
		acctBalStr += " : ";
		acctBalStr += acctIdBalMap.get(acctId);
		acctBalStr += "\n";
		ReportAccountBal acctBal = new ReportAccountBal();
		acctBal.setDepth(depth);
		acctBal.setAcctName(idAcctMap.get(acctId).getAcctName());
		acctBal.setAccountValue(acctIdBalMap.get(acctId));
		acctBals.add(acctBal);
		acctBalStrs.add(acctBalStr);
//		System.out.println(acctBalStr);
		if (!acctTree.containsKey(acctId)) {
			return;
		}
		if (CollectionUtils.isEmpty(acctTree.get(acctId))) {
			return;
		}
		for (int childAcctId : acctTree.get(acctId)) {
			printChildren(childAcctId, depth + 1, acctTree, acctIdBalMap, acctBalStrs, acctBals, idAcctMap);
		}
	}
//	public void updateAcctBalRecursive(HashMap<Integer, BigDecimal> acctIdBalMap, int acctId, BigDecimal acctVal, 
//			HashMap<Integer, Account> idAcctMap) {
//
	@Override
	public void doNetBalance(HttpServletResponse response) throws IOException {
		HashMap<Integer, BigDecimal> acctIdBalMap = new HashMap<>();
		
		List<Account> accounts = accountDao.findAccounts();
		HashMap<Integer, Account> idAcctMap = new HashMap<>();
		HashMap<Integer, Set<Integer>> acctTree = new HashMap<>();
		
//		HashMap<Account, Account> acctParentMap = new HashMap<>();
		int rootAcctId = 0;
		for (Account acct : accounts) {
			idAcctMap.put(acct.getAcctId(), acct);
			int parentAcctId = acct.getParentAcctId();
			if (parentAcctId == acct.getAcctId()) {
				rootAcctId = parentAcctId;
				continue;
			}
			if (!acctTree.containsKey(parentAcctId)) {
				acctTree.put(parentAcctId, new HashSet<>());
			}
			acctTree.get(parentAcctId).add(acct.getAcctId());
		}
		
//		for (Integer acctId : idAcctMap.keySet()) {
//			int parentId = idAcctMap.get(acctId).getParentAcctId();
//			if (idAcctMap.containsKey(parentId)) {
//				acctParentMap.put(idAcctMap.get(acctId), idAcctMap.get(parentId));
//			}
//		}
		Map<String, Object> netBalance = getNetBalance();

//		HashMap<Integer, BigDecimal> acctIdBalMap = new HashMap<>();
		List<AccountBal> chkAccounts = (List<AccountBal>) netBalance.get("chkAccounts");
		for (AccountBal acctBal : chkAccounts) {
			updateAcctBalRecursive(acctIdBalMap, acctBal.getAcctId(), acctBal.getAccountValue(), idAcctMap);
		}
		
		List<AccountBal> savAccounts = (List<AccountBal>) netBalance.get("savAccounts");
		for (AccountBal acctBal : savAccounts) {
			updateAcctBalRecursive(acctIdBalMap, acctBal.getAcctId(), acctBal.getAccountValue(), idAcctMap);
		}
		
		List<AccountBal> invAccounts = (List<AccountBal>) netBalance.get("invAccounts");
		for (AccountBal acctBal : invAccounts) {
			updateAcctBalRecursive(acctIdBalMap, acctBal.getAcctId(), acctBal.getAccountValue(), idAcctMap);
		}
		
		List<AccountBal> cardAccounts = (List<AccountBal>) netBalance.get("cardAccounts");
		for (AccountBal acctBal : cardAccounts) {
			updateAcctBalRecursive(acctIdBalMap, acctBal.getAcctId(), acctBal.getAccountValue(), idAcctMap);
		}
		
		List<AccountBal> loanAccounts = (List<AccountBal>) netBalance.get("loanAccounts");
		for (AccountBal acctBal : loanAccounts) {
			updateAcctBalRecursive(acctIdBalMap, acctBal.getAcctId(), acctBal.getAccountValue(), idAcctMap);
		}
		
//		List<AccountBal> mtgAccounts = (List<AccountBal>) netBalance.get("mtgAccounts");
//		for (AccountBal acctBal : mtgAccounts) {
//			updateAcctBalRecursive(acctIdBalMap, acctBal.getAcctId(), acctBal.getAccountValue(), idAcctMap);
//		}
//		
		int currAcctId = rootAcctId;
		int depth = 0;
		ArrayList<String> acctBalStrs = new ArrayList<>();
		ArrayList<ReportAccountBal> reportAccountBals = new ArrayList<>();
		printChildren(rootAcctId, depth, acctTree, acctIdBalMap, acctBalStrs, reportAccountBals, idAcctMap);
		for (String str : acctBalStrs) {
			System.out.println(str);
		}

		String DIRECTORY = "C:/tmp";
		String DEFAULT_FILE_NAME = "Pay.xlsx";

		MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, DEFAULT_FILE_NAME);
		System.out.println("fileName: " + DEFAULT_FILE_NAME);
		System.out.println("mediaType: " + mediaType);

		File file = new File(DIRECTORY + "/" + DEFAULT_FILE_NAME);

		// Content-Type
		// application/pdf
		response.setContentType(mediaType.getType());

		// Content-Disposition
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName());

		Workbook workbook = new XSSFWorkbook();
		
		Sheet sheet = workbook.createSheet("Persons");
//		sheet.setColumnWidth(0, 6000);
//		sheet.setColumnWidth(1, 4000);
		
		Row header = sheet.createRow(0);
		
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 11);
		font.setColor(IndexedColors.WHITE.index);
		font.setBold(true);
		headerStyle.setFont(font);
		
		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Name");
		headerCell.setCellStyle(headerStyle);
		
		headerCell = header.createCell(1);
		headerCell.setCellValue("Age");
		headerCell.setCellStyle(headerStyle);
		
//		CellStyle style = workbook.createCellStyle();
//		style.setWrapText(true);

		int rowidx = 1;
		for (ReportAccountBal reportAcctBal : reportAccountBals) {
//			System.out.println(str);
			Row row = sheet.createRow(rowidx);
			Cell cell = row.createCell(reportAcctBal.getDepth());
			cell.setCellValue(reportAcctBal.getAcctName());
//			cell.setCellStyle(style);

			cell = row.createCell(5);
			cell.setCellValue(reportAcctBal.getAccountValue() == null ? 0 : reportAcctBal.getAccountValue().doubleValue());
//			cell.setCellStyle(style);
			rowidx++;
		}

		ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();         
        outputStream.close();
        
	}
}
