package api.timeMachine;

import java.util.Map;

import org.json.JSONObject;

import doesnmatter.timeMachine.TimeMachine;
import doesnserver.Session;
import api.ApiHandler;

public class getNow extends ApiHandler
{

	public getNow()
	{
		this.info = "get server time";
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		JSONObject rtn = new JSONObject();
		long now = TimeMachine.getNow().getTime();
		rtn.put("rtnCode", this.getRtnCode(200));
		rtn.put("timestamp", now);
		return rtn;
	}

}
