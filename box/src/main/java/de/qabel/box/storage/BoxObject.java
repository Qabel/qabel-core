package de.qabel.box.storage;

import java.util.Observable;

public abstract class BoxObject extends Observable implements Comparable<BoxObject> {
    protected String name;
    protected byte[] key;

    public BoxObject(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(BoxObject another) {
        if (this instanceof BoxFile && another instanceof BoxFile) {
            return name.compareTo(another.name);
        }
        if (this instanceof BoxFolder && another instanceof BoxFolder) {
            return name.compareTo(another.name);
        }
        if (this instanceof BoxFile) {
            return -1;
        }
        return 1;
    }

    public String getName() {
        return name;
    }

    public byte[] getKey() {
        return key;
    }

    public abstract String getRef();
}
