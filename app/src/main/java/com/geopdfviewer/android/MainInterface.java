package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.model.PagePart;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainInterface extends AppCompatActivity  implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {
    private static final String TAG = "MainInterface";
    //public static final String SAMPLE_FILE = "pdf/sample1.pdf";
    public static final String SAMPLE_FILE = "pdf/cangyuan.pdf";
    Integer pageNumber = 0;
    public String content;
    public int num_line = 0;
    String pdfFileName;
    public boolean isESRI = false;
    String info;
    PDFView pdfView;
    LinearLayout linearLayout;
    /*public final static String name = "";
    public final static String WKT = "";
    public final static String uri = "";
    public final static String GPTS = "";
    public final static String BBox = "";*/
    TextView textView;

    public   String WKT = "";
    public   String uri = "";
    public   String GPTS = "";
    public   String BBox = "";

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    public void WKTFormat() {

    }



    public void ReadPDF(){
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfFileName = SAMPLE_FILE;
        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

                    }
                })
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();



    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public void getInfo(int num) {
        SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
        String str = "n_" + num + "_";
        pdfFileName = pref1.getString(str + "name", "");
        WKT = pref1.getString(str + "WKT", "");
        uri = pref1.getString(str + "uri", "");
        GPTS = pref1.getString(str + "GPTS", "");
        BBox = pref1.getString(str + "BBox", "");
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);
        Log.w(TAG, uri.toString() );

        pdfView = (PDFView) findViewById(R.id.pdfView);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

                    }
                })
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {

                    }
                })
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();

        //Log.w(TAG, uri.toString() );
        //Log.w(TAG, pdfFileName );
        setTitle(pdfFileName);
    }
    Float x, y;
    private void displayFromFile(String filePath) {
        setTitle(pdfFileName);

        pdfView = (PDFView) findViewById(R.id.pdfView);

        File file = new File(filePath);
        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        canvas.drawLine(0, 0, 100, 100, new Paint(1));
                    }
                })
                .onRender(new OnRenderListener() {
                    @Override
                    public void onInitiallyRendered(int nbPages) {

                    }
                })
                .onTap(new OnTapListener() {
                    @Override
                    public boolean onTap(MotionEvent e) {
                        textView = (TextView) findViewById(R.id.txt);
                        textView.setText(Float.toString(e.getX()) + "&" + Float.toString(e.getY()) + "&" + Float.toString(pdfView.getCurrentXOffset()));

                        return true;
                    }
                })
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    public void getInfo(){

    }

    public void locError(String str){
        Log.e(TAG, str );
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);




        Intent intent = getIntent();
        int m_num = intent.getIntExtra("num", 0);
        getInfo(m_num);
        /*String m_name = intent.getStringExtra(name);

        String m_WKT = intent.getStringExtra(WKT);
        String m_uri = intent.getStringExtra(uri);
        String m_GPTS = intent.getStringExtra(GPTS);
        String m_BBox = intent.getStringExtra(BBox);
        locError(m_name);
        locError(m_WKT);
        locError(m_uri);
        locError(m_GPTS);
        locError(m_BBox);*/
        //int num = intent.getIntExtra("num", 0);
        //setTitle(name);
        //locError(uri);
        linearLayout = (LinearLayout) findViewById(R.id.search);
        //displayFromUri(Uri.parse(uri));
        displayFromFile(uri);
        ;
        Button bt1 = (Button) findViewById(R.id.send);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                linearLayout.setVisibility(View.GONE);
            }
        });
        Button bt2 = (Button) findViewById(R.id.cancel);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                linearLayout.setVisibility(View.GONE);
            }
        });




        //final FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fam);
        com.getbase.floatingactionbutton.FloatingActionButton button1 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.measure);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textView.setText("开始测量!");
                //Log.e(TAG, "onClick: " );
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton button2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.north);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textView.setText("指北针!");
                //pdfView.resetZoom();
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton button3 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.restorezoom);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textView.setText("zoom!");
                pdfView.resetZoomWithAnimation();
            }
        });

        }

    @Override
    public void onPageChanged(int page, int pageCount) {
        //pageNumber = page;
        //setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public void save(String str){
        info = str;
        String fname = "data_" + findTitle(pdfFileName);
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput(fname, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(str);
            } catch (IOException e) {
            e.printStackTrace();
            } finally {
            try {
                if (writer != null) {
                    writer.close();
                    }} catch (IOException ee) {
                ee.printStackTrace();
                    }

            }
        }

    public String load(String name) {
        String fname = "data_" + name;
            FileInputStream in = null;
            BufferedReader reader = null;
            StringBuilder content = new StringBuilder();
            try {
                in = openFileInput(fname);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return content.toString();
        }

    public String findTitle(String str){
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                this.finish();
                break;
            case R.id.info:
                Intent intent = new Intent(MainInterface.this, info_page.class);
                intent.putExtra("extra_data", WKT);
                startActivity(intent);
                break;
            case R.id.query:
                linearLayout = (LinearLayout) findViewById(R.id.search);
                linearLayout.setVisibility(View.VISIBLE);
            default:
        }
        return true;
    }
}



