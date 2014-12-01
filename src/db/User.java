package db;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.damien.miller.Bcrypt.BCrypt;
import org.json.JSONException;
import org.json.JSONObject;

import doesnutil.EncodeUtil;

public class User extends Data
{
	protected User()
	{
		super(User.class.getSimpleName());

	}

	public String username;
	private String passwordHashed;

	/**
	 * find a user by a field
	 * 
	 * @param field
	 * @param value
	 * @return the user
	 * @throws Exception 
	 */
	public static User findOne(String field, String value) throws SQLException {
		ResultSet result = Data._find(User.class.getSimpleName(), field, value);
		if(result.next()) {
			User rtn = new User();
			rtn.id = result.getLong("id");
			rtn.username = result.getString("username");
			rtn.passwordHashed = result.getString("passwordHashed");
			return rtn;
		}
		else {
			return null;
		}
	}
	
	/**
	 * find a user by id
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static User findById(long id) throws SQLException {
		return findOne("id", String.valueOf(id));
	}

	/**
	 * hash the password
	 * 
	 * @param passwordClear
	 *            the clear password
	 * @return the hashed password
	 */
	public static String hashPassword(String passwordClear) {
		return BCrypt.hashpw(passwordClear, BCrypt.gensalt());
	}

	/**
	 * check if the password is correct
	 * 
	 * @param passwordClear
	 * @return check result
	 */
	public boolean checkPassword(String passwordClear) {
		return BCrypt.checkpw(passwordClear, this.passwordHashed);
	}
	
	/**
	 * set a new password
	 * @param passwordClear
	 */
	public void setPassword(String passwordClear) {
		this.passwordHashed = User.hashPassword(passwordClear);
	}

	/**
	 * create a user into database
	 * 
	 * @param username
	 * @param passwordClear
	 * @return the newly created user
	 * @throws SQLException 
	 */
	public static User create(String username, String passwordClear) throws SQLException {
		String passwordHashed = hashPassword(passwordClear); // hash the password

		// create values
		Map<String, String> values = new HashMap<String, String>();
		values.put("username", username);
		values.put("passwordHashed", passwordHashed);

		// construct return user
		User rtn = new User();
		Data.create(rtn, values);
		rtn.username = username;
		rtn.passwordHashed = passwordHashed;
		return rtn;
	}

	/**
	 * save all modifications
	 * @throws Exception 
	 */
	public void save() throws Exception {
		
		// create values
		Map<String, String> values = new HashMap<String, String>();
		values.put("username", username);
		values.put("passwordHashed", passwordHashed);
		
		// save
		super.save(values);
	}
	
	public String toString() {
		JSONObject jo = new JSONObject();
		
		try
		{
			
			jo.put("id", getId());
			jo.put("username", username);
			jo.put("passwordHashed", passwordHashed);
			return jo.toString();
		} catch (JSONException e)
		{
			e.printStackTrace();
			return super.toString();
		}
	}
	
	public JSONObject toJson() {
		JSONObject jo = new JSONObject();
		
		try
		{
			
			jo.put("id", getId());
			jo.put("username", username);
			return jo;
		} catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
