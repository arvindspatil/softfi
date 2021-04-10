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
public class UploadInvestmentTransDaoImpl extends JdbcDaoSupport implements UploadInvestmentTransDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private TransactionsByAcctIdQuery transByAcctIdQry;
	private TransactionsByIdQuery transByIdQry;
	private Insert insertQry;
	private Delete deleteQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        transByAcctIdQry = new TransactionsByAcctIdQuery(dataSource);
        transByIdQry = new TransactionsByIdQuery(dataSource);
        insertQry = new Insert(dataSource);
        deleteQry = new Delete(dataSource);
    }
	
	@Override
	public List<InvestmentTransaction> findTransactionsByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return transByAcctIdQry.executeByNamedParam(params);
	}

	@Override
	public List<InvestmentTransaction> findTransactionsById(int transId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("transId", transId);
		return transByIdQry.executeByNamedParam(params);
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
		params.put("pricePs", trans.getQuote());
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

	private class TransactionsByAcctIdQuery extends MappingSqlQuery<InvestmentTransaction> {
		public TransactionsByAcctIdQuery(DataSource ds) {
			super(ds, "SELECT trans_id, acct_id, trans_date, trans_type, ticker, " +
					"description, quantity, price_ps, trans_amt, trans_acct " + 
					"from upld_inv_trans " + 
					"where acct_id in (:acctId) " +
					"order by trans_date");			
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected InvestmentTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			InvestmentTransaction trans = new InvestmentTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setTransactionType(TransactionType.fromCode(rs.getInt("trans_type")));
			trans.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setQuantity(rs.getBigDecimal("quantity"));
			trans.setQuote(rs.getBigDecimal("price_ps"));
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			trans.setTransferAcctId(rs.getInt("trans_acct"));
			return trans;
		}
	}

	private class TransactionsByIdQuery extends MappingSqlQuery<InvestmentTransaction> {
		public TransactionsByIdQuery(DataSource ds) {
			super(ds, "SELECT trans_id, acct_id, trans_date, trans_type, ticker, " +
					"description, quantity, price_ps, trans_amt, trans_acct " + 
					"from upld_inv_trans " + 
					"where trans_id in (:transId) " +
					"order by trans_date");			
			declareParameter(new SqlParameter("transId", Types.INTEGER));
			compile();
		}

		@Override
		protected InvestmentTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			InvestmentTransaction trans = new InvestmentTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setTransactionType(TransactionType.fromCode(rs.getInt("trans_type")));
			trans.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setQuantity(rs.getBigDecimal("quantity"));
			trans.setQuote(rs.getBigDecimal("price_ps"));
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			trans.setTransferAcctId(rs.getInt("trans_acct"));
			return trans;
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into upld_inv_trans (acct_id, trans_date, trans_type, "
				+ "ticker, description, quantity, price_ps, trans_amt, trans_acct) "
				+ "values (:acctId, :transDate, :transType, :ticker, :description, "
				+ ":quantity, :pricePs, :transAmt, :transAcct)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("transDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("transType", Types.INTEGER));
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			declareParameter(new SqlParameter("description", Types.VARCHAR));
			declareParameter(new SqlParameter("quantity", Types.DECIMAL));
			declareParameter(new SqlParameter("pricePs", Types.DECIMAL));
			declareParameter(new SqlParameter("transAmt", Types.DECIMAL));
			declareParameter(new SqlParameter("transAcct", Types.INTEGER));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from upld_inv_trans where trans_id = :transId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("transId", Types.INTEGER));
		}
	}

}
