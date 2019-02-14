package com.project.Util;



import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.project.kothakhoj.R;

import java.io.IOException;


/**
 * Author : nilkamal,
 * Creation Date: 8/8/18.
 */
public class Util {

    public static String USER_NAME_KEY = "Full Name";
    public static String USER_EMAIL_KEY = "Email";
    public static String USER_CONTACT_KEY = "Contact No";
    public static String USER_TYPE_KEY = "isRenter";
    public static int parentFrameLayout = R.id.parent_frame_layout;
    public static int homeFrameLayout = R.id.home_layout;
    public static String RENTER_KEY = "Rent rooms";
    public static String SEEKER_KEY = "Look for rooms";
    public static String ROOM_TYPE = "Type";
    public static String ROOM_ADDRESS = "Location";
    public static String ROOM_PRICE = "Price";
    public static String ROOM_DATE = "Available Date";
    public static String ROOM_PREFERENCE = "Preference";
    public static String ROOM_FLOOR = "Floor";
    public static String ROOM_CONTACT = "Contact";
    public static String ROOM_COOKING = "Cooking";
    public static String ROOM_RENT = "Rent";
    public static String ROOM_OWNER = "Owner";
    public static String ROOM_ID = "ReferenceId";
    public static String FireStorageDownloadUrl = "gs://kothakhoj-7405a.appspot.com ";
    public static String DEFAULT_TYPE = "Select Room Type";
    public static String DEFAULT_PREFERENCE = "Select Preferred Tenants";
    public static String DEFAULT_FLOOR = "Select Floor";
    public static String DEFAULT_TIMING = "Select Contact Timing";
    private static String NETWORK_ERROR = "Not Connected to Network";
    private static String INTERNET_ERROR = "Internet not working. Please check your network settings";
    public static String NETWORK_OK = "true";
    public static String DEFAULT_MESSAGE = "I am really interested in your room. If possible I would be waiting for your call"+
            " and also visit the room";




    public static void changeFragment(FragmentManager fragmentManager, int oldFragment, Fragment newFragment,String backStackName){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(oldFragment,newFragment);
        if(backStackName != null){
            transaction.addToBackStack(backStackName);
        }
        transaction.commit();
    }


    public static String getNetworkState(Context context){
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager.getActiveNetworkInfo() != null){
            String command = "ping -c 1 google.com";
            try {
                if(Runtime.getRuntime().exec (command).waitFor() == 0){
                    return NETWORK_OK;
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return INTERNET_ERROR;
        }else{
            return NETWORK_ERROR;
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    public static String getPathFromURI(Context context,Uri uri){
        String realPath="";
// SDK < API11
        if (Build.VERSION.SDK_INT < 11) {
            String[] proj = { MediaStore.Images.Media.DATA };
            @SuppressLint("Recycle") Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = 0;
            String result="";
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                realPath=cursor.getString(column_index);
            }
        }
        // SDK >= 11 && SDK < 19
        else if (Build.VERSION.SDK_INT < 19){
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader cursorLoader = new CursorLoader(context, uri, proj, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            if(cursor != null){
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                realPath = cursor.getString(column_index);
            }
        }
        // SDK > 19 (Android 4.4)
        else if(DocumentsContract.isDocumentUri(context, uri)){
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
