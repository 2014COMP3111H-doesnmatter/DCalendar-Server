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
import doesnutil.DateUtil;

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
	// public boolean isJoint;
	public long venueId;
	public long initiatorId;
	public int frequency;
	public long lastDay;
	private int freqHelper;

	// private int[] nAcceptedId;
	// private int[] nRejectedId;
	// private int[] nWaitingId;

	public static final class Frequency
	{
		public static final int ONCE = 0;
		public static final int DAILY = 1;
		public static final int WEEKLY = 2;
		public static final int MONTHLY = 3;
	}

	/**
	 * check if the difference of two points of time fits time unit (15min)
	 * 
	 * @param toCheck
	 * @param ref
	 * @return
	 */
	private static boolean isDiffLegal(long toCheck, long ref) {
		return ((toCheck - ref) % TIME_UNIT) == 0;
	}

	/**
	 * check whether inserting an appointment is legal. things will be checked:
	 * - endTime should be greater than startTime - should not schedule
	 * appointments in the past - time unit should be 15 min - should not
	 * schedule appointments that span multiple days - should not cause any
	 * conflict
	 * 
	 * @param uid
	 * @param startTime
	 * @param endTime
	 * @param exceptApptId
	 *            when checking conflict, ignore a particular appointment.
	 *            useful when editing appointment.
	 * @param explain
	 *            get an explanation of why it is illegal
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime,
			long exceptApptId, IsLegalExplain explain) throws SQLException {
		// endTime should be greater than startTime
		if (endTime <= startTime)
		{
			if (explain != null)
				explain.explain = "end time should be greater than start time";
			return false;
		}

		// startTime should be greater than NOW
		if (startTime < TimeMachine.getNow().getTime())
		{
			if (explain != null)
				explain.explain = "cannot schedule an appointment in the past";
			return false;
		}

		// 15 min interval
		Date startTimeD = new Date(startTime);
		Date startOfDayD =
				new Date(startTimeD.getYear(), startTimeD.getMonth(),
						startTimeD.getDate());
		long startOfDay = startOfDayD.getTime();
		if (!isDiffLegal(startTime, startOfDay))
		{
			if (explain != null)
				explain.explain = "minimum time unit should be 15 minute";
			return false;
		}
		if (!isDiffLegal(endTime, startOfDay))
		{
			if (explain != null)
				explain.explain = "minimum time unit should be 15 minute";
			return false;
		}

		// within a day
		if (endTime - startOfDay > 24 * 60 * 60 * 1000)
		{
			if (explain != null)
				explain.explain =
						"cannot schedule an appointment that cover multiple days";
			return false;
		}

		// check conflict
		Appointment[] aAppt = Appointment.findByDay(uid, startOfDay);
		for(Appointment iAppt:aAppt) {
			if(iAppt.isConflictWith(startTime, endTime)) {
				if(explain != null) {
					explain.explain =
							"this appointment is conflict with appointment "
									+ iAppt.name;
				}
				return false;
			}
		}
		return true;
		
	}

	/**
	 * the same function as above, with fewer params.
	 * 
	 * @param uid
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime)
			throws SQLException {
		return isLegal(uid, startTime, endTime, 0L, null);
	}

	/**
	 * the same function as above, with fewer params.
	 * 
	 * @param uid
	 * @param startTime
	 * @param endTime
	 * @param explain
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime,
			IsLegalExplain explain) throws SQLException {
		return isLegal(uid, startTime, endTime, 0L, explain);
	}

	/**
	 * to hold the explanation in isLegal. the only point of this class is to
	 * pass a string by reference.
	 *
	 */
	public static class IsLegalExplain
	{
		private String explain;

		public String toString() {
			return explain;
		};
	}

	public static Appointment create(long initiatorId, String name,
			long venueId, long startTime, long endTime, String info,
			int frequency, long lastDay) throws SQLException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("initiatorId", String.valueOf(initiatorId));
		values.put("name", name);
		values.put("venueId", String.valueOf(venueId));
		values.put("startTime", String.valueOf(startTime));
		values.put("endTime", String.valueOf(endTime));
		values.put("info", info);
		values.put("frequency", String.valueOf(frequency));
		values.put("lastDay", String.valueOf(lastDay));
		int freqHelper = Appointment.computeFreqHelper(frequency, startTime);
		values.put("freqHelper", String.valueOf(freqHelper));

		Appointment rtn = new Appointment();
		Data.create(rtn, values);
		rtn.initiatorId = initiatorId;
		rtn.name = name;
		rtn.venueId = venueId;
		rtn.startTime = startTime;
		rtn.endTime = endTime;
		rtn.info = info;
		rtn.frequency = frequency;
		rtn.lastDay = lastDay;
		rtn.freqHelper = freqHelper;
		
		return rtn;
	}

	public void save() throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		values.put("initiatorId", String.valueOf(initiatorId));
		values.put("name", name);
		values.put("venueId", String.valueOf(venueId));
		values.put("startTime", String.valueOf(startTime));
		values.put("endTime", String.valueOf(endTime));
		values.put("info", info);
		super.save(values);
	}

	public static Appointment findById(long id) throws SQLException {
		ResultSet resultSet =
				Data._find(Appointment.class.getSimpleName(), "id", String
						.valueOf(id));
		if (!resultSet.next())
			return null;
		return createOneFromResultSet(resultSet);
	}

	/**
	 * construct an Appointment object from a SQL ResultSet only reads current
	 * record, ResultSet.next() will never be called in this function
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 */
	private static Appointment createOneFromResultSet(ResultSet resultSet)
			throws SQLException {
		Appointment rtn = new Appointment();
		rtn.id = resultSet.getLong("id");
		rtn.name = resultSet.getString("name");
		rtn.initiatorId = resultSet.getLong("initiatorId");
		rtn.venueId = resultSet.getLong("venueId");
		rtn.startTime = resultSet.getLong("startTime");
		rtn.endTime = resultSet.getLong("endTime");
		rtn.info = resultSet.getString("info");
		rtn.frequency = resultSet.getInt("frequency");
		rtn.lastDay = resultSet.getLong("lastDay");
		rtn.freqHelper = resultSet.getInt("freqHelper");
		return rtn;
	}

	public static Appointment[] findByDay(long uid, long day)
			throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();

		// once
		aAppt.addAll(Appointment.findOnceByDaySpan(uid, day, day));
		
		// daily
		aAppt.addAll(Appointment.findDailyByDaySpan(uid, day, day));
		
		// weekly
		aAppt.addAll(Appointment.findWeeklyByDay(uid, day));
		
		// monthly
		aAppt.addAll(Appointment.findMonthlyByDaySpan(uid, day, day));
		
		return aAppt.toArray(new Appointment[0]);
	}

	public static Appointment[] findByWeek(long uid, long week) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		long endOfWeek = week + 6*DateUtil.DAY_LENGTH;
		
		// once
		aAppt.addAll(Appointment.findOnceByDaySpan(uid, week, endOfWeek));
		
		// daily
		aAppt.addAll(Appointment.findDailyByDaySpan(uid, week, endOfWeek));
		
		// weekly
		aAppt.addAll(Appointment.findWeeklyByDaySpan(uid, week, endOfWeek));
		
		// monthly
		aAppt.addAll(Appointment.findMonthlyByDaySpan(uid, week, endOfWeek));
		
		return aAppt.toArray(new Appointment[0]);
	}
	
	public static Appointment[] findByMonth(long uid, long month) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		long month2 = DateUtil.getStartOfMonth(month + 32*DateUtil.DAY_LENGTH);
		long endOfMonth = month2 - DateUtil.DAY_LENGTH;
		
		// once
		aAppt.addAll(Appointment.findOnceByDaySpan(uid, month, endOfMonth));
		
		// daily
		aAppt.addAll(Appointment.findDailyByDaySpan(uid, month, endOfMonth));
		
		// weekly
		aAppt.addAll(Appointment.findWeeklyByDaySpan(uid, month, endOfMonth));
		
		// monthly
		aAppt.addAll(Appointment.findMonthlyByDaySpan(uid, month, endOfMonth));
		return aAppt.toArray(new Appointment[0]);
	}
	
	private static List<Appointment> findOnceByDaySpan(long uid, long startDay,
			long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();

		// select * from Appointment where frequency = $ONCE and uid = $uid
		// and startTime >= $startDay and endTime <= $(endDay+DateUtil.DAY_LENGTH)
		PreparedStatement statement =
				connect.prepareStatement("select * from Appointment where frequency = ? and initiatorId = ? "
						+ "and startTime >= ? and endTime <= ?");
		statement.setInt(1, Frequency.ONCE);
		statement.setLong(2, uid);
		statement.setLong(3, startDay);
		statement.setLong(4, endDay + DateUtil.DAY_LENGTH);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}

	private static List<Appointment> findDailyByDaySpan(long uid,
			long startDay, long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();

		// select * from Appointment where frequency = $DAILY and uid = $uid
		// and startTime <= $(endDay+DateUtil.DAY_LENGTH)
		// and (lastDay >= $startDay or lastDay = 0)
		PreparedStatement statement =
				connect.prepareStatement("select * from Appointment where frequency = ? and initiatorId = ? "
						+ "and startTime <= ? "
						+ "and (lastDay >= ? or lastDay = 0) ");
		statement.setInt(1, Frequency.DAILY);
		statement.setLong(2, uid);
		statement.setLong(3, endDay + DateUtil.DAY_LENGTH);
		statement.setLong(4, startDay);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}

	private static List<Appointment> findWeeklyByDay(long uid, long day) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		// select * from Appointment where frequency = $WEEKLY and uid = $uid
		// and startTime <= $(day + DateUtil.DAY_LENGTH)
		// and (lastDay >= $day or lastDay = 0)
		// and freqHelper = $freqHelper
		PreparedStatement statement =
				connect.prepareStatement("select * from Appointment where frequency = ? and initiatorId = ? "
						+ "and startTime <= ? "
						+ "and (lastDay >= ? or lastDay = 0) "
						+ "and freqHelper = ? ");
		statement.setInt(1, Frequency.WEEKLY);
		statement.setLong(2, uid);
		statement.setLong(3, day + DateUtil.DAY_LENGTH);
		statement.setLong(4, day);
		statement.setInt(5, Appointment
				.computeFreqHelper(Frequency.WEEKLY, day));
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}
	/**
	 * the day span should >= 7 , since this function does not check the freqHelper
	 * @param uid
	 * @param startDay
	 * @param endDay
	 * @return
	 * @throws SQLException
	 */
	private static List<Appointment> findWeeklyByDaySpan(long uid, long startDay, long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		// select * from Appointment where frequency = $WEEKLY and uid = $uid
		// and startTime <= $(endDay+DateUtil.DAY_LENGTH)
		// and (lastDay >= $startDay or lastDay = 0)
		PreparedStatement statement =
				connect.prepareStatement("select * from Appointment where frequency = ? and initiatorId = ? "
						+ "and startTime <= ? "
						+ "and (lastDay >= ? or lastDay = 0) ");
		statement.setInt(1, Frequency.WEEKLY);
		statement.setLong(2, uid);
		statement.setLong(3, endDay + DateUtil.DAY_LENGTH);
		statement.setLong(4, startDay);

		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}
	
	private static List<Appointment> findMonthlyByDaySpan(long uid, long startDay, long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		PreparedStatement statement = null;
		// select * from Appointment where frequency = $MONTHLY and initiatorId = $uid
		// and startTime <= $(endDay + DateUtil.DAY_LENGTH)
		// and ($startDay <= lastDay or lastDay = 0)
		String str = "select * from Appointment where frequency = ? and initiatorId = ? "
				+ "and startTime <= ? "
				+ "and (? <= lastDay or lastDay = 0) ";
		
		
		Date startD = new Date(startDay);
		Date endD = new Date(endDay);
		if(startD.getMonth() == endD.getMonth()) {
			// if within a month
			str += "and ? <= freqHelper and freqHelper <= ? ";
			statement = connect.prepareStatement(str);
			statement.setInt(5, Appointment.computeFreqHelper(Frequency.MONTHLY, startDay));
			statement.setInt(6, Appointment.computeFreqHelper(Frequency.MONTHLY, endDay));
		}
		else {
			// if span 2 months
			Date startOfMonth2D = new Date(endD.getYear(), endD.getMonth(), 0);
			long endOfMonth1 = new Date(startOfMonth2D.getTime() - DateUtil.DAY_LENGTH).getTime();
			str += "and (? <= freqHelper and freqHelper <= ? or freqHelper <= ?) ";
			statement = connect.prepareStatement(str);
			statement.setInt(5, Appointment.computeFreqHelper(Frequency.MONTHLY, startDay));
			statement.setInt(6, Appointment.computeFreqHelper(Frequency.MONTHLY, endOfMonth1));
			statement.setInt(7, Appointment.computeFreqHelper(Frequency.MONTHLY, endDay));
			
		}
		
		statement.setInt(1, Frequency.MONTHLY);
		statement.setLong(2, uid);
		statement.setLong(3, endDay + DateUtil.DAY_LENGTH);
		statement.setLong(4, startDay);
		
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}
	/**
	 * get a JSON object for output
	 * 
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
		apptJo.put("frequency", this.frequency);
		apptJo.put("lastDay", this.lastDay);
		return apptJo;
	}

	private static int computeFreqHelper(int freq, long startTime) {
		switch (freq)
		{
		case Frequency.ONCE:
			return 0;
		case Frequency.DAILY:
			return 0;
		case Frequency.WEEKLY:
			Date startD = new Date(startTime);
			return startD.getDay();
		case Frequency.MONTHLY:
			startD = new Date(startTime);
			return startD.getDate();
		}
		return 0;
	}

	private boolean isConflictWith(long startTime, long endTime) {

		Date start2D = new Date(startTime);
		Date start2DayD = new Date(start2D.getYear(), start2D.getMonth(), start2D.getDate());
		Date start1D = new Date(this.startTime);
		Date start1DayD = new Date(start1D.getYear(), start1D.getMonth(), start1D.getDate());
		long dayDiff = start2DayD.getTime() - start1DayD.getTime();
		if(dayDiff < 0) return false;

		long computedStartTime = this.startTime + dayDiff;
		long computedEndTime = this.endTime + dayDiff;
		if(start2DayD.getTime() > this.lastDay && this.lastDay!=0) return false;

		switch(this.frequency) {
		case Frequency.ONCE:
			break;
		case Frequency.DAILY:
			break;
		case Frequency.WEEKLY:
			if(this.freqHelper != Appointment.computeFreqHelper(Frequency.WEEKLY, startTime)) return false;
			break;
		case Frequency.MONTHLY:
			if(this.freqHelper != Appointment.computeFreqHelper(Frequency.MONTHLY, startTime)) return false;
			break;
		}
		return startTime < computedEndTime && computedStartTime < endTime;
	}
}
