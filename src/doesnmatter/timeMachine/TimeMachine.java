package doesnmatter.timeMachine;

import java.util.Date;

//set server time and return time
public class TimeMachine
{

	private static long serverTime;
	public static Date getNow() {
		return new Date();
	}
	public static void setNow(long timeStamp) {
		serverTime = timeStamp;
	}
}