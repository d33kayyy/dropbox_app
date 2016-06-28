package com.dk.dropbox;

import android.os.AsyncTask;
import android.widget.EditText;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

public class OpenFile extends AsyncTask<Void, Void, String> {
    private DropboxAPI dropboxAPI;
    private String fileSelected, path;
    private EditText edit;

    public OpenFile(DropboxAPI dropboxAPI, String path, String fileSelected, EditText edit) {
        this.dropboxAPI = dropboxAPI;
        this.fileSelected = fileSelected;
        this.path = path;
        this.edit = edit;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            DropboxAPI.Entry listEntry = dropboxAPI.metadata(path, 100, null, true, null);
            for (DropboxAPI.Entry entry : listEntry.contents) {
                if (entry.fileName().equals(fileSelected)) {
                    File file = File.createTempFile("temp_file", ".txt");
                    OutputStream outStream = new FileOutputStream(file);
                    dropboxAPI.getFile(path + "/" + fileSelected, null, outStream, null);

                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        StringBuffer fileContent = new StringBuffer();
                        while ((line = reader.readLine()) != null) {
                            fileContent.append(line + "\n");
                        }
                        reader.close();
                        file.delete();
                        return fileContent.toString();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (DropboxException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
        if (str != null) {
            edit.setText(str);
        }
    }
}