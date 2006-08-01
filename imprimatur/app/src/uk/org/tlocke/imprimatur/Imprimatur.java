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
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Imprimatur extends Common {
	private Document doc;

	// private Properties properties = new Properties();

	public Imprimatur(Document doc) {
		super(null, doc.getDocumentElement());
		this.doc = doc;
		// File scriptDirectory;
		/*
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new ImprimaturResolver());
			doc = builder.parse(testFile);
			/*
			 * scriptDirectory = new File(testFile.getParent()); try {
			 * FileInputStream propertiesFile = new FileInputStream(System
			 * .getProperty("user.home") + File.separator +
			 * "imprimatur.properties"); properties.load(propertiesFile); }
			 * catch (FileNotFoundException e) {
			 * System.out.println(e.getMessage()); // Do nothing }
			 */
		/*
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}

	public Document getDocument() {
		return doc;
	}

	public static void main(String[] args) throws Exception {
		String fileName = null;
		boolean passed = false;

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
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new ImprimaturResolver());
			Document doc = builder.parse(file);
			new Imprimatur(doc).process();
			System.out.println("Passed tests!");
			passed = true;
		} catch (UserException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		System.exit(passed ? 0 : 1);
	}

	/*
	 * private String getProperty(String name) { return
	 * properties.getProperty(name, name); }
	 */
	public void process() throws Exception {
		super.process();
		NodeList testGroups = doc.getDocumentElement().getElementsByTagName("test-group");
		for (int i = 0; i < testGroups.getLength(); i++) {
			new TestGroup(this, (Element) testGroups.item(i)).process();
		}
		if (testGroups.getLength() == 0) {
			new TestGroup(this, getElement()).process();
		}
	}

	static private class ImprimaturResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			if (systemId
					.equals("http://imprimatur.sourceforge.net/imprimatur-003.dtd")) {
				InputSource inputSource = new InputSource(Imprimatur.class
						.getClassLoader().getResourceAsStream(
								"uk/org/tlocke/imprimatur/imprimatur-003.dtd"));
				return inputSource;
			} else {
				return null;
			}
		}
	}
}