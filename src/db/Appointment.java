package db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;



public class Appointment extends Data
{
	protected Appointment()
	{
		super(Appointment.class.getSimpleName());
	}
	public Time startTime;
	public Time endTime;
	public String name;
	public String info;
	public boolean isJoint;
	private int venueId;
	private int initiatorId;
	private int[] nAcceptedId;
	private int[] nRejectedId;
	private int[] nWaitingId;
	
	public static boolean isLegal(long uid, Time startTime, Time endTime) throws SQLException {
		// endTime should be greater than startTime
		if(endTime.compareTo(startTime) <= 0) return false;
		
		// check conflict
		PreparedStatement statement = connect.prepareStatement(
				"select 1 from Appointment where " +
				"`initiatorId` = ? and " +
				"`endTime` > ? and " +
				"`startTime` < ? " +
				"limit 1 "
				);
		statement.setLong(1, uid);
		statement.setTime(2, startTime);
		statement.setTime(3, endTime);
		ResultSet resultSet = statement.executeQuery();
		
		return resultSet.next();
	}
	
	public static Appointment create(long uid, String name, Time startTime, Time endTime, String info) throws SQLException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("initiatorId", String.valueOf(uid));
		values.put("name", name);
		
		
		return null;
	}
	
}
