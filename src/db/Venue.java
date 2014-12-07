package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import doesnserver.notification.Notification;
import doesnserver.notification.VenueRemovalFinalized;
import doesnserver.notification.VenueRemovalInitiated;

public class Venue extends Data
{
	public String name;
	public int capacity;
	public Set<Long> aWaitingId = new HashSet<Long>();
	protected Venue() {
		super(Venue.class.getSimpleName());
	}
	public static Venue[] findAll() throws SQLException {
		ResultSet resultSet = Data._findAll(Venue.class.getSimpleName());
		ArrayList<Venue> rtn = new ArrayList<Venue>();
		int i=0;
		for(i=0; resultSet.next();i++) {
			Venue iVenue = new Venue();
			iVenue.id = resultSet.getLong("id");
			iVenue.name = resultSet.getString("name");
			rtn.add(iVenue);
		}
		return rtn.toArray(new Venue[i]);
	}
	public static Venue findOne(String key, String value) throws SQLException {
		ResultSet result = Data._find(Venue.class.getSimpleName(), key, value);
		Venue rtn = new Venue();
		if(!result.next()) {
			return null;
		}
		rtn = Venue.createOneFromResultSet(result);
		return rtn;
	}
	public static Venue findById(long id) throws SQLException {
		return findOne("id", String.valueOf(id));
	}
	public static Venue create(String name, int capacity) throws SQLException {
		Venue rtn = new Venue();

		rtn.name = name;
		rtn.capacity = capacity;
		rtn.save();
		return rtn;
	}
	public void save() throws SQLException {
		Map<String,String> values = new HashMap<String,String>();
		values.put("name", this.name);
		values.put("capacity", String.valueOf(this.capacity));
		this.save(values);
		this.saveArray("aWaitingId", this.aWaitingId);
		
	}
	public JSONObject toJson() {
		JSONObject rtn = new JSONObject();
		try
		{
			rtn.put("id", this.getId());
			rtn.put("name", this.name);
			rtn.put("capacity", this.capacity);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}															
		
		return rtn;
	}
	public boolean isRemoving() {
		return this.aWaitingId.size() > 0;
	}
	public void initiateRemoval() {
		if(this.isRemoving()) return;
		try
		{
			List<Appointment> aAppt = Appointment.findByVenue(this.getId());
			// if no user is concerned, finalize it
			if(aAppt.size() <= 0) {
				this.finalizeRemovalWithoutChecking();
				return;
			}
			this.aWaitingId.clear();
			for(Appointment appt:aAppt) {
				this.aWaitingId.add(appt.initiatorId);
				VenueRemovalInitiated notification = new VenueRemovalInitiated(this);
				Notification.add(appt.initiatorId, notification);
			}
			this.save();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	public void addConfirmedUser(long uid) {
		if(!this.isRemoving()) return;
		this.aWaitingId.remove(uid);
		
		if(this.aWaitingId.size()<=0) {
			this.finalizeRemovalWithoutChecking();
		}
		else {
			try
			{
				this.save();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	private void finalizeRemovalWithoutChecking() {
		User admin = User.findOneAdmin();
		if(admin != null) {
			VenueRemovalFinalized notification = new VenueRemovalFinalized(this);
			Notification.add(admin.getId(), notification);
		}
		try
		{
			this.delete();
			
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	private static Venue createOneFromResultSet(ResultSet result) throws SQLException {
		Venue rtn = new Venue();
		rtn.id = result.getLong("id");
		rtn.name = result.getString("name");
		rtn.findArray("aWaitingId", rtn.aWaitingId);
		return rtn;
	}
	@Override
	public void delete() throws SQLException {
		// also delete appointments
		List<Appointment> aAppt = Appointment.findByVenue(this.getId());
		for(Appointment appt:aAppt) {
			appt.delete();
		}
		
		this.deleteArray("aWaitingId");
		super.delete();
	}
	
}
