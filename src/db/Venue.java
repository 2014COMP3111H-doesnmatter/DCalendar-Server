package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

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
		rtn.id = result.getLong("id");
		rtn.name = result.getString("name");
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

}
