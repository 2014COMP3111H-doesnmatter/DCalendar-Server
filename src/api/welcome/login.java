package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class login extends ApiHandler
{

	public login() {
		this.info = "performs a login action";
		this.addParamConstraint("username");
		this.addParamConstraint("password", "the clear password");
		this.addRtnCode(201, "user does not exist");
		this.addRtnCode(202, "password incorrect");
	}
	
	@Override
	public JSONObject main(Map<String, String> params, Session session) throws Exception
	{
		String username = params.get("username");
		String passwordClear = params.get("password");
		User u = User.findOne("username", username);
		
		JSONObject rtn = new JSONObject();
		if(u == null) {
			rtn.put("rtnCode", this.getRtnCode(201));
			return rtn;
		}
		if(!u.checkPassword(passwordClear)) {
			rtn.put("rtnCode", this.getRtnCode(202));
			return rtn;
		}
		
		session.setActiveUserId(u.getId());
		
		// refresh notification when logging in
		u.refreshNotificaion();
		
		rtn.put("rtnCode", this.getRtnCode(200));
		{
			rtn.put("user", u.toJson());
		}
		
		
		return rtn;
	}

}
