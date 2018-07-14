package com.geopdfviewer.android;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.LogWriter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;


import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.geopdfviewer.android.DataUtil.makeTxt;

public class select_page extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = "select_page";
    private final static int REQUEST_CODE = 42;
    private final static int REQUEST_CODE_INPUT = 41;
    public static final int PERMISSION_CODE = 42042;
    public static final int SAMPLE_TYPE = 1;
    public static final int URI_TYPE = 2;
    private final String DEF_DIR =  Environment.getExternalStorageDirectory().toString() + "/tencent/TIMfile_recv/";

    public static final String SAMPLE_FILE = "dt/cangyuan.dt";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String LOC_DELETE_ITEM = "map_num";
    private Map_test[] map_tests = new Map_test[30];
    private List<Map_test> map_testList = new ArrayList<>();
    private Map_testAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;

    //记录地图文件添加过程
    private int add_current = 0, add_max = 0;

    //记录当前坐标信息
    double m_lat, m_long;

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

    //记录长按的position
    private String mselectedpos = "";

    //添加地图按钮声明
    com.github.clans.fab.FloatingActionButton addbt;
    //导出数据按钮声明
    com.github.clans.fab.FloatingActionButton outputbt;
    //导入数据按钮声明
    com.github.clans.fab.FloatingActionButton inputbt;
    //fam菜单声明
    FloatingActionMenu floatingActionsMenu;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        switch (isLongClick){
            case 1:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.delete).setVisible(false);
                menu.findItem(R.id.mmcancel).setVisible(false);
                menu.findItem(R.id.showinfo).setVisible(true);
                break;
            case 0:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.delete).setVisible(true);
                menu.findItem(R.id.mmcancel).setVisible(true);
                menu.findItem(R.id.showinfo).setVisible(false);
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
        setTitle(select_page.this.getResources().getText(R.string.MapList));
        selectedNum = 0;
        isLongClick = 1;
        invalidateOptionsMenu();
        floatingActionsMenu.setVisibility(View.VISIBLE);
    }

    private void parseSelectedpos(){
        if (mselectedpos.contains(" ")){
            String[] nums = mselectedpos.split(" ");
            for (int i = 0; i < nums.length; i++){
                deleteData(Integer.valueOf(nums[i]));
            }
        }else {
            deleteData(selectedNum);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            case  R.id.delete:
                if (selectedNum != 0){
                    //deleteData(selectedNum);
                    parseSelectedpos();
                    resetView();
                }else Toast.makeText(this, this.getResources().getText(R.string.NoLongClickError).toString(), Toast.LENGTH_LONG).show();
                break;
            case  R.id.mmcancel:
                if (selectedNum != 0){
                    //isLongClick = 1;
                    refreshRecycler();
                    resetView();
                }else Toast.makeText(this, this.getResources().getText(R.string.NoLongClickError).toString(), Toast.LENGTH_LONG).show();
                break;
            case  R.id.showinfo:
                //if (selectedNum != 0){
                    //showInfo(selectedNum);
                    //resetView();
                //}else
                Toast.makeText(this, this.getResources().getText(R.string.version_info_0).toString() + this.getResources().getText(R.string.version_info).toString() + "\n" + this.getResources().getText(R.string.version_info_1).toString() + "\n" + this.getResources().getText(R.string.version_info_2).toString() + "\n" + this.getResources().getText(R.string.version_info_3).toString(), Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    private void showInfo(int num){
        refreshRecycler();
        /*Intent intent = new Intent(select_page.this, info_page.class);
        intent.putExtra("extra_data", getWKT(num));
        startActivity(intent);*/
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        SharedPreferences prf1 = getSharedPreferences("filepath", MODE_PRIVATE);
        String filepath = prf1.getString("mapath", "");
        locError("mapath: " + filepath);
        if (filepath.isEmpty()) intent.setType("application/dt");
        else intent.setDataAndType(Uri.parse(filepath), "application/dt");
        try {
            startActivityForResult(intent, REQUEST_CODE);
    } catch (ActivityNotFoundException e) {
        //alert user that file manager not working
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getContext(), R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
            }
        });
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
                //Map_testAdapter.ViewHolder holder = new Map_testAdapter.ViewHolder(view);
                setTitle(select_page.this.getResources().getText(R.string.IsLongClicking));
                floatingActionsMenu.setVisibility(View.INVISIBLE);
                locError("map_num: " + Integer.toString(map_num) + "\n" + "position: " + Integer.toString(position));
                selectedNum = map_num;
                selectedpos = position;
                mselectedpos = String.valueOf(map_num);
                if (isLongClick != 0){
                    isLongClick = 0;
                }else {
                    adapter.notifyItemChanged(selectedpos);
                }
                locError("mselectedpos: " + mselectedpos);
                invalidateOptionsMenu();
            }
        });
        adapter.setOnItemClickListener(new Map_testAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, int map_num, int position) {
                Map_testAdapter.ViewHolder holder = new Map_testAdapter.ViewHolder(view);
                Map_test map = map_testList.get(position);
                if (isLongClick == 0){
                    if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY){
                        holder.cardView.setCardBackgroundColor(Color.GRAY);
                        mselectedpos = mselectedpos + " " + String.valueOf(map_num);
                    }else {
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        if (mselectedpos.contains(" ")) {
                            String replace = " " + String.valueOf(map_num);
                            mselectedpos = mselectedpos.replace(replace, "");
                            if (mselectedpos.length() == mselectedpos.replace(replace, "").length()){
                                String replace1 = String.valueOf(map_num) + " ";
                                mselectedpos = mselectedpos.replace(replace1, "");
                            }
                        }else {
                            refreshRecycler();
                            resetView();
                        }
                    }
                    locError("mselectedpos: " + mselectedpos);
                }else {
                    Intent intent = new Intent(select_page.this, MainInterface.class);
                    intent.putExtra("num", map.getM_num());
                    select_page.this.startActivity(intent);
                }
            }
        });
        //adapter.getItemSelected();
        recyclerView.setAdapter(adapter);
        /*addbt.hide(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addbt.show(true);
                addbt.setShowAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.show_from_bottom));
                addbt.setHideAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.hide_to_bottom));
            }
        }, 300);*/
    }

    //获取当前坐标位置
    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(this, this.getResources().getText(R.string.LocError), Toast.LENGTH_SHORT).show();
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
        if (location != null){
            m_lat = location.getLatitude();
            m_long = location.getLongitude();
            SharedPreferences.Editor editor = getSharedPreferences("latlong", MODE_PRIVATE).edit();
            editor.clear().commit();
            editor.putString("mlatlong", Double.toString(m_lat) + "," + Double.toString(m_long));
            editor.apply();
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
        /*
        List<POI> pois = LitePal.where("ic = ?", m_ic).find(POI.class);
        for ( POI poi : pois){
            String poic = poi.getPoic();
            LitePal.deleteAll(MTAPE.class, "POIC = ?", poic);
            LitePal.deleteAll(MPHOTO.class, "POIC = ?", poic);
        }
        LitePal.deleteAll(POI.class, "ic = ?", m_ic);
        LitePal.deleteAll(Trail.class, "ic = ?", m_ic);*/
    }

    public void deletemFile(String filePath){
        File file = new File(filePath);
        if (file.exists() && file.isFile()){
            if (file.delete()){
                //Toast.makeText(this, "删除文件成功", Toast.LENGTH_SHORT).show();
            }else Toast.makeText(this, this.getResources().getText(R.string.DeleteError), Toast.LENGTH_SHORT).show();
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

    public void Btn_clearData(){
        SharedPreferences.Editor pref = getSharedPreferences("data_num", MODE_PRIVATE).edit();
        pref.clear().commit();
        SharedPreferences.Editor pref1 = getSharedPreferences("data", MODE_PRIVATE).edit();
        pref1.clear().commit();
        //Toast.makeText(this, "清除操作完成", Toast.LENGTH_LONG).show();
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
    public static final int UPDATE_TEXT = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:
                    // 在这里进行UI操作
                    refreshRecycler();
                    if (add_current == add_max){
                    toolbar.setTitle(select_page.this.getResources().getText(R.string.MapList));
                    }
                    Log.w(TAG, "handleMessage: " );
            }
        }
    };


    private boolean isOKForAddMap(String name){
        SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
        int size = pref1.getInt("num", num_pdf);
        SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
        boolean isSameName = false;
        for (int i = 1; i <= size; i++){
            String str = "n_" + Integer.toString(i) + "_";
            if (pref2.getString(str + "name", "").equals(name)) {
                isSameName = true;
                break;
            }
        }
        if (isSameName) return false;
        else return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case REQUEST_CODE:
                uri = data.getData();
                locError(uri.toString());
                boolean isOKForAddMap = false;
                if (uri.toString().contains(".dt")) {
                    try {
                        String configPath;
                        configPath = URLDecoder.decode(uri.toString(), "utf-8");
                        if (configPath.substring(8).contains(":")) {
                            configPath = Environment.getExternalStorageDirectory().toString() + "/" + configPath.substring(configPath.lastIndexOf(":") + 1, configPath.length());
                        } else
                            configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                        //configPath = getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                        configPath = URLDecoder.decode(configPath, "utf-8");
                        isOKForAddMap = isOKForAddMap(DataUtil.findNameFromUri(Uri.parse(configPath)));
                    } catch (UnsupportedEncodingException e) {

                    }
                } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri).contains(".dt")) {
                    try {
                        String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                        configPath = URLDecoder.decode(configPath, "utf-8");
                        isOKForAddMap = isOKForAddMap(DataUtil.findNameFromUri(Uri.parse(configPath)));
                    } catch (UnsupportedEncodingException e) {

                    }
                } else Toast.makeText(this, this.getResources().getText(R.string.OpenFileError), Toast.LENGTH_SHORT).show();
                if (isOKForAddMap) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(select_page.this);
                    dialog.setTitle("请选择解析类型");
                    dialog.setMessage("如果距离值显示有误， 请删除地图后， 选择另一类型再进行添加。");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("类型一", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (uri.toString().contains(".dt")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        add_max++;
                                        try {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                    Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            locError(URLDecoder.decode(uri.getAuthority(), "utf-8"));
                                            String configPath;
                                            configPath = URLDecoder.decode(uri.toString(), "utf-8");
                                            if (configPath.substring(8).contains(":")) {
                                                configPath = Environment.getExternalStorageDirectory().toString() + "/" + configPath.substring(configPath.lastIndexOf(":") + 1, configPath.length());
                                            } else
                                                configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                            //configPath = getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                            configPath = URLDecoder.decode(configPath, "utf-8");
                                            SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                            editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                            editor.apply();
                                            manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true);
                                            locError(configPath);
                                            //locError(uri.toString());
                                            //locError(findNameFromUri(uri));
                                            //LitePal.getDatabase();
                                            Message message = new Message();
                                            message.what = UPDATE_TEXT;
                                            handler.sendMessage(message);
                                        } catch (Exception e) {
                                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).start();
                            } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri).contains(".dt")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        add_max++;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        try {
                                            String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                            configPath = URLDecoder.decode(configPath, "utf-8");
                                            SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                            editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                            editor.apply();
                                            manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true);
                                        } catch (Exception e) {
                                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                        }
                                        //locError(uri.toString());
                                        //locError(findNameFromUri(uri));
                                        //LitePal.getDatabase();
                                        Message message = new Message();
                                        message.what = UPDATE_TEXT;
                                        handler.sendMessage(message);


                                    }
                                }).start();

                            } else Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError)+ "_1", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.setNegativeButton("类型二", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (uri.toString().contains(".dt")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        add_max++;
                                        try {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                    Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            locError(URLDecoder.decode(uri.getAuthority(), "utf-8"));
                                            String configPath;
                                            configPath = URLDecoder.decode(uri.toString(), "utf-8");
                                            if (configPath.substring(8).contains(":")) {
                                                configPath = Environment.getExternalStorageDirectory().toString() + "/" + configPath.substring(configPath.lastIndexOf(":") + 1, configPath.length());
                                            } else
                                                configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                            //configPath = getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                            configPath = URLDecoder.decode(configPath, "utf-8");
                                            SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                            editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                            editor.apply();
                                            manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false);
                                            locError(configPath);
                                            //locError(uri.toString());
                                            //locError(findNameFromUri(uri));
                                            //LitePal.getDatabase();
                                            Message message = new Message();
                                            message.what = UPDATE_TEXT;
                                            handler.sendMessage(message);
                                        } catch (Exception e) {
                                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).start();
                            } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri).contains(".dt")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        add_max++;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        try {
                                            String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                            configPath = URLDecoder.decode(configPath, "utf-8");
                                            SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                            editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                            editor.apply();
                                            manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false);
                                        } catch (Exception e) {
                                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                        }
                                        //locError(uri.toString());
                                        //locError(findNameFromUri(uri));
                                        //LitePal.getDatabase();
                                        Message message = new Message();
                                        message.what = UPDATE_TEXT;
                                        handler.sendMessage(message);


                                    }
                                }).start();

                            } else Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_1", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                } else Toast.makeText(this, select_page.this.getResources().getText(R.string.AddedMapTip), Toast.LENGTH_SHORT).show();

            /*locError(getRealPath(uri.toString()));
            locError(uri.toString());
            locError(findNameFromUri(uri));
            LitePal.getDatabase();*/
                //refreshRecycler();
                    break;
                /*case REQUEST_CODE_INPUT:
                    final File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Input");
                    if (!file.exists() && !file.isDirectory()){
                        file.mkdirs();
                    }
                    uri = data.getData();
                    locError(uri.toString());
                        if (uri.toString().contains(".zip")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                toolbar.setTitle("正在解析数据");
                                                Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetInputData).toString() + R.string.QSH, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        locError(URLDecoder.decode(uri.getAuthority(), "utf-8"));
                                        String configPath;
                                        configPath = URLDecoder.decode(uri.toString(), "utf-8");
                                        if (configPath.substring(8).contains(":")) {
                                            configPath = Environment.getExternalStorageDirectory().toString() + "/" + configPath.substring(configPath.lastIndexOf(":") + 1, configPath.length());
                                        } else configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                        //configPath = getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                        configPath = URLDecoder.decode(configPath, "utf-8");
                                        SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                        editor.putString("inputpath", configPath.substring(0, configPath.lastIndexOf("/")));
                                        editor.apply();
                                        locError("filepath" + configPath.substring(0, configPath.lastIndexOf("/")));
                                        File file1 = new File(configPath);
                                        File outFile = null ;   // 输出文件的时候要有文件夹的操作
                                        ZipFile zipFile = new ZipFile(file1) ;   // 实例化ZipFile对象
                                        ZipInputStream zipInput = null ;    // 定义压缩输入流
                                        OutputStream out = null ;   // 定义输出流，用于输出每一个实体内容
                                        InputStream input = null ;  // 定义输入流，读取每一个ZipEntry
                                        ZipEntry entry = null ; // 每一个压缩实体
                                        zipInput = new ZipInputStream(new FileInputStream(file1)) ;  // 实例化ZIpInputStream
                                        while((entry = zipInput.getNextEntry())!=null){ // 得到一个压缩实体
                                            //System.out.println("解压缩" + entry.getName() + "文件。") ;
                                            locError(entry.toString());
                                            outFile = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Input", entry.getName()) ;   // 定义输出的文件路径
                                            input = zipFile.getInputStream(entry) ; // 得到每一个实体的输入流
                                            out = new FileOutputStream(outFile) ;   // 实例化文件输出流
                                            byte buffer[] = new byte[4096];
                                            int realLength;
                                            while ((realLength = input.read(buffer)) > 0){
                                                out.write(buffer, 0, realLength);
                                            }
                                            input.close() ;     // 关闭输入流
                                            out.close() ;   // 关闭输出流
                                        }
                                        input.close() ;
                                        locError(configPath);
                                        //locError(uri.toString());
                                        //locError(findNameFromUri(uri));
                                        //LitePal.getDatabase();
                                    } catch (UnsupportedEncodingException e) {
                                        Log.w(TAG, "出错" );
                                    }catch (IOException e){
                                        Log.w(TAG, "出错" );
                                    }
                                    //入库操作
                                    File ff = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Input");
                                    if (!ff.exists() && !ff.isDirectory()){
                                        ff.mkdirs();
                                    }
                                    File[] ffs = ff.listFiles();
                                    int size1 = ffs.length;
                                    for (int i = 0; i < size1; i++){
                                        if (ffs[i].toString().contains(".dtdb")){
                                            addToDataBase(ffs[i].toString());
                                            locError(ffs[i].toString());
                                            ffs[i].delete();
                                        }
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toolbar.setTitle(select_page.this.getResources().getText(R.string.MapList));
                                            Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.DataInputOk), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).start();
                        } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri).contains(".zip")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toolbar.setTitle("正在解析数据");
                                            Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetInputData).toString() + R.string.QSH, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    try {
                                        String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), uri);
                                        configPath = URLDecoder.decode(configPath, "utf-8");
                                        SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                        editor.putString("inputpath", configPath.substring(0, configPath.lastIndexOf("/")));
                                        editor.apply();
                                        locError("filepath" + configPath.substring(0, configPath.lastIndexOf("/")));
                                        File file1 = new File(configPath);
                                        File outFile = null ;   // 输出文件的时候要有文件夹的操作
                                        ZipFile zipFile = new ZipFile(file1) ;   // 实例化ZipFile对象
                                        ZipInputStream zipInput = null ;    // 定义压缩输入流
                                        OutputStream out = null ;   // 定义输出流，用于输出每一个实体内容
                                        InputStream input = null ;  // 定义输入流，读取每一个ZipEntry
                                        ZipEntry entry = null ; // 每一个压缩实体
                                        zipInput = new ZipInputStream(new FileInputStream(file1)) ;  // 实例化ZIpInputStream
                                        while((entry = zipInput.getNextEntry())!=null){ // 得到一个压缩实体
                                            //System.out.println("解压缩" + entry.getName() + "文件。") ;
                                            locError(entry.toString());
                                            outFile = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Input", entry.getName()) ;   // 定义输出的文件路径
                                            input = zipFile.getInputStream(entry) ; // 得到每一个实体的输入流
                                            out = new FileOutputStream(outFile) ;   // 实例化文件输出流
                                            byte buffer[] = new byte[4096];
                                            int realLength;
                                            while ((realLength = input.read(buffer)) > 0){
                                                out.write(buffer, 0, realLength);
                                            }
                                            input.close() ;     // 关闭输入流
                                            out.close() ;   // 关闭输出流
                                        }
                                        input.close() ;
                                    } catch (IOException e) {
                                        Log.w(TAG, "出错" );
                                    }
                                    //locError(uri.toString());
                                    //locError(findNameFromUri(uri));
                                    //LitePal.getDatabase();
//入库操作
                                    File ff = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Input");
                                    if (!ff.exists() && !ff.isDirectory()){
                                        ff.mkdirs();
                                    }
                                    File[] ffs = ff.listFiles();
                                    int size1 = ffs.length;
                                    for (int i = 0; i < size1; i++){
                                        if (ffs[i].toString().contains(".dtdb")){
                                            addToDataBase(ffs[i].toString());
                                            locError(ffs[i].toString());
                                            ffs[i].delete();
                                        }
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toolbar.setTitle(select_page.this.getResources().getText(R.string.MapList));
                                            Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.DataInputOk), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            }).start();

                        } else Toast.makeText(this, select_page.this.getResources().getText(R.string.OpenFileError), Toast.LENGTH_SHORT).show();
                        break;*/
                case 17:
                    final String path = data.getStringExtra("filePath");
                    Log.w(TAG, "llll: " + path);
                    boolean isOKForAddMap1 = false;
                    if (path.contains(".dt")) {
                        try {
                            String configPath;
                            configPath = URLDecoder.decode(path, "utf-8");
                            isOKForAddMap1 = isOKForAddMap(DataUtil.findNameFromUri(Uri.parse(configPath)));
                        } catch (UnsupportedEncodingException e) {

                        }
                    } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path)).contains(".dt")) {
                        try {
                            String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path));
                            configPath = URLDecoder.decode(configPath, "utf-8");
                            isOKForAddMap1 = isOKForAddMap(DataUtil.findNameFromUri(Uri.parse(configPath)));
                        } catch (Exception e) {
                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                        }
                    } else Toast.makeText(this, this.getResources().getText(R.string.OpenFileError), Toast.LENGTH_SHORT).show();
                    if (isOKForAddMap1) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(select_page.this);
                        dialog.setTitle("请选择解析类型");
                        dialog.setMessage("如果距离值显示有误， 请删除地图后， 选择另一类型再进行添加。");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("类型一", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (path.contains(".dt")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            add_max++;
                                            try {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                        Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                String configPath;
                                                configPath = URLDecoder.decode(path, "utf-8");
                                                SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                                editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                                editor.apply();
                                                manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true);
                                                locError(configPath);
                                                //locError(path);
                                                //locError(findNameFromUri(uri));
                                                //LitePal.getDatabase();
                                                Message message = new Message();
                                                message.what = UPDATE_TEXT;
                                                handler.sendMessage(message);
                                            } catch (Exception e) {
                                                Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).start();
                                } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path)).contains(".dt")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            add_max++;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                    Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            try {
                                                String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path));
                                                configPath = URLDecoder.decode(configPath, "utf-8");
                                                SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                                editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                                editor.apply();
                                                manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true);
                                            } catch (Exception e) {
                                                Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                            }
                                            //locError(path);
                                            //locError(findNameFromUri(uri));
                                            //LitePal.getDatabase();
                                            Message message = new Message();
                                            message.what = UPDATE_TEXT;
                                            handler.sendMessage(message);


                                        }
                                    }).start();

                                } else Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError), Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.setNegativeButton("类型二", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (path.contains(".dt")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            add_max++;
                                            try {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                        Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                String configPath;
                                                configPath = URLDecoder.decode(path, "utf-8");
                                                SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                                editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                                editor.apply();
                                                manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false);
                                                locError(configPath);
                                                //locError(path);
                                                //locError(findNameFromUri(uri));
                                                //LitePal.getDatabase();
                                                Message message = new Message();
                                                message.what = UPDATE_TEXT;
                                                handler.sendMessage(message);
                                            } catch (Exception e) {
                                                Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).start();
                                } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path)).contains(".dt")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            add_max++;
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                                    Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfo).toString() + select_page.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            try {
                                                String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path));
                                                configPath = URLDecoder.decode(configPath, "utf-8");
                                                SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                                editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                                editor.apply();
                                                manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false);
                                            } catch (Exception e) {
                                                Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                            }
                                            //locError(path);
                                            //locError(findNameFromUri(uri));
                                            //LitePal.getDatabase();
                                            Message message = new Message();
                                            message.what = UPDATE_TEXT;
                                            handler.sendMessage(message);


                                        }
                                    }).start();

                                } else Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_1", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show();
                    } else Toast.makeText(this, select_page.this.getResources().getText(R.string.AddedMapTip), Toast.LENGTH_SHORT).show();
                    break;
                case 18:
                    final File file33 = new File(Environment.getExternalStorageDirectory() + "/TuZhi" + "/Input");
                    if (!file33.exists() && !file33.isDirectory()){
                        file33.mkdirs();
                    }
                    final String path1 = data.getStringExtra("filePath");
                    if (path1.contains(".zip")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.w(TAG, "run1111: ");
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toolbar.setTitle("正在解析数据");
                                            Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetInputData).toString() + R.string.QSH, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    String configPath;
                                    configPath = URLDecoder.decode(path1, "utf-8");
                                    SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                    editor.putString("inputpath", configPath.substring(0, configPath.lastIndexOf("/")));
                                    editor.apply();
                                    locError("filepath" + configPath.substring(0, configPath.lastIndexOf("/")));
                                    File file1 = new File(configPath);
                                    File outFile = null ;   // 输出文件的时候要有文件夹的操作
                                    ZipFile zipFile = new ZipFile(file1) ;   // 实例化ZipFile对象
                                    ZipInputStream zipInput = null ;    // 定义压缩输入流
                                    OutputStream out = null ;   // 定义输出流，用于输出每一个实体内容
                                    InputStream input = null ;  // 定义输入流，读取每一个ZipEntry
                                    ZipEntry entry = null ; // 每一个压缩实体
                                    zipInput = new ZipInputStream(new FileInputStream(file1)) ;  // 实例化ZIpInputStream
                                    while((entry = zipInput.getNextEntry())!=null){ // 得到一个压缩实体
                                        //System.out.println("解压缩" + entry.getName() + "文件。") ;
                                        locError(entry.toString());
                                        outFile = new File(Environment.getExternalStorageDirectory() + "/TuZhi" + "/Input", entry.getName()) ;   // 定义输出的文件路径
                                        input = zipFile.getInputStream(entry) ; // 得到每一个实体的输入流
                                        out = new FileOutputStream(outFile) ;   // 实例化文件输出流
                                        byte buffer[] = new byte[4096];
                                        int realLength;
                                        while ((realLength = input.read(buffer)) > 0){
                                            out.write(buffer, 0, realLength);
                                        }
                                        input.close() ;     // 关闭输入流
                                        out.close() ;   // 关闭输出流
                                    }
                                    input.close() ;
                                    locError(configPath);
                                    //locError(uri.toString());
                                    //locError(findNameFromUri(uri));
                                    //LitePal.getDatabase();
                                } catch (UnsupportedEncodingException e) {
                                    Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                }catch (IOException e){
                                    Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                }
                                //入库操作
                                File ff = new File(Environment.getExternalStorageDirectory() + "/TuZhi" + "/Input");
                                if (!ff.exists() && !ff.isDirectory()){
                                    ff.mkdirs();
                                }
                                File[] ffs = ff.listFiles();
                                int size1 = ffs.length;
                                for (int i = 0; i < size1; i++){
                                    if (ffs[i].toString().contains(".dtdb")){
                                        Log.e(TAG, "the cup of life: ");
                                        addToDataBase(ffs[i].toString());
                                        locError(ffs[i].toString());
                                        ffs[i].delete();
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        toolbar.setTitle(select_page.this.getResources().getText(R.string.MapList));
                                        Log.w(TAG, "run: " );
                                        Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.DataInputOk), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).start();
                    }else Toast.makeText(this, select_page.this.getResources().getText(R.string.OpenFileError) + "_1", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    //数据库入库函数
    private void addToDataBase(String filePath){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.InputingData), Toast.LENGTH_LONG).show();
                toolbar.setTitle("数据入库中");
            }
        });
        File file = new File(filePath);
        InputStream in = null;
        int READ_TYPE;
        final int POI_TYPE = 0;
        final int TRAIL_TYPE = 1;
        final int MPHOTO_TYPE = 2;
        final int MTAPE_TYPE = 3;
        final int LINES_WHITEBLANK_TYPE = 4;
        final int NONE_TYPE = -1;
        try {
            String line;
            in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            READ_TYPE = NONE_TYPE;
            String ic = "", name = "", poic = "", description = "", time = "", starttime = "", endtime = "", m_ic = "", m_lines = "", path = "", type = "";
            int color = 0, photonum = 0, tapenum = 0, id = 0;
            float x = 0, y = 0;
            while((line = bufferedReader.readLine()) != null) {
                if (line.contains("</POI>") || line.contains("</Trail>") || line.contains("</MPHOTO>") || line.contains("</MTAPE>") || line.contains("</Lines_WhiteBlank>")) READ_TYPE = NONE_TYPE;
                if (line.contains("<POI>")) {
                    READ_TYPE = POI_TYPE;
                    continue;
                }
                if (line.contains("<Trail>")) {
                    READ_TYPE = TRAIL_TYPE;
                    continue;
                }
                if (line.contains("<MPHOTO>")) {
                    READ_TYPE = MPHOTO_TYPE;
                    continue;
                }
                if (line.contains("<MTAPE>")) {
                    READ_TYPE = MTAPE_TYPE;
                    continue;
                }
                if (line.contains("<Lines_WhiteBlank>")) {
                    READ_TYPE = LINES_WHITEBLANK_TYPE;
                    continue;
                }
                if (READ_TYPE == POI_TYPE){
                    if (line.contains("<id>")){
                        id = Integer.valueOf(line.substring(4, line.indexOf("</id>")));
                        continue;
                    }
                    if (line.contains("<ic>")){
                        ic = line.substring(4, line.indexOf("</ic>"));
                        continue;
                    }
                    if (line.contains("<type>")){
                        type = line.substring(6, line.indexOf("</type>"));
                        continue;
                    }
                    if (line.contains("<name>")){
                        name = line.substring(6, line.indexOf("</name>"));
                        continue;
                    }
                    if (line.contains("<POIC>")){
                        poic = line.substring(6, line.indexOf("</POIC>"));
                        continue;
                    }
                    if (line.contains("<photonum>")){
                        photonum = Integer.valueOf(line.substring(10, line.indexOf("</photonum>")));
                        continue;
                    }
                    if (line.contains("<description>")){
                        description = line.substring(13, line.indexOf("</description>"));
                        continue;
                    }
                    if (line.contains("<tapenum>")){
                        tapenum = Integer.valueOf(line.substring(9, line.indexOf("</tapenum>")));
                        continue;
                    }
                    if (line.contains("<x>")){
                        x = Float.valueOf(line.substring(3, line.indexOf("</x>")));
                        continue;
                    }
                    if (line.contains("<y>")){
                        y = Float.valueOf(line.substring(3, line.indexOf("</y>")));
                        continue;
                    }
                    if (line.contains("<time>")){
                        time = line.substring(6, line.indexOf("</time>"));
                        POI poi = new POI();
                        poi.setId(id);
                        poi.setIc(ic);
                        poi.setPoic(poic);
                        poi.setTime(time);
                        poi.setName(name);
                        poi.setX(x);
                        poi.setY(y);
                        poi.setType(type);
                        poi.setTapenum(tapenum);
                        poi.setPhotonum(photonum);
                        poi.setDescription(description);
                        poi.save();
                        continue;
                    }
                }
                if (READ_TYPE == TRAIL_TYPE){
                    if (line.contains("<id>")){
                        id = Integer.valueOf(line.substring(4, line.indexOf("</id>")));
                        continue;
                    }
                    if (line.contains("<ic>")){
                        ic = line.substring(4, line.indexOf("</ic>"));
                        continue;
                    }
                    if (line.contains("<name>")){
                        name = line.substring(6, line.indexOf("</name>"));
                        continue;
                    }
                    if (line.contains("<path>")){
                        path = line.substring(6, line.indexOf("</path>"));
                        continue;
                    }
                    if (line.contains("<starttime>")){
                        starttime = line.substring(11, line.indexOf("</starttime>"));
                        continue;
                    }
                    if (line.contains("<endtime>")){
                        endtime = line.substring(9, line.indexOf("</endtime>"));
                        Trail trail = new Trail();
                        trail.setId(id);
                        trail.setIc(ic);
                        trail.setName(name);
                        trail.setPath(path);
                        trail.setStarttime(starttime);
                        trail.setEndtime(endtime);
                        trail.save();
                        continue;
                    }
                }
                if (READ_TYPE == MPHOTO_TYPE){
                    if (line.contains("<id>")){
                        id = Integer.valueOf(line.substring(4, line.indexOf("</id>")));
                        continue;
                    }
                    if (line.contains("<pdfic>")){
                        ic = line.substring(7, line.indexOf("</pdfic>"));
                        continue;
                    }
                    if (line.contains("<POIC>")){
                        poic = line.substring(6, line.indexOf("</POIC>"));
                        continue;
                    }
                    if (line.contains("<path>")){
                        path = line.substring(6, line.indexOf("</path>"));
                        path = Environment.getExternalStorageDirectory() + "/TuZhi" + "/Input/" + path.substring(path.lastIndexOf("/") + 1);
                        locError("path : " + path);
                        continue;
                    }
                    if (line.contains("<time>")){
                        time = line.substring(6, line.indexOf("</time>"));
                        MPHOTO mphoto = new MPHOTO();
                        mphoto.setId(id);
                        mphoto.setPdfic(ic);
                        mphoto.setPoic(poic);
                        mphoto.setPath(path);
                        mphoto.setTime(time);
                        mphoto.save();
                        continue;
                    }
                }
                if (READ_TYPE == MTAPE_TYPE){
                    if (line.contains("<id>")){
                        id = Integer.valueOf(line.substring(4, line.indexOf("</id>")));
                        continue;
                    }
                    if (line.contains("<pdfic>")){
                        ic = line.substring(7, line.indexOf("</pdfic>"));
                        continue;
                    }
                    if (line.contains("<POIC>")){
                        poic = line.substring(6, line.indexOf("</POIC>"));
                        continue;
                    }
                    if (line.contains("<path>")){
                        path = line.substring(6, line.indexOf("</path>"));
                        path = Environment.getExternalStorageDirectory() + "/TuZhi" + "/Input/" + path.substring(path.lastIndexOf("/") + 1);
                        continue;
                    }
                    if (line.contains("<time>")){
                        time = line.substring(6, line.indexOf("</time>"));
                        MTAPE mtape = new MTAPE();
                        mtape.setId(id);
                        mtape.setPdfic(ic);
                        mtape.setPoic(poic);
                        mtape.setPath(path);
                        mtape.setTime(time);
                        mtape.save();
                        continue;
                    }
                }
                if (READ_TYPE == LINES_WHITEBLANK_TYPE){
                    if (line.contains("<m_ic>")){
                        m_ic = line.substring(6, line.indexOf("</m_ic>"));
                        continue;
                    }
                    if (line.contains("<m_lines>")){
                        m_lines = line.substring(9, line.indexOf("</m_lines>"));
                        continue;
                    }
                    if (line.contains("<m_color>")){
                        color = Integer.valueOf(line.substring(9, line.indexOf("</m_color>")));
                        Lines_WhiteBlank lines_whiteBlank = new Lines_WhiteBlank();
                        lines_whiteBlank.setIc(m_ic);
                        lines_whiteBlank.setLines(m_lines);
                        lines_whiteBlank.setColor(color);
                        lines_whiteBlank.save();
                        continue;
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.DataInputOk), Toast.LENGTH_LONG).show();
                    toolbar.setTitle(select_page.this.getResources().getText(R.string.MapList));
                }
            });
        }catch (IOException e){
            locError(e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_test_page);
        floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam_selectpage);
        floatingActionsMenu.setClosedOnTouchOutside(true);
        setTitle(select_page.this.getResources().getText(R.string.MapList));
        //获取定位信息
        getLocation();
        //locError("deviceId : " + getIMEI());
        //addMap_selectpage按钮事件编辑
        addbt = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addMap_selectpage);
        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*pickFile();
                floatingActionsMenu.close(false);*/
                Intent intent = new Intent(select_page.this, Activity_FileManage.class);
                intent.putExtra("type", ".dt");
                startActivityForResult(intent, 17);
            }
        });
        inputbt = (FloatingActionButton) findViewById(R.id.inputData_selectpage);
        inputbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                SharedPreferences prf1 = getSharedPreferences("filepath", MODE_PRIVATE);
                String filepath = prf1.getString("inputpath", "");
                if (filepath.isEmpty()) intent.setType("application/x-gzip");
                else {
                    intent.setDataAndType(Uri.parse(filepath), "application/x-gzip");
                }
                startActivityForResult(intent, REQUEST_CODE_INPUT);
                floatingActionsMenu.close(false);*/
                Intent intent = new Intent(select_page.this, Activity_FileManage.class);
                intent.putExtra("type", ".zip");
                startActivityForResult(intent, 18);
            }
        });
        //output_selectpage按钮事件编辑
        outputbt = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.outputData_selectpage);
        outputbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<POI> pois = LitePal.findAll(POI.class);
                        List<String> types = new ArrayList<>();
                        Log.w(TAG, "runlzy: " + pois.size());
                        for (int i = 0; i < pois.size(); i++){
                            String temp = pois.get(i).getType();
                            Log.w(TAG, "runlzy: " + temp);
                            if (temp != null) {
                                if (!temp.isEmpty()) {
                                    if (types.size() > 0) {
                                        for (int j = 0; j < types.size(); j++) {
                                            if (temp.equals(types.get(j))) break;
                                            else {
                                                if (j == types.size() - 1) types.add(temp);
                                                else continue;
                                            }
                                        }
                                    }else types.add(temp);
                                }
                            }
                        }
                        DataUtil.makeKML();
                        Log.w(TAG, "runlzy: " + types.size());
                        if (types.size() > 0) {
                            for (int i = 0; i < types.size(); i++) {
                                DataUtil.makeTxt(types.get(i));
                            }
                        }else DataUtil.makeTxt("");
                        DataUtil.makeTxt1();
                        DataUtil.makeWhiteBlankKML();
                        List<File> files = new ArrayList<File>();
                        StringBuffer sb = new StringBuffer();
                        int size_POI = pois.size();
                        sb = sb.append("<POI>").append("\n");
                        for (int i = 0; i < size_POI; i++){
                            sb.append("<id>").append(pois.get(i).getId()).append("</id>").append("\n");
                            sb.append("<ic>").append(pois.get(i).getIc()).append("</ic>").append("\n");
                            sb.append("<name>").append(pois.get(i).getName()).append("</name>").append("\n");
                            sb.append("<POIC>").append(pois.get(i).getPoic()).append("</POIC>").append("\n");
                            sb.append("<type>").append(pois.get(i).getType()).append("</type>").append("\n");
                            sb.append("<photonum>").append(pois.get(i).getPhotonum()).append("</photonum>").append("\n");
                            sb.append("<description>").append(pois.get(i).getDescription()).append("</description>").append("\n");
                            sb.append("<tapenum>").append(pois.get(i).getTapenum()).append("</tapenum>").append("\n");
                            sb.append("<x>").append(pois.get(i).getX()).append("</x>").append("\n");
                            sb.append("<y>").append(pois.get(i).getY()).append("</y>").append("\n");
                            sb.append("<time>").append(pois.get(i).getTime()).append("</time>").append("\n");
                        }
                        sb.append("</POI>").append("\n");
                        List<Trail> trails = LitePal.findAll(Trail.class);
                        int size_trail = trails.size();
                        sb = sb.append("<Trail>").append("\n");
                        for (int i = 0; i < size_trail; i++){
                            sb.append("<id>").append(trails.get(i).getId()).append("</id>").append("\n");
                            sb.append("<ic>").append(trails.get(i).getIc()).append("</ic>").append("\n");
                            sb.append("<name>").append(trails.get(i).getName()).append("</name>").append("\n");
                            sb.append("<path>").append(trails.get(i).getPath()).append("</path>").append("\n");
                            sb.append("<starttime>").append(trails.get(i).getStarttime()).append("</starttime>").append("\n");
                            sb.append("<endtime>").append(trails.get(i).getEndtime()).append("</endtime>").append("\n");
                        }
                        sb.append("</Trail>").append("\n");
                        List<MPHOTO> mphotos = LitePal.findAll(MPHOTO.class);
                        int size_mphoto = mphotos.size();
                        sb = sb.append("<MPHOTO>").append("\n");
                        for (int i = 0; i < size_mphoto; i++){
                            sb.append("<id>").append(mphotos.get(i).getId()).append("</id>").append("\n");
                            sb.append("<pdfic>").append(mphotos.get(i).getPdfic()).append("</pdfic>").append("\n");
                            sb.append("<POIC>").append(mphotos.get(i).getPoic()).append("</POIC>").append("\n");
                            String path = mphotos.get(i).getPath();
                            sb.append("<path>").append(path).append("</path>").append("\n");
                            files.add(new File(path));
                            sb.append("<time>").append(mphotos.get(i).getTime()).append("</time>").append("\n");
                        }
                        sb.append("</MPHOTO>").append("\n");
                        List<MTAPE> mtapes = LitePal.findAll(MTAPE.class);
                        int size_mtape = mtapes.size();
                        sb = sb.append("<MTAPE>").append("\n");
                        for (int i = 0; i < size_mtape; i++){
                            sb.append("<id>").append(mtapes.get(i).getId()).append("</id>").append("\n");
                            sb.append("<pdfic>").append(mtapes.get(i).getPdfic()).append("</pdfic>").append("\n");
                            sb.append("<POIC>").append(mtapes.get(i).getPoic()).append("</POIC>").append("\n");
                            String path = mtapes.get(i).getPath();
                            sb.append("<path>").append(path).append("</path>").append("\n");
                            files.add(new File(path));
                            sb.append("<time>").append(mtapes.get(i).getTime()).append("</time>").append("\n");
                        }
                        sb.append("</MTAPE>").append("\n");
                        List<Lines_WhiteBlank> lines_whiteBlanks = LitePal.findAll(Lines_WhiteBlank.class);
                        int size_lines_whiteBlank = lines_whiteBlanks.size();
                        sb = sb.append("<Lines_WhiteBlank>").append("\n");
                        for (int i = 0; i < size_lines_whiteBlank; i++){
                            sb.append("<m_ic>").append(lines_whiteBlanks.get(i).getIc()).append("</m_ic>").append("\n");
                            sb.append("<m_lines>").append(lines_whiteBlanks.get(i).getLines()).append("</m_lines>").append("\n");
                            sb.append("<m_color>").append(lines_whiteBlanks.get(i).getColor()).append("</m_color>").append("\n");
                        }
                        sb.append("</Lines_WhiteBlank>").append("\n");
                        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
                        if (!file.exists() && !file.isDirectory()){
                            file.mkdirs();
                        }
                        final String outputPath = Long.toString(System.currentTimeMillis());
                        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  outputPath + ".dtdb");
                        try {
                            FileOutputStream of = new FileOutputStream(file1);
                            of.write(sb.toString().getBytes());
                            of.close();
                            files.add(file1);
                        }catch (IOException e){
                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.PackingData).toString() + R.string.QSH, Toast.LENGTH_LONG).show();
                                    toolbar.setTitle("数据打包中");
                                }
                            });
                            File zipFile = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  outputPath + ".zip");
                            //InputStream inputStream = null;
                            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
                            zipOut.setComment("test");
                            int size = files.size();
                            Log.w(TAG, "run: " + size);
                            for (int i = 0; i < size; i++){
                                Log.w(TAG, "run: " + i);
                                Log.w(TAG, "run: " + files.get(i).getPath());
                                boolean isOK = false;
                                for (int k = 0; k < i; k++) {
                                    if (files.get(i).getPath().equals(files.get(k).getPath())) break;
                                    if ((k == i - 1 & !files.get(i).getPath().equals(files.get(k).getPath()) & files.get(i).exists())) isOK = true;
                                }
                                Log.w(TAG, "aa");
                                if (i == 0 & files.get(i).exists()) isOK = true;
                                if (isOK){
                                    Log.w(TAG, "aa");
                                    InputStream inputStream = new FileInputStream(files.get(i));
                                    Log.w(TAG, "aa");
                                    zipOut.putNextEntry(new ZipEntry(files.get(i).getName()));
                                    Log.w(TAG, "aa");
                                    //int temp = 0;
                                    //while ((temp = inputStream.read()) != -1){
                                    //    zipOut.write(temp);
                                    //}
                                    byte buffer[] = new byte[4096];
                                    int realLength;
                                    while ((realLength = inputStream.read(buffer)) > 0) {
                                        zipOut.write(buffer, 0, realLength);
                                    }
                                    inputStream.close();
                                }
                            }
                            zipOut.close();
                            file1.delete();
                            files.clear();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.PackingOk), Toast.LENGTH_LONG).show();
                                    toolbar.setTitle(select_page.this.getResources().getText(R.string.MapList));
                                }
                            });
                        }catch (IOException e){
                            Toast.makeText(select_page.this, select_page.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "run: " + e.toString());
                            Log.w(TAG, "run: " + e.getMessage());
                        }
                    }
                }).start();
                floatingActionsMenu.close(false);
            }
        });
        //初始化界面一
        Log.w(TAG, "onCreate: " );
        if (num_pdf != 0){
            isLongClick = 1;
            selectedNum = 0;
            refreshRecycler();
        }else initPage();


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "onResume: " );
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*if (num_pdf != 0){
        isLongClick = 1;
        selectedNum = 0;
        refreshRecycler();
        }*/
        refreshRecycler();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                add_max ++;
                manageGeoInfo("", SAMPLE_TYPE, "", DataUtil.findNamefromSample(SAMPLE_FILE), true);
                LitePal.getDatabase();
                Message message = new Message();
                message.what = UPDATE_TEXT;
                handler.sendMessage(message);
            }
        }).start();
        //Toast.makeText(this, "第一次进入", Toast.LENGTH_LONG).show();
    }

    public void locError(){
        Log.w(TAG, "可以成功运行到这里" );
    }

    public void locError(String str){
        Log.e(TAG, str );
    }

    public String[] getGeoInfo(String filePath, int Type, String uri, String name, boolean type) {
        //locError(name);
        String m_name = name;
        String m_uri = uri;
        String bmPath = "";
        File file = new File(filePath);
        InputStream in = null;
        String m_WKT = "";
        Boolean isESRI = false;
        int num_line = 0;
        String m_BBox = "", m_GPTS = "", m_MediaBox = "", m_CropBox = "", m_LPTS = "";
        try {
            if (Type == SAMPLE_TYPE){
                in = getAssets().open(SAMPLE_FILE);
                //bmPath = createThumbnails(name, filePath, SAMPLE_TYPE);
                //locError(getAssets().open("image/cangyuan.jpg").toString());
                //bmPath = getAssets().open("image/cangyuan.jpg").toString();
            }
            else {
                in = new FileInputStream(file);
                bmPath = DataUtil.getDtThumbnail(name, "/TuZhi" + "/Thumbnails",  filePath, 120, 180, 30,  select_page.this);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            int m_num_GPTS = 0;
            //line = bufferedReader.readLine()
            while((line = bufferedReader.readLine()) != null) {
                //sb.append(line + "/n");
                if(line.contains("PROJCS")) {
                    //locError(line);
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
                /*if (line.contains("GPTS") & line.contains("LPTS") & m_num_GPTS == 0){
                    m_num_GPTS++;
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
                    //Log.w(TAG, "hold on : " + line );

                }*/
                if (line.contains("GPTS") & line.contains("LPTS")){
                    m_num_GPTS++;
                    m_LPTS = line.substring(line.indexOf("LPTS") + 5);
                    m_LPTS = m_LPTS.substring(0, m_LPTS.indexOf("]"));
                    m_LPTS = m_LPTS.trim();
                    if (m_LPTS.length() > 0){
                        if (m_num_GPTS > 1){
                            String m_GPTS1 = "";
                            m_GPTS1 = line.substring(line.indexOf("GPTS") + 5);
                            m_GPTS1 = m_GPTS1.substring(0, m_GPTS1.indexOf("]"));
                            m_GPTS1 = m_GPTS1.trim();
                            String[] m_gptstr = m_GPTS.split(" ");
                            String[] m_gptstr1 = m_GPTS1.split(" ");
                            locError("1 = " + m_gptstr1[0] + " 2 = " + m_gptstr[0]);
                            if (type) {
                                if (Float.valueOf(m_gptstr1[0]) > Float.valueOf(m_gptstr[0])) {
                                    m_GPTS = m_GPTS1;//BUG
                                    //坐标飘移纠偏
                                    m_GPTS = DataUtil.getGPTS(m_GPTS, m_LPTS);
                                }
                            }else {
                                if (Float.valueOf(m_gptstr1[0]) < Float.valueOf(m_gptstr[0])) {
                                    m_GPTS = m_GPTS1;//BUG
                                    //坐标飘移纠偏
                                    m_GPTS = DataUtil.getGPTS(m_GPTS, m_LPTS);
                                }
                            }

                        }else {
                            m_GPTS = line.substring(line.indexOf("GPTS") + 5);
                            m_GPTS = m_GPTS.substring(0, m_GPTS.indexOf("]"));
                            m_GPTS = m_GPTS.trim();
                            //坐标飘移纠偏
                            m_GPTS = DataUtil.getGPTS(m_GPTS, m_LPTS);
                        }
                    }
                    //locError(m_GPTS);
                    Log.w(TAG, "hold on" + m_GPTS );
                    Log.w(TAG, "hold on : " + line );

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    add_current ++;
                    toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                    Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfoOk), Toast.LENGTH_LONG).show();
                }
            });
            in.close();
            //locError("看这里"+m_name);
            //locError("WKT: " + m_WKT);

        } catch (IOException e) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfoError).toString() + R.string.QLXWM, Toast.LENGTH_LONG).show();
                }
            });

            //Toast.makeText(this, "地理信息获取失败, 请联系程序员", Toast.LENGTH_LONG).show();
        }
        if (filePath != "") {
            //locError(m_MediaBox + "&" + m_BBox + "&" + m_GPTS);
            m_GPTS = DataUtil.rubberCoordinate(m_MediaBox, m_BBox, m_GPTS);
            //saveGeoInfo(m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name);
            String[] strings = new String[]{m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name};
            return strings;
        } else {
            m_GPTS = DataUtil.rubberCoordinate(m_MediaBox, m_BBox, m_GPTS);
            //saveGeoInfo("Demo", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name);
            String[] strings = new String[]{"Demo", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name};
            return strings;
        }
    }

    private void manageGeoInfo(String filePath, int Type, String uri, String name, boolean type){
        String[] strings = getGeoInfo(filePath, Type, uri, name, type);
        saveGeoInfo(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
    }



}
