package com.dk.dropbox;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

public class ListFolder extends AsyncTask<Void, Void, ArrayList<String>> {
    private DropboxAPI dropboxAPI;
    private String path;
    private ArrayList<String> listFolder;

    public ListFolder(DropboxAPI dropboxAPI, String path, ArrayList<String> listFolder) {
        this.dropboxAPI = dropboxAPI;
        this.path = path;
        this.listFolder = listFolder;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {

        try {
            Entry listEntry = dropboxAPI.metadata(path, 100, null, true, null);
            for (Entry item : listEntry.contents) {
                if (item.isDir) {
                    listFolder.add(item.path);
                }
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return listFolder;
    }
}
