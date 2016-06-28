package com.dk.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class DeleteFile extends AsyncTask<Void, Void, Boolean> {
    private DropboxAPI dropboxAPI;
    private String path;
    private Context context;

    public DeleteFile(Context context, DropboxAPI dropboxAPI, String path) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.path = path;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            dropboxAPI.delete(path);
            return true;
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File has been deleted!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the delete request",
                    Toast.LENGTH_LONG).show();
        }
    }
}
