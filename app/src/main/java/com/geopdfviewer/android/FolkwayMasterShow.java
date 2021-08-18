package com.geopdfviewer.android;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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

import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

public class FolkwayMasterShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkway_master_show);
        ActivityCollector.addActivity(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.folkway_master_toolbar);
        toolbar.setTitle("主持人");
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        String oid = intent.getStringExtra("objectid");
        //Toast.makeText(FolkwayMasterShow.this, oid, Toast.LENGTH_SHORT).show();

        Folkway_Master folkway_master = LitePal.where("objectid = ?", oid).find(Folkway_Master.class).get(0);
        TextView masterIdentity = findViewById(R.id.txt_master_identity);
        masterIdentity.setText(folkway_master.getIdentity());

        TextView masterName = findViewById(R.id.txt_master_name);
        masterName.setText(folkway_master.getName());

        TextView masterLevel = findViewById(R.id.txt_master_level);
        masterLevel.setText(folkway_master.getLevel());

        TextView masterUsage = findViewById(R.id.txt_master_usage);
        masterUsage.setText(folkway_master.getDuty());

        TextView masterInherit = findViewById(R.id.txt_master_pass);
        masterInherit.setText(folkway_master.getInherited());

        MakeStoryPart(folkway_master);
        MakeViligePart(folkway_master);
        MakeCeremonyPart(folkway_master);
        MakeFestivalPart(folkway_master);

        TextView masterOtherinfo = findViewById(R.id.txt_master_otherinfo);
        masterOtherinfo.setText(folkway_master.getOtherinfo());
    }

    private void MakeStoryPart(Folkway_Master master){
        TextView festivalstory = findViewById(R.id.txt_master_story);
        String storys = "";
        String[] stories = master.getStory().replace("|", "&").split("&");
        //String[] stories = "S4|S6|S7|S8".replace("|", "&").split("&");
        for (int i = 0; i < stories.length; i++) {
            List<Folkway_Story> folkway_stories = LitePal.where("objectid = ?", stories[i]).find(Folkway_Story.class);
            if (folkway_stories.size() > 0) {
                Folkway_Story folkway_story = folkway_stories.get(0);
                if (i < stories.length - 1)
                    storys += (i + 1) + ": " + folkway_story.getName() + "\n";
                else
                    storys += (i + 1) + ": " + folkway_story.getName();
            }
        }
        if (storys.length() > 0)
            festivalstory.setText(storys);
        else
            festivalstory.setText("无");
    }

    private List<String> GetViligesForMaster(Folkway_Master master){
        List<String> keyAndValues = new ArrayList<>();

        List<Folkway_StandardInfo> folkway_standardInfos = LitePal.findAll(Folkway_StandardInfo.class);
        for (int kk = 0; kk < folkway_standardInfos.size(); kk++) {
            Folkway_StandardInfo folkway_standardInfo = folkway_standardInfos.get(kk);
            String Festival = folkway_standardInfo.getFestival();
            Festival = Festival.replace("|", "&");
            String[] Festivals = Festival.split("&");
            for (int i = 0; i < Festivals.length; i++) {
                List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
                if (folkway_festivals.size() > 0){
                    String process = folkway_festivals.get(0).getProcedure();
                    process = process.replace("|", "&");
                    String[] days = process.split("&");
                    for (int j = 0; j < days.length; j++) {
                        String[] oneday = days[j].split(",");
                        for (int k = 0; k < oneday.length; k++) {
                            String activity = oneday[k];
                            if (activity.contains("C")) {
                                List<Folkway_Ceremony> list = LitePal.where("objectID = ?", activity).find(Folkway_Ceremony.class);
                                if (list.size()>0) {
                                    Folkway_Ceremony folkway_ceremony = list.get(0);
                                    String Master = folkway_ceremony.getMaster();
                                    if (Master.contains(master.getObjectID()))
                                        keyAndValues.add(folkway_standardInfo.getObjectID());
                                }
                            } else if (activity.contains("A")) {
                                List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                                if (list.size() > 0) {
                                    Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                    String Master = Folkway_OtherActivity.getMaster();
                                    if (Master.contains(master.getObjectID()))
                                        keyAndValues.add(folkway_standardInfo.getObjectID());
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
                if (folkway_ceremonies.size() > 0) {
                    Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                    String Master = folkway_ceremony.getMaster();
                    if (Master.contains(master.getObjectID()))
                        keyAndValues.add(folkway_standardInfo.getObjectID());
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

    private List<String> GetViligeNameForOID(List<String> list){
        List<String> Viliges = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_StandardInfo> folkway_standardInfos = LitePal.where("objectID = ?", list.get(i)).find(Folkway_StandardInfo.class);
            if (folkway_standardInfos.size() > 0) {
                Folkway_StandardInfo folkway_standardInfo = folkway_standardInfos.get(0);
                Viliges.add(folkway_standardInfo.getDistrictName().substring(folkway_standardInfo.getDistrictName().indexOf("州") + 1) + folkway_standardInfo.getTownName() + folkway_standardInfo.getVillageName());
            }
        }
        return Viliges;
    }

    private void MakeViligePart(Folkway_Master master){
        List<String> objectids = GetViligesForMaster(master);
        List<String> standardInfos = GetViligeNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_master_vilige);
        festivalviliges.setText("该主持人负责以下" + standardInfos.size() + "个村落：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_master_vilige);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayMasterShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayMasterShow.this, FolkwaysShow.class);
                intent.putExtra("DZQBM", master);
                FolkwayMasterShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayMasterShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetCeremonysForMaster(Folkway_Master master){
        List<String> keyAndValues = new ArrayList<>();

        List<Folkway_StandardInfo> folkway_standardInfos = LitePal.findAll(Folkway_StandardInfo.class);
        for (int kk = 0; kk < folkway_standardInfos.size(); kk++) {
            Folkway_StandardInfo folkway_standardInfo = folkway_standardInfos.get(kk);
            String Festival = folkway_standardInfo.getFestival();
            Festival = Festival.replace("|", "&");
            String[] Festivals = Festival.split("&");
            for (int i = 0; i < Festivals.length; i++) {
                List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
                if (folkway_festivals.size() > 0){
                    String process = folkway_festivals.get(0).getProcedure();
                    process = process.replace("|", "&");
                    String[] days = process.split("&");
                    for (int j = 0; j < days.length; j++) {
                        String[] oneday = days[j].split(",");
                        for (int k = 0; k < oneday.length; k++) {
                            String activity = oneday[k];
                            if (activity.contains("C")) {
                                List<Folkway_Ceremony> list = LitePal.where("objectID = ?", activity).find(Folkway_Ceremony.class);
                                if (list.size()>0) {
                                    Folkway_Ceremony folkway_ceremony = list.get(0);
                                    String Master = folkway_ceremony.getMaster();
                                    if (Master.contains(master.getObjectID()))
                                        keyAndValues.add(folkway_ceremony.getObjectID());
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
                if (folkway_ceremonies.size() > 0) {
                    Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                    String Master = folkway_ceremony.getMaster();
                    if (Master.contains(master.getObjectID()))
                        keyAndValues.add(folkway_ceremony.getObjectID());
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

    private List<String> GetCeremonysNameForOID(List<String> list){
        List<String> Ceremonys = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Ceremony.class);
            if (folkway_ceremonies.size() > 0) {
                Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                Ceremonys.add(folkway_ceremony.getName());
            }
        }
        return Ceremonys;
    }

    private void MakeCeremonyPart(Folkway_Master master){
        List<String> objectids = GetCeremonysForMaster(master);
        List<String> standardInfos = GetCeremonysNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_master_ceremony);
        festivalviliges.setText("该主持人负责以下" + standardInfos.size() + "个仪式：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_master_ceremony);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayMasterShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayMasterShow.this, FolkwayCeremonyShow.class);
                intent.putExtra("objectid", master);
                FolkwayMasterShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayMasterShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetFestivalsForMaster(Folkway_Master master){
        List<String> keyAndValues = new ArrayList<>();

        List<Folkway_StandardInfo> folkway_standardInfos = LitePal.findAll(Folkway_StandardInfo.class);
        for (int kk = 0; kk < folkway_standardInfos.size(); kk++) {
            Folkway_StandardInfo folkway_standardInfo = folkway_standardInfos.get(kk);
            String Festival = folkway_standardInfo.getFestival();
            Festival = Festival.replace("|", "&");
            String[] Festivals = Festival.split("&");
            for (int i = 0; i < Festivals.length; i++) {
                List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
                if (folkway_festivals.size() > 0){
                    String process = folkway_festivals.get(0).getProcedure();
                    process = process.replace("|", "&");
                    String[] days = process.split("&");
                    for (int j = 0; j < days.length; j++) {
                        String[] oneday = days[j].split(",");
                        for (int k = 0; k < oneday.length; k++) {
                            String activity = oneday[k];
                            if (activity.contains("C")) {
                                List<Folkway_Ceremony> list = LitePal.where("objectID = ?", activity).find(Folkway_Ceremony.class);
                                if (list.size()>0) {
                                    Folkway_Ceremony folkway_ceremony = list.get(0);
                                    String Master = folkway_ceremony.getMaster();
                                    if (Master.contains(master.getObjectID()))
                                        keyAndValues.add(folkway_festivals.get(0).getObjectID());
                                }
                            }else if (activity.contains("A")) {
                                List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                                if (list.size() > 0) {
                                    Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                    String Master = Folkway_OtherActivity.getMaster();
                                    if (Master.contains(master.getObjectID()))
                                        keyAndValues.add(folkway_festivals.get(0).getObjectID());
                                }
                            }
                        }
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

    private List<String> GetFestivalNameForOID(List<String> list){
        List<String> Festivals = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Festival.class);
            if (folkway_festivals.size() > 0) {
                Folkway_Festival folkway_festival = folkway_festivals.get(0);
                Festivals.add(folkway_festival.getName());
            }
        }
        return Festivals;
    }

    private void MakeFestivalPart(Folkway_Master master){
        List<String> objectids = GetFestivalsForMaster(master);
        List<String> standardInfos = GetFestivalNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_master_festival);
        festivalviliges.setText("该主持人负责以下" + standardInfos.size() + "个节日：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_master_festival);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayMasterShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayMasterShow.this, FolkwayFestivalShow.class);
                intent.putExtra("objectid", master);
                FolkwayMasterShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayMasterShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filemanagemenu, menu);
        menu.findItem(R.id.back_filemanage).setVisible(true);
        menu.findItem(R.id.closeall_filemanage).setVisible(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back_filemanage:
                this.finish();
                break;
            case R.id.closeall_filemanage:
                ActivityCollector.finishAll();
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
