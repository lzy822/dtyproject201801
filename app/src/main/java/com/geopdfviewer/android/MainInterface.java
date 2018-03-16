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
import android.graphics.PointF;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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




    private   String WKT = "";
    private   String uri = "";
    private   String GPTS = "";
    private   String BBox = "";
    private   String MediaBox = "";
    private   String CropBox = "";




    //坐标信息
    double m_lat,m_long;
    //获取pdf 的坐标信息
    double min_lat, max_lat, min_long, max_long;
    //坐标精度
    int definition;
    //获取pdf BBOX 信息
    float b_bottom_x, b_bottom_y, b_top_x, b_top_y;
    //获取pdf MediaBOX 信息
    float m_bottom_x, m_bottom_y, m_top_x, m_top_y;
    //获取pdf CropBOX 信息
    float c_bottom_x, c_bottom_y, c_top_x, c_top_y;
    //记录pdf 页面的长宽拉伸比例
    private float ratio_height = 1, ratio_width = 1;
    //记录屏幕高度, 宽度, viewer控件高度, 宽度
    float screen_width, screen_height, viewer_height, viewer_width;
    //记录pdf文档高度, 宽度
    float page_width, page_height;
    //记录 高度方向留白系数, 和宽度方向留白系数
    float k_h, k_w;
    //记录当前窗口所在区域的位置
    float cs_top, cs_bottom, cs_left, cs_right;

    private float current_pagewidth = 0, current_pageheight = 0;

    //记录当前轨迹
    private String m_cTrail = "";

    private boolean isGetStretchRatio = false;

    Location location;

    private LocationManager locationManager;

    private boolean isLocateEnd = false;

    private int isLocate = 0;

    private void recordTrail(Location location){
        isLocate++;
        if (location != null) {
            if (isLocateEnd || isLocate == 1){
                m_cTrail = Double.toString(m_lat) + " " + Double.toString(m_long);
                isLocateEnd = true;
            }else m_cTrail = m_cTrail + " " + Double.toString(m_lat) + " " + Double.toString(m_long) + " " + Double.toString(m_lat) + " " + Double.toString(m_long);
            //setHereLocation();
            //locError(Double.toString(m_lat) + "," + Double.toString(m_long) + "Come here");

        } else {

        }
    }

    protected final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "Location changed to: " + getLocationInfo(location));
            updateView(location);
            if (isLocateEnd) {
                recordTrail(location);
            }
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
            m_lat = location.getLatitude();
            m_long = location.getLongitude();
            //setHereLocation();
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

        locError(Float.toString(pageWidth) + "%%" + Float.toString(pageHeight));
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
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
        MediaBox = pref1.getString(str + "MediaBox", "");
        CropBox = pref1.getString(str + "CropBox", "");
        Log.w(TAG, "BBox : " + BBox );
        Log.w(TAG, "GPTS : " + GPTS );
        Log.w(TAG, "MediaBox : " + MediaBox );
        Log.w(TAG, "CropBox : " + CropBox );
        //GPTSList = new double[8];
        getGPTS();
        getBBox();
        getCropBox();
        getMediaBox();

    }

    private double w, h, w_min;

    private void getGPTS() {
        String[] GPTString = GPTS.split(" ");
        float[] GPTSs = new float[GPTString.length];
        for (int i = 0; i < GPTString.length; i++) {
            GPTSs[i] = Float.valueOf(GPTString[i]);
        }
        float lat_axis, long_axis;
        PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
        lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
        long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
        for (int i = 0; i < GPTSs.length; i = i + 2){
            if (GPTSs[i] < lat_axis) {
                if (GPTSs[i + 1] < long_axis){
                    pt_lb.x = GPTSs[i];
                    pt_lb.y = GPTSs[i + 1];
                } else {
                    pt_rb.x = GPTSs[i];
                    pt_rb.y = GPTSs[i + 1];
                }
            } else {
                if (GPTSs[i + 1] < long_axis){
                    pt_lt.x = GPTSs[i];
                    pt_lt.y = GPTSs[i + 1];
                } else {
                    pt_rt.x = GPTSs[i];
                    pt_rt.y = GPTSs[i + 1];
                }
            }
        }
        locError("see here");
        w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
        h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
        locError("see here");
        min_lat = (pt_lb.x + pt_rb.x) / 2;
        max_lat = (pt_lt.x + pt_rt.x) / 2;
        min_long = (pt_lt.y + pt_lb.y) / 2;
        max_long = (pt_rt.y + pt_rb.y) / 2;
        locError(Double.toString(min_lat));
        locError(Double.toString(max_lat));
        if (isGPSEnabled()) {
            getLocation();
        }else locError("请打开GPS功能");
    }

    private void getBBox() {
        String[] BBoxString = BBox.split(" ");
        b_bottom_x = Float.valueOf(BBoxString[0]);
        b_bottom_y = Float.valueOf(BBoxString[1]);
        b_top_x = Float.valueOf(BBoxString[2]);
        b_top_y = Float.valueOf(BBoxString[3]);

    }

    private void getMediaBox() {
        String[] MediaBoxString = MediaBox.split(" ");
        m_bottom_x = Float.valueOf(MediaBoxString[0]);
        m_bottom_y = Float.valueOf(MediaBoxString[1]);
        m_top_x = Float.valueOf(MediaBoxString[2]);
        m_top_y = Float.valueOf(MediaBoxString[3]);
        locError(Integer.toString(MediaBoxString.length));

    }

    private void getCropBox() {
        String[] CropBoxString = CropBox.split(" ");
        c_bottom_x = Float.valueOf(CropBoxString[0]);
        c_bottom_y = Float.valueOf(CropBoxString[1]);
        c_top_x = Float.valueOf(CropBoxString[2]);
        c_top_y = Float.valueOf(CropBoxString[3]);
        locError(Integer.toString(CropBoxString.length));

    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.BLACK);
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
                        PointF pt = new PointF(e.getRawX(), e.getRawY());
                        getGeoLocFromPixL(pt);
                        return true;
                    }
                })
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        if (isGetStretchRatio == false){
                            getStretchRatio(pageWidth, pageHeight);
                            viewer_height = pdfView.getHeight();
                            viewer_width = pdfView.getWidth();
                            //Log.d(TAG, Integer.toString(viewer_top) + "here" + Float.toString(viewer_height) + "here" + Integer.toString(viewer_bottom));
                        }
                        //locError(Float.toString(pageHeight) + "%%" + Float.toString(pdfView.getZoom() * 764));
                        current_pageheight = pageHeight;
                        current_pagewidth = pageWidth;
                        getK(pageWidth, pageHeight);
                        getStretchRatio(pageWidth, pageHeight);
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStrokeWidth((float)3.0);
                        paint.setStyle(Paint.Style.FILL);
                        //canvas.drawLine(b_bottom_x * ratio_width, (m_top_y - b_bottom_y) * ratio_height, b_top_x * ratio_width, (m_top_y - b_top_y) * ratio_height, paint);
                        if (isGPSEnabled()){
                            PointF pt = new PointF((float)m_lat, (float)m_long);
                            pt = getPixLocFromGeoL(pt, pageWidth, pageHeight);
                            canvas.drawCircle(pt.x, pt.y, 20, paint);
                        }else locError("请在手机设置中打开GPS功能, 否则该页面很多功能将无法正常使用");
                        getCurrentScreenLoc();
                    }
                })
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        setTitle(pdfFileName);
    }

    private void getStretchRatio(float pagewidth, float pageheight){
        isGetStretchRatio = true;
        //locError(Float.toString(pagewidth));
        //locError(Float.toString(pageheight));
        current_pageheight = pageheight;
        current_pagewidth = pagewidth;
        ratio_height =  (float)(pageheight / (m_top_y - m_bottom_y));
        ratio_width = (float)(pagewidth / (m_top_x - m_bottom_x));
        //locError(Float.toString(m_top_x) + "&&" + Float.toString(m_top_y));
        //locError(Float.toString(b_top_x) + "&&" + Float.toString(b_top_y));
        //locError(Float.toString(b_bottom_x) + "&&" + Float.toString(b_bottom_y));
    }

    private PointF getPixLocFromGeoL(PointF pt, float pageWidth, float pageHeight){
        double y_ratio = ((pt.x - min_lat) / h);
        double x_ratio = ((pt.y - min_long) / w);
        pt.x = (float) ( x_ratio * pageWidth);
        pt.y = (float) ( (1 - y_ratio) * pageHeight);
        return pt;
    }

    private PointF getPixLocFromGeoL(PointF pt){
        double y_ratio = ((pt.x - min_lat) / h);
        double x_ratio = ((pt.y - min_long) / w);
        pt.x = (float) ( x_ratio * current_pagewidth);
        pt.y = (float) ( (1 - y_ratio) * current_pageheight);
        return pt;
    }

    private void displayFromFile(String filePath) {
        setTitle(pdfFileName);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.GREEN);
        final File file = new File(filePath);
        pdfView.fromFile(file)
                .enableSwipe(false)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(false)
                .onLoad(this)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        if (isGetStretchRatio == false){
                            getStretchRatio(pageWidth, pageHeight);
                            viewer_height = pdfView.getHeight();
                            viewer_width = pdfView.getWidth();
                            //Log.d(TAG, Integer.toString(viewer_top) + "here" + Float.toString(viewer_height) + "here" + Integer.toString(viewer_bottom));
                        }
                        current_pageheight = pageHeight;
                        current_pagewidth = pageWidth;
                        //locError(Float.toString(pageHeight) + "%%" + Float.toString(pdfView.getZoom() * 764));
                        getK(pageWidth, pageHeight);
                        getStretchRatio(pageWidth, pageHeight);
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStrokeWidth((float)3.0);
                        paint.setStyle(Paint.Style.FILL);
                        //canvas.drawLine(b_bottom_x * ratio_width, (m_top_y - b_bottom_y) * ratio_height, b_top_x * ratio_width, (m_top_y - b_top_y) * ratio_height, paint);
                        if (isGPSEnabled()){
                        PointF pt = new PointF((float)m_lat, (float)m_long);
                        pt = getPixLocFromGeoL(pt, pageWidth, pageHeight);
                        canvas.drawCircle(pt.x, pt.y, 20, paint);
                        }else locError("请在手机设置中打开GPS功能, 否则该页面很多功能将无法正常使用");
                        if (isLocateEnd){
                            String[] TrailString = m_cTrail.split(" ");
                            float[] Trails = new float[TrailString.length];
                            for (int i = 0; i < TrailString.length; i++){
                                Trails[i] = Float.valueOf(TrailString[i]);
                            }
                            for (int j = 0; j < Trails.length - 2; j = j + 2){
                                PointF pt11, pt12;
                                pt11 = getPixLocFromGeoL(new PointF(Trails[j], Trails[j + 1]));
                                pt12 = getPixLocFromGeoL(new PointF(Trails[j + 2], Trails[j + 4]));
                                canvas.drawLine(pt11.x, pt11.y, pt12.x, pt12.y, paint);
                            }
                        }
                        /*float[] pts = new float[8];
                        pts[0] = 100;
                        pts[1] = 100;
                        pts[2] = 100;
                        pts[3] = 200;
                        pts[4] = 400;
                        pts[5] = 300;
                        pts[6] = 800;
                        pts[7] = 200;
                        canvas.drawLines(pts, paint);*/

                        //locError(Float.toString(xx) + "%" + Float.toString(yy));
                        getCurrentScreenLoc();
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
                        PointF pt = new PointF(e.getRawX(), e.getRawY());
                        getGeoLocFromPixL(pt);
                        return true;
                    }
                })
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
    }

    private void getK(float width, float height){
        //精确定位算法
        page_height = height;
        page_width = width;
        if (viewer_height > page_height){
            k_h = (viewer_height - page_height) / 2;
        } else k_h = 0;
        if (viewer_width > page_width){
            k_w = (viewer_width - page_width) / 2;
        } else k_w = 0;
        //locError(Float.toString(k_w) + "see" + Float.toString(k_h));
    }

    private void getCurrentScreenLoc(){
        DecimalFormat df = new DecimalFormat("0.0000");
        //精确定位算法
        float xxxx, yyyy;
        if (pdfView.getCurrentYOffset() > 0 || pdfView.getCurrentXOffset() > 0) {
            if (pdfView.getCurrentYOffset() > 0 && pdfView.getCurrentXOffset() > 0){
                cs_top = (float)max_lat;
                cs_bottom = (float)min_lat;
                cs_left = (float)min_long;
                cs_right = (float)max_long;
            }else if (pdfView.getCurrentYOffset() > 0 && pdfView.getCurrentXOffset() <= 0){
                cs_top = (float)max_lat;
                cs_bottom = (float)min_lat;
                cs_left = (float)(( Math.abs(pdfView.getCurrentXOffset()) / current_pagewidth) * w + min_long);
                cs_right = (float)(( viewer_width - pdfView.getCurrentXOffset()) / current_pagewidth * w + min_long);
            }else {
                cs_top = (float)(max_lat - Math.abs(pdfView.getCurrentYOffset()) / current_pageheight * h);
                cs_bottom = (float)(max_lat - (viewer_height - pdfView.getCurrentYOffset()) / current_pageheight * h);
                cs_left = (float)min_long;
                cs_right = (float)max_long;
            }
        } else {
            cs_top = (float)(max_lat - Math.abs(pdfView.getCurrentYOffset()) / current_pageheight * h);
            cs_bottom = (float)(max_lat - (viewer_height - pdfView.getCurrentYOffset()) / current_pageheight * h);
            cs_left = (float)(( Math.abs(pdfView.getCurrentXOffset()) / current_pagewidth) * w + min_long);
            cs_right = (float)(( viewer_width - pdfView.getCurrentXOffset()) / current_pagewidth * w + min_long);
        }
        locError(Float.toString(cs_top) + "%" + Float.toString(cs_bottom) + "%" + Float.toString(cs_left) + "%" + Float.toString(cs_right));
        //cs_top = pdfView.getCurrentYOffset()
    }

    private void getGeoLocFromPixL(PointF pt){
        textView = (TextView) findViewById(R.id.txt);
        DecimalFormat df = new DecimalFormat("0.0000");
        //精确定位算法
        float xxxx, yyyy;
        if (page_height < viewer_height || page_width < viewer_width) {
            xxxx = ((pt.x - (screen_width - viewer_width + k_w)));
            yyyy = ((pt.y - (screen_height - viewer_height + k_h)));
            if (pt.y >= (screen_height - viewer_height + k_h) && pt.y <= (screen_height - viewer_height + k_h + page_height) && pt.x >= (screen_width - viewer_width + k_w) && pt.x <= (screen_width - viewer_width + k_w + page_width)) {
                pt.x = (float)(max_lat - (yyyy) / current_pageheight * ( max_lat - min_lat));
                pt.y = (float)(( xxxx) / current_pagewidth * ( max_long - min_long) + min_long);
                textView.setText(df.format(pt.x) + "; " + df.format(pt.y));
            } else textView.setText("点击位置在区域之外");
        } else {
            xxxx = pt.x - (screen_width - viewer_width);
            yyyy = pt.y - (screen_height - viewer_height);
            pt.x = (float)(max_lat - ( yyyy - pdfView.getCurrentYOffset()) / current_pageheight * ( max_lat - min_lat));
            pt.y = (float)(( xxxx - pdfView.getCurrentXOffset()) / current_pagewidth * ( max_long - min_long) + min_long);
            textView.setText(df.format(pt.x) + "; " + df.format(pt.y));
        }


        //
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
        Log.e(TAG, "debug: " + str );
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
        }catch (SecurityException  e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        //申请动态权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 66);
        }
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
        final com.getbase.floatingactionbutton.FloatingActionButton button2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.lochere);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮2 具体功能如下:
                //locError(Float.toString(s_x) + "%" + Float.toString(s_y));
                //pdfView.moveRelativeTo(s_x, s_y);
                //button2.setIcon(R.drawable.ic_my_location);
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
        //pdfView = (PDFView) findViewById(R.id.pdfView);
        //locError(Integer.toString(pdfView.getLeft()));
        //locError(Integer.toString(pdfView.getRight()));
        //locError(Integer.toString(pdfView.getTop()));
        //locError(Integer.toString(pdfView.getBottom()));
        //locError(Integer.toString(pdfView.getHeight()));
        //locError(Integer.toString(pdfView.getMeasuredHeight()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentProvider = LocationManager.NETWORK_PROVIDER;
        getScreen();
        Log.d(TAG, currentProvider);
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

    private void getScreen(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;
        float density = dm.density;
        int screenWidth = (int) (screen_width / density);
        int screenHeight = (int) (screen_height / density);
        pdfView = (PDFView) findViewById(R.id.pdfView);

        Log.d(TAG, Float.toString(screen_width) + "^" + Float.toString(screen_height) + "^" + Float.toString(screenWidth) + "^" + Float.toString(screenHeight));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.query).setVisible(false);
        menu.findItem(R.id.trail).setVisible(true);
        menu.findItem(R.id.trailend).setVisible(true);
        return true;
    }

    private void initTrail(){
        if (isGPSEnabled()){
            locError("开始绘制轨迹");
        }else locError("请打开GPS功能");
    }

    private boolean isGPSEnabled(){
        textView = (TextView) findViewById(R.id.txt);
        //得到系统的位置服务，判断GPS是否激活
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean ok = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(ok){
            textView.setText("GPS已经开启");
            return true;
        }else{
            textView.setText("GPS没有开启");
            return false;
        }
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
            case R.id.trail:
                initTrail();
                break;
            case R.id.trailend:
                //initTrail();
                m_cTrail = "";
                if (!isLocateEnd){
                    isLocateEnd = true;
                }else Toast.makeText(this, "你没有打开位置记录功能", Toast.LENGTH_LONG).show();

                break;
            case R.id.query:
                linearLayout = (LinearLayout) findViewById(R.id.search);
                linearLayout.setVisibility(View.VISIBLE);
            default:
        }
        return true;
    }
}



