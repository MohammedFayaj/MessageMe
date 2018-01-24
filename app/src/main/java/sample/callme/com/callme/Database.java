package sample.callme.com.callme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 10/18/17.
 */

public class Database extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "CallMeontacts";

    // Contacts table name
    private static final String TABLE_CONTACTS = "phoneContacts";

    // Contacts Table Columns names
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_CONTACT_SELECTED = "contact_selected";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                +  KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT ,"
                + KEY_CONTACT_SELECTED + " TEXT "
                + ")";



        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }


    public void insertContactsfromPhone(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // Contact Name
        values.put(KEY_PH_NO, contact.getPhoneNumber()); // Contact Phone Number
        values.put(KEY_CONTACT_SELECTED ,contact.isSelected() ? "yes" :"no");

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
        close();
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2).equalsIgnoreCase("yes"));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        if(db.isOpen()){
            db.close();
        }
        // return contact list
        return contactList;
    }

    public void updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getPhoneNumber());
        values.put(KEY_CONTACT_SELECTED , contact.isSelected()?"yes":"no");

        // updating row
         db.update(TABLE_CONTACTS, values, KEY_PH_NO + " = ?",
                new String[] { String.valueOf(contact.getPhoneNumber()) });
        if(db.isOpen()){
            db.close();
        }
    }

    public List<Contact> getSelectedContacts() {

        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS + " WHERE " + KEY_CONTACT_SELECTED +" = 'yes' ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2).equalsIgnoreCase("yes"));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        if(db.isOpen()){
            db.close();
        }
        // return contact list
        return contactList;
    }

    public void deleteContacts(){

        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS + " WHERE " + KEY_CONTACT_SELECTED +" != 'yes' ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2).equalsIgnoreCase("yes"));
                // Adding contact to list
                contactList.add(contact);

            } while (cursor.moveToNext());
        }
        if(db.isOpen()){
            db.close();
        }
    }

}
