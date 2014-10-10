package api.appointment;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import doesnserver.Session;
import api.ApiHandler;

public class add extends ApiHandler
{

	public boolean requireLogin = true;
	
	@Override
	public boolean checkParams(Map<String, String> params) {
		if(!params.containsKey("name")) return false;
		if(!StringUtils.isNumeric(params.get("startTime"))) return false;
		if(!StringUtils.isNumeric(params.get("endTime"))) return false;
		if(!params.containsKey("info")) return false;
		
		return true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		return super.main(params, session);
	}

}
