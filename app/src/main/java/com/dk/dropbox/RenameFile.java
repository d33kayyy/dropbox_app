package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

public class RenameFile extends AsyncTask<Void, Void, Boolean> {
    private DropboxAPI dropboxAPI;
    private String newName;
    private Entry entry;
    private Context context;
    private ProgressDialog dialog;

    public RenameFile(Context context, DropboxAPI dropboxAPI, Entry entry, String newName) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.entry = entry;
        this.newName = newName;
        dialog = new ProgressDialog(context);
        dialog.setMessage("Renaming File");
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            dropboxAPI.move(entry.path, entry.parentPath() + newName);
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
            Toast.makeText(context, "Rename file successfully", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the rename request",
                    Toast.LENGTH_LONG).show();
        }

    }
}
