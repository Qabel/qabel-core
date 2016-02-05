package de.qabel.core.exceptions;

import java.util.HashMap;
import java.util.Map;

public class QblCreateAccountFailException extends IllegalArgumentException {

	private Map map;

	public QblCreateAccountFailException(HashMap map) {
		this.map = map;
	}

	public Map getMap() {
		return map;
	}
}
