package com.quanturium.bseries.json;

import java.util.HashMap;

public class JsonRoot
{
	public int									code;
	public JsonErrors							errors;
	public String								key;
	public JsonMember							member;
	public JsonShow								show;
	public HashMap<Integer, JsonSeason>			seasons;
	public HashMap<Integer, JsonEpisode>		planning;
	public HashMap<Integer, JsonShow>			shows;
	public HashMap<Integer, JsonEpisode>		episodes;
	public HashMap<Integer, JsonComment>		comments;
	public HashMap<Integer, JsonNotification>	notifications;
}
