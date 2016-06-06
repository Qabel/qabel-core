package de.qabel.box.storage;

import java.util.Arrays;

public class BoxFolder extends BoxObject {
    protected String ref;

    public BoxFolder(String ref, String name, byte[] key) {
        super(name);
        this.key = key;
        this.ref = ref;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoxFolder boxFolder = (BoxFolder) o;

        if (name != null ? !name.equals(boxFolder.name) : boxFolder.name != null) {
            return false;
        }
        if (!Arrays.equals(key, boxFolder.key)) {
            return false;
        }
        return !(ref != null ? !ref.equals(boxFolder.ref) : boxFolder.ref != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (key != null ? Arrays.hashCode(key) : 0);
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        return result;
    }

    @Override
    public String getRef() {
        return ref;
    }
}
