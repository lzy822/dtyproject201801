package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Activity_FileManage extends AppCompatActivity {
    private static final String TAG = "Activity_FileManage";
    private List<FileManage> fileManages = new ArrayList<>();
    private FileManageAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    String type = "";
    //声明Toolbar
    Toolbar toolbar;

    FileManage fileManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__file_manage);
        toolbar = (Toolbar) findViewById(R.id.toolbar_filemanage);
        toolbar.setTitle(R.string.FileManage);
        toolbar.inflateMenu(R.menu.filemanagemenu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.back_filemanage:
                        fileManages.clear();
                        fileManage = fileManage.SelectLast();
                        String RootPath = fileManage.getRootPath();
                        String[] strings = fileManage.getFileSubset(FileManage.BUBBLESORT);
                        for (int i = 0; i < strings.length; i++){
                            if (!strings[i].contains(".dt")) fileManages.add(new FileManage(RootPath + "/" + strings[i], RootPath + "/", ".dt"));
                            else {
                                fileManages.add(new FileManage(RootPath + "/" + strings[i], RootPath + "/", 1));
                            }
                        }
                        refreshRecycler();
                }
                return true;
            }
        });
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        String filepath = "";
        if (type.equals(".dt")) {
            SharedPreferences prf1 = getSharedPreferences("filepath", MODE_PRIVATE);
            filepath = prf1.getString("mapath", "");
            Log.w(TAG, "onCreate: " + filepath);
        }else if (type.equals(".zip")){
            SharedPreferences prf1 = getSharedPreferences("filepath", MODE_PRIVATE);
            filepath = prf1.getString("inputpath", "");
            Log.w(TAG, "onCreate: " + filepath);
        }
        if (filepath.isEmpty()) {
            fileManage = new FileManage(type);
            String[] strings = fileManage.getFileSubset(FileManage.BUBBLESORT);
            for (int i = 0; i < strings.length; i++) {
                if (!strings[i].contains(type))
                    fileManages.add(new FileManage(Environment.getExternalStorageDirectory().toString() + "/" + strings[i], Environment.getExternalStorageDirectory().toString() + "/", type));
                else {
                    fileManages.add(new FileManage(Environment.getExternalStorageDirectory().toString() + "/" + strings[i], Environment.getExternalStorageDirectory().toString() + "/", 1));
                }
            }
        } else {
            File file = new File(filepath);
            if (file.exists() & file.isDirectory()) {
                fileManage = new FileManage(filepath, filepath.substring(0, filepath.lastIndexOf("/") + 1), type);
                String[] strings = fileManage.getFileSubset(FileManage.BUBBLESORT);
                for (int i = 0; i < strings.length; i++) {
                    if (!strings[i].contains(type))
                        fileManages.add(new FileManage(filepath + "/" + strings[i], filepath + "/", type));
                    else {
                        fileManages.add(new FileManage(filepath + "/" + strings[i], filepath + "/", 1));
                    }
                }
            } else {
                fileManage = new FileManage(type);
                String[] strings = fileManage.getFileSubset(FileManage.BUBBLESORT);
                for (int i = 0; i < strings.length; i++) {
                    if (!strings[i].contains(type))
                        fileManages.add(new FileManage(Environment.getExternalStorageDirectory().toString() + "/" + strings[i], Environment.getExternalStorageDirectory().toString() + "/", type));
                    else {
                        fileManages.add(new FileManage(Environment.getExternalStorageDirectory().toString() + "/" + strings[i], Environment.getExternalStorageDirectory().toString() + "/", 1));
                    }
                }
            }
        }
        refreshRecycler();
    }

    //重新刷新Recycler
    public void refreshRecycler(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_filemanage);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FileManageAdapter(fileManages);
        adapter.setOnItemClickListener(new FileManageAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String RootPath, int position) {
                if (!RootPath.contains(type)) {
                    fileManages.clear();
                    fileManage = new FileManage(RootPath, RootPath.substring(0, RootPath.lastIndexOf("/")), type);
                    String[] strings = fileManage.getFileSubset(FileManage.BUBBLESORT);
                    for (int i = 0; i < strings.length; i++) {
                        if (!strings[i].contains(type))
                            fileManages.add(new FileManage(RootPath + "/" + strings[i], RootPath + "/", type));
                        else {
                            fileManages.add(new FileManage(RootPath + "/" + strings[i], RootPath + "/", 1));
                        }
                    }
                    refreshRecycler();
                }else {
                    Intent i = new Intent();
                    i.putExtra("filePath", RootPath);
                    setResult(RESULT_OK, i);
                    Activity_FileManage.this.finish();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }
}
