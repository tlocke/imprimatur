package uk.org.tlocke.imprimatur;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Common {
	private Common superCommon;

	private Element element;

	private String hostname;

	private int port;

	private Credentials credentials;

	Common(Common superCommon, Element element) {
		this.superCommon = superCommon;
		this.element = element;
	}

	public String getHostname() {
		return hostname;
	}

	private void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	Element getElement() {
		return element;
	}

	void process() throws Exception {
		String portString = element == null ? "" : element.getAttribute("port");
		setPort(portString.length() == 0 ? superCommon.getPort() : Integer
				.parseInt(portString));
		String hostName = element == null ? "" : element
				.getAttribute("hostname");
		setHostname(hostName.length() == 0 ? superCommon.getHostname()
				: element.getAttribute("hostname"));
		NodeList credentialsList = element.getElementsByTagName("credentials");
		if (credentialsList.getLength() > 0) {
			Element credentialsElement = (Element) credentialsList.item(0);
			credentials = new UsernamePasswordCredentials(credentialsElement
					.getAttribute("username"), credentialsElement
					.getAttribute("password"));
		}
	}
}