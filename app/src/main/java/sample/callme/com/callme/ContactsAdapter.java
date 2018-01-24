package sample.callme.com.callme;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 11/14/2017.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<Contact> contactList;
    private List<Contact> contactListFiltered;
    private ContactsAdapterListener listener;
    private Database mDatabase;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;
        public ImageView thumbnail, ivSelectedContacts;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tv_name);
            phone = (TextView) view.findViewById(R.id.tv_phone_number);
            thumbnail = (ImageView) view.findViewById(R.id.ivContactPhoto);
            ivSelectedContacts = (ImageView) view.findViewById(R.id.ivCheckMark);


        }
    }


    public ContactsAdapter(Context context, List<Contact> contactList, ContactsAdapterListener listener, Database mDB) {
        this.context = context;
        this.listener = listener;
        this.contactList = contactList;
        this.contactListFiltered = contactList;
        this.mDatabase = mDB;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_contact, parent, false);
        final MyViewHolder holder = new MyViewHolder(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Contact contact = contactListFiltered.get(position);
                if(holder.ivSelectedContacts.getVisibility()==View.VISIBLE){
                    contact.setSelected(false);
                }else{
                    contact.setSelected(true);
                }
                notifyDataSetChanged();
                mDatabase.updateContact(contact);
                listener.onContactSelected(contact);

            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Contact contact = contactListFiltered.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhoneNumber());

        if (contactListFiltered.get(position).getPhoto() != null) {
            holder.thumbnail.setImageDrawable(null);

            try {
                holder.thumbnail.setImageURI(Uri.parse(contactListFiltered.get(position).getPhoto()));

            } catch (Exception e) {
                holder.thumbnail.setImageResource(R.drawable.default_profile);

            }


        } else {
            holder.thumbnail.setImageDrawable(null);

            holder.thumbnail.setImageResource(R.drawable.default_profile);
        }
        if (contactListFiltered.get(position).isSelected()) {
            holder.ivSelectedContacts.setVisibility(View.VISIBLE);
        } else {
            holder.ivSelectedContacts.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    contactListFiltered = contactList;
                } else {
                    List<Contact> filteredList = new ArrayList<>();
                    for (Contact row : contactList) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getPhoneNumber().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList<Contact>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ContactsAdapterListener {
        void onContactSelected(Contact contact);
    }
}
