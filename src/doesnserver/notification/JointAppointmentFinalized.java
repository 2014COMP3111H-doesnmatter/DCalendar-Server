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
	public JSONObject getValueForOutput() {
		try
		{
			return this.appt.toJson(0);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new JSONObject();
		}
	}


	@Override
	public int hashCode() {
		return (int) this.appt.getId();
	}
	


}
