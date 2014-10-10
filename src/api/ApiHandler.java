package api;
import java.util.Map;

import org.json.*;

import doesnserver.CommonResponses;
import doesnserver.Session;

public class ApiHandler
{
	public boolean requireLogin = false;
	public boolean checkParams(Map<String, String> params) {
		return true;
	}
	public JSONObject main(Map<String,String> params, Session session) throws Exception {

		return CommonResponses.showNotImplemented();
	};
}
