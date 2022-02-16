package com.arvind.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:alpaca.properties")
public class AlpacaConfig {
 
	@Autowired
	private Environment env;
	
	public String alpacaKey() {
		return env.getProperty("key_id");
	}
	
	public String alpacaSecret() {
		return env.getProperty("secret_key");
	}	
}