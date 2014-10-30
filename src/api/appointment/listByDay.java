package api.appointment;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import db.Appointment;
import doesnserver.Session;
import doesnutil.DateUtil;
import api.ApiHandler;

public class listByDay extends ApiHandler
{

	public listByDay()
	{
		this.requireLogin = true;
		this.info = "list all appointments in a given day";
		this.addParamConstraint("timestamp", ParamCons.INTEGER);
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		long day = Long.parseLong(params.get("timestamp"));
		day = DateUtil.getStartOfDay(day);
		
		rtn.put("rtnCode", this.getRtnCode(200));
		List<Appointment> aAppt = Appointment.findByDay(session.getActiveUserId(), day);
		JSONArray apptJa = new JSONArray();
		for(Appointment iAppt:aAppt) {
			apptJa.put(iAppt.toJson(session.getActiveUserId()));
		}
		rtn.put("aAppointment", apptJa);
		
		return rtn;
	}
	

}
