package api.venue;

import java.util.Map;

import org.json.JSONObject;

import db.User;
import db.Venue;
import doesnserver.Session;
import api.ApiHandler;
import api.ApiHandler;

public class edit extends ApiHandler
{

	public edit()
	{
		this.requireLogin = true;
		this.addParamConstraint("venueId", ParamCons.INTEGER);
		this.addParamConstraint("name", true);
		this.addParamConstraint("capacity", ParamCons.INTEGER, true);
		this.addRtnCode(405, "permission denied");
		this.addRtnCode(406, "venue not found");
		this.addRtnCode(201, "already exists");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn  = new JSONObject();
		long activeUserId = session.getActiveUserId();
		User activeUser = User.findById(activeUserId);
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
		
		if(params.containsKey("name")) {
			if(Venue.findOne("name", params.get("name")) != null) {
				rtn.put("rtnCode", this.getRtnCode(201));
				return rtn;
			}
			venue.name = params.get("name");
		}
		if(params.containsKey("capacity")) {
			venue.capacity = Integer.parseInt(params.get("capacity"));
		}
		venue.save();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}
	

}
