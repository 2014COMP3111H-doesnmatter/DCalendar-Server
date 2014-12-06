package api.notification;

import java.util.Map;

import org.json.JSONObject;

import db.Venue;
import doesnserver.Session;
import doesnserver.notification.Notification;
import doesnserver.notification.VenueRemovalInitiated;
import api.ApiHandler;

public class test extends ApiHandler
{

	public test()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		VenueRemovalInitiated notification = new VenueRemovalInitiated(Venue.findById(1));
		
		
		Notification.add(session.getActiveUserId(), notification);
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}

}
