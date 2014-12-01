package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Venue extends Data
{
	public String name;
	public int capacity;
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
	public static Venue create(String name) throws SQLException {
		Venue rtn = new Venue();
		Map<String,String> values = new HashMap<String,String>();
		values.put("name", name);
		Data.create(rtn, values);
		rtn.name = name;
		return rtn;
	}
	public JSONObject toJson() {
		JSONObject rtn = new JSONObject();
		try
		{
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
