package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.List;

public class plqpoishow extends AppCompatActivity {
    private static final String TAG = "plqpoishow";
    private String xh, dmbzbzmc, dmbzmc, xzqdm, gg, sccj, szdw, xzq, zp, yp;
    private float lat, longi;
    private TextView name_show;
    private TextView xzqdm_show;
    private TextView bzmc_show;
    private TextView szxzq_show;
    private TextView szdw_show;
    private TextView sccj_show;
    private TextView gg_show;
    private TextView loc_show;
    private ImageView zp_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plqpoishow);
        //声明ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_plq);
        setSupportActionBar(toolbar);
        setTitle("地名标志信息");
        Intent intent = getIntent();
        xh = intent.getStringExtra("xh");
        List<kmltest> kmltests = LitePal.where("xh = ?", xh).find(kmltest.class);
        List<plqzp> plqzps = LitePal.where("xh = ?", xh).find(plqzp.class);
        List<plqyp> plqyps = LitePal.where("xh = ?", xh).find(plqyp.class);
        dmbzbzmc = kmltests.get(0).getDmbzbzmc();
        dmbzmc = kmltests.get(0).getDmbzmc();
        xzqdm = kmltests.get(0).getDmszxzqdm();
        gg = kmltests.get(0).getGg();
        lat = kmltests.get(0).getLat();
        longi = kmltests.get(0).getLongi();
        sccj = kmltests.get(0).getSccj();
        szdw = kmltests.get(0).getSzdw();
        xzq = kmltests.get(0).getSzxzq();
        zp = plqzps.get(0).getZp1();
        yp = plqyps.get(0).getYp();
        Log.w(TAG, "xhhh : " + xh);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void refresh(){
        List<kmltest> pois = LitePal.findAll(kmltest.class);
        int size = pois.size();
        int num = 0;
        for (int i = 0; i < size ; i++){
            if (xh.equals(pois.get(i).getXh())) num = i;
        }
        final List<plqyp> tapes = LitePal.where("xh = ?", xh).find(plqyp.class);
        List<plqzp> photos = LitePal.where("xh = ?", xh).find(plqzp.class);
        Log.w(TAG, "xhhh : poi" + pois.size());
        Log.w(TAG, "xhhh : tapes" + tapes.size());
        Log.w(TAG, "xhhh : photos" + dmbzbzmc);
        name_show = (TextView) findViewById(R.id.name_plq_show);
        name_show.setText(dmbzbzmc);
        xzqdm_show = (TextView) findViewById(R.id.xzqdm_plq_show);
        xzqdm_show.setText(xzqdm);
        bzmc_show = (TextView) findViewById(R.id.bzmc_plq_show);
        bzmc_show.setText(dmbzmc);
        szxzq_show = (TextView) findViewById(R.id.xzqmc_plq_show);
        szxzq_show.setText(xzq);
        szdw_show = (TextView) findViewById(R.id.szdw_plq_show);
        szdw_show.setText(szdw);
        sccj_show = (TextView) findViewById(R.id.sccj_plq_show);
        sccj_show.setText(sccj);
        gg_show = (TextView) findViewById(R.id.gg_plq_show);
        gg_show.setText(gg);
        loc_show = (TextView) findViewById(R.id.loc_plq_show);
        loc_show.setText(Float.toString(lat) + " , " + Float.toString(longi));
        Bitmap bitmap = BitmapFactory.decodeFile(zp);
        int degree = DataUtil.getPicRotate(zp);
        if (degree != 0) {
            Matrix m = new Matrix();
            m.setRotate(degree); // 旋转angle度
            Log.w(TAG, "showPopueWindowForPhoto: " + degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }
        zp_show = (ImageView) findViewById(R.id.zp_plq_show);
        zp_show.setImageBitmap(bitmap);
        zp_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MediaPlayer mediaPlayer = MediaPlayer.create(plqpoishow.this, Uri.parse(yp));
                    mediaPlayer.start();
                }catch (NullPointerException e){
                    Toast.makeText(plqpoishow.this, R.string.NoTapeTip, Toast.LENGTH_LONG).show();
                }
            }
        });



        /*name_show.setText(pois.get(num).getDmbzmc());
        xzqdm_show.setText(pois.get(num).getDmszxzqdm());
        bzmc_show.setText(pois.get(num).getDmbzbzmc());
        szxzq_show.setText(pois.get(num).getSzxzq());
        szdw_show.setText(pois.get(num).getSzdw());
        sccj_show.setText(pois.get(num).getSccj());
        gg_show.setText(pois.get(num).getGg());
        loc_show.setText(pois.get(num).getLat() + " , " + pois.get(num).getLongi());
        Bitmap bitmap = BitmapFactory.decodeFile(photos.get(num).getZp1());
        int degree = DataUtil.getPicRotate(photos.get(num).getZp1());
        if (degree != 0) {
            Matrix m = new Matrix();
            m.setRotate(degree); // 旋转angle度
            Log.w(TAG, "showPopueWindowForPhoto: " + degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }
        zp_show.setImageBitmap(bitmap);


        final MediaPlayer mediaPlayer = MediaPlayer.create(plqpoishow.this, Uri.parse(tapes.get(num).getYp()));
        yp_show.setMediaPlayer(new MediaController.MediaPlayerControl() {
            @Override
            public void start() {
                mediaPlayer.start();
            }

            @Override
            public void pause() {
                mediaPlayer.pause();
            }

            @Override
            public int getDuration() {
                return 0;
            }

            @Override
            public int getCurrentPosition() {
                return 0;
            }

            @Override
            public void seekTo(int pos) {

            }

            @Override
            public boolean isPlaying() {
                return false;
            }

            @Override
            public int getBufferPercentage() {
                return 0;
            }

            @Override
            public boolean canPause() {
                return false;
            }

            @Override
            public boolean canSeekBackward() {
                return false;
            }

            @Override
            public boolean canSeekForward() {
                return false;
            }

            @Override
            public int getAudioSessionId() {
                return 0;
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_pois).setVisible(false);
        menu.findItem(R.id.restore_pois).setVisible(false);
        menu.findItem(R.id.add_pois).setVisible(false);
        menu.findItem(R.id.deletepoi).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.query_poi_map:
                SharedPreferences.Editor editor = getSharedPreferences("update_query_attr_to_map", MODE_PRIVATE).edit();
                editor.putString("poic", xh);
                editor.apply();
                this.finish();
                break;
            case R.id.back_andupdate:
                this.finish();
                break;
            default:
        }
        return true;
    }
}
