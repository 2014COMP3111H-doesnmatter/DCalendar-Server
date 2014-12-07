package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class editUser extends ApiHandler
{

	public editUser()
	{
		this.requireLogin = true;
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addParamConstraint("username", true);
		this.addParamConstraint("fullName", true);
		this.addParamConstraint("email", true);
		this.addRtnCode(405, "permission denied");
		this.addRtnCode(406, "user not found");
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
		
		long targetUserId = Long.parseLong(params.get("id"));
		User targetUser = User.findById(targetUserId);
		if(targetUser == null) {
			rtn.put("rtnCode", this.getRtnCode(406));
		}
		
		if(params.containsKey("username")) {
			targetUser.username = params.get("username");
		}
		if(params.containsKey("fullName")) {
			targetUser.fullName = params.get("fullName");
		}
		if(params.containsKey("email")) {
			targetUser.email = params.get("email");
		}
		targetUser.save();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}

}
