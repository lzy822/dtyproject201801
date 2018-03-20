package com.geopdfviewer.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pois);
        Intent intent = getIntent();
        ic = intent.getStringExtra("ic");
        //refreshCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_pois:
                this.finish();
                break;
            case R.id.deletepoi:
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
