package com.vladimirionescu.codejam.plugin.client;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CodejamClient {

	private static final DateFormat cookieDateFormat = new SimpleDateFormat(
			"EEE, DD-MMM-yyyyy HH:mm:ss z");
	private static final DateFormat tokensDateFormat = new SimpleDateFormat(
			"yyyy-MM-DD HH:mm:ss");

	private static Client client = Client.create();

	private class ConnectionData implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8057065213698135699L;
		String authToken;
		String cookie;
		Date cookieExpirationDate;
		String middlewareToken;
		Date middlewareTokenExpirationDate;

		public Date getMiddlewareTokenExpirationDate() {
			return middlewareTokenExpirationDate;
		}

		public void setMiddlewareTokenExpirationDate(
				Date middlewareTokenExpirationDate) {
			this.middlewareTokenExpirationDate = middlewareTokenExpirationDate;
		}

		public void setCookieExpirationDate(Date cookieExpirationDate) {
			this.cookieExpirationDate = cookieExpirationDate;
		}

		public void setAuthToken(String authToken) {
			this.authToken = authToken;
		}

		public String getAuthToken() {
			return this.authToken;
		}

		public void setCookie(String cookie) {
			this.cookie = cookie;
		}

		public String getCookie() {
			return this.cookie;
		}

		public Date getCookieExpirationDate() {
			return this.cookieExpirationDate;
		}

		public void setMiddlewareToken(String middlewareToken) {
			this.middlewareToken = middlewareToken;
		}

		public String getMiddlewareToken() {
			return middlewareToken;
		}

	}

	private ConnectionData connectionData = null;

	private static class InstanceHolder {
		private static final CodejamClient codejamClient = new CodejamClient();
	}

	private CodejamClient() {

	}

	public static CodejamClient getCodeJamClient() {
		return InstanceHolder.codejamClient;
	}

	public boolean needsLogin() {
		if(connectionData == null)
		{
			return true;
		}
		if(connectionData.getCookieExpirationDate().before(new Date(System.currentTimeMillis())))
		{
			return true;
		}
		return false;
	}


	public void login(String user, String password) {
		try {
			if (connectionData == null) {
				connectionData = new ConnectionData();
			}
			WebResource clientLoginResource = client
					.resource("https://www.google.com/accounts/ClientLogin");
			MultivaluedMap<String, String> requestData = new MultivaluedMapImpl();
			requestData.add("accountType", "HOSTED_OR_GOOGLE");
			requestData.add("Email", user);
			requestData.add("Passwd", password);
			requestData.add("service", "ah");
			requestData.add("source", "codejam-plugin-0.1_beta");
			ClientResponse response = clientLoginResource.post(
					ClientResponse.class, requestData);
			// TODO error treatment
			String entity = response.getEntity(String.class);
			String token = null;
			for (String line : entity.split("\n")) {
				if (line.startsWith("Auth")) {
					token = line.split("=")[1];
				}
			}
			System.out.println("Auth token: " + token);
			connectionData.setAuthToken(token);

			WebResource cookieResource = client
					.resource("https://code.google.com/_ah/login");
			response = cookieResource.queryParam("auth", token)
					.queryParam("continue", "http://localhost/")
					.get(ClientResponse.class);

			String cookie = response.getHeaders().getFirst("Set-Cookie");
			System.out.println("Cookie: " + cookie);
			for (String cookieField : cookie.split("; ")) {
				if (cookieField.startsWith("expires")) {
					Date cookieExpirationDate = cookieDateFormat
							.parse(cookieField.split("=")[1]);
					System.out.println(cookieExpirationDate);
					connectionData
							.setCookieExpirationDate(cookieExpirationDate);
				}
			}
			connectionData.setCookie(cookie);

			retrieveMiddlewareToken();
			
			
		} catch (ParseException e) {
			//TODO
			e.printStackTrace();
		}
	}
	
	private void checkMiddlewareToken()
	{
		if(connectionData.getMiddlewareToken() != null || (connectionData.getMiddlewareTokenExpirationDate() != null && connectionData.getMiddlewareTokenExpirationDate().before(new Date(System.currentTimeMillis()))))
		{
			retrieveMiddlewareToken();
		}
	}

	private void retrieveMiddlewareToken() {
		WebResource middlewareResource = client
				.resource("https://code.google.com/codejam/middleware");

		ClientResponse response = middlewareResource
				.queryParam("cmd", "GetMiddlewareTokens")
				.queryParam("actions", "SubmitAnswer")
				.header("Referer", "https://code.google.com/codejam")
				.header("Cookie", connectionData.getCookie())
				.get(ClientResponse.class);

		try {
			JSONObject json = new JSONObject(response.getEntity(String.class));
			System.out.println(json);
			Date tokenExpirationDate = tokensDateFormat.parse(json
					.getString("expire"));
			System.out.println(tokenExpirationDate);
			String middlewareToken = json.getJSONObject("tokens").getString(
					"SubmitAnswer");
			System.out.println(middlewareToken);
			connectionData.setMiddlewareToken(middlewareToken);
			connectionData
					.setMiddlewareTokenExpirationDate(tokenExpirationDate);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void downloadInput(String problemId, String inputId, File input) {
		checkMiddlewareToken();
		//TODO
	}

	public void uploadSolution(String problemId, String inputId, File solution) {
		checkMiddlewareToken();
		//TODO
	}

	public void uploadSourceCode(String problemId, String inputId,
			File sourceCode) {
		checkMiddlewareToken();
		//TODO
	}

	public void getContestData(String contestId) {
		checkMiddlewareToken();
		//TODO
		/*
		request_referer = 'http://{0}/codejam/contest/dashboard?c={1}'.format(
			      host, contest_id)
			  request_arguments = {
			      'cmd': 'GetProblems',
			      'contest': contest_id,
			      }
			  request_headers = {
			      'Referer': request_referer,
			      'Cookie': cookie,
			      }
			  try:
			    status, reason, response = http_interface.Get(
			        host, '/codejam/contest/dashboard/do', request_arguments,
			        request_headers)
		 */
		WebResource codejamDashboardDo = client.resource("https://code.google.com/codejam/contest/dashboard/do");
		String referer = "http://code.google.com/codejam/contest/dashboard?c=" + contestId;
		ClientResponse response = codejamDashboardDo.queryParam("cmd", "GetProblems")
							.queryParam("contest", contestId)
							.header("cookie", connectionData.getCookie())
							.header("Referer", referer)
							.get(ClientResponse.class);
		
		System.out.println(response.getEntity(String.class));
	}
	
	public static void main(String args[])
	{
		
	}
	


}
