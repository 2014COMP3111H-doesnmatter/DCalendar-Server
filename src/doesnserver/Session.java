package doesnserver;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nanohttpd.NanoHTTPD;
import doesnutil.EncodeUtil;

/**
 * a small in-memory session implementation
 *
 */
public class Session
{
	private static final String COOKIE_KEY = "doesnsessionid";
	private static final long DURATION = 60 * 60 * 1000; // session duration
	private static final long CLEAN_UP_INTERVAL = DURATION / 10; // interval for cleaning up session
	
	private static Map<String,Session> sessions = new HashMap<String, Session>();
	

	/**
	 * clean up a session by id
	 * @param sessionId
	 */
	private static void expireSession(String sessionId) {
		sessions.remove(sessionId);
	}
	/**
	 * traverse all sessions and clean up whatever expired
	 */
	private static void cleanUpAllExpiredSession() {
		Iterator it = sessions.entrySet().iterator();
		long currTime = new Date().getTime();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			Session iSession = (Session)pair.getValue();
			if(iSession.isExpired(currTime)) {
				expireSession((String)pair.getKey());
			}
			it.remove(); // avoids a ConcurrentModificationException
			
		}
	}
	private static Timer timer = new Timer(); // a timer to execute the clean up task
	private static class CleanUpTask extends TimerTask {
		@Override
		public void run() {
			cleanUpAllExpiredSession();	
		}
	}
	static {
		// set up the timer
		timer.scheduleAtFixedRate(new CleanUpTask(), CLEAN_UP_INTERVAL, CLEAN_UP_INTERVAL);
	}
	
	private static int idGeneratorHelper = 0; // a helper to generate id
	/**
	 * generate a session id
	 * @return the generated id
	 */
	private static String generateSessionId() {
		return EncodeUtil.md5(String.valueOf(idGeneratorHelper++));
	}
	
	/**
	 * get a session from id. if this id does not exist, create a new session
	 * @param sessionId
	 * @return
	 */
	public static Session fromSessionId(String sessionId) {
		if(sessions.containsKey(sessionId)) {
			Session rtn = sessions.get(sessionId);
			rtn.recordActive();
			return rtn;
		}
		else {
			return newSession();
		}
		
	}
	/**
	 * get a session from Nano's cookie
	 * @param cookie
	 * @return
	 */
	public static Session fromNanoCookie(NanoHTTPD.CookieHandler cookie) {
		return fromSessionId(cookie.read(COOKIE_KEY));
	}
	/**
	 * create a new session
	 * @return
	 */
	public static Session newSession() {
		String sessionId = generateSessionId();
		Session rtn = new Session(sessionId);
		sessions.put(sessionId, rtn);
		return rtn;
	}
	
	private Map<String,Object> datas; // data in this session
	private String sessionId; // the session id
	private long activeSince; // time when created
	private Session(String sessionId) {
		this.recordActive();
		this.sessionId = sessionId;
		this.datas = new HashMap<String,Object>();
	}
	private void recordActive() {
		this.activeSince = new Date().getTime();
	}
	/**
	 * check if this session has expired
	 * @param currTime
	 * @return
	 */
	private boolean isExpired(long currTime) {
		return activeSince + DURATION < currTime;
	}
	/**
	 * check if this session has expired
	 * @return
	 */
	private boolean isExpired() {
		return isExpired(new Date().getTime());
	}
	/**
	 * get some value from this session
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return datas.get(key);
	}
	/**
	 * store something into this session
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		datas.put(key, value);
	}
	
	/**
	 * if the key exists
	 * @param key
	 * @return
	 */
	public boolean has(String key) {
		return datas.containsKey(key);
	}
	/**
	 * get the session id
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * return a Set-Cookie value for HTTP headers
	 * @return
	 */
	public String getCookieForResponse() {
		return COOKIE_KEY + "=" + sessionId + "; Path=/; HttpOnly ";
	}
	
	/**
	 * get the active user id.
	 * @return 0 if no active user
	 */
	public long getActiveUserId() {
		return this.has("activeUserId")?(long)this.get("activeUserId"):0;
	}
	
	public void setActiveUserId(long id) {
		this.set("activeUserId", id);
	}
	
}
