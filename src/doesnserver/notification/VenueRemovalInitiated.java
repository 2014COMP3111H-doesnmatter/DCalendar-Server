package doesnserver.notification;

import org.json.JSONException;
import org.json.JSONObject;

import db.Venue;

public class VenueRemovalInitiated extends Notification
{
	private Venue venue;
	public VenueRemovalInitiated(Venue venue)
	{
		this.venue = venue;
	}
	@Override
	public JSONObject getValueForOutput() {
		try
		{
			return this.venue.toJson();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new JSONObject();
		}
	}
	@Override
	public int hashCode() {
		return (int) this.venue.getId();
	}

}
