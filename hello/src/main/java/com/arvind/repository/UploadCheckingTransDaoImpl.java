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

import com.arvind.model.CheckingTransaction;
import com.arvind.util.Util;

@Repository
public class UploadCheckingTransDaoImpl extends JdbcDaoSupport implements UploadCheckingTransDao {
	
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
	public List<CheckingTransaction> findTransactionsByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return transByAcctIdQry.executeByNamedParam(params);
	}

	@Override
	public List<CheckingTransaction> findTransactionsById(int transactionId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("transId", transactionId);
		return transByIdQry.executeByNamedParam(params);
	}

	@Override
	public void insert(CheckingTransaction trans) {

		Map<String, Object> params = new HashMap<>();
		params.put("acctId", trans.getAcctId());
		params.put("transDate", Util.toTimestamp(trans.getTransDate()));
		params.put("checkNo", trans.getCheckNumber() == 0 ? null : trans.getCheckNumber());
		params.put("description", trans.getDescription());
		params.put("transAmt", trans.getTransAmt());
		params.put("transAcct", trans.getTransferAcctId());
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
//		insertQry.setReturnGeneratedKeys(true);
		insertQry.updateByNamedParam(params, keyHolder/* , new String[] {"GENERATED_ID" } */);
		trans.setTransactionId(keyHolder.getKey().intValue());
	}

	@Override
	public void delete(int transId) {
		Map<String, Object> params = new HashMap<>();
		params.put("transId", transId);
		deleteQry.updateByNamedParam(params);
	}

	
	private class TransactionsByAcctIdQuery extends MappingSqlQuery<CheckingTransaction> {
		public TransactionsByAcctIdQuery(DataSource ds) {
			super(ds, "SELECT a.trans_id, a.acct_id, a.trans_date, a.check_no, a.description, " +
					"a.trans_amt, a.trans_acct, b.acct_name as xfer_acct " + 
					"from upld_chk_trans a " +
					"left outer join acct b on b.acct_id = a.trans_acct " +					
					"where a.acct_id in (:acctId) " +
					"order by trans_date");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected CheckingTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			CheckingTransaction trans = new CheckingTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setCheckNumber(rs.getInt("check_no"));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setInDescription(trans.getDescription());
			trans.setTransferAcctId(rs.getInt("trans_acct"));
			trans.setTransferAcct(StringUtils.trimToEmpty(rs.getString("xfer_acct")));
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			return trans;
		}
	}

	private class TransactionsByIdQuery extends MappingSqlQuery<CheckingTransaction> {
		public TransactionsByIdQuery(DataSource ds) {
			super(ds, "SELECT a.trans_id, a.acct_id, a.trans_date, a.check_no, a.description, " +
					"a.trans_amt, a.trans_acct, b.acct_name as xfer_acct " + 
					"from upld_chk_trans a " + 
					"left outer join acct b on b.acct_id = a.trans_acct " +					
					"where a.trans_id in (:transId) " +
					"order by trans_date");
			declareParameter(new SqlParameter("transId", Types.INTEGER));
			compile();
		}

		@Override
		protected CheckingTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			CheckingTransaction trans = new CheckingTransaction();
			trans.setTransactionId(rs.getInt("trans_id"));
			trans.setAcctId(rs.getInt("acct_id"));
			trans.setTransDate(Util.toLocalDate(rs.getTimestamp("trans_date")));
			trans.setCheckNumber(rs.getInt("check_no"));
			trans.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			trans.setInDescription(trans.getDescription());
			trans.setTransferAcctId(rs.getInt("trans_acct"));
			trans.setTransferAcct(StringUtils.trimToEmpty(rs.getString("xfer_acct")));
			trans.setTransAmt(rs.getBigDecimal("trans_amt"));
			return trans;
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into upld_chk_trans (acct_id, trans_date, check_no, description, trans_amt, trans_acct) "
				+ "values (:acctId, :transDate, :checkNo, :description, :transAmt, :transAcct)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("transDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("checkNo", Types.INTEGER));
			declareParameter(new SqlParameter("description", Types.VARCHAR));
			declareParameter(new SqlParameter("transAmt", Types.DECIMAL));
			declareParameter(new SqlParameter("transAcct", Types.INTEGER));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from upld_chk_trans where trans_id = :transId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("transId", Types.INTEGER));
		}
	}

	private class UpdateDescriptionById extends SqlUpdate {
		private static final String SQL = "update upld_chk_trans set description = :stddesc where description = :dbdesc";

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
