package com.zen.callrecorder;

import android.os.Environment;

import java.io.File;

/**
 * Created by Ozenc Celik on 10/25/2018
 */
public class Constant {

    //MainActivity Contants
    public static final int PERMISSION_REQUEST_CODE = 200;
    public static int DELETED_DAYS = 0;
    public static int ORDER_LIST_STATE = 0; // 0 - Calendar 1 - Name 2 - Duration
    public static int FIRST_ENTER = 0;//0 - call getContacts 1 - no action
    public static boolean IS_CONTACT_LOADING = false;


    public static String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().toString() + File.separator + "Call Recorder";
    /*
    String sep = File.separator; // Use this instead of hardcoding the "/"
    String newFolder = "Call Recorder";
    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();*/

    public static int COMING_CLASS = 0; // 0 - RecordActivity, 1 - IgnoreActivity

    //CallReceiver Variables
    public static boolean IS_RECORDING_AUTOMATICALLY = false;
    public static boolean IS_RECORDING_MANUALLY = false;
    public static boolean IS_RECORDING_IGNORE = false;
    public static boolean IS_RECORDING_RECORD = false;

    public static boolean IS_RECORD_STARTED = false;

    public static final String DATE_FORMAT = "yyyy-MM-dd hh.mm.ss";


    public static void setFilePath(String path){
        AUDIO_FILE_PATH = path +  File.separator + "Call Recorder";
    }


}
