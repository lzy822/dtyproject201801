package com.geopdfviewer.android;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.provider.ContactsContract;
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
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import static com.geopdfviewer.android.DataUtil.renamePath1;

/**
 * 地图选择界面
 * 用于添加和删除地图，
 * 导入和导出数据
 *
 * @author 李正洋
 *
 * @since   1.1
 */
public class select_page extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {

    private static final String TAG = "select_page";
    private final static int REQUEST_CODE = 42;
    private final static int REQUEST_CODE_INPUT = 41;
    public static final int SAMPLE_TYPE = 1;
    public static final int URI_TYPE = 2;
    private final String DEF_DIR =  Environment.getExternalStorageDirectory().toString() + "/tencent/TIMfile_recv/";
    public static final String LOC_DELETE_ITEM = "map_num";
    private Map_test[] map_tests = new Map_test[30];
    private List<Map_test> map_testList = new ArrayList<>();

    //记录地图文件添加过程
    private int add_current = 0, add_max = 0;

    //记录当前坐标信息
    double m_lat, m_long;

    Location location;

    private LocationManager locationManager;

    class MapCollectionType{
        //NONE, GROSS, XTZ, ZYHJTZ, SHJJTZ, QYDLTZ
        public static final int NONE = -1;
        public static final int GROSS = 0;
        public static final int XTZ = 1;
        public static final int ZYHJTZ = 2;
        public static final int SHJJTZ = 3;
        public static final int QYDLTZ = 4;

    }

    //标识当前是否处于选择地图的状态
    private int mapCollectionType = MapCollectionType.GROSS;

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



    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        // TODO 2021/2/1

        menu.findItem(R.id.queryPOI).setVisible(false);
        menu.findItem(R.id.queryLatLng).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.back).setVisible(false);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                showListPopupWindowForMapQuery(searchView, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        /*老版图志switch (isLongClick){
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
        }*/
        return super.onPrepareOptionsMenu(menu);
    }

    public void showListPopupWindowForMapQuery(View view, String query) {
        final ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        query = query.trim();
        String sql = "select * from ElectronicAtlasMap where";
        String[] strings = query.split(" ");
        for (int i = 0; i < strings.length; ++i) {
            if (strings.length == 1) sql = sql + " (name LIKE '%" + strings[i] + "%')";
            else {
                if (i == 0) sql = sql + " (name LIKE '%" + strings[i] + "%'";
                else if (i != strings.length - 1)
                    sql = sql + " AND name LIKE '%" + strings[i] + "%'";
                else sql = sql + " AND name LIKE '%" + strings[i] + "%')";
            }
        }
        final List<ElectronicAtlasMap> maps = new ArrayList<>();
        Cursor cursor = LitePal.findBySQL(sql);
        if (cursor.moveToFirst()) {
            do {
                String parentNode = cursor.getString(cursor.getColumnIndex("parentnode"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int mapType = cursor.getInt(cursor.getColumnIndex("maptype"));
                String path = cursor.getString(cursor.getColumnIndex("path"));
                String MapGeoStr = cursor.getString(cursor.getColumnIndex("mapgeostr"));
                ElectronicAtlasMap map = new ElectronicAtlasMap(parentNode, name, mapType, path, MapGeoStr);
                maps.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        String[] items = new String[maps.size()];
        for (int i = 0; i < maps.size(); ++i) {
            items[i] = maps.get(i).getName();
            Log.w(TAG, "showListPopupWindowForMapQuery: " + maps.get(i).getName() + ", " + maps.get(i).getPath() + ", " + maps.get(i).getMapGeoStr());
        }

        // ListView适配器
        listPopupWindow.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, items));

        // 选择item的监听事件
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ElectronicAtlasMap map = maps.get(position);
                if (map.getMapGeoStr() != null) {
                    String MapName = map.getName();

                    Intent intent = new Intent(select_page.this, MainInterface.class);
                    intent.putExtra("MapName", MapName);
                    select_page.this.startActivity(intent);
                }
                else
                {
                    List<ElectronicAtlasMap> mapList = LitePal.findAll(ElectronicAtlasMap.class);
                    Boolean HasNode = false;
                    for (int i = 0; i < mapList.size(); i++) {
                        ElectronicAtlasMap map1 = mapList.get(i);
                        if (map1.getParentNode().equals(map.getName())){
                            ParentNodeName = map.getName();
                            mapCollectionType = map1.getMapType();
                            InitElectronicAtlasData();
                            refreshRecyclerForElectronicAtlas();
                            FloatingActionButton floatingActionButton = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.BackFloatingActionButton);
                            if (mapCollectionType == 0){
                                floatingActionButton.setVisibility(View.GONE);
                            }
                            else
                            {
                                floatingActionButton.setVisibility(View.VISIBLE);
                                floatingActionButton.setElevation(100);
                            }
                            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    List<ElectronicAtlasMap> mapList = LitePal.findAll(ElectronicAtlasMap.class);
                                    for (int i = 0; i < mapList.size(); i++) {
                                        ElectronicAtlasMap map = mapList.get(i);
                                        if (map.getName().equals(ParentNodeName)){
                                            ParentNodeName = map.getParentNode();
                                            mapCollectionType = map.getMapType();
                                            InitElectronicAtlasData();
                                            refreshRecyclerForElectronicAtlas();
                                            if (mapCollectionType == 0){
                                                floatingActionButton.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                floatingActionButton.setVisibility(View.VISIBLE);
                                                floatingActionButton.setElevation(100);
                                            }
                                            break;
                                        }
                                        else if (ParentNodeName.equals("")){
                                            mapCollectionType = 0;
                                            InitElectronicAtlasData();
                                            refreshRecyclerForElectronicAtlas();
                                            if (mapCollectionType == 0){
                                                floatingActionButton.setVisibility(View.GONE);
                                            }
                                            else
                                            {
                                                floatingActionButton.setVisibility(View.VISIBLE);
                                                floatingActionButton.setElevation(100);
                                            }
                                            break;
                                        }
                                    }
                                }
                            });
                            HasNode = true;
                            break;
                        }
                    }
                    if (HasNode == false){
                        for (int i = 0; i < mapList.size(); i++) {
                                ParentNodeName = map.getName();
                                switch (ParentNodeName) {
                                case "序图组":
                                    mapCollectionType = 1;
                                    break;
                                case "资源与环境图组":
                                    mapCollectionType = 2;
                                    break;
                                case "社会经济图组":
                                    mapCollectionType = 3;
                                    break;
                                case "区域地理图组":
                                    mapCollectionType = 4;
                                    break;
                                case "县图":
                                    mapCollectionType = 5;
                                    break;
                                case "各县城区图":
                                    mapCollectionType = 6;
                                    break;
                                case "各县影像图":
                                    mapCollectionType = 7;
                                    break;
                                case "乡镇图":
                                    mapCollectionType = 8;
                                    break;
                                case "临翔区":
                                    mapCollectionType = 9;
                                    break;
                                case "凤庆县":
                                    mapCollectionType = 10;
                                    break;
                                case "云县":
                                    mapCollectionType = 11;
                                    break;
                                case "永德县":
                                    mapCollectionType = 12;
                                    break;
                                case "镇康县":
                                    mapCollectionType = 13;
                                    break;
                                case "双江县":
                                    mapCollectionType = 14;
                                    break;
                                case "耿马县":
                                    mapCollectionType = 15;
                                    break;
                                case "沧源县":
                                    mapCollectionType = 16;
                                    break;
                            }
                                InitElectronicAtlasData();
                                refreshRecyclerForElectronicAtlas();
                                FloatingActionButton floatingActionButton = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.BackFloatingActionButton);
                                if (mapCollectionType == 0){
                                    floatingActionButton.setVisibility(View.GONE);
                                }
                                else
                                {
                                    floatingActionButton.setVisibility(View.VISIBLE);
                                    floatingActionButton.setElevation(100);
                                }
                                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        List<ElectronicAtlasMap> mapList = LitePal.findAll(ElectronicAtlasMap.class);
                                        for (int i = 0; i < mapList.size(); i++) {
                                            ElectronicAtlasMap map = mapList.get(i);
                                            if (map.getName().equals(ParentNodeName)){
                                                ParentNodeName = map.getParentNode();
                                                mapCollectionType = map.getMapType();
                                                InitElectronicAtlasData();
                                                refreshRecyclerForElectronicAtlas();
                                                if (mapCollectionType == 0){
                                                    floatingActionButton.setVisibility(View.GONE);
                                                }
                                                else
                                                {
                                                    floatingActionButton.setVisibility(View.VISIBLE);
                                                    floatingActionButton.setElevation(100);
                                                }
                                                break;
                                            }
                                            else if (ParentNodeName.equals("")){
                                                mapCollectionType = 0;
                                                InitElectronicAtlasData();
                                                refreshRecyclerForElectronicAtlas();
                                                if (mapCollectionType == 0){
                                                    floatingActionButton.setVisibility(View.GONE);
                                                }
                                                else
                                                {
                                                    floatingActionButton.setVisibility(View.VISIBLE);
                                                    floatingActionButton.setElevation(100);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                });
                                break;
                        }
                    }
                }
                listPopupWindow.dismiss();
                invalidateOptionsMenu();
            }
        });

        // 对话框的宽高
        listPopupWindow.setWidth(600);
        listPopupWindow.setHeight(600);

        // ListPopupWindow的锚,弹出框的位置是相对当前View的位置
        listPopupWindow.setAnchorView(view);

        // ListPopupWindow 距锚view的距离
        listPopupWindow.setHorizontalOffset(50);
        listPopupWindow.setVerticalOffset(100);

        listPopupWindow.setModal(false);

        listPopupWindow.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.options, menu);
        /*老版图志
        getMenuInflater().inflate(R.menu.deletetoolbar, menu);*/
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.queryPOI).setVisible(false);
        menu.findItem(R.id.queryLatLng).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.back).setVisible(false);
        return true;
    }

    private void resetView(){
        setTitle(select_page.this.getResources().getText(R.string.MapList));
        selectedNum = 0;
        isLongClick = 1;
        invalidateOptionsMenu();
        setFAMVisible(true);
    }

    private void setFAMVisible(boolean visible){
        //fam菜单声明
        FloatingActionMenu floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam_selectpage);
        if (visible)
            floatingActionsMenu.setVisibility(View.VISIBLE);
        else
            floatingActionsMenu.setVisibility(View.INVISIBLE);
    }

    private void parseSelectedpos(){
        if (mselectedpos.contains(" ")){
            String[] nums = mselectedpos.split(" ");
            for (int i = 0; i < nums.length; ++i){
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

    private void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                EnumClass.READ_EXTERNAL_STORAGE);
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                EnumClass.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            EnumClass.READ_EXTERNAL_STORAGE, EnumClass.WRITE_EXTERNAL_STORAGE},
                    EnumClass.PERMISSION_CODE
            );

            return;
        }else launchPicker();
    }

    private void launchPicker() {
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

    public void initMapNext(int num, String name, String WKT, String uri, String GPTS, String BBox, String imguri, String MediaBox, String CropBox, String ic, String center_latlong, int MapType) {
        Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong, MapType);
        map_tests[num_pdf - 1] = mapTest;
        map_testList.clear();
        for (int i = 0; i < num_pdf; ++i) {
            map_testList.add(map_tests[i]);
        }
    }

    public void initMap() {
        map_testList.clear();
        for (int j = 1; j <= num_pdf; ++j) {
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
            // TODO 修改获取函数
            int MapType = pref1.getInt(str + "MapType", -1);
            if (MapType == mapCollectionType) {
                Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong, MapType);
                map_tests[j - 1] = mapTest;
                map_testList.add(map_tests[j - 1]);
            }
        }
        //refreshRecycler();


    }

    //记录上一个所按的按钮
    private int selectedpos;

    //重新刷新Recycler
    public void refreshRecyclerForElectronicAtlas(){
        try {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setElevation(1);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
            recyclerView.setLayoutManager(layoutManager);
            Map_testAdapter adapter = new Map_testAdapter(map_testList);
            if (mapCollectionType == MapCollectionType.NONE) {
                adapter.setOnItemLongClickListener(new Map_testAdapter.OnRecyclerItemLongListener() {
                    @Override
                    public void onItemLongClick(View view, int map_num, int position) {
                        //Map_testAdapter.ViewHolder holder = new Map_testAdapter.ViewHolder(view);
                        setTitle(select_page.this.getResources().getText(R.string.IsLongClicking));
                        setFAMVisible(false);
                        locError("map_num: " + Integer.toString(map_num) + "\n" + "position: " + Integer.toString(position));
                        selectedNum = map_num;
                        selectedpos = position;
                        mselectedpos = String.valueOf(map_num);
                        if (isLongClick != 0) {
                            isLongClick = 0;
                        } else {
                            adapter.notifyItemChanged(selectedpos);
                        }
                        locError("mselectedpos: " + mselectedpos);
                        invalidateOptionsMenu();
                    }
                });
            }
            if (mapCollectionType == 1 || mapCollectionType == 2 || mapCollectionType == 3
                    || mapCollectionType == 5 || mapCollectionType == 6 || mapCollectionType == 7
                    || mapCollectionType == 9 || mapCollectionType == 10 || mapCollectionType == 11
                    || mapCollectionType == 12 || mapCollectionType == 13 || mapCollectionType == 14
                    || mapCollectionType == 15 || mapCollectionType == 16 || mapCollectionType == -1) {
                adapter.setOnItemClickListener(new Map_testAdapter.OnRecyclerItemClickListener() {
                    @Override
                    public void onItemClick(View view, String map_name, int map_num, int position) {
                        Map_testAdapter.ViewHolder holder = new Map_testAdapter.ViewHolder(view);
                        Map_test map = map_testList.get(position);
                        if (isLongClick == 0) {
                            if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY) {
                                holder.cardView.setCardBackgroundColor(Color.GRAY);
                                mselectedpos = mselectedpos + " " + String.valueOf(map_num);
                            } else {
                                holder.cardView.setCardBackgroundColor(Color.WHITE);
                                if (mselectedpos.contains(" ")) {
                                    String replace = " " + String.valueOf(map_num);
                                    mselectedpos = mselectedpos.replace(replace, "");
                                    if (mselectedpos.length() == mselectedpos.replace(replace, "").length()) {
                                        String replace1 = String.valueOf(map_num) + " ";
                                        mselectedpos = mselectedpos.replace(replace1, "");
                                    }
                                } else {
                                    refreshRecycler();
                                    resetView();
                                }
                            }
                            locError("mselectedpos: " + mselectedpos);
                        } else {
                            if (isFileExist(map.getM_uri()) || map.getM_name().equals("图志简介")) {
                                Log.w(TAG, "onItemClick: " + map.getM_name());
                                Intent intent = new Intent(select_page.this, MainInterface.class);
                                if (!map.getM_name().equals("图志简介")) {
                                    //intent.putExtra("num", map.getM_num());
                                    intent.putExtra("MapName", map.getM_name());
                                } else {
                                    intent.putExtra("num", -1);
                                }
                                select_page.this.startActivity(intent);
                            }
                        }
                    }
                });
            } else {
                adapter.setOnItemClickListener(new Map_testAdapter.OnRecyclerItemClickListener() {
                    @Override
                    public void onItemClick(View view, String map_name, int map_num, int position) {
                        ParentNodeName = map_name;
                        // TODO Gross
                        switch (map_name) {
                            case "序图组":
                                mapCollectionType = 1;
                                break;
                            case "资源与环境图组":
                                mapCollectionType = 2;
                                break;
                            case "社会经济图组":
                                mapCollectionType = 3;
                                break;
                            case "区域地理图组":
                                mapCollectionType = 4;
                                break;
                            case "县图":
                                mapCollectionType = 5;
                                break;
                            case "各县城区图":
                                mapCollectionType = 6;
                                break;
                            case "各县影像图":
                                mapCollectionType = 7;
                                break;
                            case "乡镇图":
                                mapCollectionType = 8;
                                break;
                            case "临翔区":
                                mapCollectionType = 9;
                                break;
                            case "凤庆县":
                                mapCollectionType = 10;
                                break;
                            case "云县":
                                mapCollectionType = 11;
                                break;
                            case "永德县":
                                mapCollectionType = 12;
                                break;
                            case "镇康县":
                                mapCollectionType = 13;
                                break;
                            case "双江县":
                                mapCollectionType = 14;
                                break;
                            case "耿马县":
                                mapCollectionType = 15;
                                break;
                            case "沧源县":
                                mapCollectionType = 16;
                                break;
                        }
                        /*initMap();
                        refreshRecycler();*/
                        FloatingActionButton floatingActionButton = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.BackFloatingActionButton);
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                List<ElectronicAtlasMap> mapList = LitePal.findAll(ElectronicAtlasMap.class);
                                for (int i = 0; i < mapList.size(); i++) {
                                    ElectronicAtlasMap map = mapList.get(i);
                                    if (map.getName().equals(ParentNodeName)){
                                        ParentNodeName = map.getParentNode();
                                        mapCollectionType = map.getMapType();
                                        InitElectronicAtlasData();
                                        refreshRecyclerForElectronicAtlas();
                                        if (mapCollectionType == 0){
                                            floatingActionButton.setVisibility(View.GONE);
                                        }
                                        else
                                        {
                                            floatingActionButton.setVisibility(View.VISIBLE);
                                            floatingActionButton.setElevation(100);
                                        }
                                        break;
                                    }
                                    else if (ParentNodeName.equals("")){
                                        mapCollectionType = 0;
                                        InitElectronicAtlasData();
                                        refreshRecyclerForElectronicAtlas();
                                        if (mapCollectionType == 0){
                                            floatingActionButton.setVisibility(View.GONE);
                                        }
                                        else
                                        {
                                            floatingActionButton.setVisibility(View.VISIBLE);
                                            floatingActionButton.setElevation(100);
                                        }
                                        break;
                                    }
                                }
                            }
                        });
                        InitElectronicAtlasData();
                        refreshRecyclerForElectronicAtlas();
                    }
                });
            }
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
        catch (Exception e){
            Log.w(TAG, "refreshRecyclerForElectronicAtlas: " + e.toString());
        }
    }

    //重新刷新Recycler
    public void refreshRecycler(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        Map_testAdapter adapter = new Map_testAdapter(map_testList);
        if (mapCollectionType == MapCollectionType.NONE) {
            adapter.setOnItemLongClickListener(new Map_testAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int map_num, int position) {
                    //Map_testAdapter.ViewHolder holder = new Map_testAdapter.ViewHolder(view);
                    setTitle(select_page.this.getResources().getText(R.string.IsLongClicking));
                    setFAMVisible(false);
                    locError("map_num: " + Integer.toString(map_num) + "\n" + "position: " + Integer.toString(position));
                    selectedNum = map_num;
                    selectedpos = position;
                    mselectedpos = String.valueOf(map_num);
                    if (isLongClick != 0) {
                        isLongClick = 0;
                    } else {
                        adapter.notifyItemChanged(selectedpos);
                    }
                    locError("mselectedpos: " + mselectedpos);
                    invalidateOptionsMenu();
                }
            });
        }
        if (mapCollectionType == 1 || mapCollectionType == 2 || mapCollectionType == 3
                || mapCollectionType == 5 || mapCollectionType == 6 || mapCollectionType == 7
                || mapCollectionType == 9 || mapCollectionType == 10 || mapCollectionType == 11
                || mapCollectionType == 12 || mapCollectionType == 13 || mapCollectionType == 14
                || mapCollectionType == 15 || mapCollectionType == 16 || mapCollectionType == -1) {
            adapter.setOnItemClickListener(new Map_testAdapter.OnRecyclerItemClickListener() {
                @Override
                public void onItemClick(View view, String map_name, int map_num, int position) {
                    Map_testAdapter.ViewHolder holder = new Map_testAdapter.ViewHolder(view);
                    Map_test map = map_testList.get(position);
                    if (isLongClick == 0) {
                        if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY) {
                            holder.cardView.setCardBackgroundColor(Color.GRAY);
                            mselectedpos = mselectedpos + " " + String.valueOf(map_num);
                        } else {
                            holder.cardView.setCardBackgroundColor(Color.WHITE);
                            if (mselectedpos.contains(" ")) {
                                String replace = " " + String.valueOf(map_num);
                                mselectedpos = mselectedpos.replace(replace, "");
                                if (mselectedpos.length() == mselectedpos.replace(replace, "").length()) {
                                    String replace1 = String.valueOf(map_num) + " ";
                                    mselectedpos = mselectedpos.replace(replace1, "");
                                }
                            } else {
                                refreshRecycler();
                                resetView();
                            }
                        }
                        locError("mselectedpos: " + mselectedpos);
                    } else {
                        if (isFileExist(map.getM_uri()) || map.getM_name().equals("图志简介")) {
                            Log.w(TAG, "onItemClick: " + map.getM_name());
                            Intent intent = new Intent(select_page.this, MainInterface.class);
                            if (!map.getM_name().equals("图志简介")) {
                                intent.putExtra("num", map.getM_num());
                            } else {
                                intent.putExtra("num", -1);
                            }
                            select_page.this.startActivity(intent);
                        }
                    }
                }
            });
        }
        else {
            adapter.setOnItemClickListener(new Map_testAdapter.OnRecyclerItemClickListener() {
                @Override
                public void onItemClick(View view, String map_name, int map_num, int position) {
                    // TODO Gross
                    switch (map_name){
                        case "序图组":
                            mapCollectionType = 1;
                            break;
                        case "资源与环境图组":
                            mapCollectionType = 2;
                            break;
                        case "社会经济图组":
                            mapCollectionType = 3;
                            break;
                        case "区域地理图组":
                            mapCollectionType = 4;
                            break;
                        case "县图":
                            mapCollectionType = 5;
                            break;
                        case "各县城区图":
                            mapCollectionType = 6;
                            break;
                        case "各县影像图":
                            mapCollectionType = 7;
                            break;
                        case "乡镇图":
                            mapCollectionType = 8;
                            break;
                        case "临翔区":
                            mapCollectionType = 9;
                            break;
                        case "凤庆县":
                            mapCollectionType = 10;
                            break;
                        case "云县":
                            mapCollectionType = 11;
                            break;
                        case "永德县":
                            mapCollectionType = 12;
                            break;
                        case "镇康县":
                            mapCollectionType = 13;
                            break;
                        case "双江县":
                            mapCollectionType = 14;
                            break;
                        case "耿马县":
                            mapCollectionType = 15;
                            break;
                        case "沧源县":
                            mapCollectionType = 16;
                            break;
                    }
                    initMap();
                    refreshRecycler();
                }
            });
        }
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

    private boolean isFileExist(String filepath){
        File file = new File(filepath);
        return file.exists();
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
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
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

    public void deleteAllData(){
        map_testList.clear();
        boolean deleted = false;
        for (int j = 1; j <= num_pdf; ++j) {
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
                int MapType = pref1.getInt(str + "MapType", -1);
                if (!deleted){
                    Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong, MapType);
                    map_tests[j - 1] = mapTest;
                    map_testList.add(map_tests[j - 1]);
                }else {
                    Map_test mapTest = new Map_test(name, num - 1, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong, MapType);
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
        for (int j = 1; j <= num_pdf; ++j) {
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
        refreshRecycler();


    }

    public void deleteData(int selectedNum){
        map_testList.clear();
        boolean deleted = false;
        for (int j = 1; j <= num_pdf; ++j) {
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
                int MapType = pref1.getInt(str + "MapType", -1);
            if (!deleted){
                Map_test mapTest = new Map_test(name, num, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong, MapType);
                map_tests[j - 1] = mapTest;
                map_testList.add(map_tests[j - 1]);
            }else {
                Map_test mapTest = new Map_test(name, num - 1, GPTS, BBox, WKT, uri, imguri, MediaBox, CropBox, ic, center_latlong, MapType);
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
        for (int j = 1; j <= num_pdf; ++j) {
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
        refreshRecycler();


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

    public void saveGeoInfo(String name, String uri, String WKT, String BBox, String GPTS, String img_path, String MediaBox, String CropBox, String ic, int MapType){
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
        // TODO 修改地图存储函数
        editor1.putInt(str + "MapType", MapType);

        editor1.apply();
        initMapNext(num_pdf, name, WKT, uri, GPTS, BBox, img_path, MediaBox, CropBox, ic, center_latlong, MapType);
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

    private void initVariable(){
        setSimpleVariable();
        setNum_pdf();
    }

    private void setSimpleVariable(){
        selectedNum = 0;
        isLongClick = 1;
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
        for (int i = 1; i <= size; ++i){
            String str = "n_" + Integer.toString(i) + "_";
            if (pref2.getString(str + "name", "").equals(name)) {
                isSameName = true;
                break;
            }
        }
        if (isSameName) return false;
        else return true;
    }

    private void addMap(final String path){
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
            dialog.setPositiveButton("地理框架一", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    addMapForGeoType1ForPicker(path);

                }
            });
            dialog.setNegativeButton("地理框架二", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    addMapForGeoType2ForPicker(path);

                }
            });
            dialog.show();
        } else Toast.makeText(this, select_page.this.getResources().getText(R.string.AddedMapTip), Toast.LENGTH_SHORT).show();
    }

    int MapTypeNum = -1;
    private void GetMapType(){
        final String[] items = new String[] { "序图组", "资源与环境图组", "社会经济图组", "区域地理图组县图", "区域地理图组县城区图" , "区域地理图组县影像图" , "临翔区乡镇图" , "凤庆县乡镇图" , "云县乡镇图"
                , "永德乡镇图" , "镇康乡镇图" , "双江乡镇图" , "耿马乡镇图" , "沧源乡镇图" };
        AlertDialog.Builder dialog = new AlertDialog.Builder(select_page.this);
        dialog.setTitle("请选择当前地图类型");
        //dialog.setMessage("地图集");
        dialog.setCancelable(false);
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MapTypeNum = i+1;
            }
        });
        dialog.show();
    }

    private void addMapForGeoType1ForPicker(String path){
        GetMapType();
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true, MapTypeNum);
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true, MapTypeNum);
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

    private void addMapForGeoType2ForPicker(String path){
        GetMapType();
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false, MapTypeNum);
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false, MapTypeNum);
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
                            addMapType1();
                        }
                    });
                    dialog.setNegativeButton("类型二", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addMapType2();
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
                                    for (int i = 0; i < size1; ++i){
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
                                    for (int i = 0; i < size1; ++i){
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
                    addMap(path);
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
                                for (int i = 0; i < size1; ++i){
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

    private void addMapType2(){
        GetMapType();
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false, MapTypeNum);
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false, MapTypeNum);
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

    private void addMapType1(){
        GetMapType();
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true, MapTypeNum);
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
                        manageGeoInfo(configPath, URI_TYPE, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true, MapTypeNum);
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

    private String ParentNodeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_test_page);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //获取定位信息
        getLocation();
        //初始化界面一
        //doSpecificOperation();
        //initPage();

        mapCollectionType = 0;
        if (LitePal.findAll(ElectronicAtlasMap.class).size() != 0){
            InitElectronicAtlasData();
        }
        else
        {
            GetDataForElectronicAtlas();
            //InitElectronicAtlasData();
        }


        //fam菜单声明
        FloatingActionMenu floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam_selectpage);
        floatingActionsMenu.setVisibility(View.GONE);
        floatingActionsMenu.setClosedOnTouchOutside(true);

        /*FloatingActionButton floatingActionButton = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.BackFloatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ElectronicAtlasMap> mapList = LitePal.findAll(ElectronicAtlasMap.class);
                for (int i = 0; i < mapList.size(); i++) {
                    ElectronicAtlasMap map = mapList.get(i);
                    if (map.getName().equals(ParentNodeName)){
                        ParentNodeName = map.getParentNode();
                        mapCollectionType = map.getMapType();
                        InitElectronicAtlasData();
                        refreshRecyclerForElectronicAtlas();
                        break;
                    }
                }
            }
        });*/
        FloatingActionButton floatingActionButton = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.BackFloatingActionButton);
        floatingActionButton.setVisibility(View.GONE);

    }

    private void doSpecificOperation(){
        Log.w(TAG, "doSpecificOperation: " + 2.0e-6 * 100000000.1);
        //DataUtil.getSpatialIndex();
        //LitePal.deleteAll(Trail.class);
        //Log.w(TAG, "getExternalPolygon: " + lineUtil.getExternalPolygon("25,102 25.5,102.5 26.5,101.5", 1));
        /*Point point0 = new Point(1, 4);
        Point point1 = new Point(2, 3);
        Point point2 = new Point(4, 2);
        Point point3 = new Point(6, 6);
        Point point4 = new Point(7, 7);
        Point point5 = new Point(8, 6);
        Point point6 = new Point(9, 5);
        Point point7 = new Point(10, 10);
        List<Point> list = new ArrayList<>();
        list.add(point0);
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
        list.add(point5);
        list.add(point6);
        list.add(point7);
        Douglas douglas = new Douglas(list);
        List<Point> points = douglas.douglasPeucker();
        for (int i = 0; i < points.size(); ++i) {
            Log.w(TAG, "(" + points.get(i).getX() + ","
                    + points.get(i).getY() + ")");
        }*/
        /*String str = "asdf.jpg|asdfad.jpg";
        String str1 = "asdf.jpg";*/
        ///////获取地名数据
        /*LitePal.deleteAll(DMLine.class);
        LitePal.deleteAll(DMPoint.class);
        DataUtil.getDM("/20180716/联盟街道点状地名/doc.kml", "/20180716/联盟街道线状地名/doc.kml", "/20180716/地名信息连接关系.txt", "/20180716/地名信息.txt");*/
        /*String str1 = "\"hello world\"";*/
        ///////
        //nitIconBitmap(addIconDataset());
        LitePal.deleteAll(IconDataset.class);
        addIconDataset();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d("TAG", "Max memory is " + maxMemory + "KB");

        String str = "2345";
        switch (str.substring(0,2)){
            case "k1":
                Log.w(TAG, "doSpecificOperation: " + "k1");
                break;
            case "23":
                Log.w(TAG, "doSpecificOperation: " + "23");
                break;
            default:
                Log.w(TAG, "doSpecificOperation: " + "qq");
                break;
        }

    }

    private List<IconDataset> addIconDataset(){
        List<IconDataset> iconDatasets1 = LitePal.findAll(IconDataset.class);
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/原图");
        if (file.isDirectory() && file.list().length > 0) {
            if (iconDatasets1.size() == 0) {
                File[] subFiles = file.listFiles();
                for (int i = 0; i < subFiles.length; ++i) {
                    String name = subFiles[i].getName();
                    IconDataset iconDataset = new IconDataset();
                    iconDataset.setName(name.substring(0, name.lastIndexOf(".")));
                    iconDataset.setPath(subFiles[i].getAbsolutePath());
                    iconDataset.save();
                }
                List<IconDataset> iconDatasets = LitePal.findAll(IconDataset.class);
                /*for (int i = 0; i < iconDatasets.size(); ++i) {
                    Log.w(TAG, "addIconDataset: " + iconDatasets.get(i).getName());
                }*/
                return iconDatasets;
            }else {
                File[] subFiles = file.listFiles();
                for (int i = 0; i < subFiles.length; ++i) {
                    String name = subFiles[i].getName();
                    if (LitePal.where("name = ?", name).find(IconDataset.class).size() == 0) {
                        IconDataset iconDataset = new IconDataset();
                        iconDataset.setName(name.substring(0, name.lastIndexOf(".")));
                        iconDataset.setPath(subFiles[i].getAbsolutePath());
                        iconDataset.save();
                    }
                }
                List<IconDataset> iconDatasets = LitePal.findAll(IconDataset.class);
                return iconDatasets;
            }
        }else return iconDatasets1;
    }

    List<bt> IconBitmaps;
    private void initIconBitmap(final List<IconDataset> iconDatasets){
        new Thread(new Runnable() {
            @Override
            public void run() {
                IconBitmaps = new ArrayList<>();
                for (int i = 0; i < iconDatasets.size(); ++i){
                    String path = iconDatasets.get(i).getPath();
                    Bitmap bitmap = DataUtil.getImageThumbnail(path, 80, 80);
                    IconBitmaps.add(new bt(bitmap, path));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(select_page.this, "done", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void initWidget(){
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //fam菜单声明
        FloatingActionMenu floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam_selectpage);
        floatingActionsMenu.setClosedOnTouchOutside(true);
        //locError("deviceId : " + getIMEI());
        //addMap_selectpage按钮事件编辑

        //添加地图按钮声明
        com.github.clans.fab.FloatingActionButton addbt = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addMap_selectpage);
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
        //导入数据按钮声明
        com.github.clans.fab.FloatingActionButton inputbt = (FloatingActionButton) findViewById(R.id.inputData_selectpage);
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

        //导出数据按钮声明
        com.github.clans.fab.FloatingActionButton outputbt = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.outputData_selectpage);
        outputbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<POI> pois = LitePal.findAll(POI.class);
                            List<String> types = new ArrayList<>();
                            Log.w(TAG, "runlzy: " + pois.size());
                            for (int i = 0; i < pois.size(); ++i){
                                String temp = pois.get(i).getType();
                                Log.w(TAG, "runlzy: " + temp);
                                if (temp != null) {
                                    if (!temp.isEmpty()) {
                                        if (types.size() > 0) {
                                            for (int j = 0; j < types.size(); ++j) {
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
                                for (int i = 0; i < types.size(); ++i) {
                                    DataUtil.makeTxt(types.get(i));
                                }
                            }else DataUtil.makeTxt("");
                            DataUtil.makeTxt1();
                            DataUtil.makeWhiteBlankKML();
                            List<File> files = new ArrayList<File>();
                            StringBuffer sb = new StringBuffer();
                            int size_POI = pois.size();
                            sb = sb.append("<POI>").append("\n");
                            for (int i = 0; i < size_POI; ++i){
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
                            for (int i = 0; i < size_trail; ++i){
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
                            for (int i = 0; i < size_mphoto; ++i){
                                sb.append("<id>").append(mphotos.get(i).getId()).append("</id>").append("\n");
                                sb.append("<pdfic>").append(mphotos.get(i).getPdfic()).append("</pdfic>").append("\n");
                                sb.append("<POIC>").append(mphotos.get(i).getPoic()).append("</POIC>").append("\n");
                                String path = mphotos.get(i).getPath();
                                sb.append("<path>").append(path).append("</path>").append("\n");
                                files.add(new File(path));
                                sb.append("<time>").append(mphotos.get(i).getTime()).append("</time>").append("\n");
                            }
                            sb.append("</MPHOTO>").append("\n");

                            List<MVEDIO> mvedios = LitePal.findAll(MVEDIO.class);
                            int size_mvideo = mvedios.size();
                            sb = sb.append("<MVIDEO>").append("\n");
                            for (int i = 0; i < size_mvideo; ++i){
                                sb.append("<id>").append(mvedios.get(i).getId()).append("</id>").append("\n");
                                sb.append("<pdfic>").append(mvedios.get(i).getPdfic()).append("</pdfic>").append("\n");
                                sb.append("<POIC>").append(mvedios.get(i).getPoic()).append("</POIC>").append("\n");
                                String path1 = mvedios.get(i).getThumbnailImg();
                                sb.append("<Thumbnailpath>").append(path1).append("</Thumbnailpath>").append("\n");
                                try {
                                    files.add(new File(path1));
                                }
                                catch (Exception e){

                                }
                                String path = mvedios.get(i).getPath();
                                sb.append("<path>").append(path).append("</path>").append("\n");
                                try {
                                    files.add(new File(path));
                                }
                                catch (Exception e){

                                }
                                sb.append("<time>").append(mvedios.get(i).getTime()).append("</time>").append("\n");
                            }
                            sb.append("</MVIDEO>").append("\n");

                            List<MTAPE> mtapes = LitePal.findAll(MTAPE.class);
                            int size_mtape = mtapes.size();
                            sb = sb.append("<MTAPE>").append("\n");
                            for (int i = 0; i < size_mtape; ++i){
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
                            for (int i = 0; i < size_lines_whiteBlank; ++i){
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
                                for (int i = 0; i < size; ++i){
                                    Log.w(TAG, "run: " + i);
                                    Log.w(TAG, "run: " + files.get(i).getPath());
                                    boolean isOK = false;
                                    for (int k = 0; k < i; ++k) {
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
                }catch (Exception e)
                {
                    Log.w(TAG, "error: " + e.toString());
                }
                setFAMVisible(false);
            }
        });
    }

    private void OutputData(){
        List<POI> pois = LitePal.findAll(POI.class);
        List<String> types = new ArrayList<>();
        Log.w(TAG, "runlzy: " + pois.size());
        for (int i = 0; i < pois.size(); ++i){
            String temp = pois.get(i).getType();
            Log.w(TAG, "runlzy: " + temp);
            if (temp != null) {
                if (!temp.isEmpty()) {
                    if (types.size() > 0) {
                        for (int j = 0; j < types.size(); ++j) {
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
            for (int i = 0; i < types.size(); ++i) {
                DataUtil.makeTxt(types.get(i));
            }
        }else DataUtil.makeTxt("");
        DataUtil.makeTxt1();
        DataUtil.makeWhiteBlankKML();
        List<File> files = new ArrayList<File>();
        StringBuffer sb = new StringBuffer();
        int size_POI = pois.size();
        sb = sb.append("<POI>").append("\n");
        for (int i = 0; i < size_POI; ++i){
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
        for (int i = 0; i < size_trail; ++i){
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
        for (int i = 0; i < size_mphoto; ++i){
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
        for (int i = 0; i < size_mtape; ++i){
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
        for (int i = 0; i < size_lines_whiteBlank; ++i){
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
            for (int i = 0; i < size; ++i){
                Log.w(TAG, "run: " + i);
                Log.w(TAG, "run: " + files.get(i).getPath());
                boolean isOK = false;
                for (int k = 0; k < i; ++k) {
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

    @Override
    protected void onResume() {
        super.onResume();

        refreshRecyclerForElectronicAtlas();

        /*refreshRecycler();

        SharedPreferences prf1 = getSharedPreferences("simpledata", MODE_PRIVATE);
        String filepath = prf1.getString("path", "");
        if (!filepath.isEmpty()){
            addMap(filepath);
            SharedPreferences.Editor editor = getSharedPreferences("simpledata", MODE_PRIVATE).edit();
            editor.putString("path", "");
            editor.apply();
        }*/
        /*Intent intent = select_page.this.getIntent();
        String path = intent.getStringExtra("test22");
        Log.w(TAG, "onResume: " + path);
        if (path != null && !path.isEmpty()){

        }*/
    }

    private void GetDataForElectronicAtlas(){
        LitePal.deleteAll(ElectronicAtlasMap.class);
        BatchAddMapsForAndroid();
        Log.w(TAG, "GetDataForElectronicAtlas: " + LitePal.findAll(ElectronicAtlasMap.class).size());
    }

    private void InitElectronicAtlasData(){
        Log.w(TAG, "InitElectronicAtlasData: " + mapCollectionType);
        map_testList = new ArrayList<>();
        List<ElectronicAtlasMap> maps = LitePal.findAll(ElectronicAtlasMap.class);
        for (int i = 0; i < maps.size(); i++) {
            ElectronicAtlasMap map = maps.get(i);
            if (map.getMapType() == mapCollectionType) {
                ParentNodeName = map.getParentNode();
                if (map.getMapGeoStr() == null) {
                    map_testList.add(new Map_test(map.getName(), i, "", "", "", "", "", "", "", map.getName(), "", map.getMapType()));
                } else {
                    String[] MapGeoStrs = map.getMapGeoStr().split(",");
                    //map_testList.add(new Map_test(map.getName(), i, strings[4], strings[6], "", strings[3], bmPath, strings[5], strings[5], map.getName(), GetCenterLatAndLong(GetMapGeoInfo(strings[4], strings[5], strings[6], strings[7])), Integer.parseInt(strings[2])));
                    map_testList.add(new Map_test(map.getName(), i, MapGeoStrs[0], MapGeoStrs[2], "", map.getPath(), map.getImgPath(), MapGeoStrs[1], MapGeoStrs[1], map.getName(), "", map.getMapType()));
                }
            }
        }
    }

    public void initPage(){
        initWidget();
        initVariable();
        //获取卡片数目
        if (num_pdf == 0) {
            initDemo();
        }else
            initMap();
        //Log.w(TAG, Integer.toString(num_pdf) );
        //初始化
    }

    private void BatchAddMapsForAndroid(){
        try {
            ParentNodeName = "";
            String DataFilePath = Environment.getExternalStorageDirectory().toString() + "/" + "临沧市地图集安卓/dataForAndroid.txt";
            String Data = DataUtil.readtxt(DataFilePath);
            String[] mData = Data.split("\n");
            for (int i = 0; i < mData.length; i++) {
                Log.w(TAG, "BatchAddMapsForAndroid: " + i);
                String line = mData[i];
                String[] strings = line.split(",");
                if (strings.length <= 4) {
                    ElectronicAtlasMap map = new ElectronicAtlasMap(strings[0], strings[1], Integer.parseInt(strings[2]), Environment.getExternalStorageDirectory().toString() + "/" + strings[3].replace("\\", "/"), "", null);
                    map.save();
                    if (map.getMapType() == 0)
                        map_testList.add(new Map_test(map.getName(), i, "", "", "", "", "", "", "", map.getName(), "", map.getMapType()));
                } else {
                    //PointF[] pts = GetMapGeoInfo(strings[4], strings[5], strings[6], strings[7]);
                    String bmPath = DataUtil.getDtThumbnail(strings[1], "/TuZhi" + "/Thumbnails",  Environment.getExternalStorageDirectory().toString() + "/" + strings[3].replace("\\", "/"), 120, 180, 30,  select_page.this);
                    Log.w(TAG, "BatchAddMapsForAndroid: " + bmPath);
                    String[] MapGeoStrs = (strings[4] + "," + strings[5] + "," + strings[6] + "," + strings[7]).split(",");
                    String GeoInfo = DataUtil.getGPTS(MapGeoStrs[0], MapGeoStrs[3]);
                    GeoInfo = DataUtil.rubberCoordinate(MapGeoStrs[1], MapGeoStrs[2], GeoInfo);
                    ElectronicAtlasMap map = new ElectronicAtlasMap(strings[0], strings[1], Integer.parseInt(strings[2]), Environment.getExternalStorageDirectory().toString() + "/" + strings[3].replace("\\", "/"), bmPath, GeoInfo + "," + strings[5] + "," + strings[6] + "," + strings[7]);
                    map.save();
                    //map_testList.add(new Map_test(map.getName(), i, strings[4], strings[6], "", strings[3], bmPath, strings[5], strings[5], map.getName(), GetCenterLatAndLong(GetMapGeoInfo(strings[4], strings[5], strings[6], strings[7])), Integer.parseInt(strings[2])));

                    if (map.getMapType() == 0)
                        map_testList.add(new Map_test(map.getName(), i, GeoInfo, MapGeoStrs[2], "", map.getPath(), map.getImgPath(), MapGeoStrs[1], MapGeoStrs[1], map.getName(), "", map.getMapType()));
                }
            }
        }
        catch (Exception e){
            Log.w(TAG, "BatchAddMapsForAndroid: " + e.toString());
        }
    }

    private String GetCenterLatAndLong(PointF[] pts){
        float max_lat = 0;
        float max_long = 0;
        float min_lat = Float.MAX_VALUE;
        float min_long = Float.MAX_VALUE;
        for (int i = 0; i < pts.length; i++) {
            float lat = pts[i].getLat();
            float longi = pts[i].getLong();
            if (lat > max_lat)
                max_lat = lat;
            if (longi > max_long)
                max_long = longi;
            if (lat < min_lat)
                min_lat = lat;
            if (longi < min_long)
                min_long = longi;
        }
        return (max_lat+min_lat)/2.0 + "," + (max_long+min_long)/2.0;
    }

    private static PointF[] GetMapGeoInfo(String GeoInfo, String MediaBoxInfo, String BBoxInfo, String LptsInfo){
        String MapGeoInfo = "";
        System.out.println("原始坐标： " + GeoInfo);
        MapGeoInfo = DataUtil.getGPTS(GeoInfo, LptsInfo);
        System.out.println("坐标纠偏后： " + MapGeoInfo);
        MapGeoInfo = DataUtil.rubberCoordinate(MediaBoxInfo, BBoxInfo, MapGeoInfo);
        String[] MapGeoInfos = MapGeoInfo.split(" ");
        PointF[] pts = new PointF[4];
        for (int i = 0; i < MapGeoInfos.length; i+=2) {
            pts[i/2] = new PointF(Float.valueOf(MapGeoInfos[i]), Float.valueOf(MapGeoInfos[i+1]));
        }
        return pts;
    }

    private void BatchAddMap(){
        // TODO 批量添加地图
        AddFirstView();
        AddXTZ();
        AddZYHJTZ();
        AddSHJJTZ();
        AddQYDLView();
        AddXTTZ();
        AddXCQTZ();
        AddXYXTZ();
        AddXZTView();
        AddLXQXZTZ();
        AddFQXXZTZ();
        AddYXXZTZ();
        AddYDXXZTZ();
        AddZKXXZTZ();
        AddSJXXZTZ();
        AddGMXXZTZ();
        AddCYXXZTZ();
    }

    //临沧市地图集的首界面
    private void AddFirstView(){
        saveGeoInfo("序图组", "", "", "", "", "", "", "", "序图组", 0);
        saveGeoInfo("资源与环境图组", "", "", "", "", "", "", "", "资源与环境图组", 0);
        saveGeoInfo("社会经济图组", "", "", "", "", "", "", "", "社会经济图组", 0);
        saveGeoInfo("区域地理图组", "", "", "", "", "", "", "", "区域地理图组", 0);
    }

    private void AddXTZ(){

    }

    private void AddZYHJTZ(){

    }

    private void AddSHJJTZ(){

    }

    //区域地理图组的首界面
    private void AddQYDLView(){
        saveGeoInfo("县图", "", "", "", "", "", "", "", "县图", 4);
        saveGeoInfo("各县城区图", "", "", "", "", "", "", "", "各县城区图", 4);
        saveGeoInfo("各县影像图", "", "", "", "", "", "", "", "各县影像图", 4);
        saveGeoInfo("乡镇图", "", "", "", "", "", "", "", "乡镇图", 4);
    }

    private void AddXTTZ(){
        Log.w(TAG, "AddXTTZ: " + Environment.getExternalStorageDirectory().toString() + "/" + "临沧市地图集安卓/区域地理图组/县图/凤庆县（4k）.dt");
        saveGeoInfo("凤庆县（4k）", Environment.getExternalStorageDirectory().toString() + "/" + "临沧市地图集安卓/区域地理图组/县图/凤庆县（4k）.dt", "", "0.0 0.0 1074.2871 1502.1162", "24.521004 99.79573 24.744478 99.97252 24.745514 99.79716 24.519978 99.97078", "", "0.0 0.0 1074.29 1502.12", "0.0 0.0 1074.29 1502.12", "凤庆县（4k）", 5);
    }

    private void AddXCQTZ(){

    }

    private void AddXYXTZ(){

    }

    //乡镇图组的首界面
    private void AddXZTView(){
        saveGeoInfo("临翔区", "", "", "", "", "", "", "", "临翔区", 8);
        saveGeoInfo("凤庆县", "", "", "", "", "", "", "", "凤庆县", 8);
        saveGeoInfo("云县", "", "", "", "", "", "", "", "云县", 8);
        saveGeoInfo("永德县", "", "", "", "", "", "", "", "永德县", 8);
        saveGeoInfo("镇康县", "", "", "", "", "", "", "", "镇康县", 8);
        saveGeoInfo("双江县", "", "", "", "", "", "", "", "双江县", 8);
        saveGeoInfo("耿马县", "", "", "", "", "", "", "", "耿马县", 8);
        saveGeoInfo("沧源县", "", "", "", "", "", "", "", "沧源县", 8);
    }

    private void AddLXQXZTZ(){

    }

    private void AddFQXXZTZ(){

    }

    private void AddYXXZTZ(){

    }

    private void AddYDXXZTZ(){

    }

    private void AddZKXXZTZ(){

    }

    private void AddSJXXZTZ(){

    }

    private void AddGMXXZTZ(){

    }

    private void AddCYXXZTZ(){

    }

    public void initDemo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                add_max ++;
                manageGeoInfo("", SAMPLE_TYPE, "", DataUtil.findNamefromSample(EnumClass.SAMPLE_FILE), true, 0);
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
                in = getAssets().open(EnumClass.SAMPLE_FILE);
            }
            else {
                in = new FileInputStream(file);
                //TODO 内存泄漏检测
                bmPath = DataUtil.getDtThumbnail(name, "/TuZhi" + "/Thumbnails",  filePath, 120, 180, 30,  select_page.this);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            int m_num_GPTS = 0;
            //line = bufferedReader.readLine()
            while((line = bufferedReader.readLine()) != null) {
                //sb.append(line + "/n");
                if(line.contains("PROJCS") || line.contains("GEOGCS")) {
                    //locError(line);
                    if (line.contains("PROJCS"))
                        m_WKT = line.substring(line.indexOf("PROJCS["), line.indexOf(")>>"));
                    else if (line.contains("GEOGCS"))
                        m_WKT = line.substring(line.indexOf("GEOGCS["), line.indexOf(")>>"));
                    locError("wkt信息： " + m_WKT);
                        //GEOGCS["GCS_China_Geodetic_Coordinate_System_2000",DATUM["D_China_2000",SPHEROID["CGCS2000",6378137.0,298.257222101]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]]
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
            if (!m_WKT.isEmpty()) {
                m_GPTS = DataUtil.rubberCoordinate(m_MediaBox, m_BBox, m_GPTS);
                //saveGeoInfo(m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name);
                String[] strings = new String[]{m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name};
                return strings;
            }else {
                SharedPreferences.Editor editor = getSharedPreferences("simpledata", MODE_PRIVATE).edit();
                editor.putString("path", "");
                editor.apply();
                renamePath1(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), select_page.this.getResources().getText(R.string.GetGeoInfoError).toString() + R.string.QLXWM, Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        } else {
            //m_GPTS = DataUtil.rubberCoordinate(m_MediaBox, m_BBox, m_GPTS);
            //saveGeoInfo("Demo", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name);
            String[] strings = new String[]{"图志简介", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name};
            return strings;
        }
    }

    private void manageGeoInfo(String filePath, int Type, String uri, String name, boolean type, int MapType){
        if (!filePath.isEmpty()) {
            String[] strings = getGeoInfo(filePath, Type, uri, name, type);
            if (strings != null)
                saveGeoInfo(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8], MapType);
        }else {
            try {
                String[] strings = new String[]{"图志简介", "", "", "", "", getAssets().open("image/图志简介1.jpg").toString(), "", "", "图志简介"};
                saveGeoInfo(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8], 0);
            }catch (Exception e){

            }
        }
    }



}
