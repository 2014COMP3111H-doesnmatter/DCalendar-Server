package api.appointment;

import java.util.Map;

import org.json.JSONObject;

import db.Appointment;
import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class add extends ApiHandler
{

	public add() {
		this.requireLogin = true;
		this.info = "add an appointmemt";
		this.addParamConstraint("name");
		this.addParamConstraint("venueId", ParamCons.INTEGER);
		this.addParamConstraint("startTime", ParamCons.INTEGER);
		this.addParamConstraint("endTime", ParamCons.INTEGER);
		this.addParamConstraint("info");
		this.addRtnCode(405, "venue not found");
		this.addRtnCode(406, "illegal time");
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
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		
		// check legal
		Appointment.IsLegalExplain explain = new Appointment.IsLegalExplain();
		if(!Appointment.isLegal(initiatorId, startTime, endTime, explain)) {
			rtn.put("rtnCode", this.getRtnCode(406));
			rtn.put("explain", explain);
			return rtn;
		}
		
		// create appointment
		Appointment appt = Appointment.create(initiatorId, name, venueId, startTime, endTime, info);
		
		// construct return object
		rtn.put("rtnCode", this.getRtnCode(200));
		{
			rtn.put("appointment", appt.toJson());
		}
		
		return rtn;
	}

}
