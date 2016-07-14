package com.dk.dropbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CustomAdapter extends ArrayAdapter<Entry> {
    private static final String PATH = "/"; // Root folder

    private ArrayList<String> listFolder = new ArrayList<String>();
    private Context context;
    private DropboxAPI dropboxAPI = HomeScreen.getDropboxAPI();
    private Entry entry;
    private String fileSelected, newPath, newName;
    private EditText input, editFile;
    private String[] folders;

    public CustomAdapter(Context context, int resourceId, List<Entry> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final Entry file = getItem(position);
        View view = convertView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = inflater.inflate(R.layout.item, parent, false);

            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.item_image);
            holder.fileName = (TextView) view.findViewById(R.id.prim_text);
            holder.fileSize = (TextView) view.findViewById(R.id.sec_text);
            holder.imgBtn = (ImageView) view.findViewById(R.id.img_btn);
            holder.imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PopupMenu popupMenu = new PopupMenu(context, view);
                    MenuInflater inflater = popupMenu.getMenuInflater();
                    inflater.inflate(R.menu.context_menu, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

                            entry = file;
                            fileSelected = entry.fileName();

                            input = new EditText(context);
                            input.setTextColor(Color.BLACK);

                            editFile = new EditText(context);
                            editFile.setTextColor(Color.BLACK);

                            switch (item.getItemId()) {
                                case R.id.edit:

                                    if (fileSelected.contains(".txt")) {
                                        OpenFile openFile = new OpenFile(dropboxAPI, PATH, fileSelected, editFile);
                                        openFile.execute();

                                        new AlertDialog.Builder(context)
                                                .setView(editFile)
                                                .setMessage(fileSelected)
                                                .setCancelable(false)
                                                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        String newContent = editFile.getText().toString();
                                                        File file = new File(context.getFilesDir(), fileSelected);
                                                        try {
                                                            FileWriter fileWriter = new FileWriter(file);
                                                            fileWriter.write(newContent);
                                                            fileWriter.close();

                                                            OverWriteFile overWr = new OverWriteFile(context, dropboxAPI, PATH, file.getPath());
                                                            overWr.execute();

                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                })
                                                .setNegativeButton("Cancel", null)
                                                .show();
                                    } else {
                                        showToast("Cannot edit this file");
                                    }
                                    return true;

                                case R.id.rename:

                                    input.setText(fileSelected);
                                    new AlertDialog.Builder(context)
                                            .setView(input)
                                            .setMessage("Enter the new file name")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    newName = input.getText().toString();
                                                    if (!newName.equals("")) {
                                                        rename(entry.path);
                                                    }
                                                }
                                            })
                                            .setNegativeButton("No", null)
                                            .show();
                                    return true;

                                case R.id.move:

                                    showFolders(entry.parentPath());

                                    new AlertDialog.Builder(context)
                                            .setTitle("Select Folder")
                                            .setCancelable(true)
                                            .setNegativeButton("Cancel", null)
                                            .setSingleChoiceItems(folders, 0, null)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    dialog.dismiss();
                                                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                                    newPath = folders[position];
                                                    move(entry.path);
                                                }
                                            })
                                            .show();
                                    return true;

                                case R.id.delete:

                                    new AlertDialog.Builder(context)
                                            .setMessage("Are you sure you want to delete " + fileSelected + " ?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    delete(entry.path);
                                                }
                                            })
                                            .setNegativeButton("No", null)
                                            .show();
                                    return true;

                                case R.id.download:

                                    if (!entry.isDir) {
                                        DownloadFile downloadFile = new DownloadFile(context, dropboxAPI, entry);
                                        downloadFile.execute();
                                    } else {
                                        showToast("Folder download unavailable");
                                    }
                                    return true;

                                default:
                                    return false;
                            }
                        }
                    });


                }
            });
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Display name
        String name = file.fileName();
        int len = file.fileName().length();
        if (len > 18) {
            name = file.fileName().substring(0, 10) + "..." + file.fileName().substring(len - 8);
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
                            holder.imageView.setBackground(getRoundedBackground(255, 105, 180));
                            holder.imageView.setImageResource(R.drawable.ic_code_white_36dp);
                            break;
                        case ".pdf":
                            holder.imageView.setBackground(getRoundedBackground(255, 0, 0));
                            holder.imageView.setImageResource(R.mipmap.pdf);
                            break;
                        default:
                            holder.imageView.setBackground(getRoundedBackground(3, 169, 244));
                            holder.imageView.setImageResource(R.drawable.ic_insert_drive_file_white_36dp);
                            break;
                    }
                } else {
                    holder.imageView.setBackground(getRoundedBackground(3, 169, 244));
                    holder.imageView.setImageResource(R.drawable.ic_insert_drive_file_white_36dp);
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
        ImageView imgBtn;
    }

    private void delete(String path) {
        DeleteFile deleteFile = new DeleteFile(context, dropboxAPI, path);
        deleteFile.execute();
    }

    private void move(String path) {
        MoveFile moveFile = new MoveFile(context, dropboxAPI, path, newPath, fileSelected);
        moveFile.execute();
    }

    private void rename(String path) {
        RenameFile renameFile = new RenameFile(context, dropboxAPI, path, fileSelected, newName);
        renameFile.execute();
    }

    private void showFolders(String path) { // TODO: Show path of all related
        listFolder.clear();

        // if the current path is not root, add root
        if (!path.equals("/")) {
            listFolder.add("/");
        }

        ListFolder listOfFolder = new ListFolder(dropboxAPI, PATH, listFolder);
        try {
            // get list of folder
            listOfFolder.execute().get();
            if (!path.equals("/")) {
                listOfFolder = new ListFolder(dropboxAPI, path, listFolder);
                listOfFolder.execute().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Remove the current folder
        String current = path.substring(0, path.length() - 1);
        listFolder.remove(current);

        folders = listFolder.toArray(new String[listFolder.size()]);
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}