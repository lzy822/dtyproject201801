package com.geopdfviewer.android;

import android.content.Intent;
import android.content.SharedPreferences;
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
        setTitle(pois.this.getResources().getText(R.string.POIList));
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

        SharedPreferences pref = getSharedPreferences("update_query_attr_to_map", MODE_PRIVATE);
        String poic = pref.getString("poic", "");
        if (!poic.isEmpty()) this.finish();
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        refreshCard();
    }

    private void refreshCard(){
        mPOIobjList.clear();
        Cursor cursor = DataSupport.findBySQL("select * from POI where x >= ? and x <= ? and y >= ? and y <= ?", String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long));
        if (cursor.moveToFirst()){
            do {
                String POIC = cursor.getString(cursor.getColumnIndex("poic"));
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
                setTitle(pois.this.getResources().getText(R.string.IsLongClicking));
                selectedPOIC = POIC;
                isLongClick = 0;
                invalidateOptionsMenu();
            }
        });
        adapter.setOnItemClickListener(new mPOIobjAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String map_num, int position) {
                mPOIobjAdapter.ViewHolder holder = new mPOIobjAdapter.ViewHolder(view);
                mPOIobj poi = mPOIobjList.get(position);
                if (isLongClick == 0){
                    if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY){
                        holder.cardView.setCardBackgroundColor(Color.GRAY);
                        selectedPOIC = selectedPOIC + " " + poi.getM_POIC();
                    }else {
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        if (selectedPOIC.contains(" ")) {
                            String replace = " " + String.valueOf(map_num);
                            selectedPOIC = selectedPOIC.replace(replace, "");
                            if (selectedPOIC.length() == selectedPOIC.replace(replace, "").length()){
                                String replace1 = String.valueOf(map_num) + " ";
                                selectedPOIC = selectedPOIC.replace(replace1, "");
                            }
                        }else {
                            resetView();
                        }
                    }
                    //holder.cardView.setCardBackgroundColor(Color.GRAY);
                }else {
                    Intent intent = new Intent(pois.this, singlepoi.class);
                    intent.putExtra("POIC", poi.getM_POIC());
                    pois.this.startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_andupdate).setVisible(false);
        menu.findItem(R.id.add_pois).setVisible(false);
        menu.findItem(R.id.query_poi_map).setVisible(false);
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

    private void resetView(){
        isLongClick = 1;
        setTitle(pois.this.getResources().getText(R.string.POIList));
        refreshCard();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_pois:
                this.finish();
                break;
            case R.id.restore_pois:
                resetView();
                break;
            case R.id.deletepoi:
                /*DataSupport.deleteAll(POI.class, "POIC = ?", selectedPOIC);
                DataSupport.deleteAll(MTAPE.class, "POIC = ?", selectedPOIC);
                DataSupport.deleteAll(MPHOTO.class, "POIC = ?", selectedPOIC);*/
                parseSelectedPOIC();
                resetView();
                break;
            default:
                break;
        }
        return true;
    }

    private void parseSelectedPOIC(){
        if (selectedPOIC.contains(" ")){
            String[] nums = selectedPOIC.split(" ");
            for (int i = 0; i < nums.length; i++){
                DataSupport.deleteAll(POI.class, "POIC = ?", nums[i]);
                DataSupport.deleteAll(MTAPE.class, "POIC = ?", nums[i]);
                DataSupport.deleteAll(MPHOTO.class, "POIC = ?", nums[i]);
            }
        }else {
            DataSupport.deleteAll(POI.class, "POIC = ?", selectedPOIC);
            DataSupport.deleteAll(MTAPE.class, "POIC = ?", selectedPOIC);
            DataSupport.deleteAll(MPHOTO.class, "POIC = ?", selectedPOIC);
        }
    }
}
