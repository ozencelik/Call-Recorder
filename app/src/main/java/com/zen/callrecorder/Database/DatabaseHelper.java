package com.zen.callrecorder.Database;

/**
 * Created by Ozenc Celik on 10/25/2018
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zen.callrecorder.Database.Model.Audio;
import com.zen.callrecorder.Database.Model.Contact;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "audios_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(Audio.CREATE_TABLE);
        db.execSQL(Contact.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Audio.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contact.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    /////////////////////////////////////////////////////////////////////////////////
    //Contact Part

    public long insertContact(Contact contact) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Contact.COLUMN_ID, contact.getId());
        values.put(Contact.COLUMN_CONTACT_LIST, contact.getIsInContactList());
        values.put(Contact.COLUMN_CONTACT_NAME, contact.getContactName());
        values.put(Contact.COLUMN_CONTACT_NUMBER, contact.getContactNumber());
        values.put(Contact.COLUMN_IGNORE_LIST, contact.isInIgnoreList());
        values.put(Contact.COLUMN_RECORD_LIST, contact.isInRecordList());

        // insert row
        long id = db.insert(Contact.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public void deleteAllContact() {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM "+ Contact.TABLE_NAME);
        // close db connection
        db.close();
    }

    public boolean isIgnoreContactExist(String contactNumber) {
        String selectQuery = "SELECT * FROM contacts WHERE number = ?;";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {contactNumber});

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_CONTACT_LIST)),
                        cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CONTACT_NAME)),
                        cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CONTACT_NUMBER)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_IGNORE_LIST)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_RECORD_LIST)));

                //if the contact is in ignore list return true
                if(contact.isInIgnoreList() == 1) return true;
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return false;
    }

    public boolean isRecordContactExist(String contactNumber) {

        String selectQuery = "SELECT * FROM contacts WHERE number = ?;";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[] {contactNumber});

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_CONTACT_LIST)),
                        cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CONTACT_NAME)),
                        cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CONTACT_NUMBER)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_IGNORE_LIST)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_RECORD_LIST)));

                if(contact.isInRecordList() == 1) return true;
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return false;
    }

    /* which
     * 1 - return ignoreContacts
     * 2 - return recordContacts
     */
    public List<Contact> getAllContacts(int which) {
        List<Contact> contacts = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + Contact.TABLE_NAME + " ORDER BY " +
                Contact.COLUMN_CONTACT_NAME + " ASC";

        //String selectQuery = "SELECT  * FROM " + Contact.TABLE_NAME;

        if(which == 1){
            selectQuery = "SELECT  * FROM " + Contact.TABLE_NAME + " WHERE " +
                    Contact.COLUMN_IGNORE_LIST+ " = 1" + " ORDER BY " +
                    Contact.COLUMN_CONTACT_NAME + " ASC" ;
        }else if(which == 2){
            selectQuery = "SELECT  * FROM " + Contact.TABLE_NAME + " WHERE " +
                    Contact.COLUMN_RECORD_LIST+ " = 1" + " ORDER BY " +
                    Contact.COLUMN_CONTACT_NAME + " ASC" ;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_CONTACT_LIST)),
                        cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CONTACT_NAME)),
                        cursor.getString(cursor.getColumnIndex(Contact.COLUMN_CONTACT_NUMBER)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_IGNORE_LIST)),
                        cursor.getInt(cursor.getColumnIndex(Contact.COLUMN_RECORD_LIST)));

                contacts.add(contact);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return contact list
        return contacts;
    }

    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + Contact.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public int updateContact(Contact contact) {
        //Contact can add in ignore list and record list
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Contact.COLUMN_IGNORE_LIST, contact.isInIgnoreList());
        values.put(Contact.COLUMN_RECORD_LIST, contact.isInRecordList());

        // updating row
        return db.update(Contact.TABLE_NAME, values, Contact.COLUMN_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
    }



    //End Contact Part
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Audio Part

    public long insertAudio(Audio audio) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` will be inserted automatically.
        // no need to add it
        values.put(Audio.COLUMN_CONTACT_NAME, audio.getContactName());
        values.put(Audio.COLUMN_CONTACT_NUMBER, audio.getContactNumber());
        values.put(Audio.COLUMN_TIMESTAMP, audio.getTimestamp());
        values.put(Audio.COLUMN_DURATION, audio.getDuration());
        values.put(Audio.COLUMN_FAVORITE, audio.isFavorite());
        values.put(Audio.COLUMN_FILE_PATH, audio.getFilePath());

        // insert row
        long id = db.insert(Audio.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public Audio getAudio(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Audio.TABLE_NAME,
                new String[]{Audio.COLUMN_ID, Audio.COLUMN_CONTACT_NAME, Audio.COLUMN_CONTACT_NUMBER, Audio.COLUMN_NOTE, Audio.COLUMN_TIMESTAMP, Audio.COLUMN_DURATION, Audio.COLUMN_FAVORITE, Audio.COLUMN_FILE_PATH},
                Audio.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare audio object
        Audio audio = new Audio(
                cursor.getInt(cursor.getColumnIndex(Audio.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Audio.COLUMN_CONTACT_NAME)),
                cursor.getString(cursor.getColumnIndex(Audio.COLUMN_CONTACT_NUMBER)),
                cursor.getString(cursor.getColumnIndex(Audio.COLUMN_NOTE)),
                cursor.getString(cursor.getColumnIndex(Audio.COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(Audio.COLUMN_DURATION)),
                cursor.getInt(cursor.getColumnIndex(Audio.COLUMN_FAVORITE)),
                cursor.getString(cursor.getColumnIndex(Audio.COLUMN_FILE_PATH)));

        // close the db connection
        cursor.close();

        return audio;
    }

    /**
     * state = 0 order by timestamp
     * state = 1 order by contactName A to Z
     * state = 2 orer by audio duration
     * state = 3 return favorite list audio
     * @param state
     * @return
     */

    public List<Audio> getAllAudios(int state) {
        List<Audio> audios = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Audio.TABLE_NAME + " ORDER BY " +
                Audio.COLUMN_TIMESTAMP + " DESC";
        if(state == 1){
            selectQuery = "SELECT  * FROM " + Audio.TABLE_NAME + " ORDER BY " +
                    Audio.COLUMN_CONTACT_NAME + " ASC";
        }else if(state == 2){
            selectQuery = "SELECT  * FROM " + Audio.TABLE_NAME + " ORDER BY " +
                    Audio.COLUMN_DURATION + " DESC";
        }else if(state == 3){
            selectQuery = "SELECT  * FROM " + Audio.TABLE_NAME + " WHERE " +
                    Audio.COLUMN_FAVORITE + " = 1" ;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // prepare audio object
                Audio audio = new Audio(
                        cursor.getInt(cursor.getColumnIndex(Audio.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Audio.COLUMN_CONTACT_NAME)),
                        cursor.getString(cursor.getColumnIndex(Audio.COLUMN_CONTACT_NUMBER)),
                        cursor.getString(cursor.getColumnIndex(Audio.COLUMN_NOTE)),
                        cursor.getString(cursor.getColumnIndex(Audio.COLUMN_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndex(Audio.COLUMN_DURATION)),
                        cursor.getInt(cursor.getColumnIndex(Audio.COLUMN_FAVORITE)),
                        cursor.getString(cursor.getColumnIndex(Audio.COLUMN_FILE_PATH)));

                audios.add(audio);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return audio list
        return audios;
    }

    public int getAudiosCount() {
        String countQuery = "SELECT  * FROM " + Audio.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public int updateNote(Audio audio) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Audio.COLUMN_NOTE, audio.getNote());
        values.put(Audio.COLUMN_FAVORITE, audio.isFavorite());

        // updating row
        return db.update(Audio.TABLE_NAME, values, Audio.COLUMN_ID + " = ?",
                new String[]{String.valueOf(audio.getId())});
    }

    public void deleteAudio(Audio audio) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Audio.TABLE_NAME, Audio.COLUMN_ID + " = ?",
                new String[]{String.valueOf(audio.getId())});
        db.close();
    }
}