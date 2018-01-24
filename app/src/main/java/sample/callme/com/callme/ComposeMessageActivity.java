package sample.callme.com.callme;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeMessageActivity extends AppCompatActivity {

    private EditText mComposeMessageDescription;
    private EditText mSignatureEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Compose Message");
        }
        mSignatureEditText = (EditText) findViewById(R.id.edit_text_signature);
        mComposeMessageDescription = (EditText) findViewById(R.id.edit_text_compose_message);

        if (!Utils.getOutGoingMessage(ComposeMessageActivity.this).isEmpty()) {
            mComposeMessageDescription.setText(Utils.getOutGoingMessage(ComposeMessageActivity.this));
        }

        findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mComposeMessageDescription.getText().toString().isEmpty() || mSignatureEditText.getText().toString()
                        .isEmpty()){

                    Toast.makeText(ComposeMessageActivity.this, "Can not save a empty message", Toast.LENGTH_SHORT).show();
                }else {
                    isGoingBack();
                   finish();
                }
            }
        });
        mSignatureEditText.setText(Utils.getSignature(ComposeMessageActivity.this));

    }
    private void hideKeyboard() {
        final View currentFocus = this.getCurrentFocus();

        if (currentFocus != null) {
            ((InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        isGoingBack();
        super.onBackPressed();

    }

    private void isGoingBack(){
        if (!mComposeMessageDescription.getText().toString().isEmpty()) {
            Utils.setOutGoingMessage(ComposeMessageActivity.this , mComposeMessageDescription.getText().toString().trim());
        }
        if (!mSignatureEditText.getText().toString().isEmpty()) {
            Utils.setSignature(ComposeMessageActivity.this , mSignatureEditText.getText().toString().trim());
        }
        hideKeyboard();
    }
}
