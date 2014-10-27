package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class getActiveUser extends ApiHandler
{

	public getActiveUser() {
		this.info = "who am I?";
		this.addRtnCode(201, "no active user");
	}
	
	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		long uid = session.getActiveUserId();
		// if no active user
		if(uid==0) {
			rtn.put("rtnCode", this.getRtnCode(201));
			return rtn;
		}
		User activeUser = User.findById(uid);
		rtn.put("rtnCode", this.getRtnCode(200));
		{
			JSONObject userJo = new JSONObject();
			userJo.put("username", activeUser.username);
			userJo.put("id", activeUser.getId());
			rtn.put("user", userJo);
		}
		return rtn;
	}

}
