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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.arvind.model.SavingTransaction;
import com.arvind.util.Util;

@Repository
public class UploadSavingTransDaoImpl extends JdbcDaoSupport implements UploadSavingTransDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private TransactionsByAcctIdQuery transByAcctIdQry;
	private TransactionsByIdQuery transByIdQry;
	private Insert insertQry;
	private Delete deleteQry;
	private UpdateDescriptionById updateByIdQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        transByAcctIdQry = new TransactionsByAcctIdQuery(dataSource);
        transByIdQry = new TransactionsByIdQuery(dataSource);
        insertQry = new Insert(dataSource);
        insertQry.setReturnGeneratedKeys(true);
        deleteQry = new Delete(dataSource);
        updateByIdQry = new UpdateDescriptionById(dataSource);
    }
	
	@Override
	public List<SavingTransaction> findTransactionsByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return transByAcctIdQry.executeByNamedParam(params);
	}

	@Override
	public List<SavingTransaction> findTransactionsById(int transactionId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("transId", transactionId);
		return transByIdQry.executeByNamedParam(params);
	}

	@Override
	public void insert(SavingTransaction trans) {

		Map<String, Object> params = new HashMap<>();
		params.put("acctId", trans.getAcctId());
		params.put("transDate", Util.toTimestamp(trans.getTransDate()));
		params.put("description", trans.getDescription());
		params.put("transAmt", trans.getTransAmt());
		
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

	
	private class TransactionsByAcctIdQuery extends MappingSqlQuery<SavingTransaction> {
		public TransactionsByAcctIdQuery(DataSource ds) {
			super(ds, "SELECT trans_id, acct_id, trans_date, description, trans_amt " + 
					"from upld_sav_trans " + 
					"where acct_id in (:acctId) " +
					"order by trans_date");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected SavingTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			SavingTransaction trans = new SavingTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setInDescription(trans.getDescription());
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			return trans;
		}
	}

	private class TransactionsByIdQuery extends MappingSqlQuery<SavingTransaction> {
		public TransactionsByIdQuery(DataSource ds) {
			super(ds, "SELECT trans_id, acct_id, trans_date, description, trans_amt " + 
					"from upld_sav_trans " + 
					"where trans_id in (:transId) " +
					"order by trans_date");
			declareParameter(new SqlParameter("transId", Types.INTEGER));
			compile();
		}

		@Override
		protected SavingTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			SavingTransaction trans = new SavingTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setInDescription(trans.getDescription());
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			return trans;
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into upld_sav_trans (acct_id, trans_date, description, trans_amt) "
				+ "values (:acctId, :transDate, :description, :transAmt)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("transDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("description", Types.VARCHAR));
			declareParameter(new SqlParameter("transAmt", Types.DECIMAL));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from upld_sav_trans where trans_id = :transId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("transId", Types.INTEGER));
		}
	}

	private class UpdateDescriptionById extends SqlUpdate {
		private static final String SQL = "update upld_sav_trans set description = :stddesc where description = :dbdesc";

		UpdateDescriptionById(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("stddesc", Types.VARCHAR));
			declareParameter(new SqlParameter("dbdesc", Types.VARCHAR));
		}
	}

	@Override
	public void updatePayeeMap(String inDesc, String stdDesc) {
		if (StringUtils.equals(inDesc, stdDesc)) return;		
		Map<String, Object> params = new HashMap<>();
		params.put("stddesc", stdDesc);
		params.put("dbdesc", inDesc);
		updateByIdQry.updateByNamedParam(params);
	}

}
