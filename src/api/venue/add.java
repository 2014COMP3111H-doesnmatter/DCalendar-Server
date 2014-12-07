package api.venue;

import java.util.Map;

import org.json.JSONObject;

import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class add extends ApiHandler
{

	public add() {
		this.info = "add a new venue";
		this.addParamConstraint("name");
		this.addParamConstraint("capacity", ParamCons.INTEGER);
		this.addRtnCode(201, "already exists");
	}
	
	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		// TODO capacity
		JSONObject rtn = new JSONObject();
		String name = params.get("name");
		int capacity = Integer.parseInt(params.get("capacity"));
		Venue venue = Venue.findOne("name", name);
		if(venue != null) {
			rtn.put("rtnCode", this.getRtnCode(201));
		}
		else {
			venue = Venue.create(name, capacity);
			rtn.put("rtnCode", this.getRtnCode(200));
		}
		
		rtn.put("venue", venue.toJson());
		
		return rtn;
	}

}
