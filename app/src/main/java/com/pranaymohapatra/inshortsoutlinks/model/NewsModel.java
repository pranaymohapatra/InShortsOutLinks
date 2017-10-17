package com.pranaymohapatra.inshortsoutlinks.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

//POJO for API Response Body

public class NewsModel implements Parcelable {

    public final static Parcelable.Creator<NewsModel> CREATOR = new Creator<NewsModel>() {


        @SuppressWarnings({
                "unchecked"
        })
        public NewsModel createFromParcel(Parcel in) {
            NewsModel instance = new NewsModel();
            instance.iD = (int) in.readInt();
            instance.tITLE = in.readString();
            instance.uRL = in.readString();
            instance.pUBLISHER = in.readString();
            instance.cATEGORY = in.readString();
            instance.hOSTNAME = in.readString();
            instance.tIMESTAMP = in.readLong();
            return instance;
        }

        public NewsModel[] newArray(int size) {
            return (new NewsModel[size]);
        }

    };
    @SerializedName("ID")
    @Expose
    private int iD;
    @SerializedName("TITLE")
    @Expose
    private String tITLE;
    @SerializedName("URL")
    @Expose
    private String uRL;
    @SerializedName("PUBLISHER")
    @Expose
    private String pUBLISHER;
    @SerializedName("CATEGORY")
    @Expose
    private String cATEGORY;
    @SerializedName("HOSTNAME")
    @Expose
    private String hOSTNAME;
    @SerializedName("TIMESTAMP")
    @Expose
    private long tIMESTAMP;
    private int isFavorite;

    public int getID() {
        return iD;
    }

    public void setID(int iD) {
        this.iD = iD;
    }

    public String getTITLE() {
        return tITLE;
    }

    public void setTITLE(String tITLE) {
        this.tITLE = tITLE;
    }

    public String getURL() {
        return uRL;
    }

    public void setURL(String uRL) {
        this.uRL = uRL;
    }

    public String getPUBLISHER() {
        return pUBLISHER;
    }

    public void setPUBLISHER(String pUBLISHER) {
        this.pUBLISHER = pUBLISHER;
    }

    public String getCATEGORY() {
        return cATEGORY;
    }

    public void setCATEGORY(String cATEGORY) {
        this.cATEGORY = cATEGORY;
    }

    public String getHOSTNAME() {
        return hOSTNAME;
    }

    public void setHOSTNAME(String hOSTNAME) {
        this.hOSTNAME = hOSTNAME;
    }

    public long getTIMESTAMP() {
        return tIMESTAMP;
    }

    public void setTIMESTAMP(long tIMESTAMP) {
        this.tIMESTAMP = tIMESTAMP;
    }

    public int getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(int isfavorite) {
        isFavorite = isfavorite;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(iD);
        dest.writeString(tITLE);
        dest.writeString(uRL);
        dest.writeString(pUBLISHER);
        dest.writeString(cATEGORY);
        dest.writeString(hOSTNAME);
        dest.writeLong(tIMESTAMP);
    }

    public int describeContents() {
        return 0;
    }

}