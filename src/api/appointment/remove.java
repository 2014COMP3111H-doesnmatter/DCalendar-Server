package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import db.Appointment;
import doesnmatter.timeMachine.TimeMachine;
import doesnserver.Session;
import api.ApiHandler;

public class remove extends ApiHandler
{

	public remove()
	{
		this.requireLogin = true;
		this.info = "remove an appointment";
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addRtnCode(405, "appointment not found");
		this.addRtnCode(406, "permission denied");
		this.addRtnCode(407, "illegal time");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
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
		
		// cannot delete event in the past
		long startTime = appt.startTime;
		if(startTime < TimeMachine.getNow().getTime()) {
			rtn.put("rtnCode", this.getRtnCode(407));
			return rtn;
		}
		
		appt.delete();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}

}
