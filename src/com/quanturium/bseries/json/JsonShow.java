package com.quanturium.bseries.json;

import java.util.HashMap;

public class JsonShow
{
	public String							title;
	public int								id_thetvdb;
	public String							url;
	public String							description;
	public String							status;
	public String							banner;
	public int								archive;
	public int								is_in_account;
	public boolean							isAlreadyInTheUserAccount	= false;
	public HashMap<Integer, JsonSeason2>	seasons						= new HashMap<Integer, JsonSeason2>();
}
