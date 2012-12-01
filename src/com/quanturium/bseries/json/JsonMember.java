package com.quanturium.bseries.json;

import java.util.HashMap;

public class JsonMember
{
	public String						login;
	public String						token;
	public String						password;
	public String						avatar;
	public JsonStats					stats;
	public HashMap<Integer, JsonShow>	shows;
	public JsonOptions					options;
}
