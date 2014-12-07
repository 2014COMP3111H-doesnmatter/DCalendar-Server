package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class signUp extends ApiHandler
{

	public signUp()
	{
		this.addParamConstraint("username");
		this.addParamConstraint("password");
		this.addParamConstraint("fullName", true);
		this.addParamConstraint("email", true);
		this.addRtnCode(201, "user already exist");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		String username = params.get("username");
		String passwordClear = params.get("password");
		String fullName = "";
		String email = "";
		if(params.containsKey("fullName")) fullName = params.get("fullName");
		if(params.containsKey("email")) email = params.get("email");
		
		// check user exist
		if(User.findOne("username", username) != null) {
			rtn.put("rtnCode", this.getRtnCode(201));
			return rtn;
		}
		
		User.create(username, passwordClear, fullName, email);
		rtn.put("rtnCode", this.getRtnCode(200));
		
		return rtn;
	}
	

}
