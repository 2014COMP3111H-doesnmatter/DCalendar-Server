package doesnserver.notification;

import org.json.JSONException;
import org.json.JSONObject;

import db.User;

public class UserRemovalFinalized extends Notification
{

	private User user;
	public UserRemovalFinalized(User user)
	{
		this.user = user;
	}
	@Override
	public JSONObject getValueForOutput() {
		try
		{
			return this.user.toJson();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new JSONObject();
		}
	}

}
