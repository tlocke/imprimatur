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

/**
 * @author tlocke
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
class Test {
	private Element testElement;

	private HttpClient client = new HttpClient();

	private Imprimatur imprimatur;

	public Test(Imprimatur imprimatur, Element testElement) {
		this.imprimatur = imprimatur;
		this.testElement = testElement;
	}
	
	public Imprimatur getImprimatur() {
		return imprimatur;
	}
	
	public HttpClient getHttpClient() {
		return client;
	}

	public void process() throws Exception {
		System.out.println("Test: '" + testElement.getAttribute("name") + "'.");
		NodeList requests = testElement.getElementsByTagName("request");

		for (int i = 0; i < requests.getLength(); i++) {
			new Request(this, (Element) requests.item(i)).process();
		}
	}

}
