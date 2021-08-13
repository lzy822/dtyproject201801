package com.geopdfviewer.android;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class FolkwayFestivalShow extends AppCompatActivity {

    private static final String TAG = "民族信仰地图集展示页面";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkway_festival_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.folkway_festival_toolbar);
        toolbar.setTitle("节日");
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        String oid = intent.getStringExtra("objectid");
        Toast.makeText(FolkwayFestivalShow.this, oid, Toast.LENGTH_SHORT).show();
        Folkway_Festival festival = LitePal.where("objectid = ?", oid).find(Folkway_Festival.class).get(0);
        TextView festivalname = findViewById(R.id.txt_festival_name);
        festivalname.setText(festival.getName());

        TextView festivalabstract = findViewById(R.id.txt_festival_abstract);
        festivalabstract.setText(festival.getAbstract());

        TextView festivaltime = findViewById(R.id.txt_festival_time);
        festivaltime.setText(festival.getTime());

        TextView festivallocation = findViewById(R.id.txt_festival_location);
        festivallocation.setText(festival.getLocation());

        MakeTabooPart(festival);
        MakeProcessPart(festival);
        MakeStoryPart(festival);
        MakeViligePart(festival);
        MakeMastersPart(festival);
        MakeObjectsPart(festival);
        MakeJoinmenPart(festival);

        TextView festivalotherinfo = findViewById(R.id.txt_festival_otherinfo);
        festivalotherinfo.setText(festival.getOtherInfo());
    }

    private void MakeProcessPart(Folkway_Festival festival){
        RecyclerView recyclerView = findViewById(R.id.festival_recycler_process);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayFestivalShow.this,3);
        recyclerView.setLayoutManager(layoutManager);
        List<String> mprocess = new ArrayList<>();
        List<KeyAndValue> keyAndValues = new ArrayList<>();
        String[] days = festival.getProcedure().split(";");
        for (int i = 0; i < days.length; i++) {
            String process = days[i];
            String[] processs = process.replace("|", "&").split("&");
            for (int j = 0; j < processs.length; j++) {
                String CeremonyOrActivity = processs[j];
                if (CeremonyOrActivity.contains("C")){
                    List<Folkway_Ceremony> list = LitePal.where("objectid = ?", CeremonyOrActivity).find(Folkway_Ceremony.class);
                    if (list.size() > 0) {
                        Folkway_Ceremony folkway_ceremony = list.get(0);
                        mprocess.add(folkway_ceremony.getName());
                        keyAndValues.add(new KeyAndValue(CeremonyOrActivity, String.valueOf(i)));
                    }
                }
                else if (CeremonyOrActivity.contains("A")){
                    List<Folkway_OtherActivity> list = LitePal.where("objectid = ?", CeremonyOrActivity).find(Folkway_OtherActivity.class);
                    if (list.size() > 0) {
                        Folkway_OtherActivity folkwayOtherActivity = list.get(0);
                        mprocess.add(folkwayOtherActivity.getActivityName());
                        keyAndValues.add(new KeyAndValue(CeremonyOrActivity, String.valueOf(i)));
                    }
                }
            }
        }

        FestivalAdapter festivalAdapter = new FestivalAdapter(mprocess, keyAndValues);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String CeremonyOrActivity = keyAndValues.get(position).getKey();
                if (CeremonyOrActivity.contains("C")){
                    Intent intent = new Intent(FolkwayFestivalShow.this, FolkwayCeremonyShow.class);
                    intent.putExtra("objectid", keyAndValues.get(position).getKey());
                    startActivity(intent);
                }
                /*else if (CeremonyOrActivity.contains("A")){
                    Intent intent = new Intent(FolkwayFestivalShow.this, F.class);
                    intent.putExtra("objectid", keyAndValues.get(position).getKey());
                    startActivity(intent);
                }*/
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayFestivalShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeTabooPart(Folkway_Festival festival){
        RecyclerView recyclerView = findViewById(R.id.festival_recycler_festival_taboo);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayFestivalShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> taboolist = new ArrayList<>();
        List<String> objectids = new ArrayList<>();
        String[] taboos = festival.getTaboo().replace("|", "&").split("&");
        for (int i = 0; i < taboos.length; i++) {
            String taboo = taboos[i];
            Log.w(TAG, "MakeTabooPart: " + taboo);
            List<Folkway_Taboo> list = LitePal.where("objectid = ?", taboo).find(Folkway_Taboo.class);
            if (list.size() > 0) {
                Folkway_Taboo folkway_taboo = list.get(0);
                taboolist.add((i + 1) + ": " + folkway_taboo.getName());
                objectids.add(taboo);
            }
        }

        FestivalAdapter festivalAdapter = new FestivalAdapter(taboolist);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String CeremonyOrActivity = objectids.get(position);

                /*Intent intent = new Intent(FolkwayFestivalShow.this, Fo.class);
                intent.putExtra("objectid", keyAndValues.get(position).getKey());
                startActivity(intent);*/
                /*else if (CeremonyOrActivity.contains("A")){
                    Intent intent = new Intent(FolkwayFestivalShow.this, F.class);
                    intent.putExtra("objectid", keyAndValues.get(position).getKey());
                    startActivity(intent);
                }*/
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayFestivalShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeStoryPart(Folkway_Festival festival){
        TextView festivalstory = findViewById(R.id.txt_festival_story);
        String storys = "";
        String[] stories = festival.getStory().replace("|", "&").split("&");
        //String[] stories = "S4|S6|S7|S8".replace("|", "&").split("&");
        for (int i = 0; i < stories.length; i++) {
            List<Folkway_Story> list = LitePal.where("objectid = ?", stories[i]).find(Folkway_Story.class);
            if (list.size() > 0) {
                Folkway_Story folkway_story = list.get(0);
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

    private void MakeViligePart(Folkway_Festival festival){
        List<String> objectids = new ArrayList<>();
        List<String> standardInfos = new ArrayList<>();
        List<Folkway_StandardInfo> list = LitePal.findAll(Folkway_StandardInfo.class);
        for (int i = 0; i < list.size(); i++) {
            Folkway_StandardInfo folkway_standardInfo = list.get(i);
            String fs = folkway_standardInfo.getFestival();
            if (fs.contains(festival.getObjectID()))
            {
                Log.w(TAG, "MakeViligePart: " + festival.getObjectID() + ", " + fs + ", " + i + ", " + fs.contains(festival.getObjectID()));
                objectids.add(folkway_standardInfo.getObjectID());
                standardInfos.add(folkway_standardInfo.getDistrictName().substring(folkway_standardInfo.getDistrictName().indexOf("州")+1) + folkway_standardInfo.getTownName() + folkway_standardInfo.getVillageName());
            }
        }

        PieChartView pieChartView = (PieChartView) findViewById(R.id.festival_pie);
        List<SliceValue> sliceValues = new ArrayList<>();
        DecimalFormat decimalFormat1 = new DecimalFormat("0.00");
        Log.w(TAG, "MakeViligePart: " + Float.valueOf(standardInfos.size()) / (float) list.size() + ", " + Float.valueOf((float) list.size() - standardInfos.size()) / (float) list.size());
        SliceValue sliceValue = new SliceValue(Float.valueOf(standardInfos.size()) / (float) list.size(), ChartUtils.COLOR_RED);
        SliceValue sliceValue1 = new SliceValue(Float.valueOf((float) list.size() - standardInfos.size()) / (float) list.size(), ChartUtils.COLOR_GREEN);
        sliceValue.setLabel("村落占比:" + decimalFormat1.format(Float.valueOf(standardInfos.size()) / (float) list.size() * 100) + "%");
        //sliceValue1.setLabel("无该节日的村落占比:" + decimalFormat1.format(Float.valueOf((float) list.size() - standardInfos.size()) / (float) list.size() * 100) + "%");
        /*SliceValue sliceValue = new SliceValue(0.25f, ChartUtils.COLOR_RED);
        SliceValue sliceValue1 = new SliceValue(0.75f, ChartUtils.COLOR_GREEN);
        sliceValue.setLabel("111");
        sliceValue1.setLabel("222");*/
        sliceValues.add(sliceValue);
        sliceValues.add(sliceValue1);
        PieChartData pieChartData = new PieChartData(sliceValues);
        pieChartData.setHasCenterCircle(true);
        pieChartData.setHasLabels(true);
        pieChartData.setHasLabelsOutside(false);
        pieChartData.setHasLabelsOnlyForSelected(false);
        pieChartView.setValueSelectionEnabled(true);
        pieChartView.setPieChartData(pieChartData);
        pieChartView.setVisibility(View.VISIBLE);

        TextView festivalviliges = findViewById(R.id.txt_festival_viliges);
        festivalviliges.setText("有以下" + standardInfos.size() + "个村子有该节日：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_festival_viliges);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayFestivalShow.this,1);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String district = objectids.get(position);

                Intent intent = new Intent(FolkwayFestivalShow.this, FolkwaysShow.class);
                intent.putExtra("DZQBM", district);
                FolkwayFestivalShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayFestivalShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetMastersForFestival(Folkway_Festival festival){
        List<String> keyAndValues = new ArrayList<>();
        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();


        List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", festival.getObjectID()).find(Folkway_Festival.class);
        String process = folkway_festivals.get(0).getProcedure();
        /*process = process.replace("|", "&");
        String[] days = process.split("&");*/
        String[] days = process.split(";");
        for (int j = 0; j < days.length; j++) {
            String[] oneday = days[j].replace("|", "&").split("&");
            for (int k = 0; k < oneday.length; k++) {
                String activity = oneday[k];
                if (activity.contains("C")) {
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
                                ceremonyHashMap.put(folkway_master.getObjectID(), folkway_ceremony);
                                keyAndValues.add(folkway_master.getObjectID());
                            }
                        }
                    }
                } else if (activity.contains("A")) {
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
                                OtheractivityHashMap.put(folkway_master.getObjectID(), Folkway_OtherActivity);
                                keyAndValues.add(folkway_master.getObjectID());
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

    private List<String> GetMasterNameForOID(List<String> list){
        List<String> Masters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Master> folkway_masters = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Master.class);
            Masters.add(folkway_masters.get(0).getIdentity());
        }
        return Masters;
    }

    private void MakeMastersPart(Folkway_Festival festival){
        List<String> objectids = GetMastersForFestival(festival);
        List<String> standardInfos = GetMasterNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_festival_masters);
        festivalviliges.setText("该节日有以下" + standardInfos.size() + "个主持人：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_festival_masters);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayFestivalShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayFestivalShow.this, FolkwayMasterShow.class);
                intent.putExtra("objectid", master);
                FolkwayFestivalShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayFestivalShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetObjectsForFestival(Folkway_Festival festival){
        List<String> keyAndValues = new ArrayList<>();

        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();


        List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", festival.getObjectID()).find(Folkway_Festival.class);

        String process = folkway_festivals.get(0).getProcedure();
        /*process = process.replace("|", "&");
        String[] days = process.split("&");*/
        String[] days = process.split(";");
        for (int j = 0; j < days.length; j++) {
            String[] oneday = days[j].replace("|", "&").split("&");
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
                                ceremonyHashMap.put(folkway_master.getObjectID(), folkway_ceremony);
                                keyAndValues.add(folkway_master.getObjectID());
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
                                OtheractivityHashMap.put(folkway_master.getObjectID(), Folkway_OtherActivity);
                                keyAndValues.add(folkway_master.getObjectID());
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

    private List<String> GetObjectNameForOID(List<String> list){
        List<String> Objects = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Object> Folkway_Objects = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Object.class);
            Objects.add(Folkway_Objects.get(0).getName());
        }
        return Objects;
    }

    private void MakeObjectsPart(Folkway_Festival festival){
        List<String> objectids = GetObjectsForFestival(festival);
        List<String> standardInfos = GetObjectNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_festival_objects);
        festivalviliges.setText("该节日有以下" + standardInfos.size() + "个祭祀对象：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_festival_objects);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayFestivalShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String object = objectids.get(position);

                Intent intent = new Intent(FolkwayFestivalShow.this, FolkwayObjectShow.class);
                intent.putExtra("objectid", object);
                FolkwayFestivalShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayFestivalShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetJoinmenForFestival(Folkway_Festival festival){
        List<String> keyAndValues = new ArrayList<>();

        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();


        List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", festival.getObjectID()).find(Folkway_Festival.class);

        String process = folkway_festivals.get(0).getProcedure();
        /*process = process.replace("|", "&");
        String[] days = process.split("&");*/
        String[] days = process.split(";");
        for (int j = 0; j < days.length; j++) {
            String[] oneday = days[j].replace("|", "&").split("&");
            for (int k = 0; k < oneday.length; k++) {
                String activity = oneday[k];
                if (activity.contains("C")) {
                    List<Folkway_Ceremony> list = LitePal.where("objectID = ?", activity).find(Folkway_Ceremony.class);
                    if (list.size() > 0) {
                        Folkway_Ceremony folkway_ceremony = list.get(0);
                        String Master = folkway_ceremony.getParticipants();
                        Master = Master.replace("|", "&");
                        String[] masters = Master.split("&");
                        for (int l = 0; l < masters.length; l++) {
                            List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                            if (folkway_masters.size() > 0){
                                Folkway_Participants folkway_master = folkway_masters.get(0);
                                ceremonyHashMap.put(folkway_master.getObjectID(), folkway_ceremony);
                                keyAndValues.add(folkway_master.getObjectID());
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
                            List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                            if (folkway_masters.size() > 0){
                                Folkway_Participants folkway_master = folkway_masters.get(0);
                                OtheractivityHashMap.put(folkway_master.getObjectID(), Folkway_OtherActivity);
                                keyAndValues.add(folkway_master.getObjectID());
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

    private List<String> GetJoinmenNameForOID(List<String> list){
        List<String> Objects = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Participants> Folkway_Objects = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Participants.class);
            Objects.add(Folkway_Objects.get(0).getName());
        }
        return Objects;
    }

    private void MakeJoinmenPart(Folkway_Festival festival){
        List<String> objectids = GetJoinmenForFestival(festival);
        List<String> standardInfos = GetJoinmenNameForOID(objectids);
        for (int i = 0; i < standardInfos.size(); i++) {
            standardInfos.set(i, (i+1) + ", " + standardInfos.get(i));
        }
        TextView festivalviliges = findViewById(R.id.txt_festival_joinmen);
        festivalviliges.setText("该节日有以下" + standardInfos.size() + "个参与群体：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_festival_joinmen);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayFestivalShow.this,1);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String participants = objectids.get(position);

                /*Intent intent = new Intent(FolkwayFestivalShow.this, FolkwayObjectShow.class);
                intent.putExtra("objectid", participants);
                FolkwayFestivalShow.this.startActivity(intent);*/
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayFestivalShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
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
