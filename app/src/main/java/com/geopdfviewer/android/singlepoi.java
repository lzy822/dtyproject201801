package com.geopdfviewer.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepoi);
        Intent intent = getIntent();
        POIC = intent.getStringExtra("POIC");
        List<POI> pois = DataSupport.where("POIC = ?", POIC).find(POI.class);
        POI poi = pois.get(0);
        name = poi.getName();
        Log.w(TAG, POIC );
        Log.w(TAG, name );
        Log.w(TAG, POIC.substring(3) );
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
        TextView textView_tapenum = (TextView) findViewById(R.id.txt_tapenumshow);
        textView_tapenum.setText(Integer.toString(poi.getTapenum()));
        TextView textView_loc = (TextView) findViewById(R.id.txt_locshow);
        textView_loc.setText(Float.toString(poi.getX()) + ", " + Float.toString(poi.getY()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
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
                editText_des = (EditText) findViewById(R.id.edit_des);
                editText_name = (EditText) findViewById(R.id.edit_name);
                DataSupport.deleteAll(POI.class, "POIC = ?", POIC);
                DataSupport.deleteAll(MPHOTO.class, "POIC = ?", POIC);
                DataSupport.deleteAll(MTAPE.class, "POIC = ?", POIC);
                this.finish();
                break;
            default:
        }
        return true;
    }
}
