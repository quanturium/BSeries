package com.quanturium.bseries.apis;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.quanturium.bseries.json.Json;
import com.quanturium.bseries.json.JsonRoot;
import com.quanturium.bseries.tools.Tools;

public class APIBetaSeries
{

	private static APIBetaSeries	instance;

	private final String			APIKey	= "e3fb35157161";
	private final String			baseURL	= "http://api.betaseries.com/";
	private final String			type	= "json";

	private APIBetaSeries()
	{
	}

	public static APIBetaSeries getInstance()
	{
		if (instance == null)
			instance = new APIBetaSeries();

		return instance;
	}

	private APIBetaSeriesAnswer request(String url, String[] params)
	{
		HttpClient httpclient = new DefaultHttpClient();
		APIBetaSeriesAnswer answer = new APIBetaSeriesAnswer();

		
		try
		{

			Log.i(url, baseURL + url + "." + type + "?key=" + APIKey + (params.length > 0 ? "&" : "") + Tools.implode("&", params));
			HttpGet httpget = new HttpGet(baseURL + url + "." + type + "?key=" + APIKey + (params.length > 0 ? "&" : "") + Tools.implode("&", params));

			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpclient.execute(httpget, responseHandler);

			if (answer != null)
				answer = parseAnswer(responseBody);

		}
		catch (ClientProtocolException e)
		{
			answer.json = new Json();
			answer.json.root = new JsonRoot();

			answer.json.root.code = 0;
			answer.errorNumber = 10;
			answer.errorString = getErrorString(answer.errorNumber);
		}
		catch (IOException e)
		{
			answer.json = new Json();
			answer.json.root = new JsonRoot();

			answer.json.root.code = 0;
			answer.errorNumber = 10;
			answer.errorString = getErrorString(answer.errorNumber);
		}
		catch (OutOfMemoryError e)
		{
			System.gc();
			
			answer.json = new Json();
			answer.json.root = new JsonRoot();

			answer.json.root.code = 0;
			answer.errorNumber = 12;
			answer.errorString = getErrorString(answer.errorNumber);
		}
		finally
		{
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		return answer;
	}

	public APIBetaSeriesAnswer memberSignup(String username, String email, String password)
	{
		String[] params = { "login=" + username, "password=" + password, "mail=" + email };
		return request("members/signup", params);
	}

	public APIBetaSeriesAnswer memberLogin(String username, String password)
	{
		String[] params = { "login=" + username, "password=" + Tools.md5(password) };
		return request("members/auth", params);
	}

	public APIBetaSeriesAnswer memberLogout(String token)
	{
		String[] params = { "token=" + token };
		return request("members/destroy", params);
	}

	public APIBetaSeriesAnswer memberInfos(String token)
	{
		String[] params = { "token=" + token };
		return request("members/infos", params);
	}

	public APIBetaSeriesAnswer showSearch(String query) throws UnsupportedEncodingException
	{
		String[] params = { "title=" + URLEncoder.encode(query, "UTF-8") };
		return request("shows/search", params);
	}

	public APIBetaSeriesAnswer showDisplay(String url, String token)
	{
		String[] params = { "token=" + token };
		return request("shows/display/" + url, params);
	}

	public APIBetaSeriesAnswer showEpisodes(String url, String token, int summary)
	{
		String[] params = { "token=" + token, "summary=" + summary };
		return request("shows/episodes/" + url, params);
	}

	public APIBetaSeriesAnswer showEpisode(String url, String season, String episode, String token, int summary)
	{
		String[] params = { "season=" + season, "episode=" + episode, "token=" + token, "summary=" + summary };
		return request("shows/episodes/" + url, params);
	}
	
	public APIBetaSeriesAnswer commentsEpisode(String url, String season, String episode, String token)
	{
		String[] params = { "season=" + season, "episode=" + episode, "token=" + token};
		return request("comments/episode/" + url, params);
	}

	public APIBetaSeriesAnswer showAdd(String url, String token)
	{
		String[] params = { "token=" + token };
		return request("shows/add/" + url, params);
	}

	public APIBetaSeriesAnswer showRemove(String url, String token)
	{
		String[] params = { "token=" + token };
		return request("shows/remove/" + url, params);
	}

	public APIBetaSeriesAnswer showArchive(String url, String token)
	{
		String[] params = { "token=" + token };
		return request("shows/archive/" + url, params);
	}

	public APIBetaSeriesAnswer showUnarchive(String url, String token)
	{
		String[] params = { "token=" + token };
		return request("shows/unarchive/" + url, params);
	}

	public APIBetaSeriesAnswer memberWatched(String url, String season, String episode, int rate, String token)
	{
		if(rate < 1 || rate > 5)
			rate = 0;
		
		if(rate == 0)
		{
			String[] params = { "token=" + token, "season=" + season, "episode=" + episode };
			return request("members/watched/" + url, params);
		}
		else
		{
			String[] params = { "token=" + token, "season=" + season, "episode=" + episode, "note=" + rate };
			return request("members/watched/" + url, params);
		}				
	}

	public APIBetaSeriesAnswer planningMember(String view, String token)
	{
		String[] params = { "view=" + view, "token=" + token };
		return request("planning/member", params);
	}

	public APIBetaSeriesAnswer memberNotifications(String token)
	{
		String[] params = { "token=" + token };
		return request("members/notifications", params);
	}

	public APIBetaSeriesAnswer memberEpisodes(String subtitles, String view, String token)
	{
		if (view.equals("-1"))
		{
			String[] params = { "token=" + token };
			return request("members/episodes/" + subtitles, params);
		}
		else
		{
			String[] params = { "view=" + view, "token=" + token };
			return request("members/episodes/" + subtitles, params);
		}
	}
	
	public String uriPictureShows(String url)
	{
		return this.baseURL + "pictures/show/" + url + ".jpg?key=" + this.APIKey;
	}

	public APIBetaSeriesAnswer parseAnswer(String data)
	{
		APIBetaSeriesAnswer answer = new APIBetaSeriesAnswer();

		Gson gson = new Gson();
		try
		{
			answer.json = gson.fromJson(data, Json.class);
		}
		catch (NullPointerException e)
		{
			answer.errorNumber = 11;
			answer.errorString = getErrorString(11);
			return answer;
		}
		catch (JsonParseException e)
		{
			answer.errorNumber = 11;
			answer.errorString = getErrorString(11);
			return answer;
		}

		if (answer.json.root.code == 1)
		{
			answer.errorNumber = 0;
			answer.errorString = "none";
		}
		else
		// an error occured
		{
			answer.errorNumber = answer.json.root.errors.error.code;
			answer.errorString = getErrorString(answer.json.root.errors.error.code);
		}

		return answer;
	}

	public String getErrorString(int code)
	{

		String error = "";

		switch (code)
		{
			case 10:
				error = "dix";
			break;
			case 11:
				error = "Erreur temporaire durant le chargement. Veuillez réessayer plus tard.";
			break;
			
			case 12:
				error = "Trop d'épisodes à lister. L'application est passée en mode 'premier' et ne montrera que le dernier épisode non vu pour chaque serie. Veuillez recharger.";
			break;

			case 1001:
				error = "err" + code;
			break;

			case 1002:
				error = "err" + code;
			break;

			case 1003:
				error = "err" + code;
			break;

			case 2001:
				error = "Veuillez vous deconnecter puis vous reconnecter";
			break;

			case 2002:
				error = "err" + code;
			break;

			case 2003:
				error = "err" + code;
			break;

			case 2004:
				error = "err" + code;
			break;

			case 2005:
				error = "err" + code;
			break;

			case 2006:
				error = "err" + code;
			break;

			case 2007:
				error = "err" + code;
			break;

			case 3001:
				error = "err" + code;
			break;

			case 3002:
				error = "Le champ doit contenir plus de 2 caractères";
			break;

			case 3003:
				error = "err" + code;
			break;

			case 3004:
				error = "err" + code;
			break;

			case 3005:
				error = "Caractères non autorisés";
			break;

			case 3006:
				error = "Adresse email invalide";
			break;

			case 4001:
				error = "err" + code;
			break;

			case 4002:
				error = "L'utilisateur n'éxiste pas";
			break;
			case 4003:
				error = "Mot de passe invalide";
			break;

			case 4004:
				error = "Un Compte existe déjà avec ce nom d'utilisateur ou cet adresse e-mail";
			break;
		}

		return error;
	}
}
