package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.clans.fab.FloatingActionButton;

import org.litepal.crud.DataSupport;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainInterface extends AppCompatActivity  implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener {
    private static final String TAG = "MainInterface";
    public static final String SAMPLE_FILE = "dt/cangyuan.dt";
    private final static int REQUEST_CODE_PHOTO = 42;
    private final static int REQUEST_CODE_TAPE = 43;
    public static final int PERMISSION_CODE = 42042;
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final int NONE_FILE_TYPE = 0;
    public static final int FILE_FILE_TYPE = 1;
    public static final int ASSET_FILE_TYPE = 2;
    public static int FILE_TYPE = NONE_FILE_TYPE;
    public static final int TAKE_PHOTO = 119;
    Integer pageNumber = 0;
    public String content;
    String pdfFileName;
    PDFView pdfView;
    LinearLayout linearLayout;
    TextView textView;
    TextView scaleShow;

    //声明bts容器
    List<bt> bts;




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
    //记录 高度方向留白系数, 和宽度方向留白系数
    float k_h, k_w;
    //记录当前窗口所在区域的位置
    float cs_top, cs_bottom, cs_left, cs_right;
    float cpix_top, cpix_bottom, cpix_left, cpix_right;
    //记录pdf文档高度, 宽度
    private float current_pagewidth = 0, current_pageheight = 0;

    //记录当前轨迹
    private String m_cTrail = "";

    private boolean isGetStretchRatio = false;

    //记录是否自动切换地图
    private boolean isAutoTrans = false;
    //按钮声明
    ImageButton autoTrans_imgbt;

    Location location;

    //声明Toolbar
    Toolbar toolbar;

    private LocationManager locationManager;

    //是否结束绘制
    private boolean isLocateEnd = true;

    //是否绘制POI 以及 trail
    private boolean showAll = false;
    //是否绘制POI 以及 trail
    private boolean showPOI = false;
    private boolean showTrail = false;

    //记录当前绘图类型
    private int isDrawType = NONE_DRAW_TYPE;
    public static final int POI_DRAW_TYPE = 2;
    public static final int TRAIL_DRAW_TYPE = 1;
    public static final int NONE_DRAW_TYPE = 0;

    //记录当前使用者放缩情况
    private int isZoom = ZOOM_NONE;
    public static final int ZOOM_NONE = 0;
    public static final int ZOOM_OUT = 1;
    public static final int ZOOM_IN = 2;

    //当前记录的坐标点个数
    private int isLocate = 0;

    //上一个记录下来的坐标点坐标
    private float last_x, last_y;

    //记录当前GeoPDF识别码
    private String ic;

    //记录是否开始查询操作
    private boolean isQuery = false;

    //记录当前点击像素点与当前地图的关系
    public static final int OUT = -1;
    public static final int IN_FULL = 1;
    public static final int IN_NOTFULL = 2;

    //记录当前设置点位数
    private int poinum_messure = 0;

    //记录是否开始测量
    private  boolean isMessure = false;

    //记录测量点位的坐标
    private PointF poi111, poi222;

    //按键声明
    ImageButton trail_imgbt, startTrail_imgbt, endTrail_imgbt, addPoi_imgbt, query_poi_imgbt;

    //记录当前缩放比例
    private float c_zoom = 1;

    //记录当前图号
    private int num_map;

    //记录上一张图图号
    private int num_map1;

    //记录拍照后返回的URI
    private Uri imageUri;

    //FloatingActionButton声明
    com.github.clans.fab.FloatingActionButton addPhoto_fab;
    com.github.clans.fab.FloatingActionButton locHere_fab;
    com.github.clans.fab.FloatingActionButton restoreZoom_fab;
    com.github.clans.fab.FloatingActionButton addTape_fab;
    com.github.clans.fab.FloatingActionButton messureDistance_fab;
    com.github.clans.fab.FloatingActionButton whiteBlank_fab;

    //声明测量相关按钮
    FloatingActionButton delete_messure_fab;
    FloatingActionButton cancel_messure_fab;
    FloatingActionButton backpt_messure_fab;

    //记录是否开启白板功能
    private boolean isOpenWhiteBlank = false;

    private double w, h;

    //记录是否定位到当前位置
    private boolean isPos = false;

    //声明Paint
    Paint paint, paint1, paint2, paint3, paint4, paint5, paint6, paint8;

    //记录测量坐标串
    private String messure_pts = "";

    com.github.clans.fab.FloatingActionMenu floatingActionsMenu;

    private int isMessureType = MESSURE_NONE_TYPE;
    private final static int MESSURE_NONE_TYPE = 0;
    private final static int MESSURE_DISTANCE_TYPE = 1;
    private final static int MESSURE_AREA_TYPE = 2;

    private int isCoordinateType = COORDINATE_DEFAULT_TYPE;
    private final static int COORDINATE_DEFAULT_TYPE = 0;
    private final static int COORDINATE_BLH_TYPE = 1;
    private final static int COORDINATE_XYZ_TYPE = 2;

    //记录是否需要详细地址信息
    private boolean isFullLocation = false;

    //记录verx
    float verx = 0;

    //记录是否开始绘制轨迹
    private int isDrawTrail = NONE_DRAW_TYPE;

    //记录点选的坐标位置
    private float locLatForTap, locLongForTap;

    //记录是否渲染完文件
    private boolean isRomance = false;

    //初始化传感器管理器
    private SensorManager sensorManager;
    private float predegree = 0;
    private float degree = 0;
    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (degree != 0 & predegree != degree & Math.abs(degree - predegree) > 10){
                predegree = degree;
                if (isRomance){
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
            degree = event.values[0];
            locError("predegree: " + Float.toString(predegree) + " degree: " + Float.toString(degree));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //记录比例尺格式
    DecimalFormat scale_df;

    //记录当前显示模式
    private int showMode = CENTERMODE;
    private static final int CENTERMODE = -1;
    private static final int NOCENTERMODE = -2;


    /*private RecordTrail.RecordTrailBinder recordTrailBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            recordTrailBinder = (RecordTrail.RecordTrailBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }*/

    private void recordTrail(Location location){
        isLocate++;
        if (location != null) {
            if (isLocateEnd || isLocate == 1){
                m_cTrail = m_cTrail + Double.toString(m_lat) + " " + Double.toString(m_long);
                //isLocateEnd = true;
            }else m_cTrail = m_cTrail + " " + Double.toString(m_lat) + " " + Double.toString(m_long) + " " + Double.toString(m_lat) + " " + Double.toString(m_long);
            //setHereLocation();
            //locError(Double.toString(m_lat) + "," + Double.toString(m_long) + "Come here");

        } else {

        }
    }

    //记录轨迹
    private void recordTrail(float x, float y){
        isLocate++;
        last_x = x;
        last_y = y;
        locError(Integer.toString(isLocate));
        //if (location != null) {
            if (isLocateEnd || isLocate == 1){
                if (!m_cTrail.isEmpty()){
                    if (isLocate > 2) {
                        int num = DataUtil.appearNumber(m_cTrail, " ");
                        String str = m_cTrail;
                        for (int i = 0; i <= num - 2; i++) {
                            str = str.substring(str.indexOf(" ") + 1);
                        }
                        m_cTrail = m_cTrail.substring(0, m_cTrail.length() - str.length());
                    } else m_cTrail = m_cTrail + " " + Float.toString(x) + " " + Float.toString(y);
                }else m_cTrail = m_cTrail + Float.toString(x) + " " + Float.toString(y);
            }else m_cTrail = m_cTrail + " " + Float.toString(x) + " " + Float.toString(y) + " " + Float.toString(x) + " " + Float.toString(y);
            //setHereLocation();
            //locError(Integer.toString(m_lat) + "," + Double.toString(m_long) + "Come here");

        //} else {

        //}
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

    //获取地理信息
    public void getInfo(int num) {
        num_map = num;
        SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
        String str = "n_" + num + "_";
        pdfFileName = pref1.getString(str + "name", "");
        WKT = pref1.getString(str + "WKT", "");
        uri = pref1.getString(str + "uri", "");
        GPTS = pref1.getString(str + "GPTS", "");
        BBox = pref1.getString(str + "BBox", "");
        MediaBox = pref1.getString(str + "MediaBox", "");
        CropBox = pref1.getString(str + "CropBox", "");
        ic = pref1.getString(str + "ic", "");
        Log.w(TAG, "BBox : " + BBox );
        Log.w(TAG, "GPTS : " + GPTS );
        Log.w(TAG, "MediaBox : " + MediaBox );
        Log.w(TAG, "CropBox : " + CropBox );
        Log.w(TAG, "ic : " + ic );
        //GPTSList = new double[8];

    }

    //处理地理信息
    private void manageInfo(){
        double[] gpts = DataUtil.getGPTS(GPTS);
        min_lat = gpts[0];
        max_lat = gpts[1];
        min_long = gpts[2];
        max_long = gpts[3];
        locError("delta : " + Double.toString(max_lat - min_lat + (max_long - min_long)));
        w = gpts[4];
        h = gpts[5];
        if (isGPSEnabled()) {
            getLocation();
        }else locError("请打开GPS功能");
        float[] bboxs = DataUtil.getBox(BBox);
        b_bottom_x = bboxs[0];
        b_bottom_y = bboxs[1];
        b_top_x = bboxs[2];
        b_top_y = bboxs[3];
        float[] mediaboxs = DataUtil.getBox(MediaBox);
        m_bottom_x = mediaboxs[0];
        m_bottom_y = mediaboxs[1];
        m_top_x = mediaboxs[2];
        m_top_y = mediaboxs[3];
        float[] cropbox = DataUtil.getBox(CropBox);
        c_bottom_x = cropbox[0];
        c_bottom_y = cropbox[1];
        c_top_x = cropbox[2];
        c_top_y = cropbox[3];
    }

    //记录距离之和
    private double distanceSum = 0;

    //解析测量字符串并绘制
    private void parseAndrawMessure(String mmessure_pts, Canvas canvas){
        if (isMessure & !mmessure_pts.isEmpty()) {
            distanceSum = 0;
            double distanceCurrent = 0;
            mmessure_pts = mmessure_pts.trim();
            String[] pts = mmessure_pts.split(" ");
            float[] mpts;
            if (pts.length <= 4 & pts.length > 3) {
                mpts = new float[pts.length];
                for (int i = 0; i < pts.length; i++) {
                    mpts[i] = Float.valueOf(pts[i]);
                }
                for (int i = 0; i < pts.length; i++) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                for (int i = 0; i < pts.length; i = i + 4) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx1 = new PointF(mpts[i], mpts[i + 1]);
                    PointF xx2 = new PointF(mpts[i + 2], mpts[i + 3]);
                    distanceCurrent = DataUtil.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                    distanceSum = distanceCurrent;
                }
                for (int i = 0; i < pts.length; i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = RenderUtil.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; i++) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                locError(mmessure_pts);
                locError("mpts : " + Integer.toString(mpts.length));
                canvas.drawLines(mpts, paint6);
            } else if (pts.length > 4) {
                mpts = new float[pts.length * 2 - 4];
                locError("pts.length * 2 - 4 : " + Integer.toString(pts.length * 2 - 4));
                for (int i = 0, j = 0; i < (pts.length * 2 - 4) || j < pts.length; j = j + 2) {
                    if (j == 0 || i == (pts.length * 2 - 6)) {
                        mpts[i] = Float.valueOf(pts[j]);
                        mpts[i + 1] = Float.valueOf(pts[j + 1]);
                        i = i + 2;
                        locError("i = " + Integer.toString(i) + " j : " + Integer.toString(j));
                    } else {
                        mpts[i] = Float.valueOf(pts[j]);
                        mpts[i + 1] = Float.valueOf(pts[j + 1]);
                        mpts[i + 2] = Float.valueOf(pts[j]);
                        mpts[i + 3] = Float.valueOf(pts[j + 1]);
                        i = i + 4;
                        locError("i = " + Integer.toString(i) + " j : " + Integer.toString(j));
                    }

                }

                for (int i = 0; i < pts.length - 2; i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx1 = new PointF(Float.valueOf(pts[i]), Float.valueOf(pts[i + 1]));
                    PointF xx2 = new PointF(Float.valueOf(pts[i + 2]), Float.valueOf(pts[i + 3]));
                    distanceCurrent = DataUtil.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                    distanceSum = distanceSum + distanceCurrent;
                }
                for (int i = 0; i < (pts.length * 2 - 4); i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = RenderUtil.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; i++) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                locError(mmessure_pts);
                locError("mpts : " + Integer.toString(mpts.length));
                if (isMessureType == MESSURE_DISTANCE_TYPE){
                canvas.drawLines(mpts, paint6);
                }else if (isMessureType == MESSURE_AREA_TYPE){
                    canvas.drawLines(mpts, paint6);
                    canvas.drawLine(mpts[0], mpts[1], mpts[mpts.length - 2], mpts[mpts.length - 1], paint6);
                }
            } else {
                mpts = new float[pts.length];
            }
            DecimalFormat df1 = new DecimalFormat("0.00");
            //locError(Double.toString(distanceSum));
            if (isMessureType == MESSURE_DISTANCE_TYPE){
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle(df1.format(distanceSum) + "米 , " + df1.format(distanceCurrent) + "米(轨迹记录中)");
                }else toolbar.setTitle(df1.format(distanceSum) + "米 , " + df1.format(distanceCurrent) + "米");
                //setTitle(df1.format(distanceSum) + "米");
            }else if (isMessureType == MESSURE_AREA_TYPE){
                double area = 0;
                for (int i = 0; i < mpts.length - 3; i = i + 2){
                    area = area + ( mpts[i] * mpts[i + 3] - mpts[i + 2] * mpts[i + 1]);
                }
                area = area - (mpts[0] * mpts[mpts.length - 1] - mpts[1] * mpts[mpts.length - 2]);
                area = Math.abs((area) / 2) / c_zoom / c_zoom;
                area = area * DataUtil.algorithm((max_long + min_long) / 2, (max_lat + min_lat) / 2, (max_long + min_long) / 2 + (max_long - min_long) / ( viewer_width - 2 * k_w), (max_lat + min_lat) / 2) * DataUtil.algorithm((max_long + min_long) / 2, (max_lat + min_lat) / 2, (max_long + min_long) / 2, (max_lat + min_lat) / 2 + (max_lat - min_lat) / (viewer_height - 2 * k_h));
                //area = area / 1.0965f;
                //setTitle(df1.format(area) + "平方米");
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle(df1.format(area) + "平方米(轨迹记录中)");
                }else toolbar.setTitle(df1.format(area) + "平方米");
            }

        /*PointF xx = new PointF(mpts[0], mpts[1]);
        PointF yy = new PointF(mpts[2], mpts[3]);
        PointF pt11 = RenderUtil.getPixLocFromGeoL(xx);
        PointF pt22 = RenderUtil.getPixLocFromGeoL(yy);
        mpts[0] = pt11.x;
        mpts[1] = pt11.y;
        mpts[2] = pt22.x;
        mpts[3] = pt22.y;*/


            //canvas.drawLine(mpts[0], mpts[1], mpts[2], mpts[3], paint6);
        }
    }

    //解析白板字符串并绘制
    private void parseAndrawLinesforWhiteBlank(Canvas canvas){
        int size = geometry_whiteBlanks.size();
        for (int k = 0; k < size; k++){
            locError("geometry: " + geometry_whiteBlanks.get(k).getM_lines());
            Paint paint7 = new Paint();
            paint7.setStrokeWidth(10);
            paint7.setColor(geometry_whiteBlanks.get(k).getM_color());
            paint7.setStyle(Paint.Style.STROKE);
        if (isWhiteBlank & !geometry_whiteBlanks.get(k).getM_lines().isEmpty()) {
            geometry_whiteBlanks.get(k).setM_lines(geometry_whiteBlanks.get(k).getM_lines());
            String[] pts = geometry_whiteBlanks.get(k).getM_lines().split(" ");
            float[] mpts;
            if (pts.length <= 4 & pts.length > 3) {
                mpts = new float[pts.length];
                for (int i = 0; i < pts.length; i++) {
                    mpts[i] = Float.valueOf(pts[i]);
                }
                for (int i = 0; i < pts.length; i++) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                for (int i = 0; i < pts.length; i = i + 4) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx1 = new PointF(mpts[i], mpts[i + 1]);
                    PointF xx2 = new PointF(mpts[i + 2], mpts[i + 3]);
                }
                for (int i = 0; i < pts.length; i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = RenderUtil.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; i++) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                locError(geometry_whiteBlanks.get(k).getM_lines());
                locError("mpts : " + Integer.toString(mpts.length));
                canvas.drawLines(mpts, paint7);
            } else if (pts.length > 4) {
                mpts = new float[pts.length * 2 - 4];
                locError("pts.length * 2 - 4 : " + Integer.toString(pts.length * 2 - 4));
                for (int i = 0, j = 0; i < (pts.length * 2 - 4) || j < pts.length; j = j + 2) {
                    if (j == 0 || i == (pts.length * 2 - 6)) {
                        mpts[i] = Float.valueOf(pts[j]);
                        mpts[i + 1] = Float.valueOf(pts[j + 1]);
                        i = i + 2;
                        locError("i = " + Integer.toString(i) + " j : " + Integer.toString(j));
                    } else {
                        mpts[i] = Float.valueOf(pts[j]);
                        mpts[i + 1] = Float.valueOf(pts[j + 1]);
                        mpts[i + 2] = Float.valueOf(pts[j]);
                        mpts[i + 3] = Float.valueOf(pts[j + 1]);
                        i = i + 4;
                        locError("i = " + Integer.toString(i) + " j : " + Integer.toString(j));
                    }

                }

                for (int i = 0; i < pts.length - 2; i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx1 = new PointF(Float.valueOf(pts[i]), Float.valueOf(pts[i + 1]));
                    PointF xx2 = new PointF(Float.valueOf(pts[i + 2]), Float.valueOf(pts[i + 3]));
                    distanceSum = distanceSum + DataUtil.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                }
                for (int i = 0; i < (pts.length * 2 - 4); i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = RenderUtil.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; i++) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                locError(geometry_whiteBlanks.get(k).getM_lines());
                locError("mpts : " + Integer.toString(mpts.length));
                canvas.drawLines(mpts, paint7);
            } else {
                mpts = new float[pts.length];
            }


        /*PointF xx = new PointF(mpts[0], mpts[1]);
        PointF yy = new PointF(mpts[2], mpts[3]);
        PointF pt11 = RenderUtil.getPixLocFromGeoL(xx);
        PointF pt22 = RenderUtil.getPixLocFromGeoL(yy);
        mpts[0] = pt11.x;
        mpts[1] = pt11.y;
        mpts[2] = pt22.x;
        mpts[3] = pt22.y;*/


            //canvas.drawLine(mpts[0], mpts[1], mpts[2], mpts[3], paint6);
        }
        }
    }

    //解析白板字符串并绘制1
    private void parseAndrawLinesforWhiteBlank(String whiteBlankPt, Canvas canvas){
            locError("geometry: " + whiteBlankPt);
            Paint paint7 = new Paint();
            paint7.setStrokeWidth(10);
            paint7.setColor(color_Whiteblank);
            paint7.setStyle(Paint.Style.STROKE);
            if (isWhiteBlank & !whiteBlankPt.isEmpty()) {
                whiteBlankPt = whiteBlankPt.trim();
                String[] pts = whiteBlankPt.split(" ");
                float[] mpts;
                if (pts.length <= 4 & pts.length > 3) {
                    mpts = new float[pts.length];
                    for (int i = 0; i < pts.length; i++) {
                        mpts[i] = Float.valueOf(pts[i]);
                    }
                    for (int i = 0; i < pts.length; i++) {
                        locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                    }
                    for (int i = 0; i < pts.length; i = i + 4) {
                        //mpts[i] = Float.valueOf(pts[i]);
                        PointF xx1 = new PointF(mpts[i], mpts[i + 1]);
                        PointF xx2 = new PointF(mpts[i + 2], mpts[i + 3]);
                    }
                    for (int i = 0; i < pts.length; i = i + 2) {
                        //mpts[i] = Float.valueOf(pts[i]);
                        PointF xx = new PointF(mpts[i], mpts[i + 1]);
                        PointF pt11 = RenderUtil.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        mpts[i] = pt11.x;
                        mpts[i + 1] = pt11.y;
                    }
                    for (int i = 0; i < pts.length; i++) {
                        locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                    }
                    //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    locError(whiteBlankPt);
                    locError("mpts : " + Integer.toString(mpts.length));
                    canvas.drawLines(mpts, paint7);
                } else if (pts.length > 4) {
                    mpts = new float[pts.length * 2 - 4];
                    locError("pts.length * 2 - 4 : " + Integer.toString(pts.length * 2 - 4));
                    for (int i = 0, j = 0; i < (pts.length * 2 - 4) || j < pts.length; j = j + 2) {
                        if (j == 0 || i == (pts.length * 2 - 6)) {
                            mpts[i] = Float.valueOf(pts[j]);
                            mpts[i + 1] = Float.valueOf(pts[j + 1]);
                            i = i + 2;
                            locError("i = " + Integer.toString(i) + " j : " + Integer.toString(j));
                        } else {
                            mpts[i] = Float.valueOf(pts[j]);
                            mpts[i + 1] = Float.valueOf(pts[j + 1]);
                            mpts[i + 2] = Float.valueOf(pts[j]);
                            mpts[i + 3] = Float.valueOf(pts[j + 1]);
                            i = i + 4;
                            locError("i = " + Integer.toString(i) + " j : " + Integer.toString(j));
                        }

                    }

                    for (int i = 0; i < pts.length - 2; i = i + 2) {
                        //mpts[i] = Float.valueOf(pts[i]);
                        PointF xx1 = new PointF(Float.valueOf(pts[i]), Float.valueOf(pts[i + 1]));
                        PointF xx2 = new PointF(Float.valueOf(pts[i + 2]), Float.valueOf(pts[i + 3]));
                        distanceSum = distanceSum + DataUtil.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                    }
                    for (int i = 0; i < (pts.length * 2 - 4); i = i + 2) {
                        //mpts[i] = Float.valueOf(pts[i]);
                        PointF xx = new PointF(mpts[i], mpts[i + 1]);
                        PointF pt11 = RenderUtil.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        mpts[i] = pt11.x;
                        mpts[i + 1] = pt11.y;
                    }
                    for (int i = 0; i < pts.length; i++) {
                        locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                    }
                    //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    locError(whiteBlankPt);
                    locError("mpts : " + Integer.toString(mpts.length));
                    canvas.drawLines(mpts, paint7);
                } else {
                    mpts = new float[pts.length];
                }


        /*PointF xx = new PointF(mpts[0], mpts[1]);
        PointF yy = new PointF(mpts[2], mpts[3]);
        PointF pt11 = RenderUtil.getPixLocFromGeoL(xx);
        PointF pt22 = RenderUtil.getPixLocFromGeoL(yy);
        mpts[0] = pt11.x;
        mpts[1] = pt11.y;
        mpts[2] = pt22.x;
        mpts[3] = pt22.y;*/


                //canvas.drawLine(mpts[0], mpts[1], mpts[2], mpts[3], paint6);
            }

    }

    //pdfView的ontouchlistener功能监控
    private void onTouchListenerForView(){
    }

    //显示GeoPDF
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
                        PointF pt1;
                        pt1 = getGeoLocFromPixL(pt);
                        //textView.setText();
                        if (isDrawType == POI_DRAW_TYPE & !isQuery){
                            List<POI> POIs = DataSupport.findAll(POI.class);
                            POI poi = new POI();
                            poi.setName("POI" + String.valueOf(POIs.size() + 1));
                            poi.setIc(ic);
                            poi.setX(pt1.x);
                            poi.setY(pt1.y);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            poi.setTime(simpleDateFormat.format(date));
                            poi.setPhotonum(0);
                            poi.setPoic("POI" + String.valueOf(System.currentTimeMillis()));
                            poi.save();
                            locError(pt1.toString());
                            PointF pp = RenderUtil.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            c_zoom = pdfView.getZoom();
                            pdfView.zoomWithAnimation(pp.x, pp.y, c_zoom);

                            //locError("ScaleX : " + String.valueOf(pdfView.getScaleX()) + "ScaleY : " + String.valueOf(pdfView.getScaleY()) + "Scale : " + String.valueOf(pdfView.getScaleX() * pdfView.getScaleY()));
                        }
                        if (isMessure & showMode == NOCENTERMODE){
                            locError("messure_pts" + messure_pts);
                            poinum_messure++;
                            if (poinum_messure == 1){
                                poi111 = pt1;
                                messure_pts = Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                                //setTitle("正在测量");
                                if (isDrawTrail == TRAIL_DRAW_TYPE){
                                    toolbar.setTitle("正在测量(轨迹记录中)");
                                }else toolbar.setTitle("正在测量");
                            }else if (poinum_messure == 2){
                                poi222 = pt1;
                                messure_pts = messure_pts + " " + Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }else {
                                messure_pts = messure_pts + " " + Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                            }
                        }
                        if (isMessure & showMode == CENTERMODE){
                            locError("messure_pts" + messure_pts);
                            poinum_messure++;
                            if (poinum_messure == 1){
                                poi111 = centerPointLoc;
                                messure_pts = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                if (isDrawTrail == TRAIL_DRAW_TYPE){
                                    toolbar.setTitle("正在测量(轨迹记录中)");
                                }else toolbar.setTitle("正在测量");
                            }else if (poinum_messure == 2){
                                poi222 = centerPointLoc;
                                messure_pts = messure_pts + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }else {
                                messure_pts = messure_pts + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }

                        }
                        if (isQuery && isDrawType == NONE_DRAW_TYPE){
                            List<mPOIobj> pois =  new ArrayList<>();
                            Cursor cursor = DataSupport.findBySQL("select * from POI where x >= ? and x <= ? and y >= ? and y <= ?", String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long));
                            if (cursor.moveToFirst()){
                                do {
                                    String POIC = cursor.getString(cursor.getColumnIndex("poic"));
                                    String time = cursor.getString(cursor.getColumnIndex("time"));
                                    String name = cursor.getString(cursor.getColumnIndex("name"));
                                    String description = cursor.getString(cursor.getColumnIndex("description"));
                                    int tapenum = cursor.getInt(cursor.getColumnIndex("tapenum"));
                                    int photonum = cursor.getInt(cursor.getColumnIndex("photonum"));
                                    float x = cursor.getFloat(cursor.getColumnIndex("x"));
                                    float y = cursor.getFloat(cursor.getColumnIndex("y"));
                                    mPOIobj mPOIobj = new mPOIobj(POIC, x, y, time, tapenum, photonum, name, description);
                                    pois.add(mPOIobj);
                                }while (cursor.moveToNext());
                            }
                            cursor.close();
                            locError("size : " + Integer.toString(pois.size()));
                            int n = 0;
                            int num = 0;
                            if (pois.size() > 0){
                                mPOIobj poii = pois.get(0);
                                PointF pointF = new PointF(poii.getM_X(), poii.getM_Y());
                                pointF = RenderUtil.getPixLocFromGeoL(pointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                pointF = new PointF(pointF.x, pointF.y - 70);
                                //pointF = getGeoLocFromPixL(pointF);
                                pt1 = RenderUtil.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                locError("pt1 : " + pt1.toString());
                                float delta = Math.abs( pointF.x - pt1.x) + Math.abs( pointF.y - pt1.y);
                                for (mPOIobj poi : pois){
                                    PointF mpointF = new PointF(poi.getM_X(), poi.getM_Y());
                                    mpointF = RenderUtil.getPixLocFromGeoL(mpointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    mpointF = new PointF(mpointF.x, mpointF.y - 70);
                                    if (Math.abs( mpointF.x - pt1.x) + Math.abs( mpointF.y - pt1.y) < delta && Math.abs( mpointF.x - pt1.x) + Math.abs( mpointF.y - pt1.y) < 35){
                                        locError("mpointF : " + mpointF.toString());
                                        delta = Math.abs( pointF.x - pt1.x) + Math.abs( pointF.y - pt1.y);
                                        num = n;
                                    }
                                    locError("n : " + Integer.toString(n));
                                    n++;
                                }
                                locError("num : " + Integer.toString(num));
                                locError("delta : " + Float.toString(delta));
                                if (delta < 35 || num != 0){
                                    Intent intent = new Intent(MainInterface.this, singlepoi.class);
                                    intent.putExtra("POIC", pois.get(num).getM_POIC());
                                    startActivity(intent);
                                    //locError(Integer.toString(pois.get(num).getPhotonum()));
                                }else locError("没有正常查询");
                            }

                        }
                        return true;
                    }
                })
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        current_pageheight = pageHeight;
                        current_pagewidth = pageWidth;
                            viewer_height = pdfView.getHeight();
                            viewer_width = pdfView.getWidth();
                        //float verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
                        if (pdfView.getPositionOffset() != verx & isPos){
                            locHere_fab.setImageResource(R.drawable.ic_location_searching);
                            isPos = false;
                        }
                        locError("PositionOffset : " + Float.toString(pdfView.getPositionOffset()));
                        //locError(Float.toString(pageHeight) + "%%" + Float.toString(pdfView.getZoom() * 764));
                        //float c_zoom1 = 1;
                        if (c_zoom != pdfView.getZoom()){
                            c_zoom1 = c_zoom;
                            c_zoom = pdfView.getZoom();
                            if ((c_zoom - c_zoom1) > 0) isZoom = ZOOM_IN;
                            else if ((c_zoom - c_zoom1) < 0) isZoom = ZOOM_OUT;
                        }else isZoom = ZOOM_NONE;
                        locError("zoom: " + Float.toString(c_zoom));
                        getCurrentScreenLoc();
                        double scale_deltaLong = (max_long - min_long) / pageWidth * 100;
                        double scale_distance = DataUtil.algorithm((cs_left + cs_right) / 2, (cs_bottom + cs_top) / 2, (cs_left + cs_right) / 2 + scale_deltaLong, (cs_bottom + cs_top) / 2);
                        scale_distance = scale_distance * 2.53;
                        scaleShow.setText(scale_df.format(scale_distance) + "米");
                        float[] k = RenderUtil.getK(pageWidth, pageHeight, viewer_width, viewer_height);
                        k_w = k[0];
                        k_h = k[1];
                        /*if (isOpenWhiteBlank){
                            //白板功能监控
                            onTouchListenerForView();
                        }*/
                        if (isWhiteBlank){
                            parseAndrawLinesforWhiteBlank(canvas);
                            parseAndrawLinesforWhiteBlank(whiteBlankPt, canvas);
                        }
                        if (isMessure & showMode == CENTERMODE){
                            String messure_pts1 = messure_pts;
                            locError("messure_pts1" + messure_pts1);
                            int poinum_messure1 = poinum_messure + 1;
                            //poinum_messure++;
                            if (poinum_messure1 == 1){
                                poi111 = centerPointLoc;
                                messure_pts1 = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                if (isDrawTrail == TRAIL_DRAW_TYPE){
                                    toolbar.setTitle("正在测量(轨迹记录中)");
                                }else toolbar.setTitle("正在测量");
                            }else if (poinum_messure1 == 2){
                                poi222 = centerPointLoc;
                                messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }else {
                                messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }
                            parseAndrawMessure(messure_pts1, canvas);

                        }
                        if (isMessure & showMode == NOCENTERMODE){
                            parseAndrawMessure(messure_pts, canvas);
                        }
                        //canvas.drawLine(b_bottom_x * ratio_width, (m_top_y - b_bottom_y) * ratio_height, b_top_x * ratio_width, (m_top_y - b_top_y) * ratio_height, paint);
                        if (isGPSEnabled()){
                            PointF pt = new PointF((float)m_lat, (float)m_long);
                            pt = RenderUtil.getPixLocFromGeoL(pt, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            canvas.drawCircle(pt.x, pt.y, 23, paint);
                            canvas.drawCircle(pt.x, pt.y, 20, paint5);
                            canvas.drawCircle(pt.x, pt.y, 10, paint3);
                            canvas.drawArc(pt.x - 35, pt.y - 35, pt.x + 35, pt.y + 35, degree - 105, 30, true, paint3);
                            //canvas.drawArc(pt.x - 100, pt.y + 100, pt.x + 100, pt.y - 100, degree - 15, 30, false, paint3);
                        }else locError("请在手机设置中打开GPS功能, 否则该页面很多功能将无法正常使用");
                        if (isLocateEnd && !m_cTrail.isEmpty() || showTrail){
                            List<Trail> trails = DataSupport.findAll(Trail.class);
                            for (Trail trail : trails){
                                String str1 = trail.getPath();
                                String[] TrailString = str1.split(" ");
                                float[] Trails = new float[TrailString.length];
                                for (int i = 0; i < TrailString.length; i++){
                                    Trails[i] = Float.valueOf(TrailString[i]);
                                }
                                for (int j = 0; j < Trails.length - 2; j = j + 2){
                                    PointF pt11, pt12;
                                    pt11 = RenderUtil.getPixLocFromGeoL(new PointF(Trails[j], Trails[j + 1]), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    pt12 = RenderUtil.getPixLocFromGeoL(new PointF(Trails[j + 2], Trails[j + 3]), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    canvas.drawLine(pt11.x, pt11.y, pt12.x, pt12.y, paint8);
                                }
                            }
                        }
                        if(isDrawType == POI_DRAW_TYPE || showPOI){
                            //List<POI> pois = DataSupport.where("ic = ?", ic).find(POI.class);
                            List<POI> pois = DataSupport.where("x <= " + String.valueOf(max_lat) + ";" +  "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
                            if (pois.size() > 0){
                                for (POI poi : pois){
                                    PointF pt2 = RenderUtil.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                                    //locError(Boolean.toString(poi.getPath().isEmpty()));
                                    //locError(Integer.toString(poi.getPath().length()));
                                    //locError(poi.getPath());
                                    if (poi.getPhotonum() == 0){
                                        if (poi.getTapenum() == 0){
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                                        } else canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint4);
                                    }else {
                                        List<MPHOTO> mphotos = DataSupport.where("poic = ?", poi.getPoic()).find(MPHOTO.class);
                                        if (poi.getTapenum() == 0){
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint4);
                                            //canvas.drawBitmap(, pt2.x, pt2.y - 70, paint1);
                                            int size = bts.size();
                                            if (mphotos.size() != 0) {
                                                for (int i = 0; i < size; i++) {
                                                    if (bts.get(i).getM_path().equals(mphotos.get(0).getPath())) {
                                                        canvas.drawBitmap(bts.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                                                    }
                                                }
                                            }else {
                                                POI poi1 = new POI();
                                                if (mphotos.size() != 0) poi1.setPhotonum(mphotos.size());
                                                else poi1.setToDefault("photonum");
                                                poi1.updateAll("poic = ?", poi.getPoic());
                                            }
                                        }else {
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint1);
                                            //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt2.x, pt2.y - 70, paint4);
                                            int size = bts.size();
                                            if (mphotos.size() != 0) {
                                                for (int i = 0; i < size; i++) {
                                                    if (bts.get(i).getM_path().equals(mphotos.get(0).getPath())) {
                                                        canvas.drawBitmap(bts.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                                                    }
                                                }
                                            }else {
                                                POI poi1 = new POI();
                                                if (mphotos.size() != 0) poi1.setPhotonum(mphotos.size());
                                                else poi1.setToDefault("photonum");
                                                poi1.updateAll("poic = ?", poi.getPoic());
                                            }
                                        }
                                    }
                                }}
                        }
                        if (isAutoTrans & (isZoom == ZOOM_IN || c_zoom == 10)){
                            SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
                            int size = pref1.getInt("num", 0);
                            if (size != 0){
                                float thedelta = 0;
                                int thenum = 0;
                                for (int j = 1; j <= size; j ++){
                                    SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
                                    String str = "n_" + j + "_";
                                    String Muri = pref2.getString(str + "uri", "");
                                    String MGPTS = pref2.getString(str + "GPTS", "");
                                    String[] GPTString = MGPTS.split(" ");
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
                                    //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
                                    //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
                                    locError("see here");
                                    float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                                    float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                                    float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                                    float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                                    if (verifyAreaForAutoTrans(mmax_lat, mmin_lat, mmax_long, mmin_long)){
                                        float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                                        if (j != num_map){
                                        if (thedelta == 0) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }
                                        if (thedelta1 < thedelta) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }
                                        }
                                        locError("delta : " + Float.toString(thedelta) + "thenum : " + Integer.toString(thenum));
                                        /*num_map1 = num_map;
                                        getInfo(j);
                                        toolbar.setTitle(pdfFileName);
                                        getBitmap();
                                        displayFromFile(uri);
                                        isAutoTrans = false;
                                        autoTrans.setBackgroundResource(R.drawable.ic_close_black_24dp);*/
                                    }
                                }
                                double deltaK_trans;
                                //deltaK = (max_lat - min_lat) * 0.02;
                                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, MainInterface.this, ZOOM_IN);
                                locError("deltaK_trans: " + Double.toString(deltaK_trans));
                                if (thenum != num_map & thenum != 0 & thedelta < deltaK_trans){
                                    geometry_whiteBlanks.clear();
                                    num_whiteBlankPt = 0;
                                    isWhiteBlank = false;
                                    whiteBlankPt = "";
                                    num_map1 = num_map;
                                    getInfo(thenum);
                                    manageInfo();
                                    toolbar.setTitle(pdfFileName);
                                    getBitmap();
                                    pdfView.recycle();
                                    displayFromFile(uri);
                                    isAutoTrans = false;
                                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                                    getWhiteBlankData();
                                }
                            }else Toast.makeText(MainInterface.this, "自动切换地图功能出现故障", Toast.LENGTH_SHORT).show();
                        }else if (c_zoom <= 1.5 & isAutoTrans & isZoom == ZOOM_OUT){
                            SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
                            int size = pref1.getInt("num", 0);
                            if (size != 0){
                                float thedelta = 0;
                                int thenum = 0;
                                for (int j = 1; j <= size; j ++){
                                    SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
                                    String str = "n_" + j + "_";
                                    String Muri = pref2.getString(str + "uri", "");
                                    String MGPTS = pref2.getString(str + "GPTS", "");
                                    String[] GPTString = MGPTS.split(" ");
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
                                    //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
                                    //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
                                    locError("see here");
                                    float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                                    float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                                    float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                                    float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                                    if (mmax_lat > max_lat & mmin_lat < min_lat & mmax_long > max_long & mmin_long < min_long){
                                        float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                                        if (thedelta == 0) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }
                                        if (thedelta1 < thedelta) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }
                                        locError("delta : " + Float.toString(thedelta) + "thenum : " + Integer.toString(thenum));

                                    }
                                }
                                double deltaK_trans;
                                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, MainInterface.this, ZOOM_OUT);
                                locError("deltaK_trans: " + Double.toString(deltaK_trans));
                                if (thenum != num_map & thenum != 0 & thedelta < deltaK_trans){
                                    geometry_whiteBlanks.clear();
                                    num_whiteBlankPt = 0;
                                    isWhiteBlank = false;
                                    whiteBlankPt = "";
                                    num_map1 = num_map;
                                    getInfo(thenum);
                                    manageInfo();
                                    toolbar.setTitle(pdfFileName);
                                    getBitmap();
                                    pdfView.recycle();
                                    displayFromFile(uri);
                                    isAutoTrans = false;
                                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                                    getWhiteBlankData();
                                }
                            }else Toast.makeText(MainInterface.this, "自动切换地图功能出现故障", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        toolbar.setTitle(pdfFileName);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isRomance = true;
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 2500);
    }

    float c_zoom1 = 1;
    private void displayFromFile(String filePath) {
        locError("filePath: " + filePath);
        toolbar.setTitle(pdfFileName);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.BLACK);
        final File file = new File(filePath);
        pdfView.fromFile(file)
                .enableSwipe(false)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(false)
                .onLoad(this)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                        current_pageheight = pageHeight;
                        current_pagewidth = pageWidth;
                        viewer_height = pdfView.getHeight();
                        viewer_width = pdfView.getWidth();
                        /*if (isQuery & num_map == 4 & c_zoom > 5 & ( ( cs_bottom > 24.6 & cs_top < 25.3) & ( cs_left > 102.48 & cs_right < 102.97))){
                            getInfo(3);
                            displayFromFile(uri);
                            getScreen();
                            setTitle(pdfFileName);
                            isQuery = false;
                        }
                        if (isQuery & num_map == 3 & c_zoom < 1.2 & ( ( cs_bottom < 24.92 & cs_top > 25.157) & ( cs_left < 102.63 & cs_right > 102.75))){
                            locError("bottom: 我的天哪");
                            getInfo(4);
                            displayFromFile(uri);
                            getScreen();
                            setTitle(pdfFileName);
                            isQuery = false;
                        }*/
                        //float verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
                        if (pdfView.getPositionOffset() != verx & isPos){
                            locHere_fab.setImageResource(R.drawable.ic_location_searching);
                            isPos = false;
                            locError("lzy");
                        }
                        locError("PositionOffset : " + Float.toString(pdfView.getPositionOffset()) + "verx : " + Float.toString(verx));
                        //locError("top: " + Float.toString(cs_top) + " bottom: " + Float.toString(cs_bottom) + " left: " + Float.toString(cs_left) + " right: " + Float.toString(cs_right) + " zoom: " + Float.toString(c_zoom));
                        if (c_zoom != pdfView.getZoom()){
                            c_zoom1 = c_zoom;
                            c_zoom = pdfView.getZoom();
                            if ((c_zoom - c_zoom1) > 0) {
                                locError("zoom: " + Float.toString(c_zoom - c_zoom1));
                                isZoom = ZOOM_IN;
                            }
                            else if ((c_zoom - c_zoom1) < 0) {
                                locError("zoom: " + Float.toString(c_zoom - c_zoom1));
                                isZoom = ZOOM_OUT;
                            }
                        }else isZoom = ZOOM_NONE;
                        locError("zoom: " + Float.toString(c_zoom));
                        getCurrentScreenLoc();
                        double scale_deltaLong = (max_long - min_long) / pageWidth * 100;
                        double scale_distance = DataUtil.algorithm((cs_left + cs_right) / 2, (cs_bottom + cs_top) / 2, (cs_left + cs_right) / 2 + scale_deltaLong, (cs_bottom + cs_top) / 2);
                        scale_distance = scale_distance * 2.53;
                        scaleShow.setText(scale_df.format(scale_distance) + "米");

                        if (isMessure & showMode == CENTERMODE){
                            String messure_pts1 = messure_pts;
                            locError("messure_pts1" + messure_pts1);
                            int poinum_messure1 = poinum_messure + 1;
                            //poinum_messure++;
                            if (poinum_messure1 == 1){
                                poi111 = centerPointLoc;
                                messure_pts1 = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                if (isDrawTrail == TRAIL_DRAW_TYPE){
                                    toolbar.setTitle("正在测量(轨迹记录中)");
                                }else toolbar.setTitle("正在测量");
                            }else if (poinum_messure1 == 2){
                                poi222 = centerPointLoc;
                                messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }else {
                                messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }
                            parseAndrawMessure(messure_pts1, canvas);

                        }
                        if (isMessure & showMode == NOCENTERMODE){
                            parseAndrawMessure(messure_pts, canvas);
                        }

                        /*if (isOpenWhiteBlank){
                        //白板功能监控
                        onTouchListenerForView();
                        }*/
                        //locError(Float.toString(pageHeight) + "%%" + Float.toString(pdfView.getZoom() * 764));
                        float[] k = RenderUtil.getK(pageWidth, pageHeight, viewer_width, viewer_height);
                        k_w = k[0];
                        k_h = k[1];
                        //canvas.drawLine(b_bottom_x * ratio_width, (m_top_y - b_bottom_y) * ratio_height, b_top_x * ratio_width, (m_top_y - b_top_y) * ratio_height, paint);
                        if (isGPSEnabled()){
                            PointF pt = new PointF((float)m_lat, (float)m_long);
                            pt = RenderUtil.getPixLocFromGeoL(pt, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            canvas.drawCircle(pt.x, pt.y, 23, paint);
                            canvas.drawCircle(pt.x, pt.y, 20, paint5);
                            canvas.drawCircle(pt.x, pt.y, 10, paint3);
                            /*if (predegree >= 0 & predegree < 90){
                                locError("您当前处于第一象限");
                                locError(Float.toString(predegree));
                                canvas.drawLine(pt.x, pt.y, (float) (pt.x + 50 * Math.sin(predegree)), (float)(pt.y - 50 * Math.cos(predegree)), paint6);
                            }else if (predegree >= 90 & predegree < 180){
                                locError("您当前处于第二象限");
                                locError(Float.toString(predegree));
                                canvas.drawLine(pt.x, pt.y, (float) (pt.x + 50 * Math.sin(180 - predegree)), (float)(pt.y + 50 * Math.cos(180 - predegree)), paint6);
                            }else if (predegree >= 180 & predegree < 270){
                                locError("您当前处于第三象限");
                                locError(Float.toString(predegree));
                                canvas.drawLine(pt.x, pt.y, (float) (pt.x - 50 * Math.sin(predegree - 180)), (float)(pt.y + 50 * Math.cos(predegree - 180)), paint6);
                            }else {
                                locError("您当前处于第四象限");
                                locError(Float.toString(predegree));
                                canvas.drawLine(pt.x, pt.y, (float) (pt.x - 50 * Math.sin(360 - predegree)), (float)(pt.y - 50 * Math.cos(360 - predegree)), paint6);
                            }*/
                            canvas.drawArc(pt.x - 35, pt.y - 35, pt.x + 35, pt.y + 35, degree - 105, 30, true, paint3);
                        }else locError("请在手机设置中打开GPS功能, 否则该页面很多功能将无法正常使用");
                        if (isLocateEnd && !m_cTrail.isEmpty() || showTrail){
                            List<Trail> trails = DataSupport.findAll(Trail.class);
                            for (Trail trail : trails){
                                String str1 = trail.getPath();
                                String[] TrailString = str1.split(" ");
                                float[] Trails = new float[TrailString.length];
                                for (int i = 0; i < TrailString.length; i++){
                                    Trails[i] = Float.valueOf(TrailString[i]);
                                }
                                for (int j = 0; j < Trails.length - 2; j = j + 2){
                                    PointF pt11, pt12;
                                    pt11 = RenderUtil.getPixLocFromGeoL(new PointF(Trails[j], Trails[j + 1]), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    pt12 = RenderUtil.getPixLocFromGeoL(new PointF(Trails[j + 2], Trails[j + 3]), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    canvas.drawLine(pt11.x, pt11.y, pt12.x, pt12.y, paint8);
                                }
                            }
                        }
                        if (isWhiteBlank){
                            parseAndrawLinesforWhiteBlank(canvas);
                            parseAndrawLinesforWhiteBlank(whiteBlankPt, canvas);
                        }

                        if(isDrawType == POI_DRAW_TYPE || showPOI){
                            //List<POI> pois = DataSupport.where("ic = ?", ic).find(POI.class);
                            List<POI> pois = DataSupport.where("x <= " + String.valueOf(max_lat) + ";" +  "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
                            if (pois.size() > 0){
                                for (POI poi : pois){
                                    PointF pt2 = RenderUtil.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                                    //locError(Boolean.toString(poi.getPath().isEmpty()));
                                    //locError(Integer.toString(poi.getPath().length()));
                                    //locError(poi.getPath());
                                    if (poi.getPhotonum() == 0){
                                        if (poi.getTapenum() == 0){
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                                        } else {
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint4);
                                        }
                                    }else {
                                        List<MPHOTO> mphotos = DataSupport.where("poic = ?", poi.getPoic()).find(MPHOTO.class);
                                        if (poi.getTapenum() == 0){
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint4);
                                            //canvas.drawBitmap(, pt2.x, pt2.y - 70, paint1);
                                            int size = bts.size();
                                            if (mphotos.size() != 0) {
                                                for (int i = 0; i < size; i++) {
                                                    if (bts.get(i).getM_path().equals(mphotos.get(0).getPath())) {
                                                        canvas.drawBitmap(bts.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                                                        locError("lzy");
                                                    }
                                                }
                                            }else {
                                                POI poi2 = new POI();
                                                if (mphotos.size() != 0) poi2.setPhotonum(mphotos.size());
                                                else poi2.setToDefault("photonum");
                                                poi2.updateAll("poic = ?", poi.getPoic());
                                            }
                                        }else {
                                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint1);
                                            //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt2.x, pt2.y - 70, paint4);
                                            int size = bts.size();
                                            if (mphotos.size() != 0) {
                                                for (int i = 0; i < size; i++) {
                                                    if (bts.get(i).getM_path().equals(mphotos.get(0).getPath())) {
                                                        canvas.drawBitmap(bts.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                                                        locError("lzy");
                                                    }
                                                }
                                            }else {
                                                POI poi2 = new POI();
                                                if (mphotos.size() != 0) poi2.setPhotonum(mphotos.size());
                                                else poi2.setToDefault("photonum");
                                                poi2.updateAll("poic = ?", poi.getPoic());
                                            }
                                        }
                                    }
                                }}
                        }
                        if (isAutoTrans & (isZoom == ZOOM_IN || c_zoom == 10)){
                            SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
                            int size = pref1.getInt("num", 0);
                            if (size != 0){
                                float thedelta = 0;
                                int thenum = 0;
                                for (int j = 1; j <= size; j ++){
                                    SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
                                    String str = "n_" + j + "_";
                                    String Muri = pref2.getString(str + "uri", "");
                                    String MGPTS = pref2.getString(str + "GPTS", "");
                                    String[] GPTString = MGPTS.split(" ");
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
                                    //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
                                    //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
                                    locError("see here");
                                    float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                                    float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                                    float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                                    float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                                    if (verifyAreaForAutoTrans(mmax_lat, mmin_lat, mmax_long, mmin_long)){
                                        float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                                        locError("find delta1: " + Float.toString(thedelta1));
                                        locError("find delta: " + Float.toString(thedelta));
                                        locError("find num: " + Integer.toString(j));
                                        if (j != num_map){
                                        if (thedelta == 0) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }
                                        if (thedelta1 < thedelta) {
                                            locError("change!!!");
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }}
                                        locError("delta : " + Float.toString(thedelta) + "thenum : " + Integer.toString(thenum));
                                        /*num_map1 = num_map;
                                        getInfo(j);
                                        toolbar.setTitle(pdfFileName);
                                        getBitmap();
                                        displayFromFile(uri);
                                        isAutoTrans = false;
                                        autoTrans.setBackgroundResource(R.drawable.ic_close_black_24dp);*/
                                    }
                                }
                                double deltaK_trans;
                                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, MainInterface.this, ZOOM_IN);
                                locError("deltaK_trans: " + Double.toString(deltaK_trans));
                                if (thenum != num_map & thenum != 0 & thedelta < deltaK_trans){
                                    geometry_whiteBlanks.clear();
                                    num_whiteBlankPt = 0;
                                    isWhiteBlank = false;
                                    whiteBlankPt = "";
                                    num_map1 = num_map;
                                    getInfo(thenum);
                                    manageInfo();
                                    toolbar.setTitle(pdfFileName);
                                    getBitmap();
                                    pdfView.recycle();
                                    displayFromFile(uri);
                                    isAutoTrans = false;
                                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                                    getWhiteBlankData();
                                }
                            }else Toast.makeText(MainInterface.this, "自动切换地图功能出现故障", Toast.LENGTH_SHORT).show();
                        }else if (c_zoom <= 1.5 & isAutoTrans & isZoom == ZOOM_OUT){
                            SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
                            int size = pref1.getInt("num", 0);
                            if (size != 0){
                                float thedelta = 0;
                                int thenum = 0;
                                for (int j = 1; j <= size; j ++){
                                    SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
                                    String str = "n_" + j + "_";
                                    String Muri = pref2.getString(str + "uri", "");
                                    String MGPTS = pref2.getString(str + "GPTS", "");
                                    String[] GPTString = MGPTS.split(" ");
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
                                    //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
                                    //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
                                    locError("see here");
                                    float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                                    float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                                    float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                                    float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                                    if (mmax_lat > max_lat & mmin_lat < min_lat & mmax_long > max_long & mmin_long < min_long){
                                        float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                                        if (thedelta == 0) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }else if (thedelta1 < thedelta) {
                                            thedelta = thedelta1;
                                            thenum = j;
                                        }
                                        locError("delta : " + Float.toString(thedelta) + "thenum : " + Integer.toString(thenum));

                                    }
                                }
                                double deltaK_trans;
                                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, MainInterface.this, ZOOM_OUT);
                                locError("deltaK_trans: " + Double.toString(deltaK_trans));
                                if (thenum != num_map & thenum != 0 & thedelta < deltaK_trans){
                                    geometry_whiteBlanks.clear();
                                    num_whiteBlankPt = 0;
                                    isWhiteBlank = false;
                                    whiteBlankPt = "";
                                    num_map1 = num_map;
                                    getInfo(thenum);
                                    manageInfo();
                                    toolbar.setTitle(pdfFileName);
                                    getBitmap();
                                    pdfView.recycle();
                                    displayFromFile(uri);
                                    isAutoTrans = false;
                                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                                    getWhiteBlankData();
                                }
                            }else Toast.makeText(MainInterface.this, "自动切换地图功能出现故障", Toast.LENGTH_SHORT).show();
                        }
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
                        PointF pt1 = getGeoLocFromPixL(pt);
                        if (isDrawType == POI_DRAW_TYPE & !isQuery & showMode == NOCENTERMODE){
                            List<POI> POIs = DataSupport.findAll(POI.class);
                            POI poi = new POI();
                            poi.setName("POI" + String.valueOf(POIs.size() + 1));
                            poi.setIc(ic);
                            poi.setX(pt1.x);
                            poi.setY(pt1.y);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            poi.setTime(simpleDateFormat.format(date));
                            poi.setPhotonum(0);
                            poi.setPoic("POI" + String.valueOf(System.currentTimeMillis()));
                            poi.save();
                            locError(pt1.toString());
                            PointF pp = RenderUtil.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            c_zoom = pdfView.getZoom();
                            pdfView.zoomWithAnimation(pp.x, pp.y, c_zoom);
                        }
                        if (isDrawType == POI_DRAW_TYPE & !isQuery & showMode == CENTERMODE){
                            List<POI> POIs = DataSupport.findAll(POI.class);
                            POI poi = new POI();
                            poi.setName("POI" + String.valueOf(POIs.size() + 1));
                            poi.setIc(ic);
                            poi.setX(centerPointLoc.x);
                            poi.setY(centerPointLoc.y);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                            Date date = new Date(System.currentTimeMillis());
                            poi.setTime(simpleDateFormat.format(date));
                            poi.setPhotonum(0);
                            poi.setPoic("POI" + String.valueOf(System.currentTimeMillis()));
                            poi.save();
                            locError(pt1.toString());
                            PointF pp = RenderUtil.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            c_zoom = pdfView.getZoom();
                            pdfView.zoomWithAnimation(pp.x, pp.y, c_zoom);
                        }
                        if (isMessure & showMode == NOCENTERMODE){
                            locError("messure_pts" + messure_pts);
                            poinum_messure++;
                            if (poinum_messure == 1){
                                poi111 = pt1;
                                messure_pts = Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                                if (isDrawTrail == TRAIL_DRAW_TYPE){
                                    toolbar.setTitle("正在测量(轨迹记录中)");
                                }else toolbar.setTitle("正在测量");
                            }else if (poinum_messure == 2){
                                poi222 = pt1;
                                messure_pts = messure_pts + " " + Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }else {
                                messure_pts = messure_pts + " " + Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }

                        }
                        if (isMessure & showMode == CENTERMODE){
                            locError("messure_pts" + messure_pts);
                            poinum_messure++;
                            if (poinum_messure == 1){
                                poi111 = centerPointLoc;
                                messure_pts = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                if (isDrawTrail == TRAIL_DRAW_TYPE){
                                    toolbar.setTitle("正在测量(轨迹记录中)");
                                }else toolbar.setTitle("正在测量");
                            }else if (poinum_messure == 2){
                                poi222 = centerPointLoc;
                                messure_pts = messure_pts + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }else {
                                messure_pts = messure_pts + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                                //setTitle("正在测量");
                                pdfView.zoomWithAnimation(c_zoom);
                                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            }

                        }
                        if (isQuery & isDrawType == NONE_DRAW_TYPE){
                            Log.w(TAG, "onTap: " );
                            List<mPOIobj> pois =  new ArrayList<>();
                            Cursor cursor = DataSupport.findBySQL("select * from POI where x >= ? and x <= ? and y >= ? and y <= ?", String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long));
                            if (cursor.moveToFirst()){
                                do {
                                    String POIC = cursor.getString(cursor.getColumnIndex("poic"));
                                    String time = cursor.getString(cursor.getColumnIndex("time"));
                                    String name = cursor.getString(cursor.getColumnIndex("name"));
                                    String description = cursor.getString(cursor.getColumnIndex("description"));
                                    int tapenum = cursor.getInt(cursor.getColumnIndex("tapenum"));
                                    int photonum = cursor.getInt(cursor.getColumnIndex("photonum"));
                                    float x = cursor.getFloat(cursor.getColumnIndex("x"));
                                    float y = cursor.getFloat(cursor.getColumnIndex("y"));
                                    mPOIobj mPOIobj = new mPOIobj(POIC, x, y, time, tapenum, photonum, name, description);
                                    pois.add(mPOIobj);
                                }while (cursor.moveToNext());
                            }
                            cursor.close();
                            locError("size : " + Integer.toString(pois.size()));
                            int n = 0;
                            int num = 0;
                            if (pois.size() > 0){
                                mPOIobj poii = pois.get(0);
                                PointF pointF = new PointF(poii.getM_X(), poii.getM_Y());
                                pointF = RenderUtil.getPixLocFromGeoL(pointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                pointF = new PointF(pointF.x, pointF.y - 70);
                                //pointF = getGeoLocFromPixL(pointF);
                                pt1 = RenderUtil.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                locError("pt1 : " + pt1.toString());
                                float delta = Math.abs( pointF.x - pt1.x) + Math.abs( pointF.y - pt1.y);
                                for (mPOIobj poi : pois){
                                    PointF mpointF = new PointF(poi.getM_X(), poi.getM_Y());
                                    mpointF = RenderUtil.getPixLocFromGeoL(mpointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                                    mpointF = new PointF(mpointF.x, mpointF.y - 70);
                                    if (Math.abs( mpointF.x - pt1.x) + Math.abs( mpointF.y - pt1.y) < delta && Math.abs( mpointF.x - pt1.x) + Math.abs( mpointF.y - pt1.y) < 35){
                                        locError("mpointF : " + mpointF.toString());
                                        delta = Math.abs( pointF.x - pt1.x) + Math.abs( pointF.y - pt1.y);
                                        num = n;
                                    }
                                    locError("n : " + Integer.toString(n));
                                    n++;
                                }
                                locError("num : " + Integer.toString(num));
                                locError("delta : " + Float.toString(delta));
                                if (delta < 35 || num != 0){
                                    Intent intent = new Intent(MainInterface.this, singlepoi.class);
                                    intent.putExtra("POIC", pois.get(num).getM_POIC());
                                    startActivity(intent);
                                    //locError(Integer.toString(pois.get(num).getPhotonum()));
                                }else locError("没有正常查询");
                            }

                        }
                        return true;
                    }
                })
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isRomance = true;
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 2500);
    }

    private boolean verifyAreaForAutoTrans(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long){
        double deltaLatK, deltaLongK;
        deltaLatK = (max_lat - min_lat) * 0.014;
        deltaLongK = (max_long - min_long) * 0.014;
        if ((mmin_lat - deltaLatK) < cs_bottom & (mmax_long + deltaLongK) > cs_right & (mmin_long - deltaLongK) < cs_left) return true;
        else if ((mmax_lat + deltaLatK) > cs_top & (mmax_long + deltaLongK) > cs_right & (mmin_long - deltaLongK) < cs_left) return true;
        else if ((mmax_lat + deltaLatK) > cs_top & (mmin_lat - deltaLatK) < cs_bottom & (mmin_long - deltaLongK) < cs_left) return true;
        else if ((mmax_lat + deltaLatK) > cs_top & (mmin_lat - deltaLatK) < cs_bottom & (mmax_long + deltaLongK) > cs_right) return true;
        else if ((mmax_lat + deltaLatK) > cs_top & (mmin_lat - deltaLatK) < cs_bottom & (mmax_long + deltaLongK) > cs_right & (mmin_long - deltaLongK) < cs_left) return true;
        else return false;
    }

    private boolean verifyAreaForAutoTrans(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long, int type){
        if (type == 0) {
            double deltaLatK, deltaLongK;
            deltaLatK = (max_lat - min_lat) * 0.014;
            deltaLongK = (max_long - min_long) * 0.014;
            if ((mmin_lat - deltaLatK) < cs_bottom & (mmax_long + deltaLongK) > cs_right & (mmin_long - deltaLongK) < cs_left) return true;
            else if ((mmax_lat + deltaLatK) > cs_top & (mmax_long + deltaLongK) > cs_right & (mmin_long - deltaLongK) < cs_left) return true;
            else if ((mmax_lat + deltaLatK) > cs_top & (mmin_lat - deltaLatK) < cs_bottom & (mmin_long - deltaLongK) < cs_left) return true;
            else if ((mmax_lat + deltaLatK) > cs_top & (mmin_lat - deltaLatK) < cs_bottom & (mmax_long + deltaLongK) > cs_right) return true;
            else if ((mmax_lat + deltaLatK) > cs_top & (mmin_lat - deltaLatK) < cs_bottom & (mmax_long + deltaLongK) > cs_right & (mmin_long - deltaLongK) < cs_left) return true;
            else return false;
        }else {
            if (mmin_lat < cs_bottom & mmax_long > cs_right & mmin_long < cs_left) return true;
            else if (mmax_lat > cs_top & mmax_long > cs_right & mmin_long < cs_left) return true;
            else if (mmax_lat > cs_top & mmin_lat < cs_bottom & mmin_long < cs_left) return true;
            else if (mmax_lat > cs_top & mmin_lat < cs_bottom & mmax_long > cs_right) return true;
            else if (mmax_lat > cs_top & mmin_lat < cs_bottom & mmax_long > cs_right & mmin_long < cs_left) return true;
            else return false;
        }
    }

    //获取文件读取权限
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    //打开图片的文件管理器
    void launchPicker() {
        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setData(Uri.parse(DEF_DIR));
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_CODE_PHOTO);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    int theNum;
    //获取文件管理器的返回信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            theNum = 0;
            final Uri uri = data.getData();
            Log.w(TAG, "onActivityResult: " + DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri));
            final float[] latandlong = new float[2];
            try{
                ExifInterface exifInterface = new ExifInterface(DataUtil.getRealPathFromUriForPhoto(this, uri));
                exifInterface.getLatLong(latandlong);
                locError(String.valueOf(latandlong[0]) + "%" + String.valueOf(latandlong[1]));
                final List<POI> POIs = DataSupport.findAll(POI.class);
                int size = POIs.size();
                float K = (float) 0.002;
                float delta = Math.abs(POIs.get(0).getX() - latandlong[0]) + Math.abs(POIs.get(0).getY() - latandlong[1]);
                    for (int i = 0; i < size; i++){
                        float theLat = POIs.get(i).getX();
                        float theLong = POIs.get(i).getY();
                        float delta1 = Math.abs(theLat - latandlong[0]) + Math.abs(theLong - latandlong[1]);
                        if (delta1 < delta & delta1 < K) {
                            delta = delta1;
                            theNum = i;
                        }
                    }
                    if (delta < K){
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainInterface.this);
                        dialog.setTitle("提示");
                        dialog.setMessage("你想怎样添加照片");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("合并到<" + POIs.get(theNum).getName() + ">点图集", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                POI poi = new POI();
                                poi.setPhotonum(POIs.get(theNum).getPhotonum() + 1);
                                locError("holly :" + poi.updateAll("poic = ?", POIs.get(theNum).getPoic()));
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                Date date = new Date(System.currentTimeMillis());
                                MPHOTO mphoto = new MPHOTO();
                                mphoto.setPdfic(ic);
                                mphoto.setPoic(POIs.get(theNum).getPoic());
                                mphoto.setPath(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri));
                                mphoto.setTime(simpleDateFormat.format(date));
                                mphoto.save();
                                getBitmap();
                                poiLayerBt.setChecked(true);
                                showPOI = true;

                                pdfView.resetZoomWithAnimation();
                            }
                        });
                        dialog.setNegativeButton("创建新兴趣点", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                POI poi = new POI();
                                poi.setIc(ic);
                                long time = System.currentTimeMillis();
                                poi.setPoic("POI" + String.valueOf(time));
                                poi.setPhotonum(1);
                                poi.setName("图片POI" + String.valueOf(POIs.size() + 1));
                                poi.setX(latandlong[0]);
                                poi.setY(latandlong[1]);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                Date date = new Date(System.currentTimeMillis());
                                poi.setTime(simpleDateFormat.format(date));
                                poi.save();
                                MPHOTO mphoto = new MPHOTO();
                                mphoto.setPdfic(ic);
                                mphoto.setPoic("POI" + String.valueOf(time));
                                mphoto.setPath(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri));
                                mphoto.setTime(simpleDateFormat.format(date));
                                mphoto.save();
                                getBitmap();
                                poiLayerBt.setChecked(true);
                                showPOI = true;
                                pdfView.resetZoomWithAnimation();
                            }
                        });
                        dialog.show();
                    }else {
                        POI poi = new POI();
                        poi.setIc(ic);
                        long time = System.currentTimeMillis();
                        poi.setPoic("POI" + String.valueOf(time));
                        poi.setPhotonum(1);
                        poi.setName("图片POI" + String.valueOf(POIs.size() + 1));
                        poi.setX(latandlong[0]);
                        poi.setY(latandlong[1]);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        Date date = new Date(System.currentTimeMillis());
                        poi.setTime(simpleDateFormat.format(date));
                        poi.save();
                        MPHOTO mphoto = new MPHOTO();
                        mphoto.setPdfic(ic);
                        mphoto.setPoic("POI" + String.valueOf(time));
                        mphoto.setPath(DataUtil.getRealPathFromUriForPhoto(this, uri));
                        mphoto.setTime(simpleDateFormat.format(date));
                        mphoto.save();
                        getBitmap();
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                        pdfView.resetZoomWithAnimation();
                    }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TAPE){
            theNum = 0;
            final Uri uri = data.getData();
            final long time = System.currentTimeMillis();
            final List<POI> POIs = DataSupport.findAll(POI.class);
            int size = POIs.size();
            float K = (float) 0.002;
            float delta = Math.abs(POIs.get(0).getX() - (float)m_lat) + Math.abs(POIs.get(0).getY() - (float)m_long);
            for (int i = 0; i < size; i++){
                float theLat = POIs.get(i).getX();
                float theLong = POIs.get(i).getY();
                float delta1 = Math.abs(theLat - (float)m_lat) + Math.abs(theLong - (float)m_long);
                if (delta1 < delta & delta1 < K) {
                    delta = delta1;
                    theNum = i;
                }
            }
            if (delta < K) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainInterface.this);
                dialog.setTitle("提示");
                dialog.setMessage("你想怎样添加音频");
                dialog.setCancelable(false);
                dialog.setPositiveButton("合并到<" + POIs.get(theNum).getName() + ">点音频集", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        POI poi = new POI();
                        poi.setTapenum(POIs.get(theNum).getTapenum() + 1);
                        poi.updateAll("poic = ?", POIs.get(theNum).getPoic());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        Date date = new Date(time);
                        MTAPE mtape = new MTAPE();
                        mtape.setPath(DataUtil.getRealPathFromUriForAudio(MainInterface.this, uri));
                        mtape.setPdfic(ic);
                        mtape.setPoic(POIs.get(theNum).getPoic());
                        mtape.setTime(simpleDateFormat.format(date));
                        mtape.save();
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                        pdfView.resetZoomWithAnimation();
                    }
                });
                dialog.setNegativeButton("创建新兴趣点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String POIC = "POI" + String.valueOf(time);
                        //List<POI> POIs = DataSupport.where("ic = ?", ic).find(POI.class);
                        //List<POI> POIs = DataSupport.findAll(POI.class);
                        POI poi = new POI();
                        poi.setIc(ic);
                        poi.setPoic(POIC);
                        poi.setTapenum(1);
                        poi.setName("录音POI" + String.valueOf(POIs.size() + 1));
                        poi.setX((float) m_lat);
                        poi.setY((float) m_long);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        Date date = new Date(System.currentTimeMillis());
                        poi.setTime(simpleDateFormat.format(date));
                        poi.save();
                        MTAPE mtape = new MTAPE();
                        mtape.setPath(DataUtil.getRealPathFromUriForAudio(MainInterface.this, uri));
                        mtape.setPdfic(ic);
                        mtape.setPoic(POIC);
                        mtape.setTime(simpleDateFormat.format(date));
                        mtape.save();
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                        pdfView.resetZoomWithAnimation();
                    }
                });
                dialog.show();
            }else {
                String POIC = "POI" + String.valueOf(time);
                //List<POI> POIs = DataSupport.where("ic = ?", ic).find(POI.class);
                //List<POI> POIs = DataSupport.findAll(POI.class);
                POI poi = new POI();
                poi.setIc(ic);
                poi.setPoic(POIC);
                poi.setTapenum(1);
                poi.setName("录音POI" + String.valueOf(POIs.size() + 1));
                poi.setX((float) m_lat);
                poi.setY((float) m_long);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                poi.setTime(simpleDateFormat.format(date));
                poi.save();
                MTAPE mtape = new MTAPE();
                mtape.setPath(DataUtil.getRealPathFromUriForAudio(this, uri));
                mtape.setPdfic(ic);
                mtape.setPoic(POIC);
                mtape.setTime(simpleDateFormat.format(date));
                mtape.save();
                poiLayerBt.setChecked(true);
                showPOI = true;
                pdfView.resetZoomWithAnimation();
            }
        }
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO) {
            theNum = 0;
            final String imageuri = DataUtil.getRealPath(imageUri.toString());
            File file = new File(imageuri);
            if (file.length() != 0) {
                final long time = System.currentTimeMillis();
                locError("imageUri : " + imageuri.toString());
                final float[] latandlong = new float[2];
                try{
                    MediaStore.Images.Media.insertImage(getContentResolver(), imageuri, "title", "description");
                    // 最后通知图库更新
                    MainInterface.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imageuri)));
                    ExifInterface exifInterface = new ExifInterface(imageuri);
                    exifInterface.getLatLong(latandlong);
                    locError("see here" + String.valueOf(latandlong[0]) + "%" + String.valueOf(latandlong[1]));
                    //List<POI> POIs = DataSupport.where("ic = ?", ic).find(POI.class);

                    final List<POI> POIs = DataSupport.findAll(POI.class);
                    int size = POIs.size();
                    float K = (float) 0.002;
                    float delta = Math.abs(POIs.get(0).getX() - (float)m_lat) + Math.abs(POIs.get(0).getY() - (float)m_long);
                    for (int i = 0; i < size; i++){
                        float theLat = POIs.get(i).getX();
                        float theLong = POIs.get(i).getY();
                        float delta1 = Math.abs(theLat - (float)m_lat) + Math.abs(theLong - (float)m_long);
                        if (delta1 < delta & delta1 < K) {
                            delta = delta1;
                            theNum = i;
                        }
                    }
                    if (delta < K) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainInterface.this);
                        dialog.setTitle("提示");
                        dialog.setMessage("你想怎样添加照片");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("合并到<" + POIs.get(theNum).getName() + ">点图集", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                POI poi = new POI();
                                poi.setPhotonum(POIs.get(theNum).getPhotonum() + 1);
                                poi.updateAll("poic = ?", POIs.get(theNum).getPoic());
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                Date date = new Date(time);
                                MPHOTO mphoto = new MPHOTO();
                                mphoto.setPdfic(ic);
                                mphoto.setPoic(POIs.get(theNum).getPoic());
                                mphoto.setPath(imageuri);
                                mphoto.setTime(simpleDateFormat.format(date));
                                mphoto.save();
                                getBitmap();
                                poiLayerBt.setChecked(true);
                                showPOI = true;
                                pdfView.resetZoomWithAnimation();
                            }
                        });
                        dialog.setNegativeButton("创建新兴趣点", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                POI poi = new POI();
                                poi.setIc(ic);
                                //long time = System.currentTimeMillis();
                                poi.setPoic("POI" + String.valueOf(time));
                                poi.setPhotonum(1);
                                poi.setName("图片POI" + String.valueOf(POIs.size() + 1));
                                if (latandlong[0] != 0 & latandlong[1] != 0) {
                                    poi.setX(latandlong[0]);
                                    poi.setY(latandlong[1]);
                                } else {
                                    poi.setX((float) m_lat);
                                    poi.setY((float) m_long);
                                }
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                Date date = new Date(System.currentTimeMillis());
                                poi.setTime(simpleDateFormat.format(date));
                                poi.save();
                                MPHOTO mphoto = new MPHOTO();
                                mphoto.setPdfic(ic);
                                mphoto.setPoic("POI" + String.valueOf(time));
                                mphoto.setPath(imageuri);
                                mphoto.setTime(simpleDateFormat.format(date));
                                mphoto.save();
                                getBitmap();
                                poiLayerBt.setChecked(true);
                                showPOI = true;
                                pdfView.resetZoomWithAnimation();
                            }
                        });
                        dialog.show();
                    }else {
                        //List<POI> POIs = DataSupport.findAll(POI.class);
                        POI poi = new POI();
                        poi.setIc(ic);
                        //long time = System.currentTimeMillis();
                        poi.setPoic("POI" + String.valueOf(time));
                        poi.setPhotonum(1);
                        poi.setName("图片POI" + String.valueOf(POIs.size() + 1));
                        if (latandlong[0] != 0 & latandlong[1] != 0) {
                            poi.setX(latandlong[0]);
                            poi.setY(latandlong[1]);
                        } else {
                            poi.setX((float) m_lat);
                            poi.setY((float) m_long);
                        }
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        Date date = new Date(System.currentTimeMillis());
                        poi.setTime(simpleDateFormat.format(date));
                        poi.save();
                        MPHOTO mphoto = new MPHOTO();
                        mphoto.setPdfic(ic);
                        mphoto.setPoic("POI" + String.valueOf(time));
                        mphoto.setPath(imageuri);
                        mphoto.setTime(simpleDateFormat.format(date));
                        mphoto.save();
                        getBitmap();
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                        pdfView.resetZoomWithAnimation();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }else {
                file.delete();
                Toast.makeText(MainInterface.this, "拍照失败, 请再次拍摄", Toast.LENGTH_LONG).show();
            }
            locError(imageUri.toString());
            //String imageuri = getRealPathFromUriForPhoto(this, imageUri);


        }
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
                pickFile();
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

    private void showPopueWindowForMessure(){
        View popView = View.inflate(this,R.layout.popupwindow_messure,null);
        Button bt_distance = (Button) popView.findViewById(R.id.btn_pop_distance);
        Button bt_area = (Button) popView.findViewById(R.id.btn_pop_area);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 1/3;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        //popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
        //点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);

        bt_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locHere_fab.setVisibility(View.GONE);
                centerPointModeBt.setVisibility(View.GONE);
                popupWindow.dismiss();
                delete_messure_fab.setVisibility(View.VISIBLE);
                backpt_messure_fab.setVisibility(View.VISIBLE);
                cancel_messure_fab.setVisibility(View.VISIBLE);
                whiteBlank_fab.setVisibility(View.GONE);
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle("正在测量(轨迹记录中)");
                }else toolbar.setTitle("正在测量");
                isQuery = false;
                isDrawType = NONE_DRAW_TYPE;
                isLocate = 0;
                isLocateEnd = true;
                poiLayerBt.setChecked(false);
                showPOI = false;
                isMessure = true;
                isMessureType = MESSURE_DISTANCE_TYPE;
                poinum_messure = 0;
            }
        });
        bt_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locHere_fab.setVisibility(View.GONE);
                centerPointModeBt.setVisibility(View.GONE);
                popupWindow.dismiss();
                delete_messure_fab.setVisibility(View.VISIBLE);
                backpt_messure_fab.setVisibility(View.VISIBLE);
                cancel_messure_fab.setVisibility(View.VISIBLE);
                whiteBlank_fab.setVisibility(View.GONE);
                //Toast.makeText(MainInterface.this, "暂时没有添加面积测算功能",Toast.LENGTH_SHORT).show();
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle("正在测量(轨迹记录中)");
                }else toolbar.setTitle("正在测量");
                isQuery = false;
                isDrawType = NONE_DRAW_TYPE;
                isLocate = 0;
                isLocateEnd = true;
                poiLayerBt.setChecked(false);
                showPOI = false;
                isMessure = true;
                isMessureType = MESSURE_AREA_TYPE;
                poinum_messure = 0;
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

    //记录当前记录的点数
    int num_drawpt;
    //记录白板绘图坐标点对数量
    int num_whiteBlankPt;
    //记录是否处于白板画图状态
    private boolean isWhiteBlank = false;
    //记录画笔颜色
    private int color_Whiteblank;
    //记录白板绘图独立字符串
    private String whiteBlankPt = "";
    //记录当前List<geometry_WhiteBlank>
    private List<geometry_WhiteBlank> geometry_whiteBlanks;

    private void showPopueWindowForWhiteblank(){
        final View popView = View.inflate(this,R.layout.popupwindow_whiteblank,null);
        isWhiteBlank = true;
        FloatingActionButton back_pop = (FloatingActionButton) popView.findViewById(R.id.back_pop) ;
        back_pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Lines_WhiteBlank> lines_whiteBlanks = DataSupport.where("ic = ?", ic).find(Lines_WhiteBlank.class);
                int size = lines_whiteBlanks.size();
                for (int kk = 0; kk < size; kk++){
                    Log.w(TAG, "onClick: " + lines_whiteBlanks.get(kk).getMmid());
                }
                int size1 = geometry_whiteBlanks.size();
                if (size <= 0) {
                    whiteBlankPt = "";
                    //DataSupport.deleteAll(Lines_WhiteBlank.class, "ic = ?", ic);
                    geometry_whiteBlanks.clear();
                    pdfView.zoomWithAnimation(c_zoom);
                    Toast.makeText(MainInterface.this, "已清空当前画板", Toast.LENGTH_SHORT).show();
                }else {
                    whiteBlankPt = "";
                    Log.w(TAG, "onClick: " + DataSupport.deleteAll(Lines_WhiteBlank.class, "mmid = ? and ic = ?", Integer.toString(size - 1), ic));
                    if (size1 != 0) geometry_whiteBlanks.remove(size1 - 1);
                    pdfView.zoomWithAnimation(c_zoom);
                    //Toast.makeText(MainInterface.this, "已清空当前画板", Toast.LENGTH_SHORT).show();
                }
            }
        });
        FloatingActionButton fff = (FloatingActionButton) popView.findViewById(R.id.colorSeeker_pop);
        fff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(MainInterface.this)
                        .setTitle("选择颜色")
                        .initialColor(Color.RED)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                locError("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("确定", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                //changeBackgroundColor(selectedColor);
                                locError(Integer.toString(selectedColor));
                                color_Whiteblank = selectedColor;
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });

        FloatingActionButton eraseContent = (FloatingActionButton) popView.findViewById(R.id.eraseContent);
        eraseContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Lines_WhiteBlank> lines_whiteBlanks = DataSupport.where("ic = ?", ic).find(Lines_WhiteBlank.class);
                int size = lines_whiteBlanks.size();
                if (size <= 0) {
                    whiteBlankPt = "";
                    //DataSupport.deleteAll(Lines_WhiteBlank.class, "ic = ?", ic);
                    geometry_whiteBlanks.clear();
                    pdfView.zoomWithAnimation(c_zoom);
                    Toast.makeText(MainInterface.this, "已清空当前画板", Toast.LENGTH_SHORT).show();
                }else {
                    whiteBlankPt = "";
                    DataSupport.deleteAll(Lines_WhiteBlank.class, "ic = ?", ic);
                    geometry_whiteBlanks.clear();
                    pdfView.zoomWithAnimation(c_zoom);
                    Toast.makeText(MainInterface.this, "已清空当前画板", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton popwhiteblank = (FloatingActionButton) popView.findViewById(R.id.whiteblank_pop);
        FrameLayout frameLayout = (FrameLayout) popView.findViewById(R.id.fml_pop);
        //获取屏幕宽高
        final int weight = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels - 60;

        final PopupWindow popupWindow = new PopupWindow(popView, weight ,height);
        //popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
        //点击外部popueWindow消失
        popupWindow.setOutsideTouchable(true);
        //popupWindow消失屏幕变为不透明
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1.0f;
                getWindow().setAttributes(lp);
            }
        });
        //popupWindow OnTouchListener
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //按下
                        locError("按下!!!");
                        whiteBlankPt = "";
                        num_whiteBlankPt = 0;
                        break;
                    case MotionEvent.ACTION_UP:
                        //抬起
                        locError("抬起!!!");
                        //Toast.makeText(MainInterface.this, "抬起", Toast.LENGTH_SHORT).show();
                        geometry_WhiteBlank geometry_whiteBlank = new geometry_WhiteBlank(ic, whiteBlankPt, color_Whiteblank);
                        geometry_whiteBlanks.add(geometry_whiteBlank);
                        List<Lines_WhiteBlank> liness = DataSupport.where("ic = ?", ic).find(Lines_WhiteBlank.class);
                        Log.w(TAG, "onTouch: " + liness.size());
                        int size = liness.size();
                        Lines_WhiteBlank lines = new Lines_WhiteBlank();
                        lines.setIc(ic);
                        lines.setColor(color_Whiteblank);
                        lines.setLines(whiteBlankPt);
                        lines.setMmid(size);
                        lines.save();
                        break;
                }

                PointF pt = new PointF(event.getRawX(), event.getRawY());
                pt = getGeoLocFromPixL(pt);
                locError("RawX : " + pt.x + "; RawY : " + pt.y);
                //pt_last = pt_current;
                //pt_current = pt;
                pdfView.zoomWithAnimation(c_zoom);
                /*if (event.getRawY() >= height - 100 & event.getRawY() <= height & event.getRawX() >= 50 & event.getRawX() <= 150){
                    popupWindow.dismiss();
                    whiteblank.setImageResource(R.drawable.ic_brush_black_24dp);
                }*/
                if (isWhiteBlank){
                    num_whiteBlankPt ++;
                    if (num_whiteBlankPt == 1){
                        whiteBlankPt = Float.toString(pt.x) + " " + Float.toString(pt.y);
                    }else if (num_whiteBlankPt == 2){
                        whiteBlankPt = whiteBlankPt + " " + Float.toString(pt.x) + " " + Float.toString(pt.y);
                        //setTitle("正在测量");
                        pdfView.zoomWithAnimation(c_zoom);
                        //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                        //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    }else {
                        whiteBlankPt = whiteBlankPt + " " + Float.toString(pt.x) + " " + Float.toString(pt.y);
                        //setTitle("正在测量");
                        pdfView.zoomWithAnimation(c_zoom);
                        //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    }

                }

                return true;
            }
        });
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.NO_GRAVITY,0,0);
        popwhiteblank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                whiteBlank_fab.setVisibility(View.VISIBLE);
                isOpenWhiteBlank = false;
                whiteBlank_fab.setImageResource(R.drawable.ic_brush_black_24dp);
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle("轨迹记录中");
                }else toolbar.setTitle(pdfFileName);
            }
        });
    }

    //获取当前屏幕所视区域的经纬度与像素范围
    private void getCurrentScreenLoc(){
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
        //

        if (showMode == CENTERMODE) {
            centerPointLoc = new PointF((cs_bottom + cs_top) / 2, (cs_left + cs_right) / 2);
            DecimalFormat df = new DecimalFormat("0.0000");
            if (isCoordinateType == COORDINATE_DEFAULT_TYPE) {
                textView.setText(df.format(centerPointLoc.x) + ";" + df.format(centerPointLoc.y));
            } else if (isCoordinateType == COORDINATE_BLH_TYPE) {
                textView.setText(Integer.toString((int) centerPointLoc.x) + "°" + Integer.toString((int) ((centerPointLoc.x - (int) centerPointLoc.x) * 60)) + "′" + Integer.toString((int) (((centerPointLoc.x - (int) centerPointLoc.x) * 60 - (int) ((centerPointLoc.x - (int) centerPointLoc.x) * 60)) * 60)) + "″;" + Integer.toString((int) centerPointLoc.y) + "°" + Integer.toString((int) ((centerPointLoc.y - (int) centerPointLoc.y) * 60)) + "′" + Integer.toString((int) (((centerPointLoc.y - (int) centerPointLoc.y) * 60 - (int) ((centerPointLoc.y - (int) centerPointLoc.y) * 60)) * 60)) + "″");
            } else {
                textView.setText(df.format(centerPointLoc.x) + ";" + df.format(centerPointLoc.y));
            }
        }
        //
        //cs_top = pdfView.getCurrentYOffset()
    }
    //声明中心点的位置坐标
    PointF centerPointLoc;

    //屏幕坐标位置到经纬度转化
    private PointF getGeoLocFromPixL(PointF pt){
        //textView = (TextView) findViewById(R.id.txt);
        DecimalFormat df = new DecimalFormat("0.0000");
        //精确定位算法
        float xxxx, yyyy;
        if (current_pageheight < viewer_height || current_pagewidth < viewer_width) {
            xxxx = ((pt.x - (screen_width - viewer_width + k_w)));
            yyyy = ((pt.y - (screen_height - viewer_height + k_h)));
            if (pt.y >= (screen_height - viewer_height + k_h) && pt.y <= (screen_height - viewer_height + k_h + current_pageheight) && pt.x >= (screen_width - viewer_width + k_w) && pt.x <= (screen_width - viewer_width + k_w + current_pagewidth)) {
                pt.x = (float)(max_lat - (yyyy) / current_pageheight * ( max_lat - min_lat));
                pt.y = (float)(( xxxx) / current_pagewidth * ( max_long - min_long) + min_long);
                locLatForTap = pt.x;
                locLongForTap = pt.y;
                if (isCoordinateType == COORDINATE_DEFAULT_TYPE){
                    textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
                }else if (isCoordinateType == COORDINATE_BLH_TYPE){
                    textView.setText(Integer.toString((int)locLatForTap) + "°" + Integer.toString((int)(( locLatForTap - (int)locLatForTap) * 60)) + "′" + Integer.toString((int)((( locLatForTap - (int)locLatForTap) * 60 - (int)(( locLatForTap - (int)locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int)locLongForTap) + "°" + Integer.toString((int)(( locLongForTap - (int)locLongForTap) * 60)) + "′" + Integer.toString((int)((( locLongForTap - (int)locLongForTap) * 60 - (int)(( locLongForTap - (int)locLongForTap) * 60)) * 60)) + "″");
                }else textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
            } else {
                textView.setText("点击位置在区域之外");
            }
        } else {
            xxxx = pt.x - (screen_width - viewer_width);
            yyyy = pt.y - (screen_height - viewer_height);
            pt.x = (float)(max_lat - ( yyyy - pdfView.getCurrentYOffset()) / current_pageheight * ( max_lat - min_lat));
            pt.y = (float)(( xxxx - pdfView.getCurrentXOffset()) / current_pagewidth * ( max_long - min_long) + min_long);
            locLatForTap = pt.x;
            locLongForTap = pt.y;
            if (isCoordinateType == COORDINATE_DEFAULT_TYPE){
                textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
            }else if (isCoordinateType == COORDINATE_BLH_TYPE){
                textView.setText(Integer.toString((int)locLatForTap) + "°" + Integer.toString((int)(( locLatForTap - (int)locLatForTap) * 60)) + "′" + Integer.toString((int)((( locLatForTap - (int)locLatForTap) * 60 - (int)(( locLatForTap - (int)locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int)locLongForTap) + "°" + Integer.toString((int)(( locLongForTap - (int)locLongForTap) * 60)) + "′" + Integer.toString((int)((( locLongForTap - (int)locLongForTap) * 60 - (int)(( locLongForTap - (int)locLongForTap) * 60)) * 60)) + "″");
            }else {
                textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
            }
        }
        return pt;
        //
    }

    //判断该像素点位置是否在地图区域内
    public static int verifyPixL(PointF pt, float k_w, float k_h, float screen_width, float screen_height, float viewer_width, float viewer_height, float current_pagewidth, float current_pageheight){
        final int OUT = -1;
        final int IN_FULL = 1;
        final int IN_NOTFULL = 2;
        if (current_pageheight < viewer_height || current_pagewidth < viewer_width) {
            if (pt.y >= (screen_height - viewer_height + k_h) && pt.y <= (screen_height - viewer_height + k_h + current_pageheight) && pt.x >= (screen_width - viewer_width + k_w) && pt.x <= (screen_width - viewer_width + k_w + current_pagewidth)) return IN_NOTFULL;
            else return OUT;
        } else return IN_FULL;
    }

    //屏幕坐标位置到经纬度转化1
    private PointF getGeoLocFromPixLForFull(PointF pt){
        //精确定位算法
        float xxxx, yyyy;
        xxxx = pt.x - (screen_width - viewer_width);
        yyyy = pt.y - (screen_height - viewer_height);
        pt.x = (float)(max_lat - ( yyyy - pdfView.getCurrentYOffset()) / current_pageheight * ( max_lat - min_lat));
        pt.y = (float)(( xxxx - pdfView.getCurrentXOffset()) / current_pagewidth * ( max_long - min_long) + min_long);
        locLatForTap = pt.x;
        locLongForTap = pt.y;
        return pt;
        //
    }

    //屏幕坐标位置到经纬度转化2
    private PointF getGeoLocFromPixLForNotFull(PointF pt){
        //精确定位算法
        float xxxx, yyyy;
            xxxx = ((pt.x - (screen_width - viewer_width + k_w)));
            yyyy = ((pt.y - (screen_height - viewer_height + k_h)));
            pt.x = (float) (max_lat - (yyyy) / current_pageheight * (max_lat - min_lat));
            pt.y = (float) ((xxxx) / current_pagewidth * (max_long - min_long) + min_long);
            locLatForTap = pt.x;
            locLongForTap = pt.y;
        return pt;
        //
    }

    public void locError(String str){
        Log.e(TAG, "debug: " + str );
    }

    //获取当前坐标位置
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

    //坐标监听器
    protected final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "Location changed to: " + getLocationInfo(location));
            updateView(location);
            if (!isLocateEnd) {
                recordTrail((float)location.getLatitude(), (float)location.getLongitude());
                locError(m_cTrail);
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

    //更新坐标信息
    private void updateView(Location location) {
        /*
        locError("isFullLocation : " + Boolean.toString(isFullLocation));
        locError("location : " + location.toString());
        if(isFullLocation & location != null){
        Geocoder gc = new Geocoder(MainInterface.this);
        List<Address> addresses = null;
        String msg = "";
        Log.d(TAG, "updateView.location = " + location);
            try {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                //Log.d(TAG, "updateView.addresses = " + Integer.toString(addresses.size()));
                if (addresses.size() > 0) Toast.makeText(MainInterface.this, "当前位置: " + addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
                else Toast.makeText(this, "你当前没有连接网络, 无法进行详细地址查询", Toast.LENGTH_LONG).show();
                Log.d(TAG, "updateView.addresses = " + addresses);
                if (addresses.size() > 0) {
                    msg += addresses.get(0).getAdminArea().substring(0,2);
                    msg += " " + addresses.get(0).getLocality().substring(0,2);
                    Log.d(TAG, "updateView.addresses = " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        if (location != null){
            m_lat = location.getLatitude();
            m_long = location.getLongitude();
            verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
            //setHereLocation();
            locError(Double.toString(m_lat) + "&&" + Double.toString(m_long) + "Come here");
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (isDrawTrail == TRAIL_DRAW_TYPE){
            popBackWindow("Back");
        }else super.onBackPressed();
    }

    //退出提醒弹窗
    public void popBackWindow(String str)
    {
        if (str == "Destroy"){
            AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
            backAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isDrawType = NONE_DRAW_TYPE;
                    isDrawTrail = NONE_DRAW_TYPE;
                    isLocateEnd = true;
                    recordTrail(last_x, last_y);
                    locError(m_cTrail);
                    invalidateOptionsMenu();
                    Intent stop_mService = new Intent(MainInterface.this, RecordTrail.class);
                    stopService(stop_mService);
                    /*Trail trail = new Trail();
                    List<Trail> trails = DataSupport.where("ic = ?", ic).find(Trail.class);
                    trail.setIc(ic);
                    trail.setName("路径" + Integer.toString(trails.size() + 1));
                    trail.setPath(m_cTrail);
                    trail.save();*/
                    List<Trail> trails = DataSupport.findAll(Trail.class);
                    locError("当前存在: " + Integer.toString(trails.size()) + "条轨迹");
                    MainInterface.this.finish();
                }
            });
            backAlert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            backAlert.setMessage("确定要取消轨迹绘制吗?");
            backAlert.setTitle("警告");
            backAlert.show();
        }else {

            AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
            backAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent stop_mService = new Intent(MainInterface.this, RecordTrail.class);
                    stopService(stop_mService);
                    List<Trail> trails = DataSupport.findAll(Trail.class);
                    locError("当前存在: " + Integer.toString(trails.size()) + "条轨迹");
                    System.exit(0);
                }
            });
            backAlert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            backAlert.setMessage("确定要取消轨迹绘制吗?");
            backAlert.setTitle("警告");
            backAlert.show();
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (isDrawTrail){
            case TRAIL_DRAW_TYPE:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.back).setVisible(false);
                menu.findItem(R.id.queryPOI).setVisible(true);
                menu.findItem(R.id.query).setVisible(false);
                break;
            case NONE_DRAW_TYPE:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.back).setVisible(true);
                menu.findItem(R.id.queryPOI).setVisible(true);
                menu.findItem(R.id.query).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void reDrawCache() {
        if (FILE_TYPE == NONE_FILE_TYPE) {
            Toast.makeText(this, "PDF文件读取出现问题", Toast.LENGTH_LONG).show();
        } else if (FILE_TYPE == FILE_FILE_TYPE) {
            displayFromFile(uri);
            Toast.makeText(this, "这是硬盘上的文件", Toast.LENGTH_LONG).show();
        } else if (FILE_TYPE == ASSET_FILE_TYPE) {
            displayFromAsset("Demo");
            Toast.makeText(this, "这是Demo", Toast.LENGTH_LONG).show();
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

    //记录是否可以看缩略图
    private boolean isCreateBitmap = false;


    //选择框声明
    CheckBox poiLayerBt;
    CheckBox trailLayerBt;
    CheckBox whiteBlankLayerBt;
    //显示模式声明
    CheckBox centerPointModeBt;
    //中心点声明
    ImageView centerPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        //中心点图标初始化
        centerPoint = (ImageView) findViewById(R.id.centerPoint);
        centerPointModeBt = (CheckBox) findViewById(R.id.centerPointMode);
        centerPointModeBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    showMode = CENTERMODE;
                    centerPoint.setVisibility(View.VISIBLE);
                    isQuery = true;
                    isDrawType = NONE_DRAW_TYPE;
                    locError("中心点模式");
                    isMessureType = MESSURE_NONE_TYPE;
                    query_poi_imgbt.setVisibility(View.INVISIBLE);
                }else {
                    locError("不是中心点模式");
                    showMode = NOCENTERMODE;
                    centerPoint.setVisibility(View.INVISIBLE);
                    isQuery = false;
                    query_poi_imgbt.setVisibility(View.VISIBLE);
                }
            }
        });
        //图层控制按钮初始化
        poiLayerBt = (CheckBox) findViewById(R.id.poiLayer);
        poiLayerBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    showPOI = true;
                    pdfView.zoomWithAnimation(c_zoom);
                }else {
                    showPOI = false;
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        trailLayerBt = (CheckBox) findViewById(R.id.trailLayer);
        trailLayerBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    showTrail = true;
                    pdfView.zoomWithAnimation(c_zoom);
                }else {
                    showTrail = false;
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        whiteBlankLayerBt = (CheckBox) findViewById(R.id.whiteBlankLayer) ;
        whiteBlankLayerBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    isWhiteBlank = true;
                    pdfView.zoomWithAnimation(c_zoom);
                }else {
                    isWhiteBlank = false;
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        //初始化测量相关按钮
        cancel_messure_fab = (FloatingActionButton) findViewById(R.id.cancel_messure);
        cancel_messure_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showMode == CENTERMODE) isQuery = true;
                else isQuery = false;
                centerPointModeBt.setVisibility(View.VISIBLE);
                isMessure = false;
                poinum_messure = 0;
                messure_pts = "";
                delete_messure_fab.setVisibility(View.GONE);
                backpt_messure_fab.setVisibility(View.GONE);
                cancel_messure_fab.setVisibility(View.GONE);
                whiteBlank_fab.setVisibility(View.VISIBLE);
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle("轨迹记录中");
                }else toolbar.setTitle(pdfFileName);
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        delete_messure_fab = (FloatingActionButton) findViewById(R.id.delete_messure);
        delete_messure_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messure_pts = "";
                poinum_messure = 0;
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    toolbar.setTitle("正在测量(轨迹记录中)");
                }else toolbar.setTitle("正在测量");
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        backpt_messure_fab = (FloatingActionButton) findViewById(R.id.backpts_messure);
        backpt_messure_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (poinum_messure > 1) {
                    messure_pts = messure_pts.substring(0, messure_pts.lastIndexOf(" "));
                    messure_pts = messure_pts.substring(0, messure_pts.lastIndexOf(" "));
                    poinum_messure--;
                    pdfView.zoomWithAnimation(c_zoom);
                }else {
                    messure_pts = "";
                    poinum_messure = 0;
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        //初始化白板要素List
        geometry_whiteBlanks = new ArrayList<geometry_WhiteBlank>();
        //初始化白板按钮
        whiteBlank_fab = (FloatingActionButton) findViewById(R.id.whiteBlank);
        whiteBlank_fab.setImageResource(R.drawable.ic_brush_black_24dp);
        whiteBlank_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpenWhiteBlank){
                    isOpenWhiteBlank = true;
                    //whiteBlank_fab.setImageResource(R.drawable.ic_cancel_black_24dp);
                    if (isDrawTrail == TRAIL_DRAW_TYPE){
                        toolbar.setTitle("正在进行白板绘图(轨迹记录中)");
                    }else toolbar.setTitle("正在进行白板绘图");
                    showPopueWindowForWhiteblank();
                    whiteBlank_fab.setVisibility(View.INVISIBLE);
                    isWhiteBlank = true;
                    whiteBlankLayerBt.setChecked(true);
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        //初始化比例尺格式信息
        scale_df = new DecimalFormat("0.0");
        //初始化比例尺信息
        scaleShow = (TextView) findViewById(R.id.scale);
        //获取传感器管理器系统服务
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolBar1);
        setSupportActionBar(toolbar);
        autoTrans_imgbt = (ImageButton) findViewById(R.id.trans);
        autoTrans_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAutoTrans){
                    isAutoTrans = false;
                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                }else {
                    isAutoTrans = true;
                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_check_black_24dp);
                }
            }
        });
        textView = (TextView) findViewById(R.id.txt);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DecimalFormat df = new DecimalFormat("0.0000");
                String str = (String) textView.getText();
                if (!str.equals("点击位置在区域之外") & !str.equals("在这里显示坐标值")){
                if (isCoordinateType == COORDINATE_DEFAULT_TYPE){
                    //String[] strs = str.split(";");
                    //PointF pt = new PointF(Float.valueOf(strs[0]), Float.valueOf(strs[1]));
                    textView.setText(Integer.toString((int)locLatForTap) + "°" + Integer.toString((int)(( locLatForTap - (int)locLatForTap) * 60)) + "′" + Integer.toString((int)((( locLatForTap - (int)locLatForTap) * 60 - (int)(( locLatForTap - (int)locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int)locLongForTap) + "°" + Integer.toString((int)(( locLongForTap - (int)locLongForTap) * 60)) + "′" + Integer.toString((int)((( locLongForTap - (int)locLongForTap) * 60 - (int)(( locLongForTap - (int)locLongForTap) * 60)) * 60)) + "″");
                    isCoordinateType = COORDINATE_BLH_TYPE;
                    locError(Integer.toString(textView.getHeight()));
                }else if (isCoordinateType == COORDINATE_BLH_TYPE){
                    //String[] strs = str.split(";");
                    //locError(strs[0] + "还有: " + strs[1]);
                    //PointF pt = new PointF(Float.valueOf(strs[0].substring(0, strs[0].indexOf("°"))) + (Float.valueOf(strs[0].substring(strs[0].indexOf("°") + 1, strs[0].indexOf("′"))) / 60) + (Float.valueOf(strs[0].substring(strs[0].indexOf("′") + 1, strs[0].indexOf("″"))) / 3600), Float.valueOf(strs[1].substring(0, strs[1].indexOf("°"))) + (Float.valueOf(strs[1].substring(strs[1].indexOf("°") + 1, strs[1].indexOf("′"))) / 60) + (Float.valueOf(strs[1].substring(strs[1].indexOf("′") + 1, strs[1].indexOf("″"))) / 3600));
                    textView.setText(df.format(locLatForTap) + "; " + df.format(locLongForTap));
                    isCoordinateType = COORDINATE_DEFAULT_TYPE;
                    locError(Integer.toString(textView.getHeight()));
                }
                }
            }
        });
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(textView.getText());
                Toast.makeText(MainInterface.this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth((float)3.0);
        paint.setStyle(Paint.Style.FILL);
        paint1 = new Paint();
        paint1.setColor(Color.GREEN);
        paint1.setStrokeWidth((float)2.0);
        paint1.setStyle(Paint.Style.FILL);
        paint2 = new Paint();
        paint2.setColor(Color.BLACK);
        paint2.setStrokeWidth((float)2.0);
        paint2.setStyle(Paint.Style.FILL);
        paint3 = new Paint();
        paint3.setColor(Color.BLUE);
        paint3.setStrokeWidth((float)2.0);
        paint3.setStyle(Paint.Style.FILL);
        paint4 = new Paint();
        paint4.setColor(Color.YELLOW);
        paint4.setStrokeWidth((float)2.0);
        paint4.setStyle(Paint.Style.FILL);
        paint5 = new Paint();
        paint5.setColor(Color.rgb(123, 175, 212));
        paint5.setStrokeWidth((float)2.0);
        paint5.setStyle(Paint.Style.FILL);
        paint6 = new Paint();
        paint6.setStrokeWidth(10);
        paint6.setStyle(Paint.Style.STROKE);
        paint6.setColor(Color.YELLOW);
        paint8 = new Paint();
        paint8.setStrokeWidth(10);
        paint8.setStyle(Paint.Style.STROKE);
        paint8.setColor(Color.BLUE);
        color_Whiteblank = Color.RED;
        setSupportActionBar(toolbar);
        final Intent intent = getIntent();
        int m_num = intent.getIntExtra("num", 0);
        getInfo(m_num);
        manageInfo();
        num_map1 = m_num;
        linearLayout = (LinearLayout) findViewById(R.id.search);
        if (uri != "") {
            FILE_TYPE = FILE_FILE_TYPE;
            displayFromFile(uri);
        } else {
            FILE_TYPE = ASSET_FILE_TYPE;
            displayFromAsset("Demo");
        }
        trail_imgbt = (ImageButton) findViewById(R.id.trail);
        startTrail_imgbt = (ImageButton) findViewById(R.id.startTrail);
        endTrail_imgbt = (ImageButton) findViewById(R.id.endTrail);
        addPoi_imgbt = (ImageButton) findViewById(R.id.addPoi);
        query_poi_imgbt = (ImageButton) findViewById(R.id.query_poi);
        floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam);
        floatingActionsMenu.setClosedOnTouchOutside(true);
        trail_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_lat <= max_lat & m_lat >= min_lat & m_long <= max_long & m_long >= min_long){
                    toolbar.setTitle("准备记录轨迹");
                /*PointF mmm = RenderUtil.getPixLocFromGeoL(new PointF((float) m_lat, (float)m_long));
                pdfView.zoomWithAnimation(mmm.x, mmm.y, 10);*/
                    pdfView.resetZoomWithAnimation();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            final PointF ppz = RenderUtil.getPixLocFromGeoL(new PointF((float)m_lat, (float)m_long), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            ppz.x = ppz.x - 10;
                            //final float verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pdfView.zoomCenteredTo(8, ppz);
                                    pdfView.setPositionOffset(verx);
                                }
                            });
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(task, 1000);
                    startTrail_imgbt.setVisibility(View.VISIBLE);
                    endTrail_imgbt.setVisibility(View.VISIBLE);
                    trail_imgbt.setVisibility(View.INVISIBLE);
                }else Toast.makeText(MainInterface.this, "所在位置不在当前地图中, 不可记录轨迹", Toast.LENGTH_SHORT).show();
            }
        });
        startTrail_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setTitle("正在记录轨迹");
                isDrawType = TRAIL_DRAW_TYPE;
                isDrawTrail = TRAIL_DRAW_TYPE;
                isQuery = false;
                m_cTrail = "";
                isLocateEnd = false;
                isLocate = 0;
                initTrail();
                startTrail_imgbt.setVisibility(View.INVISIBLE);
                //addpoi.setVisibility(View.INVISIBLE);
                //query_poi.setVisibility(View.INVISIBLE);
                //floatingActionsMenu.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();
                Intent start_mService = new Intent(MainInterface.this, RecordTrail.class);
                start_mService.putExtra("ic", ic);
                startService(start_mService);
            }
        });
        endTrail_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setTitle(pdfFileName);
                if (isDrawTrail == TRAIL_DRAW_TYPE){
                    isDrawType = NONE_DRAW_TYPE;
                    isDrawTrail = NONE_DRAW_TYPE;
                    isLocateEnd = true;
                    recordTrail(last_x, last_y);
                    locError(m_cTrail);
                    invalidateOptionsMenu();
                    Intent stop_mService = new Intent(MainInterface.this, RecordTrail.class);
                    stopService(stop_mService);
                    /*Trail trail = new Trail();
                    List<Trail> trails = DataSupport.where("ic = ?", ic).find(Trail.class);
                    trail.setIc(ic);
                    trail.setName("路径" + Integer.toString(trails.size() + 1));
                    trail.setPath(m_cTrail);
                    trail.save();*/
                    List<Trail> trails = DataSupport.findAll(Trail.class);
                    locError("当前存在: " + Integer.toString(trails.size()) + "条轨迹");
                    if (showMode == CENTERMODE) isQuery = true;
                    else isQuery = false;
                }else {
                    Toast.makeText(MainInterface.this, "没有打开位置记录功能", Toast.LENGTH_SHORT).show();
                }
                startTrail_imgbt.setVisibility(View.INVISIBLE);
                endTrail_imgbt.setVisibility(View.INVISIBLE);
                trail_imgbt.setVisibility(View.VISIBLE);
                pdfView.resetZoomWithAnimation();
            }
        });
        addPoi_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poiLayerBt.setChecked(true);
                showPOI = true;
                //pdfView.resetZoomWithAnimation();
                pdfView.zoomWithAnimation(c_zoom);
                if (isDrawType == POI_DRAW_TYPE){
                    isDrawType = NONE_DRAW_TYPE;
                    if (isDrawTrail == TRAIL_DRAW_TYPE){
                        toolbar.setTitle("正在记录轨迹");
                    }else toolbar.setTitle(pdfFileName);
                    if (showMode == CENTERMODE) isQuery = true;
                    else isQuery = false;
                    pdfView.zoomWithAnimation(c_zoom);
                }else {
                    isDrawType = POI_DRAW_TYPE;
                    isQuery = false;
                    if (isDrawTrail == TRAIL_DRAW_TYPE){
                        toolbar.setTitle("正在插放兴趣点(轨迹记录中)");
                    }else toolbar.setTitle("正在插放兴趣点");
                    isMessureType = MESSURE_NONE_TYPE;
                }
            }
        });
        query_poi_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poiLayerBt.setChecked(true);
                showPOI = true;
                pdfView.zoomWithAnimation(c_zoom);
                if (!isQuery){
                    if (isDrawTrail == TRAIL_DRAW_TYPE){
                        toolbar.setTitle("正在查询(轨迹记录中)");
                    }else toolbar.setTitle("正在查询");
                    isQuery = true;
                    isDrawType = NONE_DRAW_TYPE;
                    isMessureType = MESSURE_NONE_TYPE;
                }else {
                    isQuery = false;
                    if (isDrawTrail == TRAIL_DRAW_TYPE){
                        toolbar.setTitle("正在记录轨迹");
                    }else toolbar.setTitle(pdfFileName);
                }
            }
        });
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
        addPhoto_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addPhoto);
        addPhoto_fab.setImageResource(R.drawable.ic_add_a_photo);
        addPhoto_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加照片按钮具体功能如下:
                //pickFile();
                floatingActionsMenu.close(false);
                showPopueWindowForPhoto();
                isMessureType = MESSURE_NONE_TYPE;
            }
        });
        locHere_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.locHere);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //params.addRule(RelativeLayout., whiteBlank_fab.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        locHere_fab.setLayoutParams(params);
        locHere_fab.setImageResource(R.drawable.ic_location_searching);
        locHere_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //定位按钮具体功能如下:
                //Toast.makeText(MainInterface.this, "定位到当前位置功能尚未添加", Toast.LENGTH_SHORT).show();
                locError(Double.toString(m_lat));
                if (m_lat != 0 & m_long != 0){
                    if (m_lat <= max_lat & m_lat >= min_lat & m_long >= min_long & m_long<= max_long){
                if (pdfView.getZoom() != 1){
                    locHere_fab.setImageResource(R.drawable.ic_location_searching);
                    pdfView.resetZoomWithAnimation();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            final PointF ppz = RenderUtil.getPixLocFromGeoL(new PointF((float)m_lat, (float)m_long), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            ppz.x = ppz.x - 10;
                            final float verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    locHere_fab.setImageResource(R.drawable.ic_my_location);
                                    pdfView.zoomCenteredTo(8, ppz);
                                    pdfView.setPositionOffset(verx);
                                    floatingActionsMenu.close(false);
                                }
                            });
                            isPos = true;
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(task, 1000);
                    //PointF pp = RenderUtil.getPixLocFromGeoL(new PointF((float) m_lat, (float)m_long));
                    //pdfView.zoomWithAnimation(pp.x, pp.y, 10);
                }else {
                    locHere_fab.setImageResource(R.drawable.ic_my_location);
                    PointF ppz = RenderUtil.getPixLocFromGeoL(new PointF((float)m_lat, (float)m_long), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    ppz.x = ppz.x - 10;
                    pdfView.zoomCenteredTo(8, ppz);
                    //float verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
                    pdfView.setPositionOffset(verx);
                    isPos = true;
                    floatingActionsMenu.close(false);
                }
                }else Toast.makeText(MyApplication.getContext(), "当前不在该地图中", Toast.LENGTH_LONG).show();
                }else Toast.makeText(MyApplication.getContext(), "无法完成定位操作, 请开启网络或GPS设备", Toast.LENGTH_LONG).show();
            }
        });
        restoreZoom_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.restoreZoom);
        restoreZoom_fab.setImageResource(R.drawable.ic_autorenew);
        restoreZoom_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.close(false);
                //重置按钮具体功能如下:
                if (isCreateBitmap) {
                    isWhiteBlank = true;
                    poiLayerBt.setChecked(true);
                    trailLayerBt.setChecked(true);
                    whiteBlankLayerBt.setChecked(true);
                    showPOI = true;
                    showTrail = true;
                    pdfView.resetZoomWithAnimation();
                    isMessure = false;
                    if (isDrawTrail == TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("正在记录轨迹");
                    } else toolbar.setTitle(pdfFileName);
                    if (!isFullLocation) {
                        isFullLocation = true;
                        if (location != null) {
                            updateView(location);
                        } else
                            Toast.makeText(MainInterface.this, "重置失败", Toast.LENGTH_SHORT).show();
                    }
                }else Toast.makeText(MainInterface.this, "奇怪的错误!", Toast.LENGTH_SHORT).show();
            }
        });
        addTape_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addTape);
        addTape_fab.setImageResource(R.drawable.ic_sound);
        addTape_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮4 具体功能如下:
                try {
                    floatingActionsMenu.close(false);
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, REQUEST_CODE_TAPE);
                }catch (ActivityNotFoundException e){
                    Toast.makeText(MyApplication.getContext(), "无法打开录音功能", Toast.LENGTH_LONG).show();
                }
            }
        });
        messureDistance_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.messureDistance);
        messureDistance_fab.setImageResource(R.drawable.ic_straighten);
        messureDistance_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮5 具体功能如下:
                messure_pts = "";
                floatingActionsMenu.close(false);
                showPopueWindowForMessure();
            }
        });
        getWhiteBlankData();
    }

    private void getWhiteBlankData(){
        List<Lines_WhiteBlank> lines = DataSupport.where("ic = ?", ic).find(Lines_WhiteBlank.class);
        if (lines.size() >= 0){
            for (Lines_WhiteBlank line : lines){
                geometry_WhiteBlank geometry_whiteBlank = new geometry_WhiteBlank(line.getIc(), line.getLines(), line.getColor());
                geometry_whiteBlanks.add(geometry_whiteBlank);
            }
        }
    }

    public void getBitmap(){
        ////////////////////////缓存Bitmap//////////////////////////////
        bts = new ArrayList<bt>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                bts.clear();
                List<POI> pois = DataSupport.where("x <= " + String.valueOf(max_lat) + ";" +  "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
                if (pois.size() > 0){
                    for (POI poi : pois){
                        List<MPHOTO> mphotos = DataSupport.where("poic = ?", poi.getPoic()).find(MPHOTO.class);
                        //PointF pt2 = RenderUtil.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()));
                        //canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                        //locError(Boolean.toString(poi.getPath().isEmpty()));
                        //locError(Integer.toString(poi.getPath().length()));
                        //locError(poi.getPath());
                        if (poi.getPhotonum() != 0 & mphotos.size() != 0){
                            locError("poic = " + poi.getPoic());
                            locError("需要显示的缩略图数量1 : " + Integer.toString(mphotos.size()));
                            String path = mphotos.get(0).getPath();
                            Bitmap bitmap = DataUtil.getImageThumbnail(path, 100, 80);
                            if (mphotos.size() != 0) {
                                int degree = DataUtil.getPicRotate(path);
                                if (degree != 0) {
                                    Matrix m = new Matrix();
                                    m.setRotate(degree); // 旋转angle度
                                    Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                                }
                                bt btt = new bt(bitmap, mphotos.get(0).getPath());
                                bts.add(btt);
                        }
                        }else {
                            POI poi1 = new POI();
                            if (mphotos.size() != 0) poi1.setPhotonum(mphotos.size());
                            else poi1.setToDefault("photonum");
                            poi1.updateAll("poic = ?", poi.getPoic());
                        }
                    }
                }
                locError("需要显示的缩略图数量2 : " + Integer.toString(bts.size()));
                isCreateBitmap = true;
            }
        }).start();
        //////////////////////////////////////////////////////////////////
    }

    private void updateDB(){
        List<POI> pois = DataSupport.where("x <= " + String.valueOf(max_lat) + ";" +  "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
        int size = pois.size();
        for (int i = 0; i < size; i++){
            String poic = pois.get(i).getPoic();
            List<MPHOTO> mphotos = DataSupport.where("poic = ?", poic).find(MPHOTO.class);
            List<MTAPE> mtapes = DataSupport.where("poic = ?", poic).find(MTAPE.class);
            POI poi1 = new POI();
            if (mtapes.size() != 0) poi1.setTapenum(mtapes.size());
            else poi1.setToDefault("tapenum");
            if (mphotos.size() != 0) poi1.setPhotonum(mphotos.size());
            else poi1.setToDefault("photonum");
            poi1.updateAll("poic = ?", pois.get(0).getPoic());
            Log.w(TAG, "updateDB: "  + poic);
        }
        Log.w(TAG, "updateDB: " );
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentProvider = LocationManager.NETWORK_PROVIDER;
        getScreen();
        if (isDrawTrail == TRAIL_DRAW_TYPE){
            toolbar.setTitle("正在记录轨迹");
        }else toolbar.setTitle(pdfFileName);
        if (showMode == CENTERMODE){
            centerPoint.setVisibility(View.VISIBLE);
            isQuery = true;
            isDrawType = NONE_DRAW_TYPE;
            locError("中心点模式");
            isMessureType = MESSURE_NONE_TYPE;
            query_poi_imgbt.setVisibility(View.INVISIBLE);
        }else {
            locError("不是中心点模式");
            centerPoint.setVisibility(View.INVISIBLE);
            isQuery = false;
            query_poi_imgbt.setVisibility(View.VISIBLE);
        }

        ////////////////////////缓存Bitmap//////////////////////////////
        getBitmap();
        /*bts = new ArrayList<bt>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                bts.clear();
                List<POI> pois = DataSupport.where("x <= " + String.valueOf(max_lat) + ";" +  "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
                if (pois.size() > 0){
                    for (POI poi : pois){
                        //PointF pt2 = RenderUtil.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()));
                        //canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                        //locError(Boolean.toString(poi.getPath().isEmpty()));
                        //locError(Integer.toString(poi.getPath().length()));
                        //locError(poi.getPath());
                        if (poi.getPhotonum() == 0){
                            if (poi.getTapenum() == 0){
                                //canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                            } else {
                                //canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint3);
                            }
                        }else {
                            List<MPHOTO> mphotos = DataSupport.where("POIC = ?", poi.getPoic()).find(MPHOTO.class);
                            locError("需要显示的缩略图数量1 : " + Integer.toString(mphotos.size()));
                            bt btt = new bt(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), mphotos.get(0).getPath());
                            bts.add(btt);
                        }
                    }
                }
                locError("需要显示的缩略图数量2 : " + Integer.toString(bts.size()));
                isCreateBitmap = true;
            }
        }).start();*/
        //////////////////////////////////////////////////////////////////
        //注册传感器监听器
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(listener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try{
            locationManager.removeUpdates(locationListener);
        }catch (SecurityException e){
        }
        /*if (isDrawTrail == TRAIL_DRAW_TYPE){
            popBackWindow("Destroy");
        }else super.onDestroy();*/
        super.onDestroy();
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

            if (Build.VERSION.SDK_INT >= 24) {
                //locError(Environment.getExternalStorageDirectory() + "/maphoto/" + Long.toString(timenow) + ".jpg");
                imageUri = FileProvider.getUriForFile(MainInterface.this, "com.android.tuzhi.fileprovider", outputImage);

            } else imageUri = Uri.fromFile(outputImage);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO);
    }

    //PDF页面变化监控
    @Override
    public void onPageChanged(int page, int pageCount) {
        //pageNumber = page;
        //setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String findTitle(String str){
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    //获取当前屏幕的参数
    private void getScreen(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;
        float density = dm.density;
        int screenWidth = (int) (screen_width / density);
        int screenHeight = (int) (screen_height / density);

        //Log.d(TAG, Float.toString(screen_width) + "^" + Float.toString(screen_height) + "^" + Float.toString(screenWidth) + "^" + Float.toString(screenHeight));
    }

    //加载当前菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.queryPOI).setVisible(true);
        menu.findItem(R.id.query).setVisible(false);
        menu.findItem(R.id.info).setVisible(false);
        return true;
    }

    //开始记录轨迹
    private void initTrail(){
        if (isGPSEnabled()){
            locError("开始绘制轨迹");
        }else locError("请打开GPS功能");
    }

    //判断GPS功能是否处于开启状态
    private boolean isGPSEnabled(){
        //textView = (TextView) findViewById(R.id.txt);
        //得到系统的位置服务，判断GPS是否激活
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean ok = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(ok){
            //textView.setText("GPS已经开启");
            //Toast.makeText(this, "GPS已经开启", Toast.LENGTH_LONG).show();
            return true;
        }else{
            Toast.makeText(this, "GPS没有开启, 请打开GPS功能", Toast.LENGTH_SHORT).show();
            //textView.setText("GPS没有开启");
            return false;
        }
    }

    //菜单栏按钮监控
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                this.finish();
                break;
            case R.id.info:
                /*Intent intent = new Intent(MainInterface.this, info_page.class);
                intent.putExtra("extra_data", WKT);
                startActivity(intent);*/
                break;
            case R.id.query:
                linearLayout = (LinearLayout) findViewById(R.id.search);
                linearLayout.setVisibility(View.VISIBLE);
            case R.id.queryPOI:
                Intent intent2 = new Intent(MainInterface.this, pois.class);
                intent2.putExtra("ic", ic);
                intent2.putExtra("min_lat", min_lat);
                intent2.putExtra("max_lat", max_lat);
                intent2.putExtra("min_long", min_long);
                intent2.putExtra("max_long", max_long);
                startActivity(intent2);
                break;
            default:
        }
        return true;
    }
}



