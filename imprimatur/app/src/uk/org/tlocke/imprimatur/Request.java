/*
 * Copyright 2005 Tony Locke
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Request extends Common {

	Session session;

	String method;

	String path;

	String enctype;

	int status;

	String responseBody;

	NodeList parameterElements;

	URI uri;

	NodeList responseCodeElements;

	Element refreshElement = null;

	NodeList regexpElements;

	public Request(Session session, Element request, File scriptFile)
			throws Exception {
		super(session, request, scriptFile);
		this.session = session;
		method = request.getAttribute("method");
		path = request.getAttribute("path");
		enctype = request.getAttribute("enctype");
		parameterElements = request.getElementsByTagName("parameter");
		responseCodeElements = request.getElementsByTagName("response-code");
		NodeList refreshElements = request.getElementsByTagName("refresh");
		if (refreshElements.getLength() > 0) {
			refreshElement = (Element) refreshElements.item(0);
		}
		regexpElements = request.getElementsByTagName("regex");
		if (method == null || method.length() == 0) {
			method = "get";
		}
		if (enctype == null || enctype.length() == 0) {
			enctype = "application/x-www-form-urlencoded";
		}
	}

	void process() throws Exception {
		super.process();
		uri = new URI("http", null, getHostname(), getPort(), path);
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
		HttpMethod httpMethod = null;
		if (method.equals("post")) {
			httpMethod = post();
		} else if (method.equals("get")) {
			httpMethod = get();
		} else if (method.equals("delete")) {
			httpMethod = delete();
		} else {
			throw new UserException("Unknown request method type: " + method);
		}
		httpMethod.releaseConnection();

		if (responseCodeElements.getLength() > 0) {
			Element responseCodeElement = (Element) responseCodeElements
					.item(0);
			int desiredResponseCode = Integer.parseInt(responseCodeElement
					.getAttribute("value"));
			if (status != desiredResponseCode) {
				throw new UserException("Failed response code check.\n"
						+ "	desired response code: " + desiredResponseCode
						+ "\n Actual response code: " + status + "\n"
						+ "  Actual response body:\n" + responseBody);
			}
		}
		String responseBodyNoBreaks = responseBody.replaceAll("\\p{Cntrl}", "");
		for (int i = 0; i < regexpElements.getLength(); i++) {
			Element regexpElement = (Element) regexpElements.item(i);
			String pattern = regexpElement.getAttribute("pattern");
			if (!responseBodyNoBreaks.matches(pattern)) {
				throw new UserException("Failed regexp check: '" + pattern
						+ "'. Response:\n" + responseBody);
			}
		}
	}

	private HttpMethod post() throws Exception {
		PostMethod post = new PostMethod();
		// post.setFollowRedirects(true);
		post.setURI(uri);
		if (enctype.equals("application/x-www-form-urlencoded")) {
			for (int k = 0; k < parameterElements.getLength(); k++) {
				Element parameterElement = (Element) parameterElements.item(k);
				post.setParameter(parameterElement.getAttribute("name"),
						parameterElement.getTextContent());
			}
		} else if (enctype.equals("multipart/form-data")) {
			List<Part> partsList = new ArrayList<Part>();

			for (int k = 0; k < parameterElements.getLength(); k++) {

				Element parameterElement = (Element) parameterElements.item(k);
				String parameterName = parameterElement.getAttribute("name");
				String parameterValue = parameterElement.getTextContent();
				if (parameterElement.getAttribute("type").equals("file")) {
					File fileToUpload = new File(parameterValue);
					if (!fileToUpload.isAbsolute()) {
						fileToUpload = new File(getScriptFile().getParent()
								+ File.separator + fileToUpload.toString());
						// System.out.print(fileToUpload);
					}
					partsList.add(new FilePart(parameterName, fileToUpload));
				} else {
					partsList
							.add(new StringPart(parameterName, parameterValue));
				}

			}
			Part[] parts = new Part[partsList.size()];
			for (int j = 0; j < partsList.size(); j++) {
				parts[j] = (Part) partsList.get(j);
			}
			post.setRequestEntity(new MultipartRequestEntity(parts, post
					.getParams()));
		}
		status = session.getHttpClient().executeMethod(post);
		responseBody = getResponseBody(post);
		return post;
	}

	private HttpMethod get() throws Exception {
		GetMethod get = new GetMethod();
		get.setURI(uri);
		status = session.getHttpClient().executeMethod(get);
		responseBody = getResponseBody(get);
		return get;
	}

	private HttpMethod delete() throws Exception {
		DeleteMethod delete = new DeleteMethod();
		delete.setURI(uri);
		status = session.getHttpClient().executeMethod(delete);
		responseBody = getResponseBody(delete);
		return delete;
	}

	private String getResponseBody(HttpMethod method) throws IOException {
		String body = null;
		Logger logger = Logger.getLogger("org.apache.commons.httpclient");
		logger.setLevel(Level.SEVERE);
		body = method.getResponseBodyAsString();
		logger.setLevel(Level.ALL);
		return body;
	}
}
