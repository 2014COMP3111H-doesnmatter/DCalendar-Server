package api.venue;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class remove extends ApiHandler
{
	public remove() {
		this.info = "remove a venue";
		this.addParamConstraint("id", ParamCons.INTEGER);
		this.addRtnCode(201, "venue not found");
	}
	
	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {

		JSONObject rtn = new JSONObject();
		Long id = Long.valueOf(params.get("id"));
		
		Venue venue = Venue.findById(id);
		if(venue == null) {
			rtn.put("rtnCode", this.getRtnCode(201));
			return rtn;
		}
		venue.delete();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}

}
