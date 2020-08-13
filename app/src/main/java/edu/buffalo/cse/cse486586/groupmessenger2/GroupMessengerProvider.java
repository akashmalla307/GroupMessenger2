package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {
    MatrixCursor cursor;
    MatrixCursor cursor1;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        Log.v("insert", values.toString());

        Log.v("insert", values.toString());
        //sqLiteDataBase.insertGroupMessages(values);
        MatrixCursor.RowBuilder rowBuilder = cursor.newRow();
        rowBuilder.add("key",values.get("key")).add("value",values.get("value"));
        Log.v("count=",String.valueOf(cursor.getCount()));


        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        cursor = new MatrixCursor(new String[] { "key","value"});
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        Log.v("query", selection);
        Log.v("query", selection);
        Log.v("count", String.valueOf(cursor.getCount()));
//cursor.requery(selection);

        // System.out.println("inside query func");
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex("key");
                do {
                    String word = cursor.getString(columnIndex);
                    Log.v("key=",word);
                    //System.out.println("coulmnindex="+columnIndex);
                    //System.out.println("w=" + word);
                    if(word.equals(selection)){
                        Log.v("key is found","");
                        int columnIndex1 = cursor.getColumnIndex("value");
                        String word1 = cursor.getString(columnIndex1);
                        Log.v("value=",word1);
                        //Log.v("column=",String.valueOf(columnIndex1));
                        //System.out.println("coulmnindex1="+columnIndex1);
                        //System.out.println(word1);
                        cursor1 = new MatrixCursor(new String[] { "key","value"});
                        MatrixCursor.RowBuilder rowBuilder1 = cursor1.newRow();
                        rowBuilder1.add("key",word).add("value",word1);
                        //System.out.println("COUNT=");
                        // return cursor;
                        break;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
       /* int columnIndex2 = cursor1.getColumnIndex("value");
        System.out.println("coulmnindex2="+columnIndex2);
        String word3 = cursor1.getString(columnIndex2);
        System.out.println("word3="+word3);
       */ return cursor1;
    }
}
