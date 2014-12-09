package doesnserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import db.Data;
import doesnserver.notification.Notification;
import api.ApiHandler;
import nanohttpd.*;

public class DoesnServer extends NanoHTTPD
{
	public static int PORT = 8080; // port number

	/**
	 * handle request
	 */
	@Override
	public Response serve(IHTTPSession request) {
		String uri = request.getUri();

		// handle favicon.ico
		if(uri.equals("/favicon.ico")) {
			return CommonResponses.showFavicon();
		}
		Session session = Session.fromNanoCookie(request.getCookies());
		String responseString = this.serveAsJson(request, session).toString();
		Response res = new Response(responseString);
		
		// write log
		this.logRequest(uri, responseString);
		
		res.addHeader("Set-Cookie", session.getCookieForResponse());
		res.addHeader("Access-Control-Allow-Origin", "*");
		return res;

	}
	
	private void logRequest(String uri, String res) {
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true)));
		    out.println(uri);
		    out.println(res);
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private JSONObject serveAsJson(IHTTPSession request, Session session) {
		try
		{
			// get the handler class by URI
			// for example, "/welcome/login" => "api.welcome.login"
			String uri = request.getUri();
			
			Class<?> cls = Class.forName("api" + uri.replaceAll("/", "."));
			
			
			try
			{
				ApiHandler apiHandler = ((Class<ApiHandler>) cls).newInstance();
				
				// get params
				Map<String, String> files = new HashMap<String, String>();
			    Method method = request.getMethod();
			    if (Method.PUT.equals(method) || Method.POST.equals(method)) {
			    	request.parseBody(files);
			    }
			    Map<String, String> params = request.getParms();
			    
				// give some help text
				if(params.containsKey("_help")) {
					JSONObject rtn = new JSONObject();
					rtn.put("rtnCode", "200 ok");
					rtn.put("help", apiHandler.showHelp());
					return rtn;
				}
				
				// check if login is required
				if(apiHandler.requireLogin && session.getActiveUserId()<=0) {
					
					return CommonResponses.showUnauthorized();
				}
				
				// check params
				
				if(!apiHandler.checkParams(params)) {
					JSONObject rtn = CommonResponses.showParamError();
					rtn.put("help", apiHandler.showHelp());
					return rtn;
				}
				
				// pass the request to the handler
				return apiHandler.main(params, session);
				
			} catch (Exception e)
			{
				return CommonResponses.showException(e);
			}

		} catch (ClassNotFoundException e)
		{
			// handler class not found
			return CommonResponses.showNotFound();
		}
	}
	public static String formatHtml(String s) {
		return s.toString().replaceAll("\n", "<br>").replaceAll("\t",
				"&nbsp;&nbsp;&nbsp;&nbsp;");
	}

	
	
	/**
	 * start the server
	 */
	public static void main(String[] args) {
		System.out.print("Connecting to sql server...");
		if(!Data.sqlConnect()) {
			System.out.println("[failed]");
			return;
		}
		System.out.println("[ok]");
		System.out.print("Starting notification daemon...");
		Notification.start_daemon();
		System.out.println("[ok]");
		System.out.println("Port No: " + PORT);
		Debug.debug();
		ServerRunner.run(DoesnServer.class);

	}

	public DoesnServer()
	{
		super(PORT);
	}
}
