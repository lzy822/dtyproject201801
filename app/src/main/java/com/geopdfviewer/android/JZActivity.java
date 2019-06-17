package com.geopdfviewer.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.clans.fab.FloatingActionButton;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.geopdfviewer.android.DataUtil.renamePath1;

public class JZActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener, OnTapListener {

    //自动切图按钮
    ImageButton autoTrans_imgbt;

    String pdfFileName;

    //记录当前GeoPDF识别码
    private String ic;

    //记录是否开始查询操作
    private boolean isQuery = false;

    //初始化传感器管理器
    private SensorManager sensorManager;
    private float predegree = 0;
    private float degree = 0;

    //记录当前绘图类型
    private TuzhiEnum isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
    //记录是否开始绘制轨迹
    private TuzhiEnum isDrawTrail = TuzhiEnum.NONE_DRAW_TYPE;

    //记录是否渲染完文件
    private boolean isRomance = false;

    private static final String TAG = "JZActivity";
    List<Map> maps;//缓存所有map数据
    Map currentMap;//当前map数据
    TextView textView;
    //获取pdf 的坐标信息
    double min_lat, max_lat, min_long, max_long;
    //记录屏幕高度, 宽度, viewer控件高度, 宽度
    float screen_width, screen_height, viewer_height, viewer_width;
    //记录 高度方向留白系数, 和宽度方向留白系数
    float k_h, k_w;
    //记录当前窗口所在区域的位置
    float cs_top, cs_bottom, cs_left, cs_right;
    float cpix_top, cpix_bottom, cpix_left, cpix_right;
    //记录pdf文档高度, 宽度
    private float current_pagewidth = 0, current_pageheight = 0;
    //记录点选的坐标位置
    private float locLatForTap, locLongForTap;

    //记录当前轨迹
    private String m_cTrail = "";
    //当前记录的坐标点个数
    private int isLocate = 0;

    //上一个记录下来的坐标点坐标
    private float last_x, last_y;
    //是否结束绘制
    private boolean isLocateEnd = true;
    Location location;
    //记录verx
    float verx = 0;
    private LocationManager locationManager;//获取当前坐标位置

    private TuzhiEnum isCoordinateType = TuzhiEnum.COORDINATE_DEFAULT_TYPE;

    PDFView pdfView;
    //声明中心点的位置坐标
    PointF centerPointLoc;
    //记录当前显示模式
    private TuzhiEnum showMode = TuzhiEnum.CENTERMODE;
    double w, h;

    float c_zoom, c_zoom1;
    TuzhiEnum isZoom = TuzhiEnum.ZOOM_NONE;
    boolean isAutoTrans = false;

    public String mUri = "";
    public String mGpts = "";

    //声明Paint
    Paint paint, paint1, paint2, paint3, paint4, paint5, paint6, paint8, paint9, paint10;

    //记录当前图号
    private int num_map;
    List<Trail> trails;

    //坐标信息
    double m_lat = 0, m_long = 0;

    Toolbar toolbar;


    private void initPaint(){
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth((float) 3.0);
        paint.setStyle(Paint.Style.FILL);

        paint1 = new Paint();
        paint1.setColor(Color.GREEN);
        paint1.setStrokeWidth((float) 2.0);
        paint1.setStyle(Paint.Style.FILL);

        paint2 = new Paint();
        paint2.setColor(Color.BLACK);
        paint2.setStrokeWidth((float) 2.0);
        paint2.setStyle(Paint.Style.FILL);

        paint3 = new Paint();
        paint3.setColor(Color.BLUE);
        paint3.setStrokeWidth((float) 2.0);
        paint3.setStyle(Paint.Style.FILL);

        paint4 = new Paint();
        paint4.setColor(Color.YELLOW);
        paint4.setStrokeWidth((float) 2.0);
        paint4.setStyle(Paint.Style.FILL);

        paint5 = new Paint();
        paint5.setColor(Color.rgb(123, 175, 212));
        paint5.setStrokeWidth((float) 2.0);
        paint5.setStyle(Paint.Style.FILL);

        paint6 = new Paint();
        paint6.setStrokeWidth(10);
        paint6.setStyle(Paint.Style.STROKE);
        paint6.setColor(Color.YELLOW);
        paint6.setAlpha(127);

        paint8 = new Paint();
        paint8.setStrokeWidth(10);
        paint8.setStyle(Paint.Style.STROKE);
        paint8.setColor(Color.BLUE);

        paint9 = new Paint();
        paint9.setStrokeWidth(10);
        paint9.setStyle(Paint.Style.STROKE);
        paint9.setColor(Color.RED);

        paint10 = new Paint();
        paint10.setStrokeWidth(10);
        paint10.setStyle(Paint.Style.STROKE);
        paint10.setColor(Color.GREEN);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jz);

        initPaint();

        doSomethingElse();

        initWidget();

        cacheMaps();
        showMap();
    }

    private void doSomethingElse() {
        //LitePal.deleteAll(Map.class);
    }

    //屏幕坐标位置到经纬度转化
    private PointF getGeoLocFromPixL(final PointF pt) {
        //textView = (TextView) findViewById(R.id.txt);
        //精确定位算法
        PointF mpt;
        float xxxx, yyyy;
        if (current_pageheight < viewer_height || current_pagewidth < viewer_width) {
            xxxx = ((pt.x - (screen_width - viewer_width + k_w)));
            yyyy = ((pt.y - (screen_height - viewer_height + k_h)));
            if (pt.y >= (screen_height - viewer_height + k_h) && pt.y <= (screen_height - viewer_height + k_h + current_pageheight) && pt.x >= (screen_width - viewer_width + k_w) && pt.x <= (screen_width - viewer_width + k_w + current_pagewidth)) {
                mpt = new PointF();
                mpt.x = (float) (max_lat - (yyyy) / current_pageheight * (max_lat - min_lat));
                mpt.y = (float) ((xxxx) / current_pagewidth * (max_long - min_long) + min_long);
                locLatForTap = mpt.x;
                locLongForTap = mpt.y;
            } else {
                mpt = new PointF(0, 0);
            }
        } else {
            xxxx = pt.x - (screen_width - viewer_width);
            yyyy = pt.y - (screen_height - viewer_height);
            Log.w(TAG, "getGeoLocFromPixL: " + screen_height + ";" + viewer_height);
            mpt = new PointF();
            mpt.x = (float) (max_lat - (yyyy - pdfView.getCurrentYOffset()) / current_pageheight * (max_lat - min_lat));
            mpt.y = (float) ((xxxx - pdfView.getCurrentXOffset()) / current_pagewidth * (max_long - min_long) + min_long);
            locLatForTap = mpt.x;
            locLongForTap = mpt.y;
        }
        return mpt;
        //
    }

    private boolean showLocationText(PointF pt) {
        if (pt.x != 0) {
            DecimalFormat df = new DecimalFormat("0.0000");
            if (isCoordinateType == TuzhiEnum.COORDINATE_DEFAULT_TYPE) {
                textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
            } else if (isCoordinateType == TuzhiEnum.COORDINATE_BLH_TYPE) {
                textView.setText(Integer.toString((int) locLatForTap) + "°" + Integer.toString((int) ((locLatForTap - (int) locLatForTap) * 60)) + "′" + Integer.toString((int) (((locLatForTap - (int) locLatForTap) * 60 - (int) ((locLatForTap - (int) locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int) locLongForTap) + "°" + Integer.toString((int) ((locLongForTap - (int) locLongForTap) * 60)) + "′" + Integer.toString((int) (((locLongForTap - (int) locLongForTap) * 60 - (int) ((locLongForTap - (int) locLongForTap) * 60)) * 60)) + "″");
            } else {
                textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
            }
            return true;
        } else {
            textView.setText("点击位置在区域之外");
            return false;
        }
    }

    private void showLocationText() {
        DecimalFormat df = new DecimalFormat("0.0000");
        if (isCoordinateType == TuzhiEnum.COORDINATE_DEFAULT_TYPE) {
            textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
        } else if (isCoordinateType == TuzhiEnum.COORDINATE_BLH_TYPE) {
            textView.setText(Integer.toString((int) locLatForTap) + "°" + Integer.toString((int) ((locLatForTap - (int) locLatForTap) * 60)) + "′" + Integer.toString((int) (((locLatForTap - (int) locLatForTap) * 60 - (int) ((locLatForTap - (int) locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int) locLongForTap) + "°" + Integer.toString((int) ((locLongForTap - (int) locLongForTap) * 60)) + "′" + Integer.toString((int) (((locLongForTap - (int) locLongForTap) * 60 - (int) ((locLongForTap - (int) locLongForTap) * 60)) * 60)) + "″");
        } else {
            textView.setText(df.format(locLatForTap) + ";" + df.format(locLongForTap));
        }
    }

    private void transNextMap(){
        //toolbar.setTitle("下一幅图");
    }

    private void transPriorMap(){
        //toolbar.setTitle("上一幅图");
    }
    //判断GPS功能是否处于开启状态
    private boolean isGPSEnabled() {
        //textView = (TextView) findViewById(R.id.txt);
        //得到系统的位置服务，判断GPS是否激活
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean ok = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {
            //textView.setText("GPS已经开启");
            //Toast.makeText(this, "GPS已经开启", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, R.string.LocError, Toast.LENGTH_SHORT).show();
            //textView.setText("GPS没有开启");
            return false;
        }
    }

    private void drawMLocPoint(Canvas canvas){
        if (isGPSEnabled()) {
            PointF pt = new PointF((float) m_lat, (float) m_long);
            pt = LatLng.getPixLocFromGeoL(pt, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            canvas.drawCircle(pt.x, pt.y, 23, paint);
            canvas.drawCircle(pt.x, pt.y, 20, paint5);
            canvas.drawCircle(pt.x, pt.y, 10, paint3);
                            /*if (predegree >= 0 && predegree < 90){
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
            int version = Build.VERSION.SDK_INT;
            if (version >= 21) {
                canvas.drawArc(pt.x - 35, pt.y - 35, pt.x + 35, pt.y + 35, degree - 105, 30, true, paint3);
            }
        } else locError("请在手机设置中打开GPS功能, 否则该页面很多功能将无法正常使用");
    }


    public void initWidget() {
        try {
            //获取传感器管理器系统服务
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            toolbar = (Toolbar) findViewById(R.id.toolBar1_jz);
            setSupportActionBar(toolbar);

            ImageButton trail_imgbt = (ImageButton) findViewById(R.id.trail1);
            ImageButton startTrail_imgbt = (ImageButton) findViewById(R.id.startTrail1);
            ImageButton endTrail_imgbt = (ImageButton) findViewById(R.id.endTrail1);
            trail_imgbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_lat <= max_lat && m_lat >= min_lat && m_long <= max_long && m_long >= min_long) {
                        toolbar.setTitle("准备记录轨迹");
                /*PointF mmm = LatLng.getPixLocFromGeoL(new PointF((float) m_lat, (float)m_long));
                pdfView.zoomWithAnimation(mmm.x, mmm.y, 10);*/
                        pdfView.resetZoomWithAnimation();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                final PointF ppz = LatLng.getPixLocFromGeoL(new PointF((float) m_lat, (float) m_long), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
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
                    } else
                        Toast.makeText(JZActivity.this, R.string.TrailError, Toast.LENGTH_SHORT).show();
                }
            });
            startTrail_imgbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toolbar.setTitle("正在记录轨迹");
                    isDrawType = TuzhiEnum.TRAIL_DRAW_TYPE;
                    isDrawTrail = TuzhiEnum.TRAIL_DRAW_TYPE;
                    //isQuery = false;
                    m_cTrail = "";
                    isLocateEnd = false;
                    isLocate = 0;
                    initTrail();
                    startTrail_imgbt.setVisibility(View.INVISIBLE);
                    //addpoi.setVisibility(View.INVISIBLE);
                    //query_poi.setVisibility(View.INVISIBLE);
                    //floatingActionsMenu.setVisibility(View.INVISIBLE);
                    invalidateOptionsMenu();
                    Intent start_mService = new Intent(JZActivity.this, RecordTrail.class);
                    start_mService.putExtra("ic", ic);
                    startService(start_mService);
                }
            });
            endTrail_imgbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toolbar.setTitle(pdfFileName);
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                        isDrawTrail = TuzhiEnum.NONE_DRAW_TYPE;
                        isLocateEnd = true;
                        recordTrail(last_x, last_y);
                        locError(m_cTrail);
                        invalidateOptionsMenu();
                        Intent stop_mService = new Intent(JZActivity.this, RecordTrail.class);
                        stopService(stop_mService);
                    /*Trail trail = new Trail();
                    List<Trail> trails = LitePal.where("ic = ?", ic).find(Trail.class);
                    trail.setIc(ic);
                    trail.setName("路径" + Integer.toString(trails.size() + 1));
                    trail.setPath(m_cTrail);
                    trail.save();*/
                        trails = LitePal.findAll(Trail.class);
                        locError("当前存在: " + Integer.toString(trails.size()) + "条轨迹");
                        if (showMode == TuzhiEnum.CENTERMODE) isQuery = true;
                        else isQuery = false;
                    } else {
                        Toast.makeText(JZActivity.this, R.string.OpenTrailError, Toast.LENGTH_SHORT).show();
                    }
                    startTrail_imgbt.setVisibility(View.INVISIBLE);
                    endTrail_imgbt.setVisibility(View.INVISIBLE);
                    trail_imgbt.setVisibility(View.VISIBLE);
                    pdfView.resetZoomWithAnimation();
                }
            });
            ImageButton trans_next = (ImageButton) findViewById(R.id.trans_next);
            trans_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transNextMap();
                }
            });
            ImageButton trans_prior = (ImageButton) findViewById(R.id.trans_prior);
            trans_prior.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transPriorMap();
                }
            });
            autoTrans_imgbt = (ImageButton) findViewById(R.id.trans1);
            autoTrans_imgbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAutoTrans) {
                        TransMapCommand.getInstance(JZActivity.this).off();
                        isAutoTrans = false;
                        autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                    } else {
                        TransMapCommand.getInstance(JZActivity.this).on();
                        isAutoTrans = true;
                        autoTrans_imgbt.setBackgroundResource(R.drawable.ic_check_black_24dp);
                    }
                }
            });
            textView = (TextView) findViewById(R.id.txt1);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DecimalFormat df = new DecimalFormat("0.0000");
                    String str = textView.getText().toString();
                    Log.w(TAG, "onClick: " + str);
                    if (!str.equals("点击位置在区域之外") && !str.equals("在这里显示坐标值")) {
                        if (isCoordinateType == TuzhiEnum.COORDINATE_DEFAULT_TYPE) {
                            //String[] strs = str.split(";");
                            //PointF pt = new PointF(Float.valueOf(strs[0]), Float.valueOf(strs[1]));
                            textView.setText(Integer.toString((int) locLatForTap) + "°" + Integer.toString((int) ((locLatForTap - (int) locLatForTap) * 60)) + "′" + Integer.toString((int) (((locLatForTap - (int) locLatForTap) * 60 - (int) ((locLatForTap - (int) locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int) locLongForTap) + "°" + Integer.toString((int) ((locLongForTap - (int) locLongForTap) * 60)) + "′" + Integer.toString((int) (((locLongForTap - (int) locLongForTap) * 60 - (int) ((locLongForTap - (int) locLongForTap) * 60)) * 60)) + "″");
                            isCoordinateType = TuzhiEnum.COORDINATE_BLH_TYPE;
                            //locError(Integer.toString(textView.getHeight()));
                        } else if (isCoordinateType == TuzhiEnum.COORDINATE_BLH_TYPE) {
                            //String[] strs = str.split(";");
                            //locError(strs[0] + "还有: " + strs[1]);
                            //PointF pt = new PointF(Float.valueOf(strs[0].substring(0, strs[0].indexOf("°"))) + (Float.valueOf(strs[0].substring(strs[0].indexOf("°") + 1, strs[0].indexOf("′"))) / 60) + (Float.valueOf(strs[0].substring(strs[0].indexOf("′") + 1, strs[0].indexOf("″"))) / 3600), Float.valueOf(strs[1].substring(0, strs[1].indexOf("°"))) + (Float.valueOf(strs[1].substring(strs[1].indexOf("°") + 1, strs[1].indexOf("′"))) / 60) + (Float.valueOf(strs[1].substring(strs[1].indexOf("′") + 1, strs[1].indexOf("″"))) / 3600));
                            textView.setText(df.format(locLatForTap) + "; " + df.format(locLongForTap));
                            isCoordinateType = TuzhiEnum.COORDINATE_DEFAULT_TYPE;
                            //locError(Integer.toString(textView.getHeight()));
                        }
                    }
                }
            });
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    manager.setText(textView.getText());
                    Toast.makeText(JZActivity.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
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
                    try {
                        Intent intent = new Intent(JZActivity.this, Activity_FileManage.class);
                        intent.putExtra("type", ".dt");
                        startActivityForResult(intent, 17);
                    }catch (Exception e){
                        Log.w(TAG, e.toString());
                    }
                }
            });

            setStatusBarColor(this);
        }
        catch (Exception e) {
            Log.w(TAG, "initWidget: " + e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode){
                case 17:
                    final String path = data.getStringExtra("filePath");
                    addMap(path);
                    Log.w(TAG, path);
                    break;
            }
        }catch (Exception e){
            Log.w(TAG, e.toString());
        }
    }

    private void addMap(final String path){
        Log.w(TAG, "llll: " + path);
        boolean isOKForAddMap1 = false;
        if (path.contains(".dt")) {
            try {
                String configPath;
                configPath = URLDecoder.decode(path, "utf-8");
                isOKForAddMap1 = isOKForAddMap(DataUtil.findNameFromUri(Uri.parse(configPath)));
            } catch (UnsupportedEncodingException e) {

            }
        } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path)).contains(".dt")) {
            try {
                String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path));
                configPath = URLDecoder.decode(configPath, "utf-8");
                isOKForAddMap1 = isOKForAddMap(DataUtil.findNameFromUri(Uri.parse(configPath)));
            } catch (Exception e) {
                Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(this, this.getResources().getText(R.string.OpenFileError), Toast.LENGTH_SHORT).show();
        if (isOKForAddMap1) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(JZActivity.this);
            dialog.setTitle("请选择解析类型");
            dialog.setMessage("如果距离值显示有误， 请删除地图后， 选择另一类型再进行添加。");
            dialog.setCancelable(false);
            dialog.setPositiveButton("地理框架一", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (path.contains(".dt")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                            Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfo).toString() + JZActivity.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    String configPath;
                                    configPath = URLDecoder.decode(path, "utf-8");


                                    manageGeoInfo(configPath, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true);
                                    //locError(configPath);
                                    //locError(path);
                                    //locError(findNameFromUri(uri));
                                    //LitePal.getDatabase();=
                                } catch (Exception e) {
                                    Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).start();
                    } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path)).contains(".dt")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //add_max++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                        Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfo).toString() + JZActivity.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                try {
                                    String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path));
                                    configPath = URLDecoder.decode(configPath, "utf-8");
                                    /*SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                    editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                    editor.apply();*/
                                    manageGeoInfo(configPath, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), true);
                                } catch (Exception e) {
                                    Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                }
                                //locError(path);
                                //locError(findNameFromUri(uri));
                                //LitePal.getDatabase();=


                            }
                        }).start();

                    } else Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError), Toast.LENGTH_SHORT).show();

                }
            });
            dialog.setNegativeButton("地理框架二", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (path.contains(".dt")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //add_max++;
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                            Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfo).toString() + JZActivity.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    String configPath;
                                    configPath = URLDecoder.decode(path, "utf-8");


                                    manageGeoInfo(configPath, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false);
                                    //locError(configPath);
                                    //locError(path);
                                    //locError(findNameFromUri(uri));
                                    //LitePal.getDatabase();=
                                } catch (Exception e) {
                                    Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).start();
                    } else if (DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path)).contains(".dt")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //add_max++;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");
                                        Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfo).toString() + JZActivity.this.getResources().getText(R.string.QSH).toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                try {
                                    String configPath = DataUtil.getRealPathFromUriForPhoto(MyApplication.getContext(), Uri.parse(path));
                                    configPath = URLDecoder.decode(configPath, "utf-8");
                                    /*SharedPreferences.Editor editor = getSharedPreferences("filepath", MODE_PRIVATE).edit();
                                    editor.putString("mapath", configPath.substring(0, configPath.lastIndexOf("/")));
                                    editor.apply();*/
                                    manageGeoInfo(configPath, configPath, DataUtil.findNameFromUri(Uri.parse(configPath)), false);
                                } catch (Exception e) {
                                    Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError) + "_2", Toast.LENGTH_SHORT).show();
                                }
                                //locError(path);
                                //locError(findNameFromUri(uri));
                                //LitePal.getDatabase();



                            }
                        }).start();

                    } else Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.OpenFileError) + "_1", Toast.LENGTH_SHORT).show();

                }
            });
            dialog.show();
        } else Toast.makeText(this, JZActivity.this.getResources().getText(R.string.AddedMapTip), Toast.LENGTH_SHORT).show();
    }

    private boolean isOKForAddMap(String name){
        /*SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
        int size = pref1.getInt("num", num_pdf);
        SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
        boolean isSameName = false;
        for (int i = 1; i <= size; ++i){
            String str = "n_" + Integer.toString(i) + "_";
            if (pref2.getString(str + "name", "").equals(name)) {
                isSameName = true;
                break;
            }
        }
        if (isSameName) return false;
        else */
        return true;
    }

    public void saveGeoInfo(String name, String uri, String WKT, String BBox, String GPTS, String img_path, String MediaBox, String CropBox, String ic){
        double[] gpts = DataUtil.getGPTS(GPTS);
        Point centerPt = new Point((gpts[2] + gpts[3]) / 2, (gpts[0] + gpts[1]) / 2);
        String position = gpts[2] + "," + gpts[1] + ";" + gpts[3] + "," + gpts[0] + ";" + centerPt.getX() + "," + centerPt.getY();



        Map map = null;

        List<Map> maps = LitePal.where("maptype = ?", Integer.toString(EnumClass.FIFTHMAP)).find(Map.class);
        if (maps.size() > 0)
            map = new Map(name, WKT, uri, GPTS, BBox, MediaBox, CropBox, img_path, ic, position, EnumClass.LEAFMAP);
        else
            map = new Map(name, WKT, uri, GPTS, BBox, MediaBox, CropBox, img_path, ic, position, EnumClass.FIFTHMAP);
        map.save();
        maps.add(map);
    }

    private void manageGeoInfo(String filePath, String uri, String name, boolean type){
        if (!filePath.isEmpty()) {
            String[] strings = getGeoInfo(filePath, uri, name, type);
            if (strings != null)
                saveGeoInfo(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
        }else {
            try {
                String[] strings = new String[]{"图志简介", "", "", "", "", getAssets().open("image/图志简介1.jpg").toString(), "", "", "图志简介"};
                saveGeoInfo(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
            }catch (Exception e){

            }
        }
    }

    public String[] getGeoInfo(String filePath, String uri, String name, boolean type) {
        //locError(name);
        String m_name = name;
        String m_uri = uri;
        String bmPath = "";
        File file = new File(filePath);
        InputStream in = null;
        String m_WKT = "";
        Boolean isESRI = false;
        int num_line = 0;
        String m_BBox = "", m_GPTS = "", m_MediaBox = "", m_CropBox = "", m_LPTS = "";
        try {
            /*if (Type == SAMPLE_TYPE){
                in = getAssets().open(EnumClass.SAMPLE_FILE);
            }
            else */{
                in = new FileInputStream(file);
                //TODO 内存泄漏检测
                bmPath = DataUtil.getDtThumbnail(name, "/TuZhi" + "/Thumbnails",  filePath, 120, 180, 30,  JZActivity.this);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            int m_num_GPTS = 0;
            //line = bufferedReader.readLine()
            while((line = bufferedReader.readLine()) != null) {
                //sb.append(line + "/n");
                if(line.contains("PROJCS") || line.contains("GEOGCS")) {
                    //locError(line);
                    if (line.contains("PROJCS"))
                        m_WKT = line.substring(line.indexOf("PROJCS["), line.indexOf(")>>"));
                    else if (line.contains("GEOGCS"))
                        m_WKT = line.substring(line.indexOf("GEOGCS["), line.indexOf(")>>"));
                    //locError("wkt信息： " + m_WKT);
                    //GEOGCS["GCS_China_Geodetic_Coordinate_System_2000",DATUM["D_China_2000",SPHEROID["CGCS2000",6378137.0,298.257222101]],PRIMEM["Greenwich",0.0],UNIT["Degree",0.0174532925199433]]
                }
                if (line.contains("ESRI") || line.contains("esri") || line.contains("arcgis") || line.contains("ARCGIS") || line.contains("Adobe"))
                {
                    isESRI = true;
                }
                if (line.contains("/BBox") & line.contains("Viewport")){
                    //Log.w(TAG, "the line loc = " + Integer.toString(num_line) );
                    m_BBox = line.substring(line.indexOf("BBox") + 5);
                    if(!m_BBox.contains("BBox")){
                        m_BBox = m_BBox.substring(0, m_BBox.indexOf("]"));
                        m_BBox = m_BBox.trim();
                    }else {
                        m_BBox = m_BBox.substring(m_BBox.indexOf("BBox") + 5);
                        m_BBox = m_BBox.substring(0, m_BBox.indexOf("]"));
                        m_BBox = m_BBox.trim();
                    }
                    //locError("BBox : " + m_BBox);
                }
                /*if (line.contains("GPTS") & line.contains("LPTS") & m_num_GPTS == 0){
                    m_num_GPTS++;
                    m_LPTS = line.substring(line.indexOf("LPTS") + 5);
                    m_LPTS = m_LPTS.substring(0, m_LPTS.indexOf("]"));
                    m_LPTS = m_LPTS.trim();
                    if (m_LPTS.length() > 0){
                        m_GPTS = line.substring(line.indexOf("GPTS") + 5);
                        m_GPTS = m_GPTS.substring(0, m_GPTS.indexOf("]"));
                        m_GPTS = m_GPTS.trim();
                        //坐标飘移纠偏
                        m_GPTS = getGPTS(m_GPTS, m_LPTS);
                    }
                    //locError(m_GPTS);
                    Log.w(TAG, "hold on" + m_GPTS );
                    //Log.w(TAG, "hold on : " + line );

                }*/
                if (line.contains("GPTS") & line.contains("LPTS")){
                    m_num_GPTS++;
                    m_LPTS = line.substring(line.indexOf("LPTS") + 5);
                    m_LPTS = m_LPTS.substring(0, m_LPTS.indexOf("]"));
                    m_LPTS = m_LPTS.trim();
                    if (m_LPTS.length() > 0){
                        if (m_num_GPTS > 1){
                            String m_GPTS1 = "";
                            m_GPTS1 = line.substring(line.indexOf("GPTS") + 5);
                            m_GPTS1 = m_GPTS1.substring(0, m_GPTS1.indexOf("]"));
                            m_GPTS1 = m_GPTS1.trim();
                            String[] m_gptstr = m_GPTS.split(" ");
                            String[] m_gptstr1 = m_GPTS1.split(" ");
                            //locError("1 = " + m_gptstr1[0] + " 2 = " + m_gptstr[0]);
                            if (type) {
                                if (Float.valueOf(m_gptstr1[0]) > Float.valueOf(m_gptstr[0])) {
                                    m_GPTS = m_GPTS1;//BUG
                                    //坐标飘移纠偏
                                    m_GPTS = DataUtil.getGPTS(m_GPTS, m_LPTS);
                                }
                            }else {
                                if (Float.valueOf(m_gptstr1[0]) < Float.valueOf(m_gptstr[0])) {
                                    m_GPTS = m_GPTS1;//BUG
                                    //坐标飘移纠偏
                                    m_GPTS = DataUtil.getGPTS(m_GPTS, m_LPTS);
                                }
                            }

                        }else {
                            m_GPTS = line.substring(line.indexOf("GPTS") + 5);
                            m_GPTS = m_GPTS.substring(0, m_GPTS.indexOf("]"));
                            m_GPTS = m_GPTS.trim();
                            //坐标飘移纠偏
                            m_GPTS = DataUtil.getGPTS(m_GPTS, m_LPTS);
                        }
                    }
                    //locError(m_GPTS);
                    Log.w(TAG, "hold on" + m_GPTS );
                    Log.w(TAG, "hold on : " + line );

                }
                if (line.contains("MediaBox")){
                    line = line.substring(line.indexOf("MediaBox"));
                    m_MediaBox = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    m_MediaBox = m_MediaBox.trim();
                    Log.w(TAG, "MediaBox : " + m_MediaBox );
                }
                if (line.contains("CropBox")){
                    m_CropBox = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    m_CropBox = m_CropBox.trim();
                    //Log.w(TAG, m_CropBox );
                }
                num_line += 1;
            }
            if (m_CropBox.isEmpty()){
                m_CropBox = m_MediaBox;
            }
            //locError(Integer.toString(m_num_GPTS));
            //Log.w(TAG, "GPTS:" + m_GPTS );
            //Log.w(TAG, "BBox:" + m_BBox );
            if (isESRI == true) {
                m_WKT = "ESRI::" + m_WKT;
                //save(content);
            } else {
                //save(content);
            }
            Log.w(TAG, m_WKT );
            //locError();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*add_current ++;
                    toolbar.setTitle("正在提取地理信息(" + Integer.toString(add_current) + "/" + Integer.toString(add_max) + ")");*/
                    Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfoOk), Toast.LENGTH_LONG).show();
                }
            });
            in.close();
            //locError("看这里"+m_name);
            //locError("WKT: " + m_WKT);

        } catch (IOException e) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfoError).toString() + R.string.QLXWM, Toast.LENGTH_LONG).show();
                }
            });

            //Toast.makeText(this, "地理信息获取失败, 请联系程序员", Toast.LENGTH_LONG).show();
        }
        if (filePath != "") {
            //locError(m_MediaBox + "&" + m_BBox + "&" + m_GPTS);
            if (!m_WKT.isEmpty()) {
                m_GPTS = DataUtil.rubberCoordinate(m_MediaBox, m_BBox, m_GPTS);
                //saveGeoInfo(m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name);
                String[] strings = new String[]{m_name, filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name};
                return strings;
            }else {

                renamePath1(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyApplication.getContext(), JZActivity.this.getResources().getText(R.string.GetGeoInfoError).toString() + R.string.QLXWM, Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
        } else {
            //m_GPTS = DataUtil.rubberCoordinate(m_MediaBox, m_BBox, m_GPTS);
            //saveGeoInfo("Demo", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name);
            String[] strings = new String[]{"图志简介", filePath, m_WKT, m_BBox, m_GPTS, bmPath, m_MediaBox, m_CropBox, m_name};
            return strings;
        }
    }

    public void cacheMaps(){
        maps = LitePal.findAll(Map.class);
        for (int i = 0; i < maps.size(); ++i){
            /*Map map = maps.get(i);
            MapItem mapItem = new MapItem(map.getPosition(), map.getName(), map.getUri(), map.getImguri(), map.getMaptype());*/
            if (maps.get(i).getMaptype() == EnumClass.FIFTHMAP)
            {
                currentMap = maps.get(i);
                num_map = i;
                initMapVariable();
                break;
            }
        }
    }

    public void showMap(){
        try {
            displayFromFile(mUri);
            locError("good");
        }catch (NullPointerException e){
            displayFromFile("/storage/emulated/0/tencent/TIMfile_recv/cangyuan.dt");
            locError("error");
        }
    }

    public void initMapVariable(){
        pdfFileName = currentMap.getName();
        ic = currentMap.getIc();
        mUri = currentMap.getUri();
        mGpts = currentMap.getGpts();
        manageInfo(mGpts);
    }

    @TargetApi(21)
    public static void setStatusBarColor(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.BLACK);
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        getCurrentScreenFrameInfo(pageWidth, pageHeight);
        getCurrentScreenLoc();
        getZoom();
        float[] k = RenderUtil.getK(pageWidth, pageHeight, viewer_width, viewer_height);
        k_w = k[0];
        k_h = k[1];
        if (isAutoTrans && (isZoom == TuzhiEnum.ZOOM_IN || c_zoom == 10)) {
            int size = maps.size();
            if (size > 1) {
                float thedelta = 0;
                int thenum = 0;
                for (int j = 0; j < size; ++j) {
                    String Muri = maps.get(j).getUri();
                    String MGPTS = maps.get(j).getGpts();
                    if (MGPTS != null && !MGPTS.isEmpty()) {
                        String[] GPTString = MGPTS.split(" ");
                        float[] GPTSs = new float[GPTString.length];
                        for (int i = 0; i < GPTString.length; ++i) {
                            GPTSs[i] = Float.valueOf(GPTString[i]);
                        }
                        float lat_axis, long_axis;
                        PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
                        lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
                        long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
                        for (int i = 0; i < GPTSs.length; i = i + 2) {
                            if (GPTSs[i] < lat_axis) {
                                if (GPTSs[i + 1] < long_axis) {
                                    pt_lb.x = GPTSs[i];
                                    pt_lb.y = GPTSs[i + 1];
                                } else {
                                    pt_rb.x = GPTSs[i];
                                    pt_rb.y = GPTSs[i + 1];
                                }
                            } else {
                                if (GPTSs[i + 1] < long_axis) {
                                    pt_lt.x = GPTSs[i];
                                    pt_lt.y = GPTSs[i + 1];
                                } else {
                                    pt_rt.x = GPTSs[i];
                                    pt_rt.y = GPTSs[i + 1];
                                }
                            }
                        }
                        float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                        float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                        float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                        float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                        if (verifyAreaForAutoTrans(mmax_lat, mmin_lat, mmax_long, mmin_long)) {
                            float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                            if (j != num_map) {
                                if (thedelta == 0) {
                                    thedelta = thedelta1;
                                    thenum = j;
                                }
                                if (thedelta1 < thedelta) {
                                    thedelta = thedelta1;
                                    thenum = j;
                                }
                            }
                        }
                    }
                }
                double deltaK_trans;
                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, JZActivity.this, TuzhiEnum.ZOOM_IN);
                if (thenum != num_map && thenum != 0 && thedelta < deltaK_trans) {
                    //updateMapInfo(thenum);

                    processTransMapCommand(thenum);
                    //TransMapCommand.getInstance(this, thenum).process();

                    /*geometry_whiteBlanks.clear();
                    num_whiteBlankPt = 0;
                    isWhiteBlank = false;
                    whiteBlankPt = "";
                    num_map1 = num_map;
                    getInfo(thenum);
                    toolbar.setTitle(pdfFileName);
                    getNormalBitmap();
                    manageInfo();
                    pdfView.recycle();
                    displayFromFile(uri);
                    isAutoTrans = false;
                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                    getWhiteBlankData();*/
                }
            } else
                Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.AutoTransError), Toast.LENGTH_SHORT).show();
        } else if (c_zoom <= 1.5 && isAutoTrans && isZoom == TuzhiEnum.ZOOM_OUT) {
            int size = maps.size();
            if (size > 1) {
                float thedelta = 0;
                int thenum = 0;
                for (int j = 1; j <= size; ++j) {
                    String Muri = maps.get(j).getUri();
                    String MGPTS = maps.get(j).getGpts();
                    if (MGPTS != null && !MGPTS.isEmpty()) {
                        Log.w(TAG, "GPTS: " + MGPTS);
                        String[] GPTString = MGPTS.split(" ");
                        float[] GPTSs = new float[GPTString.length];
                        for (int i = 0; i < GPTString.length; ++i) {
                            if (MGPTS != null && !MGPTS.isEmpty())
                                GPTSs[i] = Float.valueOf(GPTString[i]);
                        }
                        float lat_axis, long_axis;
                        PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
                        lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
                        long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
                        for (int i = 0; i < GPTSs.length; i = i + 2) {
                            if (GPTSs[i] < lat_axis) {
                                if (GPTSs[i + 1] < long_axis) {
                                    pt_lb.x = GPTSs[i];
                                    pt_lb.y = GPTSs[i + 1];
                                } else {
                                    pt_rb.x = GPTSs[i];
                                    pt_rb.y = GPTSs[i + 1];
                                }
                            } else {
                                if (GPTSs[i + 1] < long_axis) {
                                    pt_lt.x = GPTSs[i];
                                    pt_lt.y = GPTSs[i + 1];
                                } else {
                                    pt_rt.x = GPTSs[i];
                                    pt_rt.y = GPTSs[i + 1];
                                }
                            }
                        }
                        float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                        float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                        float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                        float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                        if (mmax_lat > max_lat && mmin_lat < min_lat && mmax_long > max_long && mmin_long < min_long) {
                            float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                            if (thedelta == 0) {
                                thedelta = thedelta1;
                                thenum = j;
                            } else if (thedelta1 < thedelta) {
                                thedelta = thedelta1;
                                thenum = j;
                            }
                        }
                    }
                }
                double deltaK_trans;
                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, JZActivity.this, TuzhiEnum.ZOOM_OUT);
                if (thenum != num_map && thenum != 0 && thedelta < deltaK_trans) {
                    processTransMapCommand(thenum);
                    //updateMapInfo(thenum);
                    //TransMapCommand.getInstance(this, thenum).process();
                    /*geometry_whiteBlanks.clear();
                    num_whiteBlankPt = 0;
                    isWhiteBlank = false;
                    whiteBlankPt = "";
                    num_map1 = num_map;
                    getInfo(thenum);
                    manageInfo();
                    toolbar.setTitle(pdfFileName);
                    getNormalBitmap();
                    pdfView.recycle();
                    displayFromFile(uri);
                    isAutoTrans = false;
                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                    getWhiteBlankData();*/
                }
            } else
                Toast.makeText(JZActivity.this, JZActivity.this.getResources().getText(R.string.AutoTransError), Toast.LENGTH_SHORT).show();
        }
        drawMLocPoint(canvas);
    }

    private void processTransMapCommand(int thenum){
        TransMapCommand transMapCommand = TransMapCommand.getInstance(this);
        transMapCommand.setMap_num(thenum);
        transMapCommand.process();
    }

    public void updateMapInfo(int i){
        Map map = maps.get(i);
        ic = map.getIc();
        mUri = map.getUri();
        mGpts = map.getGpts();
        currentMap = map;
        num_map = i;
        manageInfo(mGpts);
    }

    private boolean verifyAreaForAutoTrans(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long) {
        double deltaLatK, deltaLongK;
        deltaLatK = (max_lat - min_lat) * 0.014;
        deltaLongK = (max_long - min_long) * 0.014;
        if ((mmin_lat - deltaLatK) < cs_bottom && (mmax_long + deltaLongK) > cs_right && (mmin_long - deltaLongK) < cs_left)
            return true;
        else if ((mmax_lat + deltaLatK) > cs_top && (mmax_long + deltaLongK) > cs_right && (mmin_long - deltaLongK) < cs_left)
            return true;
        else if ((mmax_lat + deltaLatK) > cs_top && (mmin_lat - deltaLatK) < cs_bottom && (mmin_long - deltaLongK) < cs_left)
            return true;
        else if ((mmax_lat + deltaLatK) > cs_top && (mmin_lat - deltaLatK) < cs_bottom && (mmax_long + deltaLongK) > cs_right)
            return true;
        else if ((mmax_lat + deltaLatK) > cs_top && (mmin_lat - deltaLatK) < cs_bottom && (mmax_long + deltaLongK) > cs_right && (mmin_long - deltaLongK) < cs_left)
            return true;
        else return false;
    }

    private void getCurrentScreenFrameInfo(float pageWidth, float pageHeight){
        current_pagewidth = pageWidth;
        current_pageheight = pageHeight;
        viewer_height = pdfView.getHeight();
        viewer_width = pdfView.getWidth();
    }

    private void getZoom(){
        if (c_zoom != pdfView.getZoom()) {
            c_zoom1 = c_zoom;
            c_zoom = pdfView.getZoom();
            if ((c_zoom - c_zoom1) > 0) {
                isZoom = TuzhiEnum.ZOOM_IN;
            } else if ((c_zoom - c_zoom1) < 0) {
                isZoom = TuzhiEnum.ZOOM_OUT;
            }
        } else isZoom = TuzhiEnum.ZOOM_NONE;
    }

    //获取当前屏幕所视区域的经纬度与像素范围
    private void getCurrentScreenLoc() {
        if (pdfView.getCurrentYOffset() > 0 || pdfView.getCurrentXOffset() > 0) {
            if (pdfView.getCurrentYOffset() > 0 && pdfView.getCurrentXOffset() > 0) {
                cs_top = (float) max_lat;
                cs_bottom = (float) min_lat;
                cs_left = (float) min_long;
                cs_right = (float) max_long;
            } else if (pdfView.getCurrentYOffset() > 0 && pdfView.getCurrentXOffset() <= 0) {
                cs_top = (float) max_lat;
                cs_bottom = (float) min_lat;
                cs_left = (float) ((Math.abs(pdfView.getCurrentXOffset()) / current_pagewidth) * w + min_long);
                cs_right = (float) ((viewer_width - pdfView.getCurrentXOffset()) / current_pagewidth * w + min_long);
            } else {
                cs_top = (float) (max_lat - Math.abs(pdfView.getCurrentYOffset()) / current_pageheight * h);
                cs_bottom = (float) (max_lat - (viewer_height - pdfView.getCurrentYOffset()) / current_pageheight * h);
                cs_left = (float) min_long;
                cs_right = (float) max_long;
            }
        } else {
            cs_top = (float) (max_lat - Math.abs(pdfView.getCurrentYOffset()) / current_pageheight * h);
            cs_bottom = (float) (max_lat - (viewer_height - pdfView.getCurrentYOffset()) / current_pageheight * h);
            cs_left = (float) ((Math.abs(pdfView.getCurrentXOffset()) / current_pagewidth) * w + min_long);
            cs_right = (float) ((viewer_width - pdfView.getCurrentXOffset()) / current_pagewidth * w + min_long);
        }
        //locError(Float.toString(cs_top) + "%" + Float.toString(cs_bottom) + "%" + Float.toString(cs_left) + "%" + Float.toString(cs_right));
        //

        centerPointLoc = new PointF((cs_bottom + cs_top) / 2, (cs_left + cs_right) / 2);
        if (showMode == TuzhiEnum.CENTERMODE) {
            locLatForTap = centerPointLoc.x;
            locLongForTap = centerPointLoc.y;
            showLocationText();
        }
        //
        //cs_top = pdfView.getCurrentYOffset()
    }

    //处理地理信息
    public void manageInfo(String GPTS) {
        double[] gpts = DataUtil.getGPTS(GPTS);
        min_lat = gpts[0];
        max_lat = gpts[1];
        min_long = gpts[2];
        max_long = gpts[3];
        //locError("delta : " + Double.toString(max_lat - min_lat + (max_long - min_long)));
        w = gpts[4];
        h = gpts[5];
        /*if (isGPSEnabled()) {
            getLocation();
        } else locError("请打开GPS功能");*/
    }

    public void displayFromFile(String filePath) {
        //PDFView pdfView;
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
        //title = pdfFileName;
        locError("filePath: " + filePath);
        locError("pdfFileName: " + pdfFileName);
        //toolbar.setTitle(pdfFileName);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    toolbar.setTitle(pdfFileName);
                });
                isRomance = true;
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 2500);
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
        PointF pt = new PointF(e.getRawX(), e.getRawY());
        //获取像素点的地理位置信息
        final PointF pt1 = getGeoLocFromPixL(pt);
        //在textview中显示地理位置信息
        showLocationText(pt1);
        return false;
    }

    @Override
    protected void onResume() {
        registerSensor();
        super.onResume();
    }

    private void registerSensor() {
        //注册传感器监听器
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (degree != 0 && predegree != degree && Math.abs(degree - predegree) > 10) {
                predegree = degree;
                if (isRomance) {
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
            degree = event.values[0];
            //locError("predegree: " + Float.toString(predegree) + " degree: " + Float.toString(degree));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    @Override
    protected void onPause() {
        sensorManager.unregisterListener(listener);
        super.onPause();
    }

    //开始记录轨迹
    private void initTrail() {
        if (isGPSEnabled()) {
            locError("开始绘制轨迹");
        } else locError("请打开GPS功能");
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(this, R.string.LocError, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        try {

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            updateView(location);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void recordTrail(Location location) {
        isLocate++;
        if (location != null) {
            if (isLocateEnd || isLocate == 1) {
                m_cTrail = m_cTrail + Double.toString(m_lat) + " " + Double.toString(m_long);
                //isLocateEnd = true;
            } else
                m_cTrail = m_cTrail + " " + Double.toString(m_lat) + " " + Double.toString(m_long) + " " + Double.toString(m_lat) + " " + Double.toString(m_long);
            //setHereLocation();
            //locError(Double.toString(m_lat) + "," + Double.toString(m_long) + "Come here");

        } else {

        }
    }

    //记录轨迹
    private void recordTrail(float x, float y) {
        isLocate++;
        last_x = x;
        last_y = y;
        locError(Integer.toString(isLocate));
        //if (location != null) {
        if (isLocateEnd || isLocate == 1) {
            if (!m_cTrail.isEmpty()) {
                if (isLocate > 2) {
                    int num = DataUtil.appearNumber(m_cTrail, " ");
                    String str = m_cTrail;
                    for (int i = 0; i <= num - 2; ++i) {
                        str = str.substring(str.indexOf(" ") + 1);
                    }
                    m_cTrail = m_cTrail.substring(0, m_cTrail.length() - str.length());
                } else m_cTrail = m_cTrail + " " + Float.toString(x) + " " + Float.toString(y);
            } else m_cTrail = m_cTrail + Float.toString(x) + " " + Float.toString(y);
        } else
            m_cTrail = m_cTrail + " " + Float.toString(x) + " " + Float.toString(y) + " " + Float.toString(x) + " " + Float.toString(y);
        //setHereLocation();
        //locError(Integer.toString(m_lat) + "," + Double.toString(m_long) + "Come here");

        //} else {

        //}
    }

    //坐标监听器
    protected final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.d(TAG, "Location changed to: " + getLocationInfo(location));
            updateView(location);
            if (!isLocateEnd) {
                recordTrail((float) location.getLatitude(), (float) location.getLongitude());
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
            } catch (SecurityException e) {

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
        if(isFullLocation && location != null){
        Geocoder gc = new Geocoder(JZActivity.this);
        List<Address> addresses = null;
        String msg = "";
        Log.d(TAG, "updateView.location = " + location);
            try {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                //Log.d(TAG, "updateView.addresses = " + Integer.toString(addresses.size()));
                if (addresses.size() > 0) Toast.makeText(JZActivity.this, "当前位置: " + addresses.get(0).getAddressLine(0), Toast.LENGTH_LONG).show();
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
        if (location != null) {
            m_lat = location.getLatitude();
            m_long = location.getLongitude();
            locError("wgs2000: " + Double.toString(m_lat) + "&&" + Double.toString(m_long) + "Come here");
            com.esri.arcgisruntime.geometry.Point wgs84Point = new com.esri.arcgisruntime.geometry.Point(m_long, m_lat, SpatialReferences.getWgs84());
            locError("wgs2000: " + Double.toString(wgs84Point.getX()) + "&&" + Double.toString(wgs84Point.getY()) + "Come here");
            com.esri.arcgisruntime.geometry.Point wgs2000Point = (com.esri.arcgisruntime.geometry.Point) GeometryEngine.project(wgs84Point, SpatialReference.create(4490));
            locError("wgs2000: " + Double.toString(wgs2000Point.getX()) + "&&" + Double.toString(wgs2000Point.getY()) + "Come here");
            m_lat = wgs2000Point.getY();
            m_long = wgs2000Point.getX();
            verx = (float) ((max_lat - m_lat) / (max_lat - min_lat));
            //setHereLocation();

        }
    }

    private void locError(String str){
        Log.w(TAG, "locError: " + str);
    }

    @Override
    protected void onDestroy() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (SecurityException e) {
            Log.w(TAG, e.toString());
        }
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void showListPopupWindow(View view, String query) {
        final ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        query = query.trim();
        //controlSpecificFunction(query);
        String sql = "select * from POI where";
        String[] strings = query.split(" ");
        for (int i = 0; i < strings.length; ++i) {
            if (strings.length == 1) sql = sql + " (name LIKE '%" + strings[i] + "%'";
            else {
                if (i == 0) sql = sql + " ((name LIKE '%" + strings[i] + "%'";
                else if (i != strings.length - 1)
                    sql = sql + " AND description LIKE '%" + strings[i] + "%'";
                else sql = sql + " AND name LIKE '%" + strings[i] + "%')";
            }
        }
        for (int i = 0; i < strings.length; ++i) {
            if (strings.length == 1) sql = sql + " OR description LIKE '%" + strings[i] + "%')";
            else {
                if (i == 0)
                    sql = sql + " OR (description LIKE '%" + strings[i] + "%'";
                else if (i != strings.length - 1)
                    sql = sql + " AND description LIKE '%" + strings[i] + "%'";
                else sql = sql + " AND description LIKE '%" + strings[i] + "%'))";
            }
        }
        sql = sql + " AND (x >= ? AND x <= ? AND y >= ? AND y <= ?)";
        Log.w(TAG, "showListPopupWindow: " + sql);
        final List<mPOIobj> pois = new ArrayList<>();
        Cursor cursor = LitePal.findBySQL(sql, String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long));
        if (cursor.moveToFirst()) {
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
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] items = new String[pois.size()];
        for (int i = 0; i < pois.size(); ++i) {
            items[i] = pois.get(i).getM_name();
        }

        // ListView适配器
        listPopupWindow.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, items));

        // 选择item的监听事件
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (pois.get(position).getM_POIC().contains("POI")) {
                    Intent intent = new Intent(JZActivity.this, singlepoi.class);
                    intent.putExtra("POIC", pois.get(position).getM_POIC());
                    JZActivity.this.startActivity(intent);
                } /*else if (esterEgg_plq && !esterEgg_lm) {
                    Intent intent = new Intent(JZActivity.this, plqpoishow.class);
                    intent.putExtra("xh", pois.get(position).getM_POIC());
                    JZActivity.this.startActivity(intent);
                } else if (pois.get(position).getM_POIC().contains("DMBZ"))
                    GoDMBZSinglePOIPage(pois.get(position).getM_POIC().replace("DMBZ", ""));
                else if (pois.get(position).getM_POIC().contains("DMLine"))
                    GoDMLSinglePOIPage(pois.get(position).getM_POIC().replace("DMLine", ""));
                else if (pois.get(position).getM_POIC().contains("DMPoint"))
                    GoDMPSinglePOIPage(pois.get(position).getM_POIC().replace("DMPoint", ""));*/

                listPopupWindow.dismiss();
                isDrawTrail = TuzhiEnum.NONE_DRAW_TYPE;
                invalidateOptionsMenu();
            }
        });

        // 对话框的宽高
        listPopupWindow.setWidth(600);
        listPopupWindow.setHeight(600);

        // ListPopupWindow的锚,弹出框的位置是相对当前View的位置
        listPopupWindow.setAnchorView(view);

        // ListPopupWindow 距锚view的距离
        listPopupWindow.setHorizontalOffset(50);
        listPopupWindow.setVerticalOffset(100);

        listPopupWindow.setModal(false);

        listPopupWindow.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (isDrawTrail) {
            case TRAIL_DRAW_TYPE:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.back).setVisible(false);
                menu.findItem(R.id.queryPOI).setVisible(true);
                menu.findItem(R.id.action_search).setVisible(false);
                //menu.findItem(R.id.queryLatLng).setVisible(false);
                break;
            case NONE_DRAW_TYPE:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.back).setVisible(false);
                menu.findItem(R.id.queryPOI).setVisible(true);
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.queryLatLng).setVisible(true);
                break;
            case SEARCH_DEMO:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.back).setVisible(false);
                menu.findItem(R.id.queryPOI).setVisible(false);
                menu.findItem(R.id.queryLatLng).setVisible(false);
                menu.findItem(R.id.action_search).setVisible(true);

                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
                // Assumes current activity is the searchable activity
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
                searchView.setSubmitButtonEnabled(true);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        showListPopupWindow(searchView, query);
                        //Toast.makeText(JZActivity.this, "该功能正在开发当中!", Toast.LENGTH_LONG).show();
                        return true;
                    }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return true;
                        }
                    });
            }

        return super.onPrepareOptionsMenu(menu);
    }

    //加载当前菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.queryPOI).setVisible(true);
        menu.findItem(R.id.back).setVisible(false);
        menu.findItem(R.id.queryLatLng).setVisible(true);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return true;
    }

    //菜单栏按钮监控
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                if (isDrawTrail != TuzhiEnum.SEARCH_DEMO) this.finish();
                else {
                    isDrawTrail = TuzhiEnum.NONE_DRAW_TYPE;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.info:
                /*Intent intent = new Intent(JZActivity.this, info_page.class);
                intent.putExtra("extra_data", WKT);
                startActivity(intent);*/
                break;
            case R.id.queryLatLng:
                isDrawTrail = TuzhiEnum.SEARCH_DEMO;
                invalidateOptionsMenu();
                break;
            case R.id.queryPOI:
                Intent intent2 = new Intent(JZActivity.this, pois.class);
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
