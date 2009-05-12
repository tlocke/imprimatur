/*
 * Copyright 2005-2009 Tony Locke
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
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

	int status;

	File bodyFile = null;

	String response;

	List<Control> controls = new ArrayList<Control>();

	URI uri;

	NodeList responseCodeElements;

	Element refreshElement = null;

	NodeList regexpElements;

	NodeList headerElements;

	public Request(Session session, Element request, File scriptFile)
			throws Exception {
		super(session, request, scriptFile);
		this.session = session;
		method = request.getAttribute("method");
		path = request.getAttribute("path");
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
			httpMethod = new GetMethod();
		} else if (method.equals("delete")) {
			httpMethod = new DeleteMethod();
		} else if (method.equals("put")) {
			httpMethod = put();
		} else {
			throw new UserException("Unknown request method type: " + method);
		}
		for (int i = 0; i < headerElements.getLength(); i++) {
			Element headerElement = (Element) headerElements.item(i);
			httpMethod.addRequestHeader(headerElement.getAttribute("name"),
					headerElement.getAttribute("value"));
		}
		httpMethod.setURI(uri);
		status = session.getHttpClient().executeMethod(httpMethod);
		Logger logger = Logger.getLogger("org.apache.commons.httpclient");
		logger.setLevel(Level.SEVERE);
		StringBuilder responseBuilder = new StringBuilder();
		for (Header header : httpMethod.getResponseHeaders()) {
			responseBuilder.append(header.toExternalForm());
		}
		responseBuilder.append(" \r\n");
		responseBuilder.append(httpMethod.getResponseBodyAsString());
		response = responseBuilder.toString();
		logger.setLevel(Level.ALL);
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
						+ "  Actual response body:\n" + response);
			}
		}
		//String responseBodyNoBreaks = response.replaceAll("\\p{Cntrl}", "");
		for (int i = 0; i < regexpElements.getLength(); i++) {
			Element regexpElement = (Element) regexpElements.item(i);
			String patternStr = regexpElement.getAttribute("pattern");
			if (!Pattern.compile(patternStr, Pattern.DOTALL).matcher(response).find()) {
				throw new UserException("Failed regexp check: '" + patternStr
						+ "'. Response:\n" + response);
			}
			//if (!responseBodyNoBreaks.matches(pattern)) {
			//	System.err.print("Leisure matches Leisure" + "POSTLeisure".matches("Leisure"));
			//	throw new UserException("Failed regexp check: '" + pattern
			//			+ "'. Response:\n" + responseBodyNoBreaks + "\n\nLeisure matches Leisure" + "Leisure".matches("Leisure"));
			//}
		}
	}

	private PostMethod post() throws Exception {
		PostMethod method = new PostMethod();

		if (bodyFile == null) {
			boolean hasFile = false;
			for (Control control : controls) {
				if (control.getType().equals("file")) {
					hasFile = true;
					break;
				}
			}
			if (hasFile) {
				List<Part> partsList = new ArrayList<Part>();

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
						partsList.add(new FilePart(control.getName(),
								fileToUpload));
					} else {
						partsList.add(new StringPart(control.getName(), control
								.getValue()));
					}
				}
				Part[] parts = new Part[partsList.size()];
				for (int j = 0; j < partsList.size(); j++) {
					parts[j] = (Part) partsList.get(j);
				}
				method.setRequestEntity(new MultipartRequestEntity(parts,
						method.getParams()));
			} else {
				for (Control control : controls) {
					method.setParameter(control.getName(), control.getValue());
				}
			}
		} else {
			RequestEntity body = new FileRequestEntity(bodyFile, null);
			method.setRequestEntity(body);
		}
		return method;
	}

	private PutMethod put() throws Exception {
		PutMethod method = new PutMethod();
		if (bodyFile != null) {
			RequestEntity body = new FileRequestEntity(bodyFile, null);
			method.setRequestEntity(body);
		}
		return method;
	}
}