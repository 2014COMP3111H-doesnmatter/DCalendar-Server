package api;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONObject;

import db.Appointment;
import db.Venue;
import doesnserver.Session;


public class debug extends ApiHandler
{

	public debug()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		rtn.put("rtnCode", this.getRtnCode(200));
		Appointment.findById(11L).sendInitiatedNotification();
		
		return rtn;
	}

}
