package api.appointment;

import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.Appointment;
import db.Venue;
import doesnserver.CommonResponses;
import doesnserver.Session;
import doesnutil.DateUtil;
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
		this.addParamConstraint("frequency", ParamCons.INTEGER);
		this.addParamConstraint("lastDay", ParamCons.INTEGER);
		this.addParamConstraint("reminderAhead", ParamCons.INTEGER, true);
		this.addParamConstraint("aWaitingId", ParamCons.STRING, "JSON array object", true);
		this.addRtnCode(405, "venue not found");
		this.addRtnCode(406, "illegal time");
		this.addRtnCode(407, "illegal frequency");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		
		JSONObject rtn = new JSONObject();
		
		long activeUserId = session.getActiveUserId();
		String name = params.get("name");
		long venueId = Long.parseLong(params.get("venueId"));
		long startTime = Long.parseLong(params.get("startTime"));
		long endTime = Long.parseLong(params.get("endTime"));
		String info = params.get("info");
		long initiatorId = session.getActiveUserId();
		int frequency = Integer.parseInt(params.get("frequency"));
		long lastDay = Long.parseLong(params.get("lastDay"));
		
		
		// check venue
		if(Venue.findById(venueId) == null) {
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		
		// check frequency
		switch(frequency) {
		case Appointment.Frequency.DAILY:
		case Appointment.Frequency.MONTHLY:
		case Appointment.Frequency.ONCE:
		case Appointment.Frequency.WEEKLY:
			break;
		default:
			rtn.put("rtnCode", this.getRtnCode(407));
			return rtn;
		}
		
		// normalize last day
		lastDay = DateUtil.earliestLastDay(endTime, frequency, lastDay);
		
		// read aWaitingId
		JSONArray jaWaitingId = null;
		long[] aWaitingId = new long[0];
		if(params.containsKey("aWaitingId")) {
			try
			{
				jaWaitingId = new JSONArray(params.get("aWaitingId"));
				aWaitingId = new long[jaWaitingId.length()];
				for(int i=0;i<jaWaitingId.length();i++) {
					aWaitingId[i] = jaWaitingId.getLong(i);
				}
				
			} catch (JSONException e1)
			{
				return CommonResponses.showParamError();
			}
		}
		
		
		
		// check legal
		Appointment.IsLegalExplain explain = new Appointment.IsLegalExplain();
		if(!Appointment.isLegal(initiatorId, startTime, endTime, frequency, lastDay, venueId, 0L, aWaitingId, explain)) {
			rtn.put("rtnCode", this.getRtnCode(406));
			rtn.put("explain", explain);
			return rtn;
		}
		
		// create appointment
		Appointment appt = Appointment.create(initiatorId, name, venueId, startTime, endTime, info, frequency, lastDay, aWaitingId);
		
		// set reminder
		if(params.containsKey("reminderAhead")) {
			long reminderAhead = Long.parseLong(params.get("reminderAhead"));
			appt.setReminderAhead(activeUserId, reminderAhead);
		}
		
		// send notification
		if(appt.isJoint()) {
			appt.sendInitiatedNotification();
		}
		
		// construct return object
		rtn.put("rtnCode", this.getRtnCode(200));
		{
			rtn.put("appointment", appt.toJson(activeUserId));
		}
		
		return rtn;
	}

}
