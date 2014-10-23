package api.notification;

import java.util.Map;

import org.json.JSONObject;

import doesnserver.Session;
import doesnserver.notification.Notification;
import api.ApiHandler;

public class next extends ApiHandler
{

	public next()
	{
		this.info = "get some notifications";
		this.requireLogin = true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		rtn.put("rtnCode", this.getRtnCode(200));
		rtn.put("aNotification", Notification.nextSync(session.getActiveUserId()));
		return rtn;
	}
	

}
