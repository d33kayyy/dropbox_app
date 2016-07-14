package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadFile extends AsyncTask<Void, Long, Boolean> {
    private Context context;
    private DropboxAPI dropboxAPI;
    private ProgressDialog dialog;
    private long fileSize;
    private Entry entry;

    public DownloadFile(Context context, DropboxAPI dropboxAPI, Entry entry) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.entry = entry;
        dialog = new ProgressDialog(context);
        dialog.setMessage("Downloading File");
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        // Just to get the file size

        fileSize = entry.bytes;

        // Download file

        String filePath = context.getFilesDir().getPath() + "/" + entry.fileName();
        File file = new File(filePath);
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            dropboxAPI.getFile(entry.path, null, outputStream, new ProgressListener() {
                @Override
                public long progressInterval() {
                    // Update the progress bar every half-second or so
                    return 500;
                }

                @Override
                public void onProgress(long current, long total) {
                    publishProgress(current);
                }
            });
            outputStream.close();
            return true;

        } catch (DropboxException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        super.onProgressUpdate(progress);
        dialog.setIndeterminate(false);
        int percent = (int) (100 * progress[0] / fileSize);
        dialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.dismiss();
        if (result) {
            Toast.makeText(context, "File has been downloaded!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the download request",
                    Toast.LENGTH_LONG).show();
        }
    }
}

