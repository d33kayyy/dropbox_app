package com.dk.dropbox;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Entry> {
    private Context context;
    private DropboxAPI dropboxAPI = HomeScreen.getDropboxAPI();

    public CustomAdapter(Context context, int resourceId, List<Entry> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Entry file = getItem(position);
        View view = convertView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = inflater.inflate(R.layout.item, parent, false);

            holder = new ViewHolder();
            holder.fileName = (TextView) view.findViewById(R.id.prim_text);
            holder.imageView = (ImageView) view.findViewById(R.id.item_image);
            holder.fileSize = (TextView) view.findViewById(R.id.sec_text);

            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Display name
        String name = file.fileName();
        int len = file.fileName().length();
        if (len > 18){
            name = file.fileName().substring(0, 10) + "..." + file.fileName().substring(len-8);
        }
        holder.fileName.setText(name);

        // Display info
        if (!file.isDir) {
            String info = file.size + " - " + file.modified.substring(5, 16);
            holder.fileSize.setText(info);
        } else {
            holder.fileSize.setText(file.modified.substring(5, 16));
        }

        // Display icon

        if (file.thumbExists) {
            GetThumbnail getThumbnail = new GetThumbnail(context.getApplicationContext(), dropboxAPI, holder.imageView, file);
            getThumbnail.execute();
        } else {
            if (file.isDir) {
                holder.imageView.setBackground(getRoundedBackground(192, 192, 192));
                holder.imageView.setImageResource(R.drawable.ic_folder_white_24dp);
            } else {

                int index = file.fileName().lastIndexOf(".");

                if (index > -1) {
                    String fileType = file.fileName().substring(index);

                    switch (fileType) {
                        case ".txt":
                        case ".doc":
                        case ".docx":
                            holder.imageView.setBackground(getRoundedBackground(30, 144, 255));
                            holder.imageView.setImageResource(R.drawable.ic_description_white_36dp);
                            break;
                        case ".zip":
                        case ".rar":
                        case ".7z":
                            holder.imageView.setBackground(getRoundedBackground(50, 205, 50));
                            holder.imageView.setImageResource(R.drawable.ic_archive_white_36dp);
                            break;
                        case ".mp3":
                        case ".m4a":
                        case ".wav":
                        case ".flac":
                            holder.imageView.setBackground(getRoundedBackground(255, 215, 0));
                            holder.imageView.setImageResource(R.drawable.ic_audiotrack_white_36dp);
                            break;
                        case ".py":
                        case ".c":
                        case ".java":
                            holder.imageView.setBackground(getRoundedBackground(255, 0, 0));
                            holder.imageView.setImageResource(R.drawable.ic_code_white_36dp);
                            break;
                        case ".pdf":
                            holder.imageView.setBackground(getRoundedBackground(255, 0, 0));
                            holder.imageView.setImageResource(R.mipmap.pdf);
                            break;
                        default:
                            holder.imageView.setBackground(getRoundedBackground(138, 43, 226));
                            holder.imageView.setImageResource(R.drawable.ic_help_outline_white_36dp);
                            break;
                    }
                } else {
                    holder.imageView.setBackground(getRoundedBackground(138, 43, 226));
                    holder.imageView.setImageResource(R.drawable.ic_help_outline_white_36dp);
                }
            }
        }

        return view;
    }

    // Create a circle drawable that can be used as background
    private Drawable getRoundedBackground(int r, int g, int b) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setFilterBitmap(true);

        Bitmap bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRGB(r, g, b);

        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        drawable.setCircular(true);

        return drawable;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView fileName;
        TextView fileSize;
    }
}
