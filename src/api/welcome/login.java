package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class login extends ApiHandler
{

	@Override
	public JSONObject main(Map<String, String> params, Session session) throws Exception
	{
		String username = params.get("username");
		String passwordClear = params.get("password");
		User u = User.findOne("username", username);
		
		JSONObject rtn = new JSONObject();
		if(u == null) {
			rtn.put("rtnCode", "201 user does not exist");
			return rtn;
		}
		if(!u.checkPassword(passwordClear)) {
			rtn.put("rtnCode", "202 password incorrect");
			return rtn;
		}
		
		session.setActiveUserId(u.getId());
		rtn.put("rtnCode", "200 ok");
		{
			JSONObject userJo = new JSONObject();
			userJo.put("username", u.username);
			userJo.put("id", u.getId());
			rtn.put("user", userJo);
		}
		
		
		return rtn;
	}

}
