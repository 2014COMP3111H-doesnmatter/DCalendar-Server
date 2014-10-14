package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import db.Appointment;
import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class add extends ApiHandler
{

	public add() {
		requireLogin = true;
	}
	
	@Override
	public boolean checkParams(Map<String, String> params) {
		if(!params.containsKey("name")) return false;
		if(!StringUtils.isNumeric(params.get("venueId"))) return false;
		if(!StringUtils.isNumeric(params.get("startTime"))) return false;
		if(!StringUtils.isNumeric(params.get("endTime"))) return false;
		if(!params.containsKey("info")) return false;
		
		return true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		
		JSONObject rtn = new JSONObject();
		
		String name = params.get("name");
		long venueId = Long.parseLong(params.get("venueId"));
		long startTime = Long.parseLong(params.get("startTime"));
		long endTime = Long.parseLong(params.get("endTime"));
		String info = params.get("info");
		long initiatorId = session.getActiveUserId();
		
		// check venue
		if(Venue.findById(venueId) == null) {
			rtn.put("rtnCode", "405 venue not found");
			return rtn;
		}
		
		// check legal
		Appointment.IsLegalExplain explain = new Appointment.IsLegalExplain();
		if(!Appointment.isLegal(initiatorId, startTime, endTime, explain)) {
			rtn.put("rtnCode", "406 illegal time");
			rtn.put("explain", explain);
			return rtn;
		}
		
		// create appointment
		Appointment appt = Appointment.create(initiatorId, name, venueId, startTime, endTime, info);
		
		// construct return object
		rtn.put("rtnCode", "200 ok");
		{
			JSONObject apptJo = new JSONObject();
			apptJo.put("id", appt.getId());
			apptJo.put("name", appt.name);
			apptJo.put("venueId", appt.venueId);
			apptJo.put("startTime", appt.startTime);
			apptJo.put("endTime", appt.endTime);
			apptJo.put("info", appt.info);
			rtn.put("appointment", apptJo);
		}
		
		return rtn;
	}

}
