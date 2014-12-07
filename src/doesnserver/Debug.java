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
import doesnutil.DateUtil;
import doesnutil.WrapperUtil;

public class Debug
{
	public static void debug() {
		
		try
		{
			long[] arr = new long[]{1L,2L,4L};
			System.out.println(Arrays.toString(arr));
			List<Long> list = new ArrayList<Long>();
			WrapperUtil.toCollection(arr, list);
			System.out.println(list.toString());
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
