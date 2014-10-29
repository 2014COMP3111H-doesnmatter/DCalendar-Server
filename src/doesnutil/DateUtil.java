package doesnutil;

import java.sql.Date;

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
		return getStartOfMonth(month + 32*DAY_LENGTH);
	}
	public static long transposeToDay(long t, long d) {
		long d1 = getStartOfDay(t);
		return t + d - d1;
	}

}
