package api.welcome;

import java.util.Map;

import org.json.JSONObject;

import doesnserver.Session;
import api.ApiHandler;

public class confirmRemoval extends ApiHandler
{

	public confirmRemoval()
	{
		this.requireLogin = true;
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		
		// TODO confirm removal
		
		rtn.put("rtnCode", this.getRtnCode(200));
		return super.main(params, session);
	}
	

}
