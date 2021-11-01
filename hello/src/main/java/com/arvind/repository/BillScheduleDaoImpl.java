package com.arvind.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.arvind.model.BillSchedule;
import com.arvind.util.FrequencyType;
import com.arvind.util.Status;
import com.arvind.util.Util;

@Repository
public class BillScheduleDaoImpl extends JdbcDaoSupport implements BillScheduleDao {
	
	@Autowired 
	private DataSource dataSource;
	
	private GetAllBillSchedulesQuery getAllBillSchedulesQuery;
//	private GetSecurityByTickerQuery getSecurityByTickerQuery;
	private Insert insertQry;
	private Delete deleteQry;

	@PostConstruct
    private void initialize() {
        setDataSource(dataSource);
        getAllBillSchedulesQuery = new GetAllBillSchedulesQuery(dataSource);
//        getAllSecuritiesQuery = new GetAllSecuritiesQuery(dataSource);
//        getSecurityByTickerQuery = new GetSecurityByTickerQuery(dataSource);
        insertQry = new Insert(dataSource);
        deleteQry = new Delete(dataSource);
    }
	
	private class GetAllBillSchedulesQuery extends MappingSqlQuery<BillSchedule> {
		public GetAllBillSchedulesQuery(DataSource ds) {
			super(ds, "select sched_id, payee, frequency, grace_period, start_date, status from payee_schedule order by payee");
			compile();
		}

		@Override
		protected BillSchedule mapRow(ResultSet rs, int rowNum) throws SQLException {
			BillSchedule billSchedule = new BillSchedule();
			billSchedule.setScheduleId(rs.getInt("sched_id"));
			billSchedule.setPayee(StringUtils.trimToEmpty(rs.getString("payee")));
			billSchedule.setFrequencyType(FrequencyType.fromCode(rs.getInt("frequency")));
			billSchedule.setGracePeriod(rs.getInt("grace_period"));
			billSchedule.setStartDate(Util.toLocalDate(rs.getTimestamp("start_date")));
			billSchedule.setStatus(Status.fromCode(rs.getInt("status")));
			return billSchedule;
		}
	}

	private class Insert extends SqlUpdate {
		private static final String SQL = "insert into payee_schedule (payee, start_date, grace_period, frequency, status) "
				+ "values (:payee, :startDate, :gracePeriod, :frequency, :status)";

		Insert(final DataSource dataSource) {
			super(dataSource, SQL);
			declareParameter(new SqlParameter("payee", Types.VARCHAR));
			declareParameter(new SqlParameter("startDate", Types.TIMESTAMP));
			declareParameter(new SqlParameter("gracePeriod", Types.INTEGER));
			declareParameter(new SqlParameter("frequency", Types.INTEGER));
			declareParameter(new SqlParameter("status", Types.INTEGER));
			setReturnGeneratedKeys(true);
			setGeneratedKeysColumnNames(new String[] {"sched_id"} );
		}
	}

	@Override
	public List<BillSchedule> findBillSchedules() {
		return getAllBillSchedulesQuery.execute();
	}

	@Override
	public void insertBillSchedule(BillSchedule sched) {
		Map<String, Object> params = new HashMap<>();
		params.put("payee", sched.getPayee());
		params.put("startDate", sched.getStartDate());
		params.put("gracePeriod", sched.getGracePeriod());
		params.put("frequency", sched.getFrequencyType().getCode());
		params.put("status", Status.ACTIVE.getCode());
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
		insertQry.updateByNamedParam(params, keyHolder);
		sched.setScheduleId(keyHolder.getKey().intValue());
	}

	private class Delete extends SqlUpdate {
		private static final String SQL = "delete from payee_schedule where sched_id = :schedId";

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

}
