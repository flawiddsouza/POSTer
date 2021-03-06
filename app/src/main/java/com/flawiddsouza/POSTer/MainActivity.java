package com.flawiddsouza.POSTer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class MainActivity extends AppCompatActivity {

    POSTerCursorAdapter posterAdapter;
    POSTerDatabaseHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView myListView = findViewById(R.id.mainListView);
        posterAdapter = new POSTerCursorAdapter(this, createCursor());
        myListView.setAdapter(posterAdapter);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (action.equals(Intent.ACTION_SEND) && type.equals("text/plain")) {

            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setEnabled(false);
            fab.setVisibility(View.GONE);

            String receivedTextSubject = intent.hasExtra(Intent.EXTRA_SUBJECT) ? intent.getStringExtra(Intent.EXTRA_SUBJECT) : "";
            String receivedTextBody = intent.hasExtra(Intent.EXTRA_TEXT) ? intent.getStringExtra(Intent.EXTRA_TEXT) : "";

            myListView.setOnItemClickListener((parent, view, position, id) -> handleShare(id, receivedTextSubject, receivedTextBody));

        } else if (action.equals(Intent.ACTION_MAIN)) { // app has been launched directly, not from share list
            registerForContextMenu(myListView); // Register the ListView for Context menu
            myListView.setOnItemClickListener((parent, view, position, id) -> runEntry(id));
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        posterAdapter.swapCursor(createCursor());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0, view.getId(), 0, "Edit"); // groupId, itemId, order, title
        menu.add(0, view.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo activeListItem = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(item.getTitle()=="Edit"){
            editEntry(activeListItem.id);
        }
        else if(item.getTitle()=="Delete"){
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure?")
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        handler.deleteEntry(activeListItem.id);
                        posterAdapter.swapCursor(createCursor());
                        Toast.makeText(MainActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else{
            return false;
        }
        return true;
    }

    public Cursor createCursor() {
        handler = POSTerDatabaseHandler.getInstance(this);
        SQLiteDatabase db = handler.getReadableDatabase();
        return db.rawQuery("SELECT * FROM entries ORDER BY _id DESC", null);
    }

    private static String bodyToString(final RequestBody request){
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.readUtf8();
        }
        catch (final IOException e) {
            return "did not work";
        }
    }

    private void post(String url, String parameter, String heading, String body, Boolean appendHeading, String jsonToAppend) throws IOException, IllegalArgumentException {
        OkHttpClient client = new OkHttpClient();

        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        if(jsonToAppend.length() > 0) {
            try {
                JSONObject parsedJSON = new JSONObject(jsonToAppend);
                parsedJSON.keys().forEachRemaining(key -> {
                    try {
                        String keyValue = parsedJSON.getString(key);
                        keyValue = keyValue.replace("{{heading}}", heading != "" ? heading : body);
                        formBodyBuilder.add(key, keyValue);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            formBodyBuilder.add(parameter, body);
        } else {
            String shareString;
            if(!heading.isEmpty() && appendHeading) {
                shareString = heading + '\n' + body;
            } else {
                shareString = body;
            }
            formBodyBuilder.add(parameter, shareString);
        }

        RequestBody formBody = formBodyBuilder.build();

//        Log.d("DEBUG_POST - formBody", bodyToString(formBody));

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String res = response.body().string();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, res, Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void postClipboard(long id) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip())
        {
            ClipData data = clipboard.getPrimaryClip();
            if (data.getItemCount() > 0)
            {
                CharSequence clipboardText = data.getItemAt(0).coerceToText(this);
                if (clipboardText != null)
                {
                    Entry entry = handler.getEntry(id);
                    try {
                        post(entry.url, entry.parameter, "", clipboardText.toString(), entry.appendHeading, entry.staticParameters);
                    } catch (IOException | IllegalArgumentException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MainActivity.this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void createEntry(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("edit", false);
        startActivity(intent);
    }

    public void editEntry(long id) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra("edit", true);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    private void runEntry(long id) {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to POST to this url?")
                .setPositiveButton("Yes", (dialog, whichButton) -> {
                    postClipboard(id);
                })
                .setNegativeButton("No", null).show();
    }

    private void handleShare(long id, String subject, String body) {
        Entry entry = handler.getEntry(id);

        try {
            post(entry.url, entry.parameter, subject, body, entry.appendHeading, entry.staticParameters);
        } catch (IOException | IllegalArgumentException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}