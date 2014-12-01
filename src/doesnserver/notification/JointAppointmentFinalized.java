package doesnserver.notification;

import org.json.JSONException;
import org.json.JSONObject;

import db.Appointment;

public class JointAppointmentFinalized extends Notification
{

	private Appointment appt = null;
	public JointAppointmentFinalized(Appointment appt)
	{
		this.appt = appt;
	}

	@Override
	public JSONObject toJson() {
		JSONObject rtn = super.toJson();
		try
		{
			rtn.put("appointment", this.appt.toJson(0));
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rtn;
	}
	


}
