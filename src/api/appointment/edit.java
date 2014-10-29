package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import db.Appointment;
import db.Appointment.IsLegalExplain;
import db.Venue;
import doesnserver.Session;
import doesnutil.DateUtil;
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
		this.addParamConstraint("frequency", ParamCons.INTEGER, true);
		this.addParamConstraint("lastDay", ParamCons.INTEGER, true);
		this.addRtnCode(405, "appointment not found");
		this.addRtnCode(406, "permission denied");
		this.addRtnCode(407, "venue not found");
		this.addRtnCode(408, "illegal time");
		this.addRtnCode(409, "illegal frequency");
		
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		// get target appt
		long apptId = Long.parseLong(params.get("id"));
		Appointment appt = Appointment.findById(apptId);
		if(appt == null) {
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		if(appt.initiatorId != session.getActiveUserId()) {
			rtn.put("rtnCode", this.getRtnCode(406));
			return rtn;
		}
		
		// venue
		if(params.containsKey("venueId")) {
			long venueId = Long.parseLong(params.get("venueId"));
			Venue venue = Venue.findById(venueId);
			if(venue == null) {
				rtn.put("rtnCode", this.getRtnCode(407));
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
		long startTime = params.containsKey("startTime") ? 
				Long.parseLong(params.get("startTime")) :
				appt.startTime;
		long endTime = params.containsKey("endTime") ? 
				Long.parseLong(params.get("endTime")) :
				appt.endTime;
		int frequency = params.containsKey("frequency") ?
				Integer.parseInt(params.get("frequency")) :
				appt.frequency;
		long lastDay = params.containsKey("lastDay") ?
				Long.parseLong(params.get("lastDay")) :
				appt.lastDay;
		
		// check frequency
		switch(frequency) {
		case Appointment.Frequency.DAILY:
		case Appointment.Frequency.MONTHLY:
		case Appointment.Frequency.ONCE:
		case Appointment.Frequency.WEEKLY:
			break;
		default:
			rtn.put("rtnCode", this.getRtnCode(409));
			return rtn;
		}
				
		// normalize last day
		if(frequency == Appointment.Frequency.ONCE) {
			lastDay = DateUtil.getStartOfDay(endTime);
		}
		else if(lastDay == 0) {
			lastDay = Long.MAX_VALUE;
		}
		else {
			lastDay = DateUtil.getStartOfDay(lastDay);
		}
				
		if(startTime != appt.startTime || endTime != appt.endTime
				|| frequency != appt.frequency || lastDay != appt.lastDay) {
			
			Appointment.IsLegalExplain explain = new Appointment.IsLegalExplain();
			if(!Appointment.isLegal(session.getActiveUserId(), startTime, endTime, frequency, lastDay, appt.getId(), explain)) {
				rtn.put("rtnCode", this.getRtnCode(408));
				rtn.put("explain", explain);
				return rtn;
			}
			
			appt.startTime = startTime;
			appt.endTime = endTime;
			
		}
		
		appt.save();
		rtn.put("rtnCode", this.getRtnCode(200));
		{
			rtn.put("appointment", appt.toJson());
		}
		
		return rtn;
	}

}
