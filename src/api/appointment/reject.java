package api.appointment;

import java.util.Map;

import org.json.JSONObject;

import doesnserver.Session;
import api.ApiHandler;

public class reject extends ApiHandler
{

	public reject()
	{
		this.addParamConstraint("appointmentId", ParamCons.STRING);
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		// TODO reject
		return super.main(params, session);
	}
	

}
