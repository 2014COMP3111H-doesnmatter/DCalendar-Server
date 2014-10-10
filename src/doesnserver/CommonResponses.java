package doesnserver;

import java.io.PrintWriter;
import java.io.StringWriter;

import nanohttpd.NanoHTTPD;
import nanohttpd.NanoHTTPD.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class CommonResponses
{

	public static Response showFavicon() {
		return new Response("no icon to display");
	}
	
	public static JSONObject showException(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("rtnCode", "500 internal server error");
			String[] aSplitted = sw.toString().split("\n");
			for(int i=0; i<aSplitted.length; i++) {
				jo.append("stackTrace", aSplitted[i]);
			}
		} catch (JSONException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return jo;
	}
	
	public static JSONObject showNotFound() {
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("rtnCode", "404 not found");
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
		
	}
	
	public static JSONObject showParamError() {
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("rtnCode", "400 param error");
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}
	
	public static JSONObject showUnauthorized() {
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("rtnCode", "401 unauthorized");
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}
	
	public static JSONObject showNotImplemented() {
		JSONObject jo = new JSONObject();
		try
		{
			jo.put("rtnCode", "501 not implemented");
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}
}
