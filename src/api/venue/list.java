package api.venue;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import db.Venue;
import doesnserver.Session;
import api.ApiHandler;

public class list extends ApiHandler
{

	public list() {
		this.info = "list all venues";
	}
	@Override
	public JSONObject main(Map<String, String> params, Session session)
			throws Exception {

		JSONObject rtn = new JSONObject();
		Venue[] aVenue = Venue.findAll();
		rtn.put("rtnCode", this.getRtnCode(200));
		{
			JSONArray venueJa = new JSONArray();
			for(int i=0; i<aVenue.length; i++) {
				JSONObject venueJo = new JSONObject();
				venueJo.put("id", aVenue[i].getId());
				venueJo.put("name", aVenue[i].name);
				venueJa.put(venueJo);
			}
			rtn.put("aVenue", venueJa);
		}
		return rtn;
	}

}
