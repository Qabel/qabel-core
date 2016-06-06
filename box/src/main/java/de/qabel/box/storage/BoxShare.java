package de.qabel.box.storage;

public class BoxShare {
    public static final String TYPE_READ = "READ";
    private final String ref;
    private final String recipient;
    private final String type;

    public BoxShare(String ref, String recipient) {
        this(ref, recipient, TYPE_READ);
    }

    public BoxShare(String ref, String recipient, String type) {
        this.ref = ref;
        this.recipient = recipient;
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getType() {
        return type;
    }
}
