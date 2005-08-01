/*
 * Created on 25-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package uk.org.tlocke.imprimatur;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author tlocke
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Request {

	Test test;

	String method;

	String path;

	String enctype;

	int status;

	String responseBody;

	NodeList parameterElements;

	boolean waitForRefreshes;

	URI uri;

	NodeList responseCodeElements;

	NodeList regexpElements;

	public Request(Test test, Element request) throws Exception {
		this.test = test;
		method = request.getAttribute("method");
		path = request.getAttribute("path");
		enctype = request.getAttribute("enctype");
		parameterElements = request.getElementsByTagName("parameter");
		waitForRefreshes = "true".equals(request
				.getAttribute("waitForRefreshes"));
		responseCodeElements = request.getElementsByTagName("responseCode");
		regexpElements = request.getElementsByTagName("regex");
		if (method == null || method.length() == 0) {
			method = "get";
		}
		if (enctype == null || enctype.length() == 0) {
			enctype = "application/x-www-form-urlencoded";
		}
		uri = new URI("http", "", test.getImprimatur().getDefaultHostName(),
				test.getImprimatur().getDefaultPort(), path);
		System.out.println("Request: '" + uri.toString() + "'.");
	}

	public void process() throws Exception {
		HttpMethod httpMethod = null;
		if (method.equals("post")) {
			httpMethod = post();
		} else if (method.equals("get")) {
			httpMethod = get();
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
				throw new UserException("Failed response code check");
			}
		}

		for (int i = 0; i < regexpElements.getLength(); i++) {
			Element regexpElement = (Element) regexpElements.item(i);
			Pattern pattern = Pattern.compile(regexpElement.getTextContent());
			Matcher matcher = pattern.matcher(responseBody);
			if (!matcher.find()) {
				throw new UserException("Failed regexp check: '"
						+ regexpElement.getTextContent() + "'. Response:\n"
						+ responseBody);
			}
		}
	}

	private HttpMethod post() throws Exception {
		PostMethod post = new PostMethod();
		post.setURI(uri);
		if (enctype.equals("application/x-www-form-urlencoded")) {
			for (int k = 0; k < parameterElements.getLength(); k++) {
				Element parameterElement = (Element) parameterElements.item(k);
				post.setParameter(parameterElement.getAttribute("name"),
						parameterElement.getAttribute("value"));
			}
		} else if (enctype.equals("multipart/form-data")) {
			List partsList = new ArrayList();

			for (int k = 0; k < parameterElements.getLength(); k++) {

				Element parameterElement = (Element) parameterElements.item(k);
				String parameterName = parameterElement.getAttribute("name");
				String parameterValue = parameterElement.getAttribute("value");
				if (parameterElement.getAttribute("type").equals("file")) {
					partsList.add(new FilePart(parameterName, new File(
							parameterValue)));
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
		status = test.getHttpClient().executeMethod(post);
		responseBody = getResponseBody(post);
		if (waitForRefreshes) {
			GetMethod getMethod = null;
			Header refreshHeader = post.getResponseHeader("Refresh");
			while (refreshHeader != null) {
				Thread.sleep(Integer.parseInt(refreshHeader.getValue()) * 1000);
				getMethod = new GetMethod(post.getURI().toString());
				status = test.getHttpClient().executeMethod(getMethod);
				refreshHeader = getMethod.getResponseHeader("Refresh");
			}
			if (getMethod != null) {
				responseBody = getResponseBody(getMethod);
			}
		}
		return post;
	}

	private HttpMethod get() throws Exception {
		GetMethod get = new GetMethod();
		get.setURI(uri);
		get.setFollowRedirects(true);
		status = test.getHttpClient().executeMethod(get);
		responseBody = getResponseBody(get);
		return get;
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
