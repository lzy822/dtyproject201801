package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class singlepoi extends AppCompatActivity {

    private static final String TAG = "singlepoi";
    private String POIC;
    private String name;
    private EditText editText_name;
    private EditText editText_des;
    private final static int REQUEST_CODE_PHOTO = 42;
    private final static int REQUEST_CODE_TAPE = 43;
    private final static int TAKE_PHOTO = 41;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepoi);
        //声明ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("兴趣点信息");
        Intent intent = getIntent();
        POIC = intent.getStringExtra("POIC");


    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh(){
        List<POI> pois = DataSupport.where("poic = ?", POIC).find(POI.class);
        List<MTAPE> tapes = DataSupport.where("poic = ?", POIC).find(MTAPE.class);
        List<MPHOTO> photos = DataSupport.where("poic = ?", POIC).find(MPHOTO.class);
        Log.w(TAG, "pois: " + pois.size() + "\n");
        Log.w(TAG, "tapes1: " + pois.get(0).getTapenum() + "\n");
        Log.w(TAG, "photos1: " + pois.get(0).getPhotonum() + "\n");
        Log.w(TAG, "tapes: " + tapes.size() + "\n");
        Log.w(TAG, "photos: " + photos.size());
        POI poi1 = new POI();
        if (tapes.size() != 0) poi1.setTapenum(tapes.size());
        else poi1.setToDefault("tapenum");
        if (photos.size() != 0) poi1.setPhotonum(photos.size());
        else poi1.setToDefault("photonum");
        poi1.updateAll("poic = ?", POIC);
        Log.w(TAG, "refresh: " + poi1.updateAll("poic = ?", POIC));
        /*POI poi = new POI();
        poi.setPhotonum(photos.size());
        poi.setTapenum(tapes.size());
        poi.updateAll("POIC = ?", POIC);*/
        List<POI> pois1 = DataSupport.where("poic = ?", POIC).find(POI.class);
        Log.w(TAG, "tapes2: " + pois1.get(0).getTapenum() + "\n");
        Log.w(TAG, "photos2: " + pois1.get(0).getPhotonum() + "\n");
        POI poi = pois.get(0);
        name = poi.getName();
        editText_name = (EditText) findViewById(R.id.edit_name);
        editText_name.setText(name);
        editText_des = (EditText) findViewById(R.id.edit_des);
        if (poi.getDescription() != null) {
            editText_des.setText(poi.getDescription());
        }else editText_des.setText("");
        TextView textView_time = (TextView) findViewById(R.id.txt_timeshow);
        textView_time.setText(poi.getTime());
        Log.w(TAG, Integer.toString(tapes.size()));
        TextView textView_photonum = (TextView) findViewById(R.id.txt_photonumshow);
        textView_photonum.setText(Integer.toString(photos.size()));
        textView_photonum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开图片列表
                Intent intent1 = new Intent(singlepoi.this, photoshow.class);
                intent1.putExtra("POIC", POIC);
                startActivity(intent1);
            }
        });
        TextView textView_tapenum = (TextView) findViewById(R.id.txt_tapenumshow);
        textView_tapenum.setText(Integer.toString(tapes.size()));
        textView_tapenum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开录音列表
                Intent intent2 = new Intent(singlepoi.this, tapeshow.class);
                intent2.putExtra("POIC", POIC);
                startActivity(intent2);
            }
        });
        TextView textView_loc = (TextView) findViewById(R.id.txt_locshow);
        DecimalFormat df = new DecimalFormat("0.0000");
        textView_loc.setText(df.format(poi.getX()) + ", " + df.format(poi.getY()));
        ImageButton addphoto = (ImageButton)findViewById(R.id.addPhoto_singlepoi);
        addphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopueWindowForPhoto();
            }
        });
        ImageButton addtape = (ImageButton)findViewById(R.id.addTape_singlepoi);
        addtape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, REQUEST_CODE_TAPE);
                }catch (ActivityNotFoundException e){
                    Toast.makeText(MyApplication.getContext(), "无法打开录音功能", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void showPopueWindowForPhoto(){
        View popView = View.inflate(this,R.layout.popupwindow_camera_need,null);
        Button bt_album = (Button) popView.findViewById(R.id.btn_pop_album);
        Button bt_camera = (Button) popView.findViewById(R.id.btn_pop_camera);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 1/3;

        final PopupWindow popupWindow = new PopupWindow(popView, weight ,height);
        //popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
        //点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);

        bt_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPicker();
                popupWindow.dismiss();

            }
        });
        bt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
                popupWindow.dismiss();

            }
        });
        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

            }
        });
        //popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);

    }

    private void takePhoto(){
        File file2 = new File(Environment.getExternalStorageDirectory() + "/photoForTuZhi");
        if (!file2.exists() && !file2.isDirectory()){
            file2.mkdirs();
        }
        long timenow = System.currentTimeMillis();
        File outputImage = new File(Environment.getExternalStorageDirectory() + "/photoForTuZhi", Long.toString(timenow) + ".jpg");
        try {
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24){
            //locError(Environment.getExternalStorageDirectory() + "/maphoto/" + Long.toString(timenow) + ".jpg");
            imageUri = FileProvider.getUriForFile(singlepoi.this, "com.android.tuzhi.fileprovider", outputImage);

        }else imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            Uri uri = data.getData();
            List<POI> POIs = DataSupport.where("poic = ?", POIC).find(POI.class);
            POI poi = new POI();
            long time = System.currentTimeMillis();
            poi.setPhotonum(POIs.get(0).getPhotonum() + 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(singlepoi.this.getResources().getText(R.string.DateAndTime).toString());
            Date date = new Date(System.currentTimeMillis());
            poi.updateAll("poic = ?", POIC);
            MPHOTO mphoto = new MPHOTO();
            mphoto.setPdfic(POIs.get(0).getIc());
            mphoto.setPoic(POIC);
            //mphoto.setPath(getRealPath(uri.getPath()));
            mphoto.setPath(DataUtil.getRealPathFromUriForPhoto(this, uri));
            mphoto.setTime(simpleDateFormat.format(date));
            mphoto.save();
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TAPE){
            Uri uri = data.getData();
            //long time = System.currentTimeMillis();
            List<POI> POIs = DataSupport.where("poic = ?", POIC).find(POI.class);
            POI poi = new POI();
            poi.setTapenum(POIs.get(0).getTapenum() + 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(singlepoi.this.getResources().getText(R.string.DateAndTime).toString());
            Date date = new Date(System.currentTimeMillis());
            poi.updateAll("poic = ?", POIC);
            MTAPE mtape = new MTAPE();
            mtape.setPath(DataUtil.getRealPathFromUriForAudio(this, uri));
            mtape.setPdfic(POIs.get(0).getIc());
            mtape.setPoic(POIC);
            mtape.setTime(simpleDateFormat.format(date));
            mtape.save();
        }
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO) {
            String imageuri = DataUtil.getRealPath(imageUri.toString());
            File file = new File(imageuri);
            if (file.length() != 0) {
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), imageuri, "title", "description");
                    // 最后通知图库更新
                    singlepoi.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imageuri)));
                }catch (IOException e){
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(singlepoi.this.getResources().getText(R.string.DateAndTime).toString());
                Date date = new Date(System.currentTimeMillis());
                List<POI> POIs = DataSupport.where("poic = ?", POIC).find(POI.class);
                POI poi = new POI();
                long time = System.currentTimeMillis();
                poi.setPhotonum(POIs.get(0).getPhotonum() + 1);
                poi.updateAll("poic = ?", POIC);
                MPHOTO mphoto = new MPHOTO();
                mphoto.setPoic(POIC);
                mphoto.setPath(imageuri);
                mphoto.setTime(simpleDateFormat.format(date));
                mphoto.save();
            }else {
                file.delete();
                Toast.makeText(singlepoi.this, "拍照失败, 请再次拍摄", Toast.LENGTH_LONG).show();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_pois).setVisible(false);
        menu.findItem(R.id.restore_pois).setVisible(false);
        menu.findItem(R.id.add_pois).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.query_poi_map:
                SharedPreferences.Editor editor = getSharedPreferences("update_query_attr_to_map", MODE_PRIVATE).edit();
                editor.putString("poic", POIC);
                editor.apply();
                this.finish();
                break;
            case R.id.back_andupdate:
                editText_des = (EditText) findViewById(R.id.edit_des);
                editText_name = (EditText) findViewById(R.id.edit_name);
                POI poi = new POI();
                poi.setName(editText_name.getText().toString());
                poi.setDescription(editText_des.getText().toString());
                poi.updateAll("poic = ?", POIC);
                this.finish();
                break;
            case R.id.deletepoi:
                DataSupport.deleteAll(POI.class, "poic = ?", POIC);
                DataSupport.deleteAll(MPHOTO.class, "poic = ?", POIC);
                DataSupport.deleteAll(MTAPE.class, "poic = ?", POIC);
                this.finish();
                break;
            default:
        }
        return true;
    }
}
