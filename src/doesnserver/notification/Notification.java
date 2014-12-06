package doesnserver.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Notification
{
	private static Map<Long, UserNotificationBuffer> monitors = new HashMap<Long, UserNotificationBuffer>();
	static final long SCHEDULE_PERIOD = 15*60*1000L; // intervals to check notifications
	static final long SCHEDULE_DELAY = 1000L; // add a small delay, because at exactly schedule beat, the event is "just about to happen",and is ambiguous
	static final long COMET_TIMEOUT = 10000L;
	/**
	 * get the next notification
	 * @param uid
	 * @return
	 */
	public static JSONArray nextSync(long uid) {
		UserNotificationBuffer monitor = getMonitorFor(uid);
		synchronized(monitor) {
			if(monitor.isEmpty()) {
				try
				{
					monitor.wait(COMET_TIMEOUT);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			return monitor.packJson();
		}
	}
	/**
	 * get a Notification instance for a uid. automatically create a new one if not exist.
	 * @param uid
	 * @return
	 */
	private static UserNotificationBuffer getMonitorFor(long uid) {
		UserNotificationBuffer rtn = null;
		if(monitors.containsKey(uid)) {
			return monitors.get(uid);
		}
		else {
			rtn = new UserNotificationBuffer(uid);
			monitors.put(uid, rtn);
			return rtn;
		}
	}
	
	/**
	 * start the notification daemon, must be called before this class works
	 */
	public static void start_daemon() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				loopCallBack();
			}
		}, getDelayFromNow(), Notification.SCHEDULE_PERIOD);
		
	}
	
	/**
	 * add a notification for a user
	 * @param uid
	 * @param notification
	 */
	public static void add(long uid, Notification notification) {
		UserNotificationBuffer buffer = getMonitorFor(uid);
		buffer.addNotification(notification);
	}
	
	/**
	 * time to find some notifications
	 */
	private static void loopCallBack() {
		Iterator<Entry<Long,UserNotificationBuffer>> it = monitors.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Long,UserNotificationBuffer> pair = it.next();
			UserNotificationBuffer notification = pair.getValue();
			notification.performSchedule();
		}
	}
	/**
	 * get the amount of time to delay from now in order to meet the schedule beat
	 * the schedule beat is 0min -> 15min -> 30min -> 45min
	 * if now is 17min, then in order to schedule at 30min, delay should be 13min
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static long getDelayFromNow() {
		Date nowD = new Date();
		Long now = nowD.getTime();
		Long start = new Date(nowD.getYear(), nowD.getMonth(), nowD.getDate()).getTime() + Notification.SCHEDULE_DELAY;
		long diff = now - start;
		return Notification.SCHEDULE_PERIOD - (diff % Notification.SCHEDULE_PERIOD);
	}
	
	
	
	
	public JSONObject toJson() {
		JSONObject rtn = new JSONObject();
		try
		{
			rtn.put("type", this.getClass().getSimpleName());
			rtn.put("value", this.getValueForOutput());
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rtn;
	}
	public abstract JSONObject getValueForOutput();
	
	
}
