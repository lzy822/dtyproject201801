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

public class FolkwayCeremonyShow extends AppCompatActivity {
    private static final String TAG = "民族信仰地图集展示页面";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkway_ceremony_show);
        ActivityCollector.addActivity(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.folkway_ceremony_toolbar);
        toolbar.setTitle("仪式");
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        String oid = intent.getStringExtra("objectid");
        //Toast.makeText(FolkwayCeremonyShow.this, oid, Toast.LENGTH_SHORT).show();

        Folkway_Ceremony ceremony = LitePal.where("objectid = ?", oid).find(Folkway_Ceremony.class).get(0);
        TextView ceremonyname = findViewById(R.id.txt_ceremony_name);
        ceremonyname.setText(ceremony.getName());

        TextView ceremonyabstract = findViewById(R.id.txt_ceremony_abstract);
        ceremonyabstract.setText(ceremony.getAbstract());

        TextView ceremonytime = findViewById(R.id.txt_ceremony_time);
        ceremonytime.setText(ceremony.getTime());

        TextView ceremonylocation = findViewById(R.id.txt_ceremony_location);
        ceremonylocation.setText(ceremony.getLocation());

        MakeTabooPart(ceremony);
        MakeStoryPart(ceremony);
        MakeViligePart(ceremony);
        MakeMastersPart(ceremony);
        MakeObjectsPart(ceremony);
        MakeJoinmenPart(ceremony);

        TextView ceremonyotherinfo = findViewById(R.id.txt_ceremony_otherinfo);
        ceremonyotherinfo.setText(ceremony.getOtherInfo());
    }

    private void MakeTabooPart(Folkway_Ceremony ceremony){
        RecyclerView recyclerView = findViewById(R.id.festival_recycler_ceremony_taboo);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayCeremonyShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> taboolist = new ArrayList<>();
        List<String> objectids = new ArrayList<>();
        String[] taboos = ceremony.getTaboo().replace("|", "&").split("&");
        for (int i = 0; i < taboos.length; i++) {
            String taboo = taboos[i];
            List<Folkway_Taboo> folkway_taboos = LitePal.where("objectid = ?", taboo).find(Folkway_Taboo.class);
            if (folkway_taboos.size() > 0) {
                Folkway_Taboo folkway_taboo = folkway_taboos.get(0);
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


                Intent intent = new Intent(FolkwayCeremonyShow.this, FolkwayTabooShow.class);
                intent.putExtra("objectid", CeremonyOrActivity);
                startActivity(intent);

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
                Toast.makeText(FolkwayCeremonyShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void MakeStoryPart(Folkway_Ceremony ceremony){
        TextView festivalstory = findViewById(R.id.txt_ceremony_story);
        String storys = "";
        String[] stories = ceremony.getStory().replace("|", "&").split("&");
        Log.w(TAG, "MakeStoryPart: " + stories.length + ", " + ceremony.getStory());
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

    private void MakeViligePart(Folkway_Ceremony ceremony){
        List<String> objectids = new ArrayList<>();
        List<String> standardInfos = new ArrayList<>();
        List<Folkway_StandardInfo> list = LitePal.findAll(Folkway_StandardInfo.class);
        for (int i = 0; i < list.size(); i++) {
            Folkway_StandardInfo folkway_standardInfo = list.get(i);
            String c = folkway_standardInfo.getCeremony();
            if (c.contains(ceremony.getObjectID()))
            {
                objectids.add(folkway_standardInfo.getObjectID());
                standardInfos.add(folkway_standardInfo.getDistrictName().substring(folkway_standardInfo.getDistrictName().indexOf("州")+1) + folkway_standardInfo.getTownName() + folkway_standardInfo.getVillageName());
            }
            else{
                String fs = folkway_standardInfo.getFestival();
                String[] fss = fs.replace("无", "").replace("|", "&").split("&");
                for (int j = 0; j < fss.length; j++) {
                    List<Folkway_Festival> folkway_festivals = LitePal.where("objectid = ?", fss[j]).find(Folkway_Festival.class);
                    if (folkway_festivals.size() > 0){
                        Folkway_Festival folkway_festival = folkway_festivals.get(0);
                        String c1 = folkway_festival.getProcedure();
                        if (c1.contains(ceremony.getObjectID())){
                            objectids.add(folkway_standardInfo.getObjectID());
                            standardInfos.add(folkway_standardInfo.getDistrictName().substring(folkway_standardInfo.getDistrictName().indexOf("州")+1) + folkway_standardInfo.getTownName() + folkway_standardInfo.getVillageName());
                            break;
                        }
                    }
                }
            }
        }

        PieChartView pieChartView = (PieChartView) findViewById(R.id.ceremony_pie);
        List<SliceValue> sliceValues = new ArrayList<>();
        DecimalFormat decimalFormat1 = new DecimalFormat("0.00");
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

        TextView festivalviliges = findViewById(R.id.txt_ceremony_viliges);
        festivalviliges.setText("有以下" + standardInfos.size() + "个村子有该仪式：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_ceremony_viliges);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayCeremonyShow.this,1);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String district = objectids.get(position);

                Intent intent = new Intent(FolkwayCeremonyShow.this, FolkwaysShow.class);
                intent.putExtra("DZQBM", district);
                FolkwayCeremonyShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayCeremonyShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetMastersForCeremony(Folkway_Ceremony ceremony){
        List<String> keyAndValues = new ArrayList<>();
        List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremony.getObjectID()).find(Folkway_Ceremony.class);
        String masters = folkway_ceremonies.get(0).getMaster();
        masters = masters.replace("无", "").replace("|", "&");
        String[] mmasters = masters.split("&");
        for (int j = 0; j < mmasters.length; j++) {
            List<Folkway_Master> list = LitePal.where("objectid = ?", mmasters[j]).find(Folkway_Master.class);
            if (list.size() > 0) {
                Folkway_Master folkway_masters = list.get(0);
                keyAndValues.add(folkway_masters.getObjectID());
            }
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

    private void MakeMastersPart(Folkway_Ceremony ceremony){
        List<String> objectids = GetMastersForCeremony(ceremony);
        List<String> standardInfos = GetMasterNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_ceremony_masters);
        festivalviliges.setText("该仪式有以下" + standardInfos.size() + "个主持人：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_ceremony_masters);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayCeremonyShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayCeremonyShow.this, FolkwayMasterShow.class);
                intent.putExtra("objectid", master);
                FolkwayCeremonyShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayCeremonyShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetObjectsForCeremony(Folkway_Ceremony ceremony){
        List<String> keyAndValues = new ArrayList<>();


        List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremony.getObjectID()).find(Folkway_Ceremony.class);

        String object = folkway_ceremonies.get(0).getObject();
        object = object.replace("无", "").replace("|", "&");
        String[] objects = object.split("&");
        for (int j = 0; j < objects.length; j++) {
            List<Folkway_Object> list = LitePal.where("objectid = ?", objects[j]).find(Folkway_Object.class);
            if (list.size() > 0) {
                Folkway_Object folkway_object = list.get(0);
                keyAndValues.add(folkway_object.getObjectID());
            }
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

    private void MakeObjectsPart(Folkway_Ceremony ceremony){
        List<String> objectids = GetObjectsForCeremony(ceremony);
        List<String> standardInfos = GetObjectNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_ceremony_objects);
        festivalviliges.setText("该仪式有以下" + standardInfos.size() + "个祭祀对象：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_ceremony_objects);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayCeremonyShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String object = objectids.get(position);

                Intent intent = new Intent(FolkwayCeremonyShow.this, FolkwayObjectShow.class);
                intent.putExtra("objectid", object);
                FolkwayCeremonyShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayCeremonyShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetJoinmenForCeremony(Folkway_Ceremony ceremony){
        List<String> keyAndValues = new ArrayList<>();


        List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremony.getObjectID()).find(Folkway_Ceremony.class);

        String participant = folkway_ceremonies.get(0).getParticipants();
        participant = participant.replace("无", "").replace("|", "&");
        String[] participants = participant.split("&");
        for (int j = 0; j < participants.length; j++) {
            List<Folkway_Participants> list = LitePal.where("objectid = ?", participants[j]).find(Folkway_Participants.class);
            if (list.size()>0) {
                Folkway_Participants folkway_participants = list.get(0);
                keyAndValues.add(folkway_participants.getObjectID());
            }
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

    private void MakeJoinmenPart(Folkway_Ceremony ceremony){
        List<String> objectids = GetJoinmenForCeremony(ceremony);
        List<String> standardInfos = GetJoinmenNameForOID(objectids);
        for (int i = 0; i < standardInfos.size(); i++) {
            standardInfos.set(i, (i+1) + ", " + standardInfos.get(i));
        }
        TextView festivalviliges = findViewById(R.id.txt_ceremony_joinmen);
        festivalviliges.setText("该仪式有以下" + standardInfos.size() + "个参与群体：");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_ceremony_joinmen);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayCeremonyShow.this,1);
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
                Toast.makeText(FolkwayCeremonyShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
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
