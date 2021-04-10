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

import com.arvind.model.AccountPosition;

@Repository
public class AccountPositionDaoImpl extends JdbcDaoSupport implements AccountPositionDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private Insert insertQry;
	private Delete deleteQry;
	private Update updateQry;
	private AllPositionsQuery allPositionsQry;
	private PositionsByAcctIdQuery positionsByAcctIdQuery;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        insertQry = new Insert(dataSource);
        deleteQry = new Delete(dataSource);
        updateQry = new Update(dataSource);
        allPositionsQry = new AllPositionsQuery(dataSource);
        positionsByAcctIdQuery = new PositionsByAcctIdQuery(dataSource);
    }
	
	@Override
	public List<AccountPosition> findPositions() {
		return allPositionsQry.execute();
	}

	@Override
	public List<AccountPosition> findPositionsByAcctId(int acctId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("acctId", acctId);
		return positionsByAcctIdQuery.executeByNamedParam(params);
	}

	@Override
	public void insert(AccountPosition acctPosition) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", acctPosition.getAcctId());
		params.put("ticker", acctPosition.getTicker());
		params.put("quantity", acctPosition.getQuantity());
		insertQry.updateByNamedParam(params);
	}

	@Override
	public void delete(int acctPositionId) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctPositionId", acctPositionId);
		deleteQry.updateByNamedParam(params);
	}

	@Override
	public void update(AccountPosition acctPosition) {
		Map<String, Object> params = new HashMap<>();
		params.put("acctId", acctPosition.getAcctId());
		params.put("ticker", acctPosition.getTicker());
		params.put("quantity", acctPosition.getQuantity());
		updateQry.updateByNamedParam(params);
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into acct_position (acct_id, ticker, quantity) "
				+ "values (:acctId, :ticker, :quantity)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			declareParameter(new SqlParameter("quantity", Types.DECIMAL));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from acct_position where acct_pstn_id = :acctPositionId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctPositionId", Types.INTEGER));
		}
	}

	private class Update extends SqlUpdate {
		private static final String SQL = "update acct_position set quantity = :quantity "
				+ "where acct_id = :acctId and ticker = :ticker";

		Update(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			declareParameter(new SqlParameter("ticker", Types.CHAR));
			declareParameter(new SqlParameter("quantity", Types.DECIMAL));
		}
	}

	private class BaseQuery extends MappingSqlQuery<AccountPosition> {
		public BaseQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		@Override
		protected AccountPosition mapRow(ResultSet rs, int rowNum) throws SQLException {
			AccountPosition acctPosition = new AccountPosition();
			acctPosition.setAcctPositionId(rs.getInt("acct_pstn_id"));
			acctPosition.setAcctId(rs.getInt("acct_id"));
			acctPosition.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			acctPosition.setQuantity(rs.getBigDecimal("quantity"));
			return acctPosition;
		}
	}

	private class AllPositionsQuery extends BaseQuery {
		public AllPositionsQuery(DataSource ds) {
			super(ds, "select acct_pstn_id, acct_id, ticker, quantity " +
					"FROM acct_position " + 
					"order by acct_id, ticker");
			compile();
		}
	}

	private class PositionsByAcctIdQuery extends BaseQuery {
		public PositionsByAcctIdQuery(DataSource ds) {
			super(ds, "select acct_pstn_id, acct_id, ticker, quantity " +
					"FROM acct_position " + 
					"where acct_id = :acctId " +
					"order by acct_id, ticker");
			declareParameter(new SqlParameter("acctId", Types.INTEGER));
			compile();
		}
	}

}
