package com.geopdfviewer.android;

import android.Manifest;
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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfRenderer;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.List;

public class MainInterface extends AppCompatActivity  implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener {
    private static final String TAG = "MainInterface";
    public static final String SAMPLE_FILE = "pdf/cangyuan.pdf";
    public static int FILE_TYPE = 0;
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


    //坐标信息
    double m_lat,m_long;
    //获取pdf的坐标信息
    double min_lat, max_lat, min_long, max_long;
    //坐标精度
    int definition;
    //获取pdf BBOX信息
    double bottom_x, bottom_y, top_x, top_y;

    Location location;

    private LocationManager locationManager;

    protected final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "Location changed to: " + getLocationInfo(location));
            updateView(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged() called with " + "provider = [" + provider + "], status = [" + status + "], extras = [" + extras + "]");
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled() called with " + "provider = [" + provider + "]");
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                Log.d(TAG, "onProviderDisabled.location = " + location);
                updateView(location);
            }catch (SecurityException e){

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled() called with " + "provider = [" + provider + "]");
        }
    };

    private void updateView(Location location) {

        Geocoder gc = new Geocoder(this);
        List<Address> addresses = null;
        String msg = "";
        Log.d(TAG, "updateView.location = " + location);
        if (location != null) {
            try {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.d(TAG, "updateView.addresses = " + addresses);
                if (addresses.size() > 0) {
                    msg += addresses.get(0).getAdminArea().substring(0,2);
                    msg += " " + addresses.get(0).getLocality().substring(0,2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //DecimalFormat df = new DecimalFormat("0.00000");
            //setLocationInfo(df.format(location.getLatitude()), df.format(location.getLongitude()));
            //setLocationInfo(location.getLatitude(), location.getLongitude());


            m_lat = location.getLatitude();
            m_long = location.getLongitude();
            //m_lat = 25.029896449028467;
            //m_long = 102.7012173402311;
            /*pdfView = (PDFView) findViewById(R.id.pdfView);
            Rect rect = new Rect(200, 300, 400, 100);
            Canvas canvas = new Canvas(rect);
            pdfView.draw();*/
            //locError("看这里!!!");
            //Log.w(TAG, location.toString() );
            locError(Double.toString(m_lat) + "&&" + Double.toString(m_long) + "Come here");

        } else {

        }
    }

    private void setLocationInfo(double lat, double longt){
        String format = "0.";
        for (int i = 0 ; i < definition ; i++){
            format = format + "0";
            //format.
        }
        DecimalFormat df = new DecimalFormat(format);
        Log.w(TAG, format );
        locError(Double.toString(lat));
        m_lat = Double.valueOf(df.format(lat));
        m_long = Double.valueOf(df.format(longt));
        textView = (TextView) findViewById(R.id.txt) ;
        textView.setText( df.format(lat)+ "$$" + df.format(longt));
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        //Log.w(TAG, Float.toString(pdfView.getZoom()) );
        //pdfView.moveRelativeTo(500, 300);
        canvas.drawLine(0, pageHeight, pageWidth, 0,  new Paint(1));
        //canvas.drawLine(30, 1177, 826, 35,  new Paint(1));
        //canvas.drawLine((float) bottom_x, (float)bottom_y, (float)top_x, (float)top_y, new Paint(1));

        //locError(Double.toString(bottom_x));
    }

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
        Log.w(TAG, "BBox : " + BBox );
        Log.w(TAG, "GPTS : " + GPTS );
        //GPTSList = new double[8];
        getGPTS();
        getBBox();

    }
private double w, h, w_min;
    private void getGPTS() {
        String[] GPTString = GPTS.split(" ");

        String m_definition = GPTString[0].substring(GPTString[0].indexOf(".") + 1);
        //locError(Double.toString());
        //Double.valueOf(GPTString[3]) - Double.valueOf(GPTString[5])
        locError("see here");
        //locError(Double.toString());
        //Double.valueOf(GPTString[7]) - Double.valueOf(GPTString[1])
        w = ((Double.valueOf(GPTString[3]) - Double.valueOf(GPTString[5])) + (Double.valueOf(GPTString[7]) - Double.valueOf(GPTString[1]))) / 2;
        h = ((Double.valueOf(GPTString[4]) - Double.valueOf(GPTString[2])) + (Double.valueOf(GPTString[0]) - Double.valueOf(GPTString[6]))) / 2;
        w_min = ((Double.valueOf(GPTString[1])) + (Double.valueOf(GPTString[5]))) / 2;
        //locError(Double.toString());
        //Double.valueOf(GPTString[4]) - Double.valueOf(GPTString[2])
        locError("see here");
        //locError(Double.toString());
        //Double.valueOf(GPTString[0]) - Double.valueOf(GPTString[6])

        definition = m_definition.length();
        min_lat = Double.valueOf(GPTString[0]);
        max_lat = Double.valueOf(GPTString[0]);
        min_long = Double.valueOf(GPTString[1]);
        max_long = Double.valueOf(GPTString[1]);
        for (int i = 0; i < GPTString.length; i = i + 2){
            if (Double.valueOf(GPTString[i]) > max_lat) max_lat = Double.valueOf(GPTString[i]);
            else if (Double.valueOf(GPTString[i]) < min_lat) min_lat = Double.valueOf(GPTString[i]);
        }
        for (int i = 1; i < GPTString.length; i = i + 2){
            if (Double.valueOf(GPTString[i]) > max_long) max_long = Double.valueOf(GPTString[i]);
            else if (Double.valueOf(GPTString[i]) < min_long) min_long = Double.valueOf(GPTString[i]);
        }
        /*Log.w(TAG, "min_lat : " + Double.toString(min_lat) );
        Log.w(TAG, "max_lat : " + Double.toString(max_lat) );
        Log.w(TAG, "min_long : " + Double.toString(min_long) );
        Log.w(TAG, "max_long : " + Double.toString(max_long) );
        Log.w(TAG, "pdf精度为: " + Integer.toString(definition) );*/

        getLocation();
        //Log.w(TAG, m_definition );
    }

    private void getBBox() {
        String[] BBoxString = BBox.split(" ");
        bottom_x = Double.valueOf(BBoxString[0]);
        bottom_y = Double.valueOf(BBoxString[1]);
        top_x = Double.valueOf(BBoxString[2]);
        top_y = Double.valueOf(BBoxString[3]);

    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .onTap(new OnTapListener() {
                    @Override
                    public boolean onTap(MotionEvent e) {
                        return false;
                    }
                })
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

                    }
                })
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        setTitle(pdfFileName);
    }
    Float x, y;
    private void displayFromFile(String filePath) {
        setTitle(pdfFileName);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        final File file = new File(filePath);
        pdfView.fromFile(file)
                .enableSwipe(false)
                .enableDoubletap(false)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(false)
                .onLoad(this)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStrokeWidth((float)3.0);
                        paint.setStyle(Paint.Style.FILL);
                        Paint paint1 = new Paint();
                        paint1.setColor(Color.GREEN);
                        paint1.setStrokeWidth((float)3.0);
                        paint1.setStyle(Paint.Style.FILL);
                        float xss = (float) m_lat;
                        float yss = (float) m_long;
                        //locError(Double.toString(m_lat) + "&&" + Double.toString(m_long));
                        //locError(Float.toString(xss) + "&&" + Float.toString(yss));
                        double y_ratio = ((m_lat - min_lat) / (max_lat - min_lat));
                        //double x_ratio = ((m_long - min_long) / (max_long - min_long));
                        double x_ratio = ((m_long - w_min) / w);
                        //double x_ratio = ((m_long - 102.61422) / 0.194225);
                        //double y_ratio = ((m_lat - 24.90466) / 0.25781);
                        locError(Double.toString(m_long));
                        locError(Double.toString(w_min));
                        double yy_ratio = ((max_lat - min_lat) / pageHeight);
                        double xx_ratio = ((max_long - min_long) / pageWidth);
                        if (xx_ratio > yy_ratio) {
                            yy_ratio = xx_ratio;
                        } else xx_ratio = yy_ratio;
                        //locError(Double.toString(xx_ratio) + "see here");

                        float xx = (float) ( x_ratio * pageWidth);
                        float yy = (float) ( (1 - y_ratio) * pageHeight);
                        /*if (x_ratio > y_ratio) {
                            y_ratio = x_ratio;
                        } else x_ratio = y_ratio;*/
                        locError(Double.toString(x_ratio) + "&&" + Double.toString(y_ratio));
                        locError(Float.toString(xx) + "&&" + Float.toString(yy));
                        //locError(Double.toString(m_lat) + "&&" + Double.toString(m_long));

                        canvas.drawCircle(xx, yy, 20, paint);
                        //canvas.drawCircle(yy, xx, 20, paint1);
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
                        textView.setText(Float.toString(e.getX()) + "&" + Float.toString(e.getY()) + "&" + Float.toString(pdfView.getCurrentXOffset()) );

                        Log.w(TAG, pdfView.getPageSize(0).toString() );
                        locError(Float.toString(pdfView.getMaxZoom()));
                        locError(Float.toString(pdfView.getZoom()));
                        //pdfView = (PDFView) findViewById(R.id.pdfView);
                        locError(Integer.toString(pdfView.getLeft()));
                        locError(Integer.toString(pdfView.getRight()));
                        locError(Integer.toString(pdfView.getTop()));
                        locError(Integer.toString(pdfView.getBottom()));
                        locError(Integer.toString(pdfView.getHeight()));
                        locError(Integer.toString(pdfView.getMeasuredHeight()));
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

    @Override
    protected void onDestroy() {
        try{
            locationManager.removeUpdates(locationListener);
        }catch (SecurityException e){
        }
        super.onDestroy();
    }

    public void locError(String str){
        Log.e(TAG, str );
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);



//
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(this, "请打开网络或GPS定位功能!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        try {

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null){
                Log.d(TAG, "onCreate.location = null");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Log.d(TAG, "onCreate.location = " + location);
            updateView(location);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8000, 5, locationListener);
        }catch (SecurityException  e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        //getLocation();
        //

        //申请动态权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 66);
        }
        //

        Intent intent = getIntent();
        int m_num = intent.getIntExtra("num", 0);
        getInfo(m_num);
        linearLayout = (LinearLayout) findViewById(R.id.search);
        if (uri != "") {
            FILE_TYPE = 1;
            displayFromFile(uri);
        } else {
            FILE_TYPE = 2;
            displayFromAsset("Demo");
        }
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
        com.getbase.floatingactionbutton.FloatingActionButton button1 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.measure);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮1 具体功能如下:
                reDrawCache();
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton button2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.north);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮2 具体功能如下:
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton button3 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.restorezoom);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮3 具体功能如下:
                pdfView.resetZoomWithAnimation();
            }
        });


        }

    private void reDrawCache() {
        if (FILE_TYPE == 0) {
            Toast.makeText(this, "PDF文件读取出现问题", Toast.LENGTH_LONG).show();
        } else if (FILE_TYPE == 1) {
            displayFromFile(uri);
            Toast.makeText(this, "这是硬盘上的文件", Toast.LENGTH_LONG).show();
        } else if (FILE_TYPE == 2) {
            displayFromAsset("Demo");
            Toast.makeText(this, "这是Demo", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 66:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须通过所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }

                }
                break;
            default:
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        locError(Integer.toString(pdfView.getLeft()));
        locError(Integer.toString(pdfView.getRight()));
        locError(Integer.toString(pdfView.getTop()));
        locError(Integer.toString(pdfView.getBottom()));
        locError(Integer.toString(pdfView.getHeight()));
        locError(Integer.toString(pdfView.getMeasuredHeight()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentProvider = LocationManager.NETWORK_PROVIDER;

        Log.d(TAG, currentProvider);
        //Location lastKnownLocation =
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        //pageNumber = page;
        //setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String findTitle(String str){
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.query).setVisible(false);
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



