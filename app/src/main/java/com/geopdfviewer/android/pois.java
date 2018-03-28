package com.geopdfviewer.android;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
    double min_lat, max_lat, min_long, max_long;
    private SQLiteOpenHelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pois);
        setTitle("兴趣点列表");
        Intent intent = getIntent();
        ic = intent.getStringExtra("ic");
        min_lat = intent.getDoubleExtra("min_lat", 0);
        max_lat = intent.getDoubleExtra("max_lat", 0);
        min_long = intent.getDoubleExtra("min_long", 0);
        max_long = intent.getDoubleExtra("max_long", 0);
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
        //List<POI> pois = DataSupport.where("ic = ?", ic).find(POI.class);
        //List<POI> pois = DataSupport.where("x <= " + String.valueOf(max_lat) + ";" +  "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
        //List<POI> pois = DataSupport.where("x >= " + String.valueOf(max_lat)).find(POI.class);
        //List<POI> pois = DataSupport.where("y <= ?", String.valueOf(max_long)).where("y >= ?", String.valueOf(min_long)).where("x >= ?", String.valueOf(min_lat)).where("x <= ?", String.valueOf(max_lat)).find(POI.class);
        //List<POI> pois = DataSupport.findAll(POI.class);
        /*Log.w(TAG, "数量 : " + Integer.toString(pois.size()));
        Log.w(TAG, " max_lat : " + Double.toString(max_lat) + " min_lat : " + Double.toString(min_lat) + " max_long : " + Double.toString(max_long) + " min_long : " + Double.toString(min_long));
        for (POI poi : pois){
            mPOIobj mPOIobj = new mPOIobj(poi.getPOIC(), poi.getX(), poi.getY(), poi.getTime(), poi.getTapenum(), poi.getPhotonum(), poi.getName(), poi.getDescription());
            mPOIobjList.add(mPOIobj);
        }*/
        //SQLiteDatabase database = dbhelper.getReadableDatabase();
        //Cursor cursor = database.rawQuery("select * from POI where x >= ? and x <= ? and y >= ? and y <= ?", new String[] {String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long)});
        Cursor cursor = DataSupport.findBySQL("select * from POI where x >= ? and x <= ? and y >= ? and y <= ?", String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long));
        Log.w(TAG, cursor.getColumnName(0) + cursor.getColumnName(1) + cursor.getColumnName(2) + cursor.getColumnName(3));;
        if (cursor.moveToFirst()){
            do {
                String POIC = cursor.getString(cursor.getColumnIndex("poic"));
                //String POIC = "Xx";
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String description = cursor.getString(cursor.getColumnIndex("description"));
                int tapenum = cursor.getInt(cursor.getColumnIndex("tapenum"));
                int photonum = cursor.getInt(cursor.getColumnIndex("photonum"));
                float x = cursor.getFloat(cursor.getColumnIndex("x"));
                float y = cursor.getFloat(cursor.getColumnIndex("y"));
                mPOIobj mPOIobj = new mPOIobj(POIC, x, y, time, tapenum, photonum, name, description);
                mPOIobjList.add(mPOIobj);
            }while (cursor.moveToNext());
        }
        cursor.close();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_poi);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new mPOIobjAdapter(mPOIobjList);
        adapter.setOnItemLongClickListener(new mPOIobjAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String POIC) {
                setTitle("正在进行长按操作");
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
                setTitle("兴趣点列表");
                refreshCard();
                invalidateOptionsMenu();
                break;
            case R.id.deletepoi:
                isLongClick = 1;
                setTitle("兴趣点列表");
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
