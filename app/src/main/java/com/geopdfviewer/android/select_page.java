package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.support.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;

import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

public class select_page extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {
    public static final String FRUIT_NAME = "fruit_name";
    public static final String FRUIT_IMAGE_ID = "fruit_image_id";
    private static final String TAG = "select_page";
    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample1.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private Map_test[] map_tests = new Map_test[10];
    private List<Map_test> map_testList = new ArrayList<>();
    private Map_testAdapter adapter;
    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }
    @ViewById
    PDFView pdfView;

    //记录总的pdf条目数
    int num_pdf = 0;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.options, menu);
        return true;

    }
    //@OptionsItem(R.id.pickFile)
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("application/pdf");

        //intent.setDataAndType(Uri.parse("content://com.android.fileexplorer.myprovider/external_files/tencent/"), "application/pdf");

        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }


    public void sendUri(Uri uri) {
    Log.w(TAG, uri.toString() );
    Intent intent = new Intent(select_page.this, MainInterface.class);
    intent.putExtra("type", "uri");
    intent.putExtra("data_uri", uri.toString());
    startActivity(intent);
    }


    public void initMap(String name) {
        Map_test mapTest = new Map_test(name);

        Log.w(TAG, Integer.toString(num_pdf) );
        Log.w(TAG, Integer.toString(map_tests.length) );
        //Log.w(TAG, map_tests[0].getName() );
        map_tests[num_pdf - 1] = mapTest;
        Log.w(TAG, Integer.toString(map_tests.length) );
        map_testList.clear();
        for (int i = 0; i < num_pdf; i++) {
            map_testList.add(map_tests[i]);
        }
    }
    public void initMap2(String name) {
        Map_test mapTest = new Map_test(name);

        Log.w(TAG, Integer.toString(num_pdf) );
        Log.w(TAG, Integer.toString(map_tests.length) );
        //Log.w(TAG, map_tests[0].getName() );
        map_tests[num_pdf - 1] = mapTest;
        Log.w(TAG, Integer.toString(map_tests.length) );
        map_testList.clear();
        for (int i = 0; i < num_pdf; i++) {
            map_testList.add(map_tests[i]);
        }
    }
    public void save(String name, String uri, String WKT){
        num_pdf ++;
        SharedPreferences.Editor editor = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        editor.putInt("num", num_pdf);
        editor.apply();
        SharedPreferences.Editor editor1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        String str = "n_" + Integer.toString(num_pdf) + "_";
        editor.putString(str + "name", name);
        editor.putString(str + "uri", uri);
        editor.putString(str + "WKT", WKT);
        editor.apply();
    }
    public void save1(String name){
        num_pdf ++;
        SharedPreferences.Editor editor = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        Toast.makeText(this, Integer.toString(num_pdf), Toast.LENGTH_LONG).show();
        editor.putInt("num", num_pdf);
        editor.apply();
        SharedPreferences.Editor editor1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        String str = "n_" + Integer.toString(num_pdf) + "_";
        editor1.putString(str + "name", name);
        editor1.apply();
    }
    public void save2(String name, Uri uri){
        num_pdf ++;
        SharedPreferences.Editor editor = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        Toast.makeText(this, Integer.toString(num_pdf), Toast.LENGTH_LONG).show();
        editor.putInt("num", num_pdf);
        editor.apply();
        SharedPreferences.Editor editor1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        String str = "n_" + Integer.toString(num_pdf) + "_";
        editor1.putString(str + "name", name);
        editor1.putString(str + "uri", uri.toString());
        editor1.apply();
    }

    public void initMapI() {
        SharedPreferences pref = getSharedPreferences("data_num", MODE_PRIVATE);
        num_pdf = pref.getInt("num", 0);
        Toast.makeText(this, Integer.toString(num_pdf), Toast.LENGTH_LONG).show();
        for (int j = 1; j <= num_pdf; j++) {
            SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
            String str = "n_" + j + "_";
            String name = pref1.getString(str + "name", "");
            Map_test mapTest = new Map_test(name);
            map_tests[j - 1] = mapTest;
            map_testList.add(map_tests[j - 1]);
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Map_testAdapter(map_testList);
        recyclerView.setAdapter(adapter);


    }
    public void setNum_pdf(){
        SharedPreferences pref = getSharedPreferences("data_num", MODE_PRIVATE);
        num_pdf = pref.getInt("num", 0);
        Log.w(TAG, Integer.toString(num_pdf) );

    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            uri = data.getData();
            //sendUri(uri);
            String name = getFileName(uri);
            save2(name, uri);
            initMap(name);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            GridLayoutManager layoutManager = new GridLayoutManager(this,2);
            recyclerView.setLayoutManager(layoutManager);
            Log.e(TAG, Integer.toString(map_testList.size()));

            adapter = new Map_testAdapter(map_testList);
            recyclerView.setAdapter(adapter);
            setTitle(Integer.toString(num_pdf));
            //Log.w(TAG, uri.toString() );
            Intent intent = new Intent(select_page.this, MainInterface.class);
            intent.putExtra("type", "uri");
            intent.putExtra("data_uri", uri.toString());
            startActivity(intent);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_page);
        com.getbase.floatingactionbutton.FloatingActionButton bt1 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab01);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(select_page.this, MainInterface.class);
                //intent.putExtra("data_uri", "asset");
                intent.putExtra("type", "asset");
                startActivity(intent);
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton bt2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab02);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickFile();
            }
        });
        setNum_pdf();

        if (num_pdf == 0) {
            save1("occupation");
            initMap2("occupation");
            Toast.makeText(this, "第一次进入", Toast.LENGTH_LONG).show();
        }

        initMapI();
    }

}
