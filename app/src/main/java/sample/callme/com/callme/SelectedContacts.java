package sample.callme.com.callme;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectedContacts extends DialogFragment implements RemoveContactListener{


    private ListView selectedContactList;
    private ContactListAdapter adapter;
    private ImageView closeButton ;
    public Database mDB = null;


    public static SelectedContacts getInstance(List<Contact> contacts) {
        Bundle b = new Bundle();
        b.putSerializable(ContactsActivity.CONTACTS_LIST , (Serializable) contacts);
        SelectedContacts detailsFragment = new SelectedContacts();
        detailsFragment.setArguments(b);
        return detailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View convertView = inflater.inflate(R.layout.activity_selected_contacts,container,false);
        selectedContactList = (ListView) convertView.findViewById(R.id.lv_selected_contacts);
        closeButton = (ImageView) convertView.findViewById(R.id.tv_close_button);
        mDB = new Database(getActivity());
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        if (mDB.getSelectedContacts() != null && mDB.getSelectedContacts().size()>0) {
            adapter = new ContactListAdapter(getActivity() , this);
            selectedContactList.setAdapter(adapter);
        }
        return convertView;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_contacts);



    }*/



    @Override
    public void onClick(Contact contact) {
            contact.setSelected(false);
           mDB.updateContact(contact);
        ((MainActivity)getActivity()).updateCount();
            if ( mDB.getSelectedContacts().size()>0 ) {
                adapter.updateAdapter();
                adapter.notifyDataSetChanged();
            } else {
                dismiss();
            }

    }


    public class ContactListAdapter extends BaseAdapter{

        private Context context;
        private List<Contact> detailsofUsers;
        private RemoveContactListener listener ;

        public ContactListAdapter(Context context ,RemoveContactListener listener) {
         this.context = context;
            this.detailsofUsers = mDB.getSelectedContacts();
            this.listener = listener ;
        }

        public void updateAdapter(){
            detailsofUsers = mDB.getSelectedContacts();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return detailsofUsers.size();
        }

        @Override
        public Contact getItem(int position) {
            return detailsofUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            HolderView hv ;
            final Contact details = getItem(position);
            if(convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.contact_view , null);
                hv = new HolderView(convertView);
                convertView.setTag(hv);
            }

            hv = (HolderView) convertView.getTag();
            hv.contactNumber.setVisibility(View.VISIBLE);
            hv.contactName.setText(details.getName());
            hv.contactNumber.setText(details.getPhoneNumber());

            hv.deleteContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(details);
                }
            });


            return convertView;
        }



        public class HolderView {
            TextView contactName ;
            TextView contactNumber;
            ImageView deleteContact ;
            public HolderView(View v) {
                contactName = (TextView) v.findViewById(R.id.tv_contact_name);
                contactNumber = (TextView) v.findViewById(R.id.tv_contact_phone);
                deleteContact = (ImageView) v.findViewById(R.id.iv_delete_icon);
            }
        }
    }
}
