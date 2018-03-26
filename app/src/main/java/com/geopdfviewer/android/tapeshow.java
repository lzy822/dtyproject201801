package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class tapeshow extends AppCompatActivity {
    private static final String TAG = "tapeshow";
    private String POIC;
    private List<mTapeobj> mTapeobjList = new ArrayList<>();
    private mTapeobjAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private String deletePath;
    private final static int REQUEST_CODE_TAPE = 43;
    private int isLongClick = 1;
    Toolbar toolbar;

    private void refreshCard(){
        mTapeobjList.clear();
        List<MTAPE> mtapes = DataSupport.where("POIC = ?", POIC).find(MTAPE.class);
        for (MTAPE mtape : mtapes){
            mTapeobj mtapeobj = new mTapeobj(mtape.getPOIC(), mtape.getPath(), mtape.getTime(), mtape.getPath());
            mTapeobjList.add(mtapeobj);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_tape);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new mTapeobjAdapter(mTapeobjList);
        adapter.setOnItemLongClickListener(new mTapeobjAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String path) {
                deletePath = path;
                isLongClick = 0;
                invalidateOptionsMenu();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tapeshow);
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);
        setTitle("录音列表");
        Intent intent = getIntent();
        POIC = intent.getStringExtra("POIC");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCard();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        switch (isLongClick){
            case 1:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.deletepoi).setVisible(false);
                menu.findItem(R.id.restore_pois).setVisible(false);
                break;
            case 0:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.back_pois).setVisible(false);
                menu.findItem(R.id.deletepoi).setVisible(true);
                menu.findItem(R.id.restore_pois).setVisible(true);
                menu.findItem(R.id.add_pois).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_andupdate).setVisible(false);
        return true;
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
                invalidateOptionsMenu();
                DataSupport.deleteAll(MTAPE.class, "POIC = ?", POIC, "path = ?", deletePath);
                refreshCard();
                break;
            case R.id.add_pois:
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(intent, REQUEST_CODE_TAPE);
                refreshCard();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TAPE){
            Uri uri = data.getData();
            long time = System.currentTimeMillis();
            List<POI> POIs = DataSupport.where("POIC = ?", POIC).find(POI.class);
            POI poi = new POI();
            poi.setTapenum(POIs.get(0).getTapenum() + 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            poi.updateAll("POIC = ?", POIC);
            MTAPE mtape = new MTAPE();
            mtape.setPath(getRealPathFromUri(this, uri));
            mtape.setPdfic(POIs.get(0).getIc());
            mtape.setPOIC(POIC);
            mtape.setTime(simpleDateFormat.format(date));
            mtape.save();
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Audio.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
