package com.dk.dropbox;

import android.app.Activity;
import android.content.Context;
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
            view = inflater.inflate(R.layout.list_row, parent, false);

            holder = new ViewHolder();
            holder.fileName = (TextView) view.findViewById(R.id.file);
            holder.imageView = (ImageView) view.findViewById(R.id.thumbnail);

            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.fileName.setText(file.fileName());

        // Display icon

        if (file.thumbExists) {
            GetThumbnail getThumbnail = new GetThumbnail(context.getApplicationContext(), dropboxAPI, holder.imageView, file);
            getThumbnail.execute();
        } else {
            if (file.isDir) {
                holder.imageView.setImageResource(R.drawable.ic_folder_open_grey_600_36dp);
            } else {

                int index = file.fileName().lastIndexOf(".");

                if (index > -1) {
                    String fileType = file.fileName().substring(index);

                    switch (fileType) {
                        case ".txt":
                            holder.imageView.setImageResource(R.drawable.ic_description_grey_600_36dp);
                            break;
                        case ".zip":
                            holder.imageView.setImageResource(R.drawable.ic_archive_grey_600_36dp);
                            break;
                        case ".mp3":
                            holder.imageView.setImageResource(R.drawable.ic_audiotrack_grey_600_36dp);
                            break;
                        default:
                            holder.imageView.setImageResource(R.drawable.ic_help_outline_grey_600_36dp);
                            break;
                    }
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_help_outline_grey_600_36dp);
                }
            }

        }

        return view;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView fileName;
    }
}
