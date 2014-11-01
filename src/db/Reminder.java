package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Reminder extends Data
{
	public Reminder()
	{
		super(Reminder.class.getSimpleName());
	}
	public long reminderAhead;
	public static Reminder findByApptAndUser(long apptId, long userId) throws SQLException {

		PreparedStatement statement = connect.prepareStatement(
				"select * from Reminder where appointmentId = ? and userId = ?");
		statement.setLong(1, apptId);
		statement.setLong(2, userId);
		statement.executeQuery();
		ResultSet resultSet = statement.getResultSet();
		if(!resultSet.next()) return null;
		Reminder rtn = new Reminder();
		rtn.id = resultSet.getLong("id");
		rtn.reminderAhead = resultSet.getLong("reminderAhead");
		return rtn;
	}
	public void save() throws SQLException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("reminderAhead", String.valueOf(this.reminderAhead));
		super.save(values);
	}
	public static Reminder create(long apptId, long userId, long reminderAhead) throws SQLException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("appointmentId", String.valueOf(apptId));
		values.put("userId", String.valueOf(userId));
		values.put("reminderAhead", String.valueOf(reminderAhead));
		Reminder rtn = new Reminder();
		Data.create(rtn, values);
		rtn.reminderAhead = reminderAhead;
		return rtn;
	}
	public static void deleteByAppt(long apptId) throws SQLException {
		PreparedStatement statement = connect.prepareStatement(
				"delete from Reminder where appointmentId = ? ");
		statement.setLong(1, apptId);
		statement.executeUpdate();
	}

}
