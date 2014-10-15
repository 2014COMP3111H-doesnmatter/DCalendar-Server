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
		this.info = "edit an appointment";
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addParamConstraint("venueId", ParamCons.INTEGER, true);
		this.addParamConstraint("startTime", ParamCons.INTEGER, true);
		this.addParamConstraint("endTime", ParamCons.INTEGER, true);
		this.addParamConstraint("name", true);
		this.addParamConstraint("info", true);
		this.addRtnCode(405, "appointment not found");
		this.addRtnCode(406, "permission denied");
		this.addRtnCode(407, "venue not found");
		this.addRtnCode(408, "illegal time");
		
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
			rtn.put("appointment", appt.toJson());
		}
		
		return rtn;
	}

}
