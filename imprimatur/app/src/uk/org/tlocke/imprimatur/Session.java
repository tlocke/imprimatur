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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.AuthScope;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class Session extends Common {
	private DefaultHttpClient client = new DefaultHttpClient();

	private Test test;

	public Session(Test test, Element testElement, File scriptFile) {
		super(test, testElement, scriptFile);
		this.test = test;
		/*
		 * if (getCredentials() != null) { setCredentials(getCredentials()); }
		 */
	}

	/*
	 * void setCredentials(Credentials credentials) {
	 * client.getState().setCredentials( new AuthScope(getHostname(), getPort(),
	 * AuthScope.ANY_REALM), getCredentials()); }
	 */
	public Test getTest() {
		return test;
	}

	public HttpClient getHttpClient() {
		return client;
	}

	public void process() throws Exception {
		super.process();
		if (getCredentials() != null) {
			client.getCredentialsProvider()
					.setCredentials(
							new AuthScope(getHostname(), getPort(),
									AuthScope.ANY_REALM), getCredentials());
		}
		NodeList requests = getElement().getChildNodes();

		for (int i = 0; i < requests.getLength(); i++) {
			Node node = requests.item(i);
			if (node.getNodeName().equals("request")) {
				new Request(this, (Element) requests.item(i), getScriptFile())
						.process();
			}
		}
		// new Request(this, getElement(), getScriptFile()).process();
	}
}
