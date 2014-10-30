package api.appointment;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import db.Appointment;
import doesnserver.Session;
import doesnutil.DateUtil;
import api.ApiHandler;

public class listByMonth extends ApiHandler
{

	public listByMonth()
	{
		this.requireLogin = true;
		this.info = "list all appointments in a given month";
		this.addParamConstraint("timestamp", ParamCons.INTEGER);
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		long month = Long.parseLong(params.get("timestamp"));
		month = DateUtil.getStartOfMonth(month);
		
		rtn.put("rtnCode", this.getRtnCode(200));
		JSONArray apptJa = new JSONArray();
		List<Appointment> aAppt = Appointment.findByMonth(session.getActiveUserId(), month);
		
		for(Appointment iAppt:aAppt) {
			apptJa.put(iAppt.toJson(session.getActiveUserId()));
		}
		rtn.put("aAppointment", apptJa);
		
		return rtn;
	}
	

}
