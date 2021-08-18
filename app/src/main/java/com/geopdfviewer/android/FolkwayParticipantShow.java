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

public class FolkwayParticipantShow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folkway_participant_show);
        ActivityCollector.addActivity(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.folkway_participant_toolbar);
        toolbar.setTitle("参与人员");
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        String oid = intent.getStringExtra("objectid");
        String xzqid = intent.getStringExtra("xzqid");
        //Toast.makeText(FolkwayParticipantShow.this, oid, Toast.LENGTH_SHORT).show();

        Folkway_Participants folkway_participants = LitePal.where("objectid = ?", oid).find(Folkway_Participants.class).get(0);
        Folkway_StandardInfo folkway_standardInfo = LitePal.where("objectid = ?", xzqid).find(Folkway_StandardInfo.class).get(0);


        TextView masterIdentity = findViewById(R.id.txt_participants_xzqname);
        masterIdentity.setText(folkway_standardInfo.getDistrictName() + folkway_standardInfo.getTownName());

        TextView masterName = findViewById(R.id.txt_participants_viligename);
        masterName.setText(folkway_standardInfo.getVillageName());

        TextView masterLevel = findViewById(R.id.txt_participants_name);
        masterLevel.setText(folkway_participants.getName());


        refreshFestivalRecyclerForParticipants(folkway_standardInfo, folkway_participants);
        refreshCeremonyRecyclerForParticipants(folkway_standardInfo, folkway_participants);
        refreshActivityRecyclerForParticipants(folkway_standardInfo, folkway_participants);
    }

    private List<String> GetFestivalsParticipantsForStandardInfo(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        String ParticipantID = folkway_participants.getObjectID();
        List<String> keyAndValues = new ArrayList<>();
        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();

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
                                String Master = folkway_ceremony.getParticipants();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                                    if (folkway_masters.size() > 0){
                                        if (ParticipantID.equals(masters[l])){
                                            keyAndValues.add(Festivals[i]);
                                            break;
                                        }
                                    }
                                }
                            }
                        } else if (activity.contains("A")) {
                            List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                            if (list.size() > 0) {
                                Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                String Master = Folkway_OtherActivity.getParticipants();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                                    if (folkway_masters.size() > 0){
                                        if (ParticipantID.equals(masters[l])){
                                            keyAndValues.add(Festivals[i]);
                                            break;
                                        }
                                    }
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

    private List<String> GetFestivalsParticipantsNameForOID(List<String> list){
        List<String> Masters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Festival> folkway_masters = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Festival.class);
            Masters.add(folkway_masters.get(0).getName());
        }
        return Masters;
    }

    private void refreshFestivalRecyclerForParticipants(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.festival_recycler_participants_festival);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayParticipantShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> mastersOID = GetFestivalsParticipantsForStandardInfo(folkway_standardInfo, folkway_participants);
        List<String> masters = GetFestivalsParticipantsNameForOID(mastersOID);
        for (int i = 0; i < masters.size(); i++) {
            masters.set(i, (i+1) + ", " + masters.get(i));
        }
        TextView textView = findViewById(R.id.txt_participants_festival);
        textView.setText("该群体可参与以下" + masters.size() + "个节日");
        FestivalAdapter festivalAdapter = new FestivalAdapter(masters);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                Intent intent = new Intent(FolkwayParticipantShow.this, FolkwayFestivalShow.class);
                intent.putExtra("objectid", mastersOID.get(position));
                startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayParticipantShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> GetCeremonysParticipantsForStandardInfo(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        String ParticipantID = folkway_participants.getObjectID();
        List<String> keyAndValues = new ArrayList<>();
        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();

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
                                String Master = folkway_ceremony.getParticipants();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                                    if (folkway_masters.size() > 0){
                                        if (ParticipantID.equals(masters[l])){
                                            keyAndValues.add(activity);
                                            break;
                                        }
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
            if (folkway_ceremonies.size() > 0) {
                Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                String Master = folkway_ceremony.getParticipants();
                Master = Master.replace("|", "&");
                String[] masters = Master.split("&");
                for (int l = 0; l < masters.length; l++) {
                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                    if (folkway_masters.size() > 0){
                        if (ParticipantID.equals(masters[l])){
                            keyAndValues.add(ceremonys[i]);
                            break;
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

    private List<String> GetCeremonysParticipantsNameForOID(List<String> list){
        List<String> Masters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_Ceremony> folkway_masters = LitePal.where("objectID = ?", list.get(i)).find(Folkway_Ceremony.class);
            Masters.add(folkway_masters.get(0).getName());
        }
        return Masters;
    }

    private void refreshCeremonyRecyclerForParticipants(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.festival_recycler_participants_ceremony);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayParticipantShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> mastersOID = GetCeremonysParticipantsForStandardInfo(folkway_standardInfo, folkway_participants);
        List<String> masters = GetCeremonysParticipantsNameForOID(mastersOID);
        for (int i = 0; i < masters.size(); i++) {
            masters.set(i, (i+1) + ", " + masters.get(i));
        }
        TextView textView = findViewById(R.id.txt_participants_ceremony);
        textView.setText("该群体可参与以下" + masters.size() + "个仪式");

        PieChartView pieChartView = (PieChartView) findViewById(R.id.participant_pie);
        List<SliceValue> sliceValues = new ArrayList<>();
        DecimalFormat decimalFormat1 = new DecimalFormat("0.00");
        int CeremonyNum = GetCeremonyNumParticipantsForStandardInfo(folkway_standardInfo, folkway_participants);
        SliceValue sliceValue = new SliceValue(Float.valueOf(masters.size()) / (float) CeremonyNum, ChartUtils.COLOR_RED);
        SliceValue sliceValue1 = new SliceValue(Float.valueOf((float) CeremonyNum - masters.size()) / (float) CeremonyNum, ChartUtils.COLOR_GREEN);
        sliceValue.setLabel("仪式占比:" + decimalFormat1.format(Float.valueOf(masters.size()) / (float) CeremonyNum * 100) + "%");
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
        FestivalAdapter festivalAdapter = new FestivalAdapter(masters);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                Intent intent = new Intent(FolkwayParticipantShow.this, FolkwayCeremonyShow.class);
                intent.putExtra("objectid", mastersOID.get(position));
                startActivity(intent);
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayParticipantShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshActivityRecyclerForParticipants(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.festival_recycler_participants_activity);
        recyclerView.setVisibility(View.VISIBLE);
        GridLayoutManager layoutManager = new GridLayoutManager(FolkwayParticipantShow.this,1);
        recyclerView.setLayoutManager(layoutManager);
        List<String> mastersOID = GetActivitiesParticipantsForStandardInfo(folkway_standardInfo, folkway_participants);
        List<String> masters = GetActivitiesParticipantsNameForOID(mastersOID);
        for (int i = 0; i < masters.size(); i++) {
            masters.set(i, (i+1) + ", " + masters.get(i));
        }
        TextView textView = findViewById(R.id.txt_participants_activity);
        textView.setText("该群体可参与以下" + masters.size() + "个活动");
        FestivalAdapter festivalAdapter = new FestivalAdapter(masters);
        recyclerView.setAdapter(festivalAdapter);
        festivalAdapter.setOnItemClickListener(new FestivalAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String Name, int position) {
                /*Intent intent = new Intent(FolkwayParticipantShow.this, F.class);
                intent.putExtra("objectid", mastersOID.get(position));
                startActivity(intent);*/
            }
        });
        festivalAdapter.setOnItemLongClickListener(new FestivalAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String ObjectName) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(ObjectName);
                Toast.makeText(FolkwayParticipantShow.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int GetCeremonyNumParticipantsForStandardInfo(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        String ParticipantID = folkway_participants.getObjectID();
        List<String> keyAndValues = new ArrayList<>();
        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();

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
                                String Master = folkway_ceremony.getParticipants();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                                    if (folkway_masters.size() > 0){
                                        keyAndValues.add(activity);
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
            if (folkway_ceremonies.size() > 0) {
                Folkway_Ceremony folkway_ceremony = folkway_ceremonies.get(0);
                String Master = folkway_ceremony.getParticipants();
                Master = Master.replace("|", "&");
                String[] masters = Master.split("&");
                for (int l = 0; l < masters.length; l++) {
                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                    if (folkway_masters.size() > 0){
                        keyAndValues.add(ceremonys[i]);
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
        return keyAndValues.size();
    }

    private List<String> GetActivitiesParticipantsForStandardInfo(Folkway_StandardInfo folkway_standardInfo, Folkway_Participants folkway_participants){
        String ParticipantID = folkway_participants.getObjectID();
        List<String> keyAndValues = new ArrayList<>();
        HashMap<String, Folkway_Ceremony> ceremonyHashMap = new HashMap<>();
        HashMap<String, Folkway_OtherActivity> OtheractivityHashMap = new HashMap<>();

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
                        if (activity.contains("A")) {
                            List<Folkway_OtherActivity> list = LitePal.where("objectID = ?", activity).find(Folkway_OtherActivity.class);
                            if (list.size() > 0) {
                                Folkway_OtherActivity Folkway_OtherActivity = list.get(0);
                                String Master = Folkway_OtherActivity.getParticipants();
                                Master = Master.replace("|", "&");
                                String[] masters = Master.split("&");
                                for (int l = 0; l < masters.length; l++) {
                                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                                    if (folkway_masters.size() > 0){
                                        if (ParticipantID.equals(masters[l])){
                                            keyAndValues.add(activity);
                                            break;
                                        }
                                    }
                                }
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
                String Master = folkway_ceremony.getParticipants();
                Master = Master.replace("|", "&");
                String[] masters = Master.split("&");
                for (int l = 0; l < masters.length; l++) {
                    List<Folkway_Participants> folkway_masters = LitePal.where("objectID = ?", masters[l]).find(Folkway_Participants.class);
                    if (folkway_masters.size() > 0){
                        if (ParticipantID.equals(masters[l])){
                            keyAndValues.add(activities[i]);
                            break;
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

    private List<String> GetActivitiesParticipantsNameForOID(List<String> list){
        List<String> Masters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Folkway_OtherActivity> folkway_masters = LitePal.where("objectID = ?", list.get(i)).find(Folkway_OtherActivity.class);
            Masters.add(folkway_masters.get(0).getActivityName());
        }
        return Masters;
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
