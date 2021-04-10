package com.arvind.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.arvind.model.Security;
import com.arvind.repository.SecurityDao;
import com.arvind.util.SecurityType;

@Controller
public class SecuritiesController {

	@Autowired
	SecurityDao securityDao;

	private static final Logger log = LoggerFactory.getLogger(SecuritiesController.class);

	@GetMapping ("/listsecurities")
	public String getAllSecurities(Model model) {
		List<Security> allSecurities = securityDao.findSecurities();
		LocalDate today = LocalDate.now();
		String formatStr = "dd.MM.yyyy";
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatStr);
		System.out.println(dtf.format(today));
		System.out.println(dtf.format(today.atStartOfDay()));
		System.out.println(dtf.format(today.minusDays(13)));
		System.out.println(dtf.format(today.plusDays(1)));
//		LocalDateTime fromDate = today.atStartOfDay();
		
	    model.addAttribute("securities", allSecurities);
	    return "cash-security-list";
	}

	@PostMapping(value="/createsecurity", params = "action=save")
	public ModelAndView createSecurity(@ModelAttribute(value="security") Security sec) {
		securityDao.insertSecurity(sec);
		return new ModelAndView("redirect:/listsecurities");
	}

	@PostMapping(value="/createsecurity", params = "action=cancel")
	public ModelAndView cancelCreateSecurity(@ModelAttribute(value="security") Security sec) {
		return new ModelAndView("redirect:/listsecurities");
	}

	@GetMapping("/newsecurity")
	public String showNewSecurityPage(Model model) {
		Security security = new Security();
	    model.addAttribute("security", security);
	    model.addAttribute("securityTypeList", SecurityType.values());
	    return "cash-create-security";
	}

}
