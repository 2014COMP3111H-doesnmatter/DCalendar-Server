package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
			escapeHelper = connect.prepareStatement("?");
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
	
	public static void truncate(String tableName) throws SQLException {
		PreparedStatement statement = connect.prepareStatement(" truncate `" + tableName + "` " );
		statement.executeUpdate();
	}
	
	/**
	 * save the Data object into database
	 * @param values col-value pairs
	 */
	protected void save(Map<String,String> values) throws SQLException {
		if(isNew()) {
			throw new SQLException("attemp to save a dangling record");
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
	
	public static <E> void _findArray(String tableName, long id, Collection<E> rtn) throws SQLException {
		ResultSet resultSet =
				Data._find(tableName, "key", String
						.valueOf(id));
		for (int i = 0; resultSet.next(); i++)
		{
			rtn.add((E)resultSet.getObject(2));
		}
	}
	protected <E> void findArray(String fieldName, Collection<E> rtn) throws SQLException {
		Data._findArray(this.tableName + "_" + fieldName, this.getId(), rtn);
	}
	
	public static <E> void _saveArray(String tableName, long id, Collection<E> aValue) throws SQLException {
		List<E> ori = new ArrayList<E>();
		_findArray(tableName, id, ori);
		List<E> toRemove = new ArrayList<E>(ori);
		List<E> toAdd = new ArrayList<E>(aValue);
		toRemove.removeAll(aValue);
		toAdd.removeAll(ori);
		
		// handle toRemove
		if(toRemove.size()>0) {
			StringBuilder statement = new StringBuilder("delete from `");
			statement.append(tableName).append("` where `key` = ")
			.append(String.valueOf(id)).append(" and ( ");
			
			String[] toJoin = new String[toRemove.size()];
			for(int i=0;i<toRemove.size();i++) {
				toJoin[i] = " `value` = " + escapeString(toRemove.get(i).toString()) + " ";
			}
			statement.append(StringUtils.join(toJoin, " or ")).append(" )");
			PreparedStatement query = connect.prepareStatement(statement.toString());
			query.executeUpdate();
		}
		
		
		// handle toAdd
		if(toAdd.size()>0) {
			for(int i=0;i<toAdd.size();i++) {
				PreparedStatement query = connect.prepareStatement("insert into `" + tableName + "` values (?, ?)");
				query.setLong(1, id);
				query.setString(2, toAdd.get(i).toString());
				try{
					query.executeUpdate();
				} catch(SQLException e) {
					e.printStackTrace();
				}
				
			}
		}
		
		
	}
	protected <E> void saveArray(String fieldName, Collection<E> aValue) throws SQLException {
		Data._saveArray(this.tableName + "_" + fieldName, this.getId(), aValue);
	}
	
	private static PreparedStatement escapeHelper = null;
	public static String escapeString(String str) {
		try
		{
			escapeHelper.setString(1, str);
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String rtn = escapeHelper.toString();
		return rtn.substring( rtn.indexOf( ": " ) + 2 );
	}
	
}
