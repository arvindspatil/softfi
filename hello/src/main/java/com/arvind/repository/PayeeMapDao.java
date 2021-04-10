package com.arvind.repository;

import java.util.List;
import java.util.Map;

public interface PayeeMapDao {
	public Map<String, String> getPayeeMap(int acctId);
	public Map<String, List<String>> getReversePayeeMap(int acctId);
	public void insert(int acctId, String inDesc, String stdDesc);
	public void update(int acctId, String inDesc, String stdDesc);
	public void delete(int acctId, String desc);
	public void updatePayeeMap(int acctId, String inDesc, String stdDesc);
}
