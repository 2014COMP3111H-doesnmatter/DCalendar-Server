package api.appointment;

import java.util.Map;

import org.json.JSONObject;

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
		// TODO Auto-generated method stub
		return super.main(params, session);
	}
	

}
