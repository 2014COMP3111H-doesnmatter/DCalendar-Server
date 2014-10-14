package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import db.Appointment;
import db.Appointment.IsLegalExplain;
import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class edit extends ApiHandler
{

	public edit() {
		this.requireLogin = true;
	}
	
	@Override
	public boolean checkParams(Map<String, String> params) {
		if(!StringUtils.isNumeric(params.get("id"))) return false;
		if(params.containsKey("venueId") && !StringUtils.isNumeric(params.get("venueId"))) return false;
		if(params.containsKey("startTime") && !StringUtils.isNumeric(params.get("startTime"))) return false;
		if(params.containsKey("endTime") && !StringUtils.isNumeric(params.get("endTime"))) return false;
		return true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		// get target appt
		long apptId = Long.parseLong(params.get("id"));
		Appointment appt = Appointment.findById(apptId);
		if(appt == null) {
			rtn.put("rtnCode", "405 appointment not found");
			return rtn;
		}
		if(appt.initiatorId != session.getActiveUserId()) {
			rtn.put("rtnCode", "406 permission denied");
			return rtn;
		}
		
		// venue
		if(params.containsKey("venueId")) {
			long venueId = Long.parseLong(params.get("venueId"));
			Venue venue = Venue.findById(venueId);
			if(venue == null) {
				rtn.put("rtnCode", "407 venue not found");
				return rtn;
			}
			appt.venueId = venueId;
		}
		
		// name
		if(params.containsKey("name")) {
			appt.name = params.get("name");
		}
		
		// info
		if(params.containsKey("info")) {
			appt.info = params.get("info");
		}
		
		// time
		if(params.containsKey("startTime") || params.containsKey("endTime")) {
			long startTime = params.containsKey("startTime") ? 
					Long.parseLong(params.get("startTime")) :
					appt.startTime;
			long endTime = params.containsKey("endTime") ? 
					Long.parseLong(params.get("endTime")) :
					appt.endTime;
			
			if(startTime != appt.startTime || endTime != appt.endTime) {
				Appointment.IsLegalExplain explain = new Appointment.IsLegalExplain();
				if(!Appointment.isLegal(session.getActiveUserId(), startTime, endTime, appt.getId(), explain)) {
					rtn.put("rtnCode", "408 illegal time");
					rtn.put("explain", explain);
					return rtn;
				}
				
				appt.startTime = startTime;
				appt.endTime = endTime;
			}	
			
		}
		
		appt.save();
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
