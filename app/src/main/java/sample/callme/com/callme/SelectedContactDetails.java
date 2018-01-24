package sample.callme.com.callme;

import java.io.Serializable;

/**
 * Created by rahul on 10/3/17.
 */

public class SelectedContactDetails implements Serializable {

    private String name;
    private String contactNumber ;

    public SelectedContactDetails(String name, String contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
