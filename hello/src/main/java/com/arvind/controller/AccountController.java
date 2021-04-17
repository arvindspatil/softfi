package com.arvind.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.arvind.model.TransactionBook;
import com.arvind.repository.AccountDao;
import com.arvind.repository.CheckingTransactionDao;
import com.arvind.repository.CreditTransactionDao;
import com.arvind.repository.InvestmentTransactionDao;
import com.arvind.repository.LoanTransactionDao;
import com.arvind.repository.PayeeMapDao;
import com.arvind.repository.SavingTransactionDao;
import com.arvind.repository.SecurityDao;
import com.arvind.repository.UploadCheckingTransDao;
import com.arvind.repository.UploadCreditTransDao;
import com.arvind.repository.UploadInvestmentTransDao;
import com.arvind.repository.UploadSavingTransDao;
import com.arvind.service.InvestmentService;
import com.arvind.service.UploadService;
import com.arvind.util.AccountType;
import com.arvind.util.Status;
import com.arvind.util.TransactionType;
import com.arvind.util.Util;

@Controller
public class AccountController {

	@Autowired
	AccountDao accountDao;

	@Autowired
	PayeeMapDao payeeMapDao;

	@Autowired
	UploadCreditTransDao updateTransDao;

	@Autowired
	CreditTransactionDao creditTransactionDao;

	@Autowired
	CheckingTransactionDao checkingTransactionDao;

	@Autowired
	SavingTransactionDao savingTransactionDao;

	@Autowired
	UploadCreditTransDao uploadTransDao;
	
	@Autowired
	UploadCheckingTransDao uploadChkTransDao;
	
	@Autowired
	UploadSavingTransDao uploadSavTransDao;
	
	@Autowired
	UploadService uploadService;

	@Autowired
	InvestmentTransactionDao investmentTransactionDao;

	@Autowired
	UploadInvestmentTransDao uploadInvTransDao;
	
	@Autowired
	LoanTransactionDao loanTransactionDao;
	
	@Autowired
	SecurityDao securityDao;

	@Autowired
	InvestmentService investmentService;

	private static final Logger log = LoggerFactory.getLogger(AccountController.class);

	@GetMapping ("/allaccounts")
	public String handleAllAccounts(Model model) {
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);		
	    model.addAttribute("accounts", allAccounts);
 	    return "cash-account-list";
	}
	
	@RequestMapping(value = "/displaycardtransactions")
    public String displayCardTransactions(
        @RequestParam(value = "acctId", required = true) int acctId,
        Model model
    ) {
		List<Account> accounts = accountDao.findAccountsById(acctId);
		String acctName = accounts.get(0).getAcctName();
		
		List<CreditTransaction> transactions = creditTransactionDao.findTransactionsByAcctId(acctId);
		model.addAttribute("transactions", transactions);
		model.addAttribute("acctName", acctName);
		return "cash-card-trans-list";
    }

	@GetMapping(value = "/displayuploadedtransactions")
    public String displayUploadedTransactions(
        @RequestParam(value = "acctId", required = false) Integer acctId,
        Model model
    ) {
		Map<String, Object> modelMap = uploadService.displayUploadedTransactions(acctId);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
//		List<CreditTransaction> transactions = new ArrayList<CreditTransaction>();
//		List<CreditTransaction> cardTransactions = new ArrayList<CreditTransaction>();
//		String acctName = StringUtils.EMPTY;
//		if (acctId != null) {
//			transactions = uploadTransDao.findTransactionsByAcctId(acctId);			
//			Map<String, String> payees = payeeMapDao.getPayeeMap();
//			for (CreditTransaction trans : transactions) {
//				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
//				trans.setInDescription(trans.getDescription());
//			}
//
//			List<Account> accounts = accountDao.findAccountsById(acctId);
//			acctName = accounts.get(0).getAcctName();
//			
//			cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);			
//		}
//		model.addAttribute("transactions", transactions);
//		model.addAttribute("selectedAcctName", acctName);
//		
//		List<Account> allAccounts = accountDao.findAccounts();
//		Util.updateParent(allAccounts);		
//	    model.addAttribute("accounts", allAccounts);
//	    
//		model.addAttribute("cardTransactions", cardTransactions);
//		model.addAttribute("acctName", acctName);
//
//		return "cash-reconcile-upload";
    }
	
	@GetMapping(value = "/displayuploadedcardtransactions")
    public String displayUploadedCardTransactions(
        @RequestParam(value = "acctId", required = false) Integer acctId,
        Model model
    ) {
		List<CreditTransaction> transactions = new ArrayList<CreditTransaction>();
		List<CreditTransaction> cardTransactions = new ArrayList<CreditTransaction>();
		String acctName = StringUtils.EMPTY;
		if (acctId != null) {
			transactions = uploadTransDao.findTransactionsByAcctId(acctId);			
			Map<String, String> payees = payeeMapDao.getPayeeMap(acctId);
			for (CreditTransaction trans : transactions) {
				trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
				trans.setInDescription(trans.getDescription());
			}

			List<Account> accounts = accountDao.findAccountsById(acctId);
			acctName = accounts.get(0).getAcctName();
			
			cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateCreditBalance(cardTransactions);
		}
		model.addAttribute("transactions", transactions);
		model.addAttribute("selectedAcctName", acctName);
		
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);		
	    model.addAttribute("accounts", allAccounts);
	    
		model.addAttribute("cardTransactions", cardTransactions);
		model.addAttribute("acctName", acctName);

		return "cash-reconcile-upload";
    }
	
	@GetMapping(value = "/displayuploadedinvtransactions")
    public String displayUploadedInvTransactions(
        @RequestParam(value = "acctId", required = false) Integer acctId,
        Model model
    ) {
		List<InvestmentTransaction> transactions = new ArrayList<InvestmentTransaction>();
		List<InvestmentTransaction> invTransactions = new ArrayList<InvestmentTransaction>();
		String acctName = StringUtils.EMPTY;
		if (acctId != null) {
			transactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
			List<Account> accounts = accountDao.findAccountsById(acctId);
			acctName = accounts.get(0).getAcctName();
			invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
			Util.updateInvestmentBalance(invTransactions);
		}
		model.addAttribute("transactions", transactions);
		model.addAttribute("selectedAcctName", acctName);
		
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);		
	    model.addAttribute("accounts", allAccounts);
	    
		model.addAttribute("invTransactions", invTransactions);
		model.addAttribute("acctName", acctName);

		return "cash-inv-reconcile-upload";
    }

	@GetMapping(value = "/displayaccountbalance")
    public String displayAccountBalance(
        @RequestParam(value = "acctId", required = false) Integer acctId,
        Model model) {
		Map<String, Object> modelMap = uploadService.displayAccountBalance(acctId);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
//
//		List<InvestmentTransaction> transactions = new ArrayList<InvestmentTransaction>();
//		List<InvestmentTransaction> invTransactions = new ArrayList<InvestmentTransaction>();
//		String acctName = StringUtils.EMPTY;
//		if (acctId != null) {
//			transactions = uploadInvTransDao.findTransactionsByAcctId(acctId);
//			List<Account> accounts = accountDao.findAccountsById(acctId);
//			acctName = accounts.get(0).getAcctName();
//			invTransactions = investmentTransactionDao.findTransactionsByAcctId(acctId);
//			Util.updateInvestmentBalance(invTransactions);
//		}
//		model.addAttribute("transactions", transactions);
//		model.addAttribute("selectedAcctName", acctName);
//		
//		List<Account> allAccounts = accountDao.findAccounts();
//		Util.updateParent(allAccounts);		
//	    model.addAttribute("accounts", allAccounts);
//	    
//		model.addAttribute("invTransactions", invTransactions);
//		model.addAttribute("acctName", acctName);
//
//		return "cash-inv-reconcile-upload";
    }
	
	@GetMapping("/newacct")
	public String showNewAccountPage(Model model) {
		List<Account> allAccounts = accountDao.findAccounts();	    	    

		Account acct = new Account();
	    model.addAttribute("account", acct);
	    model.addAttribute("acctTypeList", AccountType.values());
	    model.addAttribute("statusList", Status.values());
	    model.addAttribute("acctList", allAccounts);
	    return "cash-create-acct";
	}

	@GetMapping("/shownewinvtrans")
	public String showNewInvTransaction(Model model) {
		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.INVESTMENT.getCode());
		List<Account> invAccounts = accountDao.findAccountsByType(acctTypes);
		
		acctTypes.add(AccountType.CHECKING.getCode());
		acctTypes.add(AccountType.SAVINGS.getCode());
		acctTypes.add(AccountType.INVESTMENT.getCode());
		List<Account> allAccounts = accountDao.findAccountsByType(acctTypes);

		InvestmentTransaction trans = new InvestmentTransaction();
	    model.addAttribute("trans", trans);
	    model.addAttribute("transTypeList", TransactionType.values());
	    model.addAttribute("invAcctList", invAccounts);
	    model.addAttribute("acctList", allAccounts);
	    return "cash-add-inv-trans";
	}

	@PostMapping(value="/createacct", params = "action=save")
	public ModelAndView createAccount(@ModelAttribute(value="account") Account acct) {
		accountDao.insertAccount(acct);
		return new ModelAndView("redirect:/allaccounts");
	}

	@GetMapping(value="/getuploadedtransactions")
	public String getUploadedTransactions(ModelMap model) {
//		List<Account> accounts = accountDao.findAccountsByName(acctName);
//		int acctId = accounts.get(0).getAcctId();					
//		model.addAttribute("acctId", acctId);
//		List<CreditTransaction> transactions = uploadTransDao.findTransactionsByAcctId(acctId);
//		List<CreditTransaction> cardTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
		
//		Map<String, String> payees = payeeMapDao.getPayeeMap();
//		for (CreditTransaction trans : transactions) {
//			trans.setDescription(payees.containsKey(trans.getDescription()) ? payees.get(trans.getDescription()) : trans.getDescription());
//			trans.setInDescription(trans.getDescription());
//		}
		List<CreditTransaction> transactions = new ArrayList<>();
		String acctName = StringUtils.EMPTY;
		
		model.addAttribute("transactions", transactions);
		model.addAttribute("cardTransactions", transactions);
		model.addAttribute("selectedAcctName", acctName);
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
	    model.addAttribute("accounts", allAccounts);
		return "cash-reconcile-upload";
	}

	@PostMapping(value="/getuploadedtransactions", params = "action=save")
	public String fetchUploadedTransactions(@ModelAttribute(value="acctName") String acctName, ModelMap model) { // throws InterruptedException {
		Map<String, Object> modelMap = uploadService.fetchUploadedTransactions(acctName);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
	}

	@PostMapping(value="/getuploadedtransactions", params = "action=add")
	public String addTransactions(@ModelAttribute(value="acctName") String acctName, ModelMap model) {
		Map<String, Object> modelMap = uploadService.addTransaction(acctName);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
	}

	@PostMapping(value="/getuploadedtransactions", params = "action=addquote")
	public String addQuote(@ModelAttribute(value="acctName") String acctName, ModelMap model) {
		Map<String, Object> modelMap = uploadService.addQuote();
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
	}

	@PostMapping(value="/createacct", params = "action=cancel")
	public ModelAndView cancelCreateAccount(@ModelAttribute(value="account") Account acct) {
		return new ModelAndView("redirect:/allaccounts");
	}

	@GetMapping ("/displayuploadaccts")
	public String displayUploadAccounts(Model model) {
		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.AUTOLOAN.getCode());
		acctTypes.add(AccountType.CHECKING.getCode());
		acctTypes.add(AccountType.CREDIT.getCode());
		acctTypes.add(AccountType.MORTGAGE.getCode());
		acctTypes.add(AccountType.SAVINGS.getCode());
		acctTypes.add(AccountType.INVESTMENT.getCode());
		List<Account> allAccounts = accountDao.findAccountsByType(acctTypes);
	    model.addAttribute("accounts", allAccounts);
	    return "cash-display-upload";
	}

	@PostMapping ("/uploadacct")
	public String handleUploadAccount(@RequestParam("file") MultipartFile file,
			@RequestParam(name="acctName") String acctName,
			ModelMap model) {
		Map<String, Object> modelMap = uploadService.uploadTransactions(file, acctName);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
		/*
		 * else {
		 */		//	return new ModelAndView("forward:/displayuploadedtransactions", model);
//		}

		
//		Map<String, String> payees = payeeMapDao.getPayeeMap();
//		List<Account> accounts = accountDao.findAccountsByName(acctName);
//		int acctId = accounts.get(0).getAcctId();
//		
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
//		
//		BufferedReader br;
//		List<Transaction> result = new ArrayList<>();
//		try {
//		     String line;
//		     InputStream is = file.getInputStream();
//		     br = new BufferedReader(new InputStreamReader(is));
//		     boolean hdrLine = true;
//		     while ((line = br.readLine()) != null) {
//		    	 if (hdrLine) {
//		    		 hdrLine = false;
//		    		 continue;
//		    	 }
//
//		    	 String[] fields = line.split(",");
//		    	 CreditTransaction trans = new CreditTransaction();
//		    	 LocalDate localDate = LocalDate.parse(fields[0], formatter);
//		    	 trans.setTransDate(localDate);
//		    	 trans.setInDescription(fields[2]);
//		    	 trans.setDescription(payees.containsKey(fields[2]) ? payees.get(fields[2]) : fields[2]);
//		    	 trans.setTransAmt(new BigDecimal(fields[4]));
//		    	 trans.setAcctId(acctId);
//		    	 updateTransDao.insert(trans);
//		    	 System.out.println(line);
//		    	 result.add(trans);
//		     }
//		  } catch (IOException e) {
//		    System.err.println(e.getMessage());       
//		  }
//		
//		List<CreditTransaction> savedTransactions = creditTransactionDao.findTransactionsByAcctId(acctId);
//
//		TransactionBook book = new TransactionBook();
//		book.setTransactions(result);
//	    model.addAttribute("tbook", book);
//	    model.addAttribute("transactions", result);
//	    model.addAttribute("acctName", acctName);
//	    model.addAttribute("transtoupdate", new CreditTransaction());
//	    model.addAttribute("savedTransactions", savedTransactions);
//	    return "cash-card-uploaded-trans-list";
	}

	@PostMapping ("/uploadacctquicken")
	public String handleUploadAccountQuicken(@RequestParam("file") MultipartFile file,
			@RequestParam(name="acctName") String acctName,
			ModelMap model) {
		Map<String, Object> modelMap = uploadService.uploadTransactionsQuicken(file, acctName);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");
	}


	@PostMapping(value="/savetrans1")
	public ModelAndView saveTransaction(@ModelAttribute(value="transtoupdate") TransactionBook trans) { // , Model model) {
	    ModelAndView mav = new ModelAndView("displayv6");
//		BookList books = new BookList();
//	    List<Book> firstNames = actorDao.findEmployeeByTitle(book.getTitle());
//	    List<Book> allbooks = actorDao.findEmployeeById(100);
//	    
//	    for (Book bk : allbooks) {
//	    	books.addBook(bk);
//	    }	    	    
//	    mav.addObject("form", books);
//	    mav.addObject("result", firstNames);
	    return mav;
	}

	/*
	 * @GetMapping("/newacct") public String showNewAccountPage(Model model) {
	 * List<Account> allAccounts = accountDao.findAccounts();
	 * 
	 * Account acct = new Account(); model.addAttribute("account", acct);
	 * model.addAttribute("acctTypeList", AccountType.values());
	 * model.addAttribute("statusList", Status.values());
	 * model.addAttribute("acctList", allAccounts); return "cash-create-acct"; }
	 */
	
//	@GetMapping(value = "/accepttrans")
//    public ModelAndView acceptTransaction1(
//        @RequestParam(value = "transId", required = true) Integer transactionId,
//        @RequestParam(value = "acctId", required = true) Integer acctId,
//        @RequestParam(value = "transDate", required = true) String transDate,
//        @RequestParam(value = "desc", required = false) String description,
//        @RequestParam(value = "transAmt", required = false) String transAmt,
//        ModelMap model
//    ) {
//		CreditTransaction trans = new CreditTransaction();
//		trans.setAcctId(acctId);
//		trans.setTransDate(LocalDate.parse(transDate));
//		trans.setDescription(description);
//		trans.setTransAmt(new BigDecimal(transAmt));
//		creditTransactionDao.insert(trans);
//
//		uploadTransDao.delete(transactionId);
//
//		model.addAttribute("acctId", acctId);
//		return new ModelAndView("forward:/displayuploadedcardtransactions", model);
//    }
//	
	@GetMapping(value = "/accepttrans")
    public ModelAndView acceptTransaction(
        @RequestParam(value = "transId", required = true) String transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        ModelMap model
    ) {
		Map<String, Object> modelMap = uploadService.acceptTransaction(Integer.valueOf(transactionId), acctId);
		model.addAllAttributes(modelMap);
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

	@GetMapping(value = "/savetrans")
    public String editTransaction(
        @RequestParam(value = "transId", required = true) String transactionId,
        @RequestParam(value = "transDate", required = true) String transDate,
        @RequestParam(value = "desc", required = false) String description,
        @RequestParam(value = "transAmt", required = false) String transAmt,
        @RequestParam(value = "acctName", required = false) String acctName,
        Model model
    ) {
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		int acctId = accounts.get(0).getAcctId();

		CreditTransaction trans = new CreditTransaction();
		trans.setTransactionId(Integer.valueOf(transactionId));
		trans.setDescription(description);
		trans.setInDescription(description);
		trans.setTransDate(LocalDate.parse(transDate));
		trans.setTransAmt(new BigDecimal(transAmt));
		trans.setAcctId(acctId);
		model.addAttribute("trans", trans);
		return "cash-edit-card-trans";
    }

	@GetMapping(value = "/updatequote")
	public ModelAndView updatePrice(@ModelAttribute(value="quote") Quote quote, ModelMap model) {
		investmentService.updatePrice(quote);
//		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
//			String transAcctName = trans.getTransferAcct();
//			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
//			Account transferAcct = accounts.get(0);
//			trans.setTransferAcctId(transferAcct.getAcctId());
//		}
//		checkingTransactionDao.insert(trans);
//		uploadService.updateCheckingTransferTransaction(trans);
//		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

//
//	@GetMapping(value = "/updatequote")
//    public String editTransaction(
//        @RequestParam(value = "transId", required = true) String transactionId,
//        @RequestParam(value = "transDate", required = true) String transDate,
//        @RequestParam(value = "desc", required = false) String description,
//        @RequestParam(value = "transAmt", required = false) String transAmt,
//        @RequestParam(value = "acctName", required = false) String acctName,
//        Model model
//    ) {
//		List<Account> accounts = accountDao.findAccountsByName(acctName);
//		int acctId = accounts.get(0).getAcctId();
//
//		CreditTransaction trans = new CreditTransaction();
//		trans.setTransactionId(Integer.valueOf(transactionId));
//		trans.setDescription(description);
//		trans.setInDescription(description);
//		trans.setTransDate(LocalDate.parse(transDate));
//		trans.setTransAmt(new BigDecimal(transAmt));
//		trans.setAcctId(acctId);
//		model.addAttribute("trans", trans);
//		return "cash-edit-card-trans";
//    }

	@GetMapping(value = "/savechktrans1")
    public String editTransaction1(
        @RequestParam(value = "transId", required = true) String transactionId,
        @RequestParam(value = "transDate", required = true) String transDate,
        @RequestParam(value = "checkNo", required = true) String checkNo,
        @RequestParam(value = "desc", required = false) String description,
        @RequestParam(value = "transAmt", required = false) String transAmt,
        @RequestParam(value = "acctName", required = false) String acctName,
        Model model
    ) {
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		int acctId = accounts.get(0).getAcctId();

		CheckingTransaction trans = new CheckingTransaction();
		trans.setTransactionId(Integer.valueOf(transactionId));
		trans.setCheckNumber(Integer.valueOf(checkNo));
		trans.setDescription(description);
		trans.setInDescription(description);
		trans.setTransDate(LocalDate.parse(transDate));
		trans.setTransAmt(new BigDecimal(transAmt));
		trans.setAcctId(acctId);
		model.addAttribute("trans", trans);
		return "cash-edit-chk-trans";
    }

	@GetMapping(value = "/savechktrans")
    public String editTransaction(
        @RequestParam(value = "transId", required = true) int transactionId,
        @RequestParam(value = "acctId", required = true) int acctId,
        Model model
    ) {
		List<CheckingTransaction> chkTransactions = uploadChkTransDao.findTransactionsById(transactionId);
		if (!CollectionUtils.isEmpty(chkTransactions)) {
			CheckingTransaction trans = chkTransactions.get(0);
			model.addAttribute("trans", trans);
		} else {
			model.addAttribute("trans", new CheckingTransaction());
		}
		List<Account> allAccounts = accountDao.findAccounts();
	    model.addAttribute("accounts", allAccounts);
		return "cash-edit-chk-trans";
	}

	@GetMapping(value = "/savesavtrans")
    public String editSavTransaction(
        @RequestParam(value = "transId", required = true) int transactionId,
        @RequestParam(value = "acctId", required = true) int acctId,
        Model model
    ) {
		List<SavingTransaction> savTransactions = uploadSavTransDao.findTransactionsById(transactionId);
		if (!CollectionUtils.isEmpty(savTransactions)) {
			SavingTransaction trans = savTransactions.get(0);
			model.addAttribute("trans", trans);
		} else {
			model.addAttribute("trans", new SavingTransaction());
		}
		return "cash-edit-sav-trans";
	}

	@GetMapping(value = "/edittrans")
    public String editTransaction(
        @RequestParam(value = "transId", required = true) String transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        Model model
    ) {
		Map<String, Object> modelMap = uploadService.editTransaction(Integer.valueOf(transactionId), acctId);
		model.addAllAttributes(modelMap);
		return (String) modelMap.get("view");

//		List<Account> accounts = accountDao.findAccountsByName(acctName);
//		int acctId = accounts.get(0).getAcctId();
//
//		CreditTransaction trans = new CreditTransaction();
//		trans.setTransactionId(Integer.valueOf(transactionId));
//		trans.setDescription(description);
//		trans.setInDescription(description);
//		trans.setTransDate(LocalDate.parse(transDate));
//		trans.setTransAmt(new BigDecimal(transAmt));
//		trans.setAcctId(acctId);
//		model.addAttribute("trans", trans);
//		return "cash-edit-card-trans";
    }

	public static BigDecimal calculateTransAmt(TransactionType transType, String inTransAmt) {
		BigDecimal transAmt = new BigDecimal(inTransAmt).abs();
		if (transType == TransactionType.BUY ||
				transType == TransactionType.XOUT ||
						transType == TransactionType.WITHDRAW) {
			transAmt = transAmt.multiply(new BigDecimal(-1));
		}
		return transAmt;
	}
	
	public static BigDecimal calculateFees(TransactionType transType, BigDecimal transAmt, String quantity, String quote) {
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
	
	@GetMapping(value = "/saveinvtrans")
    public String editInvTransaction(
    	@RequestParam(value = "transType", required = true) String transactionType,
        @RequestParam(value = "transDate", required = true) String transDate,
        @RequestParam(value = "ticker", required = false) String ticker,
        @RequestParam(value = "desc", required = true) String description,
        @RequestParam(value = "quantity", required = false) String quantity,
        @RequestParam(value = "quote", required = false) String quote,
        @RequestParam(value = "fees", required = false) String inFees,
        @RequestParam(value = "transAmt", required = true) String inTransAmt,
        @RequestParam(value = "transferAcct", required = false) String transferAcct,
        @RequestParam(value = "acctName", required = false) String acctName,
        Model model
	) {
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		int acctId = accounts.get(0).getAcctId();
		TransactionType transType = TransactionType.fromDesc(transactionType);
		BigDecimal transAmt = calculateTransAmt(transType, inTransAmt);
		BigDecimal fees = calculateFees(transType, transAmt, quantity, quote);

		InvestmentTransaction trans = new InvestmentTransaction();

		trans.setTransactionType(transType);
		trans.setTransDate(LocalDate.parse(transDate));
		trans.setTicker(ticker);
		trans.setDescription(description);
		trans.setInDescription(description);
		if (StringUtils.isNotBlank(quantity)) {
			trans.setQuantity(new BigDecimal(quantity));
		}
		if (StringUtils.isNotBlank(quote)) {
			trans.setQuote(new BigDecimal(quote));
		}
		trans.setFees(fees);
		trans.setTransAmt(transAmt);

		if (StringUtils.isNotBlank(transferAcct)) {
			List<Account> transAccts = accountDao.findAccountsByName(transferAcct);
			int transferAcctId = transAccts.get(0).getAcctId();
			trans.setTransferAcctId(transferAcctId);
		}
		trans.setAcctId(acctId);
		model.addAttribute("trans", trans);
		return "cash-edit-inv-trans";
	}
		//		switch (transType) {
//			case ADD:
//			case DEPOSIT:
//			case DIVIDEND:
//			case ENDING_BALANCE:
//			case INTEREST:
//			case OTHER:
//			case OPENING_BALANCE:
//			case REINVEST:
//			case REMOVE:
//			case SELL:
//			case XIN:
//				break;
//			case BUY:
//			case XOUT:
//			case WITHDRAW: {
//				transAmt = transAmt.multiply(new BigDecimal(-1));
//				
//			}
//			break;
//
//		default:
//			break;
//		}
	
	@GetMapping(value = "/deletetrans")
    public ModelAndView deleteTransaction(
        @RequestParam(value = "transId", required = true) Integer transactionId,
        @RequestParam(value = "acctId", required = false) Integer acctId,
        ModelMap model
    ) {
		Map<String, Object> modelMap = uploadService.deleteTransaction(transactionId, acctId);
		model.addAllAttributes(modelMap);
		return new ModelAndView((String) modelMap.get("forward"), model);
    }

	@GetMapping(value = "/deletecardtrans")
    public ModelAndView deleteCardTransaction(
        @RequestParam(value = "transId", required = true) Integer transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        ModelMap model
    ) {
		creditTransactionDao.delete(transactionId);
		model.addAttribute("acctId", acctId);
		return new ModelAndView("forward:/displayuploadedcardtransactions", model);
    }
	
	@GetMapping(value = "/deletecheckingtrans")
    public ModelAndView deleteCheckingTransaction(
        @RequestParam(value = "transId", required = true) Integer transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        ModelMap model
    ) {
		checkingTransactionDao.delete(transactionId);
		model.addAttribute("acctId", acctId);
		return new ModelAndView("forward:/displayuploadedtransactions", model);
    }

	@GetMapping(value = "/deletesavingtrans")
    public ModelAndView deleteSavingTransaction(
        @RequestParam(value = "transId", required = true) Integer transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        ModelMap model
    ) {
		savingTransactionDao.delete(transactionId);
		model.addAttribute("acctId", acctId);
		return new ModelAndView("forward:/displayuploadedtransactions", model);
    }
	
	@GetMapping(value = "/deleteinvtrans")
    public ModelAndView deleteInvTransaction(
        @RequestParam(value = "transId", required = true) Integer transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        ModelMap model
    ) {
		investmentTransactionDao.delete(transactionId);
		model.addAttribute("acctId", acctId);
		return new ModelAndView("forward:/displayuploadedinvtransactions", model);
    }

	@GetMapping(value = "/deleteloantrans")
    public ModelAndView deleteLoanTransaction(
        @RequestParam(value = "transId", required = true) Integer transactionId,
        @RequestParam(value = "acctId", required = true) Integer acctId,
        ModelMap model
    ) {
		loanTransactionDao.delete(transactionId);
		model.addAttribute("acctId", acctId);
		return new ModelAndView("forward:/displayuploadedtransactions", model);
    }
	
	@GetMapping(value = "/updatecardtrans", params = "action=save")
	public ModelAndView updateCardTrans(@ModelAttribute(value="trans") CreditTransaction trans, Boolean decision, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		creditTransactionDao.insert(trans);
		if (BooleanUtils.isTrue(decision)) {
			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());
		}
		uploadTransDao.delete(trans.getTransactionId());
		if (BooleanUtils.isTrue(decision)) {
			uploadTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
		}
		uploadService.updateCreditTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedcardtransactions", model);
	}

	@GetMapping(value = "/updatechktrans", params = "action=save")
	public ModelAndView updateCheckingTrans(@ModelAttribute(value="trans") CheckingTransaction trans, Boolean decision, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		checkingTransactionDao.insert(trans);
		if (BooleanUtils.isTrue(decision)) {
			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());			
		}
		uploadChkTransDao.delete(trans.getTransactionId());
		if (BooleanUtils.isTrue(decision)) {
			uploadChkTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
		}
		uploadService.updateCheckingTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}
	
	@GetMapping(value = "/updatesavtrans", params = "action=save")
	public ModelAndView updateSavingTrans(@ModelAttribute(value="trans") SavingTransaction trans, Boolean decision, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		savingTransactionDao.insert(trans);
		if (BooleanUtils.isTrue(decision)) {
			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());			
		}
		uploadSavTransDao.delete(trans.getTransactionId());
		if (BooleanUtils.isTrue(decision)) {
			uploadSavTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
		}
		uploadService.updateSavingTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

	@GetMapping(value = "/addsavtrans", params = "action=saveold")
	public ModelAndView addaSavingTrans(@ModelAttribute(value="trans") SavingTransaction trans, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		savingTransactionDao.insert(trans);
//		if (BooleanUtils.isTrue(decision)) {
//			payeeMapDao.updatePayeeMap(trans.getAcctId(), trans.getInDescription(), trans.getDescription());			
//		}
//		uploadSavTransDao.delete(trans.getTransactionId());
//		if (BooleanUtils.isTrue(decision)) {
//			uploadSavTransDao.updatePayeeMap(trans.getInDescription(), trans.getDescription());
//		}
		uploadService.updateSavingTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}
	
	@GetMapping(value = "/addsavtrans", params = "action=save")
	public ModelAndView addSavingTrans(@ModelAttribute(value="trans") SavingTransaction trans, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		savingTransactionDao.insert(trans);
		uploadService.updateSavingTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}
	
	@GetMapping(value = "/addcardtrans", params = "action=save")
	public ModelAndView addCardTrans(@ModelAttribute(value="trans") CreditTransaction trans, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		creditTransactionDao.insert(trans);
		uploadService.updateCreditTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

	@GetMapping(value = "/addchktrans", params = "action=save")
	public ModelAndView addCheckingTrans(@ModelAttribute(value="trans") CheckingTransaction trans, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		checkingTransactionDao.insert(trans);
		uploadService.updateCheckingTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

	@GetMapping(value = "/addloantrans", params = "action=save")
	public ModelAndView addLoanTrans(@ModelAttribute(value="trans") LoanTransaction trans, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}
		loanTransactionDao.insert(trans);
		uploadService.updateLoanTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

	@GetMapping(value = "/updateinvtrans", params = "action=save")
	public ModelAndView updateInvTrans(@ModelAttribute(value="trans") InvestmentTransaction trans, ModelMap model) {
		if (StringUtils.isNotBlank(trans.getTransferAcct()) && trans.getTransferAcctId() == 0) {
			String transAcctName = trans.getTransferAcct();
			List<Account> accounts = accountDao.findAccountsByName(transAcctName);
			Account transferAcct = accounts.get(0);
			trans.setTransferAcctId(transferAcct.getAcctId());
		}

		cleanInvTransaction(trans);
		investmentTransactionDao.insert(trans);
		investmentService.updatePrice(trans);
		uploadService.updateInvestmentTransferTransaction(trans);
		model.addAttribute("acctId", trans.getAcctId());
		return new ModelAndView("forward:/displayuploadedinvtransactions", model);
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
	
	@GetMapping(value="/updatecardtrans", params = "action=cancel")
	public ModelAndView cancelUpdateCardTrans(@ModelAttribute(value="trans") CreditTransaction trans) {
		return new ModelAndView("redirect:/allaccounts");
	}

	@GetMapping(value = "/nothing")
    public void doNothing() {
//        model.addAttribute("id", description);
//        List<Integer> userIds = asList(1,2,3,4);
//        model.addAttribute("userIds", userIds);
        return;
    }
	
	@GetMapping("/displaycreateinvtransaction")
	public String showNewSecurityPage(Model model) {
		InvestmentTransaction transaction = new InvestmentTransaction();
	    model.addAttribute("transaction", transaction);
	    return "cash-display-create-invtransaction";
	}

	@PostMapping(value = "/getuploadedtransactions", params = "action=download")
	public ModelAndView downloadAccount(@ModelAttribute(value = "acctName") String acctName, ModelMap model) {
		Map<String, Object> modelMap = uploadService.downloadAccount(acctName);
		model.addAllAttributes(modelMap);
		return new ModelAndView("redirect:/displayuploadedtransactions");
	}

	@GetMapping ("/updatepositions")
	public String updatePositions(Model model) {
		investmentService.updatePositions();		
		List<Security> allSecurities = securityDao.findSecurities();
	    model.addAttribute("securities", allSecurities);
	    return "cash-security-list";
	}

	@PostMapping(value="/getuploadedtransactions", params = "action=refreshquotes")
	public ModelAndView updateQuotes(@ModelAttribute(value="acctName") String acctName, ModelMap model) { // Model model) {
		investmentService.updateQuotes();
		System.out.println("Here");
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		model.addAttribute("acctId", acct.getAcctId());
		model.addAttribute("acctName", acctName);
		return new ModelAndView("redirect:/displayuploadedtransactions", model);
//		return new ModelAndView("forward:/displayuploadedtransactions", model);
	}

//	@GetMapping (value = "/getpositions")
//	public String getPositions(
//        @RequestParam(value = "acctId", required = true) Integer acctId,
//        ModelMap model) {
//		investmentService.getPositions(acctId);
//		List<Security> allSecurities = securityDao.findSecurities();
//	    model.addAttribute("securities", allSecurities);
//	    return "cash-security-list";
//	}
//	
	@PostMapping(value="/getuploadedtransactions", params = "action=balance")
	public String getPositions(@ModelAttribute(value="acctName") String acctName, ModelMap model) {
		AccountBal bal = investmentService.getPositions(acctName);
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
	    model.addAttribute("accounts", allAccounts);
		model.addAttribute("selectedAcctName", acctName);
		List<AccountBal> ballist = new ArrayList<>();
		ballist.add(bal);
//		model.addAttribute("bal", bal);
		model.addAttribute("ballist", ballist);
		
		
//		Workbook workbook = new XSSFWorkbook();
//		 
//		Sheet sheet = workbook.createSheet("Persons");
//		sheet.setColumnWidth(0, 6000);
//		sheet.setColumnWidth(1, 4000);
//		 
//		Row header = sheet.createRow(0);
//		 
//		CellStyle headerStyle = workbook.createCellStyle();
//		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
//		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//		 
//		XSSFFont font = ((XSSFWorkbook) workbook).createFont();
//		font.setFontName("Arial");
//		font.setFontHeightInPoints((short) 16);
//		font.setBold(true);
//		headerStyle.setFont(font);
//		 
//		Cell headerCell = header.createCell(0);
//		headerCell.setCellValue("Name");
//		headerCell.setCellStyle(headerStyle);
//		
//		headerCell = header.createCell(1);
//		headerCell.setCellValue("Age");
//		headerCell.setCellStyle(headerStyle);
//		
//		CellStyle style = workbook.createCellStyle();
//		style.setWrapText(true);
//		 
//		Row row = sheet.createRow(2);
//		Cell cell = row.createCell(0);
//		cell.setCellValue("John Smith");
//		cell.setCellStyle(style);
//		 
//		cell = row.createCell(1);
//		cell.setCellValue(20);
//		cell.setCellStyle(style);
//		
//		File currDir = new File(".");
//		String path = currDir.getAbsolutePath();
//		String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";
//		 
//		FileOutputStream outputStream;
//		try {
//			outputStream = new FileOutputStream(fileLocation);
//			workbook.write(outputStream);
//			workbook.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return "cash-inv-portfolio";
	}

	@PostMapping(value="/getuploadedtransactions", params = "action=allinvbalance")
	public String getPositions(ModelMap model) {
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
	    model.addAttribute("accounts", allAccounts);
		List<AccountBal> ballist = new ArrayList<>();
		
		BigDecimal acctVal = BigDecimal.ZERO;
		BigDecimal acctPosValChange = BigDecimal.ZERO;

		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.INVESTMENT.getCode());
		List<Account> invAccounts = accountDao.findAccountsByType(acctTypes);
		for (Account invAcct : invAccounts) {
		    AccountBal bal = investmentService.getPositions(invAcct.getAcctName());
		    acctVal = acctVal.add(bal.getAccountValue());
		    acctPosValChange = acctPosValChange.add(bal.getChangeInPositionsValue());
			ballist.add(bal);
		}
//		model.addAttribute("selectedAcctName", acctName);		
		model.addAttribute("ballist", ballist);
		model.addAttribute("netval", acctVal);
		model.addAttribute("netvalchange", acctPosValChange);
		return "cash-inv-portfolio";
	}

	@GetMapping ("/netbalance")
	public String getNetBalance(Model model) {
		Map<String, Object> modelMap = uploadService.getNetBalance();
//		uploadService.doNetBalance();
		model.addAllAttributes(modelMap);
		Util.startDateReport();
	    return "cash-portfolio";
	}

	@GetMapping ("/exportdailyreport")
	public String exportBalanceReport(HttpServletResponse response, Model model) throws IOException {
		Map<String, Object> modelMap = uploadService.getNetBalance();
		model.addAllAttributes(modelMap);
		uploadService.doNetBalance(response);
	    return "cash-portfolio";
	}

//	@PostMapping(value="/getuploadedtransactions", params = "action=allbalance")
//	public String getNetBalance(@ModelAttribute(value="acctName") String acctName, ModelMap model) {
//		AccountBal bal = investmentService.getPositions(acctName);
//
//		List<Account> allAccounts = accountDao.findAccounts();
//		Util.updateParent(allAccounts);
//	    model.addAttribute("accounts", allAccounts);
//		model.addAttribute("selectedAcctName", acctName);
//		model.addAttribute("bal", bal);
//				
//		return "cash-inv-portfolio";
//	}

}
