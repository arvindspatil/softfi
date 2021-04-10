package com.arvind.hello;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public class ActorDaoImpl extends JdbcDaoSupport implements ActorDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private CustomerMappingQuery customerQry;
	private CustomerBkMappingQuery customerBkQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        customerQry = new CustomerMappingQuery(dataSource);
        customerBkQry = new CustomerBkMappingQuery(dataSource);
    }
	
//	@Override
//	protected void initDao() throws Exception {
//		customerQry = new CustomerMappingQuery(dataSource);	
//	}
//
	@Override
	public List<Book> findEmployeeById(int empId) {
		Map params = new HashMap<>();
		params.put("id", 10);
		List<Book> rc = customerQry.executeByNamedParam(params);
		return rc;
//        Employee emp = null;
//        String query = "select * from employee where emp_id=?"; 
//        Object[] inputs = new Object[] {empId};
        // getJdbcTemplate().execu
//        JdbcTemplate jdbcTemplate;
//        jdbcTemplate.c
//        MappingSqlQuery<String> qry;
        // qry.execut
//        emp = (Employee) getJdbcTemplate().queryForObject(query, inputs, 
//                                    new BeanPropertyRowMapper(Employee.class));
//        return emp;
    }
	
	@Override
	public List<Book> findEmployeeByTitle(String bkTitle) {
		Map params = new HashMap<>();
		params.put("lastname", bkTitle);
		List<Book> rc = customerBkQry.executeByNamedParam(params);
		return rc;
    }
	
	private class CustomerMappingQuery extends MappingSqlQuery<Book> {
		public CustomerMappingQuery(DataSource ds) {
			super(ds, "SELECT actor_id, first_name, last_name FROM actor where actor_id <= :id");
			declareParameter(new SqlParameter("id", Types.INTEGER));
			compile();
		}

		@Override
		protected Book mapRow(ResultSet rs, int rowNum) throws SQLException {
			Book book = new Book();
			book.setId(rs.getInt("actor_id"));
			book.setAuthor(rs.getString("first_name"));
			book.setTitle(rs.getString("last_name"));
			return book;
		}
//
//
//	    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
//	        Customer cust = new Customer();
//	        cust.setId((Integer) rs.getObject("id"));
//	        cust.setName(rs.getString("name"));
//	        return cust;
//	    } 
	}

	private class CustomerBkMappingQuery extends MappingSqlQuery<Book> {
		public CustomerBkMappingQuery(DataSource ds) {
			super(ds, "SELECT actor_id, first_name, last_name FROM actor where last_name = :lastname");
			declareParameter(new SqlParameter("lastname", Types.VARCHAR));
			compile();
		}

		@Override
		protected Book mapRow(ResultSet rs, int rowNum) throws SQLException {
			Book book = new Book();
			book.setId(rs.getInt("actor_id"));
			book.setAuthor(rs.getString("first_name"));
			book.setTitle(rs.getString("last_name"));
			return book;
		}
	}

}
