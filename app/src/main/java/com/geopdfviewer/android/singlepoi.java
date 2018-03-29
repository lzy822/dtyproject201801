package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

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
        List<POI> pois = DataSupport.where("POIC = ?", POIC).find(POI.class);
        List<MTAPE> tapes = DataSupport.where("POIC = ?", POIC).find(MTAPE.class);
        List<MPHOTO> photos = DataSupport.where("POIC = ?", POIC).find(MPHOTO.class);
        POI poi = pois.get(0);
        poi.setPhotonum(photos.size());
        poi.setTapenum(tapes.size());
        poi.updateAll("POIC = ?", POIC);
        name = poi.getName();
        editText_name = (EditText) findViewById(R.id.edit_name);
        editText_name.setText(name);
        editText_des = (EditText) findViewById(R.id.edit_des);
        if (poi.getDescription() != null) {
            editText_des.setText(poi.getDescription());
        }else editText_des.setText("");
        TextView textView_time = (TextView) findViewById(R.id.txt_timeshow);
        textView_time.setText(poi.getTime());
        Log.w(TAG, Integer.toString(poi.getTapenum()));
        TextView textView_photonum = (TextView) findViewById(R.id.txt_photonumshow);
        textView_photonum.setText(Integer.toString(poi.getPhotonum()));
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
        textView_tapenum.setText(Integer.toString(poi.getTapenum()));
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
        textView_loc.setText(Float.toString(poi.getX()) + ", " + Float.toString(poi.getY()));
        ImageButton addphoto = (ImageButton)findViewById(R.id.addPhoto_singlepoi);
        addphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPicker();
            }
        });
        ImageButton addtape = (ImageButton)findViewById(R.id.addTape_singlepoi);
        addtape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(intent, REQUEST_CODE_TAPE);
            }
        });
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
            case R.id.back_andupdate:
                editText_des = (EditText) findViewById(R.id.edit_des);
                editText_name = (EditText) findViewById(R.id.edit_name);
                POI poi = new POI();
                poi.setName(editText_name.getText().toString());
                poi.setDescription(editText_des.getText().toString());
                poi.updateAll("POIC = ?", POIC);
                this.finish();
                break;
            case R.id.deletepoi:
                DataSupport.deleteAll(POI.class, "POIC = ?", POIC);
                DataSupport.deleteAll(MPHOTO.class, "POIC = ?", POIC);
                DataSupport.deleteAll(MTAPE.class, "POIC = ?", POIC);
                this.finish();
                break;
            default:
        }
        return true;
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
