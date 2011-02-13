package uk.org.tlocke.imprimatur;

import java.io.File;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Common {
	private Common superCommon;

	private Element element;

	private String scheme;

	private String hostname;

	private int port;

	private Credentials credentials;

	private File scriptFile;

	Common(Common superCommon, Element element, File scriptFile) {
		this.superCommon = superCommon;
		this.element = element;
		setScriptFile(scriptFile);
	}

	public String getScheme() {
		return scheme;
	}

	private void setScheme(String scheme) {
		this.scheme = scheme;
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

	public File getScriptFile() {
		return scriptFile;
	}

	private void setScriptFile(File scriptFile) {
		this.scriptFile = scriptFile;
	}

	void process() throws Exception {
		String scheme = element.getAttribute("scheme");
		setScheme(scheme.length() == 0 ? superCommon.getScheme() : scheme);

		String portString = element.getAttribute("port");
		setPort(portString.length() == 0 ? superCommon.getPort() : Integer
				.parseInt(portString));

		String hostName = element.getAttribute("hostname");
		setHostname(hostName.length() == 0 ? superCommon.getHostname()
				: hostName);

		NodeList credentialsList = element.getElementsByTagName("credentials");
		if (credentialsList.getLength() > 0) {
			Element credentialsElement = (Element) credentialsList.item(0);
			credentials = new UsernamePasswordCredentials(credentialsElement
					.getAttribute("username"), credentialsElement
					.getAttribute("password"));
		} else if (superCommon != null) {
			credentials = superCommon.getCredentials();
		}
	}
}