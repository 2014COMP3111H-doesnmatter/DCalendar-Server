package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import doesnserver.Session;
import api.ApiHandler;

public class editUser extends ApiHandler
{

	public editUser()
	{
		this.addParamConstraint("username", true);
		this.addParamConstraint("firstName", true);
		this.addParamConstraint("lastName", true);
		this.addParamConstraint("email", true);
		this.addParamConstraint("rank", ParamCons.INTEGER, true);
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		// TODO edit user
		return super.main(params, session);
	}

}
