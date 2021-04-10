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
import org.springframework.util.CollectionUtils;

import com.arvind.model.Security;
import com.arvind.util.SecurityType;

@Repository
public class SecurityDaoImpl extends JdbcDaoSupport implements SecurityDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private GetAllSecuritiesQuery getAllSecuritiesQuery;
	private GetSecurityByTickerQuery getSecurityByTickerQuery;
	private Insert insertQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        getAllSecuritiesQuery = new GetAllSecuritiesQuery(dataSource);
        getSecurityByTickerQuery = new GetSecurityByTickerQuery(dataSource);
        insertQry = new Insert(dataSource);
    }
	
	@Override
	public List<Security> findSecurities() {
		return getAllSecuritiesQuery.execute();
	}

	@Override
	public void insertSecurity(Security sec) {
		Map<String, Object> params = new HashMap<>();
		params.put("ticker", sec.getTicker());
		params.put("description", sec.getDescription());
		params.put("status", sec.getSecurityType().getCode());
		insertQry.updateByNamedParam(params);
	}

	private class GetAllSecuritiesQuery extends MappingSqlQuery<Security> {
		public GetAllSecuritiesQuery(DataSource ds) {
			super(ds, "select ticker, description, status from security order by ticker");
			compile();
		}

		@Override
		protected Security mapRow(ResultSet rs, int rowNum) throws SQLException {
			Security security = new Security();
			security.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			security.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			security.setSecurityType(SecurityType.fromCode(rs.getInt("status")));
			return security;
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into security (ticker, description, status) "
				+ "values (:ticker, :description, :status)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			declareParameter(new SqlParameter("description", Types.VARCHAR));
			declareParameter(new SqlParameter("status", Types.INTEGER));
		}
	}

	@Override
	public Map<String, Security> getSecurities() {
		List<Security> securities = findSecurities();
		Map<String, Security> allSecurities = new HashMap<>();
		for (Security security : securities) {
			allSecurities.put(security.getTicker(), security);
		}
		return allSecurities;
	}

	@Override
	public Security getSecurityByTicker(String ticker) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ticker", ticker);
		List<Security> securities = getSecurityByTickerQuery.executeByNamedParam(params);
		if (CollectionUtils.isEmpty(securities)) {
			return null;
		}
		return securities.get(0);
	}

	private class GetSecurityByTickerQuery extends MappingSqlQuery<Security> {
		public GetSecurityByTickerQuery(DataSource ds) {
			super(ds, "select ticker, description, status from security where ticker in (:ticker) order by ticker");
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			compile();
		}

		@Override
		protected Security mapRow(ResultSet rs, int rowNum) throws SQLException {
			Security security = new Security();
			security.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			security.setDescription(StringUtils.trimToEmpty(rs.getString("description")));
			security.setSecurityType(SecurityType.fromCode(rs.getInt("status")));
			return security;
		}
	}


}
