package api.venue;

import api.ApiHandler;
import api.ApiHandler;

public class edit extends ApiHandler
{

	public edit()
	{
		this.addParamConstraint("venueId", ParamCons.INTEGER);
		this.addParamConstraint("name", true);
		this.addParamConstraint("capacity", ParamCons.INTEGER, true);
		this.addRtnCode(201, "already exists");
	}

}
