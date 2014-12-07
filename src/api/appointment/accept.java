package api.appointment;

import java.util.Map;

import org.json.JSONObject;

import db.Appointment;
import doesnserver.Session;
import api.ApiHandler;
import api.ApiHandler;

public class accept extends ApiHandler
{

	public accept()
	{
		this.requireLogin = true;
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addRtnCode(405, "appointment not found");
		this.addRtnCode(406, "illegal time");
		this.addRtnCode(407, "user not waiting");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		
		JSONObject rtn = new JSONObject();
		long activeUserId = session.getActiveUserId();
		long appointmentId = Long.valueOf(params.get("id"));
		Appointment a = Appointment.findById(appointmentId);
		if(a==null){
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		
		if(a.isConflictWithUser(activeUserId)) {
			rtn.put("rtnCode", this.getRtnCode(406));
			return rtn;
		}
		
		if(!a.aWaitingId.contains(activeUserId)) {
			rtn.put("rtnCode", this.getRtnCode(407));
			return rtn;
		}
		
		a.addAcceptedUser(activeUserId);
		a.save();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}

}
