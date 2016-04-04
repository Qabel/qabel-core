package de.qabel.core.exceptions;

import java.util.HashMap;
import java.util.Map;

public class QblCreateAccountFailException extends IllegalArgumentException {

    private Map map;

    public QblCreateAccountFailException(HashMap map) {
        super(map.toString());
        this.map = map;
    }

    public Map getMap() {
        return map;
    }
}
