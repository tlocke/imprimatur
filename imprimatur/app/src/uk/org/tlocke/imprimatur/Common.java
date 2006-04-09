package uk.org.tlocke.imprimatur;

import org.apache.commons.httpclient.Credentials;

public abstract class Common {
	private String hostname;
	private int port;
	private Credentials credentials;
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public Credentials getCredentials() {
		return credentials;
	}
	
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	protected void setFields(Common common) {
		this.hostname = common.getHostname();
		this.port = common.getPort();
		this.credentials = common.getCredentials();
	}
}
