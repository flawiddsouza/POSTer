package com.flawiddsouza.POSTer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class POSTerCursorAdapter extends CursorAdapter {

    public POSTerCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView) view.findViewById(R.id.text);
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String url = cursor.getString(cursor.getColumnIndexOrThrow("url"));
        if(name != null && !name.isEmpty()) {
            textView.setText(name);
        } else {
            textView.setText(url);
        }
    }
}
