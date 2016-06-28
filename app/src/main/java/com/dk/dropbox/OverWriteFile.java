package com.dk.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class OverWriteFile extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private DropboxAPI dropboxAPI;
    private String folderPath, filePath;

    public OverWriteFile(Context context, DropboxAPI dropboxAPI, String folderPath, String filePath) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.folderPath = folderPath;
        this.filePath = filePath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            try {
                dropboxAPI.putFileOverwrite(folderPath + file.getName(), fileInputStream, file.length(), null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }

            fileInputStream.close();
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File changes have been saved", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the overwrite request",
                    Toast.LENGTH_LONG).show();
        }

    }
}

