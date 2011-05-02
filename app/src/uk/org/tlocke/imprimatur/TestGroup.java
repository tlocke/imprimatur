package uk.org.tlocke.imprimatur;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestGroup extends Common {
	private Imprimatur imprimatur;
	
	TestGroup(Imprimatur imprimatur, Element element, File scriptFile) {
		super(imprimatur, element, scriptFile);
		this.imprimatur = imprimatur;
	}

	protected void process() throws Exception {
		super.process();
		NodeList tests = getElement().getChildNodes();
		//.getElementsByTagName("test");
		for (int i = 0; i < tests.getLength(); i++) {
			Node node = tests.item(i);
			if (node.getNodeName().equals("test")) {
			new Test(this, (Element) tests.item(i), getScriptFile()).process();
			}
		}
		new Test(this, getElement(), getScriptFile()).process();
	}
	
	public Imprimatur getImprimatur() {
		return imprimatur;
	}
}
