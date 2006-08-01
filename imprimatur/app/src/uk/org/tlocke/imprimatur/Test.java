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

import org.apache.commons.httpclient.HttpClient;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class Test extends Common {
	private HttpClient client = new HttpClient();

	private TestGroup testGroup;

	public Test(TestGroup testGroup, Element testElement) {
		super(testGroup, testElement);
		this.testGroup = testGroup;
	}

	public TestGroup getTestGroup() {
		return testGroup;
	}

	public HttpClient getHttpClient() {
		return client;
	}

	public void process() throws Exception {
		super.process();
		System.out.println("Test: '" + getElement().getAttribute("name") + "'.");
		NodeList sessions = getElement().getElementsByTagName("session");

		for (int i = 0; i < sessions.getLength(); i++) {
			new Session(this, (Element) sessions.item(i)).process();
		}
		if (sessions.getLength() == 0) {
			new Session(this, getElement()).process();
		}
	}
}
