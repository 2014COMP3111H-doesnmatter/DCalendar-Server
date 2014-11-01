package api;

import java.util.Map;

import org.json.JSONObject;

import db.Appointment;
import db.Data;
import db.Reminder;
import db.Venue;
import doesnserver.Session;

public class truncate extends ApiHandler
{

	public truncate()
	{
		this.info = "truncate Appointment, Reminder and Venue tables";
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		Data.truncate(Appointment.class.getSimpleName());
		Data.truncate(Reminder.class.getSimpleName());
		Data.truncate(Venue.class.getSimpleName());
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}

}
