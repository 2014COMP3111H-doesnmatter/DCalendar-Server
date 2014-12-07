package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class initiateRemoval extends ApiHandler
{

	public initiateRemoval()
	{
		this.requireLogin = true;
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addRtnCode(405, "permission denied");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		User activeUser = User.findById(session.getActiveUserId());
		if(!activeUser.isAdmin) {
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		
		long uid = Long.parseLong(params.get("id"));
		User targetUser = User.findById(uid);
		targetUser.initiateRemoval();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}
	

}
