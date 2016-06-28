package com.dk.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UploadFile extends AsyncTask<Void, Long, Boolean> {
    private Context context;
    private DropboxAPI dropboxAPI;
    private String folderPath, filePath;
    private ProgressDialog dialog;
    private Long fileSize;

    public UploadFile(Context context, DropboxAPI dropboxAPI, String folderPath, String filePath) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.folderPath = folderPath;
        this.filePath = filePath;
        dialog = new ProgressDialog(context);
        dialog.setTitle("Upload File");
        dialog.setMessage("Please wait...");
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

        try {

            File file = new File(filePath);
            fileSize = file.length();
            FileInputStream fileInputStream = new FileInputStream(file);

            try {
                dropboxAPI.putFile(folderPath + file.getName(), fileInputStream, file.length(), null, new ProgressListener() {
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
            Toast.makeText(context, "File has been uploaded!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error occurred while processing the upload request",
                    Toast.LENGTH_LONG).show();
        }
    }
}
