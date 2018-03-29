package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

public class photoshow extends AppCompatActivity {

    private static final String TAG = "photoshow";
    private String POIC;
    private List<mPhotobj> mPhotobjList = new ArrayList<>();
    private mPhotobjAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private String deletePath;
    private final static int REQUEST_CODE_PHOTO = 42;
    private int isLongClick = 1;
    Toolbar toolbar;

    private void refreshCard(){
        mPhotobjList.clear();
        List<MPHOTO> mphotos = DataSupport.where("POIC = ?", POIC).find(MPHOTO.class);
        for (MPHOTO mphoto : mphotos){
            mPhotobj mphotobj = new mPhotobj(mphoto.getPOIC(), mphoto.getPOIC(), mphoto.getTime(), mphoto.getPath());
            mPhotobjList.add(mphotobj);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_photo);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new mPhotobjAdapter(mPhotobjList);
        adapter.setOnItemLongClickListener(new mPhotobjAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String path) {
                setTitle("正在进行长按操作");
                deletePath = path;
                isLongClick = 0;
                invalidateOptionsMenu();
            }
        });
        adapter.setOnItemClickListener(new mPhotobjAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String path, int position) {
                mPhotobjAdapter.ViewHolder holder = new mPhotobjAdapter.ViewHolder(view);
                if (isLongClick == 0){
                    if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY){
                        holder.cardView.setCardBackgroundColor(Color.GRAY);
                        deletePath = deletePath + "wslzy" + path;
                    }else {
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        if (deletePath.contains("wslzy")) {
                            String replace = "wslzy" + path;
                            deletePath = deletePath.replace(replace, "");
                            if (deletePath.length() == deletePath.replace(replace, "").length()){
                                String replace1 = path + "wslzy";
                                deletePath = deletePath.replace(replace1, "");
                            }
                        }else {
                            resetView();
                        }
                    }
                }else {

                }
            }
        });
        recyclerView.setAdapter(adapter);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoshow);
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        setTitle("相片列表");
        Intent intent = getIntent();
        POIC = intent.getStringExtra("POIC");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_andupdate).setVisible(false);
        return true;
    }

    private void resetView(){
        isLongClick = 1;
        setTitle("相片列表");
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
                isLongClick = 1;
                setTitle("相片列表");
                invalidateOptionsMenu();
                //DataSupport.deleteAll(MPHOTO.class, "POIC = ?", POIC, "path = ?", deletePath);
                parseSelectedPath();
                refreshCard();
                break;
            case R.id.add_pois:
                launchPicker();
                refreshCard();
                break;
            default:
                break;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            Uri uri = data.getData();
            List<POI> POIs = DataSupport.where("POIC = ?", POIC).find(POI.class);
            POI poi = new POI();
            long time = System.currentTimeMillis();
            poi.setPhotonum(POIs.get(0).getPhotonum() + 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            poi.updateAll("POIC = ?", POIC);
            MPHOTO mphoto = new MPHOTO();
            mphoto.setPdfic(POIs.get(0).getIc());
            mphoto.setPOIC(POIC);
            mphoto.setPath(getRealPath(uri.getPath()));
            mphoto.setTime(simpleDateFormat.format(date));
            mphoto.save();
        }
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_CODE_PHOTO);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    //获取File可使用路径
    public String getRealPath(String filePath) {
        if (!filePath.contains("raw")) {
            String str = "/external_files";
            String Dir = Environment.getExternalStorageDirectory().toString();
            filePath = Dir + filePath.substring(str.length());
        }else {
            filePath = filePath.substring(5);
        }
        return filePath;
    }

    private void parseSelectedPath(){
        if (deletePath.contains("wslzy")){
            String[] nums = deletePath.split("wslzy");
            Log.w(TAG, "parseSelectedPath: " + nums[0] );
            for (int i = 0; i < nums.length; i++){
                DataSupport.deleteAll(MPHOTO.class, "POIC = ? and path = ?", POIC, nums[i]);
            }
        }else {
            DataSupport.deleteAll(MPHOTO.class, "POIC = ? and path = ?", POIC, deletePath);
        }
    }

}
