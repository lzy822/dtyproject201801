package com.geopdfviewer.android;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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



import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    //记录当前坐标信息
    double m_lat, m_long;
    String m_latlong_description;

    Location location;

    private LocationManager locationManager;

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }

    //记录总的pdf条目数
    int num_pdf = 0;

    private int selectedNum = 0;

    Uri uri;

    //记录当前pdf地图中心的经纬度值
    float m_center_x, m_center_y;

    //记录是否长按
    private int isLongClick = 1;

    //声明Toolbar
    Toolbar toolbar;

    com.getbase.floatingactionbutton.FloatingActionButton bt2;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        switch (isLongClick){
            case 1:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.delete).setVisible(false);
                menu.findItem(R.id.mmcancel).setVisible(false);
                menu.findItem(R.id.showinfo).setVisible(false);
                break;
            case 0:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.delete).setVisible(true);
                menu.findItem(R.id.mmcancel).setVisible(true);
                menu.findItem(R.id.showinfo).setVisible(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.options, menu);
        getMenuInflater().inflate(R.menu.deletetoolbar, menu);
        return true;
    }

    private void resetView(){
        setTitle("地图列表");
        selectedNum = 0;
        isLongClick = 1;
        invalidateOptionsMenu();
        bt2.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            case  R.id.delete:
                if (selectedNum != 0){
                    deleteData(selectedNum);
                    resetView();
                }else Toast.makeText(this, "请长按某个子项后, 再选择菜单栏操作", Toast.LENGTH_LONG).show();
                break;
            case  R.id.mmcancel:
                if (selectedNum != 0){
                    //isLongClick = 1;
                    refreshRecycler();
                    resetView();
                }else Toast.makeText(this, "请长按某个子项后, 再选择菜单栏操作", Toast.LENGTH_LONG).show();
                break;
            case  R.id.showinfo:
                if (selectedNum != 0){
                    showInfo(selectedNum);
                    resetView();
                }else Toast.makeText(this, "请长按某个子项后, 再选择菜单栏操作", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    private void showInfo(int num){
        refreshRecycler();
        Intent intent = new Intent(select_page.this, info_page.class);
        intent.putExtra("extra_data", getWKT(num));
        startActivity(intent);
    }

    private String getWKT(int num) {
        SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
        String str = "n_" + num + "_";
        return pref1.getString(str + "WKT", "");
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

        //launchPicker();
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

    private void requestAuthority(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(select_page.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(select_page.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(select_page.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(select_page.this, permissions, 118);
        }else {
            getLocation();
            initPage();
        }
    }

    public void initMapNext(int num, String name, String WKT, String uri, String GPTS, String BBox, String imguri, String MediaBox, String CropBox, String ic, String center_latlong) {
        Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong);
        map_tests[num_pdf - 1] = mapTest;
        map_testList.clear();
        for (int i = 0; i < num_pdf; i++) {
            map_testList.add(map_tests[i]);
        }
    }

    public void initMap() {
        map_testList.clear();
        for (int j = 1; j <= num_pdf; j++) {
            //locError(Integer.toString(j));
            SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
            String str = "n_" + j + "_";
            int num = pref1.getInt(str + "num", 0);
            String name = pref1.getString(str + "name", "");
            String WKT = pref1.getString(str + "WKT", "");
            String uri = pref1.getString(str + "uri", "");
            String GPTS = pref1.getString(str + "GPTS", "");
            String BBox = pref1.getString(str + "BBox", "");
            String imguri = pref1.getString(str + "img_path", "");
            String MediaBox = pref1.getString(str + "MediaBox", "");
            String CropBox = pref1.getString(str + "CropBox", "");
            String center_latlong = pref1.getString(str + "center_latlong", "");
            String ic = pref1.getString(str + "ic", "");
            Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong);
            map_tests[j - 1] = mapTest;
            map_testList.add(map_tests[j - 1]);
        }
        refreshRecycler();


    }

    //记录上一个所按的按钮
    private int selectedpos;
    //重新刷新Recycler
    public void refreshRecycler(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Map_testAdapter(map_testList);
        adapter.setOnItemLongClickListener(new Map_testAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, int map_num, int position) {
                setTitle("正在进行长按操作");
                bt2.setVisibility(View.INVISIBLE);
                locError("map_num: " + Integer.toString(map_num) + "\n" + "position: " + Integer.toString(position));
                selectedNum = map_num;
                if (isLongClick != 0){
                    isLongClick = 0;
                    selectedpos = position;
                }else {
                    adapter.notifyItemChanged(selectedpos);
                    selectedpos = position;
                }


                invalidateOptionsMenu();
                //deleteData(position);
            }
        });
        //adapter.getItemSelected();
        recyclerView.setAdapter(adapter);
    }

    //获取当前坐标位置
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(this, "请打开网络或GPS定位功能!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        try {

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null){
                Log.d(TAG, "onCreate.location = null");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Log.d(TAG, "onCreate.location = " + location);
            updateView(location);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
        }catch (SecurityException  e){
            e.printStackTrace();
        }
    }

    //坐标监听器
    protected final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "Location changed to: " + getLocationInfo(location));
            updateView(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged() called with " + "provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled() called with " + "provider = [" + provider + "]");
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                Log.d(TAG, "onProviderDisabled.location = " + location);
                updateView(location);
            }catch (SecurityException e){

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled() called with " + "provider = [" + provider + "]");
        }
    };

    //更新坐标信息
    private void updateView(Location location) {
        Geocoder gc = new Geocoder(this);
        List<Address> addresses = null;
        String msg = "";
        Log.d(TAG, "updateView.location = " + location);
        if (location != null) {
            try {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                m_latlong_description = addresses.get(0).getAddressLine(0);
                Toast.makeText(this, "你当前在: " + m_latlong_description, Toast.LENGTH_LONG).show();
                Log.d(TAG, "updateView.addresses = " + addresses);
                if (addresses.size() > 0) {
                    msg += addresses.get(0).getAdminArea().substring(0,2);
                    msg += " " + addresses.get(0).getLocality().substring(0,2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            m_lat = location.getLatitude();
            m_long = location.getLongitude();
            SharedPreferences.Editor editor = getSharedPreferences("latlong", MODE_PRIVATE).edit();
            editor.clear().commit();
            editor.putString("mlatlong", Double.toString(m_lat) + "," + Double.toString(m_long));
            editor.apply();
            //setHereLocation();
            //locError(Double.toString(m_lat) + "&&" + Double.toString(m_long) + "Come here");

        } else {

        }
    }

    public void deleteData(int selectedNum){
        map_testList.clear();
        boolean deleted = false;
        for (int j = 1; j <= num_pdf; j++) {
            if (j != selectedNum){
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
            String MediaBox = pref1.getString(str + "MediaBox", "");
            String CropBox = pref1.getString(str + "CropBox", "");
            String ic = pref1.getString(str + "ic", "");
            String center_latlong = pref1.getString(str + "center_latlong", "");
            if (!deleted){
                Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong);
                map_tests[j - 1] = mapTest;
                map_testList.add(map_tests[j - 1]);
            }else {
                Map_test mapTest = new Map_test(name, num - 1, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong);
                map_tests[j - 2] = mapTest;
                map_testList.add(map_tests[j - 2]);
            }
            }else {
                SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
                String str = "n_" + j + "_";
                String imguri = pref1.getString(str + "img_path", "");
                String the_ic = pref1.getString(str + "ic", "");
                deleteMDatabase(the_ic);
                deletemFile(imguri);
                deleted = true;
            }
        }
        num_pdf = num_pdf - 1;
        SharedPreferences.Editor editor = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        editor.putInt("num", num_pdf);
        editor.apply();
        SharedPreferences.Editor editor1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor1.clear().commit();
        for (int j = 1; j <= num_pdf; j++) {
            String str = "n_" + Integer.toString(j) + "_";
            editor1.putInt(str + "num", j);
            editor1.putString(str + "name", map_tests[j - 1].getM_name());
            editor1.putString(str + "uri", map_tests[j - 1].getM_uri());
            editor1.putString(str + "WKT", map_tests[j - 1].getM_WKT());
            editor1.putString(str + "BBox", map_tests[j - 1].getM_BBox());
            editor1.putString(str + "MediaBox", map_tests[j - 1].getM_MediaBox());
            editor1.putString(str + "CropBox", map_tests[j - 1].getM_CropBox());
            editor1.putString(str + "GPTS", map_tests[j - 1].getM_GPTS());
            editor1.putString(str + "img_path", map_tests[j - 1].getM_imguri());
            editor1.putString(str + "ic", map_tests[j - 1].getM_ic());
            editor1.putString(str + "center_latlong", map_tests[j - 1].getM_center_latlong());
            editor1.apply();
        }
        initMap();


    }

    public void deleteMDatabase(String m_ic){
        List<POI> pois = DataSupport.where("ic = ?", m_ic).find(POI.class);
        for ( POI poi : pois){
            String poic = poi.getPOIC();
            DataSupport.deleteAll(MTAPE.class, "POIC = ?", poic);
            DataSupport.deleteAll(MPHOTO.class, "POIC = ?", poic);
        }
        DataSupport.deleteAll(POI.class, "ic = ?", m_ic);
        DataSupport.deleteAll(Trail.class, "ic = ?", m_ic);
    }

    public void deletemFile(String filePath){
        File file = new File(filePath);
        if (file.exists() && file.isFile()){
            if (file.delete()){
                //Toast.makeText(this, "删除文件成功", Toast.LENGTH_SHORT).show();
            }else Toast.makeText(this, "删除文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveGeoInfo(String name, String uri, String WKT, String BBox, String GPTS, String img_path, String MediaBox, String CropBox, String ic){
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
        editor1.putString(str + "MediaBox", MediaBox);
        editor1.putString(str + "CropBox", CropBox);
        editor1.putString(str + "GPTS", GPTS);
        editor1.putString(str + "img_path", img_path);
        editor1.putString(str + "ic", ic);
        String center_latlong = Double.toString(m_center_x) + "," + Double.toString(m_center_y);
        locError(center_latlong);
        editor1.putString(str + "center_latlong", center_latlong);
        editor1.apply();
        initMapNext(num_pdf, name, WKT, uri, GPTS, BBox, img_path, MediaBox, CropBox, ic, center_latlong);
    }

    public String createThumbnails(String fileName, String filePath, int Type){
        File file = new File(Environment.getExternalStorageDirectory() + "/PdfReader");
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        fileName = fileName + Long.toString(System.currentTimeMillis());
        String outPath = Environment.getExternalStorageDirectory() + "/PdfReader/" + fileName + ".jpg";
        PdfiumCore pdfiumCore = new PdfiumCore(this);
        int pageNum = 0;
        File m_pdf_file;
        OutputStream outputStream1 = null;
        InputStream ip = null;
        try {
            m_pdf_file = new File(filePath);
            PdfDocument pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(m_pdf_file, ParcelFileDescriptor.MODE_READ_WRITE));
            Log.w(TAG, Integer.toString(pdfiumCore.getPageCount(pdf)));
            pdfiumCore.openPage(pdf, pageNum);
            Bitmap bitmap = Bitmap.createBitmap(120, 180, Bitmap.Config.RGB_565);
            pdfiumCore.renderPageBitmap(pdf, bitmap, pageNum, 0, 0, 120, 180);
            pdfiumCore.closeDocument(pdf);
            File of = new File(Environment.getExternalStorageDirectory() + "/PdfReader", fileName + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(of);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
            outputStream.flush();
            outputStream.close();
            if (Type == SAMPLE_TYPE){
                deletemFile(Environment.getExternalStorageDirectory() + "/PdfReader/" + fileName + ".pdf");
            }
        }
        catch (IOException e) {
            Log.w(TAG, e.getMessage() );
            Toast.makeText(this, "无法获取示例文件!", Toast.LENGTH_LONG).show();
        }
        return outPath;
    }

    public void Btn_clearData(){
        SharedPreferences.Editor pref = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        pref.clear().commit();
        SharedPreferences.Editor pref1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        pref1.clear().commit();
        Toast.makeText(this, "清除操作完成", Toast.LENGTH_LONG).show();
        initPage();
        refreshRecycler();
        /*Intent intent = getIntent();
        int loc_delete = intent.getIntExtra(LOC_DELETE_ITEM, 0);
        locError(Integer.toString(loc_delete));*/
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
        String str = "";
        num = appearNumber(uri.toString(), "/");
        try {
            String configPath = uri.toString();
            configPath = URLDecoder.decode(configPath, "utf-8");
            locError(configPath);
            str = configPath;
            for (int i = 1; i <= num; i++){
                str = str.substring(str.indexOf("/") + 1);
            }
            str = str.substring(0, str.length() - 4);

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return str;

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
            try {
                String configPath = uri.toString();
                configPath = URLDecoder.decode(configPath, "utf-8");
                locError(configPath);
                getGeoInfo(getRealPath(configPath), URI_TYPE, configPath, findNameFromUri(uri));
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

            //locError(data.getData().getHost());
            /*locError(uri.toString());
            String m_filePath = uri.toString().substring("content://com.android.fileexplorer.myprovider/".length());
            m_filePath = "content://com.geopdfviewer.android.provider/" + m_filePath;
            getGeoInfo(getRealPath(uri.toString()), URI_TYPE, m_filePath, findNameFromUri(uri));*/
            //getGeoInfo(getRealPath(uri.toString()), URI_TYPE, uri.toString(), findNameFromUri(uri));
            locError(getRealPath(uri.toString()));
            locError(uri.toString());
            locError(findNameFromUri(uri));
            LitePal.getDatabase();
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
        /*if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //launchPicker();
            }else {
                Toast.makeText(this, "必须通过所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                finish();
            }
        }*/
        switch (requestCode) {
            /*case 66:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须通过所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }

                }
                break;*/
            case 118:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须通过所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }else {
                            getLocation();
                            initPage();
                        }
                    }

                }
                break;
            default:
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_test_page);

        //pickFile();
        setTitle("地图列表");
        //申请动态权限
        requestAuthority();

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 66);
        }*/
        //getLocation();
        /*Uri uri = Uri.parse("/storage/emulated/0/MIUI/sound_recorder/3月20日 上午10点36分.mp3");
        MediaPlayer mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();*/
        //locError();
        //Clear按钮事件编辑
        /*Button btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Btn_clearData();
            }
        });*/
        //子floating按钮事件编辑
        bt2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab02);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPicker();
            }
        });
        //初始化界面一
        //initPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (num_pdf != 0){
        isLongClick = 1;
        selectedNum = 0;
        refreshRecycler();
        }
    }

    public void initPage(){
        //获取卡片数目
        setNum_pdf();
        if (num_pdf == 0) {
            initDemo();
        }
        //Log.w(TAG, Integer.toString(num_pdf) );
        //初始化
        initMap();
    }

    public void initDemo(){
        getGeoInfo("", SAMPLE_TYPE, "", findNamefromSample(SAMPLE_FILE));
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

    private boolean isDrift(String LPTS){
        locError("Drift : " + LPTS);
        boolean isDrift = false;
        String[] LPTSStrings = LPTS.split(" ");
        //locError(Integer.toString(LPTSStrings.length));
        for (int i = 0; i < LPTSStrings.length; i++){
            //locError(LPTSStrings[i]);
            if (Float.valueOf(LPTSStrings[i]) != 0 && Float.valueOf(LPTSStrings[i]) != 1){
                isDrift = true;
                break;
            }
        }
        return isDrift;
    }

    private String rubberCoordination(String MediaBox, String BBox, String GPTS){
        locError("GPTS: " + GPTS);
        locError(MediaBox + "&" + BBox + "&" + GPTS);
        String[] MediaBoxString = MediaBox.split(" ");
        String[] BBoxString = BBox.split(" ");
        String[] GPTSString = GPTS.split(" ");
        //将String 数组转换为 Float 数组
        float[] MediaBoxs = new float[MediaBoxString.length];
        float[] BBoxs = new float[BBoxString.length];
        float[] GPTSs = new float[GPTSString.length];
        for (int i = 0; i < MediaBoxString.length; i++) {
            MediaBoxs[i] = Float.valueOf(MediaBoxString[i]);
            //locError("MediaBoxs : " + MediaBoxs[i]);
        }
        for (int i = 0; i < BBoxString.length; i++) {
            BBoxs[i] = Float.valueOf(BBoxString[i]);
            //locError("BBoxs : " + Float.toString(BBoxs[i]));
        }
        //优化BBOX拉伸算法
        float del;
        /*if (BBoxs[0] > BBoxs[2]){
            del = BBoxs[0];
            BBoxs[0] = BBoxs[2];
            BBoxs[2] = del;
        }*/
        if (BBoxs[1] < BBoxs[3]){
            del = BBoxs[1];
            BBoxs[1] = BBoxs[3];
            BBoxs[3] = del;
        }
        //
        for (int i = 0; i < GPTSString.length; i++) {
            GPTSs[i] = Float.valueOf(GPTSString[i]);
        }

        if (Math.floor(MediaBoxs[2]) != Math.floor(BBoxs[2]) && Math.floor(MediaBoxs[3]) != Math.floor(BBoxs[3])) {
            PointF pt1_lb = new PointF(), pt1_lt = new PointF(), pt1_rt = new PointF(), pt1_rb = new PointF();
            PointF pt_lb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF(), pt_rb = new PointF();
            pt1_lb.x = BBoxs[0] / MediaBoxs[2];
            pt1_lb.y = BBoxs[3] / MediaBoxs[3];
            pt1_lt.x = BBoxs[0] / MediaBoxs[2];
            pt1_lt.y = BBoxs[1] / MediaBoxs[3];
            pt1_rt.x = BBoxs[2] / MediaBoxs[2];
            pt1_rt.y = BBoxs[1] / MediaBoxs[3];
            pt1_rb.x = BBoxs[2] / MediaBoxs[2];
            pt1_rb.y = BBoxs[3] / MediaBoxs[3];
            float lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            float long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
            for (int i = 0; i < GPTSs.length; i = i + 2) {
                if (GPTSs[i] < lat_axis) {
                    if (GPTSs[i + 1] < long_axis) {
                        pt_lb.x = GPTSs[i];
                        pt_lb.y = GPTSs[i + 1];
                    } else {
                        pt_rb.x = GPTSs[i];
                        pt_rb.y = GPTSs[i + 1];
                    }
                } else {
                    if (GPTSs[i + 1] < long_axis) {
                        pt_lt.x = GPTSs[i];
                        pt_lt.y = GPTSs[i + 1];
                    } else {
                        pt_rt.x = GPTSs[i];
                        pt_rt.y = GPTSs[i + 1];
                    }
                }
            }
            GPTS = Float.toString(pt1_lb.x) + " " + Float.toString(pt1_lb.y) + " " + Float.toString(pt1_lt.x) + " " + Float.toString(pt1_lt.y) + " " + Float.toString(pt1_rt.x) + " " + Float.toString(pt1_rt.y) + " " + Float.toString(pt1_rb.x) + " " + Float.toString(pt1_rb.y);
            locError(GPTS);
            float delta_lat = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2, delta_long = ((pt_rb.y - pt_lb.y) + (pt_rt.y - pt_lt.y)) / 2;
            float delta_width = pt1_rb.x - pt1_lb.x, delta_height = pt1_lt.y - pt1_lb.y;
            pt_lb.x = pt_lb.x - (delta_lat / delta_height * (pt1_lb.y - 0));
            pt_lb.y = pt_lb.y - (delta_long / delta_width * (pt1_lb.x - 0));
            pt_lt.x = pt_lt.x - (delta_lat / delta_height * (pt1_lt.y - 1));
            pt_lt.y = pt_lt.y - (delta_long / delta_width * (pt1_lt.x - 0));
            pt_rb.x = pt_rb.x - (delta_lat / delta_height * (pt1_rb.y - 0));
            pt_rb.y = pt_rb.y - (delta_long / delta_width * (pt1_rb.x - 1));
            pt_rt.x = pt_rt.x - (delta_lat / delta_height * (pt1_rt.y - 1));
            pt_rt.y = pt_rt.y - (delta_long / delta_width * (pt1_rt.x - 1));
            m_center_x = ( pt_lb.x + pt_lt.x + pt_rb.x + pt_rt.x) / 4;
            m_center_y = ( pt_lb.y + pt_lt.y + pt_rb.y + pt_rt.y) / 4;
            //locError("GETGPTS: " + Double.toString(m_center_x));
            GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
        }else {
            m_center_x = ( GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            m_center_y = ( GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
            //locError("GETGPTS: " + Double.toString(m_center_x));
        }
        locError("GPTS : test" + GPTS);
            return GPTS;

    }

    private String getGPTS(String GPTS, String LPTS){
        //locError("看这里: " + " & LPTS " + LPTS);
        if (isDrift(LPTS) == true) {
            //locError("看这里: " + GPTS + " & LPTS " + LPTS);
            float lat_axis, long_axis;
            float lat_axis1, long_axis1;
            DecimalFormat df = new DecimalFormat("0.0");
            String[] GPTSStrings = GPTS.split(" ");
            String[] LPTSStrings = LPTS.split(" ");
            //将String 数组转换为 Float 数组
            float[] GPTSs = new float[GPTSStrings.length];
            float[] LPTSs = new float[LPTSStrings.length];
            for (int i = 0; i < LPTSStrings.length; i++) {
                LPTSs[i] = Float.valueOf(LPTSStrings[i]);
            }
            for (int i = 0; i < GPTSStrings.length; i++) {
                GPTSs[i] = Float.valueOf(GPTSStrings[i]);
            }
            //
            //构建两个矩形
            //构建经纬度矩形
            PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
            //PointF pt_lb1 = new PointF(), pt_rb1 = new PointF(), pt_lt1 = new PointF(), pt_rt1 = new PointF();
            lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
            for (int i = 0; i < GPTSs.length; i = i + 2){
                if (GPTSs[i] < lat_axis) {
                    if (GPTSs[i + 1] < long_axis){
                        pt_lb.x = GPTSs[i];
                        pt_lb.y = GPTSs[i + 1];
                    } else {
                        pt_rb.x = GPTSs[i];
                        pt_rb.y = GPTSs[i + 1];
                    }
                } else {
                    if (GPTSs[i + 1] < long_axis){
                        pt_lt.x = GPTSs[i];
                        pt_lt.y = GPTSs[i + 1];
                    } else {
                        pt_rt.x = GPTSs[i];
                        pt_rt.y = GPTSs[i + 1];
                    }
                }
            }
            //GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
            //locError(GPTS);
            //
            //构建LPTS 矩形
            //预处理LPTS
            for (int i = 0; i < LPTSs.length; i++){
                LPTSs[i] = Float.valueOf(df.format(LPTSs[i]));
            }
            //
            PointF pt_lb1 = new PointF(), pt_rb1 = new PointF(), pt_lt1 = new PointF(), pt_rt1 = new PointF();
            lat_axis1 = (LPTSs[0] + LPTSs[2] + LPTSs[4] + LPTSs[6]) / 4;
            long_axis1 = (LPTSs[1] + LPTSs[3] + LPTSs[5] + LPTSs[7]) / 4;
            for (int i = 0; i < LPTSs.length; i = i + 2){
                if (LPTSs[i] < lat_axis1) {
                    if (LPTSs[i + 1] < long_axis1){
                        pt_lb1.x = LPTSs[i];
                        pt_lb1.y = LPTSs[i + 1];
                    } else {
                        pt_rb1.x = LPTSs[i];
                        pt_rb1.y = LPTSs[i + 1];
                    }
                } else {
                    if (LPTSs[i + 1] < long_axis1){
                        pt_lt1.x = LPTSs[i];
                        pt_lt1.y = LPTSs[i + 1];
                    } else {
                        pt_rt1.x = LPTSs[i];
                        pt_rt1.y = LPTSs[i + 1];
                    }
                }
            }
            GPTS = Float.toString(pt_lb1.x) + " " + Float.toString(pt_lb1.y) + " " + Float.toString(pt_lt1.x) + " " + Float.toString(pt_lt1.y) + " " + Float.toString(pt_rt1.x) + " " + Float.toString(pt_rt1.y) + " " + Float.toString(pt_rb1.x) + " " + Float.toString(pt_rb1.y);
            locError("GETGPTS: " + GPTS);
            float delta_lat = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2, delta_long = ((pt_rb.y - pt_lb.y) + (pt_rt.y - pt_lt.y)) / 2;
            float delta_width = pt_rb1.y - pt_lb1.y, delta_height = pt_lt1.x - pt_lb1.x;
            pt_lb.x = pt_lb.x - (delta_lat / delta_height * (pt_lb1.x - 0));
            pt_lb.y = pt_lb.y - (delta_long / delta_width * (pt_lb1.y - 0));
            pt_lt.x = pt_lt.x - (delta_lat / delta_height * (pt_lt1.x - 1));
            pt_lt.y = pt_lt.y - (delta_long / delta_width * (pt_lt1.y - 0));
            pt_rb.x = pt_rb.x - (delta_lat / delta_height * (pt_rb1.x - 0));
            pt_rb.y = pt_rb.y - (delta_long / delta_width * (pt_rb1.y - 1));
            pt_rt.x = pt_rt.x - (delta_lat / delta_height * (pt_rt1.x - 1));
            pt_rt.y = pt_rt.y - (delta_long / delta_width * (pt_rt1.y - 1));
            GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
            //locError(GPTS);
            //
            //
        }
        //locError(Boolean.toString(isDrift));
        return GPTS;
    }

    public void getGeoInfo(String filePath, int Type, String uri, String name) {
        //locError(name);
        String m_name = name;
        String m_uri = uri;
        String bmPath = "";
        File file = new File(filePath);
        InputStream in = null;
        String m_WKT = "";
        Boolean isESRI = false;
        int num_line = 0;
        try {
            if (Type == SAMPLE_TYPE){
                in = getAssets().open(SAMPLE_FILE);
                //bmPath = createThumbnails(name, filePath, SAMPLE_TYPE);
                //locError(getAssets().open("image/cangyuan.jpg").toString());
                //bmPath = getAssets().open("image/cangyuan.jpg").toString();
            }
            else {
                in = new FileInputStream(file);
                bmPath = createThumbnails(name, filePath, URI_TYPE);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            String m_BBox = "", m_GPTS = "", m_MediaBox = "", m_CropBox = "", m_LPTS = "";
            int m_num_GPTS = 0;
            //line = bufferedReader.readLine()
            while((line = bufferedReader.readLine()) != null) {
                //sb.append(line + "/n");
                if(line.contains("PROJCS")) {

                    m_WKT = line.substring(line.indexOf("PROJCS["), line.indexOf(")>>"));

                }
                if (line.contains("ESRI") || line.contains("esri") || line.contains("arcgis") || line.contains("ARCGIS") || line.contains("Adobe"))
                {
                    isESRI = true;
                }
                if (line.contains("/BBox") & line.contains("Viewport")){
                    //Log.w(TAG, "the line loc = " + Integer.toString(num_line) );
                    m_BBox = line.substring(line.indexOf("BBox") + 5);
                    if(!m_BBox.contains("BBox")){
                    m_BBox = m_BBox.substring(0, m_BBox.indexOf("]"));
                    m_BBox = m_BBox.trim();
                    }else {
                        m_BBox = m_BBox.substring(m_BBox.indexOf("BBox") + 5);
                        m_BBox = m_BBox.substring(0, m_BBox.indexOf("]"));
                        m_BBox = m_BBox.trim();
                    }
                    locError("BBox : " + m_BBox);
                }
                if (line.contains("GPTS") & line.contains("LPTS")){
                    m_LPTS = line.substring(line.indexOf("LPTS") + 5);
                    m_LPTS = m_LPTS.substring(0, m_LPTS.indexOf("]"));
                    m_LPTS = m_LPTS.trim();
                    if (m_LPTS.length() > 0){
                        m_GPTS = line.substring(line.indexOf("GPTS") + 5);
                        m_GPTS = m_GPTS.substring(0, m_GPTS.indexOf("]"));
                        m_GPTS = m_GPTS.trim();
                        //坐标飘移纠偏
                        m_GPTS = getGPTS(m_GPTS, m_LPTS);
                    }
                    //locError(m_GPTS);
                    Log.w(TAG, "hold on" + m_GPTS );

                }
                if (line.contains("MediaBox")){
                    line = line.substring(line.indexOf("MediaBox"));
                    m_MediaBox = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    m_MediaBox = m_MediaBox.trim();
                    Log.w(TAG, "MediaBox : " + m_MediaBox );
                }
                if (line.contains("CropBox")){
                    m_CropBox = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    m_CropBox = m_CropBox.trim();
                    //Log.w(TAG, m_CropBox );
                }
                num_line += 1;
            }
            if (m_CropBox.isEmpty()){
                m_CropBox = m_MediaBox;
            }
            locError(Integer.toString(m_num_GPTS));
            //Log.w(TAG, "GPTS:" + m_GPTS );
            //Log.w(TAG, "BBox:" + m_BBox );
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
            //locError("看这里"+m_name);
            //locError("WKT: " + m_WKT);
            if (filePath != "") {
                //locError(m_MediaBox + "&" + m_BBox + "&" + m_GPTS);
                m_GPTS = rubberCoordination(m_MediaBox, m_BBox, m_GPTS);
                saveGeoInfo(m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name + Long.toString(System.currentTimeMillis()));
            } else {
                m_GPTS = rubberCoordination(m_MediaBox, m_BBox, m_GPTS);
                saveGeoInfo("Demo", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name + Long.toString(System.currentTimeMillis()));
            }
        } catch (IOException e) {
            Toast.makeText(this, "地理信息获取失败, 请联系程序员", Toast.LENGTH_LONG).show();
        }

    }



}
