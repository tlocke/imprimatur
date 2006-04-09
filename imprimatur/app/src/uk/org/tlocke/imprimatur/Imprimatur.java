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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Imprimatur extends Common {
	private Document doc;

	private Properties properties = new Properties();

	public Imprimatur(File testFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new ImprimaturResolver());
			doc = builder.parse(testFile);
			try {
				FileInputStream propertiesFile = new FileInputStream(System
						.getProperty("user.home")
						+ File.separator + "imprimatur.properties");
				properties.load(propertiesFile);
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				// Do nothing
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Document getDocument() {
		return doc;
	}

	public static void main(String[] args) throws Exception {
		String fileName = null;
		
		if (args == null || args.length == 0) {
			fileName = "../tests/default.xml";
		} else {
			fileName = args[0];
		}
		File file = new File(fileName);
		if (!file.exists()) {
			throw new UserException("The file: '" + file.getCanonicalPath()
					+ "' doesn't exist.");

		}
		System.exit(new Imprimatur(file).runTests() == true ? 0 : 1);
	}

	private String getProperty(String name) {
		return properties.getProperty(name, name);
	}

	public boolean runTests() {
		boolean passed = false;
		try {
			Element documentElement = doc.getDocumentElement();
			setPort(Integer.parseInt(documentElement.getAttribute("port")));
			setHostname(documentElement.getAttribute("hostname"));
			NodeList credentialsList = documentElement
					.getElementsByTagName("credentials");
			if (credentialsList.getLength() > 0) {
				Element credentialsElement = (Element) credentialsList.item(0);
				setCredentials(new UsernamePasswordCredentials(
						getProperty(credentialsElement.getAttribute("username")),
						getProperty(credentialsElement.getAttribute("password"))));
			}
			NodeList tests = documentElement.getElementsByTagName("test");
			for (int i = 0; i < tests.getLength(); i++) {
				new Test(this, (Element) tests.item(i)).process();
			}
			System.out.println("Passed tests!");
			passed = true;
		} catch (UserException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return passed;
	}

	private class ImprimaturResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			if (systemId
					.equals("http://imprimatur.sourceforge.net/imprimatur-002.dtd")) {
				InputSource inputSource = new InputSource(Imprimatur.class
						.getClassLoader().getResourceAsStream(
								"uk/org/tlocke/imprimatur/imprimatur-002.dtd"));
				return inputSource;
			} else {
				return null;
			}
		}
	}
}