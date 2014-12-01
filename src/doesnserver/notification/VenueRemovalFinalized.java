package doesnserver.notification;

import org.json.JSONException;
import org.json.JSONObject;

import db.Venue;

public class VenueRemovalFinalized extends Notification
{

	private Venue venue;
	public VenueRemovalFinalized(Venue venue)
	{
		this.venue = venue;
	}
	@Override
	public JSONObject toJson() {
		// TODO Auto-generated method stub
		JSONObject rtn = super.toJson();
		try
		{
			rtn.put("venue", this.venue.toJson());
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rtn;
	}
}
