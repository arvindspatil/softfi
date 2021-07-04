package com.arvind.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.stereotype.Repository;

import com.arvind.model.Account;
import com.arvind.util.AccountType;
import com.arvind.util.Status;
import com.arvind.util.Util;

@Repository
public class AccountDaoImpl extends JdbcDaoSupport implements AccountDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private AllAccountsQuery allAccountsQry;
	private AccountsByTypeQuery accountsByTypeQry;
	private AccountsByNameQuery accountsByNameQry;
	private AccountsByIdQuery accountsByIdQry;
	private AccountsReconDatesQuery accountsReconDatesQry;
	private GetReconDateQuery getReconDateQry;
	private Insert insertQry;
	private InsertAcctRecon insertAcctReconQry;
	private UpdateAcctRecon updateAcctReconQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        allAccountsQry = new AllAccountsQuery(dataSource);
        accountsByTypeQry = new AccountsByTypeQuery(dataSource);
        accountsByNameQry = new AccountsByNameQuery(dataSource);
        accountsByIdQry = new AccountsByIdQuery(dataSource);
        accountsReconDatesQry = new AccountsReconDatesQuery(dataSource);
        getReconDateQry = new GetReconDateQuery(dataSource);
        insertQry = new Insert(dataSource);
        insertAcctReconQry = new InsertAcctRecon(dataSource);
        updateAcctReconQry = new UpdateAcctRecon(dataSource);
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
	public List<Pair<Account, LocalDate>> findReconDateByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return getReconDateQry.executeByNamedParam(params);
	}

	@Override
	public void insertAccount(Account acct) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctName", acct.getAcctName());
		params.put("acctType", acct.getAcctType().getCode());
		params.put("acctStatus", acct.getStatus().getCode());
		params.put("parentAcctId", acct.getParentAcctId());
		insertQry.updateByNamedParam(params);
	}

	@Override
	public void insertAccountRecon(int acctId, LocalDate reconDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", acctId);
		params.put("reconDate", reconDate);
		insertAcctReconQry.updateByNamedParam(params);
	}

	@Override
	public void updateAccountRecon(int acctId, LocalDate reconDate) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", acctId);
		params.put("reconDate", reconDate);
		updateAcctReconQry.updateByNamedParam(params);
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

	private class AccountsReconDatesQuery extends MappingSqlQuery<Pair<Account, LocalDate>> {
		public AccountsReconDatesQuery(DataSource ds) {
			super(ds, "SELECT a.acct_id, a.acct_name, a.acct_type, a.acct_status, " + 
					"b.recon_date FROM acct a " + 
					"left outer join acct_recon b on b.acct_id = a.acct_id " + 
					"where a.acct_type in (:acctTypes) " +
					"order by b.recon_date, a.acct_type");
			declareParameter(new SqlParameter("acctTypes", Types.INTEGER));
			compile();
		}

		@Override
		protected Pair<Account, LocalDate> mapRow(ResultSet rs, int rowNum) throws SQLException {
			Account acct = new Account();
			
			LocalDate reconDate = Util.toLocalDate(rs.getTimestamp("recon_date"));
			acct.setAcctId(rs.getInt("acct_id"));
			acct.setAcctName(StringUtils.trimToEmpty(rs.getString("acct_name")));
			acct.setAcctType(AccountType.fromCode(rs.getInt("acct_type")));
			acct.setStatus(Status.fromCode(rs.getInt("acct_status")));
			return Pair.of(acct, reconDate);
		}
	}

	private class GetReconDateQuery extends MappingSqlQuery<Pair<Account, LocalDate>> {
		public GetReconDateQuery(DataSource ds) {
			super(ds, "SELECT a.acct_id, a.acct_name, a.acct_type, a.acct_status, " + 
					"b.recon_date FROM acct a " + 
					"left outer join acct_recon b on b.acct_id = a.acct_id " + 
					"where a.acct_id = :acctId ");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected Pair<Account, LocalDate> mapRow(ResultSet rs, int rowNum) throws SQLException {
			Account acct = new Account();
			
			LocalDate reconDate = Util.toLocalDate(rs.getTimestamp("recon_date"));
			acct.setAcctId(rs.getInt("acct_id"));
			acct.setAcctName(StringUtils.trimToEmpty(rs.getString("acct_name")));
			acct.setAcctType(AccountType.fromCode(rs.getInt("acct_type")));
			acct.setStatus(Status.fromCode(rs.getInt("acct_status")));
			return Pair.of(acct, reconDate);
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

	private class InsertAcctRecon extends SqlUpdate {
		private static final String SQL = "insert into acct_recon (acct_id, recon_date) "
				+ "values (:acctId, :reconDate)";

		InsertAcctRecon(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("reconDate", Types.TIMESTAMP));
		}
	}

	private class UpdateAcctRecon extends SqlUpdate {
		private static final String SQL = "update acct_recon set recon_date = :reconDate where acct_id = :acctId";

		UpdateAcctRecon(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("reconDate", Types.TIMESTAMP));
		}
	}


	@Override
	public List<Pair<Account, LocalDate>> findAllReconDates() {
		List<Integer> acctTypes = new ArrayList<>();
		acctTypes.add(AccountType.CHECKING.getCode());
		acctTypes.add(AccountType.SAVINGS.getCode());
		acctTypes.add(AccountType.CREDIT.getCode());
		acctTypes.add(AccountType.INVESTMENT.getCode());
		acctTypes.add(AccountType.AUTOLOAN.getCode());
		acctTypes.add(AccountType.MORTGAGE.getCode());
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctTypes", acctTypes);
		return accountsReconDatesQry.executeByNamedParam(params);
	}

//	public List<Pair<Account, LocalDate>> findAllReconDates() {
//		List<Integer> acctTypes = new ArrayList<>();
//		acctTypes.add(AccountType.CHECKING.getCode());
//		acctTypes.add(AccountType.SAVINGS.getCode());
//		acctTypes.add(AccountType.CREDIT.getCode());
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("acctTypes", acctTypes);
//		return accountsReconDatesQry.executeByNamedParam(params);
//	}

}
