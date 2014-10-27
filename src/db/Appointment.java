package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import doesnmatter.timeMachine.TimeMachine;



public class Appointment extends Data
{
	protected Appointment()
	{
		super(Appointment.class.getSimpleName());
	}
	public static final long TIME_UNIT = 15 * 60 * 1000;
	public long startTime;
	public long endTime;
	public String name;
	public String info;
	//public boolean isJoint;
	public long venueId;
	public long initiatorId;
	//private int[] nAcceptedId;
	//private int[] nRejectedId;
	//private int[] nWaitingId;
	
	/**
	 * check if the difference of two points of time fits time unit (15min)
	 * @param toCheck
	 * @param ref
	 * @return
	 */
	private static boolean isDiffLegal(long toCheck, long ref) {
		return ((toCheck - ref) % TIME_UNIT) == 0;
	}
	
	
	/**
	 * check whether inserting an appointment is legal. things will be checked:
	 * - endTime should be greater than startTime
	 * - should not schedule appointments in the past
	 * - time unit should be 15 min
	 * - should not schedule appointments that span multiple days
	 * - should not cause any conflict
	 * @param uid
	 * @param startTime 
	 * @param endTime
	 * @param exceptApptId when checking conflict, ignore a particular appointment. useful when editing appointment.
	 * @param explain get an explanation of why it is illegal
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime, long exceptApptId, IsLegalExplain explain) throws SQLException {
		// endTime should be greater than startTime
		if(endTime <= startTime) {
			if(explain != null) explain.explain = "endTime should be greater than startTime";
			return false;
		}
		
		// startTime should be greater than NOW
		if(startTime < TimeMachine.getNow().getTime()) {
			if(explain != null) explain.explain = "cannot schedule an appointment in the past";
			return false;
		}
		
		// 15 min interval
		Date startTimeD = new Date(startTime);
		Date startOfDayD = new Date(startTimeD.getYear(), startTimeD.getMonth(), startTimeD.getDate());
		long startOfDay = startOfDayD.getTime();
		if(!isDiffLegal(startTime, startOfDay)) {
			if(explain != null) explain.explain = "minimum time unit should be 15 minute";
			return false;
		}
		if(!isDiffLegal(endTime, startOfDay)) {
			if(explain != null) explain.explain = "minimum time unit should be 15 minute";
			return false;
		}
			
		// within a day
		if(endTime - startOfDay > 24 * 60 * 60 * 1000) {
			if(explain != null) explain.explain = "cannot schedule an appointment that cover multiple days";
			return false;
		}
		
		// check conflict
		PreparedStatement statement = connect.prepareStatement(
				"select `id` from Appointment where " +
				"`initiatorId` = ? and " +
				"`endTime` > ? and " +
				"`startTime` < ? " + 
				( exceptApptId>0 ? " and id <> ? " : "") +
				"limit 1 "
				);
		statement.setLong(1, uid);
		statement.setLong(2, startTime);
		statement.setLong(3, endTime);
		if(exceptApptId > 0) statement.setLong(4, exceptApptId);
		ResultSet resultSet = statement.executeQuery();
		
		if(resultSet.next()) {
			long targetId = resultSet.getLong("id");
			if(explain != null) explain.explain = "this appointment is conflict with appointment#"+ targetId;
			return false;
		}
		return true;
	}
	/**
	 * the same function as above, with fewer params.
	 * @param uid
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime) throws SQLException {
		return isLegal(uid, startTime, endTime, 0L, null);
	}
	/**
	 * the same function as above, with fewer params.
	 * @param uid
	 * @param startTime
	 * @param endTime
	 * @param explain
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime, IsLegalExplain explain) throws SQLException {
		return isLegal(uid, startTime, endTime, 0L, explain);
	}
	
	/**
	 * to hold the explanation in isLegal. the only point of this class is to pass a string by reference.
	 *
	 */
	public static class IsLegalExplain {
		private String explain;
		public String toString() {return explain;};
	}
	
	public static Appointment create(long initiatorId, String name, long venueId, long startTime, long endTime, String info) throws SQLException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("initiatorId", String.valueOf(initiatorId));
		values.put("name", name);
		values.put("venueId", String.valueOf(venueId));
		values.put("startTime", String.valueOf(startTime));
		values.put("endTime", String.valueOf(endTime));
		values.put("info", info);
		
		Appointment rtn = new Appointment();
		Data.create(rtn, values);
		rtn.initiatorId = initiatorId;
		rtn.name = name;
		rtn.venueId = venueId;
		rtn.startTime = startTime;
		rtn.endTime = endTime;
		rtn.info = info;
		
		return rtn;
	}
	
	public void save() throws Exception {
		Map<String, String> values = new HashMap<String,String>();
		values.put("initiatorId", String.valueOf(initiatorId));
		values.put("name", name);
		values.put("venueId", String.valueOf(venueId));
		values.put("startTime", String.valueOf(startTime));
		values.put("endTime", String.valueOf(endTime));
		values.put("info", info);
		super.save(values);
	}
	
	public static Appointment findById(long id) throws SQLException {
		ResultSet resultSet = Data._find(Appointment.class.getSimpleName(), "id", String.valueOf(id));
		if(!resultSet.next()) return null;
		return createOneFromResultSet(resultSet);
	}
	
	/**
	 * find all appointments within a time span for a particular user
	 * @param initiatorId
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws SQLException
	 */
	public static Appointment[] findByTimeSpan(long initiatorId, long startTime, long endTime) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		// select * from Appointment where initiatorId = $initiatorId and startTime >= $startTime..
		PreparedStatement statement = connect.prepareStatement(
				"select * from `Appointment` where " +
				"`initiatorId` = ? and " + 
				"`startTime` >= ? and " +
				"`endTime` <= ? "
				);
		statement.setLong(1, initiatorId);
		statement.setLong(2, startTime);
		statement.setLong(3, endTime);
		
		// execute query
		ResultSet resultSet = statement.executeQuery();
		
		// gather result
		while(resultSet.next()) {
			Appointment iAppt = createOneFromResultSet(resultSet);
			aAppt.add(iAppt);
		}
		
		Appointment[] rtn = new Appointment[aAppt.size()];
		return aAppt.toArray(rtn);
		
		
	}
	/**
	 * construct an Appointment object from a SQL ResultSet
	 * only reads current record, ResultSet.next() will never be called in this function
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private static Appointment createOneFromResultSet(ResultSet resultSet) throws SQLException {
		Appointment rtn = new Appointment();
		rtn.id = resultSet.getLong("id");
		rtn.name = resultSet.getString("name");
		rtn.initiatorId = resultSet.getLong("initiatorId");
		rtn.venueId = resultSet.getLong("venueId");
		rtn.startTime = resultSet.getLong("startTime");
		rtn.endTime = resultSet.getLong("endTime");
		rtn.info = resultSet.getString("info");
		return rtn;
	}
	
	/**
	 * get a JSON object for output
	 * @return
	 * @throws Exception
	 */
	public JSONObject toJson() throws Exception {
		JSONObject apptJo = new JSONObject();
		apptJo.put("id", this.getId());
		apptJo.put("name", this.name);
		apptJo.put("venueId", this.venueId);
		apptJo.put("startTime", this.startTime);
		apptJo.put("endTime", this.endTime);
		apptJo.put("info", this.info);
		return apptJo;
	}
	
	
	
}
