package com.dk.dropbox;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Folder extends AppCompatActivity {
    private static final String PATH = "/";
    private static final int FILE_SELECT_CODE = 0;

    // Variables
    private DropboxAPI dropboxAPI = HomeScreen.getDropboxAPI();
    private ArrayList<Entry> listFile;
    private ArrayList<String> listFolder = new ArrayList<String>();
    private ArrayAdapter<Entry> adapter;
    private ListView listView;
    private GridView gridView;
    private EditText input, editFile;
    private String folderName, folderPath, fileSelected, newPath, newName, filePath;
    private String[] folders;
    private Entry entry;

    private Toolbar toolbar;

    public void back() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_layout);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_navigate_before_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get folder name from HomeScreen
        Intent intent = this.getIntent();
        folderName = intent.getStringExtra(Intent.EXTRA_TEXT);
        getSupportActionBar().setTitle(folderName);
        folderPath = PATH + folderName + "/";

        // CustomAdapter to display files and thumbnails

        listFile = new ArrayList<Entry>();

        adapter = new CustomAdapter(this, R.id.listView, listFile);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new ItemClickListener());

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new ItemClickListener());

        registerForContextMenu(gridView);
        registerForContextMenu(listView);

        sync();

        loadSetting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dropboxAPI.getSession().isLinked()) {
            backToLogin();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_layout);

        SharedPreferences sharedPref = this.getSharedPreferences(
                "AppPreferences", Context.MODE_PRIVATE);
        String layout = sharedPref.getString("Layout", "Grid");

        if (layout.equals("Grid")) {
            item.setIcon(R.drawable.ic_list_white_24dp);
        } else {
            item.setIcon(R.drawable.ic_apps_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            sync();
            return true;

        } else if (id == R.id.action_upload) {
            showFileChooser();
            return true;

        } else if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            logOut();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;

        } else if (id == R.id.action_layout) {
            SharedPreferences sharedPref = this.getSharedPreferences(
                    "AppPreferences", Context.MODE_PRIVATE);
            String layout = sharedPref.getString("Layout", "Grid");

            SharedPreferences.Editor editor = sharedPref.edit();

            if (layout.equals("Grid")) {
                item.setIcon(R.drawable.ic_apps_white_24dp);
                editor.putString("Layout", "List");
                editor.apply();
            } else {
                item.setIcon(R.drawable.ic_list_white_24dp);
                editor.putString("Layout", "Grid");
                editor.apply();
            }

            loadSetting();

            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        entry = adapter.getItem(info.position);
        fileSelected = entry.fileName();

        input = new EditText(this);
        input.setTextColor(Color.BLACK);

        editFile = new EditText(this);
        editFile.setTextColor(Color.BLACK);

        switch (item.getItemId()) {
            case R.id.edit:

                if (fileSelected.contains(".txt")) {
                    OpenFile openFile = new OpenFile(dropboxAPI, folderPath, fileSelected, editFile);
                    openFile.execute();

                    new AlertDialog.Builder(this)
                            .setView(editFile)
                            .setMessage(fileSelected)
                            .setCancelable(false)
                            .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String newContent = editFile.getText().toString();
                                    File file = new File(getApplicationContext().getFilesDir(), fileSelected);
                                    try {
                                        FileWriter fileWriter = new FileWriter(file);
                                        fileWriter.write(newContent);
                                        fileWriter.close();

                                        OverWriteFile overWr = new OverWriteFile(getApplicationContext(), dropboxAPI, folderPath, file.getPath());
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
                new AlertDialog.Builder(this)
                        .setView(input)
                        .setMessage("Enter the new file name")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                newName = input.getText().toString();
                                if (!newName.equals("")) {
                                    rename();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;

            case R.id.move:

                showFolders();

                new AlertDialog.Builder(this)
                        .setTitle("Select Folder")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", null)
                        .setSingleChoiceItems(folders, 0, null)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                newPath = folders[position];
                                move();
                            }
                        })
                        .show();
                return true;

            case R.id.delete:

                new AlertDialog.Builder(this)
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

                if ((fileSelected.substring(fileSelected.length() - 4)).contains(".")) {
                    DownloadFile downloadFile = new DownloadFile(this, dropboxAPI, entry);
                    downloadFile.execute();
                } else {
                    showToast("Folder download unavailable");
                }

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                // get real path from uri
                filePath = getPath(getApplicationContext(), uri);

                upload();
            }
        }
    }

    // Methods

    private void loadSetting() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                "AppPreferences", Context.MODE_PRIVATE);
        String layout = sharedPref.getString("Layout", "Grid");

        if (layout.equals("List")) {
            listView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }

    }

    private void sync() {
        SyncFile syncFile = new SyncFile(this, dropboxAPI, folderPath, adapter);
        syncFile.execute();
    }

    private void upload() {
        UploadFile uploadFile = new UploadFile(this, dropboxAPI, folderPath, filePath);
        uploadFile.execute();
    }

    private void delete(String path) {
        DeleteFile deleteFile = new DeleteFile(this, dropboxAPI, path);
        deleteFile.execute();
    }

    private void move() {
        MoveFile moveFile = new MoveFile(this, dropboxAPI, folderPath, newPath, fileSelected);
        moveFile.execute();
    }

    private void rename() {
        RenameFile renameFile = new RenameFile(this, dropboxAPI, folderPath, fileSelected, newName);
        renameFile.execute();
    }

    private void changeLayout() {
        if (gridView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a file to Upload"), 0);
    }

    private void showFolders() {
        listFolder.clear();
        listFolder.add("/"); // root

        ListFolder listOfFolder = new ListFolder(dropboxAPI, PATH, listFolder);
        try {
            // get list of parent folder
            listOfFolder.execute().get();
            // get list of subfolder
            listOfFolder = new ListFolder(dropboxAPI, folderPath, listFolder);
            listOfFolder.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        folders = listFolder.toArray(new String[listFolder.size()]);
    }

    private void logOut() {
        // Remove credentials from the session
        dropboxAPI.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        backToLogin();
    }

    private void backToLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    /*============= Handle Uri to get file path to upload (from StackOverFlow) ====================*/

    public static String getPath(Context context, Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

     /*========================== Methods from DropBox API SDK ====================================*/

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Entry item = adapter.getItem(position);
            if (item.isDir) {
                Intent intent = new Intent(getApplicationContext(), Folder.class);
                intent.putExtra(Intent.EXTRA_TEXT, folderName + "/" + item.fileName());
                startActivity(intent);
            }
        }
    }
}
