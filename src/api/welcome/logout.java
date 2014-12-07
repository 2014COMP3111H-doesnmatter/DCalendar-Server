package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import doesnserver.Session;
import doesnserver.notification.Notification;
import api.ApiHandler;

public class logout extends ApiHandler
{

	public logout()
	{
		this.requireLogin = true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		rtn.put("rtnCode", this.getRtnCode(200));
		long activeUserId = session.getActiveUserId();
		session.setActiveUserId(0L);
		Notification.stop(activeUserId);
		return rtn;
	}
	

}
