package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

public class SyncFile extends AsyncTask<Void, Void, ArrayList<Entry>> {
    private DropboxAPI dropboxAPI;
    private Context context;
    private String path;
    private ArrayAdapter<Entry> adapter;
    private ProgressDialog dialog;

    public SyncFile(Context context, DropboxAPI dropboxAPI, String path, ArrayAdapter<Entry> adapter) {
        super();
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.path = path;
        this.adapter = adapter;
        dialog = new ProgressDialog(context);
        dialog.setTitle("Please wait");
        dialog.setMessage("Syncing file...");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.show();
    }

    @Override
    protected ArrayList<Entry> doInBackground(Void... params) {
        ArrayList<Entry> listFile = new ArrayList<Entry>();

        try {
            Entry listEntry = dropboxAPI.metadata(path, 100, null, true, null);
            for (Entry item : listEntry.contents) {
                listFile.add(item);
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return listFile;
    }

    @Override
    protected void onPostExecute(ArrayList<Entry> listFile) {
        if (listFile != null) {
            adapter.clear();

            // Display folder first
            for (Entry item : listFile) {
                if (item.isDir)
                    adapter.add(item);
            }

            for (Entry item : listFile) {
                if (!item.isDir)
                    adapter.add(item);
            }
        }
        dialog.dismiss();
    }
}