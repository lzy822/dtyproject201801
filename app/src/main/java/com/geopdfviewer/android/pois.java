package com.geopdfviewer.android;

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

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class pois extends AppCompatActivity {
    private static final String TAG = "pois";
    private String ic;
    private List<mPOIobj> mPOIobjList = new ArrayList<>();
    private mPOIobjAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private String selectedPOIC;
    private int isLongClick = 1;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pois);
        setTitle("兴趣点列表");
        Intent intent = getIntent();
        ic = intent.getStringExtra("ic");
        //refreshCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        refreshCard();
    }

    private void refreshCard(){
        mPOIobjList.clear();
        List<POI> pois = DataSupport.where("ic = ?", ic).find(POI.class);
        for (POI poi : pois){
            mPOIobj mPOIobj = new mPOIobj(poi.getPOIC(), poi.getX(), poi.getY(), poi.getTime(), poi.getTapenum(), poi.getPhotonum(), poi.getName(), poi.getDescription());
            mPOIobjList.add(mPOIobj);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_poi);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new mPOIobjAdapter(mPOIobjList);
        adapter.setOnItemLongClickListener(new mPOIobjAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String POIC) {
                selectedPOIC = POIC;
                isLongClick = 0;
                invalidateOptionsMenu();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_andupdate).setVisible(false);
        menu.findItem(R.id.add_pois).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        switch (isLongClick){
            case 1:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.restore_pois).setVisible(false);
                menu.findItem(R.id.deletepoi).setVisible(false);
                break;
            case 0:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.back_pois).setVisible(false);
                menu.findItem(R.id.deletepoi).setVisible(true);
                menu.findItem(R.id.restore_pois).setVisible(true);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_pois:
                this.finish();
                break;
            case R.id.restore_pois:
                isLongClick = 1;
                refreshCard();
                invalidateOptionsMenu();
                break;
            case R.id.deletepoi:
                isLongClick = 1;
                DataSupport.deleteAll(POI.class, "POIC = ?", selectedPOIC);
                DataSupport.deleteAll(MTAPE.class, "POIC = ?", selectedPOIC);
                DataSupport.deleteAll(MPHOTO.class, "POIC = ?", selectedPOIC);
                refreshCard();
                break;
            default:
                break;
        }
        return true;
    }
}
