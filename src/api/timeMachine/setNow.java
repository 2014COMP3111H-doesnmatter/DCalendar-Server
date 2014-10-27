package api.timeMachine;

import java.util.Map;

import org.json.JSONObject;

import doesnmatter.timeMachine.TimeMachine;
import doesnserver.Session;
import api.ApiHandler;

public class setNow extends ApiHandler
{
	public setNow() {
		this.info = "adjust the server time";
		this.addParamConstraint("timestamp", ApiHandler.ParamCons.INTEGER,"the new timestamp of 'now', in ms");
	}

	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {
		long newTimestamp = Long.parseLong(params.get("timestamp"));
		TimeMachine.setNow(newTimestamp);
		JSONObject rtn = new JSONObject();
		rtn.put("rtnCode", this.getRtnCode(200));
		return rtn;
	}
	
	
}
