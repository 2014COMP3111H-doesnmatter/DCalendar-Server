package doesnmatter.timeMachine;

import java.util.Date;

//set server time and return time
public class TimeMachine
{
	private static long timeDiff = 0L;
	public static Date getNow() {
		if(timeDiff==0) return new Date();
		return new Date(new Date().getTime() + timeDiff);
	}
	public static void setNow(long timestamp) {
		timeDiff = timestamp - new Date().getTime();
	}
}