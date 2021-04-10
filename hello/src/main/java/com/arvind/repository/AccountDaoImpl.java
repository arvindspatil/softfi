package com.arvind.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.stereotype.Repository;

import com.arvind.model.Account;
import com.arvind.util.AccountType;
import com.arvind.util.Status;

@Repository
public class AccountDaoImpl extends JdbcDaoSupport implements AccountDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private AllAccountsQuery allAccountsQry;
	private AccountsByTypeQuery accountsByTypeQry;
	private AccountsByNameQuery accountsByNameQry;
	private AccountsByIdQuery accountsByIdQry;
	private Insert insertQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        allAccountsQry = new AllAccountsQuery(dataSource);
        accountsByTypeQry = new AccountsByTypeQuery(dataSource);
        accountsByNameQry = new AccountsByNameQuery(dataSource);
        accountsByIdQry = new AccountsByIdQuery(dataSource);
        insertQry = new Insert(dataSource);
    }
	
	@Override
	public List<Account> findAccounts() {
		return allAccountsQry.execute(); // ByNamedParam(params);
	}

	@Override
	public List<Account> findAccountsByType(List<Integer> acctTypes) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctTypes", acctTypes);
		return accountsByTypeQry.executeByNamedParam(params);
	}

	@Override
	public List<Account> findAccountsByName(String acctName) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctName", acctName);
		return accountsByNameQry.executeByNamedParam(params);
	}

	@Override
	public List<Account> findAccountsById(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return accountsByIdQry.executeByNamedParam(params);
	}

	@Override
	public void insertAccount(Account acct) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctName", acct.getAcctName());
		params.put("acctType", acct.getAcctType().getCode());
		params.put("acctStatus", acct.getStatus().getCode());
		params.put("parentAcctId", acct.getParentAcctId());
//		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
//		insertQry.setReturnGeneratedKeys(true);
//		insertQry.updateByNamedParam(params, generatedKeyHolder);
		insertQry.updateByNamedParam(params);
//		acct.setAcctId(generatedKeyHolder.getKey().intValue());
	}

	private class AllAccountsQuery extends MappingSqlQuery<Account> {
		public AllAccountsQuery(DataSource ds) {
			super(ds, "SELECT a.acct_id, a.acct_name, a.acct_type, a.acct_status, a.parent_acct_id, " + 
					"b.acct_name as parent_acct_name FROM acct a " + 
					"inner join acct b on b.acct_id = a.parent_acct_id " + 
					"order by a.parent_acct_id, a.acct_id");
			compile();
		}

		@Override
		protected Account mapRow(ResultSet rs, int rowNum) throws SQLException {
			Account acct = new Account();
			acct.setAcctId(rs.getInt("acct_id"));
			acct.setAcctName(StringUtils.trimToEmpty(rs.getString("acct_name")));
			acct.setAcctType(AccountType.fromCode(rs.getInt("acct_type")));
			acct.setStatus(Status.fromCode(rs.getInt("acct_status")));
			acct.setParentAcctId(rs.getInt("parent_acct_id"));
			acct.setParentAcctName(StringUtils.trimToEmpty(rs.getString("parent_acct_name")));
			return acct;
		}
	}

	private class AccountsByTypeQuery extends MappingSqlQuery<Account> {
		public AccountsByTypeQuery(DataSource ds) {
			super(ds, "SELECT a.acct_id, a.acct_name, a.acct_type, a.acct_status, a.parent_acct_id, " + 
					"b.acct_name as parent_acct_name FROM acct a " + 
					"inner join acct b on b.acct_id = a.parent_acct_id " + 
					"where a.acct_type in (:acctTypes) " +
					"order by a.parent_acct_id, a.acct_id");
			declareParameter(new SqlParameter("acctTypes", Types.INTEGER));
			compile();
		}

		@Override
		protected Account mapRow(ResultSet rs, int rowNum) throws SQLException {
			Account acct = new Account();
			acct.setAcctId(rs.getInt("acct_id"));
			acct.setAcctName(StringUtils.trimToEmpty(rs.getString("acct_name")));
			acct.setAcctType(AccountType.fromCode(rs.getInt("acct_type")));
			acct.setStatus(Status.fromCode(rs.getInt("acct_status")));
			acct.setParentAcctId(rs.getInt("parent_acct_id"));
			acct.setParentAcctName(StringUtils.trimToEmpty(rs.getString("parent_acct_name")));
			return acct;
		}
	}

	private class AccountsByNameQuery extends MappingSqlQuery<Account> {
		public AccountsByNameQuery(DataSource ds) {
			super(ds, "SELECT a.acct_id, a.acct_name, a.acct_type, a.acct_status, a.parent_acct_id, " + 
					"b.acct_name as parent_acct_name FROM acct a " + 
					"inner join acct b on b.acct_id = a.parent_acct_id " + 
					"where a.acct_name in (:acctName) " +
					"order by a.parent_acct_id, a.acct_id");
			declareParameter(new SqlParameter("acctName", Types.CHAR));
			compile();
		}

		@Override
		protected Account mapRow(ResultSet rs, int rowNum) throws SQLException {
			Account acct = new Account();
			acct.setAcctId(rs.getInt("acct_id"));
			acct.setAcctName(StringUtils.trimToEmpty(rs.getString("acct_name")));
			acct.setAcctType(AccountType.fromCode(rs.getInt("acct_type")));
			acct.setStatus(Status.fromCode(rs.getInt("acct_status")));
			acct.setParentAcctId(rs.getInt("parent_acct_id"));
			acct.setParentAcctName(StringUtils.trimToEmpty(rs.getString("parent_acct_name")));
			return acct;
		}
	}

	private class AccountsByIdQuery extends MappingSqlQuery<Account> {
		public AccountsByIdQuery(DataSource ds) {
			super(ds, "SELECT a.acct_id, a.acct_name, a.acct_type, a.acct_status, a.parent_acct_id, " + 
					"b.acct_name as parent_acct_name FROM acct a " + 
					"inner join acct b on b.acct_id = a.parent_acct_id " + 
					"where a.acct_id in (:acctId) " +
					"order by a.parent_acct_id, a.acct_id");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected Account mapRow(ResultSet rs, int rowNum) throws SQLException {
			Account acct = new Account();
			acct.setAcctId(rs.getInt("acct_id"));
			acct.setAcctName(StringUtils.trimToEmpty(rs.getString("acct_name")));
			acct.setAcctType(AccountType.fromCode(rs.getInt("acct_type")));
			acct.setStatus(Status.fromCode(rs.getInt("acct_status")));
			acct.setParentAcctId(rs.getInt("parent_acct_id"));
			acct.setParentAcctName(StringUtils.trimToEmpty(rs.getString("parent_acct_name")));
			return acct;
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into acct (acct_name, acct_type, acct_status, parent_acct_id) "
				+ "values (:acctName, :acctType, :acctStatus, :parentAcctId)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctName", Types.VARCHAR));
			declareParameter(new SqlParameter("acctType", Types.INTEGER));
			declareParameter(new SqlParameter("acctStatus", Types.INTEGER));
			declareParameter(new SqlParameter("parentAcctId", Types.INTEGER));
		}
	}

}
