package com.arvind.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.arvind.model.AccountBal;
import com.arvind.model.BillSchedule;
import com.arvind.model.CheckingTransaction;
import com.arvind.model.CreditTransaction;
import com.arvind.model.OtherTransaction;
import com.arvind.util.Status;
import com.arvind.util.Util;

@Repository
public class OtherTransactionDaoImpl extends JdbcDaoSupport implements OtherTransactionDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private TransactionsQuery transactionsQuery;
	private TransactionsByAcctIdQuery transactionsByAcctIdQuery;
	private TransactionsByAcctNameQuery transactionsByAcctNameQuery;
	private Insert insertQry;
	private Delete deleteQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        transactionsQuery = new TransactionsQuery(dataSource);
        transactionsByAcctIdQuery = new TransactionsByAcctIdQuery(dataSource);
        transactionsByAcctNameQuery = new TransactionsByAcctNameQuery(dataSource);
        insertQry = new Insert(dataSource);
        deleteQry = new Delete(dataSource);
    }
	
	@Override
	public void insert(OtherTransaction trans) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", trans.getAcctId());
		params.put("transDate", Util.toTimestamp(trans.getTransDate()));
		params.put("description", trans.getDescription());
		params.put("transAmt", trans.getTransAmt());
		params.put("transAcct", trans.getTransferAcctId());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		insertQry.updateByNamedParam(params, keyHolder);
		trans.setTransactionId(keyHolder.getKey().intValue());
	}

	@Override
	public void delete(int transId) {
		Map<String, Object> params = new HashMap<>();
		params.put("transId", transId);
		deleteQry.updateByNamedParam(params);
	}

	@Override
	public List<OtherTransaction> findTransactions() {
		return transactionsQuery.execute();
	}

	@Override
	public List<OtherTransaction> findTransactionsByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return transactionsByAcctIdQuery.executeByNamedParam(params);
	}
	
	@Override
	public List<OtherTransaction> findTransactionsByAcctName(String acctName) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctName", acctName);
		return transactionsByAcctNameQuery.executeByNamedParam(params);
	}

	@Override
	public TreeMap<LocalDate, BigDecimal> getAccountBalance(AccountBal bal, int acctId) {
		TreeMap<LocalDate, BigDecimal> balanceMap = new TreeMap<>();
		List<OtherTransaction> transactions = findTransactionsByAcctId(acctId);
		if (CollectionUtils.isEmpty(transactions)) {
			bal.setBalanceAmt(BigDecimal.ZERO);
		} else {
			balanceMap = Util.updateOtherBalanceByMonth(transactions);
			bal.setBalanceAmt(transactions.get(0).getBalanceAmt());
		}
		bal.setAccountValue(bal.getBalanceAmt());
		return balanceMap;
	}

	private class BaseQuery extends MappingSqlQuery<OtherTransaction> {
		public BaseQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		@Override
		protected OtherTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			OtherTransaction trans = new OtherTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			trans.setInDescription(trans.getDescription());
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setTransferAcctId(rs.getInt("trans_acct"));
			trans.setTransferAcct(StringUtils.trimToEmpty(rs.getString("xfer_acct")));
			return trans;
		}
	}

	private class TransactionsByAcctIdQuery extends BaseQuery {
		public TransactionsByAcctIdQuery(DataSource ds) {
			super(ds, "select a.trans_id, a.acct_id, a.trans_date, a.description, " +
					"a.trans_amt, a.trans_acct, b.acct_name as xfer_acct " +
					"FROM other_trans a " +
					"left outer join acct b on b.acct_id = a.trans_acct " +
					"where a.acct_id in (:acctId) " +
					"order by a.trans_date");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}
	}
	
	private class TransactionsByAcctNameQuery extends BaseQuery {
		public TransactionsByAcctNameQuery(DataSource ds) {
			super(ds, "select a.trans_id, a.acct_id, a.trans_date, a.description, " + 
					"a.trans_amt, a.trans_acct, c.acct_name as xfer_acct " +
					"FROM other_trans a " +
					"inner join acct b on b.acct_id = a.acct_id " +
					"left outer join acct c on c.acct_id = a.trans_acct " +
					"where b.acct_name in (:acctName) " +
					"order by a.trans_date");
			declareParameter(new SqlParameter("acctName", Types.VARCHAR));
			compile();
		}
	}

	private class TransactionsQuery extends BaseQuery {
		public TransactionsQuery(DataSource ds) {
			super(ds, "select a.trans_id, a.acct_id, a.trans_date, a.description, " +
					"a.trans_amt, a.trans_acct, b.acct_name as xfer_acct " +
					"FROM other_trans a " + 
					"left outer join acct b on b.acct_id = a.trans_acct " +
					"order by a.trans_date");
			compile();
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into other_trans (acct_id, trans_date, description, trans_amt, trans_acct) "
				+ "values (:acctId, :transDate, :description, :transAmt, :transAcct)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("transDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("description", Types.CHAR));
			declareParameter(new SqlParameter("transAmt", Types.DECIMAL));
			declareParameter(new SqlParameter("transAcct", Types.INTEGER));
			setReturnGeneratedKeys(true);
			setGeneratedKeysColumnNames(new String[] {"trans_id"} );
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from other_trans where trans_id = :transId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("transId", Types.INTEGER));
		}
	}
}
