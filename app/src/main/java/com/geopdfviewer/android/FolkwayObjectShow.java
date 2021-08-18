package com.geopdfviewer.android;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolkwayObjectShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkway_object_show);
        ActivityCollector.addActivity(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.folkway_object_toolbar);
        toolbar.setTitle("祭祀对象");
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        String oid = intent.getStringExtra("objectid");
        //Toast.makeText(FolkwayObjectShow.this, oid, Toast.LENGTH_SHORT).show();

        Folkway_Object folkway_object = LitePal.where("objectid = ?", oid).find(Folkway_Object.class).get(0);

        TextView objectName = findViewById(R.id.txt_object_name);
        objectName.setText(folkway_object.getName());

        TextView objectAbstract = findViewById(R.id.txt_object_abstract);
        objectAbstract.setText(folkway_object.getAbstract());

        TextView objectSacrifice = findViewById(R.id.txt_object_Sacrifice);
        objectSacrifice.setText(folkway_object.getSacrifice());

        TextView objectOtherinfo = findViewById(R.id.txt_object_otherinfo);
        objectOtherinfo.setText(folkway_object.getOtherInfo());

        MakeStoryPart(folkway_object);
        MakeViligePart(folkway_object);
        MakeCeremonyPart(folkway_object);
        MakeFestivalPart(folkway_object);
    }

    private void MakeStoryPart(Folkway_Object object){
        TextView festivalstory = findViewById(R.id.txt_object_story);
        String storys = "";
        String[] stories = object.getStory().replace("|", "&").split("&");
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

    private List<String> GetViligesForMaster(Folkway_Object object){
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
                                    String Master = folkway_ceremony.getObject();
                                    if (Master.contains(object.getObjectID()))
                                        keyAndValues.add(folkway_standardInfo.getObjectID());
                                }
                            } else if (activity.contains("A")) {
                                List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                                if (list.size() > 0) {
                                    Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                    String Master = Folkway_OtherActivity.getObject();
                                    if (Master.contains(object.getObjectID()))
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
                    String Master = folkway_ceremony.getObject();
                    if (Master.contains(object.getObjectID()))
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

    private void MakeViligePart(Folkway_Object object){
        List<String> objectids = GetViligesForMaster(object);
        List<String> standardInfos = GetViligeNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_object_vilige);
        festivalviliges.setText("有以下" + standardInfos.size() + "个村落祭祀该对象：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_object_vilige);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayObjectShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayObjectShow.this, FolkwaysShow.class);
                intent.putExtra("DZQBM", master);
                FolkwayObjectShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayObjectShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetCeremonysForMaster(Folkway_Object object){
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
                                    String Master = folkway_ceremony.getObject();
                                    if (Master.contains(object.getObjectID()))
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
                    String Master = folkway_ceremony.getObject();
                    if (Master.contains(object.getObjectID()))
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

    private void MakeCeremonyPart(Folkway_Object object){
        List<String> objectids = GetCeremonysForMaster(object);
        List<String> standardInfos = GetCeremonysNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_object_ceremony);
        festivalviliges.setText("有以下" + standardInfos.size() + "个仪式祭祀该对象：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_object_ceremony);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayObjectShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayObjectShow.this, FolkwayCeremonyShow.class);
                intent.putExtra("objectid", master);
                FolkwayObjectShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayObjectShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetFestivalsForMaster(Folkway_Object object){
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
                                    String Master = folkway_ceremony.getObject();
                                    if (Master.contains(object.getObjectID()))
                                        keyAndValues.add(folkway_festivals.get(0).getObjectID());
                                }
                            }else if (activity.contains("A")) {
                                List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                                if (list.size() > 0) {
                                    Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                    String Master = Folkway_OtherActivity.getObject();
                                    if (Master.contains(object.getObjectID()))
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

    private void MakeFestivalPart(Folkway_Object object){
        List<String> objectids = GetFestivalsForMaster(object);
        List<String> standardInfos = GetFestivalNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_object_festival);
        festivalviliges.setText("有以下" + standardInfos.size() + "个节日祭祀该对象：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_object_festival);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayObjectShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayObjectShow.this, FolkwayFestivalShow.class);
                intent.putExtra("objectid", master);
                FolkwayObjectShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayObjectShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
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
