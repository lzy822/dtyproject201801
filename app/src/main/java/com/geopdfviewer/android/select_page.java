package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
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
import android.widget.Button;
import android.widget.ImageView;
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
import com.shockwave.pdfium.PdfiumCore;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class select_page extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = "select_page";
    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;
    public static final int SAMPLE_TYPE = 1;
    public static final int URI_TYPE = 2;
    private final String DEF_DIR =  Environment.getExternalStorageDirectory().toString() + "/tencent/TIMfile_recv/";

    public static final String SAMPLE_FILE = "pdf/cangyuan.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String LOC_DELETE_ITEM = "map_num";
    private Map_test[] map_tests = new Map_test[15];
    private List<Map_test> map_testList = new ArrayList<>();
    private Map_testAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
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


    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(DEF_DIR), "application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
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



    public void initMapNext(int num, String name, String WKT, String uri, String GPTS, String BBox, String imguri) {
        Map_test mapTest = new Map_test(num, name, WKT, uri, GPTS, BBox, imguri);
        map_tests[num_pdf - 1] = mapTest;
        map_testList.clear();
        for (int i = 0; i < num_pdf; i++) {
            map_testList.add(map_tests[i]);
        }
    }



    public void initMap() {
        map_testList.clear();
        for (int j = 1; j <= num_pdf; j++) {
            locError(Integer.toString(j));
            SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
            String str = "n_" + j + "_";
            int num = pref1.getInt(str + "num", 0);
            String name = pref1.getString(str + "name", "");
            String WKT = pref1.getString(str + "WKT", "");
            String uri = pref1.getString(str + "uri", "");
            String GPTS = pref1.getString(str + "GPTS", "");
            String BBox = pref1.getString(str + "BBox", "");
            String imguri = pref1.getString(str + "img_path", "");
            Map_test mapTest = new Map_test(num, name, WKT, uri, GPTS, BBox, imguri);
            map_tests[j - 1] = mapTest;
            map_testList.add(map_tests[j - 1]);
        }
        refreshRecycler();


    }

    public void refreshRecycler(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Map_testAdapter(map_testList);
        recyclerView.setAdapter(adapter);
    }

    public void saveGeoInfo(String name, String uri, String WKT, String BBox, String GPTS, String img_path){
        num_pdf ++;
        SharedPreferences.Editor editor = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        editor.putInt("num", num_pdf);
        editor.apply();
        SharedPreferences.Editor editor1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        String str = "n_" + Integer.toString(num_pdf) + "_";
        editor1.putInt(str + "num", num_pdf);
        editor1.putString(str + "name", name);
        editor1.putString(str + "uri", uri);
        editor1.putString(str + "WKT", WKT);
        editor1.putString(str + "BBox", BBox);
        editor1.putString(str + "GPTS", GPTS);
        editor1.putString(str + "img_path", img_path);
        editor1.apply();
        initMapNext(num_pdf, name, WKT, uri, GPTS, BBox, img_path);
    }


    public String createThumbnails(String fileName, String filePath, int Type){

        String outPath = Environment.getExternalStorageDirectory() + "/PdfReader/" + fileName + ".jpg";
        PdfiumCore pdfiumCore = new PdfiumCore(this);
        int pageNum = 0;
        File m_pdf_file;
        OutputStream outputStream1 = null;
        InputStream ip = null;

        //File m_pdf_file = new File("/storage/emulated/0/tencent/TIMfile_recv/sample_big.pdf");
        //Log.w(TAG, Boolean.toString(m_pdf_file.canRead()) );
        try {
            if(Type == SAMPLE_TYPE) {
                ip = getAssets().open(SAMPLE_FILE);
                outputStream1 = new FileOutputStream(m_pdf_file = new File(Environment.getExternalStorageDirectory() + "/PdfReader/" + fileName + ".pdf"));
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = ip.read(bytes)) != -1) {
                    outputStream1.write(bytes, 0, read);
                }
                ip.close();
                outputStream1.close();
            }else {
                m_pdf_file = new File(filePath);
            }
            //PdfDocument pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(getAssets().open(SAMPLE_FILE), ParcelFileDescriptor.MODE_READ_WRITE));
            PdfDocument pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(m_pdf_file, ParcelFileDescriptor.MODE_READ_WRITE));
            Log.w(TAG, Integer.toString(pdfiumCore.getPageCount(pdf)));

            pdfiumCore.openPage(pdf, pageNum);
            int width = pdfiumCore.getPageWidth(pdf, pageNum);
            int height = pdfiumCore.getPageHeight(pdf, pageNum);
            Bitmap bitmap = Bitmap.createBitmap(120, 180, Bitmap.Config.RGB_565);
            pdfiumCore.renderPageBitmap(pdf, bitmap, pageNum, 0, 0, 120, 180);
            pdfiumCore.closeDocument(pdf);
            File file1 = new File(Environment.getExternalStorageDirectory() + "/PdfReader");
            if(!file1.exists() && !file1.isDirectory()){
                file1.mkdirs();
            }

            File of = new File(Environment.getExternalStorageDirectory() + "/PdfReader", fileName + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(of);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();

            /*ImageView imageView = (ImageView) findViewById(R.id.img_show);
            imageView.setVisibility(View.VISIBLE);
            Log.e(TAG, outPath );
            imageView.setImageURI(Uri.parse(outPath));*/
            //imageView.setImageBitmap(bitmap);



        }
        catch (IOException e) {
            Log.w(TAG, e.getMessage() );
            Toast.makeText(this, "无法获取示例文件!", Toast.LENGTH_LONG).show();
        }
        Log.e(TAG, outPath );
        return outPath;
    }


    public void Btn_clearData(){

        /*SharedPreferences.Editor pref = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        pref.clear().commit();
        SharedPreferences.Editor pref1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        pref1.clear().commit();
        Toast.makeText(this, "清除操作完成", Toast.LENGTH_LONG).show();
        initPage();
        refreshRecycler();*/
        Intent intent = getIntent();
        int loc_delete = intent.getIntExtra(LOC_DELETE_ITEM, 0);
        locError(Integer.toString(loc_delete));
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

    public String findNameFromUri(Uri uri){
        int num;
        num = appearNumber(uri.toString(), "/");
        String str = uri.toString();
        //Log.w(TAG, uri.toString() );
        //Log.w(TAG, Integer.toString(num) );
        for (int i = 1; i <= num; i++){
            str = str.substring(str.indexOf("/") + 1);

        }
        str = str.substring(0, str.length() - 4);
        //locError(str);
        return str;
        //locError(str);

    }

    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            uri = data.getData();
            //locError(data.getData().getHost());
            /*locError(uri.toString());
            String m_filePath = uri.toString().substring("content://com.android.fileexplorer.myprovider/".length());
            m_filePath = "content://com.geopdfviewer.android.provider/" + m_filePath;
            getGeoInfo(getRealPath(uri.toString()), URI_TYPE, m_filePath, findNameFromUri(uri));*/
            getGeoInfo(getRealPath(uri.toString()), URI_TYPE, uri.toString(), findNameFromUri(uri));
            refreshRecycler();
        }
    }

    //获取File可使用路径
    public String getRealPath(String filePath) {
        String str = "content://com.android.fileexplorer.myprovider/external_files";
        String Dir = Environment.getExternalStorageDirectory().toString();
        filePath = Dir + filePath.substring(str.length());
        return filePath;
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
        setContentView(R.layout.activity_select_test_page);
        adapter = new Map_testAdapter(map_testList);
        adapter.setOnItemLongClickListener(new Map_testAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, int position) {
            }
        });

        //Clear按钮事件编辑
        Button btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_clearData();
            }
        });
        //第一个子floating按钮事件编辑
        com.getbase.floatingactionbutton.FloatingActionButton bt1 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab01);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(select_page.this, MainInterface.class);
                //intent.putExtra("data_uri", "asset");
                intent.putExtra("type", "asset");
                startActivity(intent);*/
                getGeoInfo("", SAMPLE_TYPE, "", findNamefromSample(SAMPLE_FILE));

            }
        });
        //第二个子floating按钮事件编辑
        com.getbase.floatingactionbutton.FloatingActionButton bt2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab02);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickFile();
            }
        });
        //初始化界面一
        initPage();
    }

    public void initPage(){
        //获取卡片数目
        setNum_pdf();
        if (num_pdf == 0) {
            initDemo();
        }
        Log.w(TAG, Integer.toString(num_pdf) );
        //初始化
        initMap();
    }

    public void initDemo(){
        //save1("demo");
        saveGeoInfo("demo", "", "", "", "", "");
        //initMap2("occupation");
        Toast.makeText(this, "第一次进入", Toast.LENGTH_LONG).show();
    }



    public String findNamefromSample(String str){
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    public void locError(){
        Log.w(TAG, "可以成功运行到这里" );
    }

    public void locError(String str){
        Log.e(TAG, str );
    }

    public void getGeoInfo(String filePath, int Type, String uri, String name) {
        locError(name);
        String m_name = name;
        String m_uri = uri;
        String bmPath = "";



        //locError();
        File file = new File(filePath);
        InputStream in = null;
        String m_WKT = "";
        Boolean isESRI = false;
        int num_line = 0;
        try {
            if (Type == SAMPLE_TYPE){
                in = getAssets().open(SAMPLE_FILE);
                bmPath = createThumbnails(name, filePath, SAMPLE_TYPE);
            }
            else {
                in = new FileInputStream(file);
                bmPath = createThumbnails(name, filePath, URI_TYPE);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //StringBuffer sb = new StringBuffer("");
            String line;
            String m_BBox = "", m_GPTS = "";
            //locError();
            while((line = bufferedReader.readLine()) != null) {
                //sb.append(line + "/n");
                if(line.contains("PROJCS")) {

                    m_WKT = line.substring(line.indexOf("PROJCS["), line.indexOf(")>>"));

                }
                if (line.contains("ESRI") || line.contains("esri") || line.contains("arcgis") || line.contains("ARCGIS") || line.contains("Adobe"))
                {
                    isESRI = true;
                }
                if (line.contains("/BBox")){
                    //Log.w(TAG, "the line loc = " + Integer.toString(num_line) );
                    m_BBox = line.substring(line.indexOf("BBox") + 5);
                    m_BBox = m_BBox.substring(0, m_BBox.indexOf("]"));
                    m_BBox = m_BBox.trim();
                }
                if (line.contains("GPTS")){
                    m_GPTS = line.substring(line.indexOf("GPTS") + 5);
                    m_GPTS = m_GPTS.substring(0, m_GPTS.indexOf("]"));
                    m_GPTS = m_GPTS.trim();
                }
                num_line += 1;
            }

            Log.w(TAG, "GPTS:" + m_GPTS );
            Log.w(TAG, "BBox:" + m_BBox );
            if (isESRI == true) {
                m_WKT = "ESRI::" + m_WKT;
                //save(content);
            } else {
                //save(content);
            }
            Log.w(TAG, m_WKT );
            if (Type == SAMPLE_TYPE) {
                //setTitle(findTitle(pdfFileName));
            } else {
                //getFileName(Uri.parse(filePath));
            }
            //locError();

            Toast.makeText(this, "获取完毕!", Toast.LENGTH_LONG).show();
            in.close();
            saveGeoInfo(m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath);
        } catch (IOException e) {
            Toast.makeText(this, "地理信息获取失败, 请联系程序员", Toast.LENGTH_LONG).show();
        }

    }


}
