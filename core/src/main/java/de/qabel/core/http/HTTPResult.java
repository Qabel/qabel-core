package de.qabel.core.http;

import java.util.Date;

public class HTTPResult<T> {
    int responseCode;
    boolean ok;
    Date lastModified;
    T data;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date lastModified() {
        return lastModified;
    }
}
