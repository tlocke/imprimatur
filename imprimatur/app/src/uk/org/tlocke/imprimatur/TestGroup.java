package uk.org.tlocke.imprimatur;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestGroup extends Common {
	private Imprimatur imprimatur;
	
	TestGroup(Imprimatur imprimatur, Element element) {
		super(imprimatur, element);
		this.imprimatur = imprimatur;
	}

	protected void process() throws Exception {
		super.process();
		NodeList tests = getElement().getElementsByTagName("test");
		for (int i = 0; i < tests.getLength(); i++) {
			new Test(this, (Element) tests.item(i)).process();
		}
		if (tests.getLength() == 0) {
			new Test(this, getElement()).process();
		}
	}
	
	public Imprimatur getImprimatur() {
		return imprimatur;
	}
}
