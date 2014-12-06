package api.appointment;

import java.util.Map;

import org.json.JSONObject;

import doesnserver.Session;
import api.ApiHandler;
import api.ApiHandler;

public class accept extends ApiHandler
{

	public accept()
	{
		this.requireLogin = true;
		this.addParamConstraint("appointmentId", ParamCons.STRING);
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		// TODO confirm
		return super.main(params, session);
	}

}
