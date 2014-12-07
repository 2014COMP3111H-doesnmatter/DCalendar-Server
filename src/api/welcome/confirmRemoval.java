package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import doesnserver.Session;
import api.ApiHandler;

public class confirmRemoval extends ApiHandler
{

	public confirmRemoval()
	{
		this.requireLogin = true;
		this.addRtnCode(405, "not removing");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		User activeUser = User.findById(session.getActiveUserId());
		if(!activeUser.isRemoving()) {
			rtn.put("rtnCode", this.getRtnCode(405));
			return rtn;
		}
		session.setActiveUserId(0L);
		activeUser.finalizeRemoval();
		
		
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}
	

}
