package com.arvind.hello;

import java.util.List;

public interface ActorDao {
	public List<Book> findEmployeeById(int empId);
	public List<Book> findEmployeeByTitle(String bkTitle);
}
