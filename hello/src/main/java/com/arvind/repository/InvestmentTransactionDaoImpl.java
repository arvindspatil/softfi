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

import com.arvind.model.InvestmentTransaction;
import com.arvind.util.TransactionType;
import com.arvind.util.Util;

@Repository
public class InvestmentTransactionDaoImpl extends JdbcDaoSupport implements InvestmentTransactionDao {
	
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
	public void insert(InvestmentTransaction trans) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", trans.getAcctId());
		params.put("transDate", Util.toTimestamp(trans.getTransDate()));
		params.put("transType", trans.getTransactionType().getCode());
		params.put("ticker", trans.getTicker());
		params.put("description", trans.getDescription());
		params.put("quantity", trans.getQuantity());
		params.put("fees", trans.getFees());
		params.put("quote", trans.getQuote());
		params.put("transAmt", trans.getTransAmt());
		params.put("transAcct", trans.getTransferAcctId());
		insertQry.updateByNamedParam(params);
	}

	@Override
	public void delete(int transId) {
		Map<String, Object> params = new HashMap<>();
		params.put("transId", transId);
		deleteQry.updateByNamedParam(params);
	}

	@Override
	public List<InvestmentTransaction> findTransactions() {
		return transactionsQuery.execute();
	}

	@Override
	public List<InvestmentTransaction> findTransactionsByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return transactionsByAcctIdQuery.executeByNamedParam(params);
	}
	
	@Override
	public List<InvestmentTransaction> findTransactionsByAcctName(String acctName) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctName", acctName);
		return transactionsByAcctNameQuery.executeByNamedParam(params);
	}

	private class BaseQuery extends MappingSqlQuery<InvestmentTransaction> {
		public BaseQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		@Override
		protected InvestmentTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			InvestmentTransaction trans = new InvestmentTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setTransactionType(TransactionType.fromCode(rs.getInt("trans_type")));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setInDescription(trans.getDescription());
			trans.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			trans.setQuantity(rs.getBigDecimal("quantity"));
			trans.setFees(rs.getBigDecimal("fees"));
			trans.setQuote(rs.getBigDecimal("quote"));
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			trans.setTransferAcctId(rs.getInt("trans_acct"));
			trans.setTransferAcct(StringUtils.trimToEmpty(rs.getString("xfer_acct")));
			return trans;
		}
	}

	private class TransactionsByAcctIdQuery extends BaseQuery {
		public TransactionsByAcctIdQuery(DataSource ds) {
			super(ds, "select a.trans_id, a.acct_id, a.trans_date, a.trans_type, a.ticker, a.description, " +
							"a.quantity, a.fees, a.quote, a.trans_amt, a.trans_acct, b.acct_name as xfer_acct FROM inv_trans a " +
							"left outer join acct b on b.acct_id = a.trans_acct " +
							"where a.acct_id in (:acctId) " +
							"order by a.trans_date");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}
	}
	
	private class TransactionsByAcctNameQuery extends BaseQuery {
		public TransactionsByAcctNameQuery(DataSource ds) {
			super(ds, "select a.trans_id, a.acct_id, a.trans_date, a.trans_type, a.ticker, a.description, " +
					"a.quantity, a.fees, a.quote, a.trans_amt, a.trans_acct, c.acct_name as xfer_acct FROM inv_trans a " +
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
			super(ds, "select a.trans_id, a.acct_id, a.trans_date, a.trans_type, a.ticker, a.description, a.quantity, " +
					"a.fees, a.quote, a.trans_amt, a.trans_acct, b.acct_name as xfer_acct FROM inv_trans a " +
					"left outer join acct b on b.acct_id = a.trans_acct " +
					"order by a.trans_date");
			compile();
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into inv_trans (acct_id, trans_date, trans_type, ticker, description, " +
				"quantity, fees, quote, trans_amt, trans_acct) values (:acctId, :transDate, :transType, " +
				":ticker, :description, :quantity, :fees, :quote, :transAmt, :transAcct)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("transDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("transType", Types.INTEGER));
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			declareParameter(new SqlParameter("description", Types.VARCHAR));
			declareParameter(new SqlParameter("quantity", Types.DECIMAL));
			declareParameter(new SqlParameter("fees", Types.DECIMAL));
			declareParameter(new SqlParameter("quote", Types.DECIMAL));
			declareParameter(new SqlParameter("transAmt", Types.DECIMAL));
			declareParameter(new SqlParameter("transAcct", Types.INTEGER));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from inv_trans where trans_id = :transId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("transId", Types.INTEGER));
		}
	}
}
