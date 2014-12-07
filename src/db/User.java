package db;

import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.damien.miller.Bcrypt.BCrypt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import doesnserver.notification.Notification;
import doesnserver.notification.UserRemovalFinalized;
import doesnserver.notification.UserRemovalInitiated;
import doesnutil.EncodeUtil;

public class User extends Data
{
	protected User()
	{
		super(User.class.getSimpleName());

	}

	public String username;
	private String passwordHashed;
	private boolean isRemoving;
	public boolean isAdmin;
	public String fullName;
	public String email;

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
			return User.createOneFromResultSet(result);
		}
		else {
			return null;
		}
	}
	
	public static List<User> list() throws SQLException {
		ResultSet result = Data._findAll(User.class.getSimpleName());
		List<User> rtn = new ArrayList<User>();
		while(result.next()) {
			rtn.add(User.createOneFromResultSet(result));
		}
		return rtn;
	}
	
	public static JSONArray listById(long[] aId) {
		JSONArray ja = new JSONArray();
		for(long l:aId) {
			User user = User.findById(l);
			if(user != null) ja.put(user.toJson());
		}
		return ja;
	}
	
	private static User createOneFromResultSet(ResultSet result) throws SQLException {
		User rtn = new User();
		rtn.id = result.getLong("id");
		rtn.username = result.getString("username");
		rtn.passwordHashed = result.getString("passwordHashed");
		rtn.isRemoving = result.getInt("isRemoving")==1;
		rtn.isAdmin = result.getInt("isAdmin")==1;
		rtn.fullName = result.getString("fullName");
		rtn.email = result.getString("email");
		return rtn;
	}
	
	/**
	 * find a user by id
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static User findById(long id){
		try
		{
			return findOne("id", String.valueOf(id));
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
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
	public static User create(String username, String passwordClear, String fullName, String email) throws SQLException {
		String passwordHashed = hashPassword(passwordClear); // hash the password

		User rtn = new User();
		rtn.username = username;
		rtn.passwordHashed = passwordHashed;
		rtn.isAdmin = false;
		rtn.fullName = fullName;
		rtn.email = email;
		rtn.save();
		
		return rtn;
	}

	/**
	 * save all modifications
	 * @throws Exception 
	 */
	public void save() throws SQLException {
		
		// create values
		Map<String, String> values = new HashMap<String, String>();
		values.put("username", username);
		values.put("passwordHashed", passwordHashed);
		values.put("isAdmin", this.isAdmin?"1":"0");
		values.put("isRemoving", this.isRemoving?"1":"0");
		values.put("fullName", this.fullName);
		values.put("email", this.email);
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
			jo.put("isAdmin", this.isAdmin);
			jo.put("email", this.email);
			jo.put("fullName", this.fullName);
			return jo;
		} catch (JSONException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isRemoving() {
		return this.isRemoving;
	}
	public void initiateRemoval() {
		if(this.isRemoving) return;
		this.isRemoving = true;
		try
		{
			this.save();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		UserRemovalInitiated notification = new UserRemovalInitiated(this);
		Notification.add(this.getId(), notification);
	}
	
	public void finalizeRemoval() {
		if(!this.isRemoving) return;
		this.isRemoving = false;
		try
		{
			UserRemovalFinalized notification = new UserRemovalFinalized(this);
			User admin = User.findOneAdmin();
			if(admin != null) Notification.add(admin.getId(), notification);
			this.delete();
			
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	public static User findOneAdmin() {
		try
		{
			return User.findOne("isAdmin", "1");
		} catch (SQLException e)
		{
			
			e.printStackTrace();
			return null;
		}
	}
}
