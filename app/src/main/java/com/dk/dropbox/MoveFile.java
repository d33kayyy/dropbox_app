package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class MoveFile extends AsyncTask<Void, Void, Boolean> {
    private static final String PATH = "/";

    private DropboxAPI dropboxAPI;
    private String path, fileName, newPath;
    private Context context;
    private ProgressDialog dialog;

    public MoveFile(Context context, DropboxAPI dropboxAPI, String path, String newPath, String fileName) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.path = path;
        this.newPath = newPath;
        this.fileName = fileName;
        dialog = new ProgressDialog(context);
        dialog.setMessage("Moving File");
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            dropboxAPI.move(path + fileName, PATH + newPath + "/" + fileName);
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
            Toast.makeText(context, fileName + " has been moved!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the move request",
                    Toast.LENGTH_LONG).show();
        }
    }
}
