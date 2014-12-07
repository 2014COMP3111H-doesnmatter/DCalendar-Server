package api.appointment;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.Appointment;
import doesnserver.CommonResponses;
import doesnserver.Session;
import api.ApiHandler;

public class getAvailableTimeSlot extends ApiHandler
{

	public getAvailableTimeSlot()
	{
		this.addParamConstraint("aUserId");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		try
		{
			JSONArray jaUserId = new JSONArray(params.get("aUserId"));
			long[] aUserId = new long[jaUserId.length()];
			for(int i=0;i<jaUserId.length();i++) {
				aUserId[i] = jaUserId.getLong(i);
			}
			rtn.put("rtnCode", this.getRtnCode(200));
			rtn.put("msg", Appointment.getAvailableTimeSlot(aUserId));
			return rtn;
			
		} catch (JSONException e1)
		{
			return CommonResponses.showParamError();
		}
		
	}
	

}
