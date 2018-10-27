package com.zen.callrecorder.Database.Model;


public class Contact {

    public static final String TABLE_NAME = "contacts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CONTACT_LIST = "contactList";
    public static final String COLUMN_CONTACT_NAME = "name";
    public static final String COLUMN_CONTACT_NUMBER = "number";
    public static final String COLUMN_IGNORE_LIST = "ignoreList";
    public static final String COLUMN_RECORD_LIST = "recordList";


    private int id;
    private int isInContactList;
    private String contactName;
    private String contactNumber;
    private int isInIgnoreList;
    private int isInRecordList;



    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_CONTACT_LIST + " TEXT,"
                    + COLUMN_CONTACT_NAME + " TEXT,"
                    + COLUMN_CONTACT_NUMBER + " TEXT,"
                    + COLUMN_IGNORE_LIST + " INTEGER,"
                    + COLUMN_RECORD_LIST + " INTEGER"
                    + ")";

    public Contact() {
    }

    public Contact(int id, int isInContactList, String contactName, String contactNumber, int isInIgnoreList, int isInRecordList) {
        this.id = id;
        this.isInContactList = isInContactList;
        this.contactName = contactName;
        this.contactNumber = contactNumber;
        this.isInRecordList = isInRecordList;
        this.isInIgnoreList = isInIgnoreList;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIsInContactList() {
        return isInContactList;
    }

    public void setIsInContactList(int contactId) {
        this.isInContactList = contactId;
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

    public int isInIgnoreList() {
        return isInIgnoreList;
    }

    public void setInIgnoreList(int inIgnoreList) {
        isInIgnoreList = inIgnoreList;
    }

    public int isInRecordList() {
        return isInRecordList;
    }

    public void setInRecordList(int inRecordList) {
        isInRecordList = inRecordList;
    }

}