package uk.org.tlocke.imprimatur;

import java.io.File;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Element;

public abstract class Common {
	private Common superCommon;

	private Element element;

	private String scheme;

	private String hostname;

	private int port;

	private File scriptFile;

	//private DefaultHttpClient client;

	Common(Common superCommon, Element element, File scriptFile) {
		this.superCommon = superCommon;
		this.element = element;
		setScriptFile(scriptFile);
		if (superCommon == null) {
		//	client = new DefaultHttpClient();
		} else {
		//	client = superCommon.getHttpClient();
		}
		/*
		org.apache.http.client.CookieStore store = client.getCookieStore();
		Debug.print("Common about to print cookies " + client.hashCode());
		for (Cookie cookie : client.getCookieStore().getCookies()) {
			Debug.print(cookie.toString());
		}
		*/
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

	Element getElement() {
		return element;
	}

	public File getScriptFile() {
		return scriptFile;
	}

	private void setScriptFile(File scriptFile) {
		this.scriptFile = scriptFile;
	}

	public DefaultHttpClient getHttpClient() {
		return superCommon.getHttpClient();
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
	}
}