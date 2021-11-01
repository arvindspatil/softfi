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

import com.arvind.model.Bills;
import com.arvind.util.BillStatus;
import com.arvind.util.Util;

@Repository
public class BillsDaoImpl extends JdbcDaoSupport implements BillsDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private GetAllBillsQuery getAllBillsQuery;
	private GetBillsByStatusQuery getBillsByStatusQuery;
	private Insert insertQry;
	private Delete deleteQry;

	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        getAllBillsQuery = new GetAllBillsQuery(dataSource);
        getBillsByStatusQuery = new GetBillsByStatusQuery(dataSource);
        insertQry = new Insert(dataSource);
        deleteQry = new Delete(dataSource);
    }
	
	private class GetAllBillsQuery extends MappingSqlQuery<Bills> {
		public GetAllBillsQuery(DataSource ds) {
			super(ds, "select bill_id, sched_id, payee, stmt_date, due_date, pay_date, amount, status from bills order by stmt_date, payee");
			compile();
		}

		@Override
		protected Bills mapRow(ResultSet rs, int rowNum) throws SQLException {
			Bills bill = new Bills();
			bill.setBillId(rs.getInt("bill_id"));
			bill.setScheduleId(rs.getInt("sched_id"));
			bill.setPayee(StringUtils.trimToEmpty(rs.getString("payee")));
			bill.setStmtDate(Util.toLocalDate(rs.getTimestamp("stmt_date")));
			bill.setDueDate(Util.toLocalDate(rs.getTimestamp("due_date")));
			bill.setPayDate(Util.toLocalDate(rs.getTimestamp("pay_date")));
			bill.setAmount(rs.getBigDecimal("amount"));
			bill.setStatus(BillStatus.fromCode(rs.getInt("status")));
			return bill;
		}
	}

	private class GetBillsByStatusQuery extends MappingSqlQuery<Bills> {
		public GetBillsByStatusQuery(DataSource ds) {
			super(ds, "select bill_id, sched_id, payee, stmt_date, due_date, pay_date, amount, status from bills where status = :pstatus order by stmt_date, payee");
			declareParameter(new SqlParameter("pstatus", Types.INTEGER));
			compile();
		}

		@Override
		protected Bills mapRow(ResultSet rs, int rowNum) throws SQLException {
			Bills bill = new Bills();
			bill.setBillId(rs.getInt("bill_id"));
			bill.setScheduleId(rs.getInt("sched_id"));
			bill.setPayee(StringUtils.trimToEmpty(rs.getString("payee")));
			bill.setStmtDate(Util.toLocalDate(rs.getTimestamp("stmt_date")));
			bill.setDueDate(Util.toLocalDate(rs.getTimestamp("due_date")));
			bill.setPayDate(Util.toLocalDate(rs.getTimestamp("pay_date")));
			bill.setAmount(rs.getBigDecimal("amount"));
			bill.setStatus(BillStatus.fromCode(rs.getInt("status")));
			return bill;
		}
	}


	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into bills (sched_id, payee, stmt_date, due_date, pay_date, amount, status) "
				+ "values (:schedId, :payee, :stmtDate, :dueDate, :payDate, :pamount, :pstatus)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("schedId", Types.INTEGER));
			declareParameter(new SqlParameter("payee", Types.VARCHAR));
			declareParameter(new SqlParameter("stmtDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("dueDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("payDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("pamount", Types.DECIMAL));
			declareParameter(new SqlParameter("pstatus", Types.INTEGER));
		}
	}

	@Override
	public void insertBill(Bills bill) {
		Map<String, Object> params = new HashMap<>();
		params.put("payee", bill.getPayee());
		params.put("schedId", bill.getScheduleId());
		params.put("stmtDate", Util.toTimestamp(bill.getStmtDate()));
		params.put("dueDate", Util.toTimestamp(bill.getDueDate()));
		params.put("payDate", Util.toTimestamp(bill.getPayDate()));
		params.put("pamount", bill.getAmount());
		params.put("pstatus", BillStatus.UNPAID.getCode());
		insertQry.updateByNamedParam(params);		
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from bills where sched_id = :schedId";

		Delete(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("schedId", Types.INTEGER));
		}
	}

	@Override
	public void delete(int schedId) {
		Map<String, Object> params = new HashMap<>();
		params.put("schedId", schedId);
		deleteQry.updateByNamedParam(params);
	}

	@Override
	public List<Bills> getPendingBills() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pstatus", BillStatus.UNPAID.getCode());
		return getBillsByStatusQuery.executeByNamedParam(params);
	}

	@Override
	public List<Bills> getAllBills() {
		return getAllBillsQuery.execute();
	}

}
