/*
 * Copyright 2005-2011 Tony Locke
 * 
 * This file is part of Imprimatur.
 * 
 * Imprimatur is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * Imprimatur is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Imprimatur; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package uk.org.tlocke.imprimatur;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Request extends Common {
	private String method;

	private String path;

	private HttpResponse response;

	private File bodyFile = null;

	private String responseStr;

	private List<Control> controls = new ArrayList<Control>();

	private URI uri;

	private NodeList responseCodeElements;

	private Element refreshElement = null;

	private NodeList regexpElements;

	private NodeList headerElements;

	private NodeList credentialsElements;

	private boolean followRedirects = false;

	public Request(Test test, Element request, File scriptFile)
			throws Exception {
		super(test, request, scriptFile);
		method = request.getAttribute("method");
		path = request.getAttribute("path");
		if (request.getAttribute("follow-redirects").equals("true")) {
			followRedirects = true;
		}
		String bodyFileName = request.getAttribute("body-file");

		if (bodyFileName.length() != 0) {
			bodyFile = new File(bodyFileName);
			if (!bodyFile.isAbsolute()) {
				bodyFile = new File(getScriptFile().getParent()
						+ File.separator + bodyFile.toString());
			}
			if (!bodyFile.exists()) {
				throw new Exception("The request body file '"
						+ bodyFile.toString() + "' does not exist.");
			}
		}
		NodeList controlElements = request.getElementsByTagName("control");
		for (int i = 0; i < controlElements.getLength(); i++) {
			controls.add(new Control((Element) controlElements.item(i)));
		}
		responseCodeElements = request.getElementsByTagName("response-code");
		NodeList refreshElements = request.getElementsByTagName("refresh");
		if (refreshElements.getLength() > 0) {
			refreshElement = (Element) refreshElements.item(0);
		}
		regexpElements = request.getElementsByTagName("regex");
		if (method == null || method.length() == 0) {
			method = "get";
		}
		headerElements = request.getElementsByTagName("header");
		credentialsElements = request.getElementsByTagName("credentials");
	}

	void process() throws Exception {
		super.process();
		if (credentialsElements.getLength() > 0) {
			Element credentialsElement = (Element) credentialsElements.item(0);
			getHttpClient()
					.getCredentialsProvider()
					.setCredentials(
							new AuthScope(getHostname(), getPort(),
									AuthScope.ANY_REALM),
							new UsernamePasswordCredentials(credentialsElement
									.getAttribute("username"),
									credentialsElement.getAttribute("password")));
		}
		uri = URIUtils.createURI(getScheme(), getHostname(), getPort(), path, null, null);
		System.out.println("Request: '" + uri.toString() + "'.");
		int maxTries = 1;
		long delay = 0;
		if (refreshElement != null) {
			delay = Long.parseLong(refreshElement.getAttribute("wait"));
			maxTries = Integer.parseInt(refreshElement.getAttribute("max"));
		}
		int i = 0;
		boolean hasSucceeded = false;
		Exception exception = null;
		while (i < maxTries && !hasSucceeded) {
			try {
				processOnce();
				hasSucceeded = true;
			} catch (Exception e) {
				exception = e;
			}
			Thread.sleep(delay);
			i++;
		}
		if (!hasSucceeded) {
			throw exception;
		}
	}

	private void processOnce() throws Exception {
		HttpRequestBase httpMethod = null;
		if (method.equals("post")) {
			httpMethod = post();
		} else if (method.equals("get")) {
			httpMethod = new HttpGet();
		} else if (method.equals("delete")) {
			httpMethod = new HttpDelete();
		} else if (method.equals("put")) {
			httpMethod = put();
		} else if (method.equals("head")) {
			httpMethod = new HttpHead();
		} else {
			throw new UserException("Unknown request method type: " + method);
		}

		HttpParams params = httpMethod.getParams();
		HttpClientParams.setRedirecting(params, followRedirects);
		httpMethod.setParams(params);
		for (int i = 0; i < headerElements.getLength(); i++) {
			Element headerElement = (Element) headerElements.item(i);
			httpMethod.addHeader(headerElement.getAttribute("name"),
					headerElement.getAttribute("value"));
		}
		httpMethod.setURI(uri);
		response = getHttpClient().execute(httpMethod);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		/*
		if (followRedirects
				&& (statusCode == HttpServletResponse.SC_SEE_OTHER || statusCode == HttpServletResponse.SC_MOVED_TEMPORARILY)) {
			String location = response.getFirstHeader("Location")
					.getValue();
			Debug.print("Location: " + location);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				EntityUtils.consume(entity);
			}
			httpMethod = new HttpGet(location);
			response = session.getHttpClient().execute(httpMethod);
		}
		*/
		Logger logger = Logger.getLogger("org.apache.commons.httpclient");
		logger.setLevel(Level.SEVERE);
		StringBuilder responseBuilder = new StringBuilder();
		
		for (Header header : response.getAllHeaders()) {
			responseBuilder.append(header.toString() + "\r\n");
		}
		responseBuilder.append("\r\n");
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			responseBuilder.append(EntityUtils.toString(entity));
		}

		responseStr = responseBuilder.toString();
		//Debug.print(responseStr);
		logger.setLevel(Level.ALL);

		if (responseCodeElements.getLength() > 0) {
			Element responseCodeElement = (Element) responseCodeElements
					.item(0);
			int desiredResponseCode = Integer.parseInt(responseCodeElement
					.getAttribute("value"));
			if (statusCode != desiredResponseCode) {
				throw new UserException("Failed response code check.\n"
						+ "	desired response code: " + desiredResponseCode
						+ "\n Actual response code: " + statusCode + "\n"
						+ "  Actual response body:\n" + responseStr);
			}
		}

		for (int i = 0; i < regexpElements.getLength(); i++) {
			Element regexpElement = (Element) regexpElements.item(i);
			String patternStr = regexpElement.getAttribute("pattern");
			if (!Pattern.compile(patternStr, Pattern.DOTALL).matcher(responseStr)
					.find()) {
				throw new UserException("Failed regexp check: '" + patternStr
						+ "'. Response:\n" + responseStr);
			}
		}
	}

	private HttpPost post() throws Exception {
		HttpPost method = new HttpPost();

		if (bodyFile == null) {
			boolean hasFile = false;
			for (Control control : controls) {
				if (control.getType().equals("file")) {
					hasFile = true;
					break;
				}
			}
			if (hasFile) {
				MultipartEntity me = new MultipartEntity();
				for (Control control : controls) {
					if (control.getType().equals("file")) {
						File fileToUpload = new File(control.getValue());
						if (!fileToUpload.isAbsolute()) {
							fileToUpload = new File(getScriptFile().getParent()
									+ File.separator + fileToUpload.toString());
						}
						if (!fileToUpload.exists()) {
							throw new Exception("The file '"
									+ fileToUpload.toString()
									+ "' does not exist.");
						}
						me.addPart(control.getName(), new FileBody(fileToUpload));
					} else {
						me.addPart(control.getName(), new StringBody(control
								.getValue()));
					}
				}
				method.setEntity(me);
			} else {
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				for (Control control : controls) {
					nvps.add(new BasicNameValuePair(control.getName(), control.getValue()));
				}
				method.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			}
		} else {
			method.setEntity(new FileEntity(bodyFile, null));
		}
		return method;
	}

	private HttpPut put() throws Exception {
		HttpPut method = new HttpPut();
		if (bodyFile != null) {
			method.setEntity(new FileEntity(bodyFile, null));
		}
		return method;
	}
}