package com.arvind.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arvind.model.Account;
import com.arvind.model.json.ChartData;
import com.arvind.model.json.ChartDataset;
import com.arvind.model.json.ChartLabels;
import com.arvind.repository.AccountDao;
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
	UploadService uploadService;

	@GetMapping ("/allaccounts")
	public List<Account> handleAllAccounts(Model model) {
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
		return allAccounts;
	}

	@GetMapping ("/chart-data")
	public ChartData getChartData(Model model) {
		
		ChartData chartData = new ChartData();
		
		List<String> labelList = Util.getChartKeys();
		
		chartData.setLabels(labelList);
		
		List<ChartDataset> datasets = new ArrayList<>();

		Map<AccountType, Object> histMap = uploadService.getHistoricalBalance();
		List<BigDecimal> checkingData = Util.filterByType(AccountType.CHECKING, histMap);
		
		ChartDataset bankingDataset = new ChartDataset();
		bankingDataset.setData(checkingData);
		bankingDataset.setLabel(AccountType.CHECKING.name());
		bankingDataset.setLineTension(0);
		bankingDataset.setBackgroundColor("transparent");
		bankingDataset.setBorderColor("blue");
		bankingDataset.setPointBackgroundColor("blue");
		bankingDataset.setBorderWidth(4);
		datasets.add(bankingDataset);

		ChartDataset cardDataset = new ChartDataset();
		List<BigDecimal> cardData = Util.filterByType(AccountType.CREDIT, histMap);
		cardDataset.setData(cardData);
		cardDataset.setLabel(AccountType.CREDIT.name());
		cardDataset.setLineTension(0);
		cardDataset.setBackgroundColor("transparent");
		cardDataset.setBorderColor("red");
		cardDataset.setPointBackgroundColor("red");
		cardDataset.setBorderWidth(4);
		datasets.add(cardDataset);

		ChartDataset savDataset = new ChartDataset();
		List<BigDecimal> savData = Util.filterByType(AccountType.SAVINGS, histMap);
		savDataset.setData(savData);
		savDataset.setLabel(AccountType.SAVINGS.name());
		savDataset.setLineTension(0);
		savDataset.setBackgroundColor("transparent");
		savDataset.setBorderColor("yellow");
		savDataset.setPointBackgroundColor("yellow");
		savDataset.setBorderWidth(4);
		datasets.add(savDataset);

		ChartDataset loanDataset = new ChartDataset();
		List<BigDecimal> loanData = Util.filterByType(AccountType.MORTGAGE, histMap);
		loanDataset.setData(loanData);
		loanDataset.setLabel(AccountType.MORTGAGE.name());
		loanDataset.setLineTension(0);
		loanDataset.setBackgroundColor("transparent");
		loanDataset.setBorderColor("green");
		loanDataset.setPointBackgroundColor("green");
		loanDataset.setBorderWidth(4);
		datasets.add(loanDataset);

		ChartDataset invDataset = new ChartDataset();
		List<BigDecimal> invData = Util.filterByType(AccountType.INVESTMENT, histMap);
		invDataset.setData(invData);
		invDataset.setLabel(AccountType.INVESTMENT.name());
		invDataset.setLineTension(0);
		invDataset.setBackgroundColor("transparent");
		invDataset.setBorderColor("orange");
		invDataset.setPointBackgroundColor("orange");
		invDataset.setBorderWidth(4);
		datasets.add(invDataset);

		ChartDataset netDataset = new ChartDataset();
		List<BigDecimal> netData = Util.filterByType(AccountType.NET, histMap);
		netDataset.setData(netData);
		netDataset.setLabel(AccountType.NET.name());
		netDataset.setLineTension(0);
		netDataset.setBackgroundColor("transparent");
		netDataset.setBorderColor("indigo");
		netDataset.setPointBackgroundColor("indigo");
		netDataset.setBorderWidth(4);
		datasets.add(netDataset);

		chartData.setDatasets(datasets);

		return chartData;
		
	}

}
