package sample.callme.com.callme;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


import sample.callme.com.callme.ContactsAdapter.ContactsAdapterListener;

public class ContactsActivity extends AppCompatActivity implements ContactSelected, ContactsAdapterListener {

    private ListView mContactList;
 //   private ContactsList mContactAdapter;
    private int contactsSelectedCount = 0;
    public static final String CONTACTS_COUNT = "contacts_count";
    public static final String CONTACTS_LIST = "contacts_list";
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public Database mDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Contacts");
        }
//        requestContactPermissions();
        mDB = new Database(ContactsActivity.this);
        mContactList = (ListView) findViewById(R.id.lv_contact_list);
       mContactList.setVisibility(View.GONE);
       /* // check for runtime permissions available for user to access the contacts
        if (ContextCompat.checkSelfPermission(ContactsActivity.this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {*/
      //  mContactAdapter = new ContactsList(ContactsActivity.this, this);
       // mContactList.setAdapter(mContactAdapter);
        /*} else {
            requestContactPermissions();
        }*/

    setUpRecyclerView();
    }
    ContactsAdapter mContactAdapter;
    private void setUpRecyclerView() {

        List<Contact> contactList = mDB.getAllContacts();
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact t1) {
                return contact.getName().compareTo(t1.getName());
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mContactAdapter = new ContactsAdapter(this, contactList, this,mDB);
        recyclerView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));
        recyclerView.setAdapter(mContactAdapter);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (mContactAdapter != null && query != null) {
                mContactAdapter.getFilter().filter(query);

            } else {
                if (mContactAdapter != null) {
                    mContactAdapter.notifyDataSetChanged();
                }
            }
        }

    }
    private void hideKeyboard() {
        final View currentFocus = getCurrentFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);

        }
        inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);


    }
    /*private void requestContactPermissions() {
        if (ContextCompat.checkSelfPermission(ContactsActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ContactsActivity.this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(ContactsActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS },
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }*/

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       switch (requestCode) {
           case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
               // If request is cancelled, the result arrays are empty.
               if (grantResults.length > 0
                       && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                   // permission was granted, yay! Do the
                   // contacts-related task you need to do.
                   mContactAdapter = new ContactsList(ContactsActivity.this, this);
                   mContactList.setAdapter(mContactAdapter);

               } else {

                   // permission denied, boo! Disable the
                   // functionality that depends on this permission.
               }
               return;
           }
       }

    }*/

    /*public static ArrayList<Contact> getContactsList(Context context) {
        ArrayList<Contact> contacts=new ArrayList<>();
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contacts.add(new Contact(name,phoneNumber));

        }
        phones.close();
        return contacts;
    }*/

    @Override
    public void checkBoxSelected(int position) {
        contactsSelectedCount++;
    }

    @Override
    public void checkBoxUnSelected(int position) {
        contactsSelectedCount--;
    }

    @Override
    public void onContactSelected(Contact contact) {
       if(contact.isSelected()){
           contactsSelectedCount++;
       }else {
           contactsSelectedCount--;
       }
    }

//old code -sa
//    private class ContactsList extends BaseAdapter {
//        private Context mContext;
//        private List<Contact> contacts = new ArrayList<>();
//        private ContactSelected listener;
//
//        public ContactsList(Context mContext, ContactSelected listener) {
//            this.mContext = mContext;
//            contacts = mDB.getAllContacts();
//            this.listener = listener;
//
//        }
//
//        @Override
//        public int getCount() {
//            return contacts.size();
//        }
//
//        @Override
//        public Contact getItem(int position) {
//            return contacts.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_layout, null);
//                holder = new ViewHolder(convertView);
//                convertView.setTag(holder);
//
//            }
//            holder = (ViewHolder) convertView.getTag();
//
//
//            final Contact contactDetails = getItem(position);
//            holder.mContactName.setText(contactDetails.getName());
//            holder.mCheckBox.setChecked(contactDetails.isSelected());
//            holder.mContactNumber.setText(contactDetails.getPhoneNumber());
//            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        listener.checkBoxSelected(position);
//                        contactDetails.setSelected(true);
//                        mDB.updateContact(contactDetails);
//
//                    } else {
//                        contactDetails.setSelected(false);
//                        mDB.updateContact(contactDetails);
//                        listener.checkBoxUnSelected(position);
//
//                    }
//                }
//            });
//
//
//            return convertView;
//        }
//
//        public LayoutInflater.Filter getFilter() {
//            return new LayoutInflater.Filter() {
//                @Override
//                public boolean onLoadClass(Class aClass) {
//                    return false;
//                }
//            };
//        }
//
//
//        private class ViewHolder {
//            private TextView mContactName;
//            private CheckBox mCheckBox;
//            private TextView mContactNumber;
//
//            public ViewHolder(View v) {
//
//                mContactName = (TextView) v.findViewById(R.id.tv_name);
//                mCheckBox = (CheckBox) v.findViewById(R.id.cb_user_selected);
//                mContactNumber = (TextView) v.findViewById(R.id.tv_phone_number);
//
//            }
//
//        }
//    }

    SearchView searchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_contacts, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_item_contacts_search);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setMaxWidth(Integer.MAX_VALUE);

        try {
            searchView.setQueryHint("Search");
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {


                    if (mContactAdapter != null && newText != null) {
                        mContactAdapter.getFilter().filter(newText);

                        return true;
                    }

                    return false;
                }


            });
        } catch (Exception e) {

            e.printStackTrace();

        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                    return true;
                }
                hideKeyboard();
                Intent data = new Intent();
                data.putExtra(CONTACTS_COUNT, contactsSelectedCount);
                setResult(RESULT_OK, data);
                NavUtils.navigateUpFromSameTask(this);
                finish();
                System.out.println("Count=== " +contactsSelectedCount);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        hideKeyboard();
        Intent data = new Intent();
        data.putExtra(CONTACTS_COUNT, contactsSelectedCount);
        setResult(RESULT_OK, data);
        super.onBackPressed();
        System.out.println("Count=== " +contactsSelectedCount);
        finish();
    }





}
