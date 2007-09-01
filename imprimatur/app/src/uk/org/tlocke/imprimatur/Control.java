package uk.org.tlocke.imprimatur;

import org.w3c.dom.Element;

public class Control {
	private String type;

	private String name;

	private String value;

	public Control(Element controlElement) {
		type = controlElement.getAttribute("type");
		name = controlElement.getAttribute("name");
		value = controlElement.getAttribute("value")
				+ controlElement.getTextContent();
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
