package api.venue;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class initiateRemoval extends ApiHandler
{

	public initiateRemoval()
	{
		this.requireLogin = true;
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addRtnCode(405, "permission denied");
		this.addRtnCode(406, "venue not found");
		this.addRtnCode(407, "already removing");
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
		long venueId = Long.parseLong(params.get("id"));
		Venue venue = Venue.findById(venueId);
		
		if(venue == null) {
			rtn.put("rtnCode", this.getRtnCode(406));
			return rtn;
		}
		
		if(venue.isRemoving()) {
			rtn.put("rtnCode", this.getRtnCode(407));
			return rtn;
		}
		
		venue.initiateRemoval();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}
	

}
