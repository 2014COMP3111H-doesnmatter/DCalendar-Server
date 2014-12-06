package api.venue;

import java.util.Map;

import org.json.JSONObject;

import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class confirmRemoval extends ApiHandler
{

	public confirmRemoval()
	{
		this.requireLogin = true;
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addRtnCode(405, "venue not found");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		long venueId = Long.parseLong(params.get("id"));
		Venue venue = Venue.findById(venueId);
		
		if(venue == null) {
			rtn.put("rtnCode", this.getRtnCode(405));
		}
		
		// TODO confirm venue removal
		
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}
	

}
