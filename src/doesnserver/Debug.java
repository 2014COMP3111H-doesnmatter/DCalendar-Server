package doesnserver;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import db.Appointment;
import db.Data;
import db.User;
import doesnutil.DateUtil;

public class Debug
{
	public static void debug() {
		try
		{
			
			Appointment a = Appointment.findById(3);
			
			System.out.println(a.aAcceptedId);
			
			
			a.aAcceptedId.remove(2L);
			a.aAcceptedId.add(4L);
			System.out.println(a.aAcceptedId);

			a.save();
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
