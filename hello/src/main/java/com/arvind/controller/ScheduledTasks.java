package com.arvind.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.arvind.service.InvestmentService;

@Component
public class ScheduledTasks {
 
	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

//	@Scheduled(fixedRate = 10)
//	@Scheduled(cron = "0 21 16 * * *") // "0/15 * * * * *")
	public void reportCurrentTime() {
		log.info("The time is now {}", dateFormat.format(new Date()));
	}

}