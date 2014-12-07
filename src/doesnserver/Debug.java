package doesnserver;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import db.Appointment;
import db.Data;
import db.User;
import doesnserver.notification.UserRemovalFinalized;
import doesnserver.notification.UserRemovalInitiated;
import doesnutil.DateUtil;
import doesnutil.WrapperUtil;

public class Debug
{
	public static void debug() {
		
		try
		{
			User u = User.findById(1L);
			UserRemovalFinalized n1 = new UserRemovalFinalized(u);
			UserRemovalInitiated n2 = new UserRemovalInitiated(u);
			System.out.println(n1.equals(n2));
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
