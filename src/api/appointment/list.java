package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;





import db.Appointment;
import doesnserver.Session;
import api.ApiHandler;

public class list extends ApiHandler
{

	public list()
	{
		this.requireLogin = true;
		this.info = "list all appointments during a time span";
		this.addParamConstraint("startTime", ParamCons.INTEGER);
		this.addParamConstraint("endTime", ParamCons.INTEGER);
	}
	@Override
	public boolean checkParams(Map<String, String> params) {
		if(!StringUtils.isNumeric(params.get("startTime"))) return false;
		if(!StringUtils.isNumeric(params.get("endTime"))) return false;
		return true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		long startTime = Long.parseLong(params.get("startTime"));
		long endTime = Long.parseLong(params.get("endTime"));
		Appointment[] aAppt = Appointment.findByTimeSpan(session.getActiveUserId(), startTime, endTime);
		
		rtn.put("rtnCode", "200 ok");
		JSONArray ja = new JSONArray();
		for(int i=0; i<aAppt.length; i++) {
			
			ja.put(aAppt[i].toJson());
		}
		rtn.put("aAppointment", ja);
		return rtn;
	}


}
