package com.arvind.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.arvind.hello.ItemDeserializer;
import com.arvind.hello.QuoteListDeserializer;
import com.arvind.model.Account;
import com.arvind.model.AccountBal;
import com.arvind.model.AccountPosition;
import com.arvind.model.CheckingTransaction;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.Quote;
import com.arvind.model.SavingTransaction;
import com.arvind.model.Security;
import com.arvind.repository.AccountDao;
import com.arvind.repository.AccountPositionDao;
import com.arvind.repository.CheckingTransactionDao;
import com.arvind.repository.CreditTransactionDao;
import com.arvind.repository.InvestmentTransactionDao;
import com.arvind.repository.PayeeMapDao;
import com.arvind.repository.QuoteDao;
import com.arvind.repository.SavingTransactionDao;
import com.arvind.repository.SecurityDao;
import com.arvind.repository.UploadCheckingTransDao;
import com.arvind.repository.UploadCreditTransDao;
import com.arvind.repository.UploadInvestmentTransDao;
import com.arvind.repository.UploadSavingTransDao;
import com.arvind.util.AccountType;
import com.arvind.util.SecurityType;
import com.arvind.util.TransactionType;
import com.arvind.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;


@Service
public class InvestmentServiceImpl implements InvestmentService {

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
	UploadCreditTransDao uploadTransDao;
	
	@Autowired
	UploadCheckingTransDao uploadChkTransDao;

	@Autowired
	UploadSavingTransDao uploadSavTransDao;

	@Autowired
	UploadInvestmentTransDao uploadInvTransDao;

	@Autowired
	AccountPositionDao acctPositionDao;

	@Autowired
	SecurityDao securityDao;

	@Autowired
	QuoteDao quoteDao;

	private static final Logger log = LoggerFactory.getLogger(InvestmentServiceImpl.class);

	public Set<String> getTickersUsedSince(LocalDate startDt) {
		Set<String> tickers = new HashSet<>();
		List<InvestmentTransaction> transactions = investmentTransactionDao.findTransactionsSinceDate(startDt);
		for (InvestmentTransaction trans : transactions) {
			if (StringUtils.isNotBlank(trans.getTicker())) {
				tickers.add(trans.getTicker());
			}
		}
		return tickers;
	}
	
	@Override
	public Map<String, Object> updatePositions() {
		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.INVESTMENT.getCode());
		List<Account> invAccounts = accountDao.findAccountsByType(acctTypes);
		
		for (Account acct : invAccounts) {
			int acctId = acct.getAcctId();
			
			List<InvestmentTransaction> transactions = investmentTransactionDao.findTransactionsByAcctId(acct.getAcctId());
			HashMap<String, BigDecimal> balMap = Util.updateInvestmentBalance(transactions);

			List<AccountPosition> positions = acctPositionDao.findPositionsByAcctId(acctId);
			HashMap<String, AccountPosition> dbPositions = new HashMap<>();
			for (AccountPosition position : positions) {
				dbPositions.put(position.getTicker(), position);
			}
			
			List<AccountPosition> updatePositions = new ArrayList<>();
			List<AccountPosition> addPositions = new ArrayList<>();
			for (String ticker : balMap.keySet()) {
				if (dbPositions.containsKey(ticker)) {
					if (dbPositions.get(ticker).getQuantity().compareTo(balMap.get(ticker)) != 0) {
						AccountPosition updPosition = dbPositions.get(ticker);
						updPosition.setQuantity(balMap.get(ticker));
						updatePositions.add(updPosition);
					}
				} else {
					AccountPosition addPosition = new AccountPosition();
					addPosition.setAcctId(acctId);
					addPosition.setTicker(ticker);
					addPosition.setQuantity(balMap.get(ticker));
					addPositions.add(addPosition);
				}
			}
			for (AccountPosition pstn : updatePositions) {
				acctPositionDao.update(pstn);
				log.info("U Acct " + pstn.getAcctId() + " ticker " + pstn.getTicker() + " qty " + pstn.getQuantity());
			}
			for (AccountPosition pstn : addPositions) {
				acctPositionDao.insert(pstn);
				log.info("A Acct " + pstn.getAcctId() + " ticker " + pstn.getTicker() + " qty " + pstn.getQuantity());
			}

		}

		return null;
	}

	@Override
	@Scheduled(cron = "0 30 16 * * *")
	public Map<String, Object> updateQuotes() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		log.info("The time is now {}", dateFormat.format(new Date()));
		
		updatePositions();
		
		HashMap<String, HashMap<LocalDateTime, Quote>> currentQuotes = new HashMap<>();
		List<Quote> dbQuotes = quoteDao.findQuotes();
		
		for (Quote dbQuote : dbQuotes) {
			String ticker = dbQuote.getTicker();
			if (!currentQuotes.containsKey(ticker)) {
				currentQuotes.put(ticker, new HashMap<>());
			}
			currentQuotes.get(ticker).put(dbQuote.getQuoteDate().atStartOfDay(), dbQuote);
		}
		
		List<AccountPosition> positions = acctPositionDao.findPositions();
		LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(13);
		Set<String> tickers = getTickersUsedSince(startDate);
		for (AccountPosition pstn : positions) {
			if (pstn.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
				tickers.add(pstn.getTicker());
			}
		}

		List<Security> securities = securityDao.findSecurities();
		HashMap<String, Security> secMap = new HashMap<>();
		for (Security sec : securities) {
			if (sec.getSecurityType() == SecurityType.MUTUAL_FUND ||
				sec.getSecurityType() == SecurityType.STOCK) {
				secMap.put(sec.getTicker(), sec);
			}
		}

//		tickers.clear();
//		tickers.add("MTD");
//		
		List<Quote> quotes = new ArrayList<>();
		for (String ticker : tickers) {			
			List<Quote> tickerQuotes = new ArrayList<>();
//			if (!StringUtils.equals(ticker, "VHYAX")) {
//				continue;
//			}
			if (secMap.containsKey(ticker)) {
				ObjectMapper mapper = new ObjectMapper();
				SimpleModule module = new SimpleModule();
//				module.addDeserializer(Quote.class, new ItemDeserializer());
				module.addDeserializer(List.class, new QuoteListDeserializer());
				mapper.registerModule(module);
				try {
					
					TimeUnit.SECONDS.sleep(30);
/*					String urlStr = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=";
					urlStr += ticker;
					urlStr += "&apikey=LVOYR1B8IC22JABA";
					log.info(urlStr);
					Quote testQ = mapper.readValue(new URL(urlStr), Quote.class); 
					log.info(testQ.toString());
					quotes.add(testQ);
*/
					String urlStr = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
					urlStr += ticker;
					urlStr += "&outputsize=full";
					urlStr += "&apikey=LVOYR1B8IC22JABA";
					log.info(urlStr);
					List<Quote> testQ = mapper.readValue(new URL(urlStr), List.class); 
					log.info("Ticker " + ticker + " has " + testQ.size() + " quotes");
					tickerQuotes.addAll(testQ);
//						} catch (IOException  e) {
				} catch (IOException | InterruptedException e) {
					log.info("Got Exception");
					e.printStackTrace();
				}
			}
			tickerQuotes = Util.filterByDates(tickerQuotes, startDate);
			quotes.addAll(tickerQuotes);
		}

		List<Quote> addQuotes = new ArrayList<>();
		List<Quote> updQuotes = new ArrayList<>();
		for (Quote currentQuote : quotes) {
			if (currentQuotes.containsKey(currentQuote.getTicker())) {
				HashMap<LocalDateTime, Quote> tickerQuotes = currentQuotes.get(currentQuote.getTicker());
				if (tickerQuotes.containsKey(currentQuote.getQuoteDate().atStartOfDay())) {
					Quote dbQuote = tickerQuotes.get(currentQuote.getQuoteDate().atStartOfDay());
					if (dbQuote.getPricePs().compareTo(currentQuote.getPricePs()) == 0) {
						continue;
					} else if (dbQuote.getPricePs().compareTo(currentQuote.getPricePs()) != 0) {
						updQuotes.add(currentQuote);
						continue;
					}
				}
			}
			addQuotes.add(currentQuote);
		}

		List<Quote> newAddQuotes = new ArrayList<>();
		HashMap<LocalDate, HashMap<String, Quote>> outMap = new HashMap<>();
		for (Quote addQ : addQuotes) {
			if (outMap.containsKey(addQ.getQuoteDate())) {
				HashMap<String, Quote> tickerMap = outMap.get(addQ.getQuoteDate());
				if (!tickerMap.containsKey(addQ.getTicker())) {
					newAddQuotes.add(addQ);
				}
				tickerMap.put(addQ.getTicker(), addQ);
			} else {
				HashMap<String, Quote> tickerMap = new HashMap<>();
				tickerMap.put(addQ.getTicker(), addQ);
				outMap.put(addQ.getQuoteDate(), tickerMap);
				newAddQuotes.add(addQ);
			}
		}
		
		BufferedWriter bw = null;
		try {
			File quotefile = new File("C:/tmp/data.txt");
			FileWriter fw = new FileWriter(quotefile);
			bw = new BufferedWriter(fw);
			Path filePath = Paths.get("C:/tmp", "data.txt");

			for (Quote quote : newAddQuotes) {
				bw.write("Add: " + quote.toString());
//				quoteDao.insert(quote);
			}
			
			for (Quote quote : updQuotes) {
				bw.write("Upd: " + quote.toString());
				//				quoteDao.update(quote);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (Quote quote : newAddQuotes) {
			quote.toString();
			quoteDao.insert(quote);
		}
		
		for (Quote quote : updQuotes) {
			quoteDao.update(quote);
		}
		return null;
	}

	@Override
//	@Scheduled(cron = "0 15 14 * * *")
	public Map<String, Object> updateQuoteList() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		log.info("The time is now {}", dateFormat.format(new Date()));
		
		updatePositions();
		
		HashMap<String, HashMap<LocalDate, Quote>> currentQuotes = new HashMap<>();
		List<Quote> dbQuotes = quoteDao.findQuotes();
		for (Quote dbQuote : dbQuotes) {
			String ticker = dbQuote.getTicker();
			if (!currentQuotes.containsKey(ticker)) {
				currentQuotes.put(ticker, new HashMap<>());
			}
			currentQuotes.get(ticker).put(dbQuote.getQuoteDate(), dbQuote);
		}
		
		List<AccountPosition> positions = acctPositionDao.findPositions();
		Set<String> tickers = new HashSet<>();
		for (AccountPosition pstn : positions) {
			if (pstn.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
				tickers.add(pstn.getTicker());
			}
		}
		
		List<Security> securities = securityDao.findSecurities();
		HashMap<String, Security> secMap = new HashMap<>();
		for (Security sec : securities) {
			if (sec.getSecurityType() == SecurityType.MUTUAL_FUND ||
				sec.getSecurityType() == SecurityType.STOCK) {
				secMap.put(sec.getTicker(), sec);
			}
		}

		List<Quote> quotes = new ArrayList<>();
		for (String ticker : tickers) {			
			if (secMap.containsKey(ticker)) {
				ObjectMapper mapper = new ObjectMapper();
				SimpleModule module = new SimpleModule();
//				module.addDeserializer(Quote.class, new ItemDeserializer());
				module.addDeserializer(List.class, new QuoteListDeserializer());
				mapper.registerModule(module);
				try {
					TimeUnit.SECONDS.sleep(30);
//					String urlStr = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=";
//					urlStr += ticker;
//					urlStr += "&apikey=LVOYR1B8IC22JABA";
//					log.info(urlStr);
//					Quote testQ = mapper.readValue(new URL(urlStr), Quote.class); 
//					log.info(testQ.toString());
//					quotes.add(testQ);

					String urlStr = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
					urlStr += ticker;
					urlStr += "&apikey=LVOYR1B8IC22JABA";
					log.info(urlStr);
					List<Quote> testQ = mapper.readValue(new URL(urlStr), List.class); 
					log.info("Ticker " + ticker + " has " + testQ.size() + " quotes");
					quotes.addAll(testQ);
////					quotes.add(testQ);
				} catch (IOException | InterruptedException e) {
					log.info("Got Exception");
					e.printStackTrace();
				}
//				
//				break;
			}
		}
		
		List<Quote> addQuotes = new ArrayList<>();
		List<Quote> updQuotes = new ArrayList<>();
		for (Quote currentQuote : quotes) {
			if (currentQuotes.containsKey(currentQuote.getTicker())) {
				HashMap<LocalDate, Quote> tickerQuotes = currentQuotes.get(currentQuote.getTicker());
				if (tickerQuotes.containsKey(currentQuote.getQuoteDate())) {
					Quote dbQuote = tickerQuotes.get(currentQuote.getQuoteDate());
					if (dbQuote.getPricePs().compareTo(currentQuote.getPricePs()) == 0) {
//						log.info("Skipping update for " + dbQuote.getTicker() + " for date " + dbQuote.getQuoteDate());
						continue;
					} else if (dbQuote.getPricePs().compareTo(currentQuote.getPricePs()) != 0) {
						updQuotes.add(currentQuote);
						continue;
					}
				}
			}
			addQuotes.add(currentQuote);
			log.info("Adding quote for " + currentQuote.getTicker() + " for date " + currentQuote.getQuoteDate());
		}
		
		for (Quote quote : addQuotes) {
			quoteDao.insert(quote);
		}
		
		for (Quote quote : updQuotes) {
			quoteDao.update(quote);
		}
		return null;
	}

	@Override
	public void updatePrice(InvestmentTransaction trans) {
		if (trans.getQuote() == null) {
			return;
		}
		Quote quote = new Quote();
		quote.setTicker(trans.getTicker());
		quote.setQuoteDate(trans.getTransDate());
		quote.setPricePs(trans.getQuote());
		updatePrice(quote);
		
//		if (StringUtils.isBlank(trans.getTicker())) {
//			return;
//		}
//		
//		List<Quote> dbQuotes = quoteDao.findQuoteByTickerDate(trans.getTicker(), trans.getTransDate());
//		if (CollectionUtils.isEmpty(dbQuotes)) {
//			Quote quote = new Quote();
//			quote.setTicker(trans.getTicker());
//			quote.setQuoteDate(trans.getTransDate());
//			quote.setPricePs(trans.getQuote());
//			quoteDao.insert(quote);
//		}
	}

	@Override
	public void updatePrice(Quote quote) {
		if (StringUtils.isBlank(quote.getTicker())) {
			return;
		}
		
		List<Quote> dbQuotes = quoteDao.findQuoteByTickerDate(quote.getTicker(), quote.getQuoteDate());
		if (CollectionUtils.isEmpty(dbQuotes)) {
			quoteDao.insert(quote);
		}
	}

	public AccountBal getPositionsNew(String acctName) {
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		int acctId = acct.getAcctId();

		AccountBal balance = new AccountBal();

		balance.setAcctId(acctId);
		balance.setAcctName(acctName);
		balance.setAcctType(acct.getAcctType());
		balance.setParentAcctId(acct.getParentAcctId());
		balance.setParentAcctName(acct.getParentAcctName());

		List<AccountPosition> acctPositions = new ArrayList<>();
		List<AccountPosition> positions = acctPositionDao.findPositionsByAcctId(acctId);
		if (CollectionUtils.isEmpty(positions)) {
			return null;
		}
		
		List<Quote> quotes = quoteDao.findLatestQuotes();
		HashMap<String, Quote> quoteMap = new HashMap<>();
		for (Quote quote : quotes) {
			quoteMap.put(quote.getTicker(), quote);
		}
		
		Map<String, Security> securities = securityDao.getSecurities();
		
		BigDecimal acctVal = BigDecimal.ZERO;
		for (AccountPosition position : positions) {
			if (StringUtils.equals(position.getTicker(), "Cash")) {
				BigDecimal balAmt = position.getQuantity().setScale(2, BigDecimal.ROUND_HALF_UP);
				balance.setBalanceAmt(balAmt);
				acctVal = acctVal.add(position.getQuantity());
			} else {
				if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}
				
				BigDecimal currentQuote = quoteMap.containsKey(position.getTicker()) ? 
						quoteMap.get(position.getTicker()).getPricePs() : BigDecimal.ONE;

				BigDecimal currentQty = position.getQuantity();

				BigDecimal positionVal = currentQuote.multiply(currentQty);
				acctVal = acctVal.add(positionVal);
				
				currentQuote = currentQuote.setScale(2, BigDecimal.ROUND_HALF_UP);
				position.setCurrentQuote(currentQuote);

				currentQty = currentQty.setScale(3, BigDecimal.ROUND_HALF_UP);
				position.setQuantity(currentQty);

				positionVal = positionVal.setScale(2, BigDecimal.ROUND_HALF_UP);				
				position.setCurrentValue(positionVal);	
				
				position.setDescription(securities.get(position.getTicker()).getDescription());
				acctPositions.add(position);
			}
		}
		balance.setPositions(acctPositions);
		balance.setAccountValue(acctVal.setScale(2, BigDecimal.ROUND_HALF_UP));
		return balance;
	}

	@Override
	public AccountBal getPositions(String acctName) {
		List<Account> accounts = accountDao.findAccountsByName(acctName);
		Account acct = accounts.get(0);
		int acctId = acct.getAcctId();

		AccountBal balance = new AccountBal();

		balance.setAcctId(acctId);
		balance.setAcctName(acctName);
		balance.setAcctType(acct.getAcctType());
		balance.setParentAcctId(acct.getParentAcctId());
		balance.setParentAcctName(acct.getParentAcctName());

		List<AccountPosition> acctPositions = new ArrayList<>();
		List<AccountPosition> positions = acctPositionDao.findPositionsByAcctId(acctId);
		if (CollectionUtils.isEmpty(positions)) {
			return null;
		}

		List<Quote> quotes1 = quoteDao.findRecentQuotes();
		HashMap<String, Pair<Quote, BigDecimal>> latestQuotes = Util.getLatestQuotes(quotes1);
		
		List<Quote> quotes = quoteDao.findLatestQuotes();
		HashMap<String, Quote> quoteMap1 = new HashMap<>();
		for (Quote quote : quotes) {
			quoteMap1.put(quote.getTicker(), quote);
		}
		
		Map<String, Security> securities = securityDao.getSecurities();
		
		BigDecimal acctVal = BigDecimal.ZERO;
		BigDecimal acctPosValChange = BigDecimal.ZERO;
		
		for (AccountPosition position : positions) {
			if (StringUtils.equals(position.getTicker(), "Cash")) {
				BigDecimal balAmt = position.getQuantity().setScale(2, BigDecimal.ROUND_HALF_UP);
				balance.setBalanceAmt(balAmt);
				acctVal = acctVal.add(position.getQuantity());
			} else {
				if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}

				BigDecimal currentQuote = BigDecimal.ONE;
				BigDecimal changeInPrice = BigDecimal.ZERO;

				if (latestQuotes.containsKey(position.getTicker())) {
					currentQuote = latestQuotes.get(position.getTicker()).getKey().getPricePs();
					changeInPrice = latestQuotes.get(position.getTicker()).getValue();
				}
//				BigDecimal currentQuote = quoteMap.containsKey(position.getTicker()) ? 
//						quoteMap.get(position.getTicker()).getPricePs() : BigDecimal.ONE;

				BigDecimal currentQty = position.getQuantity();

				BigDecimal positionVal = currentQuote.multiply(currentQty);
				acctVal = acctVal.add(positionVal);

				BigDecimal changeInPositionVal = changeInPrice.multiply(currentQty);
				acctPosValChange = acctPosValChange.add(changeInPositionVal);

				changeInPrice = changeInPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
				position.setChangeInPrice(changeInPrice);

				changeInPositionVal = changeInPositionVal.setScale(2, BigDecimal.ROUND_HALF_UP);
				position.setChangeInValue(changeInPositionVal);

				// currentQuote = currentQuote.setScale(2, BigDecimal.ROUND_HALF_UP);
				currentQuote = currentQuote.setScale(2, RoundingMode.HALF_UP);
				position.setCurrentQuote(currentQuote);

				currentQty = currentQty.setScale(3, BigDecimal.ROUND_HALF_UP);
				position.setQuantity(currentQty);

				positionVal = positionVal.setScale(2, BigDecimal.ROUND_HALF_UP);				
				position.setCurrentValue(positionVal);	
				
				position.setDescription(securities.containsKey(position.getTicker()) ? securities.get(position.getTicker()).getDescription() : position.getTicker());
				acctPositions.add(position);
			}
		}
		balance.setPositions(acctPositions);
		balance.setAccountValue(acctVal.setScale(2, BigDecimal.ROUND_HALF_UP));
		balance.setChangeInPositionsValue(acctPosValChange.setScale(2, BigDecimal.ROUND_HALF_UP));
		return balance;
	}

//	public TreeMap<LocalDate, BigDecimal> getAccountBalance1(AccountBal bal, int acctId) {
//		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
//		List<CheckingTransaction> transactions = findTransactionsByAcctId(acctId);
//		if (CollectionUtils.isEmpty(transactions)) {
//			bal.setBalanceAmt(BigDecimal.ZERO);
//		} else {
//			balanceMap = Util.updateCheckingBalanceByMonth(transactions);
//			for (Map.Entry<LocalDate, BigDecimal> entry : balanceMap.entrySet()) {
//				System.out.println(entry.getKey() + "/" + entry.getValue());
//			}
//			bal.setBalanceAmt(transactions.get(0).getBalanceAmt());			
//		}
//		bal.setAccountValue(bal.getBalanceAmt());
//		return balanceMap;
//	}

//	public static HashMap<String, BigDecimal> updateInvestmentBalance(List<InvestmentTransaction> transactions) {
//		HashMap<String, BigDecimal> shareBal = new HashMap<>();
//		BigDecimal balance = new BigDecimal(0);
//		for (InvestmentTransaction trans : transactions) {
//			balance = balance.add(trans.getTransAmt());
//			trans.setBalanceAmt(balance);
//			
//			if (StringUtils.isNotBlank(trans.getTicker())) {
//				BigDecimal transQuantity = calculateQuantity(trans);
//				if (shareBal.containsKey(trans.getTicker())) {
//					transQuantity = shareBal.get(trans.getTicker()).add(transQuantity);
//				}
//				shareBal.put(trans.getTicker(), transQuantity);
//				trans.setBalanceQty(transQuantity);
//			}
//		}
//		shareBal.put("Cash", balance);
//		Collections.reverse(transactions);
//		return shareBal;
//	}


}
