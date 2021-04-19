package com.arvind.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import com.arvind.model.Account;
import com.arvind.model.CheckingTransaction;
import com.arvind.model.CreditTransaction;
import com.arvind.model.InvestmentTransaction;
import com.arvind.model.LoanTransaction;
import com.arvind.model.Quote;
import com.arvind.model.SavingTransaction;

public class Util {
	public static void updateParent(List<Account> allAccounts) {
		Map<Integer, Account> acctIdMap = new HashMap<Integer, Account>();
		for (Account acct : allAccounts) {
			acctIdMap.put(acct.getAcctId(), acct);
		}
		
		for (Account acct : allAccounts) {
			String fullName = getFullName(acct, acctIdMap);
			acct.setParentAcctName(fullName);
		}
	}
	
	public static List<Quote> filterByDates(List<Quote> quotes, LocalDate startDate) {
		TreeMap<LocalDate, Quote> dateQuotes = new TreeMap<>();
		TreeMap<LocalDate, Quote> lastOfMonthQuotes = new TreeMap<>();
		TreeMap<LocalDate, Quote> firstOfMonthQuotes = new TreeMap<>();
		TreeMap<LocalDate, Quote> filteredQuotes = new TreeMap<>();
		
		LocalDate todayDt = LocalDate.now();
		LocalDate firstOfMonth = todayDt.withDayOfMonth(1);
		for (LocalDate currentDt : dateQuotes.keySet()) {
			if (currentDt.compareTo(firstOfMonth) <= 0) continue;
			filteredQuotes.put(currentDt, dateQuotes.get(currentDt));
		}
		
		for (Quote quote : quotes) {
			dateQuotes.put(quote.getQuoteDate(), quote);
		}

		for (LocalDate lastDt : dateQuotes.keySet()) {
			if (lastDt.compareTo(startDate) < 0) continue;
			LocalDate lastDateOfMth = lastDt.withDayOfMonth(lastDt.lengthOfMonth());
			lastOfMonthQuotes.put(lastDateOfMth, dateQuotes.get(lastDt));
		}

		for (LocalDate firstDt : dateQuotes.descendingKeySet()) {
			if (firstDt.compareTo(startDate) < 0) continue;
			LocalDate firstDateOfMth = firstDt.withDayOfMonth(1);
			firstOfMonthQuotes.put(firstDateOfMth, dateQuotes.get(firstDt));
		}

		filteredQuotes.putAll(lastOfMonthQuotes); //.values());
		filteredQuotes.putAll(firstOfMonthQuotes); // .values());
		
//		for (Quote quote : filteredQuotes) {
//			System.out.println("Date/Ticker/Value: "
//				+ quote.getQuoteDate() + "/"
//				+ quote.getTicker() + "/"
//				+ quote.getPricePs());
//		}
		return new ArrayList(filteredQuotes.values());		
	}

	public static List<BigDecimal> filterByType(AccountType acctType, Map<AccountType, Object> data) {
		List<BigDecimal> acctBals = new ArrayList<>();
		TreeMap<LocalDate, BigDecimal> acctData = (TreeMap<LocalDate, BigDecimal>) data.get(acctType);
		// TreeMap<LocalDate, BigDecimal> acctData = acctData1.
		int limit = 12;
		int idx = 0;
		for (LocalDate keyDt : acctData.descendingKeySet()) {
//			System.out.println(keyDt + "/" + acctData.get(keyDt));
			acctBals.add(acctData.get(keyDt));
			idx++;
			if (idx > limit) break;
		}
		Collections.reverse(acctBals);
		return acctBals;
	}

	public static List<String> getChartKeys() {
		List<String> keys = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/YY");
		LocalDate todayDt = LocalDate.now();
		keys.add(todayDt.format(formatter));		
		for (int idx = 0; idx < 12; idx++) {
			LocalDate dt = todayDt.withDayOfMonth(1).minusMonths(idx);			
			keys.add(dt.format(formatter));
		}
		Collections.reverse(keys);
		return keys;
	}
	
	public static String getFullName(Account acct, Map<Integer, Account> acctIdAccount) {
		String fullName = StringUtils.EMPTY;
		if (!StringUtils.equals("Root", acct.getAcctName())) {
			fullName = getFullName(acctIdAccount.get(acct.getParentAcctId()), acctIdAccount) + "/" 
					+ acct.getAcctName();
		}
		return fullName;
	}
	
	public static void updateBalanceHistory(TreeMap<LocalDate, BigDecimal> netHist, TreeMap<LocalDate, BigDecimal> acctHist) {
		for (Map.Entry<LocalDate, BigDecimal> entry : acctHist.entrySet()) {
			if (netHist.containsKey(entry.getKey())) {
				netHist.put(entry.getKey(), netHist.get(entry.getKey()).add(entry.getValue()));
			} else {
				netHist.put(entry.getKey(), entry.getValue());
			}
//			System.out.println(entry.getKey() + "/" + entry.getValue());
		}
	}

	public static TreeMap<LocalDate, BigDecimal> updateNetBalanceHistory(
			TreeMap<LocalDate, BigDecimal> checkingBalHist,
			TreeMap<LocalDate, BigDecimal> savingsBalHist, 
			TreeMap<LocalDate, BigDecimal> creditBalHist, 
			TreeMap<LocalDate, BigDecimal> loanBalHist, 
			TreeMap<LocalDate, BigDecimal> invBalHist) {
		TreeMap<LocalDate, BigDecimal> netBalHist = new TreeMap<>();
		for (LocalDate idxDt : checkingBalHist.keySet()) {
			BigDecimal netBal = BigDecimal.ZERO;
			netBal = netBal.add(checkingBalHist.get(idxDt))
					.add(savingsBalHist.containsKey(idxDt) ? savingsBalHist.get(idxDt) : BigDecimal.ZERO)
					.add(creditBalHist.containsKey(idxDt) ? creditBalHist.get(idxDt) : BigDecimal.ZERO)
					.add(loanBalHist.containsKey(idxDt) ? loanBalHist.get(idxDt) : BigDecimal.ZERO)
					.add(invBalHist.containsKey(idxDt) ? invBalHist.get(idxDt) : BigDecimal.ZERO);
			netBalHist.put(idxDt, netBal);					
		}
		return netBalHist;
	}
	
	public static void updateMissingBalanceHistory(TreeMap<LocalDate, BigDecimal> netHist) {
		LocalDate currentDt = LocalDate.now().withDayOfMonth(1).plusMonths(1);
		LocalDate idxDt = netHist.firstKey();
		while (idxDt.compareTo(currentDt) <= 0) {
			if (!netHist.containsKey(idxDt)) {
				LocalDate prevDt = idxDt.minusMonths(1);
				netHist.put(idxDt, netHist.get(prevDt));
			}
			idxDt = idxDt.plusMonths(1);
		}
		for (Map.Entry<LocalDate, BigDecimal> entry : netHist.entrySet()) {
//			if (netHist.containsKey(entry.getKey())) {
//				netHist.put(entry.getKey(), netHist.get(entry.getKey()).add(entry.getValue()));
//			} else {
//				netHist.put(entry.getKey(), entry.getValue());
//			}
//			System.out.println(entry.getKey() + "/" + entry.getValue());
		}
	}

//	public static void updateInvBalHist(TreeMap<LocalDate, BigDecimal> netHist, TreeMap<LocalDate, BigDecimal> acctHist) {
//		updateCreditBalHist(netHist, acctHist);
//	}
//
//	public static void updateCheckingBalHist(TreeMap<LocalDate, BigDecimal> netHist, TreeMap<LocalDate, BigDecimal> acctHist) {
//		updateCreditBalHist(netHist, acctHist);
//	}
//
//	public static void updateSavingBalHist(TreeMap<LocalDate, BigDecimal> netHist, TreeMap<LocalDate, BigDecimal> acctHist) {
//		updateCreditBalHist(netHist, acctHist);
//	}
//
//	public static void updateLoanBalHist(TreeMap<LocalDate, BigDecimal> netHist, TreeMap<LocalDate, BigDecimal> acctHist) {
//		updateCreditBalHist(netHist, acctHist);
//	}

	public static LocalDate toLocalDate(Timestamp ts) {
		if (ts == null) {
			return null;
		}
		return ts.toLocalDateTime().toLocalDate();
	}

	public static LocalDate startDateReport() {
		LocalDate todayDt = LocalDate.now();
		LocalDate startDt = todayDt.withDayOfMonth(1).minusMonths(12);
//		System.out.println("Start Date = " + startDt);
//		System.out.println("Start Month = " + todayDt.getMonth());
		return startDt;
	}
	
	public static Timestamp toTimestamp(LocalDate dt) {
		if (dt == null) {
			return null;
		}
		return Timestamp.valueOf(dt.atStartOfDay());
	}

	public static String trimQuotes(String str) {
		return StringUtils.trimToEmpty(StringUtils.substringBetween(StringUtils.trimToEmpty(str), "\""));
	}

	public static String trimCommas(String str) {
	    String regex = "(?<=[\\d])(,)(?=[\\d])";
	    Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(str);
	    str = m.replaceAll("");
	    return str;
	}
	
	public static void updateSavingBalance(List<SavingTransaction> transactions) {
		BigDecimal balance = new BigDecimal(0);
		for (SavingTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
		}
		Collections.reverse(transactions);
	}

	public static TreeMap<LocalDate, BigDecimal> updateSavingBalanceByMonth(List<SavingTransaction> transactions) {
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		BigDecimal balance = new BigDecimal(0);
		for (SavingTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
			balanceMap.put(trans.getTransDate().plusMonths(1).withDayOfMonth(1), trans.getBalanceAmt());
		}
		Collections.reverse(transactions);
		return balanceMap;
	}
	
	public static void updateCheckingBalance(List<CheckingTransaction> transactions) {
		BigDecimal balance = new BigDecimal(0);
		for (CheckingTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
		}
		Collections.reverse(transactions);
	}

	public static void updateCreditBalance(List<CreditTransaction> transactions) {
		BigDecimal balance = new BigDecimal(0);
		for (CreditTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
		}
		Collections.reverse(transactions);
	}

	public static TreeMap<LocalDate, BigDecimal> updateCreditBalanceByMonth(List<CreditTransaction> transactions) {
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		BigDecimal balance = new BigDecimal(0);
		for (CreditTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
			balanceMap.put(trans.getTransDate().plusMonths(1).withDayOfMonth(1), trans.getBalanceAmt());
		}
		Collections.reverse(transactions);
		return balanceMap;
	}

	public static TreeMap<LocalDate, BigDecimal> updateCheckingBalanceByMonth(List<CheckingTransaction> transactions) {
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		BigDecimal balance = new BigDecimal(0);
		for (CheckingTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
			balanceMap.put(trans.getTransDate().plusMonths(1).withDayOfMonth(1), trans.getBalanceAmt());
		}
		Collections.reverse(transactions);
		return balanceMap;
	}
	
	public static HashMap<String, BigDecimal> updateInvestmentBalance(List<InvestmentTransaction> transactions) {
		HashMap<String, BigDecimal> shareBal = new HashMap<>();
		BigDecimal balance = new BigDecimal(0);
		for (InvestmentTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
			
			if (StringUtils.isNotBlank(trans.getTicker())) {
				BigDecimal transQuantity = calculateQuantity(trans);
				if (shareBal.containsKey(trans.getTicker())) {
					transQuantity = shareBal.get(trans.getTicker()).add(transQuantity);
				}
				shareBal.put(trans.getTicker(), transQuantity);
				trans.setBalanceQty(transQuantity);
			}
		}
		shareBal.put("Cash", balance);
		Collections.reverse(transactions);
		return shareBal;
	}

	public static void updateLoanBalance(List<LoanTransaction> transactions) {
		BigDecimal balance = new BigDecimal(0);
		for (LoanTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
		}
		Collections.reverse(transactions);
	}
	
	public static TreeMap<LocalDate, BigDecimal> updateLoanBalanceByMonth(List<LoanTransaction> transactions) {
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		BigDecimal balance = new BigDecimal(0);
		for (LoanTransaction trans : transactions) {
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
			balanceMap.put(trans.getTransDate().plusMonths(1).withDayOfMonth(1), trans.getBalanceAmt());
		}
		Collections.reverse(transactions);
		return balanceMap;
	}

	public static BigDecimal calculateQuantity(InvestmentTransaction trans) {
		TransactionType transType = trans.getTransactionType();
		if (trans.getQuantity() == null || trans.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		
		BigDecimal transQuantity = trans.getQuantity().abs();
		if (isIntegerValue(transQuantity)) {
			transQuantity = BigDecimal.valueOf(transQuantity.intValue());
		} else {
			transQuantity = transQuantity.stripTrailingZeros();
		}
		
		if (transType == TransactionType.REMOVE ||
			transType == TransactionType.SELL) {
			transQuantity = transQuantity.multiply(new BigDecimal(-1));
		}
		return transQuantity;
	}
	
	public static boolean isIntegerValue(BigDecimal bd) {
		return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
	}

	public static HashMap<String, Pair<Quote, BigDecimal>> getLatestQuotes(List<Quote> recentQuotes) { // , HashMap<String, BigDecimal> changeInPrice, List<Quote> latestQuotes) {
		HashMap<String, Pair<Quote, BigDecimal>> latestQuotes = new HashMap<>();
		if (CollectionUtils.isEmpty(recentQuotes)) {
			return latestQuotes;
		}
		
		Iterator<Quote> iter = recentQuotes.iterator();
		while (iter.hasNext()) {
			Quote quote = iter.next();
			if (latestQuotes.containsKey(quote.getTicker())) {
				Pair<Quote, BigDecimal> pair = latestQuotes.get(quote.getTicker());
				Quote recentQuote = pair.getKey();
				BigDecimal changeInPrice = pair.getValue();
				if (changeInPrice.compareTo(recentQuote.getPricePs()) == 0) {
					changeInPrice = changeInPrice.subtract(quote.getPricePs());
					Pair<Quote, BigDecimal> updatedPair = Pair.of(recentQuote, changeInPrice);
					latestQuotes.put(quote.getTicker(), updatedPair);
				}
			} else {
				Pair<Quote, BigDecimal> pair = Pair.of(quote, quote.getPricePs());
				latestQuotes.put(quote.getTicker(), pair);
			}
		}
		return latestQuotes;
	}
	
	public static TreeMap<LocalDate, BigDecimal> updateInvBalanceByMonth(List<InvestmentTransaction> transactions, TreeMap<LocalDate, TreeMap<String, BigDecimal>> shareBalanceMap) {
		
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		
		HashMap<String, BigDecimal> shareBal = new HashMap<>();
		BigDecimal balance = new BigDecimal(0);
		for (InvestmentTransaction trans : transactions) {
			LocalDate keyDt = trans.getTransDate().plusMonths(1).withDayOfMonth(1);
			balance = balance.add(trans.getTransAmt());
			trans.setBalanceAmt(balance);
			balanceMap.put(keyDt, trans.getBalanceAmt());
			if (!shareBalanceMap.containsKey(keyDt)) {
				TreeMap<String, BigDecimal> shareBalMap = new TreeMap<>();
				shareBalMap.putAll(shareBal);
				shareBalMap.values().removeAll(Collections.singletonList(new BigDecimal(0)));
				shareBalanceMap.put(keyDt, shareBalMap);
			}
			
			TreeMap<String, BigDecimal> tickerMap = shareBalanceMap.get(keyDt);
			if (StringUtils.isNotBlank(trans.getTicker())) {
				BigDecimal transQuantity = Util.calculateQuantity(trans);
				if (shareBal.containsKey(trans.getTicker())) {
					transQuantity = shareBal.get(trans.getTicker()).add(transQuantity);
				}
				shareBal.put(trans.getTicker(), transQuantity);
				tickerMap.put(trans.getTicker(), transQuantity);
				trans.setBalanceQty(transQuantity);
			}
		}
		shareBal.put("Cash", balance);
		Collections.reverse(transactions);

		LocalDate currentDt = LocalDate.now().withDayOfMonth(1).plusMonths(1);
		LocalDate idxDt = balanceMap.firstKey();
		while (idxDt.compareTo(currentDt) <= 0) {
			LocalDate prevDt = idxDt.minusMonths(1);
			if (!balanceMap.containsKey(idxDt)) {
				balanceMap.put(idxDt, balanceMap.get(prevDt));
			}

			if (!shareBalanceMap.containsKey(idxDt)) {
				TreeMap<String, BigDecimal> prevTickerMap = shareBalanceMap.get(prevDt);
				TreeMap<String, BigDecimal> tickerMap = new TreeMap<>();
				tickerMap.putAll(prevTickerMap);
				shareBalanceMap.put(idxDt, tickerMap);
			}
			
			idxDt = idxDt.plusMonths(1);
		}
		
		return balanceMap;
	}

}
