package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class RenameFile extends AsyncTask<Void, Void, Boolean> {
    private DropboxAPI dropboxAPI;
    private String path, fileName, newName;
    private Context context;
    private ProgressDialog dialog;

    public RenameFile(Context context, DropboxAPI dropboxAPI, String path, String fileName, String newName) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.path = path;
        this.newName = newName;
        this.fileName = fileName;
        dialog = new ProgressDialog(context);
        dialog.setMessage("Renaming File");
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            dropboxAPI.move(path + fileName, path + newName);
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
