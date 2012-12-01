package com.quanturium.bseries.json;

import java.util.HashMap;

public class JsonEpisode
{
	public int							season;					// 01
	public int							episode;					// 01
	public String						number;					// SO1E02
	public int							global;
	public int							date;						// Timestamp en s
	public String						show;
	public String						url;						// Titre du show unique
	public String						title;						// Titre du show
	public String						description;
	public String						screen;
	public int							comments;
	public int							has_seen;					// 0/1
	public JsonNote						note;
	public boolean						isSeparator		= false;
	public boolean						isViewOnlyOne	= false;	// Par rapport a l'option "onlyone" qui affiche uniquement le dernier episode non vu dans "mes épisodes non vu". Lorsqu'il sera activé, on remplacera l'item par un texte disant de recharger
	public HashMap<Integer, JsonSub>	subs;
}
