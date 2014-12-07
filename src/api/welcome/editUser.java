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
		this.addParamConstraint("password", true);
		this.addParamConstraint("username", true);
		this.addParamConstraint("fullName", true);
		this.addParamConstraint("email", true);
		this.addRtnCode(405, "permission denied");
		this.addRtnCode(406, "user not found");
		this.addRtnCode(201, "already exists");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		
		long targetUserId = Long.parseLong(params.get("id"));
		User targetUser = User.findById(targetUserId);
		if(targetUser == null) {
			rtn.put("rtnCode", this.getRtnCode(406));
		}
		
		long activeUserId = session.getActiveUserId();
		User activeUser = User.findById(activeUserId);
		if(targetUserId != activeUserId && !activeUser.isAdmin) {
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		
		if(params.containsKey("username")) {
			if(User.findOne("username", params.get("username")) != null) {
				rtn.put("rtnCode", this.getRtnCode(201));
				return rtn;
			}
			targetUser.username = params.get("username");
		}
		if(params.containsKey("fullName")) {
			targetUser.fullName = params.get("fullName");
		}
		if(params.containsKey("email")) {
			targetUser.email = params.get("email");
		}
		if(params.containsKey("password")) {
			targetUser.setPassword(params.get("password"));
		}
		targetUser.save();
		rtn.put("rtnCode", this.getRtnCode(200));
		rtn.put("user", targetUser.toJson());
		return rtn;
	}

}
