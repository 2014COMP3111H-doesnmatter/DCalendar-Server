package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import doesnserver.Session;


public class Data
{

	public static Connection connect = null; // the sql connection
	
	public static boolean sqlConnect() {
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Properties connectionProps = new Properties();
			connectionProps.put("user", "114509");
			connectionProps.put("password", "cuizheng");
			connect =
					DriverManager
							.getConnection("jdbc:mysql://johnserver0.youmu.moe/doesnmatter?user=doesnmatter&password=122333");
			return true;
		} catch (Exception e)															
		{
			return false;
		}
	}
	private String tableName;
	protected long id = 0;
	protected Data(String tableName) {
		this.tableName = tableName;
	}
	public Data() {
		this.tableName = this.getClass().getSimpleName();
	}
	public boolean isNew() {
		return id == 0;
	}
	public long getId() {
		return id;
	}
	public String getTableName() {
		return tableName;
	}

	public static ResultSet _find(String tableName, String field, String value) throws SQLException {
		PreparedStatement statement = connect.prepareStatement(" select * from `" + tableName + "` where `" + field + "` = ? " );
		statement.setString(1, value);
		ResultSet result = statement.executeQuery();
		return result;
	}
	
	public static ResultSet _findAll(String tableName) throws SQLException {
		PreparedStatement statement = connect.prepareStatement(" select * from `" + tableName + "`" );
		ResultSet result = statement.executeQuery();
		return result;
	}
	
	public void delete() throws SQLException {
		if(this.isNew()) {
			return;
		}
		PreparedStatement statement = connect.prepareStatement(" delete from `" + tableName + "` where `id` = " + id );
		statement.executeUpdate();
		this.id = 0;
	}
	
	/**
	 * save the Data object into database
	 * @param values col-value pairs
	 * @throws Exception
	 */
	protected void save(Map<String,String> values) throws Exception {
		if(isNew()) {
			throw new Exception("attempt to save a dangling entry");
		}
		
		int nColumn = values.size();
		String[] aPiece = new String[nColumn];
		String[] aValue = new String[nColumn];
		
		Iterator<Entry<String,String>> it = values.entrySet().iterator();
		for(int i=0; it.hasNext(); i++) {
			Entry pairs = it.next();
			aPiece[i] = " `" + (String)pairs.getKey() + "` = ? ";
			aValue[i] = (String)pairs.getValue();
			it.remove(); // avoids a ConcurrentModificationException
		}

		// update `$tableName` set `col1` = ? , `col2` = ?, ... where `id` = $id
		PreparedStatement statement = connect.prepareStatement(
				" update `" + tableName + "` set "
				+ StringUtils.join(aPiece, ",")
				+ "where `id` = " + this.id
				);
		
		// fill values
		for(int i=0; i<nColumn; i++) {
			statement.setString(i+1, aValue[i]);
		}
		
		// execute query
		statement.executeUpdate();
		
	}
	
	/**
	 * create a database entry. 
	 * @param holder a Data object to hold the created data. the generated id will be put inside this object
	 * @param values col-value pairs
	 * @throws SQLException
	 */
	public static <D extends Data> void create(D holder, Map<String, String> values) throws SQLException {
		int nColumn = values.size();
		
		String[] aColumn = new String[nColumn];
		String[] aValue = new String[nColumn];
		
		// get column and value array
		Iterator<Map.Entry<String, String>> it = values.entrySet().iterator();
	    for (int i=0; it.hasNext(); i++) {
	        Map.Entry pairs = it.next();
	        aColumn[i] = "`" + (String)pairs.getKey() + "`";
	        aValue[i] = (String)pairs.getValue();
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
	    
	    // create a pattern of (col1, col2, col3)
	    String colJoined = "(" + StringUtils.join(aColumn, ",") + ")";
		
		// create a pattern of (?,?,?...)
		String[] toJoin = new String[nColumn];
		Arrays.fill(toJoin, " ?");
		String joined = "(" + StringUtils.join(toJoin, ",") + ")";
		
		// insert into tablename (col1,col2,col3...) values (?,?,?...)
		PreparedStatement statement = connect
		          .prepareStatement("insert into `" + holder.getTableName() + "` " + colJoined + " values " + joined, Statement.RETURN_GENERATED_KEYS);
		
		// fill values
		for(int i=0; i<nColumn; i++) {
			statement.setString(i+1, aValue[i]);
		}
	    
	    // execute query
	    statement.executeUpdate();
	    
	    
	    // get generated key
	    ResultSet generatedKeys = statement.getGeneratedKeys();
	    if (generatedKeys.next()) {
            holder.id = generatedKeys.getLong(1);
        }
        else {
            throw new SQLException("Creating user failed, no ID obtained.");
        }
	}
	
}
