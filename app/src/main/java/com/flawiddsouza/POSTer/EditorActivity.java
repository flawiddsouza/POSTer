package com.flawiddsouza.POSTer;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONObject;

public class EditorActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextUrl;
    private EditText editTextParameter;
    private CheckBox checkBoxAppendHeading;
    private EditText editTextStaticParameters;
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

        editTextName = findViewById(R.id.name);
        editTextUrl = findViewById(R.id.url);
        editTextParameter = findViewById(R.id.parameter);
        checkBoxAppendHeading = findViewById(R.id.appendHeading);
        editTextStaticParameters = findViewById(R.id.staticParameters);

        handler = POSTerDatabaseHandler.getInstance(this);

        Bundle bundle = getIntent().getExtras();
        edit = bundle.getBoolean("edit");

        if(edit) {
            actionBar.setTitle("Edit Entry");

            id = bundle.getLong("id");
            entry = handler.getEntry(id);

            editTextName.setText(entry.name);
            editTextUrl.setText(entry.url);
            editTextParameter.setText(entry.parameter);
            checkBoxAppendHeading.setChecked(entry.appendHeading);
            editTextStaticParameters.setText(entry.staticParameters);

            editTextName.setSelection(editTextName.getText().length()); // Place cursor at the end of text of the first TextBox
        }
    }

    @Override
    public void onBackPressed() {
        Entry entry = new Entry();
        entry.name = editTextName.getText().toString();
        entry.url = editTextUrl.getText().toString();
        entry.parameter = editTextParameter.getText().toString();
        entry.appendHeading = checkBoxAppendHeading.isChecked();
        entry.staticParameters = editTextStaticParameters.getText().toString();

        if(entry.staticParameters.length() > 0) {
            try {
                new JSONObject(entry.staticParameters);
            } catch (Exception e) {
                editTextStaticParameters.setError("Invalid JSON given for static parameters");
                return;
            }
        }

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
        entry.name = editTextName.getText().toString();
        entry.url = editTextUrl.getText().toString();
        entry.parameter = editTextParameter.getText().toString();
        entry.appendHeading = checkBoxAppendHeading.isChecked();
        entry.staticParameters = editTextStaticParameters.getText().toString();

        if(entry.staticParameters.length() > 0) {
            try {
                new JSONObject(entry.staticParameters);
            } catch (Exception e) {
                editTextStaticParameters.setError("Invalid JSON given for static parameters");
                return false;
            }
        }

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