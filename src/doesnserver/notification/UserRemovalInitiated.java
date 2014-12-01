package doesnserver.notification;

import org.json.JSONException;
import org.json.JSONObject;

import db.User;

public class UserRemovalInitiated extends Notification
{

	private User user;
	public UserRemovalInitiated(User user)
	{
		this.user = user;
	}
	@Override
	public JSONObject toJson() {
		// TODO Auto-generated method stub
		JSONObject rtn = super.toJson();
		try
		{
			rtn.put("user", this.user.toJson());
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rtn;
	}

}
