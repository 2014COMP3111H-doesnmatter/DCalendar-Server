package api.welcome;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class listUser extends ApiHandler
{

	public listUser()
	{
		this.info = "list all users";
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		rtn.put("rtnCode", this.getRtnCode(200));
		JSONArray userJa = new JSONArray();
		List<User> aUser = User.list();
		for(int i=0; i<aUser.size(); i++) {
			userJa.put(aUser.get(i).toJson());
		}
		rtn.put("aUser", userJa);
		return rtn;
	}
	

}
