package com.arvind.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.arvind.util.Util;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1/")
public class RestAccountController {

	@Autowired
	AccountDao accountDao;

	@GetMapping ("/allaccounts")
	public List<Account> handleAllAccounts(Model model) {
		List<Account> allAccounts = accountDao.findAccounts();
		Util.updateParent(allAccounts);
		return allAccounts;
	}

	@GetMapping ("/chart-data")
	public ChartData getChartData(Model model) {
		
		ChartData chartData = new ChartData();
		
		ChartLabels labels = new ChartLabels();
		List<String> labelList = new ArrayList<>();
		labelList.add("Sunday");
		labelList.add("Monday");
		labelList.add("Tuesday");
		labelList.add("Wednesday");
		labelList.add("Thursday");
		labelList.add("Friday");
		labelList.add("Saturday");
		labels.setLabels(labelList);
		
		chartData.setLabels(labelList);
		
		List<ChartDataset> datasets = new ArrayList<>();
		
		ChartDataset bankingDataset = new ChartDataset();
		List<Integer> datalist = new ArrayList<Integer>(
				Arrays.asList(15339, 21345, 18483, 24003, 23489, 24092, 18000));
		bankingDataset.setData(datalist);
		bankingDataset.setLabel("Banking");
		bankingDataset.setLineTension(0);
		bankingDataset.setBackgroundColor("transparent");
		bankingDataset.setBorderColor("blue");
		bankingDataset.setPointBackgroundColor("blue");
		bankingDataset.setBorderWidth(4);
		datasets.add(bankingDataset);

		ChartDataset cardDataset = new ChartDataset();
		List<Integer> cardDatalist = new ArrayList<Integer>(
				Arrays.asList(10339, 11345, 13483, 20003, 22489, 21092, 19000));
		cardDataset.setData(cardDatalist);
		cardDataset.setLabel("Credit Card");
		cardDataset.setLineTension(0);
		cardDataset.setBackgroundColor("transparent");
		cardDataset.setBorderColor("red");
		cardDataset.setPointBackgroundColor("red");
		cardDataset.setBorderWidth(4);
		datasets.add(cardDataset);
		
		chartData.setDatasets(datasets);

		return chartData;
		
	}

}
