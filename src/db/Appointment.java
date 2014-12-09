package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import doesnmatter.timeMachine.TimeMachine;
import doesnserver.notification.JointAppointmentFinalized;
import doesnserver.notification.JointAppointmentInitiated;
import doesnserver.notification.Notification;
import doesnutil.DateUtil;
import doesnutil.WrapperUtil;

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
	private boolean isJoint;
	public long venueId;
	public long initiatorId;
	public int frequency;
	public long lastDay;
	private int freqHelper;

	public Set<Long> aAcceptedId = new HashSet<Long>();
	public Set<Long> aRejectedId = new HashSet<Long>();
	public Set<Long> aWaitingId = new HashSet<Long>();

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
	 * @param frequency
	 * @param lastDay
	 * @param exceptApptId
	 *            when checking conflict, ignore a particular appointment.
	 *            useful when editing appointment.
	 * @param explain
	 *            get an explanation of why it is illegal
	 * @return
	 * @throws SQLException
	 */
	public static boolean isLegal(long uid, long startTime, long endTime,
			int frequency, long lastDay, long venueId, long exceptApptId,
			long[] aWaitingId, IsLegalExplain explain) throws SQLException {
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

		// check venue capacity
		Venue venue = Venue.findById(venueId);
		if (venue == null)
		{
			if (explain != null)
				explain.explain = "venue does not exist";
			return false;
		}
		if (venue.capacity <= aWaitingId.length)
		{
			if (explain != null)
				explain.explain = "venue capacity is " + venue.capacity + " but there are " + (aWaitingId.length+1) + " participants";
			return false;
		}

		// check conflict
		if (Appointment.isConflictWithVenue(startTime, endTime, frequency,
				lastDay, venueId, exceptApptId, explain))
			return false;
		if (Appointment.isConflictWithUser(startTime, endTime, frequency,
				lastDay, uid, exceptApptId, explain))
			return false;
		for (long waitingId : aWaitingId)
		{
			if (Appointment.isConflictWithUser(startTime, endTime, frequency,
					lastDay, waitingId, 0, explain))
				return false;
		}

		return true;

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
			int frequency, long lastDay, long[] aWaitingId) throws SQLException {

		int freqHelper = Appointment.computeFreqHelper(frequency, startTime);

		Appointment rtn = new Appointment();
		rtn.initiatorId = initiatorId;
		rtn.name = name;
		rtn.venueId = venueId;
		rtn.startTime = startTime;
		rtn.endTime = endTime;
		rtn.info = info;
		rtn.frequency = frequency;
		rtn.lastDay = lastDay;
		rtn.freqHelper = freqHelper;
		if (aWaitingId != null && aWaitingId.length > 0)
		{
			rtn.isJoint = true;
			for (long waitingId : aWaitingId)
			{
				rtn.aWaitingId.add(waitingId);
			}
		}
		rtn.save();

		return rtn;
	}

	public static Appointment findById(long id) throws SQLException {
		ResultSet resultSet =
				Data._find(Appointment.class.getSimpleName(), "id", String
						.valueOf(id));
		if (!resultSet.next())
			return null;
		return createOneFromResultSet(resultSet);
	}

	public static List<Appointment> findByDay(long uid, long day)
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

		return aAppt;
	}

	public static List<Appointment> findByWeek(long uid, long week)
			throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		long endOfWeek = week + 6 * DateUtil.DAY_LENGTH;

		// once
		aAppt.addAll(Appointment.findOnceByDaySpan(uid, week, endOfWeek));

		// daily
		aAppt.addAll(Appointment.findDailyByDaySpan(uid, week, endOfWeek));

		// weekly
		aAppt.addAll(Appointment.findWeeklyByDaySpan(uid, week, endOfWeek));

		// monthly
		aAppt.addAll(Appointment.findMonthlyByDaySpan(uid, week, endOfWeek));

		return aAppt;
	}

	public static List<Appointment> findByMonth(long uid, long month)
			throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		long month2 =
				DateUtil.getStartOfMonth(month + 32 * DateUtil.DAY_LENGTH);
		long endOfMonth = month2 - DateUtil.DAY_LENGTH;

		// once
		aAppt.addAll(Appointment.findOnceByDaySpan(uid, month, endOfMonth));

		// daily
		aAppt.addAll(Appointment.findDailyByDaySpan(uid, month, endOfMonth));

		// weekly
		aAppt.addAll(Appointment.findWeeklyByDaySpan(uid, month, endOfMonth));

		// monthly
		aAppt.addAll(Appointment.findMonthlyByDaySpan(uid, month, endOfMonth));
		return aAppt;
	}

	public static List<Appointment> findByVenue(long venueId)
			throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		ResultSet resultSet =
				Data._find(Appointment.class.getSimpleName(), "venueId", String
						.valueOf(venueId));
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}

	private static String makeSqlSelectorForUser(long uid) {
		return "select `Appointment`.* from `Appointment_aAcceptedId` "
				+ "right join `Appointment` "
				+ "on `Appointment_aAcceptedId`.`key`=`Appointment`.`id` "
				+ "where `value`= " + uid + " "
				+ "union distinct select * from `Appointment` where `initiatorId`= " + uid + " ";
	}

	private static List<Appointment> findOnceByDaySpan(long uid, long startDay,
			long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();

		// select * from Appointment where frequency = $ONCE
		// and startTime >= $startDay and endTime <=
		// $(endDay+DateUtil.DAY_LENGTH)
		PreparedStatement statement =
				connect.prepareStatement("select * from ("
						+ Appointment.makeSqlSelectorForUser(uid)
						+ ") as Temp where frequency = ? "
						+ "and startTime >= ? and endTime <= ?");
		statement.setInt(1, Frequency.ONCE);
		statement.setLong(2, startDay);
		statement.setLong(3, endDay + DateUtil.DAY_LENGTH);
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

		// select * from Appointment where frequency = $DAILY
		// and startTime <= $(endDay+DateUtil.DAY_LENGTH)
		// and lastDay >= $startDay
		PreparedStatement statement =
				connect.prepareStatement("select * from ("
						+ makeSqlSelectorForUser(uid)
						+ ") as Temp where frequency = ? "
						+ "and startTime <= ? " + "and lastDay >= ? ");
		statement.setInt(1, Frequency.DAILY);
		statement.setLong(2, endDay + DateUtil.DAY_LENGTH);
		statement.setLong(3, startDay);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}

	private static List<Appointment> findWeeklyByDay(long uid, long day)
			throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		// select * from Appointment where frequency = $WEEKLY
		// and startTime <= $(day + DateUtil.DAY_LENGTH)
		// and lastDay >= $day
		// and freqHelper = $freqHelper
		PreparedStatement statement =
				connect.prepareStatement("select * from ("
						+ makeSqlSelectorForUser(uid)
						+ ") as Temp where frequency = ? "
						+ "and startTime <= ? " + "and lastDay >= ? "
						+ "and freqHelper = ? ");
		statement.setInt(1, Frequency.WEEKLY);
		statement.setLong(2, day + DateUtil.DAY_LENGTH);
		statement.setLong(3, day);
		statement.setInt(4, Appointment
				.computeFreqHelper(Frequency.WEEKLY, day));
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}

	/**
	 * the day span should >= 7 , since this function does not check the
	 * freqHelper
	 * 
	 * @param uid
	 * @param startDay
	 * @param endDay
	 * @return
	 * @throws SQLException
	 */
	private static List<Appointment> findWeeklyByDaySpan(long uid,
			long startDay, long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		// select * from Appointment where frequency = $WEEKLY
		// and startTime <= $(endDay+DateUtil.DAY_LENGTH)
		// and lastDay >= $startDay
		PreparedStatement statement =
				connect.prepareStatement("select * from ("
						+ makeSqlSelectorForUser(uid)
						+ ") as Temp where frequency = ? "
						+ "and startTime <= ? " + "and lastDay >= ? ");
		statement.setInt(1, Frequency.WEEKLY);
		statement.setLong(2, endDay + DateUtil.DAY_LENGTH);
		statement.setLong(3, startDay);

		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
	}

	private static List<Appointment> findMonthlyByDaySpan(long uid,
			long startDay, long endDay) throws SQLException {
		List<Appointment> aAppt = new ArrayList<Appointment>();
		PreparedStatement statement = null;
		// select * from Appointment where frequency = $MONTHLY
		// and startTime <= $(endDay + DateUtil.DAY_LENGTH)
		// and $startDay <= lastDay
		String str =
				"select * from (" + makeSqlSelectorForUser(uid)
						+ ") as Temp where frequency = ? "
						+ "and startTime <= ? " + "and ? <= lastDay ";

		Date startD = new Date(startDay);
		Date endD = new Date(endDay);
		if (startD.getMonth() == endD.getMonth())
		{
			// if within a month
			str += "and ? <= freqHelper and freqHelper <= ? ";
			statement = connect.prepareStatement(str);
			statement.setInt(4, Appointment.computeFreqHelper(
					Frequency.MONTHLY, startDay));
			statement.setInt(5, Appointment.computeFreqHelper(
					Frequency.MONTHLY, endDay));
		} else
		{
			// if span 2 months
			Date startOfMonth2D = new Date(endD.getYear(), endD.getMonth(), 0);
			long endOfMonth1 =
					new Date(startOfMonth2D.getTime() - DateUtil.DAY_LENGTH)
							.getTime();
			str +=
					"and (? <= freqHelper and freqHelper <= ? or freqHelper <= ?) ";
			statement = connect.prepareStatement(str);
			statement.setInt(4, Appointment.computeFreqHelper(
					Frequency.MONTHLY, startDay));
			statement.setInt(5, Appointment.computeFreqHelper(
					Frequency.MONTHLY, endOfMonth1));
			statement.setInt(6, Appointment.computeFreqHelper(
					Frequency.MONTHLY, endDay));

		}

		statement.setInt(1, Frequency.MONTHLY);
		statement.setLong(2, endDay + DateUtil.DAY_LENGTH);
		statement.setLong(3, startDay);

		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next())
		{
			aAppt.add(Appointment.createOneFromResultSet(resultSet));
		}
		return aAppt;
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
		rtn.isJoint = resultSet.getInt("isJoint") > 0;
		if (rtn.isJoint)
		{
			// aAcceptedId
			rtn.findArray("aAcceptedId", rtn.aAcceptedId);

			// aRejectedId
			rtn.findArray("aRejectedId", rtn.aRejectedId);

			// aWaitingId
			rtn.findArray("aWaitingId", rtn.aWaitingId);
		}

		return rtn;
	}

	public static List<Appointment> createFromResultSet(ResultSet result)
			throws SQLException {
		List<Appointment> rtn = new ArrayList<Appointment>();
		while (result.next())
		{
			rtn.add(createOneFromResultSet(result));
		}
		return rtn;
	}

	public static String getAvailableTimeSlot(long[] aUid) {
		TimeSlotHelper helper =
				new TimeSlotHelper(DateUtil.getStartOfDay(TimeMachine.getNow()
						.getTime()), 5);
		for (long uid : aUid)
		{
			helper.removeByUser(uid);
		}
		return helper.toSentence();
	}

	public void save() throws SQLException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("initiatorId", String.valueOf(initiatorId));
		values.put("name", name);
		values.put("venueId", String.valueOf(venueId));
		values.put("startTime", String.valueOf(startTime));
		values.put("endTime", String.valueOf(endTime));
		values.put("info", info);
		values.put("frequency", String.valueOf(this.frequency));
		values.put("lastDay", String.valueOf(this.lastDay));
		this.freqHelper =
				Appointment.computeFreqHelper(this.frequency, this.startTime);
		values.put("freqHelper", String.valueOf(this.freqHelper));
		values.put("isJoint", this.isJoint ? "1" : "0");
		super.save(values);
		if (this.isJoint)
		{
			this.saveArray("aAcceptedId", this.aAcceptedId);
			this.saveArray("aRejectedId", this.aRejectedId);
			this.saveArray("aWaitingId", this.aWaitingId);
		}
	}

	/**
	 * get a JSON object for output
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject toJson(long uid) throws Exception {
		JSONObject apptJo = new JSONObject();
		apptJo.put("id", this.getId());
		apptJo.put("initiator", User.findById(this.initiatorId).toJson());
		apptJo.put("name", this.name);
		apptJo.put("venueId", this.venueId);
		apptJo.put("startTime", this.startTime);
		apptJo.put("endTime", this.endTime);
		apptJo.put("info", this.info);
		apptJo.put("frequency", this.frequency);
		apptJo.put("lastDay", this.lastDay);
		if (uid >= 0)
			apptJo.put("reminderAhead", this.getReminderAhead(uid));
		apptJo.put("isJoint", this.isJoint);

		if (this.isJoint)
		{
			apptJo.put("aWaiting", User.listById(WrapperUtil
					.toArray(this.aWaitingId)));
			apptJo.put("aAccepted", User.listById(WrapperUtil
					.toArray(this.aAcceptedId)));
			apptJo.put("aRejected", User.listById(WrapperUtil
					.toArray(this.aRejectedId)));

		}

		return apptJo;
	}

	public long getReminderAhead(long uid) throws SQLException {
		Reminder reminder = Reminder.findByApptAndUser(this.getId(), uid);
		return reminder == null ? 0 : reminder.reminderAhead;
	}

	public void setReminderAhead(long uid, long reminderAhead)
			throws SQLException {
		Reminder reminder = Reminder.findByApptAndUser(this.getId(), uid);
		if (reminder == null)
		{
			Reminder.create(this.getId(), uid, reminderAhead);
		} else
		{
			reminder.reminderAhead = reminderAhead;
			reminder.save();
		}

	}

	public void delete() throws SQLException {
		Reminder.deleteByAppt(this.getId());
		if (this.isJoint)
		{
			this.deleteArray("aWaitingId");
			this.deleteArray("aRejectedId");
			this.deleteArray("aAcceptedId");
		}

		super.delete();
	}

	public void addAcceptedUser(long uid) {
		if (!this.aWaitingId.contains(uid))
			return;
		this.aWaitingId.remove(uid);
		this.aAcceptedId.add(uid);
		if (this.isFinalized())
			this.sendFinalizedNotification();
	}

	public boolean isFinalized() {
		return this.aWaitingId.size() <= 0 && this.aRejectedId.size() <= 0;
	}

	public void addRejectedUser(long uid) {
		if (!this.aWaitingId.contains(uid))
			return;
		this.aWaitingId.remove(uid);
		this.aRejectedId.add(uid);
	}

	public boolean isJoint() {
		return this.isJoint;
	}

	public void setJoint(boolean isJoint) {
		if (this.isJoint == isJoint)
			return;
		if (isJoint)
		{
			this.isJoint = true;
		} else
		{
			try
			{
				this.isJoint = false;
				this.deleteArray("aWaitingId");
				this.deleteArray("aAcceptedId");
				this.deleteArray("aRejectedId");
			} catch (SQLException e)
			{
				e.printStackTrace();
			}

		}
	}

	public void sendInitiatedNotification() {
		for (long uid : this.aWaitingId)
		{
			JointAppointmentInitiated notification =
					new JointAppointmentInitiated(this);
			Notification.add(uid, notification);
		}
	}

	private void sendFinalizedNotification() {
		JointAppointmentFinalized notification =
				new JointAppointmentFinalized(this);
		Notification.add(this.initiatorId, notification);
	}

	private boolean isConflictWith(long startTime, long endTime, int frequency,
			long lastDay) {

		// day span
		long startDay1 = DateUtil.getStartOfDay(this.startTime);
		long startDay2 = DateUtil.getStartOfDay(startTime);
		long endDay1 = this.lastDay;
		long endDay2 = lastDay;
		if (endDay2 < startDay1 || endDay1 < startDay2)
			return false;

		// frequency
		int freq1 = this.frequency;
		int freq2 = frequency;
		int freqHelper1;
		int freqHelper2;
		if (freq1 == Frequency.WEEKLY || freq2 == Frequency.WEEKLY)
		{
			freqHelper1 =
					Appointment.computeFreqHelper(Frequency.WEEKLY, startDay1);
			freqHelper2 =
					Appointment.computeFreqHelper(Frequency.WEEKLY, startDay2);
			if (freqHelper1 != freqHelper2)
				return false;
		} else if (freq1 == Frequency.MONTHLY || freq2 == Frequency.MONTHLY)
		{
			freqHelper1 =
					Appointment.computeFreqHelper(Frequency.MONTHLY, startDay1);
			freqHelper2 =
					Appointment.computeFreqHelper(Frequency.MONTHLY, startDay2);
			if (freqHelper1 != freqHelper2)
				return false;
		}

		// time
		long startTime1 = DateUtil.transposeToDay(this.startTime, startDay2);
		long endTime1 = DateUtil.transposeToDay(this.endTime, startDay2);
		long startTime2 = startTime;
		long endTime2 = endTime;

		return startTime1 < endTime2 && startTime2 < endTime1;
	}

	public boolean isConflictWithUser(long uid) {
		return Appointment.isConflictWithUser(this.startTime, this.endTime,
				this.frequency, this.lastDay, uid, 0L, null);
	}

	private static boolean isConflictWithVenue(long startTime, long endTime,
			int frequency, long lastDay, long venueId, long exceptApptId,
			IsLegalExplain explain) {
		try
		{
			List<Appointment> aAppt = Appointment.findByVenue(venueId);
			for (Appointment iAppt : aAppt)
			{
				if (iAppt.id == exceptApptId)
					continue;
				if (iAppt
						.isConflictWith(startTime, endTime, frequency, lastDay))
				{
					if (explain != null)
					{
						explain.explain =
								"this appointment is conflict with appointment "
										+ iAppt.name + " in location";
					}
					return true;
				}
			}
			return false;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private static boolean isConflictWithUser(long startTime, long endTime,
			int frequency, long lastDay, long uid, long exceptApptId,
			IsLegalExplain explain) {

		try
		{
			long startOfDay = DateUtil.getStartOfDay(startTime);
			List<Appointment> aAppt;
			if (frequency == Frequency.DAILY)
			{
				long month = DateUtil.getStartOfMonth(startTime);
				aAppt = Appointment.findByMonth(uid, month);
				aAppt.addAll(Appointment.findByMonth(uid, DateUtil
						.nextMonth(month)));
			} else
			{
				aAppt = Appointment.findByDay(uid, startOfDay);
			}

			for (Appointment iAppt : aAppt)
			{
				if (iAppt.id == exceptApptId)
					continue;
				if (iAppt
						.isConflictWith(startTime, endTime, frequency, lastDay))
				{
					if (explain != null)
					{
						explain.explain =
								"this appointment is conflict with appointment "
										+ iAppt.name;
					}
					return true;
				}
			}
			return false;
		} catch (Exception e)
		{
			e.printStackTrace();
			return true;
		}

	}

}

class TimeSlotHelper
{
	private static final int nDaySlot = 24 * 4;
	private boolean[] aIsAvailable = null;
	private int nSlot = 0;
	private long startOfDay;
	private int nDay;
	private static DateFormat dateFormat = new SimpleDateFormat(
			"MMM.dd HH:mm");

	public TimeSlotHelper(long startOfDay, int nDay)
	{
		this.startOfDay = startOfDay;
		this.nDay = nDay;
		this.nSlot = nDaySlot * nDay;
		this.aIsAvailable = new boolean[this.nSlot];
		Arrays.fill(this.aIsAvailable, true);
	}

	private int getIndex(long time) {
		return (int) ((time - this.startOfDay) / Appointment.TIME_UNIT);
	}

	private long getSlot(int i) {
		return this.startOfDay + i * Appointment.TIME_UNIT;
	}

	public void removeBySpan(long startTime, long endTime) {
		int startIndex = this.getIndex(startTime);
		int endIndex = this.getIndex(endTime);
		for (int i = startIndex; i < endIndex; i++)
		{
			if (i < 0 || i >= nSlot)
				continue;
			this.aIsAvailable[i] = false;
		}
	}

	public void removeByAppointment(Appointment appt, int dayOffset) {
		this.removeBySpan(DateUtil.transposeToDay(appt.startTime,
				this.startOfDay + dayOffset*DateUtil.DAY_LENGTH), DateUtil.transposeToDay(appt.endTime,
				this.startOfDay + dayOffset*DateUtil.DAY_LENGTH));
	}

	public void removeByUser(long uid) {
		try
		{

			for (int i = 0; i < this.nDay; i++)
			{
				List<Appointment> aAppt = Appointment.findByDay(uid, this.startOfDay + i
						* DateUtil.DAY_LENGTH);
				for(Appointment appt : aAppt) {
					this.removeByAppointment(appt, i);
				}

			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String toSentence() {
		List<Long> aStartTime = new ArrayList<Long>();
		List<Long> aEndTime = new ArrayList<Long>();

		boolean currAvailability = false;
		for (int i = 0; i < nSlot; i++)
		{
			if (this.aIsAvailable[i] == currAvailability)
				continue;
			currAvailability = this.aIsAvailable[i];
			if (currAvailability)
			{
				aStartTime.add(this.getSlot(i));
			} else
			{
				aEndTime.add(this.getSlot(i));
			}
		}
		if (currAvailability)
		{
			aEndTime.add(this.getSlot(nSlot));
		}

		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < aStartTime.size(); i++)
		{
			Date startD = new Date(aStartTime.get(i));
			Date endD = new Date(aEndTime.get(i));
			sb.append("[").append(dateFormat.format(startD)).append(" - ").append(
					dateFormat.format(endD)).append("] \n");
		}

		return sb.toString();
	}
}