package doesnserver.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

public class UserNotificationBuffer
{

	private long uid;
	private Set<Notification> aNotification = new HashSet<Notification>(); //TODO: notification buffer goes here
	public UserNotificationBuffer(long uid)
	{
		this.uid = uid;
		
	}
	boolean isEmpty() {
		return this.aNotification.size() <= 0;
	}
	/**
	 * dump the buffer to a JSONArray for output
	 * buffer will be cleared
	 * if buffer is empty, it will return []
	 * @return
	 */
	JSONArray packJson() {
		if(this.isEmpty()) return new JSONArray();
		JSONArray rtn = new JSONArray();
		for(Notification notification:this.aNotification) {
			rtn.put(notification.toJson());
		}
		aNotification.clear();
		return rtn;
	}
	/**
	 * time to see whether i have a notification!
	 */
	void performSchedule() {
		synchronized(this) {
			// TODO: scan for notification and put it into buffer
			//this.aNotification.add(null);
			
			//this.notifyAll();
		}
		
		
	}
	public synchronized void addNotification(Notification notification) {
		this.aNotification.add(notification);
		this.notifyAll();
	}

}
