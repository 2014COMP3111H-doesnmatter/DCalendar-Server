package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import db.Appointment;
import doesnserver.Session;
import api.ApiHandler;

public class remove extends ApiHandler
{

	public remove()
	{
		this.requireLogin = true;
	}

	@Override
	public boolean checkParams(Map<String, String> params) {
		if(!StringUtils.isNumeric(params.get("id"))) return false;
		return true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
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
		appt.delete();
		rtn.put("rtnCode", "200 ok");
		return rtn;
	}

}
