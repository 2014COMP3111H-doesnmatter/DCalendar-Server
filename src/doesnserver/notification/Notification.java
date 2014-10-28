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

public class Notification
{
	private static Map<Long, Notification> monitors = new HashMap<Long, Notification>();
	private static final long SCHEDULE_PERIOD = 15*60*1000; // intervals to check notifications
	private static final long SCHEDULE_DELAY = 1000; // add a small delay, because at exactly schedule beat, the event is "just about to happen",and is ambiguous
	
	/**
	 * get the next notification
	 * @param uid
	 * @return
	 */
	public static JSONArray nextSync(long uid) {
		Notification monitor = getMonitorFor(uid);
		synchronized(monitor) {
			if(monitor.isEmpty()) {
				try
				{
					monitor.wait();
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
	private static Notification getMonitorFor(long uid) {
		Notification rtn = null;
		if(monitors.containsKey(uid)) {
			return monitors.get(uid);
		}
		else {
			rtn = new Notification(uid);
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
	 * time to find some notifications
	 */
	private static void loopCallBack() {
		Iterator<Entry<Long,Notification>> it = monitors.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Long,Notification> pair = it.next();
			Notification notification = pair.getValue();
			notification.performSchedule();
			it.remove();
		}
	}
	
	
	private JSONArray lastPackedJson = null;
	private long uid;
	private List<String> units = new ArrayList<String>(); //TODO: notification buffer goes here
	public Notification(long uid)
	{
		this.uid = uid;
		
	}
	private boolean isEmpty() {
		return this.units.size() <= 0;
	}
	/**
	 * dump the buffer to a JSONArray for output
	 * buffer will be cleared
	 * if buffer is empty, it will return last result
	 * @return
	 */
	private JSONArray packJson() {
		if(this.isEmpty()) return lastPackedJson;
		JSONArray rtn = new JSONArray();
		for(int i=0; i<units.size(); i++) {
			rtn.put(units.get(i));
		}
		units.clear();
		this.lastPackedJson = rtn;
		return rtn;
	}
	/**
	 * time to see whether i have a notification!
	 */
	private void performSchedule() {
		synchronized(this) {
			// TODO: scan for notification and put it into buffer
			this.units.add("somethin' to know!");
			
			this.notifyAll();
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
}
