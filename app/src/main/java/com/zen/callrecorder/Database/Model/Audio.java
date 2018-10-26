package com.zen.callrecorder.Database.Model;

import java.io.Serializable;

public class Audio implements Serializable {

    public static final String TABLE_NAME = "audios";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CONTACT_NAME = "name";
    public static final String COLUMN_CONTACT_NUMBER = "number";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_FAVORITE = "favorite";
    public static final String COLUMN_FILE_PATH = "filePath";

    private int id;
    private String contactName;
    private String contactNumber;
    private String note;
    private String timestamp;
    private String duration;
    private int isFavorite;
    private String filePath;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CONTACT_NAME + " TEXT,"
                    + COLUMN_CONTACT_NUMBER + " TEXT,"
                    + COLUMN_NOTE + " TEXT,"
                    + COLUMN_TIMESTAMP + " TEXT,"
                    + COLUMN_DURATION + " TEXT,"
                    + COLUMN_FAVORITE + " INTEGER,"
                    + COLUMN_FILE_PATH + " TEXT"
                    + ")";

    public Audio() {
    }

    public Audio(int id, String contactName, String contactNumber, String note, String timestamp, String duration, int isFavorite, String filePath) {
        this.id = id;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.note = note;
        this.timestamp = timestamp;
        this.duration = duration;
        this.isFavorite = isFavorite;
        this.filePath = filePath;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int isFavorite() {
        return isFavorite;
    }

    public void setFavorite(int favorite) {
        isFavorite = favorite;
    }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}