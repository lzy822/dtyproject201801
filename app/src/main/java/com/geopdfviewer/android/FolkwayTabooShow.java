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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

public class FolkwayTabooShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkway_taboo_show);
        ActivityCollector.addActivity(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.folkway_taboo_toolbar);
        toolbar.setTitle("禁忌");
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        String oid = intent.getStringExtra("objectid");
        //Toast.makeText(FolkwayTabooShow.this, oid, Toast.LENGTH_SHORT).show();


        Folkway_Taboo folkway_taboo = LitePal.where("objectid = ?", oid).find(Folkway_Taboo.class).get(0);

        TextView masterIdentity = findViewById(R.id.txt_taboo_name);
        masterIdentity.setText(folkway_taboo.getName());

        refreshFestivalRecyclerForTaboo(folkway_taboo);
        refreshCeremonyRecyclerForTaboo(folkway_taboo);
        MakeViligePart(folkway_taboo);
    }

    private List<String> GetFestivalsTaboosForStandardInfo(Folkway_Taboo folkway_taboo){
        String TabooId = folkway_taboo.getObjectID();
        List<String> keyAndValues = new ArrayList<>();
        List<Folkway_StandardInfo> folkway_standardInfos = LitePal.findAll(Folkway_StandardInfo.class);
        for (int c = 0; c < folkway_standardInfos.size(); c++) {
            Folkway_StandardInfo folkway_standardInfo = folkway_standardInfos.get(c);
            String Festival = folkway_standardInfo.getFestival();
            Festival = Festival.replace("|", "&");
            String[] Festivals = Festival.split("&");
            a:for (int i = 0; i < Festivals.length; i++) {
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
                                    String Master = folkway_ceremony.getTaboo();
                                    if (Master.contains(TabooId))
                                        keyAndValues.add(Festivals[i]);
                                }
                            } else if (activity.contains("A")) {
                                List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                                if (list.size() > 0) {
                                    Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                    String Master = Folkway_OtherActivity.getTaboo();
                                    if (Master.contains(TabooId))
                                        keyAndValues.add(Festivals[i]);
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

    private List<String> GetFestivalsTaboosNameForOID(List<String> list){
        List<String> Masters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Festival> folkway_masters = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Festival.class);
            Masters.add(folkway_masters.get(0).getName());
        }
        return Masters;
    }

    private void refreshFestivalRecyclerForTaboo(Folkway_Taboo folkway_taboo){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.festival_recycler_taboo_festival);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayTabooShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> mastersOID = GetFestivalsTaboosForStandardInfo(folkway_taboo);
        List<String> masters = GetFestivalsTaboosNameForOID(mastersOID);
        for (int i = 0; i < masters.size(); i++) {
            masters.set(i, (i+1) + ", " + masters.get(i));
        }
        TextView textView = findViewById(R.id.txt_taboo_festival);
        textView.setText("以下" + masters.size() + "个节日有该禁忌");
        FestivalAdapter festivalAdapter = new FestivalAdapter(masters);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                Intent intent = new Intent(FolkwayTabooShow.this, FolkwayFestivalShow.class);
                intent.putExtra("objectid", mastersOID.get(position));
                startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayTabooShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetCeremonysTaboosForStandardInfo(Folkway_Taboo folkway_taboo){
        String TabooId = folkway_taboo.getObjectID();
        List<String> keyAndValues = new ArrayList<>();

        List<Folkway_StandardInfo> folkway_standardInfos = LitePal.findAll(Folkway_StandardInfo.class);
        for (int c = 0; c < folkway_standardInfos.size(); c++) {
            Folkway_StandardInfo folkway_standardInfo = folkway_standardInfos.get(c);
            String Festival = folkway_standardInfo.getFestival();
            Festival = Festival.replace("|", "&");
            String[] Festivals = Festival.split("&");
            a:
            for (int i = 0; i < Festivals.length; i++) {
                List<Folkway_Festival> folkway_festivals = LitePal.where("objectID = ?", Festivals[i]).find(Folkway_Festival.class);
                if (folkway_festivals.size() > 0) {
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
                                    String Master = folkway_ceremony.getTaboo();
                                    if (Master.contains(TabooId))
                                        keyAndValues.add(activity);
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
                    String Master = folkway_ceremony.getTaboo();
                    if (Master.contains(TabooId))
                        keyAndValues.add(ceremonys[i]);
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

    private List<String> GetCeremonysTaboosNameForOID(List<String> list){
        List<String> Masters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Ceremony> folkway_masters = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Ceremony.class);
            Masters.add(folkway_masters.get(0).getName());
        }
        return Masters;
    }

    private void refreshCeremonyRecyclerForTaboo(Folkway_Taboo folkway_taboo){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.festival_recycler_taboo_ceremony);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayTabooShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> mastersOID = GetCeremonysTaboosForStandardInfo(folkway_taboo);
        List<String> masters = GetCeremonysTaboosNameForOID(mastersOID);
        for (int i = 0; i < masters.size(); i++) {
            masters.set(i, (i+1) + ", " + masters.get(i));
        }
        TextView textView = findViewById(R.id.txt_taboo_ceremony);
        textView.setText("以下" + masters.size() + "个仪式有该禁忌");
        FestivalAdapter festivalAdapter = new FestivalAdapter(masters);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                Intent intent = new Intent(FolkwayTabooShow.this, FolkwayCeremonyShow.class);
                intent.putExtra("objectid", mastersOID.get(position));
                startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayTabooShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetViligesForTaboo(Folkway_Taboo folkway_taboo){
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
                                    String Master = folkway_ceremony.getTaboo();
                                    if (Master.contains(folkway_taboo.getObjectID()))
                                        keyAndValues.add(folkway_standardInfo.getObjectID());
                                }
                            } else if (activity.contains("A")) {
                                List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                                if (list.size() > 0) {
                                    Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                    String Master = Folkway_OtherActivity.getTaboo();
                                    if (Master.contains(folkway_taboo.getObjectID()))
                                        keyAndValues.add(folkway_standardInfo.getObjectID());
                                }
                            }
                        }
                    }
                }
            }
            String activity = folkway_standardInfo.getActivity();
            activity = activity.replace("|", "&");
            String[] activities = activity.split("&");
            for (int i = 0; i < activities.length; i++) {
                List<Folkway_OtherActivity> folkway_ceremonies = LitePal.where("objectID = ?", activities[i]).find(Folkway_OtherActivity.class);
                if (folkway_ceremonies.size() > 0) {
                    Folkway_OtherActivity folkway_ceremony = folkway_ceremonies.get(0);
                    String Master = folkway_ceremony.getTaboo();
                    if (Master.contains(folkway_taboo.getObjectID()))
                        keyAndValues.add(folkway_standardInfo.getObjectID());
                }
            }

            String ceremony = folkway_standardInfo.getCeremony();
            ceremony = ceremony.replace("|", "&");
            String[] ceremonys = ceremony.split("&");
            for (int i = 0; i < ceremonys.length; i++) {
                List<Folkway_Ceremony> folkway_ceremonies = LitePal.where("objectID = ?", ceremonys[i]).find(Folkway_Ceremony.class);
                if (folkway_ceremonies.size() > 0) {
                    Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                    String Master = folkway_ceremony.getTaboo();
                    if (Master.contains(folkway_taboo.getObjectID()))
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

    private void MakeViligePart(Folkway_Taboo folkway_taboo){
        List<String> objectids = GetViligesForTaboo(folkway_taboo);
        List<String> standardInfos = GetViligeNameForOID(objectids);
        TextView festivalviliges = findViewById(R.id.txt_taboo_vilige);
        festivalviliges.setText("有以下" + standardInfos.size() + "个村落有该禁忌");

        RecyclerView recyclerView = findViewById(R.id.festival_recycler_taboo_vilige);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayTabooShow.this,3);
        recyclerView.setLayoutManager(layoutManager);

        FestivalAdapter festivalAdapter = new FestivalAdapter(standardInfos);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                String master = objectids.get(position);

                Intent intent = new Intent(FolkwayTabooShow.this, FolkwaysShow.class);
                intent.putExtra("DZQBM", master);
                FolkwayTabooShow.this.startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayTabooShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
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
