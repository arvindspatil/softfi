package com.arvind.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
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

import com.arvind.model.Quote;
import com.arvind.util.Util;

@Repository
public class QuoteDaoImpl extends JdbcDaoSupport implements QuoteDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private QuotesQuery quotesQuery;
	private QuotesByTickerQuery quotesByTickerQuery;
	private LatestQuotesQuery latestQuotesQuery;
	private QuotesByTickerDateQuery quotesByTickerDateQuery;
	private QuotesNearestByTickerDateQuery quotesNearestByTickerDateQuery;
	private RecentQuotesQuery recentQuotesQuery;
	private Insert insertQry;
	private Delete deleteQry;
	private Update updateQry;
	
	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        quotesQuery = new QuotesQuery(dataSource);
        quotesByTickerQuery = new QuotesByTickerQuery(dataSource);
        latestQuotesQuery = new LatestQuotesQuery(dataSource);
        quotesByTickerDateQuery = new QuotesByTickerDateQuery(dataSource);
        quotesNearestByTickerDateQuery = new QuotesNearestByTickerDateQuery(dataSource);
        recentQuotesQuery = new RecentQuotesQuery(dataSource);
        insertQry = new Insert(dataSource);
        deleteQry = new Delete(dataSource);
        updateQry = new Update(dataSource);
    }
	
	@Override
	public void insert(Quote quote) {
		Map<String, Object> params = new HashMap<>();
		params.put("ticker", quote.getTicker());
		params.put("quoteDate", Util.toTimestamp(quote.getQuoteDate()));
		params.put("pricePs", quote.getPricePs());
		insertQry.updateByNamedParam(params);
	}

	@Override
	public void delete(int quoteId) {
		Map<String, Object> params = new HashMap<>();
		params.put("quoteId", quoteId);
		deleteQry.updateByNamedParam(params);
	}

	@Override
	public void update(Quote quote) {
		Map<String, Object> params = new HashMap<>();
		params.put("ticker", quote.getTicker());
		params.put("quoteDate", Util.toTimestamp(quote.getQuoteDate()));
		params.put("pricePs", quote.getPricePs());
		updateQry.updateByNamedParam(params);
	}

	@Override
	public List<Quote> findQuotes() {
		return quotesQuery.execute();
	}

	@Override
	public List<Quote> findQuotesByTicker(String ticker) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ticker", ticker);
		return quotesByTickerQuery.executeByNamedParam(params);
	}

	@Override
	public List<Quote> findLatestQuotes() {
		return latestQuotesQuery.execute();
	}

	@Override
	public List<Quote> findRecentQuotes() {
		return recentQuotesQuery.execute();
	}

	private class BaseQuery extends MappingSqlQuery<Quote> {
		public BaseQuery(DataSource ds, String sql) {
			super(ds, sql);
		}

		@Override
		protected Quote mapRow(ResultSet rs, int rowNum) throws SQLException {
			Quote quote = new Quote();
			quote.setTicker(StringUtils.trimToEmpty(rs.getString("ticker")));
			quote.setPricePs(rs.getBigDecimal("price_ps"));
			quote.setQuoteDate(Util.toLocalDate(rs.getTimestamp("quote_date")));
			return quote;
		}
	}

	private class QuotesByTickerQuery extends BaseQuery {
		public QuotesByTickerQuery(DataSource ds) {
			super(ds, "select ticker, quote_date, price_ps " +
					"FROM sec_quote " + 
					"where ticker in (:ticker) " +
					"order by ticker, quote_date");
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			compile();
		}
	}

	private class QuotesByTickerDateQuery extends BaseQuery {
		public QuotesByTickerDateQuery(DataSource ds) {
			super(ds, "select ticker, quote_date, price_ps " +
					"FROM sec_quote " + 
					"where ticker in (:ticker) " +
					"and quote_date in (:quoteDate) " +
					"order by ticker, quote_date");
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			declareParameter(new SqlParameter("quoteDate", Types.TIMESTAMP));
			compile();
		}
	}
	
	private class QuotesNearestByTickerDateQuery extends BaseQuery {
		public QuotesNearestByTickerDateQuery(DataSource ds) {
			super(ds, "select ticker, quote_date, price_ps " +
					"FROM sec_quote " + 
					"where ticker in (:ticker) " +
					"and quote_date <= :quoteDate " +
					"order by ticker, quote_date desc");
			declareParameter(new SqlParameter("ticker", Types.VARCHAR));
			declareParameter(new SqlParameter("quoteDate", Types.TIMESTAMP));
			compile();
		}
	}
	
	private class QuotesQuery extends BaseQuery {
		public QuotesQuery(DataSource ds) {
			super(ds, "select ticker, quote_date, price_ps " +
					"FROM sec_quote " + 
					"order by ticker, quote_date");
			compile();
		}
	}

	private class LatestQuotesQuery extends BaseQuery {
		public LatestQuotesQuery(DataSource ds) {
			super(ds, "select a.ticker, a.quote_date, a.price_ps " + 
					"from cash.sec_quote a " + 
					"inner join (select ticker, max(quote_date) as max_date " + 
					"from cash.sec_quote " + 
					"group by ticker) b on b.ticker = a.ticker and b.max_date = a.quote_date");
			compile();
		}
	}

	private class RecentQuotesQuery extends BaseQuery {
		public RecentQuotesQuery(DataSource ds) {
			super(ds, "select ticker, quote_date, price_ps " +
					"FROM sec_quote " + 
					"where quote_date > date_sub(now(), interval 15 day) " +
					"order by ticker, quote_date desc");
			compile();
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into sec_quote (ticker, quote_date, price_ps) "
				+ "values (:ticker, :quoteDate, :pricePs)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("ticker", Types.CHAR));
			declareParameter(new SqlParameter("quoteDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("pricePs", Types.DECIMAL));
		}
	}

	private class Update extends SqlUpdate {
		private static final String SQL = "update sec_quote set price_ps = :pricePs "
				+ "where ticker = :ticker and quote_date = :quoteDate";

		Update(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("ticker", Types.CHAR));
			declareParameter(new SqlParameter("quoteDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("pricePs", Types.DECIMAL));
		}
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from sec_quote where quote_id = :quoteId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("quoteId", Types.INTEGER));
		}
	}

	@Override
	public List<Quote> findQuoteByTickerDate(String ticker, LocalDate quoteDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ticker", ticker);
		params.put("quoteDate", Util.toTimestamp(quoteDate));
		return quotesByTickerDateQuery.executeByNamedParam(params);
	}

	@Override
	public List<Quote> findNearestQuoteByTickerDate(String ticker, LocalDate quoteDate) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ticker", ticker);
		params.put("quoteDate", Util.toTimestamp(quoteDate));
		return quotesNearestByTickerDateQuery.executeByNamedParam(params);
	}

}
