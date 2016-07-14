package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

public class MoveFile extends AsyncTask<Void, Void, Boolean> {
    private static final String PATH = "/";

    private DropboxAPI dropboxAPI;
    private String newPath;
    private Entry entry;
    private Context context;
    private ProgressDialog dialog;

    public MoveFile(Context context, DropboxAPI dropboxAPI, Entry entry, String newPath) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.entry = entry;
        this.newPath = newPath;
        dialog = new ProgressDialog(context);
        dialog.setMessage("Moving File");
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            dropboxAPI.move(entry.path, newPath + "/" + entry.fileName());
            return true;
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.dismiss();
        if (result) {
            Toast.makeText(context, "File has been moved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the move request",
                    Toast.LENGTH_LONG).show();
        }
    }
}
