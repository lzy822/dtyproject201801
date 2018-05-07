package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
            mTapeobj mtapeobj = new mTapeobj(mtape.getPoic(), mtape.getPoic(), mtape.getTime(), mtape.getPath());
            mTapeobjList.add(mtapeobj);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_tape);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new mTapeobjAdapter(mTapeobjList);
        adapter.setOnItemLongClickListener(new mTapeobjAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String path) {
                setTitle("正在进行长按操作");
                deletePath = path;
                isLongClick = 0;
                invalidateOptionsMenu();
                Log.w(TAG, "onItemLongClick: " + deletePath );
            }
        });
        adapter.setOnItemClickListener(new mTapeobjAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String path, int position) {
                mTapeobjAdapter.ViewHolder holder = new mTapeobjAdapter.ViewHolder(view);
                if (isLongClick == 0){
                    if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY){
                        holder.cardView.setCardBackgroundColor(Color.GRAY);
                        deletePath = deletePath + "wslzy" + path;
                    }else {
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        if (deletePath.contains("wslzy")) {
                            String replace = "wslzy" + path;
                            deletePath = deletePath.replace(replace, "");
                        }else {
                            resetView();
                        }
                    }
                }else {
                    MediaPlayer mediaPlayer = MediaPlayer.create(tapeshow.this, Uri.parse(path));
                    mediaPlayer.start();
                }
                Log.w(TAG, "onItemClick: " + deletePath );
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void resetView(){
        isLongClick = 1;
        setTitle("录音列表");
        refreshCard();
        invalidateOptionsMenu();
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
                resetView();
                break;
            case R.id.deletepoi:
                isLongClick = 1;
                invalidateOptionsMenu();
                //DataSupport.deleteAll(MTAPE.class, "POIC = ?", POIC, "path = ?", deletePath);
                //DataSupport.deleteAll(MTAPE.class, "path = ?", deletePath);
                //DataSupport.deleteAll(MTAPE.class, "POIC = ?", POIC);
                parseSelectedPath();
                setTitle("录音列表");
                refreshCard();
                break;
            case R.id.add_pois:
                try {
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, REQUEST_CODE_TAPE);
                    refreshCard();
                }catch (ActivityNotFoundException e){
                    Toast.makeText(MyApplication.getContext(), "无法打开录音功能", Toast.LENGTH_LONG).show();
                }
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
            Date date = new Date(time);
            poi.updateAll("POIC = ?", POIC);
            MTAPE mtape = new MTAPE();
            mtape.setPath(DataUtil.getRealPathFromUriForAudio(this, uri));
            mtape.setPdfic(POIs.get(0).getIc());
            mtape.setPoic(POIC);
            mtape.setTime(simpleDateFormat.format(date));
            mtape.save();
        }
    }

    private void parseSelectedPath(){
        List<POI> pois = DataSupport.where("POIC = ?", POIC).find(POI.class);
        if (deletePath.contains("wslzy")){
            String[] nums = deletePath.split("wslzy");
            for (int i = 0; i < nums.length; i++){
                Log.w(TAG, "parseSelectedPath: " + nums[i]);
                DataSupport.deleteAll(MTAPE.class, "POIC = ? and path = ?", POIC, nums[i]);
            }
        }else {
            Log.w(TAG, "parseSelectedPath111: " + deletePath);
            DataSupport.deleteAll(MTAPE.class, "POIC = ? and path = ?", POIC, deletePath);
        }
    }

}
