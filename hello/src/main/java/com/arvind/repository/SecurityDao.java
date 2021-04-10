package com.arvind.repository;

import java.util.List;
import java.util.Map;

import com.arvind.model.Security;

public interface SecurityDao {
	public List<Security> findSecurities();
	public void insertSecurity(Security sec);
	public Map<String, Security> getSecurities();
	public Security getSecurityByTicker(String ticker);
}
