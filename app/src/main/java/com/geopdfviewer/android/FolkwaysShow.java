package com.geopdfviewer.android;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.table.TableData;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolkwaysShow extends AppCompatActivity {

    private static final String TAG = "民族信仰地图集展示页面";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkways_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.festival_toolbar);
        toolbar.setTitle("民族信仰地图集展示页面");
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String DZQBM = intent.getStringExtra("DZQBM");


        //AddData();
        /*SmartTable table1 = findViewById(R.id.StandardInfoTable);
        List<User> list = new ArrayList<>();
        list.add(new User("张三", 25, 19950822, "无", "六一班"));
        list.add(new User("李三", 21, 19950822, "无", "六二班"));
        Column<String> column1 = new Column<>("姓名", "name");
        Column<Integer> column2 = new Column<>("年龄", "age");
        Column<Long> column3 = new Column<>("更新时间", "time");
        Column<String> column4 = new Column<>("头像", "portrait");
        //如果是多层，可以通过.来实现多级查询
        Column<String> column5 = new Column<>("班级", "className");
        //组合列
        Column totalColumn1 = new Column("组合列名",column1,column2);
        //表格数据 datas是需要填充的数据
        final TableData<User> tableData = new TableData<>("表格名",list,totalColumn1,column3);
        //设置数据
        table1.setTableData(tableData);

        SmartTable table2 = findViewById(R.id.FestivalTable);
        final TableData<User> tableData1 = new TableData<>("表格名1",list,column4,column5);
        //设置数据
        table2.setTableData(tableData1);*/
        RefreshPage(DZQBM);
    }

    private void RefreshPage(String XZQDM){

        List<Folkway_StandardInfo> list = LitePal.findAll(Folkway_StandardInfo.class);
        for (int i = 0; i < list.size(); i++) {
            Folkway_StandardInfo folkway_standardInfo = list.get(i);
            if (folkway_standardInfo.getObjectID().equals(XZQDM)){
                TextView districtTextView = findViewById(R.id.txt_DistrictName);
                districtTextView.setText(folkway_standardInfo.getDistrictName());
                TextView txt_TownName = findViewById(R.id.txt_TownName);
                txt_TownName.setText(folkway_standardInfo.getTownName());
                TextView ViliageTextView = findViewById(R.id.txt_Viliage);
                ViliageTextView.setText(folkway_standardInfo.getVillageName());
                TextView txt_Nation = findViewById(R.id.txt_Nation);
                txt_Nation.setText(folkway_standardInfo.getNation());
                TextView txt_NationHouseHold = findViewById(R.id.txt_NationHouseHold);
                txt_NationHouseHold.setText(folkway_standardInfo.getNationHouseHold() + "户");
                TextView txt_NationNumber = findViewById(R.id.txt_NationNumber);
                txt_NationNumber.setText(folkway_standardInfo.getNationNumber() + "人");
                TextView txt_TotalHouseHold = findViewById(R.id.txt_TotalHouseHold);
                txt_TotalHouseHold.setText(folkway_standardInfo.getTotalHouseHold() + "户");
                TextView txt_TotalNumber = findViewById(R.id.txt_TotalNumber);
                txt_TotalNumber.setText(folkway_standardInfo.getTotalNumber() + "人");
                TextView txt_Thought = findViewById(R.id.txt_Thought);
                txt_Thought.setText(folkway_standardInfo.getThought());
                refreshRecyclerForFestival(folkway_standardInfo);
                refreshRecyclerForCeremony(folkway_standardInfo);
                refreshRecyclerForMaster(folkway_standardInfo);
                refreshRecyclerForObject(folkway_standardInfo);
                break;
            }
        }


    }

    public static String readtxt(String path) {
        //按行读取，不能保留换行等格式，所以需要手动添加每行换行符。
        //String result = "";
        StringBuffer txtContent = new StringBuffer();
        File file = new File(path);
        try {
            int len = 0;
            FileInputStream in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in, "utf-8");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
            while ((s = br.readLine()) != null) {
                if (len != 0) {// 处理换行符的问题，第一行不换行
                    txtContent.append(new String(("\r\n" + s).getBytes(), "utf-8"));
                } else {
                    txtContent.append(new String(s.getBytes(), "utf-8"));
                }
                len++;
            }
            reader.close();
            in.close();
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txtContent.toString();
    }

    private void refreshRecyclerForFestival(Folkway_StandardInfo folkway_standardInfo){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.festival_recycler_view);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwaysShow.this,3);
        recyclerView.setLayoutManager(layoutManager);
        List<String> keyAndValues = new ArrayList<>();
        String Festival = folkway_standardInfo.getFestival();
        Festival = Festival.replace("|", "&");
        String[] Festivals = Festival.split("&");
        Log.w(TAG, "refreshRecyclerForFestival: " + Festivals.length);
        for (int i = 0; i < Festivals.length; i++) {
            Log.w(TAG, "refreshRecyclerForFestival: " + Festivals[i]);
            List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
            if (folkway_festivals.size() > 0)
                keyAndValues.add(folkway_festivals.get(0).getName());
        }
        FestivalAdapter festivalAdapter = new FestivalAdapter(keyAndValues);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {

            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwaysShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshRecyclerForCeremony(Folkway_StandardInfo folkway_standardInfo){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.ceremony_recycler_view);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwaysShow.this,3);
        recyclerView.setLayoutManager(layoutManager);
        List<String> keyAndValues = new ArrayList<>();

        String ceremony = folkway_standardInfo.getCeremony();

        ceremony = ceremony.replace("|", "&");
        String[] ceremonys = ceremony.split("&");
        for (int i = 0; i < ceremonys.length; i++) {
            Log.w(TAG, "refreshRecyclerForCeremony: " + ceremonys[i]);
            List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremonys[i]).find(Folkway_Ceremony.class);
            if (folkway_ceremonies.size() > 0)
                keyAndValues.add(folkway_ceremonies.get(0).getName());
        }

        FestivalAdapter festivalAdapter = new FestivalAdapter(keyAndValues);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {

            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwaysShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetMastersForStandardInfo(Folkway_StandardInfo folkway_standardInfo){
        List<String> keyAndValues = new ArrayList<>();
        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();

        String Festival = folkway_standardInfo.getFestival();
        Festival = Festival.replace("|", "&");
        String[] Festivals = Festival.split("&");
        for (int i = 0; i < Festivals.length; i++) {
            Log.w(TAG, "GetMastersForStandardInfo: 有节日" + Festivals[i]);
            List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
            if (folkway_festivals.size() > 0){
                String process = folkway_festivals.get(0).getProcedure();
                Log.w(TAG, "GetMastersForStandardInfo: " + process);
                process = process.replace("|", "&");
                Log.w(TAG, "GetMastersForStandardInfo: " + process);
                String[] days = process.split("&");
                for (int j = 0; j < days.length; j++) {
                    String[] oneday = days[j].split(",");
                    for (int k = 0; k < oneday.length; k++) {
                        String activity = oneday[k];
                        if (activity.contains("C")) {
                            Log.w(TAG, "GetMastersForStandardInfo: 有节日：" + Festivals[i] + ", 有仪式：" + activity);
                            List<Folkway_Ceremony> list = LitePal.where("objectID = ?", activity).find(Folkway_Ceremony.class);
                            if (list.size()>0) {
                                Folkway_Ceremony folkway_ceremony = list.get(0);
                                String Master = folkway_ceremony.getMaster();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Master> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Master.class);
                                    if (folkway_masters.size() > 0){
                                        Folkway_Master folkway_master = folkway_masters.get(0);
                                        ceremonyHashMap.put(folkway_master.getIdentity(), folkway_ceremony);
                                        keyAndValues.add(folkway_master.getIdentity());
                                    }
                                }
                            }
                        } else if (activity.contains("A")) {
                            Log.w(TAG, "GetMastersForStandardInfo: 有节日：" + Festivals[i] + ", 有其他活动：" + activity);
                            List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                            if (list.size() > 0) {
                                Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                String Master = Folkway_OtherActivity.getMaster();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Master> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Master.class);
                                    if (folkway_masters.size() > 0){
                                        Folkway_Master folkway_master = folkway_masters.get(0);
                                        OtheractivityHashMap.put(folkway_master.getIdentity(), Folkway_OtherActivity);
                                        keyAndValues.add(folkway_master.getIdentity());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        String ceremony = folkway_standardInfo.getCeremony();
        ceremony = ceremony.replace("|", "&");
        String[] ceremonys = ceremony.split("&");
        for (int i = 0; i < ceremonys.length; i++) {
            Log.w(TAG, "GetMastersForStandardInfo: 有仪式：" + ceremonys[i]);
            List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremonys[i]).find(Folkway_Ceremony.class);
            if (folkway_ceremonies.size() > 0) {
                Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                String Master = folkway_ceremony.getMaster();
                Master = Master.replace("|", "&");
                String[] masters = Master.split("&");
                for (int l = 0; l < masters.length; l++) {
                    List<Folkway_Master> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Master.class);
                    if (folkway_masters.size() > 0){
                        Folkway_Master folkway_master = folkway_masters.get(0);
                        ceremonyHashMap.put(folkway_master.getIdentity(), folkway_ceremony);
                        keyAndValues.add(folkway_master.getIdentity());
                    }
                }
            }
        }

        HashMap<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < keyAndValues.size(); i++) {
            if (hashMap.containsKey(keyAndValues.get(i)))
                keyAndValues.remove(i--);
            else
                hashMap.put(keyAndValues.get(i), "");
        }
        return keyAndValues;
    }

    private List<String> GetObjectsForStandardInfo(Folkway_StandardInfo folkway_standardInfo){
        List<String> keyAndValues = new ArrayList<>();

        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();
        String Festival = folkway_standardInfo.getFestival();
        Festival = Festival.replace("|", "&");
        String[] Festivals = Festival.split("&");
        for (int i = 0; i < Festivals.length; i++) {
            List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
            if (folkway_festivals.size()>0) {
                String process = folkway_festivals.get(0).getProcedure();
                process = process.replace("|", "&");
                String[] days = process.split("&");
                for (int j = 0; j < days.length; j++) {
                    String[] oneday = days[j].split(",");
                    for (int k = 0; k < oneday.length; k++) {
                        String activity = oneday[k];
                        if (activity.contains("C")) {
                            List<Folkway_Ceremony> list = LitePal.where("objectID = ?", activity).find(Folkway_Ceremony.class);
                            if (list.size() > 0) {
                                Folkway_Ceremony folkway_ceremony = list.get(0);
                                String Master = folkway_ceremony.getObject();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Object> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Object.class);
                                    if (folkway_masters.size() > 0){
                                        Folkway_Object folkway_master = folkway_masters.get(0);
                                        ceremonyHashMap.put(folkway_master.getName(), folkway_ceremony);
                                        keyAndValues.add(folkway_master.getName());
                                    }
                                }
                            }
                        } else if (activity.contains("A")) {
                            List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                            if (list.size()>0) {
                                Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                String Master = Folkway_OtherActivity.getObject();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Object> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Object.class);
                                    if (folkway_masters.size() > 0){
                                        Folkway_Object folkway_master = folkway_masters.get(0);
                                        OtheractivityHashMap.put(folkway_master.getName(), Folkway_OtherActivity);
                                        keyAndValues.add(folkway_master.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        String ceremony = folkway_standardInfo.getCeremony();
        ceremony = ceremony.replace("|", "&");
        String[] ceremonys = ceremony.split("&");
        for (int i = 0; i < ceremonys.length; i++) {
            List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremonys[i]).find(Folkway_Ceremony.class);
            if (folkway_ceremonies.size()>0) {
                Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                String Master = folkway_ceremony.getObject();
                Master = Master.replace("|", "&");
                String[] masters = Master.split("&");
                for (int l = 0; l < masters.length; l++) {
                    List<Folkway_Object> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Object.class);
                    if (folkway_masters.size() > 0){
                        Folkway_Object folkway_master = folkway_masters.get(0);
                        ceremonyHashMap.put(folkway_master.getName(), folkway_ceremony);
                        keyAndValues.add(folkway_master.getName());
                    }
                }
            }
        }
        HashMap<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < keyAndValues.size(); i++) {
            if (hashMap.containsKey(keyAndValues.get(i)))
                keyAndValues.remove(i--);
            else
                hashMap.put(keyAndValues.get(i), "");
        }
        return keyAndValues;
    }

    private void refreshRecyclerForMaster(Folkway_StandardInfo folkway_standardInfo){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.master_recycler_view);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwaysShow.this,3);
        recyclerView.setLayoutManager(layoutManager);
        List<String> masters = GetMastersForStandardInfo(folkway_standardInfo);
        FestivalAdapter festivalAdapter = new FestivalAdapter(masters);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {

            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwaysShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshRecyclerForObject(Folkway_StandardInfo folkway_standardInfo){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.object_recycler_view);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwaysShow.this,3);
        recyclerView.setLayoutManager(layoutManager);
        List<String> Objects = GetObjectsForStandardInfo(folkway_standardInfo);
        FestivalAdapter festivalAdapter = new FestivalAdapter(Objects);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {

            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwaysShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filemanagemenu, menu);
        menu.findItem(R.id.back_filemanage).setVisible(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_filemanage:
                this.finish();
                break;
            default:
        }
        return true;
    }
}
