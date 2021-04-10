package com.arvind.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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

@Repository
public class PayeeMapDaoImpl extends JdbcDaoSupport implements PayeeMapDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private GetAllPayeeMapQuery getAllPayeeMapQuery;
	private ExcludePayeeMapQuery excludePayeeMapQuery;
	private Insert insertQry;
	private Update updateQry;
	private Delete deleteQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        getAllPayeeMapQuery = new GetAllPayeeMapQuery(dataSource);
        excludePayeeMapQuery = new ExcludePayeeMapQuery(dataSource);
        insertQry = new Insert(dataSource);
        updateQry = new Update(dataSource);
        deleteQry = new Delete(dataSource);
    }
	
	@Override
	public Map<String, String> getPayeeMap(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		List<String> excludePayees = excludePayeeMapQuery.executeByNamedParam(params);

		Map<String, String> payees = new HashMap<>();
		List<Pair<String, String>> payeeMap = getAllPayeeMapQuery.executeByNamedParam(params);
		for (Pair<String, String> pair : payeeMap) {
			if (!excludePayees.contains(pair.getKey())) {
				payees.put(pair.getKey(), pair.getValue());
			}
		}		
		return payees;
	}

	@Override
	public Map<String, List<String>> getReversePayeeMap(int acctId) {
		Map<String, String> payeeMap = getPayeeMap(acctId);
		Map<String, List<String>> reversePayeeMap = new HashMap<String, List<String>>();
		for (String inDesc : payeeMap.keySet()) {
			String stdDesc = payeeMap.get(inDesc);
			List<String> inDescs = new ArrayList<String>();
			if (reversePayeeMap.containsKey(stdDesc)) {
				inDescs = reversePayeeMap.get(stdDesc);
			}
			inDescs.add(inDesc);
			reversePayeeMap.put(stdDesc, inDescs);
		}
		return reversePayeeMap;
	}

	@Override
	public void updatePayeeMap(int acctId, String inDesc, String stdDesc) {
		if (StringUtils.equals(inDesc, stdDesc)) return;
		Map<String, String> payeeMap = getPayeeMap(acctId);
		Map<String, List<String>> reversePayeeMap = getReversePayeeMap(acctId);
		if (!payeeMap.containsKey(inDesc)) {
			if (reversePayeeMap.containsKey(inDesc)) {
				List<String> inDescs = reversePayeeMap.get(inDesc);
				for (String inDescIter : inDescs) {
					if (StringUtils.equals(inDescIter, stdDesc)) {
						delete(acctId, stdDesc);
					} else {
						update(acctId, inDescIter, stdDesc);
					}
				}
			}
			insert(acctId, inDesc, stdDesc);
		} else {
			String existingStdDesc = payeeMap.get(inDesc);
			if (!StringUtils.equals(stdDesc, existingStdDesc)) {
				if (StringUtils.equals(inDesc, stdDesc) ) {
					delete(acctId, stdDesc);
				} else {
					update(acctId, inDesc, stdDesc);
				}
			}
		}
	}
	
	@Override
	public void insert(int acctId, String inDesc, String stdDesc) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", acctId);
		params.put("inPayee", inDesc);
		params.put("stdPayee", stdDesc);
		insertQry.updateByNamedParam(params);
	}

	@Override
	public void update(int acctId, String inDesc, String stdDesc) {
		Map<String, Object> params = new HashMap<>();
		params.put("inPayee", inDesc);
		params.put("stdPayee", stdDesc);
		params.put("acctId", acctId);
		updateQry.updateByNamedParam(params);
	}

	@Override
	public void delete(int acctId, String desc) {
		Map<String, Object> params = new HashMap<>();
		params.put("payee", desc);
		params.put("acctId",  acctId);
		deleteQry.updateByNamedParam(params);
	}
	
	private class GetAllPayeeMapQuery extends MappingSqlQuery<Pair<String, String>> {
		public GetAllPayeeMapQuery(DataSource ds) {
			super(ds, "select std_payee, in_payee from trans_payee where acct_id in (:acctId) order by in_payee");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			String in_payee = StringUtils.trimToEmpty(rs.getString("in_payee"));
			String std_payee = StringUtils.trimToEmpty(rs.getString("std_payee"));
			return Pair.of(in_payee, std_payee);
		}
	}

	private class ExcludePayeeMapQuery extends MappingSqlQuery<String> {
		public ExcludePayeeMapQuery(DataSource ds) {
			super(ds, "select in_payee from excl_trans_payee where acct_id = :acctId order by in_payee");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}

		@Override
		protected String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return StringUtils.trimToEmpty(rs.getString("in_payee"));
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into trans_payee (acct_id, std_payee, in_payee) "
				+ "values (:acctId, :stdPayee, :inPayee)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("stdPayee", Types.VARCHAR));
			declareParameter(new SqlParameter("inPayee", Types.VARCHAR));
		}
	}

	private class Update extends SqlUpdate {
		private static final String SQL = "update trans_payee set std_payee = :stdPayee "
				+ "where acct_id = :acctId and in_payee = :inPayee";

		Update(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("stdPayee", Types.VARCHAR));
			declareParameter(new SqlParameter("inPayee", Types.VARCHAR));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from trans_payee where acct_id = :acctId and in_payee = :payee ";
		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("payee", Types.VARCHAR));
		}
	}
	



}
