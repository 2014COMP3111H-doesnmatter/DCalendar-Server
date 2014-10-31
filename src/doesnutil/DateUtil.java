package doesnutil;

import java.sql.Date;

import db.Appointment;

public class DateUtil
{

	public static final long DAY_LENGTH = 86400000L;

	public static long getStartOfDay(long t) {
		Date d = new Date(t);
		Date dayD = new Date(d.getYear(), d.getMonth(), d.getDate());
		return dayD.getTime();
	}

	public static long getStartOfMonth(long t) {
		Date d = new Date(t);
		Date dayD = new Date(d.getYear(), d.getMonth(), 1);
		return dayD.getTime();
	}

	public static long nextMonth(long month) {
		return getStartOfMonth(month + 32 * DAY_LENGTH);
	}

	public static long prevMonth(long month) {
		return getStartOfMonth(month - 32 * DAY_LENGTH);
	}

	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0) ;
	}
	
	public static int countDayInMonth(long month) {
		Date d = new Date(month);
		int monthIndex = d.getMonth();
		int year = d.getYear();
		switch(monthIndex) {
		case 0:
		case 2:
		case 4:
		case 6:
		case 7:
		case 9:
		case 11:
			return 31;
		case 1:
			return isLeapYear(year)?29:28;
		default:
			return 30;	
		}
	}

	public static long transposeToDay(long t, long d) {
		long d1 = getStartOfDay(t);
		return t + d - d1;
	}

	/**
	 * return the earliest lastDay
	 * @param endTime
	 * @param frequency
	 * @param lastDay
	 * @return
	 */
	public static long earliestLastDay(long endTime, int frequency,
			long lastDay) {
		if (frequency == Appointment.Frequency.ONCE)
		{
			return DateUtil.getStartOfDay(endTime);
		} else if (lastDay == 0)
		{
			return Long.MAX_VALUE;
		} else
		{
			lastDay = DateUtil.getStartOfDay(lastDay);
			switch (frequency)
			{
			case Appointment.Frequency.DAILY:
				return lastDay;
			case Appointment.Frequency.WEEKLY:
				int endTimeFreqHelper = new Date(endTime).getDay();
				int lastDayFreqHelper = new Date(lastDay).getDay();
				int dayDiff = lastDayFreqHelper - endTimeFreqHelper;
				if (dayDiff < 0)
					dayDiff += 7;
				return lastDay - dayDiff * DateUtil.DAY_LENGTH;
			case Appointment.Frequency.MONTHLY:
				endTimeFreqHelper = new Date(endTime).getDate();
				lastDayFreqHelper = new Date(lastDay).getDate();
				dayDiff = lastDayFreqHelper - endTimeFreqHelper;
				if(dayDiff>=0) {
					return lastDay - dayDiff * DateUtil.DAY_LENGTH;
				}
				long monthCursor = prevMonth(getStartOfMonth(lastDay));
				while(countDayInMonth(monthCursor) < endTimeFreqHelper) {
					monthCursor = prevMonth(monthCursor);
				}
				Date rtn = new Date(monthCursor);
				rtn.setDate(endTimeFreqHelper);
				return rtn.getTime();
			default:
				return lastDay;
			}
		}
	}
}
