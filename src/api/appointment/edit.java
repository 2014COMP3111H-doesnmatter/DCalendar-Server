package api.appointment;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.Appointment;
import db.Appointment.IsLegalExplain;
import db.Venue;
import doesnmatter.timeMachine.TimeMachine;
import doesnserver.CommonResponses;
import doesnserver.Session;
import doesnutil.DateUtil;
import doesnutil.WrapperUtil;
import api.ApiHandler;

public class edit extends ApiHandler
{

	public edit()
	{
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
		this.addParamConstraint("reminderAhead", ParamCons.INTEGER, true);
		this.addParamConstraint("aWaitingId");
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
		long activeUserId = session.getActiveUserId();

		// get target appt
		long apptId = Long.parseLong(params.get("id"));
		Appointment appt = Appointment.findById(apptId);
		if (appt == null)
		{
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		if (appt.initiatorId != activeUserId)
		{
			rtn.put("rtnCode", this.getRtnCode(406));
			return rtn;
		}

		// venue
		if (params.containsKey("venueId"))
		{
			long venueId = Long.parseLong(params.get("venueId"));
			Venue venue = Venue.findById(venueId);
			if (venue == null)
			{
				rtn.put("rtnCode", this.getRtnCode(407));
				return rtn;
			}
		}

		// name
		if (params.containsKey("name"))
		{
			appt.name = params.get("name");
		}

		// info
		if (params.containsKey("info"))
		{
			appt.info = params.get("info");
		}

		// time
		long startTime =
				params.containsKey("startTime") ? Long.parseLong(params
						.get("startTime")) : appt.startTime;
		long endTime =
				params.containsKey("endTime") ? Long.parseLong(params
						.get("endTime")) : appt.endTime;
		int frequency =
				params.containsKey("frequency") ? Integer.parseInt(params
						.get("frequency")) : appt.frequency;
		long lastDay =
				params.containsKey("lastDay") ? Long.parseLong(params
						.get("lastDay")) : appt.lastDay;
		long venueId = params.containsKey("venueId") ? Long.parseLong(params
				.get("venueId")) : appt.venueId;

		// cannot edit event in the past
		if (startTime < TimeMachine.getNow().getTime())
		{
			rtn.put("rtnCode", this.getRtnCode(408));
			rtn.put("explain", "cannot edit event in the past");
			return rtn;
		}

		// check frequency
		switch (frequency)
		{
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
		lastDay = DateUtil.earliestLastDay(endTime, frequency, lastDay);

		if (startTime != appt.startTime || endTime != appt.endTime
				|| frequency != appt.frequency || lastDay != appt.lastDay || venueId != appt.venueId)
		{

			Appointment.IsLegalExplain explain =
					new Appointment.IsLegalExplain();
			// get candidiates from accepted, rejected and waiting
			Set<Long> candidates = new HashSet<Long>();
			candidates.addAll(appt.aAcceptedId);
			candidates.addAll(appt.aRejectedId);
			candidates.addAll(appt.aWaitingId);

			if (!Appointment.isLegal(session.getActiveUserId(), startTime,
					endTime, frequency, lastDay, venueId, appt.getId(), WrapperUtil
							.toArray(candidates), explain))
			{
				rtn.put("rtnCode", this.getRtnCode(408));
				rtn.put("explain", explain);
				return rtn;
			}

			appt.startTime = startTime;
			appt.endTime = endTime;
			appt.frequency = frequency;
			appt.lastDay = lastDay;
			appt.venueId = venueId;

		}

		// reminder
		if (params.containsKey("reminderAhead"))
		{
			long reminderAhead = Long.parseLong(params.get("reminderAhead"));
			appt.setReminderAhead(activeUserId, reminderAhead);
		}

		

		// read aWaitingId
		if(params.containsKey("aWaitingId")) {
			
			JSONArray jaWaitingId = null;
			long[] aWaitingId = null;
			try
			{
				jaWaitingId = new JSONArray(params.get("aWaitingId"));
				aWaitingId = new long[jaWaitingId.length()];
				for (int i = 0; i < jaWaitingId.length(); i++)
				{
					aWaitingId[i] = jaWaitingId.getLong(i);
				}
				
				// clear joint user before setting
				appt.setJoint(true);
				appt.aWaitingId.clear();
				appt.aAcceptedId.clear();
				appt.aRejectedId.clear();
				
				// set waiting user
				WrapperUtil.toCollection(aWaitingId, appt.aWaitingId);
				
				// send notificaion
				appt.sendInitiatedNotification();
			} catch (JSONException e1)
			{
				return CommonResponses.showParamError();
			}
		}
		else {
			appt.setJoint(false);
		}
		

		rtn.put("rtnCode", this.getRtnCode(200));
		{
			rtn.put("appointment", appt.toJson(activeUserId));
		}
		appt.save();

		return rtn;
	}

}
