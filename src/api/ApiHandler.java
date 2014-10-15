package api;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.json.*;

import doesnserver.CommonResponses;
import doesnserver.Session;

public class ApiHandler
{
	public boolean requireLogin = false;
	protected String info = "";
	private List<ParamCons> nConstraint = new ArrayList<ParamCons>();
	private Map<Integer, String> rtnCodes = new HashMap<Integer, String>();
	protected class ParamCons {
		private String name;
		private int type;
		private String comment;
		private boolean isOptional;
		public static final int STRING = 0;
		public static final int INTEGER = 1;
	}
	
	public ApiHandler() {
		this.addRtnCode(200, "ok");
	}
	
	public boolean checkParams(Map<String, String> params) {
		Iterator<ParamCons> it = nConstraint.iterator();
		while(it.hasNext()) {
			ParamCons cons = it.next();
			if(!checkOneParam(cons, params.get(cons.name))) return false;
		}
		return true;
	}
	/**
	 * check if a value satisfies a param constraint
	 * @param cons
	 * @param value null if doesn't exist
	 * @return
	 */
	private boolean checkOneParam(ParamCons cons, String value) {
		if(value==null) return cons.isOptional;
		switch(cons.type) {
		case ParamCons.INTEGER:
			return StringUtils.isNumeric(value);
		}
		return true;
	}
	public JSONObject main(Map<String,String> params, Session session) throws Exception {

		return CommonResponses.showNotImplemented();
	};
	protected void addParamConstraint(String name,int type, String comment, boolean isOptional) {
		ParamCons cons = new ParamCons();
		cons.name = name;
		cons.type = type;
		cons.comment = comment;
		cons.isOptional = isOptional;
		this.nConstraint.add(cons);
	}
	protected void addParamConstraint(String name,int type, String comment) {
		this.addParamConstraint(name, type, comment, false);
	}
	protected void addParamConstraint(String name, String comment) {
		this.addParamConstraint(name, ParamCons.STRING, comment, false);
	}
	protected void addParamConstraint(String name,int type) {
		this.addParamConstraint(name, type, "", false);
	}
	protected void addParamConstraint(String name) {
		this.addParamConstraint(name, ParamCons.STRING, "", false);
	}
	protected void addParamConstraint(String name,int type, boolean isOptional) {
		this.addParamConstraint(name, type, "", isOptional);
	}
	protected void addParamConstraint(String name, boolean isOptional) {
		this.addParamConstraint(name, ParamCons.STRING, "", isOptional);
	}
	
	public JSONArray showHelp() throws JSONException {
		JSONArray rtn = new JSONArray();
		
		rtn.put("@description " + this.info);
		
		if(this.requireLogin) {
			rtn.put("REQUIRE LOGIN");
		}
		
		{
			Iterator<ParamCons> it = nConstraint.iterator();
			while(it.hasNext()) {
				ParamCons cons = it.next();
				rtn.put("@param " + showParamHelp(cons));
			}
		}
		{
			Iterator<Entry<Integer,String>> it = rtnCodes.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Integer, String> pair = it.next();
				rtn.put("@returns " + pair.getValue());
				it.remove();
			}
		}
		
		
		return rtn;
	}
	private String showParamHelp(ParamCons cons) {
		StringBuilder rtn = new StringBuilder();
		rtn.append(cons.name);
		rtn.append(' ');
		
		rtn.append('{');
		switch(cons.type) {
		case ParamCons.INTEGER:
			rtn.append("Integer");
			break;
		case ParamCons.STRING:
			rtn.append("String");
			break;
		}
		rtn.append("} ");
		if(cons.isOptional) rtn.append(" [Optional] ");
		rtn.append(cons.comment);
		return rtn.toString();
	}
	protected void addRtnCode(int codeNum, String info) {
		this.rtnCodes.put(codeNum, String.valueOf(codeNum) + " " + info);
	}
	protected String getRtnCode(int codeNum) {
		return this.rtnCodes.get(codeNum);
	}
	
}
