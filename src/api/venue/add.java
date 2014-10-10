package api.venue;

import java.util.Map;

import org.json.JSONObject;

import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class add extends ApiHandler
{

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		String name = params.get("name");
		Venue venue = Venue.findOne("name", name);
		if(venue != null) {
			rtn.put("rtnCode", "201 already exists");
		}
		else {
			venue = Venue.create(name);
			rtn.put("rtnCode", "200 ok");
		}
		
		JSONObject venueJo = new JSONObject();
		venueJo.put("id", venue.getId());
		venueJo.put("name", venue.name);
		rtn.put("venue", venueJo);
		
		return rtn;
	}

}
