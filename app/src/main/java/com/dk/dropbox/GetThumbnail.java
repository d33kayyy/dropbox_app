package com.dk.dropbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class GetThumbnail extends AsyncTask<Void, Void, Boolean> {
    private ImageView view;
    private DropboxAPI dropboxAPI;
    private Context context;
    private Bitmap thumbnail;
    private Entry entry;

    public GetThumbnail(Context context, DropboxAPI dropboxAPI, ImageView view, Entry entry) {
        this.context = context.getApplicationContext();
        this.dropboxAPI = dropboxAPI;
        this.view = view;
        this.entry = entry;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            String cachePath = context.getCacheDir().getAbsolutePath() + "/" + entry.fileName();
            String epath = entry.path;

            FileOutputStream os;
            try {
                os = new FileOutputStream(cachePath);
            } catch (FileNotFoundException e) {
                return false;
            }

            dropboxAPI.getThumbnail(epath, os, DropboxAPI.ThumbSize.ICON_128x128,
                    DropboxAPI.ThumbFormat.JPEG, null);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            thumbnail = BitmapFactory.decodeFile(cachePath, bmOptions);

//            thumbnail = Drawable.createFromPath(cachePath);
            return true;

        } catch (DropboxException e1) {
            e1.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), thumbnail);
            drawable.setCircular(true);
            view.setBackground(drawable);
        } else {
            Bitmap bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawRGB(222, 184, 135);

            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            drawable.setCircular(true);

            view.setBackground(drawable);
            view.setImageResource(R.drawable.ic_photo_white_36dp);
        }
    }
}
