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
	public boolean checkParams(Map<String, String> params) {
		if(!StringUtils.isNumeric(params.get("id"))) return false;
		return true;
	}
	
	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {

		JSONObject rtn = new JSONObject();
		Long id = Long.valueOf(params.get("id"));
		
		Venue venue = Venue.findById(id);
		if(venue == null) {
			rtn.put("rtnCode", "201 venue does not exist");
			return rtn;
		}
		venue.delete();
		rtn.put("rtnCode", "200 ok");
		return rtn;
	}

}
