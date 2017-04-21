package com.flawiddsouza.POSTer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class EditorActivity extends AppCompatActivity {

    private EditText editTextUrl;
    private EditText editTextParameter;
    private boolean edit;
    private long id;
    POSTerDatabaseHandler handler;
    Entry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Add Entry");

        editTextUrl = (EditText) findViewById(R.id.url);
        editTextParameter = (EditText) findViewById(R.id.parameter);
        handler = POSTerDatabaseHandler.getInstance(this);

        Bundle bundle = getIntent().getExtras();
        edit = bundle.getBoolean("edit");

        if(edit) {
            actionBar.setTitle("Edit Entry");
            id = bundle.getLong("id");
            entry = handler.getEntry(id);
            editTextUrl.setText(entry.url);
            editTextUrl.setSelection(editTextUrl.getText().length()); // Place cursor at the end of text
            editTextParameter.setText(entry.parameter);
        }
    }

    @Override
    public void onBackPressed() {
        Entry entry = new Entry();
        entry.url = editTextUrl.getText().toString();
        entry.parameter = editTextParameter.getText().toString();

        if(entry.url.isEmpty() && !entry.parameter.isEmpty()) { // if url is empty & parameter isn't
            editTextUrl.setError(getString(R.string.editor_url_error));
        }
        else if(!entry.url.isEmpty() && entry.parameter.isEmpty()) { // if parameter is empty & url isn't
            editTextParameter.setError(getString(R.string.editor_parameter_error));
        }
        else if(entry.url.isEmpty() && entry.parameter.isEmpty()) { // if both are empty
            if(edit) {
                handler.deleteEntry(id);
            }
            finish();
        } else { // if both are filled
            if(edit) {
                handler.updateEntry(id, entry);
            } else {
                handler.addEntry(entry);
            }
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp(){ // same code as onBackPressed(), only return statements are additional
        Entry entry = new Entry();
        entry.url = editTextUrl.getText().toString();
        entry.parameter = editTextParameter.getText().toString();

        if(entry.url.isEmpty() && !entry.parameter.isEmpty()) { // if url is empty & parameter isn't
            editTextUrl.setError(getString(R.string.editor_url_error));
            return false;
        }
        else if(!entry.url.isEmpty() && entry.parameter.isEmpty()) { // if parameter is empty & url isn't
            editTextParameter.setError(getString(R.string.editor_parameter_error));
            return false;
        }
        else if(entry.url.isEmpty() && entry.parameter.isEmpty()) { // if both are empty
            if(edit) {
                handler.deleteEntry(id);
            }
            finish();
            return true;
        } else { // if both are filled
            if(edit) {
                handler.updateEntry(id, entry);
            } else {
                handler.addEntry(entry);
            }
            finish();
            return true;
        }
    }
}