package com.geopdfviewer.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class JZActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener, OnTapListener {
    private static final String TAG = "JZActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jz);
        initWidget();

        cacheMaps();
        showMap();
    }

    public void initWidget() {
        try {
            com.github.clans.fab.FloatingActionMenu floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam1);
            floatingActionsMenu.setClosedOnTouchOutside(true);
            FloatingActionButton addTape = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addTape1);
            addTape.setImageResource(R.drawable.ic_sound);
            addTape.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            FloatingActionButton addPhoto = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addPhoto1);
            addPhoto.setImageResource(R.drawable.ic_add_a_photo);
            addPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            FloatingActionButton restoreZoom_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.restoreZoom1);
            restoreZoom_fab.setImageResource(R.drawable.ic_autorenew);
            restoreZoom_fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            FloatingActionButton messureDistance_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.messureDistance1);
            messureDistance_fab.setImageResource(R.drawable.ic_straighten);
            messureDistance_fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            FloatingActionButton addMap_jzactivity = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addMap_jzactivity);
            addMap_jzactivity.setImageResource(R.drawable.ic_map_black);
            addMap_jzactivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            setStatusBarColor(this);
        }
        catch (Exception e) {
            Log.w(TAG, "initWidget: " + e.toString());
        }
    }

    public void addMap(){

    }

    public void cacheMaps(){

    }

    public void showMap(){
        displayFromFile("/storage/emulated/0/tencent/TIMfile_recv/cangyuan.dt");
    }

    @TargetApi(21)
    public static void setStatusBarColor(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.BLACK);
    }
    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }

    @Override
    public boolean onTap(MotionEvent e) {
        return false;
    }

    private void displayFromFile(String filePath) {
        PDFView pdfView;
        pdfView = (PDFView) findViewById(R.id.pdfView1);
        pdfView.setBackgroundColor(Color.WHITE);
        pdfView.setMidZoom(10);
        pdfView.setMaxZoom(20);
        final File file = new File(filePath);
        pdfView.fromFile(file)
                .password("123123123")
                .enableSwipe(false)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .onLoad(this)
                .onDraw(this)
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {

                    }
                })
                .onTap(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
        /*title = pdfFileName;
        toolbar.setTitle(pdfFileName);
        locError("filePath: " + filePath);
        locError("pdfFileName: " + pdfFileName);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isRomance = true;
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 2500);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
