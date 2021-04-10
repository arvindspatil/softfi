package com.arvind.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arvind.model.Account;
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
	
}
