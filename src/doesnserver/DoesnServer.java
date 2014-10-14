package doesnserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import db.Data;
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

		try
		{
			// get the handler class by URI
			// for example, "/welcome/login" => "api.welcome.login"
			String uri = request.getUri();

			// handle favicon.ico
			if(uri.equals("/favicon.ico")) {
				return CommonResponses.showFavicon();
			}
			
			Class<?> cls = Class.forName("api" + uri.replaceAll("/", "."));

			// get session from cookie
			Session session = Session.fromNanoCookie(request.getCookies());
			
			
			try
			{
				ApiHandler apiHandler = ((Class<ApiHandler>) cls).newInstance();
				
				// check if login is required
				if(apiHandler.requireLogin && session.getActiveUserId()<=0) {
					
					return new Response(CommonResponses.showUnauthorized().toString());
				}

				// check params
				Map<String, String> params = request.getParms();
				if(!apiHandler.checkParams(params)) {
					return new Response(CommonResponses.showParamError().toString());
				}
				
				// pass the request to the handler
				Response res = new Response(apiHandler.main(params, session).toString());
				res.addHeader("Set-Cookie", session.getCookieForResponse());
				
				return res;
				
			} catch (Exception e)
			{
				return new Response(CommonResponses.showException(e).toString());
			}

		} catch (ClassNotFoundException e)
		{
			// handler class not found
			return new Response(CommonResponses.showNotFound().toString());
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
		System.out.println("Port No: " + PORT);
		Debug.debug();
		ServerRunner.run(DoesnServer.class);

	}

	public DoesnServer()
	{
		super(PORT);
	}
}
