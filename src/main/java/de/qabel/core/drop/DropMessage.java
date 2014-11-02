package de.qabel.core.drop;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class DropMessage<T extends ModelObject>{
    @SerializedName("version")
    private int version = 1;
    @SerializedName("time_stamp")
    private long time = 1L;
    @SerializedName("acknowledge_id")
    private String acknowledgeID = "0";
    @SerializedName("sender")
    private String sender = "";
    @SerializedName("model_object")
    private Class<T> modelObject;
    @SerializedName("data")
    private T data;


    public DropMessage(){}

    public DropMessage(int version, Date time, String acknowledgeID, String sender, Class<T> modelObject, T data) {
        setVersion(version);
        setTime(time);
        setAcknowledgeID(acknowledgeID);
        setSender(sender);
        setModelObject(modelObject);
        setData(data);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time.getTime();
    }

    public String getAcknowledgeID() {
        return acknowledgeID;
    }

    public void setAcknowledgeID(String acknowledgeID) {
        this.acknowledgeID = acknowledgeID;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Class<T> getModelObject() {
        return modelObject;
    }

    public void setModelObject(Class<T> modelObject) {
        this.modelObject = modelObject;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
