/*
 * Created on 25-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
