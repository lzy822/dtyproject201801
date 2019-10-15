package com.geopdfviewer.android;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import com.esri.arcgisruntime.geometry.AreaUnit;
import com.esri.arcgisruntime.geometry.AreaUnitId;
import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.LinearUnit;
import com.esri.arcgisruntime.geometry.LinearUnitId;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask;
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

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 地图主界面
 * 用于显示地图内容
 *
 * @author  李正洋
 *
 * @since   1.1
 */
public class MainInterface extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener, OnTapListener {
    private static final String TAG = "MainInterface";
    public static TuzhiEnum FILE_TYPE = TuzhiEnum.NONE_FILE_TYPE;
    Integer pageNumber = 0;
    public String content;
    String pdfFileName;
    PDFView pdfView;
    TextView textView;
    TextView scaleShow;
    //记录标题
    String title;

    private static int ORIGINAL_ZOOM = 1;



    //声明bts容器
    List<bt> bts;
    //声明bts1容器
    List<bt> bts1;

    private String WKT = "";
    private String uri = "";
    private String GPTS = "";
    private String BBox = "";
    private String MediaBox = "";
    private String CropBox = "";

    //坐标信息
    double m_lat = 0, m_long = 0;
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
    private TuzhiEnum isDrawType = TuzhiEnum.NONE_DRAW_TYPE;

    //记录当前使用者放缩情况
    private TuzhiEnum isZoom = TuzhiEnum.ZOOM_NONE;

    //点面判断 测试面数据
    private List<LatLng> latLngs_1;
    private List<LatLng> latLngs_2;
    private List<LatLng> latLngs_3;
    private List<LatLng> latLngs1_1;
    private List<LatLng> latLngs1_2;
    private List<LatLng> latLngs1_3;
    private List<List<LatLng>> patchsForLatLng;
    private List<List<LatLng>> patchsForPix;

    //当前记录的坐标点个数
    private int isLocate = 0;

    //上一个记录下来的坐标点坐标
    private float last_x, last_y;

    //记录当前GeoPDF识别码
    private String ic;

    //记录是否开始查询操作
    private boolean isQuery = false;

    //记录当前设置点位数
    private int poinum_messure = 0;

    //记录是否开始测量
    private boolean isMessure = false;

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
    Paint paint, paint1, paint2, paint3, paint4, paint5, paint6, paint8, paint9, paint10;

    //记录测量坐标串
    private String messure_pts = "";

    com.github.clans.fab.FloatingActionMenu floatingActionsMenu;

    private TuzhiEnum isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;

    private TuzhiEnum isCoordinateType = TuzhiEnum.COORDINATE_DEFAULT_TYPE;

    //记录是否需要详细地址信息
    private boolean isFullLocation = false;

    //记录verx
    float verx = 0;

    //记录是否开始绘制轨迹
    private TuzhiEnum isDrawTrail = TuzhiEnum.NONE_DRAW_TYPE;

    //记录点选的坐标位置
    private float locLatForTap, locLongForTap;

    //记录是否渲染完文件
    private boolean isRomance = false;

    //初始化传感器管理器
    private SensorManager sensorManager;
    private float predegree = 0;
    private float degree = 0;
    //记录比例尺格式
    DecimalFormat scale_df;

    //记录当前显示模式
    private TuzhiEnum showMode = TuzhiEnum.CENTERMODE;


    private boolean esterEgg_lm = false;

    //记录分段距离值
    private List<DistanceLatLng> distanceLatLngs = new ArrayList<>();
    private List<DistanceLatLng> distanceLatLngs1 = new ArrayList<>();
    private List<DistanceLatLng> distanceLatLngs2 = new ArrayList<>();
    private List<List<DistanceLatLng>> distancesLatLngs = new ArrayList<>();


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

    boolean CreatePOI = false;
    int POIType = -1;
    //记录距离之和
    private double distanceSum = 0;
    int theNum;
    float c_zoom1 = 1;

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
    private TuzhiEnum queryMode;

    SearchView searchView;
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
    List<kmltest> kmltests;
    //记录彩蛋1是否开启
    private boolean esterEgg_plq = false;
    //记录彩蛋2是否开启
    private boolean esterEgg_redline = false;
    //记录彩蛋4是否开启
    private boolean esterEgg_dm = false;
    //存储地名数据
    List<DMPoint> dmPoints = new ArrayList<>();
    List<DMLine> dmLines = new ArrayList<>();

    boolean type1Checked = false;
    boolean type2Checked = false;
    boolean type3Checked = false;
    CheckBox type1_checkbox;
    CheckBox type2_checkbox;
    CheckBox type3_checkbox;
    String[] strings;
    List<Trail> trails;
    SimpleDateFormat simpleDateFormat1;
    //记录当前绘制的线要素
    String drawLineFeature = "";
    //记录当前记录的所有线要素列表
    List<String> LineFeatures = new ArrayList<>();

    List<DMBZ> dmbzList;
    //声明中心点的位置坐标
    PointF centerPointLoc;
    private boolean hasBitmap1 = false;

    List<bt> DMBZBts = new ArrayList<>();
    private boolean hasDMBZBitmap = false;
    List<bt> DMBts = new ArrayList<>();
    private boolean hasDMBitmap = false;

    private boolean hasQueriedPoi = false;
    mPOIobj queriedPoi;
    private boolean hasQueriedLine = false;
    DMLine queriedLine;

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

    private String AddNormalPOI(final PointF pt1, final int num) {
        List<POI> POIs = LitePal.findAll(POI.class);
        POI poi = new POI();
        poi.setName("POI" + String.valueOf(POIs.size() + 1));
        poi.setIc(ic);
        if (showMode == TuzhiEnum.NOCENTERMODE) {
            poi.setX(pt1.x);
            poi.setY(pt1.y);
        } else {
            poi.setX(centerPointLoc.x);
            poi.setY(centerPointLoc.y);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MainInterface.this.getResources().getText(R.string.DateAndTime).toString());
        Date date = new Date(System.currentTimeMillis());
        poi.setTime(simpleDateFormat.format(date));
        poi.setPhotonum(0);
        String mpoic = "POI" + String.valueOf(System.currentTimeMillis());
        poi.setPoic(mpoic);
        poi.setType(strings[num]);
        poi.save();
        return mpoic;
    }

    private void AddIconPOI(final PointF pt1) {
        MPOI poi = new MPOI();
        poi.setNum(System.currentTimeMillis());
        poi.setImgPath(IconBitmaps.get(0).getM_path());
        if (showMode == TuzhiEnum.NOCENTERMODE) {
            poi.setLat(pt1.x);
            poi.setLng(pt1.y);
        } else {
            poi.setLat(centerPointLoc.x);
            poi.setLng(centerPointLoc.y);
        }
        poi.setHeight(80);
        poi.setWidth(80);
        poi.save();
    }

    private String[] queryDMLine(final PointF pt1) {
        String theLineId = "";
        String Linebzmc = "";
        boolean QueryLine = false;
        if (dmLines.size() > 0) {
            int linenum = dmLines.size();
            double deltas = 0;
            long calnum = 1;
            //显示线状要素
            for (int j = 0; j < linenum; ++j) {
                List<String> lines = dmLines.get(j).getMultiline();
                int linenum1 = lines.size();
                /*Paint paintk = new Paint();
                paintk.setStrokeWidth(0.15f);
                paintk.setColor(Color.BLACK);
                paintk.setStyle(Paint.Style.STROKE);*/
                for (int k = 0; k < linenum1; ++k) {
                    //String mline = lineUtil.getExternalPolygon(lines.get(k), 0.001);
                    //String[] strings = mline.split(" ");
                    String[] strings = lines.get(k).split(" ");
                    for (int cc = 0; cc < strings.length - 1; ++cc) {
                        String[] ptx1 = strings[cc].split(",");
                        String[] ptx2 = strings[cc + 1].split(",");
                        //PointF pointF = LatLng.getPixLocFromGeoL(new PointF(Float.valueOf(ptx1[1]), Float.valueOf(ptx1[0])), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        //PointF pointF1 = LatLng.getPixLocFromGeoL(new PointF(Float.valueOf(ptx2[1]), Float.valueOf(ptx2[0])), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        //PointF theTouchPt = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        Point pointF = new Point(Double.valueOf(ptx1[1]), Double.valueOf(ptx1[0]));
                        Point pointF1 = new Point(Double.valueOf(ptx2[1]), Double.valueOf(ptx2[0]));
                        PointF theTouchPt = pt1;
                        if (deltas == 0) {
                            //deltas = lineUtil.getDistance(lineUtil.getLineNormalEquation(pointF.x, pointF.y, pointF1.x, pointF1.y), theTouchPt);
                            double thedis = lineUtil.getDistance(lineUtil.getLineNormalEquation(pointF.getX(), pointF.getY(), pointF1.getX(), pointF1.getY()), theTouchPt);
                            if (thedis <= (lineUtil.getDistance1(pointF, pt1) >= lineUtil.getDistance1(pointF1, pt1) ? lineUtil.getDistance1(pointF, pt1) : lineUtil.getDistance1(pointF1, pt1)) && thedis >= (lineUtil.getDistance1(pointF, pt1) >= lineUtil.getDistance1(pointF1, pt1) ? lineUtil.getDistance1(pointF1, pt1) : lineUtil.getDistance1(pointF, pt1))) {
                                deltas = thedis;
                                theLineId = dmLines.get(j).getMapid();
                                Linebzmc = dmLines.get(j).getBzmc();
                            } else {
                                deltas = (lineUtil.getDistance1(pointF, pt1) >= lineUtil.getDistance1(pointF1, pt1) ? lineUtil.getDistance1(pointF1, pt1) : lineUtil.getDistance1(pointF, pt1));
                            }
                        } else {
                            //double delta1 = lineUtil.getDistance(lineUtil.getLineNormalEquation(pointF.x, pointF.y, pointF1.x, pointF1.y), theTouchPt);
                            double delta1 = lineUtil.getDistance(lineUtil.getLineNormalEquation(pointF.getX(), pointF.getY(), pointF1.getX(), pointF1.getY()), theTouchPt);
                            if (delta1 <= (lineUtil.getDistance1(pointF, pt1) >= lineUtil.getDistance1(pointF1, pt1) ? lineUtil.getDistance1(pointF, pt1) : lineUtil.getDistance1(pointF1, pt1)) && delta1 >= (lineUtil.getDistance1(pointF, pt1) >= lineUtil.getDistance1(pointF1, pt1) ? lineUtil.getDistance1(pointF1, pt1) : lineUtil.getDistance1(pointF, pt1))) {
                                //deltas = delta1;
                                //theId = dmLines.get(j).getBzmc();
                            } else
                                delta1 = (lineUtil.getDistance1(pointF, pt1) >= lineUtil.getDistance1(pointF1, pt1) ? lineUtil.getDistance1(pointF1, pt1) : lineUtil.getDistance1(pointF, pt1));
                            if (delta1 < deltas) {
                                deltas = delta1;
                                theLineId = dmLines.get(j).getMapid();
                                Linebzmc = dmLines.get(j).getBzmc();
                            }
                            calnum++;
                        }
                        //canvas.drawRoundRect(pointF.y>pointF1.y?pointF1.y:pointF.y, pointF.x>pointF1.x?pointF.x:pointF1.x, pointF.y>pointF1.y?pointF.y:pointF1.y, pointF.x>pointF1.x?pointF1.x:pointF.x, 0.5f,  0.5f, paint);
                        //canvas.drawRoundRect(pointF.y>pointF1.y?pointF1.y:pointF.y, pointF.x>pointF1.x?pointF.x:pointF1.x, pointF.y>pointF1.y?pointF.y:pointF1.y, pointF.x>pointF1.x?pointF1.x:pointF.x, 0.5f,  0.5f, paintk);
                    }
                }
            }
            if (deltas < 0.000005) {
                // TODO : 设计并完成查询语句
                //GoDMLSinglePOIPage(theLineId);
                QueryLine = true;
            }
        }
        return new String[]{theLineId, Linebzmc};
    }

    private String[] queryDMPoint(final PointF pt1) {
        String thePointId = "";
        String Pointbzmc = "";
        boolean QueryPoint = false;
        int n = 0;
        int num = 0;
        if (dmPoints.size() > 0) {
            DMPoint poii = dmPoints.get(0);
            PointF pointF = new PointF(poii.getLat(), poii.getLng());
            pointF = LatLng.getPixLocFromGeoL(pointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            pointF = new PointF(pointF.x, pointF.y - 70);
            //pointF = getGeoLocFromPixL(pointF);
            PointF pt8 = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            locError("pt1special : " + pt8.toString());
            float delta = Math.abs(pointF.x - pt8.x) + Math.abs(pointF.y - pt8.y);
            for (DMPoint poi : dmPoints) {
                PointF mpointF = new PointF(poi.getLat(), poi.getLng());
                mpointF = LatLng.getPixLocFromGeoL(mpointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                mpointF = new PointF(mpointF.x, mpointF.y - 70);
                if (Math.abs(mpointF.x - pt8.x) + Math.abs(mpointF.y - pt8.y) < delta && Math.abs(mpointF.x - pt8.x) + Math.abs(mpointF.y - pt8.y) < 35) {
                    locError("mpointFspecial : " + mpointF.toString());
                    delta = Math.abs(pointF.x - pt8.x) + Math.abs(pointF.y - pt8.y);
                    num = n;
                }
                locError("n : " + Integer.toString(n));
                n++;
            }
            locError("numspecial : " + Integer.toString(num));
            locError("deltaspecial : " + Float.toString(delta));
            if (delta < 35) {
                        /*Intent intent = new Intent(MainInterface.this, plqpoishow.class);
                        Log.w(TAG, "xhhh : " + kmltests.get(num).getXh());
                        intent.putExtra("xh", kmltests.get(num).getXh());
                        startActivity(intent);*/
                locError("GoDMBZSinglePOIPage" + dmPoints.get(num).getDimingid());
                QueryPoint = true;
                Pointbzmc = dmPoints.get(num).getBzmc();
                thePointId = dmPoints.get(num).getMapid();
                //GoDMPSinglePOIPage(dmPoints.get(num).getDimingid());
                //isQueried = true;
                //Toast.makeText(MainInterface.this, kmltests.get(num).getDmbzmc(), Toast.LENGTH_LONG).show();
                //locError(Integer.toString(kmltests.get(num).getPhotonum()));
            } else {
                locError("没有正常查询");
            }
        }
        return new String[]{thePointId, Pointbzmc};
    }

    private boolean queryDM(final PointF pt1) {
        String[] line = queryDMLine(pt1);
        String[] point = queryDMPoint(pt1);
        boolean QueryPoint = false;
        boolean QueryLine = false;
        if (!line[0].isEmpty()) QueryLine = true;
        if (!point[0].isEmpty()) QueryPoint = true;
        if (QueryPoint && QueryLine) {
            final String lineId = line[0];
            final String pointId = point[0];
            AlertDialog.Builder builder = new AlertDialog.Builder(MainInterface.this);
            builder.setTitle("提示");
            builder.setMessage("请选择你编辑的要素");
            builder.setPositiveButton(line[1] + "(线要素)", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GoDMLSinglePOIPage(lineId);
                }
            });
            builder.setNegativeButton(point[1] + "(点要素)", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GoDMPSinglePOIPage(pointId);
                }
            });
            builder.show();
        } else {
            if (QueryPoint) GoDMPSinglePOIPage(point[0]);
            else if (QueryLine) GoDMLSinglePOIPage(line[0]);
        }
        if (QueryLine || QueryPoint)
            return true;
        else
            return false;
    }

    private String addDMBZPoi(final PointF pt1) {
        dmbzList = LitePal.findAll(DMBZ.class);
        int size = dmbzList.size();
        Log.w(TAG, "dmbzList: " + size);
        DMBZ dmbz = new DMBZ();
        if (showMode == TuzhiEnum.NOCENTERMODE) {
            dmbz.setLat(pt1.x);
            dmbz.setLng(pt1.y);
        } else {
            dmbz.setLat(centerPointLoc.x);
            dmbz.setLng(centerPointLoc.y);
        }
        dmbz.setXH(String.valueOf(size + 1));
        dmbz.setTime(simpleDateFormat1.format(new Date(System.currentTimeMillis())));
        dmbz.save();
        dmbzList = LitePal.findAll(DMBZ.class);
        return dmbz.getXH();
    }

    public String queryNormalPoi(final PointF pt1) {
        List<mPOIobj> pois = new ArrayList<>();
        Cursor cursor = LitePal.findBySQL("select * from POI where x >= ? and x <= ? and y >= ? and y <= ?", String.valueOf(min_lat), String.valueOf(max_lat), String.valueOf(min_long), String.valueOf(max_long));
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
        locError("size : " + Integer.toString(pois.size()));
        int n = 0;
        int num = 0;
        if (pois.size() > 0) {
            mPOIobj poii = pois.get(0);
            PointF pointF1 = new PointF(poii.getM_X(), poii.getM_Y());
            pointF1 = LatLng.getPixLocFromGeoL(pointF1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            pointF1 = new PointF(pointF1.x, pointF1.y - 70);
            //pointF = getGeoLocFromPixL(pointF);
            final PointF pt9 = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            locError("pt1 : " + pt9.toString());
            float mdelta = Math.abs(pointF1.x - pt9.x) + Math.abs(pointF1.y - pt9.y);
            for (mPOIobj poi : pois) {
                PointF mpointF1 = new PointF(poi.getM_X(), poi.getM_Y());
                Log.w(TAG, "mpointF1 queried: " + mpointF1.x + ";" + mpointF1.y);
                mpointF1 = LatLng.getPixLocFromGeoL(mpointF1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                mpointF1 = new PointF(mpointF1.x, mpointF1.y - 70);
                if (Math.abs(mpointF1.x - pt9.x) + Math.abs(mpointF1.y - pt9.y) < mdelta && Math.abs(mpointF1.x - pt9.x) + Math.abs(mpointF1.y - pt9.y) < 35) {
                    //locError("mpointF : " + mpointF1.toString());
                    mdelta = Math.abs(pointF1.x - pt9.x) + Math.abs(pointF1.y - pt9.y);
                    num = n;
                }
                locError("n : " + Integer.toString(n));
                n++;
            }
            locError("num : " + Integer.toString(num));
            locError("mdelta : " + Float.toString(mdelta));
            if (mdelta < 35 || num != 0) {
                locError("起飞 : " + Float.toString(mdelta));
                return pois.get(num).getM_POIC();
                //locError(Integer.toString(pois.get(num).getPhotonum()));
            } else {
                locError("没有正常查询");
                return "";
            }
        } else return "";
    }

    private long QueriedIconPoiNum = -1;

    public long queryIconPoi(final PointF pt1) {
        int n = 0;
        int num = 0;
        List<MPOI> pois = LitePal.findAll(MPOI.class);
        if (pois.size() > 0) {
            MPOI poii = pois.get(0);
            PointF pointF1 = new PointF(poii.getLat(), poii.getLng());
            pointF1 = LatLng.getPixLocFromGeoL(pointF1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            //pointF = getGeoLocFromPixL(pointF);
            final PointF pt9 = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            locError("pt1 : " + pt9.toString());
            float mdelta = Math.abs(pointF1.x - pt9.x) + Math.abs(pointF1.y - pt9.y);
            for (MPOI poi : pois) {
                PointF mpointF1 = new PointF(poi.getLat(), poi.getLng());
                Log.w(TAG, "IconQuery queried: " + mpointF1.x + ";" + mpointF1.y);
                mpointF1 = LatLng.getPixLocFromGeoL(mpointF1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                if (Math.abs(mpointF1.x - pt9.x) + Math.abs(mpointF1.y - pt9.y) < mdelta && Math.abs(mpointF1.x - pt9.x) + Math.abs(mpointF1.y - pt9.y) < 50) {
                    //locError("mpointF : " + mpointF1.toString());
                    mdelta = Math.abs(pointF1.x - pt9.x) + Math.abs(pointF1.y - pt9.y);
                    num = n;
                }
                locError("IconQueryn : " + Integer.toString(n));
                n++;
            }
            locError("IconQuerynum : " + Integer.toString(num));
            locError("IconQuerydelta : " + Float.toString(mdelta));
            if (mdelta < 50 || num != 0) {
                //WidthAndHeight = (int)pois.get(num).getWidth();
                IconHeight = (int) pois.get(num).getHeight();
                IconWidth = (int) pois.get(num).getWidth();
                removeBufferIconBitmapForProgress();
                bufferIconBitmapMinus(pois.get(num));
                bufferIconBitmapPlus(pois.get(num));
                pdfView.zoomWithAnimation(c_zoom);
                forbiddenCheckboxForQuery();
                forbiddenWidgetForQuery();
                showQueriedWidget();
                locError("IconQuery起飞 : " + Float.toString(mdelta));
                return pois.get(num).getNum();
                //locError(Integer.toString(pois.get(num).getPhotonum()));
            } else {
                locError("IconQuery没有正常查询");
                return -1;
            }
        } else return -1;
    }

    @Override
    public boolean onTap(final MotionEvent e) {
        PointF pt = new PointF(e.getRawX(), e.getRawY());
        //获取像素点的地理位置信息
        final PointF pt1 = getGeoLocFromPixL(pt);
        //在textview中显示地理位置信息
        showLocationText(pt1);

        if (pt1.x != 0) {

            if (isDrawType == TuzhiEnum.POI_DRAW_TYPE && !isQuery) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainInterface.this);
                builder1.setTitle("提示");
                builder1.setMessage("请选择你要添加的兴趣点种类");
                builder1.setNegativeButton("标志点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showPopueWindowForIconAdd(pt1);
                        //AddIconPOI(pt1);
                        //pdfView.zoomWithAnimation(c_zoom);
                    }
                });
                builder1.setPositiveButton("普通兴趣点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainInterface.this);
                        builder.setTitle("提示");
                        builder.setMessage("请选择你要添加的图层");
                        builder.setNeutralButton(strings[0], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CreatePOI = true;
                                POIType = 0;
                                // TODO LM
                                if (!esterEgg_lm) {
                                    GoNormalSinglePOIPage(AddNormalPOI(pt1, 0));
                                }
                                pdfView.resetZoomWithAnimation();
                                POIType = -1;
                                CreatePOI = false;
                            }
                        });
                        builder.setNegativeButton(strings[1], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CreatePOI = true;
                                POIType = 1;

                                // TODO LM
                                if (!esterEgg_lm) {
                                    GoNormalSinglePOIPage(AddNormalPOI(pt1, 1));
                                } else {
                                    GoDMBZSinglePOIPage(addDMBZPoi(pt1));
                                }
                                pdfView.resetZoomWithAnimation();
                                POIType = -1;
                                CreatePOI = false;
                            }
                        });
                        builder.setPositiveButton(strings[2], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CreatePOI = true;
                                POIType = 2;
                                // TODO LM
                                if (!esterEgg_lm) {
                                    GoNormalSinglePOIPage(AddNormalPOI(pt1, 2));
                                }
                                pdfView.resetZoomWithAnimation();
                                POIType = -1;
                                CreatePOI = false;
                            }
                        });
                        builder.show();
                    }
                });
                builder1.show();
                /*if (CreatePOI && POIType != -1) {
                    List<POI> POIs = LitePal.findAll(POI.class);
                    POI poi = new POI();
                    poi.setName("POI" + String.valueOf(POIs.size() + 1));
                    poi.setIc(ic);
                    if (showMode == TuzhiEnum.NOCENTERMODE) {
                        poi.setX(pt1.x);
                        poi.setY(pt1.y);
                    } else {
                        poi.setX(centerPointLoc.x);
                        poi.setY(centerPointLoc.y);
                    }
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MainInterface.this.getResources().getText(R.string.DateAndTime).toString());
                    Date date = new Date(System.currentTimeMillis());
                    poi.setTime(simpleDateFormat.format(date));
                    poi.setPhotonum(0);
                    String mpoic = "POI" + String.valueOf(System.currentTimeMillis());
                    poi.setPoic(mpoic);
                    if (POIType == 0) {
                        poi.setType(strings[0]);
                    }
                    else if (POIType == 1) {
                        poi.setType(strings[1]);
                    }
                    else if (POIType == 2) {
                        poi.setType(strings[2]);
                    }
                    poi.save();
                    locError(pt1.toString());
                    pdfView.zoomWithAnimation(c_zoom);
                    POIType = -1;
                    CreatePOI = false;
                }*/
                /*Intent intent = new Intent(MainInterface.this, singlepoi.class);
                intent.putExtra("POIC", mpoic);
                startActivity(intent);*/

            }

            //记录线要素
            if (isDrawType == TuzhiEnum.LINE_DRAW_TYPE) {
                // TODO : 编辑添加线要素添加逻辑
                whiteBlank_fab.setVisibility(View.GONE);
                PointF ptf = pt1;
                Log.w(TAG, "onTaplzylzylzy: " + centerPointLoc.x);
                if (drawLineFeature.isEmpty()) {
                    if (showMode == TuzhiEnum.CENTERMODE) {
                        drawLineFeature = Float.toString(centerPointLoc.y) + "," + Float.toString(centerPointLoc.x) + ",0";
                    } else {
                        drawLineFeature = Float.toString(ptf.y) + "," + Float.toString(ptf.x) + ",0";
                    }
                } else {
                    if (showMode == TuzhiEnum.CENTERMODE) {
                        drawLineFeature = drawLineFeature + " " + Float.toString(centerPointLoc.y) + "," + Float.toString(centerPointLoc.x) + ",0";
                    } else {
                        drawLineFeature = drawLineFeature + " " + Float.toString(ptf.y) + "," + Float.toString(ptf.x) + ",0";
                    }
                }

                Log.w(TAG, "onTaplzylzylzy: " + drawLineFeature);
            }
            ///////////////////

            if (isMessure) {
                MessureChanged = true;
                locError("messure_pts" + messure_pts);
                poinum_messure++;
                if (poinum_messure == 1) {
                    if (showMode == TuzhiEnum.NOCENTERMODE) {
                        messure_pts = Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                    } else {
                        messure_pts = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                    }
                    //setTitle("正在测量");
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("正在测量(轨迹记录中)");
                    } else toolbar.setTitle("正在测量");
                } else if (poinum_messure == 2) {
                    if (showMode == TuzhiEnum.NOCENTERMODE) {
                        messure_pts = messure_pts + " " + Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                    } else {
                        messure_pts = messure_pts + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                    }
                    //setTitle("正在测量");
                    pdfView.zoomWithAnimation(c_zoom);
                    //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                } else {
                    if (showMode == TuzhiEnum.NOCENTERMODE) {
                        messure_pts = messure_pts + " " + Float.toString(pt1.x) + " " + Float.toString(pt1.y);
                    } else {
                        messure_pts = messure_pts + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                    }
                    //setTitle("正在测量");
                    pdfView.zoomWithAnimation(c_zoom);
                }
                if (showMode == TuzhiEnum.NOCENTERMODE) {
                    switch (distancesLatLngs.size()) {
                        case 0:
                            int size = distanceLatLngs.size();
                            if (size > 0) {
                                double distance = LatLng.algorithm(distanceLatLngs.get(size - 1).getLongitude(), distanceLatLngs.get(size - 1).getLatitude(), pt1.y, pt1.x);
                                DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, distanceLatLngs.get(size - 1).getDistance() + (float) distance);
                                distanceLatLngs.add(distanceLatLng);
                            } else {
                                DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, 0);
                                distanceLatLngs.add(distanceLatLng);
                            }
                            break;
                        case 1:
                            int size1 = distanceLatLngs1.size();
                            if (size1 > 0) {
                                double distance = LatLng.algorithm(distanceLatLngs1.get(size1 - 1).getLongitude(), distanceLatLngs1.get(size1 - 1).getLatitude(), pt1.y, pt1.x);
                                DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, distanceLatLngs1.get(size1 - 1).getDistance() + (float) distance);
                                distanceLatLngs1.add(distanceLatLng);
                            } else {
                                DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, 0);
                                distanceLatLngs1.add(distanceLatLng);
                            }
                            break;
                        case 2:
                            int size2 = distanceLatLngs2.size();
                            if (size2 > 0) {
                                double distance = LatLng.algorithm(distanceLatLngs2.get(size2 - 1).getLongitude(), distanceLatLngs2.get(size2 - 1).getLatitude(), pt1.y, pt1.x);
                                DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, distanceLatLngs2.get(size2 - 1).getDistance() + (float) distance);
                                distanceLatLngs2.add(distanceLatLng);
                            } else {
                                DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, 0);
                                distanceLatLngs2.add(distanceLatLng);
                            }
                            break;
                        default:
                            Toast.makeText(MainInterface.this, R.string.MessureNumOutOfIndex, Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    switch (distancesLatLngs.size()) {
                        case 0:
                            int size = distanceLatLngs.size();
                            if (size > 0) {
                                double distance = LatLng.algorithm(distanceLatLngs.get(size - 1).getLongitude(), distanceLatLngs.get(size - 1).getLatitude(), centerPointLoc.y, centerPointLoc.x);
                                DistanceLatLng distanceLatLng = new DistanceLatLng(centerPointLoc.x, centerPointLoc.y, distanceLatLngs.get(size - 1).getDistance() + (float) distance);
                                distanceLatLngs.add(distanceLatLng);
                            } else {
                                DistanceLatLng distanceLatLng = new DistanceLatLng(centerPointLoc.x, centerPointLoc.y, 0);
                                distanceLatLngs.add(distanceLatLng);
                            }
                            break;
                        case 1:
                            int size1 = distanceLatLngs1.size();
                            if (size1 > 0) {
                                double distance = LatLng.algorithm(distanceLatLngs1.get(size1 - 1).getLongitude(), distanceLatLngs1.get(size1 - 1).getLatitude(), centerPointLoc.y, centerPointLoc.x);
                                DistanceLatLng distanceLatLng = new DistanceLatLng(centerPointLoc.x, centerPointLoc.y, distanceLatLngs1.get(size1 - 1).getDistance() + (float) distance);
                                distanceLatLngs1.add(distanceLatLng);
                            } else {
                                DistanceLatLng distanceLatLng = new DistanceLatLng(centerPointLoc.x, centerPointLoc.y, 0);
                                distanceLatLngs1.add(distanceLatLng);
                            }
                            break;
                        case 2:
                            int size2 = distanceLatLngs2.size();
                            if (size2 > 0) {
                                double distance = LatLng.algorithm(distanceLatLngs2.get(size2 - 1).getLongitude(), distanceLatLngs2.get(size2 - 1).getLatitude(), centerPointLoc.y, centerPointLoc.x);
                                DistanceLatLng distanceLatLng = new DistanceLatLng(centerPointLoc.x, centerPointLoc.y, distanceLatLngs2.get(size2 - 1).getDistance() + (float) distance);
                                distanceLatLngs2.add(distanceLatLng);
                            } else {
                                DistanceLatLng distanceLatLng = new DistanceLatLng(centerPointLoc.x, centerPointLoc.y, 0);
                                distanceLatLngs2.add(distanceLatLng);
                            }
                            break;
                        default:
                            Toast.makeText(MainInterface.this, R.string.MessureNumOutOfIndex, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
                pdfView.zoomWithAnimation(c_zoom);
                //PointF mpt = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                //DistanceLatLng distanceLatLng = new DistanceLatLng(pt1.x, pt1.y, (float) distanceSum);
                //distanceLatLngs.add(distanceLatLng);
            }
            boolean isQueried = false;
            if (isQuery && isDrawType == TuzhiEnum.NONE_DRAW_TYPE) {
                Log.w(TAG, "onTapspecial : ");
                // TODO PLQ
                if (esterEgg_plq) {
                    int n = 0;
                    int num = 0;
                    if (kmltests.size() > 0) {
                        kmltest poii = kmltests.get(0);
                        PointF pointF = new PointF(poii.getLat(), poii.getLongi());
                        pointF = LatLng.getPixLocFromGeoL(pointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        pointF = new PointF(pointF.x, pointF.y - 70);
                        //pointF = getGeoLocFromPixL(pointF);
                        PointF pt8 = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        locError("pt1special : " + pt8.toString());
                        float delta = Math.abs(pointF.x - pt8.x) + Math.abs(pointF.y - pt8.y);
                        for (kmltest poi : kmltests) {
                            PointF mpointF = new PointF(poi.getLat(), poi.getLongi());
                            mpointF = LatLng.getPixLocFromGeoL(mpointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            mpointF = new PointF(mpointF.x, mpointF.y - 70);
                            if (Math.abs(mpointF.x - pt8.x) + Math.abs(mpointF.y - pt8.y) < delta && Math.abs(mpointF.x - pt8.x) + Math.abs(mpointF.y - pt8.y) < 35) {
                                locError("mpointFspecial : " + mpointF.toString());
                                delta = Math.abs(pointF.x - pt8.x) + Math.abs(pointF.y - pt8.y);
                                num = n;
                            }
                            locError("n : " + Integer.toString(n));
                            n++;
                        }
                        locError("numspecial : " + Integer.toString(num));
                        locError("deltaspecial : " + Float.toString(delta));
                        if (delta < 35 || num != 0) {
                            Intent intent = new Intent(MainInterface.this, plqpoishow.class);
                            Log.w(TAG, "xhhh : " + kmltests.get(num).getXh());
                            intent.putExtra("xh", kmltests.get(num).getXh());
                            startActivity(intent);
                            isQueried = true;
                            //Toast.makeText(MainInterface.this, kmltests.get(num).getDmbzmc(), Toast.LENGTH_LONG).show();
                            //locError(Integer.toString(kmltests.get(num).getPhotonum()));
                        } else locError("没有正常查询");
                    }
                    // TODO LM
                } else if (esterEgg_lm) {
                    int n = 0;
                    int num = 0;
                    if (dmbzList.size() > 0) {
                        DMBZ poii = dmbzList.get(0);
                        PointF pointF = new PointF(poii.getLat(), poii.getLng());
                        pointF = LatLng.getPixLocFromGeoL(pointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        pointF = new PointF(pointF.x, pointF.y - 70);
                        //pointF = getGeoLocFromPixL(pointF);
                        PointF pt8 = LatLng.getPixLocFromGeoL(pt1, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        locError("pt1special : " + pt8.toString());
                        float delta = Math.abs(pointF.x - pt8.x) + Math.abs(pointF.y - pt8.y);
                        for (DMBZ poi : dmbzList) {
                            PointF mpointF = new PointF(poi.getLat(), poi.getLng());
                            mpointF = LatLng.getPixLocFromGeoL(mpointF, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                            mpointF = new PointF(mpointF.x, mpointF.y - 70);
                            if (Math.abs(mpointF.x - pt8.x) + Math.abs(mpointF.y - pt8.y) < delta && Math.abs(mpointF.x - pt8.x) + Math.abs(mpointF.y - pt8.y) < 35) {
                                locError("mpointFspecial : " + mpointF.toString());
                                delta = Math.abs(pointF.x - pt8.x) + Math.abs(pointF.y - pt8.y);
                                num = n;
                            }
                            locError("n : " + Integer.toString(n));
                            n++;
                        }
                        locError("numspecial : " + Integer.toString(num));
                        locError("deltaspecial : " + Float.toString(delta));
                        if (delta < 35 || num != 0) {
                        /*Intent intent = new Intent(MainInterface.this, plqpoishow.class);
                        Log.w(TAG, "xhhh : " + kmltests.get(num).getXh());
                        intent.putExtra("xh", kmltests.get(num).getXh());
                        startActivity(intent);*/
                            locError("GoDMBZSinglePOIPage" + dmbzList.get(num).getXH());
                            GoDMBZSinglePOIPage(dmbzList.get(num).getXH());
                            isQueried = true;
                            //Toast.makeText(MainInterface.this, kmltests.get(num).getDmbzmc(), Toast.LENGTH_LONG).show();
                            //locError(Integer.toString(kmltests.get(num).getPhotonum()));
                        } else locError("没有正常查询");
                    }
                }

                // TODO DM
                if (esterEgg_dm && !isQueried) {
                    PointF dmPt = pt1;
                    isQueried = queryDM(dmPt);
                }

                if (!isQueried) {
                    String poic = queryNormalPoi(pt1);
                    if (!poic.isEmpty()) {
                        GoNormalSinglePOIPage(poic);
                        isQueried = true;
                    }
                }
                if (!isQueried && QueriedIconPoiNum == -1) {
                    Log.w(TAG, "IconQuery: ");
                    QueriedIconPoiNum = queryIconPoi(pt1);
                    if (QueriedIconPoiNum != -1)
                        isQueried = true;
                }
            }
        }
        return true;
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

    private void setLocationInfo(double lat, double longt) {
        String format = "0.";
        for (int i = 0; i < definition; ++i) {
            format = format + "0";
            //format.
        }
        DecimalFormat df = new DecimalFormat(format);
        Log.w(TAG, format);
        locError(Double.toString(lat));
        m_lat = Double.valueOf(df.format(lat));
        m_long = Double.valueOf(df.format(longt));
        textView = (TextView) findViewById(R.id.txt);
        textView.setText(df.format(lat) + "$$" + df.format(longt));
    }

    private boolean InspectTrail(float[] trails) {
        for (int i = 0; i < trails.length; i = i + 2) {
            if (trails[i] < cs_right && trails[i] > cs_left && trails[i + 1] < cs_top && trails[i + 1] > cs_bottom) {
                return true;
            }
        }
        return false;
    }

    private void drawLineFromLineString(final String mapid, final String line, final boolean isTL, boolean colorChange, Canvas canvas, Paint paint, Paint paint1) {
        String[] strings = line.split(" ");
        for (int n = 0; n < strings.length - 1; n++) {
            String[] ptx1 = strings[n].split(",");
            String[] ptx2 = strings[n + 1].split(",");
            PointF pointF = LatLng.getPixLocFromGeoL(new PointF(Float.valueOf(ptx1[1]), Float.valueOf(ptx1[0])), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            PointF pointF1 = LatLng.getPixLocFromGeoL(new PointF(Float.valueOf(ptx2[1]), Float.valueOf(ptx2[0])), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            if (isTL) {
                if (colorChange) {
                    paint.setColor(Color.WHITE);
                } else paint.setColor(Color.BLACK);
                colorChange = !colorChange;
            }
            if (hasQueriedLine && queriedLine.getMapid().equals(mapid))
                canvas.drawLine(pointF.x, pointF.y, pointF1.x, pointF1.y, paint1);
            else
                canvas.drawLine(pointF.x, pointF.y, pointF1.x, pointF1.y, paint);
            //canvas.drawRoundRect(pointF.y>pointF1.y?pointF1.y:pointF.y, pointF.x>pointF1.x?pointF.x:pointF1.x, pointF.y>pointF1.y?pointF.y:pointF1.y, pointF.x>pointF1.x?pointF1.x:pointF.x, 0.5f,  0.5f, paint);
            //canvas.drawRoundRect(pointF.y>pointF1.y?pointF1.y:pointF.y, pointF.x>pointF1.x?pointF.x:pointF1.x, pointF.y>pointF1.y?pointF.y:pointF1.y, pointF.x>pointF1.x?pointF1.x:pointF.x, 0.5f,  0.5f, paintk);
        }
    }

    private void drawLineFromLineString1(final String mapid, final String line, final boolean isTL, boolean colorChange, Canvas canvas, Paint paint, Paint paint1) {
        String[] strings = line.split(" ");
        List<PointF> pointFS = new ArrayList<>();
        for (int n = 0; n < strings.length; n++) {
            String[] ptx1 = strings[n].split(",");
            PointF pointF = LatLng.getPixLocFromGeoL(new PointF(Float.valueOf(ptx1[1]), Float.valueOf(ptx1[0])), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            pointFS.add(pointF);
        }
        //Douglas douglas = new Douglas(pointFS);
        //pointFS = douglas.douglasPeucker();
        for (int n = 0; n < pointFS.size() - 1; n++) {
            PointF pointF = LatLng.getPixLocFromGeoL(pointFS.get(n), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            PointF pointF1 = LatLng.getPixLocFromGeoL(pointFS.get(n + 1), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            if (isTL) {
                if (colorChange) {
                    paint.setColor(Color.WHITE);
                } else paint.setColor(Color.BLACK);
                colorChange = !colorChange;
            }
            if (hasQueriedLine && queriedLine.getMapid().equals(mapid))
                canvas.drawLine(pointF.x, pointF.y, pointF1.x, pointF1.y, paint1);
            else
                canvas.drawLine(pointF.x, pointF.y, pointF1.x, pointF1.y, paint);
            //canvas.drawRoundRect(pointF.y>pointF1.y?pointF1.y:pointF.y, pointF.x>pointF1.x?pointF.x:pointF1.x, pointF.y>pointF1.y?pointF.y:pointF1.y, pointF.x>pointF1.x?pointF1.x:pointF.x, 0.5f,  0.5f, paint);
            //canvas.drawRoundRect(pointF.y>pointF1.y?pointF1.y:pointF.y, pointF.x>pointF1.x?pointF.x:pointF1.x, pointF.y>pointF1.y?pointF.y:pointF1.y, pointF.x>pointF1.x?pointF1.x:pointF.x, 0.5f,  0.5f, paintk);
        }
    }

    private void drawDM(Canvas canvas) {
        int ptnum = dmPoints.size();
        int linenum = dmLines.size();
        //显示线状要素
        for (int j = 0; j < linenum; ++j) {
            List<String> lines = dmLines.get(j).getMultiline();
            int linenum1 = lines.size();
            boolean isTL = false;
                /*Paint paintk = new Paint();
                paintk.setStrokeWidth(0.15f);
                paintk.setColor(Color.BLACK);
                paintk.setStyle(Paint.Style.STROKE);*/

            Paint paint1 = new Paint();
            paint1.setStrokeWidth(10);
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setColor(Color.rgb(255, 0, 255));

            Paint paint = new Paint();
            paint.setStrokeWidth(10);
            paint.setStyle(Paint.Style.STROKE);
            switch (dmLines.get(j).getLbdm().substring(0, 3)){
                case "121":
                    paint.setColor(Color.rgb(201, 233, 254));
                    break;
                case "232":
                    paint.setColor(Color.rgb(255, 127, 63));
                    break;
                case "233":
                    paint.setColor(Color.BLACK);
                    isTL = true;
                    break;
                case "235":
                    paint.setColor(Color.rgb(255, 255, 0));
                    break;
                case "236":
                    paint.setColor(Color.BLACK);
                    break;
                case "239":
                    paint.setColor(Color.rgb(231, 120, 23));
                    break;
                case "243":
                    paint.setColor(Color.rgb(201, 233, 254));
                    break;
                case "244":
                    paint.setColor(Color.BLACK);
                    break;


            }
            /*if (dmLines.get(j).getLbdm().substring(0, 3).contains("121"))
                paint.setColor(Color.rgb(201, 233, 254));
            else if (dmLines.get(j).getLbdm().substring(0, 3).contains("232"))
                paint.setColor(Color.rgb(255, 127, 63));
            else if (dmLines.get(j).getLbdm().substring(0, 3).contains("233")) {
                paint.setColor(Color.BLACK);
                isTL = true;
            } else if (dmLines.get(j).getLbdm().substring(0, 3).contains("235"))
                paint.setColor(Color.rgb(255, 255, 0));
            else if (dmLines.get(j).getLbdm().substring(0, 3).contains("236"))
                paint.setColor(Color.BLACK);
            else if (dmLines.get(j).getLbdm().substring(0, 3).contains("239"))
                paint.setColor(Color.rgb(231, 120, 23));
            else if (dmLines.get(j).getLbdm().substring(0, 3).contains("243"))
                paint.setColor(Color.rgb(201, 233, 254));
            else if (dmLines.get(j).getLbdm().substring(0, 3).contains("244"))
                paint.setColor(Color.BLACK);*/
            boolean colorChange = false;
            float maxlat = dmLines.get(j).getMaxlat();
            float minlat = dmLines.get(j).getMinlat();
            float maxlng = dmLines.get(j).getMaxlng();
            float minlng = dmLines.get(j).getMinlng();
            if ((maxlat <= max_lat && maxlat >= min_lat) || (maxlng <= max_long && maxlng >= min_long) || (minlat >= min_lat && minlat <= max_lat) || (minlng <= max_long && minlng >= min_long) || (maxlat >= max_lat && minlat <= min_lat) || (maxlng >= max_long && minlng <= min_long)) {
                for (int k = 0; k < linenum1; ++k) {
                    //String mline = lineUtil.getExternalPolygon(lines.get(k), 0.001);
                    //String[] strings = mline.split(" ");
                    drawLineFromLineString(dmLines.get(j).getMapid(), lines.get(k), isTL, colorChange, canvas, paint, paint1);
                }
            } else
                Log.w(TAG, "drawDM: " + dmLines.get(j).getBzmc() + "; " + dmLines.get(j).getMinlng() + "; " + dmLines.get(j).getMaxlng() + "; " + dmLines.get(j).getMinlat() + "; " + dmLines.get(j).getMaxlat());
        }
        ///////////////////////
        List<PointF> showpts = new ArrayList<>();
        //显示点状要素
        for (int i = 0; i < ptnum; ++i) {
            //Log.w(TAG, "onLayerDrawn!!!: " + dmPoints.get(i).getLat());
            PointF pt = LatLng.getPixLocFromGeoL(new PointF(dmPoints.get(i).getLat(), dmPoints.get(i).getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            if ((dmPoints.get(i).getLng() < cs_right && dmPoints.get(i).getLng() > cs_left && dmPoints.get(i).getLat() < cs_top && dmPoints.get(i).getLat() > cs_bottom)) {
                //canvas.drawRect(new RectF(pt.x - 5, pt.y - 38, pt.x + 5, pt.y), paint2);
                //canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                if (c_zoom != 20) {
                    if (showpts.size() == 0) {
                        showpts.add(pt);
                        canvas.drawRect(new RectF(pt.x - 5, pt.y - 38, pt.x + 5, pt.y), paint2);
                        canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                        if (dmPoints.get(i).getImgpath() != null) {
                            if (!dmPoints.get(i).getImgpath().isEmpty()) {
                                if (hasDMBitmap && i < DMBts.size())
                                    canvas.drawBitmap(DMBts.get(i).getM_bm(), pt.x, pt.y - 70, paint1);
                                if (dmPoints.get(i).getTapepath() != null) {
                                    if (!dmPoints.get(i).getTapepath().isEmpty())
                                        canvas.drawCircle(pt.x, pt.y - 70, 35, paint1);
                                    else canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                } else {
                                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                }
                            } else {
                                if (dmPoints.get(i).getTapepath() != null) {
                                    if (!dmPoints.get(i).getTapepath().isEmpty())
                                        canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                    else canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);

                                } else {
                                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                    //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                }
                            }
                        } else {
                            if (dmPoints.get(i).getTapepath() != null) {
                                if (!dmPoints.get(i).getTapepath().isEmpty())
                                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                else canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                            } else {
                                canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                            }
                        }
                    } else {
                        float deltaDistance = 0;
                        for (int j = 0; j < showpts.size(); ++j) {
                            if (j == 0)
                                deltaDistance = Math.abs(pt.x - showpts.get(j).x) + Math.abs(pt.y - showpts.get(j).y);
                            else {
                                float deltaDistance1 = Math.abs(pt.x - showpts.get(j).x) + Math.abs(pt.y - showpts.get(j).y);
                                if (deltaDistance1 < deltaDistance)
                                    deltaDistance = deltaDistance1;
                            }
                        }
                        if (deltaDistance > 200) {
                            showpts.add(pt);
                            canvas.drawRect(new RectF(pt.x - 5, pt.y - 38, pt.x + 5, pt.y), paint2);
                            canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                            if (dmPoints.get(i).getImgpath() != null) {
                                if (!dmPoints.get(i).getImgpath().isEmpty()) {
                                    if (hasDMBitmap && i < DMBts.size())
                                        canvas.drawBitmap(DMBts.get(i).getM_bm(), pt.x, pt.y - 70, paint1);
                                    /*int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }*/
                                    if (dmPoints.get(i).getTapepath() != null) {
                                        if (!dmPoints.get(i).getTapepath().isEmpty())
                                            canvas.drawCircle(pt.x, pt.y - 70, 35, paint1);
                                        else canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                    } else {
                                        canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                    }
                                } else {
                                    if (dmPoints.get(i).getTapepath() != null) {
                                        if (!dmPoints.get(i).getTapepath().isEmpty())
                                            canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                        else canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                        //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);

                                    } else {
                                        canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                        //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                    }
                                }
                            } else {
                                if (dmPoints.get(i).getTapepath() != null) {
                                    if (!dmPoints.get(i).getTapepath().isEmpty())
                                        canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                    else canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                } else {
                                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                }
                            }
                        }
                    }
                } else {
                    canvas.drawRect(new RectF(pt.x - 5, pt.y - 38, pt.x + 5, pt.y), paint2);
                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                    if (dmPoints.get(i).getImgpath() != null) {
                        if (!dmPoints.get(i).getImgpath().isEmpty()) {
                            if (hasDMBitmap && i < DMBts.size())
                                canvas.drawBitmap(DMBts.get(i).getM_bm(), pt.x, pt.y - 70, paint1);
                                    /*int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }*/
                            if (dmPoints.get(i).getTapepath() != null) {
                                if (!dmPoints.get(i).getTapepath().isEmpty())
                                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint1);
                                else canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                            } else {
                                canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                            }
                        } else {
                            if (dmPoints.get(i).getTapepath() != null) {
                                if (!dmPoints.get(i).getTapepath().isEmpty())
                                    canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                                else canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);

                            } else {
                                canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                                //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                            }
                        }
                    } else {
                        if (dmPoints.get(i).getTapepath() != null) {
                            if (!dmPoints.get(i).getTapepath().isEmpty())
                                canvas.drawCircle(pt.x, pt.y - 70, 35, paint4);
                            else canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                            //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                        } else {
                            canvas.drawCircle(pt.x, pt.y - 70, 35, paint);
                        }
                    }
                }
            }
            ////////////////////
        }
        //////////////////////

    }

    private void drawDMBZ(Canvas canvas) {
        List<PointF> showpts = new ArrayList<>();
        for (int j = 0; j < dmbzList.size(); ++j) {
            Log.w(TAG, "onLayerDrawn:" + dmbzList.size());
            PointF ppt = LatLng.getPixLocFromGeoL(new PointF(dmbzList.get(j).getLat(), dmbzList.get(j).getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            if ((dmbzList.get(j).getLng() < cs_right && dmbzList.get(j).getLng() > cs_left && dmbzList.get(j).getLat() < cs_top && dmbzList.get(j).getLat() > cs_bottom)) {
                if (c_zoom == 10) {
                    canvas.drawRect(new RectF(ppt.x - 5, ppt.y - 38, ppt.x + 5, ppt.y), paint2);
                    if (dmbzList.get(j).getIMGPATH() == null || dmbzList.get(j).getIMGPATH().isEmpty()) {
                        if (dmbzList.get(j).getTAPEPATH() == null || dmbzList.get(j).getTAPEPATH().isEmpty())
                            canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                        else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                    } else {
                        if (dmbzList.get(j).getTAPEPATH() == null || dmbzList.get(j).getTAPEPATH().isEmpty())
                            canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                        else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint1);
                        if (hasDMBZBitmap && j <= DMBZBts.size() - 1) {
                            canvas.drawBitmap(DMBZBts.get(j).getM_bm(), ppt.x, ppt.y - 70, paint1);
                        }
                    }
                } else {
                    if (showpts.size() == 0) {
                        showpts.add(ppt);
                        canvas.drawRect(new RectF(ppt.x - 5, ppt.y - 38, ppt.x + 5, ppt.y), paint2);
                        canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                        if (dmbzList.get(j).getIMGPATH() != null) {
                            if (!dmbzList.get(j).getIMGPATH().isEmpty()) {
                                if (hasDMBZBitmap && j < DMBZBts.size())
                                    canvas.drawBitmap(DMBZBts.get(j).getM_bm(), ppt.x, ppt.y - 70, paint1);
                                if (dmbzList.get(j).getTAPEPATH() != null) {
                                    if (!dmbzList.get(j).getTAPEPATH().isEmpty())
                                        canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint1);
                                    else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                } else {
                                    canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                }
                            } else {
                                if (dmbzList.get(j).getTAPEPATH() != null) {
                                    if (!dmbzList.get(j).getTAPEPATH().isEmpty())
                                        canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                    else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);

                                } else {
                                    canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                    //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                }
                            }
                        } else {
                            if (dmbzList.get(j).getTAPEPATH() != null) {
                                if (!dmbzList.get(j).getTAPEPATH().isEmpty())
                                    canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                            } else {
                                canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                            }
                        }
                    } else {
                        float deltaDistance = 0;
                        for (int k = 0; k < showpts.size(); ++k) {
                            if (k == 0)
                                deltaDistance = Math.abs(ppt.x - showpts.get(k).x) + Math.abs(ppt.y - showpts.get(k).y);
                            else {
                                float deltaDistance1 = Math.abs(ppt.x - showpts.get(k).x) + Math.abs(ppt.y - showpts.get(k).y);
                                if (deltaDistance1 < deltaDistance)
                                    deltaDistance = deltaDistance1;
                            }
                        }
                        if (deltaDistance > 200) {
                            showpts.add(ppt);
                            canvas.drawRect(new RectF(ppt.x - 5, ppt.y - 38, ppt.x + 5, ppt.y), paint2);
                            canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                            if (dmbzList.get(j).getIMGPATH() != null) {
                                if (!dmbzList.get(j).getIMGPATH().isEmpty()) {
                                    if (hasDMBitmap && j < DMBZBts.size())
                                        canvas.drawBitmap(DMBZBts.get(j).getM_bm(), ppt.x, ppt.y - 70, paint1);
                                    /*int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }*/
                                    if (dmbzList.get(j).getTAPEPATH() != null) {
                                        if (!dmbzList.get(j).getTAPEPATH().isEmpty())
                                            canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint1);
                                        else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                    } else {
                                        canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                    }
                                } else {
                                    if (dmbzList.get(j).getTAPEPATH() != null) {
                                        if (!dmbzList.get(j).getTAPEPATH().isEmpty())
                                            canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                        else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                        //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);

                                    } else {
                                        canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                        //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                    }
                                }
                            } else {
                                if (dmbzList.get(j).getTAPEPATH() != null) {
                                    if (!dmbzList.get(j).getTAPEPATH().isEmpty())
                                        canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint4);
                                    else canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                } else {
                                    canvas.drawCircle(ppt.x, ppt.y - 70, 35, paint);
                                }
                            }
                        }
                    }
                }
            }

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

    private String SimplifyLines(String lines) {
        locError("看这里：" + lines);
        String SimplifiedLines = "";
        try {
            String[] cors = lines.split(" ");
            PointCollection pointCollection = new PointCollection(SpatialReferences.getWgs84());
            for (int i = 0; i < cors.length / 2; i = i + 2) {
                pointCollection.add(new com.esri.arcgisruntime.geometry.Point(Double.valueOf(cors[i + 1]), Double.valueOf(cors[i])));
            }
            locError("看这里：" + pointCollection.size());
            Polyline polyline = new Polyline(pointCollection);
            Polyline SimplifiedPolyline = (Polyline) GeometryEngine.generalize(polyline, 0.000005, true);
            int mNum = 0;
            for (int i = 0; i < SimplifiedPolyline.getParts().size(); ++i) {
                for (com.esri.arcgisruntime.geometry.Point pt : SimplifiedPolyline.getParts().get(i).getPoints()) {
                    ++mNum;
                    SimplifiedLines += pt.getY() + " " + pt.getX() + " ";
                }
            }
            locError("看这里：" + mNum + SimplifiedPolyline.toJson());
            /*Multipoint multipoint = (Multipoint) GeometryEngine.boundary(SimplifiedPolyline);
            //locError("看这里：" + multipoint.getGeometryType().toString());
            int num = multipoint.getPoints().size();
            for(int i = 0; i < num; ++i)
            {
                SimplifiedLines += multipoint.getPoints().get(i).getY() + " " + multipoint.getPoints().get(i).getX() + " ";
            }*/
            SimplifiedLines = SimplifiedLines.trim();
        }
        catch (IllegalArgumentException e1)
        {
            Log.w(TAG, "SimplifyLines: " + e1.toString());
        }
        catch (Exception e) {
            Log.w(TAG, "SimplifyLines: " + e.toString());
        }
        locError("看这里：" + SimplifiedLines);
        return  SimplifiedLines;
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        Log.w(TAG, "zoomCenteredTo: " + pdfView.getZoom());
        Log.w(TAG, "zoomCenteredTo: " + current_pageheight + "; " + current_pagewidth);
        current_pageheight = pageHeight;
        current_pagewidth = pageWidth;
        viewer_height = pdfView.getHeight();
        viewer_width = pdfView.getWidth();

                        /*if (isQuery && num_map == 4 & c_zoom > 5 & ( ( cs_bottom > 24.6 & cs_top < 25.3) & ( cs_left > 102.48 & cs_right < 102.97))){
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
        if (pdfView.getPositionOffset() != verx && isPos) {
            locHere_fab.setImageResource(R.drawable.ic_location_searching);
            isPos = false;
            locError("lzy");
        }
        locError("PositionOffset : " + Float.toString(pdfView.getPositionOffset()) + "verx : " + Float.toString(verx));
        //locError("top: " + Float.toString(cs_top) + " bottom: " + Float.toString(cs_bottom) + " left: " + Float.toString(cs_left) + " right: " + Float.toString(cs_right) + " zoom: " + Float.toString(c_zoom));
        if (c_zoom != pdfView.getZoom()) {
            c_zoom1 = c_zoom;
            c_zoom = pdfView.getZoom();
            if ((c_zoom - c_zoom1) > 0) {
                locError("zoom: " + Float.toString(c_zoom - c_zoom1));
                isZoom = TuzhiEnum.ZOOM_IN;
            } else if ((c_zoom - c_zoom1) < 0) {
                locError("zoom: " + Float.toString(c_zoom - c_zoom1));
                isZoom = TuzhiEnum.ZOOM_OUT;
            }
        } else isZoom = TuzhiEnum.ZOOM_NONE;
        locError("zoom: " + Float.toString(c_zoom));
        getCurrentScreenLoc();
        ImageView imageView = (ImageView) findViewById(R.id.myscale);
        //double scale_deltaLong = (max_long - min_long) / pageWidth * 100;
        double scale_deltaLong = (max_long - min_long) / pageWidth * imageView.getWidth();
        Log.w(TAG, "onLayerDrawn: " + imageView.getWidth());
        double scale_distance = LatLng.algorithm((cs_left + cs_right) / 2, (cs_bottom + cs_top) / 2, (cs_left + cs_right) / 2 + scale_deltaLong, (cs_bottom + cs_top) / 2);
        Log.w(TAG, "scale_distance: " + scale_distance);
        Log.w(TAG, "getMetric: " + getMetric());
        //scale_distance = scale_distance * getMetric();
        if (scale_distance > 1000) {
            scale_distance = scale_distance / 1000;
            scaleShow.setText(scale_df.format(scale_distance) + "公里");
        } else scaleShow.setText(scale_df.format(scale_distance) + "米");


        //Log.w(TAG, "onLayerDrawn!!!: " + dmPoints.size());
        //Log.w(TAG, "onLayerDrawn!!!: " + type1Checked);



        /*if (isMessure && showMode == TuzhiEnum.CENTERMODE) {
            String messure_pts1 = messure_pts;
            locError("messure_pts1" + messure_pts1);
            int poinum_messure1 = poinum_messure + 1;
            //poinum_messure++;
            if (poinum_messure1 == 1) {
                messure_pts1 = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                    toolbar.setTitle("正在测量(轨迹记录中)");
                } else toolbar.setTitle("正在测量");
            } else if (poinum_messure1 == 2) {
                messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                //setTitle("正在测量");
                pdfView.zoomWithAnimation(c_zoom);
                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
            } else {
                messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                //setTitle("正在测量");
                pdfView.zoomWithAnimation(c_zoom);
                //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
            }
            parseAndrawMessure(messure_pts1, canvas);

        }
        if (isMessure && showMode == TuzhiEnum.NOCENTERMODE) {
            parseAndrawMessure(messure_pts, canvas);
        }*/

        if (isMessure){
            switch (showMode){
                case CENTERMODE:
                    String messure_pts1 = messure_pts;
                    locError("messure_pts1" + messure_pts1);
                    int poinum_messure1 = poinum_messure + 1;
                    //poinum_messure++;
                    /*if (poinum_messure1 == 1) {
                        messure_pts1 = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                        if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                            toolbar.setTitle("正在测量(轨迹记录中)");
                        } else toolbar.setTitle("正在测量");
                    } else if (poinum_messure1 == 2) {
                        messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                        //setTitle("正在测量");
                        pdfView.zoomWithAnimation(c_zoom);
                        //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    } else {
                        messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                        //setTitle("正在测量");
                        pdfView.zoomWithAnimation(c_zoom);
                        //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    }*/
                    switch (poinum_messure1){
                        case 1:
                            messure_pts1 = Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                            if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                                toolbar.setTitle("正在测量(轨迹记录中)");
                            } else toolbar.setTitle("正在测量");
                            break;
                        case 2:
                            messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                            //setTitle("正在测量");
                            pdfView.zoomWithAnimation(c_zoom);
                            //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            messure_pts1 = messure_pts1 + " " + Float.toString(centerPointLoc.x) + " " + Float.toString(centerPointLoc.y);
                            //setTitle("正在测量");
                            pdfView.zoomWithAnimation(c_zoom);
                            break;

                    }
                    parseAndrawMessure(messure_pts1, canvas);
                    break;
                case NOCENTERMODE:
                    parseAndrawMessure(messure_pts, canvas);
                    break;
            }
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

        drawMLocPoint(canvas);
        if (isLocateEnd && !m_cTrail.isEmpty() || showTrail) {
            for (int ii = 0; ii < trails.size(); ii++) {
                String str1 = trails.get(ii).getPath();
                String[] TrailString = str1.split(" ");
                float[] Trails = new float[TrailString.length];
                for (int i = 0; i < TrailString.length; ++i) {
                    Trails[i] = Float.valueOf(TrailString[i]);
                }
                if (InspectTrail(Trails)) {
                    Log.w(TAG, "onLayerDrawn: ");
                    for (int j = 0; j < Trails.length - 2; j = j + 2) {
                        PointF pt11, pt12;
                        pt11 = LatLng.getPixLocFromGeoL(new PointF(Trails[j], Trails[j + 1]), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        pt12 = LatLng.getPixLocFromGeoL(new PointF(Trails[j + 2], Trails[j + 3]), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        canvas.drawLine(pt11.x, pt11.y, pt12.x, pt12.y, paint8);
                    }
                }
            }
        }
        if (isWhiteBlank) {
            try {
                // TODO 点位抽稀算法
                parseAndrawLinesforWhiteBlank(canvas);
                parseAndrawLinesforWhiteBlank(whiteBlankPt, canvas);
            /*SimplifyLines(whiteBlankPt);
            parseAndrawLinesforWhiteBlank(SimplifyLines(whiteBlankPt), canvas);*/
            }
            catch (Exception em) {
                locError(em.toString());
            }
        }
        // TODO REDLINE
        if (showMode == TuzhiEnum.CENTERMODE && esterEgg_redline) {
            managePatchsData();
            drawDemoArea(canvas);
        }
        if (isDrawType == TuzhiEnum.POI_DRAW_TYPE || showPOI) {

            // TODO DM
            if (esterEgg_dm && type1Checked) {
                drawDM(canvas);
            }
            // TODO PLQ
            if (esterEgg_plq) drawPLQData(canvas);
            //List<POI> pois = LitePal.where("ic = ?", ic).find(POI.class);

            // TODO LM
            if (type2Checked && esterEgg_lm) {
                drawDMBZ(canvas);
            }

            List<PointF> showpts = new ArrayList<>();
            List<POI> pois = LitePal.where("x <= " + String.valueOf(max_lat) + ";" + "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
            int size0 = pois.size();
            for (int i = 0; i < size0; ++i) {
                if (strings[0].equals(pois.get(i).getType()) && type1Checked) {
                    if ((pois.get(i).getY() < cs_right && pois.get(i).getY() > cs_left && pois.get(i).getX() < cs_top && pois.get(i).getX() > cs_bottom)) {
                        PointF pt3 = LatLng.getPixLocFromGeoL(new PointF(pois.get(i).getX(), pois.get(i).getY()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        if (c_zoom != 20) {
                            if (showpts.size() == 0) {
                                showpts.add(pt3);
                                canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                                //canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                if (pois.get(i).getPhotonum() == 0) {
                                    if (pois.get(i).getTapenum() == 0) {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                    } else {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                    }
                                } else {
                                    if (pois.get(i).getTapenum() == 0) {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                        //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                        int size = bts.size();
                                        for (int j = 0; j < size; ++j) {
                                            if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                                canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                locError("lzy");
                                            }
                                        }
                                    } else {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                        //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                        int size = bts.size();
                                        for (int j = 0; j < size; ++j) {
                                            if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                                canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                locError("lzy");
                                            }
                                        }
                                    }
                                }
                            } else {
                                float deltaDistance = 0;
                                for (int j = 0; j < showpts.size(); ++j) {
                                    if (j == 0)
                                        deltaDistance = Math.abs(pt3.x - showpts.get(j).x) + Math.abs(pt3.y - showpts.get(j).y);
                                    else {
                                        float deltaDistance1 = Math.abs(pt3.x - showpts.get(j).x) + Math.abs(pt3.y - showpts.get(j).y);
                                        if (deltaDistance1 < deltaDistance)
                                            deltaDistance = deltaDistance1;
                                    }
                                }
                                if (deltaDistance > 200) {
                                    showpts.add(pt3);
                                    canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                    if (pois.get(i).getPhotonum() == 0) {
                                        if (pois.get(i).getTapenum() == 0) {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                        } else {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                        }
                                    } else {
                                        //List<MPHOTO> mphotos = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MPHOTO.class);
                                        if (pois.get(i).getTapenum() == 0) {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                            //canvas.drawBitmap(, pt2.x, pt2.y - 70, paint1);
                                            int size = bts.size();
                                            for (int j = 0; j < size; ++j) {
                                                if (bts.get(j).getPoic().equals(pois.get(i).getPoic())) {
                                                    canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                    locError("lzy");
                                                }
                                            }
                                        } else {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                            //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                            int size = bts.size();
                                            for (int j = 0; j < size; ++j) {
                                                if (bts.get(j).getPoic().equals(pois.get(i).getPoic())) {
                                                    canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                    locError("lzy");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                            if (pois.get(i).getPhotonum() == 0) {
                                if (pois.get(i).getTapenum() == 0) {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                } else {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                }
                            } else {
                                if (pois.get(i).getTapenum() == 0) {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                    int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }
                                } else {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                    //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                    int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (strings[1].equals(pois.get(i).getType()) && type2Checked) {
                    if ((pois.get(i).getY() < cs_right && pois.get(i).getY() > cs_left && pois.get(i).getX() < cs_top && pois.get(i).getX() > cs_bottom)) {
                        PointF pt3 = LatLng.getPixLocFromGeoL(new PointF(pois.get(i).getX(), pois.get(i).getY()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        if (c_zoom != 20) {
                            if (showpts.size() == 0) {
                                showpts.add(pt3);
                                canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                                canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                if (pois.get(i).getPhotonum() == 0) {
                                    if (pois.get(i).getTapenum() == 0) {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                    } else {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                    }
                                } else {
                                    if (pois.get(i).getTapenum() == 0) {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                        //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                        int size = bts.size();
                                        for (int j = 0; j < size; ++j) {
                                            if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                                canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                locError("lzy");
                                            }
                                        }
                                    } else {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                        //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                        int size = bts.size();
                                        for (int j = 0; j < size; ++j) {
                                            if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                                canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                locError("lzy");
                                            }
                                        }
                                    }
                                }
                            } else {
                                float deltaDistance = 0;
                                for (int j = 0; j < showpts.size(); ++j) {
                                    if (j == 0)
                                        deltaDistance = Math.abs(pt3.x - showpts.get(j).x) + Math.abs(pt3.y - showpts.get(j).y);
                                    else {
                                        float deltaDistance1 = Math.abs(pt3.x - showpts.get(j).x) + Math.abs(pt3.y - showpts.get(j).y);
                                        if (deltaDistance1 < deltaDistance)
                                            deltaDistance = deltaDistance1;
                                    }
                                }
                                if (deltaDistance > 200) {
                                    showpts.add(pt3);
                                    canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                    if (pois.get(i).getPhotonum() == 0) {
                                        if (pois.get(i).getTapenum() == 0) {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                        } else {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                        }
                                    } else {
                                        List<MPHOTO> mphotos = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MPHOTO.class);
                                        if (pois.get(i).getTapenum() == 0) {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                            //canvas.drawBitmap(, pt2.x, pt2.y - 70, paint1);
                                            int size = bts.size();
                                            for (int j = 0; j < size; ++j) {
                                                if (bts.get(j).getPoic().equals(pois.get(i).getPoic())) {
                                                    canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                    locError("lzy");
                                                }
                                            }
                                        } else {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                            //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                            int size = bts.size();
                                            for (int j = 0; j < size; ++j) {
                                                if (bts.get(j).getPoic().equals(pois.get(i).getPoic())) {
                                                    canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                    locError("lzy");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                            if (pois.get(i).getPhotonum() == 0) {
                                if (pois.get(i).getTapenum() == 0) {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                } else {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                }
                            } else {
                                if (pois.get(i).getTapenum() == 0) {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                    int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }
                                } else {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                    //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                    int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (strings[2].equals(pois.get(i).getType()) && type3Checked) {
                    if ((pois.get(i).getY() < cs_right && pois.get(i).getY() > cs_left && pois.get(i).getX() < cs_top && pois.get(i).getX() > cs_bottom)) {
                        PointF pt3 = LatLng.getPixLocFromGeoL(new PointF(pois.get(i).getX(), pois.get(i).getY()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        if (c_zoom != 20) {
                            if (showpts.size() == 0) {
                                showpts.add(pt3);
                                canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                                canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                if (pois.get(i).getPhotonum() == 0) {
                                    if (pois.get(i).getTapenum() == 0) {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                    } else {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                    }
                                } else {
                                    if (pois.get(i).getTapenum() == 0) {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                        //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                        int size = bts.size();
                                        for (int j = 0; j < size; ++j) {
                                            if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                                canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                locError("lzy");
                                            }
                                        }
                                    } else {
                                        canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                        //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                        int size = bts.size();
                                        for (int j = 0; j < size; ++j) {
                                            if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                                canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                locError("lzy");
                                            }
                                        }
                                    }
                                }
                            } else {
                                float deltaDistance = 0;
                                for (int j = 0; j < showpts.size(); ++j) {
                                    if (j == 0)
                                        deltaDistance = Math.abs(pt3.x - showpts.get(j).x) + Math.abs(pt3.y - showpts.get(j).y);
                                    else {
                                        float deltaDistance1 = Math.abs(pt3.x - showpts.get(j).x) + Math.abs(pt3.y - showpts.get(j).y);
                                        if (deltaDistance1 < deltaDistance)
                                            deltaDistance = deltaDistance1;
                                    }
                                }
                                if (deltaDistance > 200) {
                                    showpts.add(pt3);
                                    canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                    if (pois.get(i).getPhotonum() == 0) {
                                        if (pois.get(i).getTapenum() == 0) {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                        } else {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                        }
                                    } else {
                                        List<MPHOTO> mphotos = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MPHOTO.class);
                                        if (pois.get(i).getTapenum() == 0) {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                            //canvas.drawBitmap(, pt2.x, pt2.y - 70, paint1);
                                            int size = bts.size();
                                            for (int j = 0; j < size; ++j) {
                                                if (bts.get(j).getPoic().equals(pois.get(i).getPoic())) {
                                                    canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                    locError("lzy");
                                                }
                                            }
                                        } else {
                                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                            //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                            int size = bts.size();
                                            for (int j = 0; j < size; ++j) {
                                                if (bts.get(j).getPoic().equals(pois.get(i).getPoic())) {
                                                    canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                                    locError("lzy");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            canvas.drawRect(new RectF(pt3.x - 5, pt3.y - 38, pt3.x + 5, pt3.y), paint2);
                            canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                            if (pois.get(i).getPhotonum() == 0) {
                                if (pois.get(i).getTapenum() == 0) {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint);
                                } else {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                }
                            } else {
                                if (pois.get(i).getTapenum() == 0) {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint4);
                                    //canvas.drawBitmap(, pt3.x, pt3.y - 70, paint1);
                                    int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }
                                } else {
                                    canvas.drawCircle(pt3.x, pt3.y - 70, 35, paint1);
                                    //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt3.x, pt3.y - 70, paint4);
                                    int size = bts.size();
                                    for (int j = 0; j < size; ++j) {
                                        if (pois.get(i).getPoic().equals(bts.get(j).getPoic())) {
                                            canvas.drawBitmap(bts.get(j).getM_bm(), pt3.x, pt3.y - 70, paint1);
                                            locError("lzy");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            /*if (pois.size() > 0) {
                for (POI poi : pois) {
                    PointF pt2 = LatLng.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                    //locError(Boolean.toString(poi.getPath().isEmpty()));
                    //locError(Integer.toString(poi.getPath().length()));
                    //locError(poi.getPath());
                    if (poi.getPhotonum() == 0) {
                        if (poi.getTapenum() == 0) {
                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                        } else {
                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint4);
                        }
                    } else {
                        List<MPHOTO> mphotos = LitePal.where("poic = ?", poi.getPoic()).find(MPHOTO.class);
                        if (poi.getTapenum() == 0) {
                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint4);
                            //canvas.drawBitmap(, pt2.x, pt2.y - 70, paint1);
                            int size = bts.size();
                            if (mphotos.size() != 0) {
                                for (int i = 0; i < size; ++i) {
                                    if (bts.get(i).getM_path().equals(mphotos.get(0).getPath())) {
                                        canvas.drawBitmap(bts.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                                        locError("lzy");
                                    }
                                }
                            } else {
                                POI poi2 = new POI();
                                if (mphotos.size() != 0) poi2.setPhotonum(mphotos.size());
                                else poi2.setToDefault("photonum");
                                poi2.updateAll("poic = ?", poi.getPoic());
                            }
                        } else {
                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint1);
                            //canvas.drawBitmap(getImageThumbnail(mphotos.get(0).getPath(), 100, 80), pt2.x, pt2.y - 70, paint4);
                            int size = bts.size();
                            if (mphotos.size() != 0) {
                                for (int i = 0; i < size; ++i) {
                                    if (bts.get(i).getM_path().equals(mphotos.get(0).getPath())) {
                                        canvas.drawBitmap(bts.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                                        locError("lzy");
                                    }
                                }
                            } else {
                                POI poi2 = new POI();
                                if (mphotos.size() != 0) poi2.setPhotonum(mphotos.size());
                                else poi2.setToDefault("photonum");
                                poi2.updateAll("poic = ?", poi.getPoic());
                            }
                        }
                    }
                }
            }*/
        }
        if (isDrawType == TuzhiEnum.LINE_DRAW_TYPE && (LineFeatures.size() > 0 || !drawLineFeature.isEmpty())) {
            for (int i = 0; i < LineFeatures.size(); ++i) {
                drawLineFromLineString("", LineFeatures.get(i), false, false, canvas, paint9, paint2);
            }
            String mdrawLineFeature = drawLineFeature;
            if (showMode == TuzhiEnum.CENTERMODE) {
                mdrawLineFeature = mdrawLineFeature + " " + centerPointLoc.y + "," + centerPointLoc.x + ",0";
            }
            if (!drawLineFeature.isEmpty())
                drawLineFromLineString("", mdrawLineFeature, false, false, canvas, paint9, paint2);
        }
        if (isAutoTrans && (isZoom == TuzhiEnum.ZOOM_IN || c_zoom == 10)) {
            SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
            int size = pref1.getInt("num", 0);
            if (size != 0) {
                float thedelta = 0;
                int thenum = 0;
                for (int j = 1; j <= size; ++j) {
                    SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
                    String str = "n_" + j + "_";
                    String Muri = pref2.getString(str + "uri", "");
                    String MGPTS = pref2.getString(str + "GPTS", "");
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
                        locError("see here");
                        //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
                        //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
                        locError("see here");
                        float mmin_lat = (pt_lb.x + pt_rb.x) / 2;
                        float mmax_lat = (pt_lt.x + pt_rt.x) / 2;
                        float mmin_long = (pt_lt.y + pt_lb.y) / 2;
                        float mmax_long = (pt_rt.y + pt_rb.y) / 2;
                        if (verifyAreaForAutoTrans(mmax_lat, mmin_lat, mmax_long, mmin_long)) {
                            float thedelta1 = Math.abs(cs_top - mmax_lat) + Math.abs(cs_bottom - mmin_lat) + Math.abs(cs_right - mmax_long) + Math.abs(cs_left - mmin_long);
                            locError("find delta1: " + Float.toString(thedelta1));
                            locError("find delta: " + Float.toString(thedelta));
                            locError("find num: " + Integer.toString(j));
                            if (j != num_map) {
                                if (thedelta == 0) {
                                    thedelta = thedelta1;
                                    thenum = j;
                                }
                                if (thedelta1 < thedelta) {
                                    locError("change!!!");
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
                }
                double deltaK_trans;
                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, MainInterface.this, TuzhiEnum.ZOOM_IN);
                locError("deltaK_trans: " + Double.toString(deltaK_trans));
                if (thenum != num_map && thenum != 0 && thedelta < deltaK_trans) {
                    geometry_whiteBlanks.clear();
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
                    getWhiteBlankData();
                }
            } else
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.AutoTransError), Toast.LENGTH_SHORT).show();
        } else if (c_zoom <= 1.5 && isAutoTrans && isZoom == TuzhiEnum.ZOOM_OUT) {
            SharedPreferences pref1 = getSharedPreferences("data_num", MODE_PRIVATE);
            int size = pref1.getInt("num", 0);
            if (size != 0) {
                float thedelta = 0;
                int thenum = 0;
                for (int j = 1; j <= size; ++j) {
                    SharedPreferences pref2 = getSharedPreferences("data", MODE_PRIVATE);
                    String str = "n_" + j + "_";
                    String Muri = pref2.getString(str + "uri", "");
                    String MGPTS = pref2.getString(str + "GPTS", "");
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
                        locError("see here");
                        //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
                        //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
                        locError("see here");
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
                            locError("delta : " + Float.toString(thedelta) + "thenum : " + Integer.toString(thenum));

                        }
                    }
                }
                double deltaK_trans;
                deltaK_trans = RenderUtil.getDeltaKforTrans(pageWidth, max_long, min_long, MainInterface.this, TuzhiEnum.ZOOM_OUT);
                locError("deltaK_trans: " + Double.toString(deltaK_trans));
                if (thenum != num_map && thenum != 0 && thedelta < deltaK_trans) {
                    geometry_whiteBlanks.clear();
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
                    getWhiteBlankData();
                }
            } else
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.AutoTransError), Toast.LENGTH_SHORT).show();
        }
        if (hasQueriedPoi) {
            PointF ptf = LatLng.getPixLocFromGeoL(new PointF(queriedPoi.getM_X(), queriedPoi.getM_Y()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            Paint ptSpecial = new Paint();
            ptSpecial.setColor(Color.rgb(255, 0, 255));
            ptSpecial.setStyle(Paint.Style.FILL);
            canvas.drawCircle(ptf.x, ptf.y - 70, 35, ptSpecial);
            canvas.drawRect(new RectF(ptf.x - 5, ptf.y - 38, ptf.x + 5, ptf.y), paint2);
        }
        if (isMessure && isMessureType == TuzhiEnum.MESSURE_DISTANCE_TYPE) drawMessureLine(canvas);
        drawMPOI(canvas);
        drawQueriedMPOI(canvas);

        if (mmpoints != null && !mmpoints.isEmpty()){
            for (int i = 0; i < mmpoints.size() - 1; ++i){
                if (i != mmpoints.size() - 1) {
                    PointF pointF0 = LatLng.getPixLocFromGeoL(new PointF((float) mmpoints.get(i).getY(), (float) mmpoints.get(i).getX()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    PointF pointF1 = LatLng.getPixLocFromGeoL(new PointF((float) mmpoints.get(i + 1).getY(), (float) mmpoints.get(i + 1).getX()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    canvas.drawLine(pointF0.x, pointF0.y, pointF1.x, pointF1.y, paint9);
                }else {
                    PointF pointF0 = LatLng.getPixLocFromGeoL(new PointF((float) mmpoints.get(i).getY(), (float) mmpoints.get(i).getX()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    PointF pointF1 = LatLng.getPixLocFromGeoL(new PointF((float) mmpoints.get(0).getY(), (float) mmpoints.get(0).getX()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    canvas.drawLine(pointF0.x, pointF0.y, pointF1.x, pointF1.y, paint8);
                }
            }
        }
    }

    private void drawSubMPOI(Canvas canvas, MPOI mpoi) {
        for (int j = 0; j < IconBitmaps.size(); ++j) {
            if (mpoi.getHeight() == 80) {
                if (IconBitmaps.get(j).getM_path().equals(mpoi.getImgPath())) {
                    PointF pointF = LatLng.getPixLocFromGeoL(new PointF(mpoi.getLat(), mpoi.getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    canvas.drawBitmap(IconBitmaps.get(j).getM_bm(), (float) (pointF.x - (mpoi.getWidth() / 2)), (float) (pointF.y - (mpoi.getHeight() / 2)), paint9);
                    break;
                }
            } else {
                if (IconBitmaps.get(j).getM_path().equals(mpoi.getImgPath() + "," + Integer.toString((int) mpoi.getHeight()))) {
                    PointF pointF = LatLng.getPixLocFromGeoL(new PointF(mpoi.getLat(), mpoi.getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    canvas.drawBitmap(IconBitmaps.get(j).getM_bm(), (float) (pointF.x - (mpoi.getWidth() / 2)), (float) (pointF.y - (mpoi.getHeight() / 2)), paint9);
                    break;
                }
            }
        }
    }

    private void drawMPOI(Canvas canvas) {
        List<MPOI> mpois = LitePal.findAll(MPOI.class);
        for (int i = 0; i < mpois.size(); ++i) {
            if (QueriedIconPoiNum == -1) {
                drawSubMPOI(canvas, mpois.get(i));
            } else if (mpois.get(i).getNum() != QueriedIconPoiNum) {
                drawSubMPOI(canvas, mpois.get(i));
            }
        }
    }


    private int WidthAndHeight = 80;
    private int IconWidth;
    private int IconHeight;

    private void drawQueriedMPOI1(Canvas canvas) {
        if (QueriedIconPoiNum != -1 && !IconShift) {
            MPOI mpoi = LitePal.where("num = ?", Long.toString(QueriedIconPoiNum)).find(MPOI.class).get(0);
            for (int j = 0; j < IconBitmaps.size(); ++j) {
                if (WidthAndHeight == 80) {
                    if (IconBitmaps.get(j).getM_path().equals(mpoi.getImgPath())) {
                        PointF pointF = LatLng.getPixLocFromGeoL(new PointF(mpoi.getLat(), mpoi.getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        //if (WidthAndHeight == mpoi.getWidth())
                        canvas.drawBitmap(IconBitmaps.get(j).getM_bm(), (float) (pointF.x - (WidthAndHeight / 2)), (float) (pointF.y - (WidthAndHeight / 2)), paint9);
                        /*else {
                            if (IconBitmaps.size() > iconBitmapNum)
                                IconBitmaps.remove(iconBitmapNum);
                            IconBitmaps.add(new bt(DataUtil.getImageThumbnail(mpoi.getImgPath(), WidthAndHeight, WidthAndHeight), mpoi.getImgPath() + Integer.toString(WidthAndHeight)));
                            canvas.drawBitmap(IconBitmaps.get(iconBitmapNum).getM_bm(), (float) (pointF.x - (WidthAndHeight / 2)), (float) (pointF.y - (WidthAndHeight / 2)), paint9);
                        }*/
                        canvas.drawRect((float) (pointF.x - (WidthAndHeight / 2)), (float) (pointF.y - (WidthAndHeight / 2)), (float) (pointF.x + (WidthAndHeight / 2)), (float) (pointF.y + (WidthAndHeight / 2)), paint9);
                    }
                } else {
                    if (IconBitmaps.get(j).getM_path().equals(mpoi.getImgPath() + "," + Integer.toString(WidthAndHeight))) {
                        PointF pointF = LatLng.getPixLocFromGeoL(new PointF(mpoi.getLat(), mpoi.getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        //if (WidthAndHeight == mpoi.getWidth())
                        canvas.drawBitmap(IconBitmaps.get(j).getM_bm(), (float) (pointF.x - (WidthAndHeight / 2)), (float) (pointF.y - (WidthAndHeight / 2)), paint9);
                        /*else {
                            if (IconBitmaps.size() > iconBitmapNum)
                                IconBitmaps.remove(iconBitmapNum);
                            IconBitmaps.add(new bt(DataUtil.getImageThumbnail(mpoi.getImgPath(), WidthAndHeight, WidthAndHeight), mpoi.getImgPath() + Integer.toString(WidthAndHeight)));
                            canvas.drawBitmap(IconBitmaps.get(iconBitmapNum).getM_bm(), (float) (pointF.x - (WidthAndHeight / 2)), (float) (pointF.y - (WidthAndHeight / 2)), paint9);
                        }*/
                        canvas.drawRect((float) (pointF.x - (WidthAndHeight / 2)), (float) (pointF.y - (WidthAndHeight / 2)), (float) (pointF.x + (WidthAndHeight / 2)), (float) (pointF.y + (WidthAndHeight / 2)), paint9);
                    }
                }

            }
        }
    }

    private void drawQueriedMPOI(Canvas canvas) {
        if (QueriedIconPoiNum != -1 && !IconShift) {
            MPOI mpoi = LitePal.where("num = ?", Long.toString(QueriedIconPoiNum)).find(MPOI.class).get(0);
            for (int j = 0; j < IconBitmaps.size(); ++j) {
                if (IconHeight == 80) {
                    if (IconBitmaps.get(j).getM_path().equals(mpoi.getImgPath())) {
                        PointF pointF = LatLng.getPixLocFromGeoL(new PointF(mpoi.getLat(), mpoi.getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        canvas.drawBitmap(IconBitmaps.get(j).getM_bm(), (float) (pointF.x - (IconWidth / 2)), (float) (pointF.y - (IconHeight / 2)), paint9);
                        canvas.drawRect((float) (pointF.x - (IconWidth / 2)), (float) (pointF.y - (IconHeight / 2)), (float) (pointF.x + (IconWidth / 2)), (float) (pointF.y + (IconHeight / 2)), paint9);
                    }
                } else {
                    if (IconBitmaps.get(j).getM_path().equals(mpoi.getImgPath() + "," + Integer.toString(IconHeight))) {
                        Log.w(TAG, "drawQueriedMPOI: " + IconWidth + ";" + IconHeight);
                        PointF pointF = LatLng.getPixLocFromGeoL(new PointF(mpoi.getLat(), mpoi.getLng()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        canvas.drawBitmap(IconBitmaps.get(j).getM_bm(), (float) (pointF.x - (IconWidth / 2)), (float) (pointF.y - (IconHeight / 2)), paint9);
                        canvas.drawRect((float) (pointF.x - (IconWidth / 2)), (float) (pointF.y - (IconHeight / 2)), (float) (pointF.x + (IconWidth / 2)), (float) (pointF.y + (IconHeight / 2)), paint9);
                    }
                }

            }
        }
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
        Log.w(TAG, "BBox : " + BBox);
        Log.w(TAG, "GPTS : " + GPTS);
        Log.w(TAG, "MediaBox : " + MediaBox);
        Log.w(TAG, "CropBox : " + CropBox);
        Log.w(TAG, "ic : " + ic);
        //GPTSList = new double[8];

    }

    //处理地理信息
    private void manageInfo() {
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
        } else locError("请打开GPS功能");
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

    //解析测量字符串并绘制
    private void parseAndrawMessure(String mmessure_pts, Canvas canvas) {
        if (isMessure && !mmessure_pts.isEmpty()) {
            distanceSum = 0;
            double distanceCurrent = 0;
            mmessure_pts = mmessure_pts.trim();
            String[] pts = mmessure_pts.split(" ");
            float[] mpts;
            if (pts.length <= 4 && pts.length > 3) {
                mpts = new float[pts.length];
                for (int i = 0; i < pts.length; ++i) {
                    mpts[i] = Float.valueOf(pts[i]);
                }
                for (int i = 0; i < pts.length; ++i) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                for (int i = 0; i < pts.length; i = i + 4) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx1 = new PointF(mpts[i], mpts[i + 1]);
                    PointF xx2 = new PointF(mpts[i + 2], mpts[i + 3]);
                    distanceCurrent = LatLng.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                    distanceSum = distanceCurrent;
                }
                for (int i = 0; i < pts.length; i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = LatLng.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; ++i) {
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
                    distanceCurrent = LatLng.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                    distanceSum = distanceSum + distanceCurrent;
                }
                //Path path = new Path();
                for (int i = 0; i < (pts.length * 2 - 4); i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = LatLng.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; ++i) {
                    locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                }
                //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                locError(mmessure_pts);
                locError("mpts : " + Integer.toString(mpts.length));
                if (isMessureType == TuzhiEnum.MESSURE_DISTANCE_TYPE) {
                    canvas.drawLines(mpts, paint6);
                } else if (isMessureType == TuzhiEnum.MESSURE_AREA_TYPE) {
                    //canvas.drawLines(mpts, paint6);
                    //canvas.drawLine(mpts[0], mpts[1], mpts[mpts.length - 2], mpts[mpts.length - 1], paint6);

                }
            } else {
                mpts = new float[pts.length];
            }
            if (isMessureType == TuzhiEnum.MESSURE_AREA_TYPE) {
                drawNormalPath(canvas);
            }
            DecimalFormat df1 = new DecimalFormat("0.00");
            //locError(Double.toString(distanceSum));
            if (MessureChanged) {
                if (isMessureType == TuzhiEnum.MESSURE_DISTANCE_TYPE) {
                /*if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE){
                    toolbar.setTitle(df1.format(distanceSum) + "米 , " + df1.format(distanceCurrent) + "米(轨迹记录中)");
                }else {
                    toolbar.setTitle(df1.format(distanceSum) + "米 , " + df1.format(distanceCurrent) + "米");
                }*/
                    //setTitle(df1.format(distanceSum) + "米");
                } else if (isMessureType == TuzhiEnum.MESSURE_AREA_TYPE) {
                /*for (int i = 0; i < mpts.length - 3; i = i + 2) {
                    area = area + (mpts[i] * mpts[i + 3] - mpts[i + 2] * mpts[i + 1]);
                }
                area = area - (mpts[0] * mpts[mpts.length - 1] - mpts[1] * mpts[mpts.length - 2]);
                area = Math.abs((area) / 2) / c_zoom / c_zoom;
                area = area * LatLng.algorithm((max_long + min_long) / 2, (max_lat + min_lat) / 2, (max_long + min_long) / 2 + (max_long - min_long) / (viewer_width - 2 * k_w), (max_lat + min_lat) / 2) * LatLng.algorithm((max_long + min_long) / 2, (max_lat + min_lat) / 2, (max_long + min_long) / 2, (max_lat + min_lat) / 2 + (max_lat - min_lat) / (viewer_height - 2 * k_h));*/
                    //area = area / 1.0965f;
                    //setTitle(df1.format(area) + "平方米");
                    PointCollection pointCollection = new PointCollection(SpatialReference.create(4490));
                    for (int i = 0; i < distanceLatLngs.size(); ++i) {
                        com.esri.arcgisruntime.geometry.Point point = new com.esri.arcgisruntime.geometry.Point(distanceLatLngs.get(i).getLongitude(), distanceLatLngs.get(i).getLatitude(), SpatialReference.create(4490));
                        pointCollection.add(point);
                    }
                /*if (showMode == TuzhiEnum.CENTERMODE){
                    com.esri.arcgisruntime.geometry.Point point = new com.esri.arcgisruntime.geometry.Point(centerPointLoc.y, centerPointLoc.x, SpatialReference.create(4490));
                    pointCollection.add(point);
                }*/
                    if (pointCollection.size() > 2) {
                        Polygon polygon = new Polygon(pointCollection);
                        area = GeometryEngine.areaGeodetic(polygon, new AreaUnit(AreaUnitId.SQUARE_KILOMETERS), GeodeticCurveType.GEODESIC);
                        com.esri.arcgisruntime.geometry.Point point = GeometryEngine.labelPoint(polygon);
                        CenterPtMessuredArea = new PointF((float)point.getY(), (float)point.getX());
                    }
                    //Log.w(TAG, "onLongClick: " + GeometryEngine.areaGeodetic(polygon, new AreaUnit(AreaUnitId.SQUARE_KILOMETERS), GeodeticCurveType.GEODESIC));
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle(df1.format(area) + "平方公里(轨迹记录中)");
                    } else toolbar.setTitle(df1.format(area) + "平方公里");
                }
                MessureChanged = false;
            }


        /*PointF xx = new PointF(mpts[0], mpts[1]);
        PointF yy = new PointF(mpts[2], mpts[3]);
        PointF pt11 = LatLng.getPixLocFromGeoL(xx);
        PointF pt22 = LatLng.getPixLocFromGeoL(yy);
        mpts[0] = pt11.x;
        mpts[1] = pt11.y;
        mpts[2] = pt22.x;
        mpts[3] = pt22.y;*/


            //canvas.drawLine(mpts[0], mpts[1], mpts[2], mpts[3], paint6);
        }
    }

    private double area = 0;
    private boolean MessureChanged;
    private PointF CenterPtMessuredArea = new PointF();

    private void drawNormalPath(Canvas canvas){
        Path path = new Path();
        int size = distanceLatLngs.size();
        for (int kk = 0; kk < size; ++kk){
            PointF pt0 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(kk).getLatitude(), distanceLatLngs.get(kk).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            canvas.drawCircle(pt0.x, pt0.y, 10, paint3);
            if (kk == 0){
                path.moveTo(pt0.x, pt0.y);
            }else {
                path.lineTo(pt0.x, pt0.y);
            }
        }
        if (size >= 2) {
            if (showMode == TuzhiEnum.CENTERMODE) {
                Log.w(TAG, "drawNormalPath: " + centerPointLoc.x + centerPointLoc.y);
                PointF pt2 = LatLng.getPixLocFromGeoL(centerPointLoc, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                path.lineTo(pt2.x, pt2.y);
            }
            PointF pt1 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(0).getLatitude(), distanceLatLngs.get(0).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            path.lineTo(pt1.x, pt1.y);
        }
        Paint fillPaint = new Paint();
        /*if (distanceLatLngs.size() > 2)
            fillPaint.setStyle(Paint.Style.FILL);
        else
            fillPaint.setStyle(Paint.Style.STROKE);*/
        fillPaint.setColor(Color.YELLOW);
        fillPaint.setAlpha(128);
        Log.w(TAG, "drawNormalPath: " + size);
        if (size >= 2){
            Log.w(TAG, "drawNormalPath22: ");
            fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawPath(path, fillPaint);
            PointF centerPt = LatLng.getPixLocFromGeoL(CenterPtMessuredArea, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            if (size >= 3) {
                DecimalFormat df1 = new DecimalFormat("0.00");
                Paint paint0 = new Paint();
                paint0.setColor(Color.RED);  //设置画笔颜色
                paint0.setStrokeWidth(5);//设置画笔宽度
                paint0.setTextSize(55);
                paint0.setStyle(Paint.Style.FILL);
                String text0 = df1.format(area);
                String text1 = "平方公里";
                canvas.drawText(text0 + text1, centerPt.x - (text1.length() / 2 * 55 + text0.length() / 4 * 55), centerPt.y + 27, paint0);
            }
        }/*else if (size == 1){
            Log.w(TAG, "drawNormalPath11: ");
            PointF pt0 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(0).getLatitude(), distanceLatLngs.get(0).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            canvas.drawCircle(pt0.x, pt0.y, 10, paint3);
        }else if (size == 2){
            Log.w(TAG, "drawNormalPath: ");
            PointF pt0 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(0).getLatitude(), distanceLatLngs.get(0).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            PointF pt1 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(1).getLatitude(), distanceLatLngs.get(1).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            canvas.drawLine(pt0.x, pt0.y, pt1.x, pt1.y, paint6);
            //drawCenterModePath(canvas);
        }*/
    }

    private void drawCenterModePath(Canvas canvas){
        Path path = new Path();
        for (int kk = 0; kk < distanceLatLngs.size(); ++kk){
            PointF pt0 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(kk).getLatitude(), distanceLatLngs.get(kk).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            if (kk == 0){
                path.moveTo(pt0.x, pt0.y);
            }else {
                path.lineTo(pt0.x, pt0.y);
                if (kk == distanceLatLngs.size() - 1){
                    PointF pt2 = LatLng.getPixLocFromGeoL(centerPointLoc, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    PointF pt1 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLngs.get(0).getLatitude(), distanceLatLngs.get(0).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    path.lineTo(pt2.x, pt2.y);
                    path.lineTo(pt1.x, pt1.y);
                }
            }
        }
        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.YELLOW);
        fillPaint.setAlpha(128);
        canvas.drawPath(path, fillPaint);
    }

    //解析白板字符串并绘制
    private void parseAndrawLinesforWhiteBlank(Canvas canvas) {
        int size = geometry_whiteBlanks.size();
        for (int k = 0; k < size; ++k) {
            if (canDrawLine(geometry_whiteBlanks.get(k).getMaxlat(), geometry_whiteBlanks.get(k).getMinlat(), geometry_whiteBlanks.get(k).getMaxlng(), geometry_whiteBlanks.get(k).getMinlng())){
            locError("geometry: " + geometry_whiteBlanks.get(k).getM_lines());
            Paint paint7 = new Paint();
            paint7.setStrokeWidth(3);
            paint7.setColor(geometry_whiteBlanks.get(k).getM_color());
            paint7.setStyle(Paint.Style.STROKE);
            if (isWhiteBlank && !geometry_whiteBlanks.get(k).getM_lines().isEmpty()) {

                geometry_whiteBlanks.get(k).setM_lines(geometry_whiteBlanks.get(k).getM_lines());
                String[] pts = geometry_whiteBlanks.get(k).getM_lines().split(" ");
                float[] mpts;
                if (pts.length <= 4 && pts.length > 3) {
                    mpts = new float[pts.length];
                    //TODO 优化点位白板刷新算法
                    for (int i = 0; i < pts.length; ++i) {
                        mpts[i] = Float.valueOf(pts[i]);
                    }
                    double max_lat1 = mpts[0];
                    double min_lat1 = mpts[0];
                    double max_long1 = mpts[1];
                    double min_long1 = mpts[1];
                    /*for (int i = 0; i < pts.length; ++i) {
                        locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                    }
                    for (int i = 0; i < pts.length; i = i + 4) {
                        //mpts[i] = Float.valueOf(pts[i]);
                        PointF xx1 = new PointF(mpts[i], mpts[i + 1]);
                        PointF xx2 = new PointF(mpts[i + 2], mpts[i + 3]);
                    }*/
                    for (int i = 0; i < pts.length; i = i + 2) {
                        if (mpts[i] > max_lat1) max_lat1 = mpts[i];
                        if (mpts[i] < min_lat1) min_lat1 = mpts[i];
                        if (mpts[i + 1] > max_long1) max_long1 = mpts[i + 1];
                        if (mpts[i + 1] < min_long1) min_long1 = mpts[i + 1];
                        PointF xx = new PointF(mpts[i], mpts[i + 1]);
                        PointF pt11 = LatLng.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        mpts[i] = pt11.x;
                        mpts[i + 1] = pt11.y;
                    }
                    /*for (int i = 0; i < pts.length; ++i) {
                        locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                    }*/
                    //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    locError(geometry_whiteBlanks.get(k).getM_lines());
                    locError("mpts : " + Integer.toString(mpts.length));
                    if ((max_lat1 < cs_top && max_lat1 > cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                            || (max_lat1 < cs_top && max_lat1 > cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                            || (min_lat1 < cs_top && min_lat1 > cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                            || (min_lat1 < cs_top && min_lat1 > cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                            || (max_lat1 > cs_top && min_lat1 < cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                            || (max_lat1 > cs_top && min_lat1 < cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                            || (max_lat1 < cs_top && max_lat1 > cs_bottom && max_long1 > cs_right && min_long1 < cs_left)
                            || (min_lat1 < cs_top && min_lat1 > cs_bottom && max_long1 > cs_right && min_long1 < cs_left)) {
                        canvas.drawLines(mpts, paint7);
                        locError("画线");
                    }
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

                    double max_lat1 = mpts[0];
                    double min_lat1 = mpts[0];
                    double max_long1 = mpts[1];
                    double min_long1 = mpts[1];

                    /*for (int i = 0; i < pts.length - 2; i = i + 2) {
                        //mpts[i] = Float.valueOf(pts[i]);
                        PointF xx1 = new PointF(Float.valueOf(pts[i]), Float.valueOf(pts[i + 1]));
                        PointF xx2 = new PointF(Float.valueOf(pts[i + 2]), Float.valueOf(pts[i + 3]));
                        distanceSum = distanceSum + LatLng.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                    }*/
                    for (int i = 0; i < (pts.length * 2 - 4); i = i + 2) {
                        if (mpts[i] > max_lat1) max_lat1 = mpts[i];
                        if (mpts[i] < min_lat1) min_lat1 = mpts[i];
                        if (mpts[i + 1] > max_long1) max_long1 = mpts[i + 1];
                        if (mpts[i + 1] < min_long1) min_long1 = mpts[i + 1];
                        PointF xx = new PointF(mpts[i], mpts[i + 1]);
                        PointF pt11 = LatLng.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        mpts[i] = pt11.x;
                        mpts[i + 1] = pt11.y;
                    }
                    /*for (int i = 0; i < pts.length; ++i) {
                        locError("mpts[" + Integer.toString(i) + "] : " + Float.toString(mpts[i]));
                    }*/
                    //Toast.makeText(MainInterface.this, "距离为: " + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    locError(geometry_whiteBlanks.get(k).getM_lines());
                    locError("mpts : " + Integer.toString(mpts.length));

                    if ((max_lat1 < cs_top && max_lat1 > cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                            || (max_lat1 < cs_top && max_lat1 > cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                            || (min_lat1 < cs_top && min_lat1 > cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                            || (min_lat1 < cs_top && min_lat1 > cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                            || (max_lat1 > cs_top && min_lat1 < cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                            || (max_lat1 > cs_top && min_lat1 < cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                            || (max_lat1 < cs_top && max_lat1 > cs_bottom && max_long1 > cs_right && min_long1 < cs_left)
                            || (min_lat1 < cs_top && min_lat1 > cs_bottom && max_long1 > cs_right && min_long1 < cs_left)) {
                        canvas.drawLines(mpts, paint7);

                        locError("画线");
                    }
                } else {
                    mpts = new float[pts.length];
                }
            }

        /*PointF xx = new PointF(mpts[0], mpts[1]);
        PointF yy = new PointF(mpts[2], mpts[3]);
        PointF pt11 = LatLng.getPixLocFromGeoL(xx);
        PointF pt22 = LatLng.getPixLocFromGeoL(yy);
        mpts[0] = pt11.x;
        mpts[1] = pt11.y;
        mpts[2] = pt22.x;
        mpts[3] = pt22.y;*/


                //canvas.drawLine(mpts[0], mpts[1], mpts[2], mpts[3], paint6);
            }
        }
    }

    private boolean canDrawLine(double max_lat1, double min_lat1, double max_long1, double min_long1) {
        if((max_lat1 < cs_top && max_lat1 > cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                || (max_lat1 < cs_top && max_lat1 > cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                || (min_lat1 < cs_top && min_lat1 > cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                || (min_lat1 < cs_top && min_lat1 > cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                || (max_lat1 > cs_top && min_lat1 < cs_bottom && max_long1 < cs_right && max_long1 > cs_left)
                || (max_lat1 > cs_top && min_lat1 < cs_bottom && min_long1 < cs_right && min_long1 > cs_left)
                || (max_lat1 < cs_top && max_lat1 > cs_bottom && max_long1 > cs_right && min_long1 < cs_left)
                || (min_lat1 < cs_top && min_lat1 > cs_bottom && max_long1 > cs_right && min_long1 < cs_left))
            return true;
        else return false;
    }

    //解析白板字符串并绘制1
    private void parseAndrawLinesforWhiteBlank(String whiteBlankPt, Canvas canvas) {
        locError("geometry: " + whiteBlankPt);
        Paint paint7 = new Paint();
        paint7.setStrokeWidth(3);
        paint7.setColor(color_Whiteblank);
        paint7.setStyle(Paint.Style.STROKE);
        if (isWhiteBlank && !whiteBlankPt.isEmpty()) {
            whiteBlankPt = whiteBlankPt.trim();
            String[] pts = whiteBlankPt.split(" ");
            float[] mpts;
            if (pts.length <= 4 && pts.length > 3) {
                mpts = new float[pts.length];
                for (int i = 0; i < pts.length; ++i) {
                    mpts[i] = Float.valueOf(pts[i]);
                }
                for (int i = 0; i < pts.length; ++i) {
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
                    PointF pt11 = LatLng.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; ++i) {
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
                    distanceSum = distanceSum + LatLng.algorithm(xx1.y, xx1.x, xx2.y, xx2.x);
                }
                for (int i = 0; i < (pts.length * 2 - 4); i = i + 2) {
                    //mpts[i] = Float.valueOf(pts[i]);
                    PointF xx = new PointF(mpts[i], mpts[i + 1]);
                    PointF pt11 = LatLng.getPixLocFromGeoL(xx, current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    mpts[i] = pt11.x;
                    mpts[i + 1] = pt11.y;
                }
                for (int i = 0; i < pts.length; ++i) {
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
        PointF pt11 = LatLng.getPixLocFromGeoL(xx);
        PointF pt22 = LatLng.getPixLocFromGeoL(yy);
        mpts[0] = pt11.x;
        mpts[1] = pt11.y;
        mpts[2] = pt22.x;
        mpts[3] = pt22.y;*/


            //canvas.drawLine(mpts[0], mpts[1], mpts[2], mpts[3], paint6);
        }

    }

    private double getDFromDFM(String string) {
        Log.w(TAG, "getDFromDFM: " + string);
        Log.w(TAG, "getDFromDFM: " + string.substring(0, string.indexOf("°")));
        Log.w(TAG, "getDFromDFM: " + string.substring(string.indexOf("°") + 1, string.indexOf("′")));
        Log.w(TAG, "getDFromDFM: " + string.substring(string.indexOf("′") + 1, string.indexOf("″")));
        int d = Integer.valueOf(string.substring(0, string.indexOf("°")));
        int f = Integer.valueOf(string.substring(string.indexOf("°") + 1, string.indexOf("′")));
        double f1 = f / 60;
        int m = Integer.valueOf(string.substring(string.indexOf("′") + 1, string.indexOf("″")));
        double m1 = m / 3600;
        Log.w(TAG, "getDFromDFM: " + Double.toString(d + f1 + m1));
        return d + f1 + m1;
    }

    private void drawDemoArea(Canvas canvas) {
        Log.w(TAG, "drawDemoArea: ");
        LatLng lt = new LatLng(centerPointLoc.x, centerPointLoc.y);
        int sizzzze = patchsForPix.size();
        for (int b = 0; b < sizzzze; b++) {
            int size = patchsForPix.get(b).size();
            for (int i = 0; i < size; ++i) {
                if (LatLng.PtInPolygon(lt, patchsForLatLng.get(b))) {
                    if (i < size - 1)
                        canvas.drawLine(patchsForPix.get(b).get(i).getLatitude(), patchsForPix.get(b).get(i).getLongitude(), patchsForPix.get(b).get(i + 1).getLatitude(), patchsForPix.get(b).get(i + 1).getLongitude(), paint10);
                    else
                        canvas.drawLine(patchsForPix.get(b).get(i).getLatitude(), patchsForPix.get(b).get(i).getLongitude(), patchsForPix.get(b).get(0).getLatitude(), patchsForPix.get(b).get(0).getLongitude(), paint10);
                } else {
                    if (i < size - 1)
                        canvas.drawLine(patchsForPix.get(b).get(i).getLatitude(), patchsForPix.get(b).get(i).getLongitude(), patchsForPix.get(b).get(i + 1).getLatitude(), patchsForPix.get(b).get(i + 1).getLongitude(), paint9);
                    else
                        canvas.drawLine(patchsForPix.get(b).get(i).getLatitude(), patchsForPix.get(b).get(i).getLongitude(), patchsForPix.get(b).get(0).getLatitude(), patchsForPix.get(b).get(0).getLongitude(), paint9);
                }
            }
        }
    }

    private void drawPLQData(Canvas canvas) {
        List<PointF> showpts = new ArrayList<>();
        int size = kmltests.size();
        for (int i = 0; i < size; ++i) {
            //LatLng latLng = kmltests.get(i).getLatLng();
            if (kmltests.get(i).getLat() != 0) {
                Log.w(TAG, "drawPLQData: ");
                if ((kmltests.get(i).getLongi() < cs_right && kmltests.get(i).getLongi() > cs_left && kmltests.get(i).getLat() < cs_top && kmltests.get(i).getLat() > cs_bottom)) {
                    PointF pt2 = LatLng.getPixLocFromGeoL(new PointF(kmltests.get(i).getLat(), kmltests.get(i).getLongi()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    if (c_zoom != 20) {
                        if (showpts.size() == 0) {
                            showpts.add(pt2);
                            canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                            canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                            if (hasBitmap1 && i <= bts1.size() - 1)
                                canvas.drawBitmap(bts1.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                        } else {
                            float deltaDistance = 0;
                            for (int j = 0; j < showpts.size(); ++j) {
                                if (j == 0)
                                    deltaDistance = Math.abs(pt2.x - showpts.get(j).x) + Math.abs(pt2.y - showpts.get(j).y);
                                else {
                                    float deltaDistance1 = Math.abs(pt2.x - showpts.get(j).x) + Math.abs(pt2.y - showpts.get(j).y);
                                    if (deltaDistance1 < deltaDistance)
                                        deltaDistance = deltaDistance1;
                                }
                            }
                            if (deltaDistance > 200) {
                                showpts.add(pt2);
                                canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                                canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                                if (hasBitmap1 && i <= bts1.size() - 1)
                                    canvas.drawBitmap(bts1.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                            }
                        }
                    } else {
                        canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                        canvas.drawCircle(pt2.x, pt2.y - 70, 35, paint);
                        if (hasBitmap1 && i <= bts1.size() - 1)
                            canvas.drawBitmap(bts1.get(i).getM_bm(), pt2.x, pt2.y - 70, paint1);
                    }
                }
            }
        }
    }

    //显示GeoPDF
    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;
        title = pdfFileName;
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.WHITE);
        pdfView.fromAsset(EnumClass.SAMPLE_FILE)
                .defaultPage(pageNumber)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
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

    public void drawMessureLine(Canvas canvas) {
        int DistanceSize = distancesLatLngs.size();
        if (DistanceSize > 0) {
            for (int j = 0; j < DistanceSize; ++j) {
                List<DistanceLatLng> distanceLatLngList = distancesLatLngs.get(j);
                for (int i = 0; i < distanceLatLngList.size(); ++i) {
                    DistanceLatLng distanceLatLng = distanceLatLngList.get(i);
                    PointF point = LatLng.getPixLocFromGeoL(new PointF(distanceLatLng.getLatitude(), distanceLatLng.getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    Paint paint0 = new Paint();
                    paint0.setColor(Color.RED);  //设置画笔颜色
                    paint0.setStrokeWidth(5);//设置画笔宽度
                    paint0.setTextSize(55);
                    paint0.setStyle(Paint.Style.FILL);
                    Paint paint01 = new Paint();
                    paint01.setColor(Color.WHITE);  //设置画笔颜色
                    paint01.setAlpha(180);
                    paint01.setStyle(Paint.Style.FILL);
                    //canvas.drawText(String.valueOf(distanceLatLng.getDistance()) + "米", distanceLatLng.getLatitude(), distanceLatLng.getLongitude(), paint0);
                    //if (distanceLatLng.getDistance() < 1000) canvas.drawText(String.valueOf(distanceLatLng.getDistance()) + "米", distanceLatLng.getLatitude(), distanceLatLng.getLongitude(), paint0);
                    //else canvas.drawText(String.valueOf(distanceLatLng.getDistance() / 1000) + "千米", distanceLatLng.getLatitude(), distanceLatLng.getLongitude(), paint0);
                    if (i < distanceLatLngList.size() - 1) {
                        DistanceLatLng distanceLatLng1 = distanceLatLngList.get(i + 1);
                        PointF point1 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLng1.getLatitude(), distanceLatLng1.getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                        canvas.drawLine(point.x, point.y, point1.x, point1.y, paint6);
                    }
                    if (distanceLatLng.getDistance() < 1000) {
                        if (distanceLatLng.getDistance() == 0) {
                            canvas.drawRect(point.x, point.y - 55, point.x + 110, point.y + 20, paint01);
                            canvas.drawText("起点", point.x, point.y, paint0);
                        } else {
                            String string = scale_df.format(distanceLatLng.getDistance()) + "米";
                            canvas.drawRect(point.x, point.y - 55, point.x + (string.length() - 2) * 27 + 55, point.y + 20, paint01);
                            canvas.drawText(scale_df.format(distanceLatLng.getDistance()) + "米", point.x, point.y, paint0);
                        }
                    } else {
                        String string = scale_df.format(distanceLatLng.getDistance() / 1000) + "公里";
                        canvas.drawRect(point.x, point.y - 55, point.x + (string.length() - 2) * 27 + 110, point.y + 20, paint01);
                        canvas.drawText(scale_df.format(distanceLatLng.getDistance() / 1000) + "公里", point.x, point.y, paint0);
                    }
                    canvas.drawCircle(point.x, point.y, 10, paint3);
                }
            }
        }

        for (int i = 0; i < distanceLatLngs.size(); ++i) {
            DistanceLatLng distanceLatLng = distanceLatLngs.get(i);
            PointF point = LatLng.getPixLocFromGeoL(new PointF(distanceLatLng.getLatitude(), distanceLatLng.getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
            Paint paint0 = new Paint();
            paint0.setColor(Color.RED);  //设置画笔颜色
            paint0.setStrokeWidth(5);//设置画笔宽度
            paint0.setTextSize(55);
            paint0.setStyle(Paint.Style.FILL);
            Paint paint01 = new Paint();
            paint01.setColor(Color.WHITE);  //设置画笔颜色
            paint01.setAlpha(180);
            paint01.setStyle(Paint.Style.FILL);
            //canvas.drawText(String.valueOf(distanceLatLng.getDistance()) + "米", distanceLatLng.getLatitude(), distanceLatLng.getLongitude(), paint0);
            //if (distanceLatLng.getDistance() < 1000) canvas.drawText(String.valueOf(distanceLatLng.getDistance()) + "米", distanceLatLng.getLatitude(), distanceLatLng.getLongitude(), paint0);
            //else canvas.drawText(String.valueOf(distanceLatLng.getDistance() / 1000) + "千米", distanceLatLng.getLatitude(), distanceLatLng.getLongitude(), paint0);
            if (i < distanceLatLngs.size() - 1) {
                DistanceLatLng distanceLatLng1 = distanceLatLngs.get(i + 1);
                PointF point1 = LatLng.getPixLocFromGeoL(new PointF(distanceLatLng1.getLatitude(), distanceLatLng1.getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                canvas.drawLine(point.x, point.y, point1.x, point1.y, paint6);
            }
            if (distanceLatLng.getDistance() < 1000) {
                if (distanceLatLng.getDistance() == 0) {
                    canvas.drawRect(point.x, point.y - 55, point.x + 110, point.y + 20, paint01);
                    canvas.drawText("起点", point.x, point.y, paint0);
                } else {
                    String string = scale_df.format(distanceLatLng.getDistance()) + "米";
                    canvas.drawRect(point.x, point.y - 55, point.x + (string.length() - 2) * 27 + 55, point.y + 20, paint01);
                    canvas.drawText(scale_df.format(distanceLatLng.getDistance()) + "米", point.x, point.y, paint0);
                }
            } else {
                String string = scale_df.format(distanceLatLng.getDistance() / 1000) + "公里";
                canvas.drawRect(point.x, point.y - 55, point.x + (string.length() - 2) * 27 + 110, point.y + 20, paint01);
                canvas.drawText(scale_df.format(distanceLatLng.getDistance() / 1000) + "公里", point.x, point.y, paint0);
            }
            canvas.drawCircle(point.x, point.y, 10, paint3);
        }
    }


    private void displayFromFile(String filePath) {
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(Color.WHITE);
        pdfView.setMidZoom(10);
        pdfView.setMaxZoom(20);
        final File file = new File(filePath);
        pdfView.fromFile(file)
                .password("123123123")
                .enableSwipe(false)
                .defaultPage(pageNumber)
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
        title = pdfFileName;
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
        timer.schedule(task, 2500);
    }

    private void managePatchsData() {
        int sizzze = patchsForLatLng.size();
        Log.w(TAG, "onCreatesize: " + patchsForPix.size());
        for (int b = 0; b < sizzze; b++) {
            Log.w(TAG, "onCreatesize1: " + patchsForPix.size());
            if (b == 0) {
                int size222 = patchsForLatLng.get(b).size();
                for (int i = 0; i < size222; ++i) {
                    PointF pt = LatLng.getPixLocFromGeoL(new PointF(patchsForLatLng.get(b).get(i).getLatitude(), patchsForLatLng.get(b).get(i).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    if (latLngs1_1.size() < size222)
                        latLngs1_1.add(new LatLng(pt.x, pt.y));
                    else {
                        latLngs1_1.get(i).setLatitude(pt.x);
                        latLngs1_1.get(i).setLongitude(pt.y);
                    }
                }
                if (patchsForPix.size() < patchsForLatLng.size())
                    patchsForPix.add(latLngs1_1);
                else {
                    patchsForPix.set(b, latLngs1_1);
                }
            } else if (b == 1) {
                int size222 = patchsForLatLng.get(b).size();
                for (int i = 0; i < size222; ++i) {
                    PointF pt = LatLng.getPixLocFromGeoL(new PointF(patchsForLatLng.get(b).get(i).getLatitude(), patchsForLatLng.get(b).get(i).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    if (latLngs1_2.size() < size222)
                        latLngs1_2.add(new LatLng(pt.x, pt.y));
                    else {
                        latLngs1_2.get(i).setLatitude(pt.x);
                        latLngs1_2.get(i).setLongitude(pt.y);
                    }
                }
                if (patchsForPix.size() < patchsForLatLng.size())
                    patchsForPix.add(latLngs1_2);
                else {
                    patchsForPix.set(b, latLngs1_2);
                }
            }
            if (b == 2) {
                int size222 = patchsForLatLng.get(b).size();
                for (int i = 0; i < size222; ++i) {
                    PointF pt = LatLng.getPixLocFromGeoL(new PointF(patchsForLatLng.get(b).get(i).getLatitude(), patchsForLatLng.get(b).get(i).getLongitude()), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
                    if (latLngs1_3.size() < size222)
                        latLngs1_3.add(new LatLng(pt.x, pt.y));
                    else {
                        latLngs1_3.get(i).setLatitude(pt.x);
                        latLngs1_3.get(i).setLongitude(pt.y);
                    }
                }
                if (patchsForPix.size() < patchsForLatLng.size())
                    patchsForPix.add(latLngs1_3);
                else {
                    patchsForPix.set(b, latLngs1_3);
                }
            }
        }
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

    private boolean verifyAreaForAutoTrans(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long, int type) {
        if (type == 0) {
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
        } else {
            if (mmin_lat < cs_bottom && mmax_long > cs_right && mmin_long < cs_left) return true;
            else if (mmax_lat > cs_top && mmax_long > cs_right && mmin_long < cs_left) return true;
            else if (mmax_lat > cs_top && mmin_lat < cs_bottom && mmin_long < cs_left) return true;
            else if (mmax_lat > cs_top && mmin_lat < cs_bottom && mmax_long > cs_right) return true;
            else if (mmax_lat > cs_top && mmin_lat < cs_bottom && mmax_long > cs_right && mmin_long < cs_left)
                return true;
            else return false;
        }
    }

    //获取文件读取权限
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                EnumClass.READ_EXTERNAL_STORAGE);
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                EnumClass.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            EnumClass.READ_EXTERNAL_STORAGE, EnumClass.WRITE_EXTERNAL_STORAGE},
                    EnumClass.PERMISSION_CODE
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
            startActivityForResult(intent, EnumClass.REQUEST_CODE_PHOTO);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void AddPhoto(final Uri uri, final float[] latandlong, final int num) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MainInterface.this.getResources().getText(R.string.DateAndTime).toString());
        final Date date = new Date(System.currentTimeMillis());
        CreatePOI = true;
        POIType = num;
        final List<POI> POIs = LitePal.where("type = ?", strings[num]).find(POI.class);
        int size = POIs.size();
        if (size > 0) {
            float K = (float) 0.002;
            float delta = Math.abs(POIs.get(0).getX() - latandlong[0]) + Math.abs(POIs.get(0).getY() - latandlong[1]);
            for (int i = 0; i < size; ++i) {
                float theLat = POIs.get(i).getX();
                float theLong = POIs.get(i).getY();
                float delta1 = Math.abs(theLat - latandlong[0]) + Math.abs(theLong - latandlong[1]);
                if (delta1 < delta && delta1 < K) {
                    delta = delta1;
                    theNum = i;
                }
            }
            if (delta < K) {
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(MainInterface.this);
                dialog1.setTitle("提示");
                dialog1.setMessage("你想怎样添加照片");
                dialog1.setCancelable(false);
                dialog1.setPositiveButton("合并到<" + POIs.get(theNum).getName() + ">点图集", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        POI poi = new POI();
                        poi.setPhotonum(POIs.get(theNum).getPhotonum() + 1);
                        locError("holly :" + poi.updateAll("poic = ?", POIs.get(theNum).getPoic()));
                        DataUtil.addPhotoToDB(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri), ic, POIs.get(theNum).getPoic(), simpleDateFormat.format(date));
                        getNormalBitmap();
                        updateMapPage(POIs.get(theNum).getPoic(), num);
                    }
                });
                dialog1.setNegativeButton("创建新兴趣点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long time = System.currentTimeMillis();
                        String poic = "POI" + String.valueOf(time);
                        DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), latandlong[0], latandlong[1], simpleDateFormat.format(date), num);
                        DataUtil.addPhotoToDB(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri), ic, poic, simpleDateFormat.format(date));
                        getNormalBitmap();
                        updateMapPage(poic, num);
                    }
                });
                dialog1.show();
            } else {
                long time = System.currentTimeMillis();
                String poic = "POI" + String.valueOf(time);
                DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), latandlong[0], latandlong[1], simpleDateFormat.format(date), num);
                DataUtil.addPhotoToDB(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri), ic, poic, simpleDateFormat.format(date));
                getNormalBitmap();
                updateMapPage(poic, num);
            }
        } else {
            long time = System.currentTimeMillis();
            String poic = "POI" + String.valueOf(time);
            DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), latandlong[0], latandlong[1], simpleDateFormat.format(date), num);
            DataUtil.addPhotoToDB(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri), ic, poic, simpleDateFormat.format(date));
            getNormalBitmap();
            updateMapPage(poic, num);
        }
        pdfView.zoomWithAnimation(c_zoom);
        POIType = -1;
        CreatePOI = false;
    }

    private void AddTape(final Uri uri, final int num) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MainInterface.this.getResources().getText(R.string.DateAndTime).toString());
        final Date date = new Date(System.currentTimeMillis());
        theNum = 0;
        final long time = System.currentTimeMillis();
        final List<POI> POIs = LitePal.where("type = ?", strings[num]).find(POI.class);
        int size = POIs.size();
        if (size > 0) {
            float K = (float) 0.002;
            float delta = Math.abs(POIs.get(0).getX() - (float) m_lat) + Math.abs(POIs.get(0).getY() - (float) m_long);
            for (int i = 0; i < size; ++i) {
                float theLat = POIs.get(i).getX();
                float theLong = POIs.get(i).getY();
                float delta1 = Math.abs(theLat - (float) m_lat) + Math.abs(theLong - (float) m_long);
                if (delta1 < delta && delta1 < K) {
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
                        DataUtil.addTapeToDB(DataUtil.getRealPathFromUriForAudio(MainInterface.this, uri), ic, POIs.get(theNum).getPoic(), simpleDateFormat.format(date));
                        updateMapPage(POIs.get(theNum).getPoic(), num);
                    }
                });
                dialog.setNegativeButton("创建新兴趣点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String POIC = "POI" + String.valueOf(time);
                        //List<POI> POIs = LitePal.where("ic = ?", ic).find(POI.class);
                        //List<POI> POIs = LitePal.findAll(POI.class);
                        DataUtil.addPOI(ic, POIC, "录音POI" + String.valueOf(POIs.size() + 1), (float) m_lat, (float) m_long, simpleDateFormat.format(date), num);
                        DataUtil.addTapeToDB(DataUtil.getRealPathFromUriForAudio(MainInterface.this, uri), ic, POIC, simpleDateFormat.format(date));
                        updateMapPage(POIC, num);
                    }
                });
                dialog.show();
            } else {
                String POIC = "POI" + String.valueOf(time);
                //List<POI> POIs = LitePal.where("ic = ?", ic).find(POI.class);
                //List<POI> POIs = LitePal.findAll(POI.class);
                DataUtil.addPOI(ic, POIC, "录音POI" + String.valueOf(POIs.size() + 1), (float) m_lat, (float) m_long, simpleDateFormat.format(date), num);
                DataUtil.addTapeToDB(DataUtil.getRealPathFromUriForAudio(this, uri), ic, POIC, simpleDateFormat.format(date));
                updateMapPage(POIC, num);
            }
        } else {
            String POIC = "POI" + String.valueOf(time);
            //List<POI> POIs = LitePal.where("ic = ?", ic).find(POI.class);
            //List<POI> POIs = LitePal.findAll(POI.class);
            DataUtil.addPOI(ic, POIC, "录音POI" + String.valueOf(POIs.size() + 1), (float) m_lat, (float) m_long, simpleDateFormat.format(date), num);
            DataUtil.addTapeToDB(DataUtil.getRealPathFromUriForAudio(MainInterface.this, uri), ic, POIC, simpleDateFormat.format(date));
            updateMapPage(POIC, num);
        }
    }

    private void AddTakePhoto(final String imageuri, final float[] latandlong, final int num) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MainInterface.this.getResources().getText(R.string.DateAndTime).toString());
        final Date date = new Date(System.currentTimeMillis());
        final long time = System.currentTimeMillis();
        final List<POI> POIs = LitePal.where("type = ?", strings[num]).find(POI.class);
        int size = POIs.size();
        if (size > 0) {
            float K = (float) 0.002;
            float delta = Math.abs(POIs.get(0).getX() - (float) m_lat) + Math.abs(POIs.get(0).getY() - (float) m_long);
            for (int i = 0; i < size; ++i) {
                float theLat = POIs.get(i).getX();
                float theLong = POIs.get(i).getY();
                float delta1 = Math.abs(theLat - (float) m_lat) + Math.abs(theLong - (float) m_long);
                if (delta1 < delta && delta1 < K) {
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
                        Date date = new Date(time);
                        DataUtil.addPhotoToDB(imageuri, ic, POIs.get(theNum).getPoic(), simpleDateFormat.format(date));
                        getNormalBitmap();
                        updateMapPage(POIs.get(theNum).getPoic(), num);
                    }
                });
                dialog.setNegativeButton("创建新兴趣点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //long time = System.currentTimeMillis();
                        String poic = "POI" + String.valueOf(time);
                        if (latandlong[0] != 0 && latandlong[1] != 0) {
                            DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), latandlong[0], latandlong[1], simpleDateFormat.format(date), num);
                        } else {
                            DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), (float) m_lat, (float) m_long, simpleDateFormat.format(date), num);
                        }
                        DataUtil.addPhotoToDB(imageuri, ic, poic, simpleDateFormat.format(date));
                        getNormalBitmap();
                        updateMapPage(poic, num);
                    }
                });
                dialog.show();
            } else {
                //List<POI> POIs = LitePal.findAll(POI.class);
                //long time = System.currentTimeMillis();
                String poic = "POI" + String.valueOf(time);
                if (latandlong[0] != 0 && latandlong[1] != 0) {
                    DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), latandlong[0], latandlong[1], simpleDateFormat.format(date), num);
                } else {
                    DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), (float) m_lat, (float) m_long, simpleDateFormat.format(date), num);
                }
                DataUtil.addPhotoToDB(imageuri, ic, poic, simpleDateFormat.format(date));
                getNormalBitmap();
                updateMapPage(poic, num);
            }
        } else {
            String poic = "POI" + String.valueOf(time);
            if (latandlong[0] != 0 && latandlong[1] != 0) {
                DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), latandlong[0], latandlong[1], simpleDateFormat.format(date), num);
            } else {
                DataUtil.addPOI(ic, poic, "图片POI" + String.valueOf(POIs.size() + 1), (float) m_lat, (float) m_long, simpleDateFormat.format(date), num);
            }
            DataUtil.addPhotoToDB(imageuri, ic, poic, simpleDateFormat.format(date));
            getNormalBitmap();
            updateMapPage(poic, num);
        }
    }

    private void updateMapPage(String poic, int num) {
        poiLayerBt.setChecked(true);
        showPOI = true;
        pdfView.resetZoomWithAnimation();
        /*Intent intent = new Intent(MainInterface.this, singlepoi.class);
        intent.putExtra("POIC", poic);
        startActivity(intent);*/
        Log.w(TAG, "updateMapPage: ");
        // TODO LM
        if (!esterEgg_lm) GoNormalSinglePOIPage(poic);
    }

    private void GoNormalSinglePOIPage(String poic) {
        Log.w(TAG, "updateMapPage: 0");
        if (!poic.isEmpty()) {
            Intent intent = new Intent(MainInterface.this, singlepoi.class);
            intent.putExtra("POIC", poic);
            intent.putExtra("type", 0);
            startActivity(intent);
        }
    }

    private void GoDMBZSinglePOIPage(String XH) {
        Log.w(TAG, "updateMapPage: 1");
        Intent intent = new Intent(MainInterface.this, singlepoi.class);
        intent.putExtra("DMBZ", XH);
        intent.putExtra("type", 1);
        startActivity(intent);
    }

    private void GoDMLSinglePOIPage(String MapId) {
        Log.w(TAG, "updateMapPage: 1");
        Intent intent = new Intent(MainInterface.this, singlepoi.class);
        intent.putExtra("DML", MapId);
        intent.putExtra("type", 2);
        startActivity(intent);
    }

    private void GoDMPSinglePOIPage(String MapId) {
        Log.w(TAG, "updateMapPage: 1");
        Intent intent = new Intent(MainInterface.this, singlepoi.class);
        intent.putExtra("DMP", MapId);
        intent.putExtra("type", 3);
        startActivity(intent);
    }

    /*private void GoNormalSinglePOIPage(String poic){
        Intent intent = new Intent(MainInterface.this, singlepoi.class);
        intent.putExtra("POIC", poic);
        startActivity(intent);
    }

    private void GoNormalSinglePOIPage(String poic){
        Intent intent = new Intent(MainInterface.this, singlepoi.class);
        intent.putExtra("POIC", poic);
        startActivity(intent);
    }*/

    private void showPopueWindowForPhoto() {
        View popView = View.inflate(this, R.layout.popupwindow_camera_need, null);
        Button bt_album = (Button) popView.findViewById(R.id.btn_pop_album);
        Button bt_camera = (Button) popView.findViewById(R.id.btn_pop_camera);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 1 / 3;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
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
                if (m_lat != 0) takePhoto();
                else
                    Toast.makeText(MainInterface.this, R.string.LocError, Toast.LENGTH_LONG).show();
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
        popupWindow.showAtLocation(popView, Gravity.BOTTOM, 0, 50);

    }

    private void showPopueWindowForMessure() {
        View popView = View.inflate(this, R.layout.popupwindow_messure, null);
        Button bt_distance = (Button) popView.findViewById(R.id.btn_pop_distance);
        final Button bt_area = (Button) popView.findViewById(R.id.btn_pop_area);
        Button bt_cancle = (Button) popView.findViewById(R.id.btn_pop_cancel);
        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 1 / 3;

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
                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                    toolbar.setTitle("正在测量(轨迹记录中)");
                } else toolbar.setTitle("正在测量");
                isQuery = false;
                isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                isLocate = 0;
                isLocateEnd = true;
                poiLayerBt.setChecked(false);
                showPOI = false;
                isMessure = true;
                isMessureType = TuzhiEnum.MESSURE_DISTANCE_TYPE;
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
                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                    toolbar.setTitle("正在测量(轨迹记录中)");
                } else toolbar.setTitle("正在测量");
                isQuery = false;
                isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                isLocate = 0;
                isLocateEnd = true;
                poiLayerBt.setChecked(false);
                showPOI = false;
                isMessure = true;
                isMessureType = TuzhiEnum.MESSURE_AREA_TYPE;
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
                locHere_fab.setVisibility(View.GONE);
            }
        });
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM, 0, 50);

    }
    PointF mLastPTForWhiteBlank = null;
    private void showPopueWindowForWhiteblank() {
        final View popView = View.inflate(this, R.layout.popupwindow_whiteblank, null);
        isWhiteBlank = true;
        FloatingActionButton back_pop = (FloatingActionButton) popView.findViewById(R.id.back_pop);
        back_pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Lines_WhiteBlank> lines_whiteBlanks = LitePal.where("ic = ?", ic).find(Lines_WhiteBlank.class);
                int size = lines_whiteBlanks.size();
                for (int kk = 0; kk < size; ++kk) {
                    Log.w(TAG, "onClick: " + lines_whiteBlanks.get(kk).getMmid());
                }
                int size1 = geometry_whiteBlanks.size();
                if (size <= 0) {
                    whiteBlankPt = "";
                    //LitePal.deleteAll(Lines_WhiteBlank.class, "ic = ?", ic);
                    geometry_whiteBlanks.clear();
                    pdfView.zoomWithAnimation(c_zoom);
                    Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.HasDeleteAllWhiteBlank), Toast.LENGTH_SHORT).show();
                } else {
                    whiteBlankPt = "";
                    Log.w(TAG, "onClick: " + LitePal.deleteAll(Lines_WhiteBlank.class, "mmid = ? and ic = ?", Integer.toString(size - 1), ic));
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
                List<Lines_WhiteBlank> lines_whiteBlanks = LitePal.where("ic = ?", ic).find(Lines_WhiteBlank.class);
                int size = lines_whiteBlanks.size();
                if (size <= 0) {
                    whiteBlankPt = "";
                    //LitePal.deleteAll(Lines_WhiteBlank.class, "ic = ?", ic);
                    geometry_whiteBlanks.clear();
                    pdfView.zoomWithAnimation(c_zoom);
                    Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.HasDeleteAllWhiteBlank), Toast.LENGTH_SHORT).show();
                } else {
                    whiteBlankPt = "";
                    LitePal.deleteAll(Lines_WhiteBlank.class, "ic = ?", ic);
                    geometry_whiteBlanks.clear();
                    pdfView.zoomWithAnimation(c_zoom);
                    Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.HasDeleteAllWhiteBlank), Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton popwhiteblank = (FloatingActionButton) popView.findViewById(R.id.whiteblank_pop);
        FrameLayout frameLayout = (FrameLayout) popView.findViewById(R.id.fml_pop);
        //获取屏幕宽高
        final int weight = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels - 60;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
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
                locHere_fab.setVisibility(View.VISIBLE);
            }
        });
        //popupWindow OnTouchListener
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
                        List<Lines_WhiteBlank> liness = LitePal.where("ic = ?", ic).find(Lines_WhiteBlank.class);
                        Log.w(TAG, "onTouch: " + liness.size());
                        int size = liness.size();
                        Lines_WhiteBlank lines = new Lines_WhiteBlank();
                        lines.setIc(ic);
                        lines.setColor(color_Whiteblank);
                        lines.setLines(whiteBlankPt);
                        lines.setMmid(size);
                        float[] spatialIndex = DataUtil.getSpatialIndex(whiteBlankPt);
                        lines.setMaxlat(spatialIndex[0]);
                        lines.setMinlat(spatialIndex[1]);
                        lines.setMaxlng(spatialIndex[2]);
                        lines.setMinlng(spatialIndex[3]);
                        lines.save();
                        geometry_WhiteBlank geometry_whiteBlank = new geometry_WhiteBlank(ic, whiteBlankPt, color_Whiteblank, spatialIndex[0], spatialIndex[2], spatialIndex[1], spatialIndex[3]);
                        geometry_whiteBlanks.add(geometry_whiteBlank);
                        mLastPTForWhiteBlank = null;
                        break;
                }

                PointF pt = new PointF(event.getRawX(), event.getRawY());
                pt = getGeoLocFromPixL(pt);
                locError("RawX : " + pt.x + "; RawY : " + pt.y);
                //pt_last = pt_current;
                //pt_current = pt;
                //pdfView.zoomWithAnimation(c_zoom);
                /*if (event.getRawY() >= height - 100 && event.getRawY() <= height && event.getRawX() >= 50 && event.getRawX() <= 150){
                    popupWindow.dismiss();
                    whiteblank.setImageResource(R.drawable.ic_brush_black_24dp);
                }*/
                double mDis = -1;
                try {
                    if(mLastPTForWhiteBlank != null) {
                        mDis = LatLng.algorithm(pt.y, pt.x, mLastPTForWhiteBlank.y, mLastPTForWhiteBlank.x);

                        locError("RawX" + mDis);
                    }
                }
                catch (Exception e)
                {
                    locError("RawX" + e.toString());
                }
                //if (isWhiteBlank && ( mDis >= 0.2 || mDis == -1)) {//设置点位基础抽稀
                if (isWhiteBlank) {//不设置点位基础抽稀
                    //locError("RawX" + LatLng.algorithm(pt.y, pt.x, mLastPTForWhiteBlank.y, mLastPTForWhiteBlank.x));
                    mLastPTForWhiteBlank = pt;
                    num_whiteBlankPt++;
                    if (num_whiteBlankPt == 1) {
                        whiteBlankPt = Float.toString(pt.x) + " " + Float.toString(pt.y);
                    } else if (num_whiteBlankPt == 2) {
                        whiteBlankPt = whiteBlankPt + " " + Float.toString(pt.x) + " " + Float.toString(pt.y);
                        //setTitle("正在测量");
                        pdfView.zoomWithAnimation(c_zoom);
                        //locError(Double.toString(algorithm(poi111.y, poi111.x, poi222.y, poi222.x)));
                        //Toast.makeText(MainInterface.this, "距离为" + Double.toString(distanceSum) + "米", Toast.LENGTH_LONG).show();
                    } else {
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
        popupWindow.showAtLocation(popView, Gravity.NO_GRAVITY, 0, 0);
        popwhiteblank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                whiteBlank_fab.setVisibility(View.VISIBLE);
                isOpenWhiteBlank = false;
                whiteBlank_fab.setImageResource(R.drawable.ic_brush_black_24dp);
                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                    toolbar.setTitle("轨迹记录中");
                } else toolbar.setTitle(pdfFileName);
            }
        });
    }

    private void controlSpecificFunction(String query) {
        // TODO EASTER
        SharedPreferences.Editor editor;
        switch (query){
            case "kqbz":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_plq", true);
                editor.apply();
                esterEgg_plq = true;
                getEsterEgg_plq();
                pdfView.zoomWithAnimation(c_zoom);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggOpenInfo), Toast.LENGTH_LONG).show();
                break;
            case "gbbz":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_plq", false);
                editor.apply();
                esterEgg_plq = false;
                pdfView.zoomWithAnimation(c_zoom);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggCloseInfo), Toast.LENGTH_LONG).show();
                break;
            case "kqhx":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_redline", true);
                editor.apply();
                esterEgg_redline = true;
                getEsterEgg_redline();
                pdfView.zoomWithAnimation(c_zoom);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggOpenInfo), Toast.LENGTH_LONG).show();
                break;
            case "gbhx":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_redline", false);
                editor.apply();
                esterEgg_redline = false;
                pdfView.zoomWithAnimation(c_zoom);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggCloseInfo), Toast.LENGTH_LONG).show();
                break;
            case "kqlm":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_lm", true);
                editor.apply();
                esterEgg_lm = true;
                dmbzList = LitePal.findAll(DMBZ.class);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggOpenInfo), Toast.LENGTH_LONG).show();
                break;
            case "gblm":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_lm", false);
                editor.apply();
                esterEgg_lm = false;
                dmbzList.clear();
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggCloseInfo), Toast.LENGTH_LONG).show();
                break;
            case "kqdm":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_dm", true);
                editor.apply();
                esterEgg_dm = true;
                getEsterEgg_dm();
                pdfView.zoomWithAnimation(c_zoom);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggOpenInfo), Toast.LENGTH_LONG).show();
                break;
            case "gbdm":
                editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_dm", false);
                editor.apply();
                esterEgg_dm = false;
                pdfView.zoomWithAnimation(c_zoom);
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggCloseInfo), Toast.LENGTH_LONG).show();
                break;
        }

    }

    public void showListPopupWindow(View view, String query) {
        final ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        query = query.trim();
        controlSpecificFunction(query);
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
        if (esterEgg_plq) {
            String sql1 = "select * from kmltest where";
            String[] strings1 = query.split(" ");
            for (int i = 0; i < strings1.length; ++i) {
                if (i == 0) sql1 = sql1 + " dmbzmc LIKE '%" + strings1[i] + "%'";
                else sql1 = sql1 + " AND dmbzmc LIKE '%" + strings1[i] + "%'";
            }
            Cursor cursor1 = LitePal.findBySQL(sql1);
            if (cursor1.moveToFirst()) {
                do {
                    String xh = cursor1.getString(cursor1.getColumnIndex("xh"));
                    String dmbzmc = cursor1.getString(cursor1.getColumnIndex("dmbzmc"));
                    mPOIobj mPOIobj = new mPOIobj(xh, dmbzmc);
                    pois.add(mPOIobj);
                } while (cursor1.moveToNext());
            }
            cursor1.close();
        }
        if (esterEgg_lm) {
            String sql1 = "select * from DMBZ where";
            String[] strings1 = query.split(" ");
            for (int i = 0; i < strings1.length; ++i) {
                if (i == 0) sql1 = sql1 + " bzmc LIKE '%" + strings1[i] + "%'";
                else sql1 = sql1 + " AND bzmc LIKE '%" + strings1[i] + "%'";
            }
            Cursor cursor1 = LitePal.findBySQL(sql1);
            if (cursor1.moveToFirst()) {
                do {
                    String xh = "DMBZ" + cursor1.getString(cursor1.getColumnIndex("xh"));
                    String dmbzmc = cursor1.getString(cursor1.getColumnIndex("bzmc"));
                    mPOIobj mPOIobj = new mPOIobj(xh, dmbzmc);
                    pois.add(mPOIobj);
                } while (cursor1.moveToNext());
            }
            cursor1.close();
        }
        if (esterEgg_dm) {
            String sql1 = "select * from DMLine where";
            String[] strings1 = query.split(" ");
            for (int i = 0; i < strings1.length; ++i) {
                if (i == 0) sql1 = sql1 + " bzmc LIKE '%" + strings1[i] + "%'";
                else sql1 = sql1 + " AND bzmc LIKE '%" + strings1[i] + "%'";
            }
            Cursor cursor1 = LitePal.findBySQL(sql1);
            if (cursor1.moveToFirst()) {
                do {
                    String xh = "DMLine" + cursor1.getString(cursor1.getColumnIndex("mapid"));
                    String dmbzmc = cursor1.getString(cursor1.getColumnIndex("bzmc"));
                    mPOIobj mPOIobj = new mPOIobj(xh, dmbzmc);
                    pois.add(mPOIobj);
                } while (cursor1.moveToNext());
            }
            cursor1.close();

            String sql2 = "select * from DMPoint where";
            String[] strings2 = query.split(" ");
            for (int i = 0; i < strings2.length; ++i) {
                if (i == 0) sql2 = sql2 + " bzmc LIKE '%" + strings2[i] + "%'";
                else sql2 = sql2 + " AND bzmc LIKE '%" + strings2[i] + "%'";
            }
            Cursor cursor2 = LitePal.findBySQL(sql2);
            if (cursor2.moveToFirst()) {
                do {
                    String xh = "DMPoint" + cursor2.getString(cursor2.getColumnIndex("mapid"));
                    String dmbzmc = cursor2.getString(cursor2.getColumnIndex("bzmc"));
                    mPOIobj mPOIobj = new mPOIobj(xh, dmbzmc);
                    pois.add(mPOIobj);
                } while (cursor2.moveToNext());
            }
            cursor2.close();
        }
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
                    Intent intent = new Intent(MainInterface.this, singlepoi.class);
                    intent.putExtra("POIC", pois.get(position).getM_POIC());
                    MainInterface.this.startActivity(intent);
                } else if (esterEgg_plq && !esterEgg_lm) {
                    Intent intent = new Intent(MainInterface.this, plqpoishow.class);
                    intent.putExtra("xh", pois.get(position).getM_POIC());
                    MainInterface.this.startActivity(intent);
                } else if (pois.get(position).getM_POIC().contains("DMBZ"))
                    GoDMBZSinglePOIPage(pois.get(position).getM_POIC().replace("DMBZ", ""));
                else if (pois.get(position).getM_POIC().contains("DMLine"))
                    GoDMLSinglePOIPage(pois.get(position).getM_POIC().replace("DMLine", ""));
                else if (pois.get(position).getM_POIC().contains("DMPoint"))
                    GoDMPSinglePOIPage(pois.get(position).getM_POIC().replace("DMPoint", ""));

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
        locError(Float.toString(cs_top) + "%" + Float.toString(cs_bottom) + "%" + Float.toString(cs_left) + "%" + Float.toString(cs_right));
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

    //屏幕坐标位置到经纬度转化
    private PointF getGeoLocFromPixL0(PointF pt) {
        //textView = (TextView) findViewById(R.id.txt);
        DecimalFormat df = new DecimalFormat("0.0000");
        //精确定位算法
        float xxxx, yyyy;
        if (current_pageheight < viewer_height || current_pagewidth < viewer_width) {
            xxxx = ((pt.x - (screen_width - viewer_width + k_w)));
            yyyy = ((pt.y - (screen_height - viewer_height + k_h)));
            if (pt.y >= (screen_height - viewer_height + k_h) && pt.y <= (screen_height - viewer_height + k_h + current_pageheight) && pt.x >= (screen_width - viewer_width + k_w) && pt.x <= (screen_width - viewer_width + k_w + current_pagewidth)) {
                pt.x = (float) (max_lat - (yyyy) / current_pageheight * (max_lat - min_lat));
                pt.y = (float) ((xxxx) / current_pagewidth * (max_long - min_long) + min_long);
                locLatForTap = pt.x;
                locLongForTap = pt.y;
                showLocationText();
            } else {
                textView.setText("点击位置在区域之外");
            }
        } else {
            xxxx = pt.x - (screen_width - viewer_width);
            yyyy = pt.y - (screen_height - viewer_height);
            pt.x = (float) (max_lat - (yyyy - pdfView.getCurrentYOffset()) / current_pageheight * (max_lat - min_lat));
            pt.y = (float) ((xxxx - pdfView.getCurrentXOffset()) / current_pagewidth * (max_long - min_long) + min_long);
            locLatForTap = pt.x;
            locLongForTap = pt.y;
            showLocationText();
        }
        return pt;
        //
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

    //判断该像素点位置是否在地图区域内
    public static int verifyPixL(PointF pt, float k_w, float k_h, float screen_width, float screen_height, float viewer_width, float viewer_height, float current_pagewidth, float current_pageheight) {
        final int OUT = -1;
        final int IN_FULL = 1;
        final int IN_NOTFULL = 2;
        if (current_pageheight < viewer_height || current_pagewidth < viewer_width) {
            if (pt.y >= (screen_height - viewer_height + k_h) && pt.y <= (screen_height - viewer_height + k_h + current_pageheight) && pt.x >= (screen_width - viewer_width + k_w) && pt.x <= (screen_width - viewer_width + k_w + current_pagewidth))
                return IN_NOTFULL;
            else return OUT;
        } else return IN_FULL;
    }

    //屏幕坐标位置到经纬度转化1
    private PointF getGeoLocFromPixLForFull(PointF pt) {
        //精确定位算法
        float xxxx, yyyy;
        xxxx = pt.x - (screen_width - viewer_width);
        yyyy = pt.y - (screen_height - viewer_height);
        pt.x = (float) (max_lat - (yyyy - pdfView.getCurrentYOffset()) / current_pageheight * (max_lat - min_lat));
        pt.y = (float) ((xxxx - pdfView.getCurrentXOffset()) / current_pagewidth * (max_long - min_long) + min_long);
        locLatForTap = pt.x;
        locLongForTap = pt.y;
        return pt;
        //
    }

    //屏幕坐标位置到经纬度转化2
    private PointF getGeoLocFromPixLForNotFull(PointF pt) {
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

    public void locError(String str) {
        Log.e(TAG, "debug: " + str);
    }

    //获取当前坐标位置
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

    //退出提醒弹窗
    public void popBackWindow(String str) {
        if (str.equals("Destroy")) {
            AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
            backAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                    isDrawTrail = TuzhiEnum.NONE_DRAW_TYPE;
                    isLocateEnd = true;
                    recordTrail(last_x, last_y);
                    locError(m_cTrail);
                    invalidateOptionsMenu();
                    Intent stop_mService = new Intent(MainInterface.this, RecordTrail.class);
                    stopService(stop_mService);
                    /*Trail trail = new Trail();
                    List<Trail> trails = LitePal.where("ic = ?", ic).find(Trail.class);
                    trail.setIc(ic);
                    trail.setName("路径" + Integer.toString(trails.size() + 1));
                    trail.setPath(m_cTrail);
                    trail.save();*/
                    trails = LitePal.findAll(Trail.class);
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
        } else {

            AlertDialog.Builder backAlert = new AlertDialog.Builder(this);
            backAlert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent stop_mService = new Intent(MainInterface.this, RecordTrail.class);
                    stopService(stop_mService);
                    trails = LitePal.findAll(Trail.class);
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
        }
    }

    private boolean getEsterEgg_plq() {
        if (esterEgg_plq) {
            kmltests = LitePal.findAll(kmltest.class);
            File file1 = new File(Environment.getExternalStorageDirectory() + "/doc.kml");
            File file2 = new File(Environment.getExternalStorageDirectory() + "/地名标志录音");
            File file3 = new File(Environment.getExternalStorageDirectory() + "/地名标志照片");
            if (kmltests.size() == 0 && file1.exists() && file2.exists() && file3.exists() && file2.isDirectory() && file3.isDirectory()) {
                LitePal.deleteAll(plqzp.class);
                LitePal.deleteAll(plqyp.class);
                LitePal.deleteAll(kmltest.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DataUtil.getKML(Environment.getExternalStorageDirectory() + "/doc.kml");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggOpenInfo), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
                kmltests = LitePal.findAll(kmltest.class);
                return true;
            } else if (kmltests.size() == 0) {
                Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggErrorInfo), Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                editor.putBoolean("open_plq", false);
                editor.apply();
                return false;
            } else if (kmltests.size() != 0 && file1.exists() && file2.exists() && file3.exists() && file2.isDirectory() && file3.isDirectory()) {
                getBitmap1();
                return true;
            }
            SharedPreferences.Editor editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
            editor.putBoolean("open_plq", false);
            editor.apply();
            return false;
        }
        return false;
    }

    private boolean getEsterEgg_redline() {
        if (esterEgg_redline) {
            patchsForLatLng = new ArrayList<>();
            patchsForPix = new ArrayList<>();
            latLngs_1 = new ArrayList<>();
            latLngs_2 = new ArrayList<>();
            latLngs_3 = new ArrayList<>();
            latLngs_1.add(new LatLng((float) 25.0609, (float) 102.7469));
            latLngs_1.add(new LatLng((float) 25.0358, (float) 102.7469));
            latLngs_1.add(new LatLng((float) 25.0131, (float) 102.7273));
            latLngs_1.add(new LatLng((float) 25.0364, (float) 102.7122));
            latLngs_1.add(new LatLng((float) 25.0605, (float) 102.6986));
            patchsForLatLng.add(latLngs_1);
            latLngs_2.add(new LatLng((float) 25.0721, (float) 102.7222));
            latLngs_2.add(new LatLng((float) 25.0793, (float) 102.7181));
            latLngs_2.add(new LatLng((float) 25.0885, (float) 102.7189));
            latLngs_2.add(new LatLng((float) 25.1072, (float) 102.7418));
            latLngs_2.add(new LatLng((float) 25.0968, (float) 102.7558));
            patchsForLatLng.add(latLngs_2);
            latLngs_3.add(new LatLng((float) 25.0460, (float) 102.6662));
            latLngs_3.add(new LatLng((float) 25.0519, (float) 102.6591));
            latLngs_3.add(new LatLng((float) 25.0667, (float) 102.6549));
            latLngs_3.add(new LatLng((float) 25.0756, (float) 102.6627));
            latLngs_3.add(new LatLng((float) 25.0676, (float) 102.6858));
            patchsForLatLng.add(latLngs_3);
            Log.w(TAG, "onCreatesize: " + patchsForLatLng.size());
            latLngs1_1 = new ArrayList<>();
            latLngs1_2 = new ArrayList<>();
            latLngs1_3 = new ArrayList<>();
            return true;
        }
        return false;
    }

    private boolean getEsterEgg_dm() {
        if (esterEgg_dm) {
            dmLines = LitePal.findAll(DMLine.class);
            dmPoints = LitePal.findAll(DMPoint.class);
            File file1 = new File(Environment.getExternalStorageDirectory() + "/20180716/联盟街道点状地名/doc.kml");
            File file2 = new File(Environment.getExternalStorageDirectory() + "/20180716/联盟街道线状地名/doc.kml");
            File file3 = new File(Environment.getExternalStorageDirectory() + "/20180716/地名信息连接关系.txt");
            File file4 = new File(Environment.getExternalStorageDirectory() + "/20180716/地名信息.txt");
            File file5 = new File(Environment.getExternalStorageDirectory() + "/20180716/联盟街道点状地名");
            File file6 = new File(Environment.getExternalStorageDirectory() + "/20180716/联盟街道线状地名");
            File file7 = new File(Environment.getExternalStorageDirectory() + "/20180716");
            if (dmPoints.size() == 0) {
                if (file1.exists() && file2.exists() && file3.exists() && file4.exists() && file5.isDirectory() && file6.isDirectory() && file7.isDirectory()) {
                    LitePal.deleteAll(DMLine.class);
                    LitePal.deleteAll(DMPoint.class);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DataUtil.getDM("/20180716/联盟街道点状地名/doc.kml", "/20180716/联盟街道线状地名/doc.kml", "/20180716/地名信息连接关系.txt", "/20180716/地名信息.txt");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggOpenInfo), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).start();
                    dmLines = LitePal.findAll(DMLine.class);
                    dmPoints = LitePal.findAll(DMPoint.class);
                    return true;
                } else {
                    Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.EasterEggErrorInfo), Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
                    editor.putBoolean("open_dm", false);
                    editor.apply();
                    return false;
                }
            } else if (dmPoints.size() != 0 && file1.exists() && file2.exists() && file3.exists() && file4.exists() && file5.isDirectory() && file6.isDirectory() && file7.isDirectory()) {
                getDMitmap();
                return true;
            }
            SharedPreferences.Editor editor = getSharedPreferences("easter_egg", MODE_PRIVATE).edit();
            editor.putBoolean("open_dm", false);
            editor.apply();
            return false;
        }
        return false;
    }

    private void updateDBFormOldToNow() {
        String[] types = getResources().getStringArray(R.array.Type);
        updateDBForTypeInit(types);
        updateDBForTypeChange(types);
    }

    private void updateDBForTypeChange(final String[] types) {
        POI poi = new POI();
        poi.setType(types[0]);
        poi.updateAll("type = ?", "地名");
        poi.setType(types[1]);
        poi.updateAll("type = ?", "地名标志");
        poi.setType(types[2]);
        poi.updateAll("type = ?", "门牌管理");
    }

    private void updateDBForTypeInit(final String[] types) {
        List<POI> poiList = LitePal.findAll(POI.class);
        int sssizee = poiList.size();
        for (int i = 0; i < sssizee; ++i) {
            if (poiList.get(i).getType() == null || poiList.get(i).getType().isEmpty()) {
                POI poi = new POI();
                poi.setType(types[0]);
                poi.updateAll("poic = ?", poiList.get(i).getPoic());
            }
        }
    }

    private void initCheckbox() {
        trailLayerBt = (CheckBox) findViewById(R.id.trailLayer);
        whiteBlankLayerBt = (CheckBox) findViewById(R.id.whiteBlankLayer);
        centerPointModeBt = (CheckBox) findViewById(R.id.centerPointMode);
        poiLayerBt = (CheckBox) findViewById(R.id.poiLayer);
        type1_checkbox = (CheckBox) findViewById(R.id.type1_poiLayer);
        type2_checkbox = (CheckBox) findViewById(R.id.type2_poiLayer);
        type3_checkbox = (CheckBox) findViewById(R.id.type3_poiLayer);
        trailLayerBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showTrail = true;
                    pdfView.zoomWithAnimation(c_zoom);
                } else {
                    showTrail = false;
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        whiteBlankLayerBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isWhiteBlank = true;
                    pdfView.zoomWithAnimation(c_zoom);
                } else {
                    isWhiteBlank = false;
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        centerPointModeBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showMode = TuzhiEnum.CENTERMODE;
                    centerPoint.setVisibility(View.VISIBLE);
                    isQuery = true;
                    isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                    locError("中心点模式");
                    isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
                    query_poi_imgbt.setVisibility(View.INVISIBLE);
                } else {
                    locError("不是中心点模式");
                    showMode = TuzhiEnum.NOCENTERMODE;
                    centerPoint.setVisibility(View.INVISIBLE);
                    isQuery = false;
                    query_poi_imgbt.setVisibility(View.VISIBLE);
                }
            }
        });
        //图层控制按钮初始化
        poiLayerBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        if (!type1Checked && !type2Checked && !type3Checked) {
                            type1_checkbox.setChecked(true);
                            type2_checkbox.setChecked(true);
                            type3_checkbox.setChecked(true);
                            showPOI = true;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, e.toString());
                    }
                } else {
                    try {
                        type1_checkbox.setChecked(false);
                        type2_checkbox.setChecked(false);
                        type3_checkbox.setChecked(false);
                        showPOI = false;
                    } catch (Exception e) {
                        Log.w(TAG, e.toString());
                    }
                }
            }
        });
        type1_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    Log.w(TAG, "onCheckedChanged 1 : " + isChecked);
                    if (isChecked) {
                        type1Checked = true;
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                    } else {
                        type1Checked = false;
                        if (!type1Checked && !type2Checked && !type3Checked) {
                            poiLayerBt.setChecked(false);
                            showPOI = false;
                        }
                    }
                    pdfView.zoomWithAnimation(c_zoom);
                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
            }
        });
        type2_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    Log.w(TAG, "onCheckedChanged 2 : " + isChecked);
                    if (isChecked) {
                        type2Checked = true;
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                    } else {
                        type2Checked = false;
                        if (!type1Checked && !type2Checked && !type3Checked){
                            poiLayerBt.setChecked(false);
                            showPOI = false;
                        }
                    }
                    pdfView.zoomWithAnimation(c_zoom);
                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
            }
        });
        type3_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    Log.w(TAG, "onCheckedChanged 3 : " + isChecked);
                    if (isChecked) {
                        type3Checked = true;
                        poiLayerBt.setChecked(true);
                        showPOI = true;
                    } else {
                        type3Checked = false;
                        if (!type1Checked && !type2Checked && !type3Checked){
                            poiLayerBt.setChecked(false);
                            showPOI = false;
                        }
                    }
                    pdfView.zoomWithAnimation(c_zoom);
                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
            }
        });
        //
        type1_checkbox.setText(strings[0]);
        type2_checkbox.setText(strings[1]);
        type3_checkbox.setText(strings[2]);
    }

    private void initEsterEgg() {
        SharedPreferences pref = getSharedPreferences("easter_egg", MODE_PRIVATE);
        esterEgg_plq = pref.getBoolean("open_plq", false);
        esterEgg_redline = pref.getBoolean("open_redline", false);
        getEsterEgg_plq();
        getEsterEgg_redline();
        esterEgg_lm = pref.getBoolean("open_lm", false);
        if (esterEgg_lm) {
            getDMBZBitmap();
        }
        esterEgg_dm = pref.getBoolean("open_dm", false);
        getEsterEgg_dm();
    }

    private void showLeaflets() {
        forbiddenWidget();
        FILE_TYPE = TuzhiEnum.ASSET_FILE_TYPE;
        displayFromAsset("图志简介");
    }

    private void initVariable(final int m_num) {
        initMapVariable(m_num);
        initOtherVariable();
    }

    private int receiveInfo() {
        final Intent intent = getIntent();
        return intent.getIntExtra("num", 0);
    }

    private void initMapVariable(final int m_num) {
        getInfo(m_num);
        manageInfo();
        num_map1 = m_num;
        File m_file = new File(uri);
        if (uri != null && !uri.isEmpty() && m_file.exists()) {
            FILE_TYPE = TuzhiEnum.FILE_FILE_TYPE;
        }
    }

    private void doSpecificOperation() {
        /*LitePal.deleteAll(kmltest.class);
        LitePal.deleteAll(plqzp.class);
        LitePal.deleteAll(plqyp.class);*/
        //LitePal.deleteAll(DMBZ.class);
        //DataUtil.getKML1(Environment.getExternalStorageDirectory().toString() + "/doc.kml");
        //LitePal.deleteAll(DMBZ.class);
        Sampler.getInstance().init(MainInterface.this, 100);
        Sampler.getInstance().start();
        updateDBFormOldToNow();//将老数据库内容转换为array所存的类型
        initEsterEgg();//初始化当前彩蛋
        //LitePal.deleteAll(IconDataset.class);
        initIconBitmap(addIconDataset());//进行武警DEMO的图例缓存
        /*
        PointCollection points = new PointCollection(SpatialReferences.getWgs84());
        for (int i = 0; i < 10; ++i){
            com.esri.arcgisruntime.geometry.Point point = new com.esri.arcgisruntime.geometry.Point(i, i, SpatialReferences.getWgs84());
            points.add(point);
        }
        Polyline polyline = new Polyline(points);
        Log.w(TAG, "doSpecificOperation: " + polyline.toJson());
        Log.w(TAG, "doSpecificOperation: " + polyline.toString());
        Polygon polygon = GeometryEngine.buffer(polyline, 1);
        int i = 0;
        for (Iterator<com.esri.arcgisruntime.geometry.Point> iter = polygon.toPolyline().getParts().getPartsAsPoints().iterator(); iter.hasNext();) {
            ++i;
            Log.w(TAG, "doSpecificOperation: " + iter.next());
        }
        Log.w(TAG, "doSpecificOperation: " + i);
        */

    }

    /*
     *  用于缓存武警DEMO的图例数据
     *  访问根目录下的./原图 文件夹
     *
     *  如果IconDataset数据库中没有数据且该文件夹存在
     *  就进行图例缓存
     *
     *  @author 李正洋
     *
     *  @see    com.geopdfviewer.android.IconDataset
     *
     *  @since  1.6
     */
    private List<IconDataset> addIconDataset() {
        List<IconDataset> iconDatasets1 = LitePal.findAll(IconDataset.class);
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/原图");
        if (file.isDirectory() && iconDatasets1.size() == 0) {
            File[] subFiles = file.listFiles();
            for (int i = 0; i < subFiles.length; ++i) {
                String name = subFiles[i].getName();
                IconDataset iconDataset = new IconDataset();
                iconDataset.setName(name.substring(0, name.lastIndexOf(".")));
                iconDataset.setPath(subFiles[i].getAbsolutePath());
                iconDataset.save();
            }
            List<IconDataset> iconDatasets = LitePal.findAll(IconDataset.class);
            for (int i = 0; i < iconDatasets.size(); ++i) {
                Log.w(TAG, "addIconDataset: " + iconDatasets.get(i).getName());
            }
            return iconDatasets;
        } else return iconDatasets1;
    }

    private void showPopueWindowForIcon() {
        final View popView = View.inflate(this, R.layout.popupwindow_iconchoose, null);
        RecyclerView recyclerView1 = (RecyclerView) popView.findViewById(R.id.iconchoose_recycler_view);
        GridLayoutManager layoutManager1 = new GridLayoutManager(popView.getContext(), 1);
        layoutManager1.setOrientation(OrientationHelper.HORIZONTAL);
        recyclerView1.setLayoutManager(layoutManager1);
        //xzqTreeAdapter adapter1 = new xzqTreeAdapter(DataUtil.bubbleSort(LitePal.findAll(xzq.class)));
        List<IconDataset> iconDatasets = LitePal.findAll(IconDataset.class);
        final IconDatasetAdapter adapter1 = new IconDatasetAdapter(iconDatasets);
        //获取屏幕宽高
        final int weight = getResources().getDisplayMetrics().widthPixels;
        //final int height = getResources().getDisplayMetrics().heightPixels;
        final int height = 300;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        adapter1.setOnItemClickListener(new IconDatasetAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String iconPath, int position) {
                MPOI mpoi = new MPOI();
                mpoi.setImgPath(iconPath);
                mpoi.updateAll("num = ?", Long.toString(QueriedIconPoiNum));
                if (WidthAndHeight != 80) {
                    Bitmap bitmap = DataUtil.getImageThumbnail(iconPath, WidthAndHeight, WidthAndHeight);
                    IconBitmaps.add(new bt(bitmap, iconPath + "," + Integer.toString(WidthAndHeight)));
                }
                popupWindow.dismiss();
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        recyclerView1.setAdapter(adapter1);
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

        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.TOP, 0, 400);
    }

    private void showPopueWindowForIconAdd(final PointF pt1) {
        final View popView = View.inflate(this, R.layout.popupwindow_iconchoose, null);
        RecyclerView recyclerView1 = (RecyclerView) popView.findViewById(R.id.iconchoose_recycler_view);
        GridLayoutManager layoutManager1 = new GridLayoutManager(popView.getContext(), 1);
        layoutManager1.setOrientation(OrientationHelper.HORIZONTAL);
        recyclerView1.setLayoutManager(layoutManager1);
        //xzqTreeAdapter adapter1 = new xzqTreeAdapter(DataUtil.bubbleSort(LitePal.findAll(xzq.class)));
        List<IconDataset> iconDatasets = LitePal.findAll(IconDataset.class);
        final IconDatasetAdapter adapter1 = new IconDatasetAdapter(iconDatasets);
        //获取屏幕宽高
        final int weight = getResources().getDisplayMetrics().widthPixels;
        //final int height = getResources().getDisplayMetrics().heightPixels;
        final int height = 300;

        final PopupWindow popupWindow = new PopupWindow(popView, weight, height);
        adapter1.setOnItemClickListener(new IconDatasetAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String iconPath, int position) {
                MPOI poi = new MPOI();
                poi.setNum(System.currentTimeMillis());
                poi.setImgPath(iconPath);
                if (showMode == TuzhiEnum.NOCENTERMODE) {
                    poi.setLat(pt1.x);
                    poi.setLng(pt1.y);
                } else {
                    poi.setLat(centerPointLoc.x);
                    poi.setLng(centerPointLoc.y);
                }
                int[] whs = DataUtil.getWHForOrigin(iconPath, 80);
                poi.setHeight(whs[1]);
                poi.setWidth(whs[0]);
                Log.w(TAG, "onItemClick: " + whs[0] + ";" + whs[1]);
                poi.save();
                popupWindow.dismiss();
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        recyclerView1.setAdapter(adapter1);
        //popupWindow.setAnimationStyle(R.style.anim_popup_dir);
        popupWindow.setFocusable(true);
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

        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.TOP, 0, 400);
    }

    private List<bt> IconBitmaps;//用于缓存所有图例的缩略图
    private int iconBitmapNum;//用于记录缩略图数目

    /*
     *  用于缓存武警DEMO的图例数据
     *
     *  根据图例原图地址逐张生成缩略图
     *  将缩略图缓存到IconBitmaps全局变量中
     *
     *  将MPOI数据库中的数据也逐张生成缩略图
     *  将缩略图缓存到IconBitmaps全局变量中
     *
     *  获取IconBitmaps缓存的缩略图数目
     *  存放到iconBitmapNum全局变量中
     *
     *  @author 李正洋
     *
     *  @param  iconDatasets    所有图例的地址缓存List
     *
     *  @see    com.geopdfviewer.android.IconDataset
     *  @since  1.6
     */
    private void initIconBitmap(final List<IconDataset> iconDatasets) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IconBitmaps = new ArrayList<>();
                for (int i = 0; i < iconDatasets.size(); ++i) {
                    String path = iconDatasets.get(i).getPath();
                    //Bitmap bitmap = DataUtil.getImageThumbnail(path, 80, 80);
                    Bitmap bitmap = DataUtil.getImageThumbnailForOrigin(path, 80);
                    IconBitmaps.add(new bt(bitmap, path));
                }
                List<MPOI> mpois = LitePal.findAll(MPOI.class);
                for (int i = 0; i < mpois.size(); ++i) {
                    //if (mpois.get(i).getWidth() != 80 && mpois.get(i).getImgPath() != null) {
                    if (mpois.get(i).getHeight() != 80 && mpois.get(i).getImgPath() != null) {
                        String path = mpois.get(i).getImgPath();
                        //Bitmap bitmap = DataUtil.getImageThumbnail(path, (int) mpois.get(i).getWidth(), (int) mpois.get(i).getHeight());
                        //IconBitmaps.add(new bt(bitmap, path + "," + Integer.toString((int) mpois.get(i).getWidth())));
                        Bitmap bitmap = DataUtil.getImageThumbnailForOrigin(path, (int) mpois.get(i).getHeight());
                        IconBitmaps.add(new bt(bitmap, path + "," + Integer.toString((int) mpois.get(i).getHeight())));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "run: " + IconBitmaps.size());
                        iconBitmapNum = IconBitmaps.size();
                        //pdfView.zoomWithAnimation(c_zoom);
                    }
                });
            }
        }).start();
    }

    private void initIconBitmap1(final List<IconDataset> iconDatasets) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                IconBitmaps = new ArrayList<>();
                for (int i = 0; i < iconDatasets.size(); ++i) {
                    String path = iconDatasets.get(i).getPath();
                    //Bitmap bitmap = DataUtil.getImageThumbnail(path, 80, 80);
                    Bitmap bitmap = DataUtil.getImageThumbnailForOrigin(path, 80);
                    IconBitmaps.add(new bt(bitmap, path));
                }
                List<MPOI> mpois = LitePal.findAll(MPOI.class);
                for (int i = 0; i < mpois.size(); ++i) {
                    //if (mpois.get(i).getWidth() != 80 && mpois.get(i).getImgPath() != null) {
                    if (mpois.get(i).getHeight() != 80 && mpois.get(i).getImgPath() != null) {
                        String path = mpois.get(i).getImgPath();
                        //Bitmap bitmap = DataUtil.getImageThumbnail(path, (int) mpois.get(i).getWidth(), (int) mpois.get(i).getHeight());
                        //IconBitmaps.add(new bt(bitmap, path + "," + Integer.toString((int) mpois.get(i).getWidth())));
                        Bitmap bitmap = DataUtil.getImageThumbnailForOrigin(path, (int) mpois.get(i).getHeight());
                        IconBitmaps.add(new bt(bitmap, path + "," + Integer.toString((int) mpois.get(i).getHeight())));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "run: " + IconBitmaps.size());
                        iconBitmapNum = IconBitmaps.size();
                        pdfView.zoomWithAnimation(c_zoom);
                    }
                });
            }
        }).start();
    }

    private void bufferIconBitmapPlus1(final MPOI mpoi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: " + mpoi.getImgPath());
                Bitmap bitmap1 = DataUtil.getImageThumbnail(mpoi.getImgPath(), WidthAndHeight + 5, WidthAndHeight + 5);
                IconBitmaps.add(new bt(bitmap1, mpoi.getImgPath() + "," + Integer.toString(WidthAndHeight + 5)));
                Log.w(TAG, "run: " + IconBitmaps.get(IconBitmaps.size() - 1).getM_path());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void bufferIconBitmapMinus1(final MPOI mpoi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = DataUtil.getImageThumbnail(mpoi.getImgPath(), WidthAndHeight - 5, WidthAndHeight - 5);
                IconBitmaps.add(new bt(bitmap, mpoi.getImgPath() + "," + Integer.toString(WidthAndHeight - 5)));
                Log.w(TAG, "run: " + IconBitmaps.get(IconBitmaps.size() - 1).getM_path());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void bufferIconBitmapPlus(final MPOI mpoi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "run: " + mpoi.getImgPath());
                Bitmap bitmap1 = DataUtil.getImageThumbnailForOrigin(mpoi.getImgPath(), IconHeight + 5);
                IconBitmaps.add(new bt(bitmap1, mpoi.getImgPath() + "," + Integer.toString(IconHeight + 5)));
                Log.w(TAG, "run: " + IconBitmaps.get(IconBitmaps.size() - 1).getM_path());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void bufferIconBitmapMinus(final MPOI mpoi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = DataUtil.getImageThumbnailForOrigin(mpoi.getImgPath(), IconHeight - 5);
                IconBitmaps.add(new bt(bitmap, mpoi.getImgPath() + "," + Integer.toString(IconHeight - 5)));
                Log.w(TAG, "run: " + IconBitmaps.get(IconBitmaps.size() - 1).getM_path());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void removeBufferIconBitmapForProgress1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < IconBitmaps.size(); ++i) {
                    if (i >= iconBitmapNum && IconBitmaps.get(i).getM_path().contains(",") && (!IconBitmaps.get(i).getM_path().contains(Integer.toString(WidthAndHeight - 5)) && !IconBitmaps.get(i).getM_path().contains(Integer.toString(WidthAndHeight + 5)) && !IconBitmaps.get(i).getM_path().contains(Integer.toString(WidthAndHeight)))) {
                        IconBitmaps.remove(i);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void removeBufferIconBitmapForProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < IconBitmaps.size(); ++i) {
                    if (i >= iconBitmapNum && IconBitmaps.get(i).getM_path().contains(",") && (!IconBitmaps.get(i).getM_path().contains(Integer.toString(IconHeight - 5)) && !IconBitmaps.get(i).getM_path().contains(Integer.toString(IconHeight + 5)) && !IconBitmaps.get(i).getM_path().contains(Integer.toString(IconHeight)))) {
                        IconBitmaps.remove(i);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void removeBufferIconBitmapForOK1(final MPOI mpoi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < IconBitmaps.size(); ++i) {
                    if (i >= iconBitmapNum && IconBitmaps.get(i).getM_path().contains(",") && !IconBitmaps.get(i).getM_path().contains(Integer.toString((int) mpoi.getWidth()))) {
                        IconBitmaps.remove(i);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void removeBufferIconBitmapForOK(final MPOI mpoi) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < IconBitmaps.size(); ++i) {
                    if (i >= iconBitmapNum && IconBitmaps.get(i).getM_path().contains(",") && !IconBitmaps.get(i).getM_path().contains(Integer.toString((int) mpoi.getHeight()))) {
                        IconBitmaps.remove(i);
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(MainInterface.this, "done", Toast.LENGTH_LONG).show();
                        //Log.w(TAG, "run: " + IconBitmaps.size());
                        //iconBitmapNum = IconBitmaps.size();
                    }
                });
            }
        }).start();
    }

    private void initOtherVariable() {
        MessureChanged = false;
        strings = getResources().getStringArray(R.array.Type);
        trails = LitePal.findAll(Trail.class);
        simpleDateFormat1 = new SimpleDateFormat(MainInterface.this.getResources().getText(R.string.Date).toString());
        //初始化白板要素List
        geometry_whiteBlanks = new ArrayList<geometry_WhiteBlank>();
        getWhiteBlankData();
        //初始化比例尺格式信息
        scale_df = new DecimalFormat("0.0");

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

        color_Whiteblank = Color.RED;
    }

    private void forbiddenWidgetForQuery() {
        trail_imgbt.setVisibility(View.INVISIBLE);
        autoTrans_imgbt.setVisibility(View.INVISIBLE);
        centerPoint.setVisibility(View.GONE);
        locHere_fab.setVisibility(View.GONE);
        whiteBlank_fab.setVisibility(View.GONE);
        whiteBlankLayerBt.setVisibility(View.GONE);
        floatingActionsMenu.setVisibility(View.GONE);
        addPoi_imgbt.setVisibility(View.GONE);
    }

    private void showWidgetForQuery() {
        autoTrans_imgbt.setVisibility(View.VISIBLE);
        trail_imgbt.setVisibility(View.VISIBLE);
        centerPoint.setImageResource(R.drawable.ic_center_focus_weak_black_24dp);
        centerPoint.setVisibility(View.VISIBLE);
        locHere_fab.setVisibility(View.VISIBLE);
        whiteBlank_fab.setVisibility(View.VISIBLE);
        whiteBlankLayerBt.setVisibility(View.VISIBLE);
        floatingActionsMenu.setVisibility(View.VISIBLE);
        addPoi_imgbt.setVisibility(View.VISIBLE);
    }

    private boolean IconShift = false;

    private void showQueriedWidget() {
        FloatingActionButton iconplus = (FloatingActionButton) findViewById(R.id.icon_plus);
        FloatingActionButton iconminus = (FloatingActionButton) findViewById(R.id.icon_minus);
        FloatingActionButton iconok = (FloatingActionButton) findViewById(R.id.icon_ok);
        FloatingActionButton iconshift = (FloatingActionButton) findViewById(R.id.icon_shift);
        FloatingActionButton iconchoose = (FloatingActionButton) findViewById(R.id.icon_choose);
        FloatingActionButton icondelete = (FloatingActionButton) findViewById(R.id.icon_delete);
        iconplus.setVisibility(View.VISIBLE);
        iconminus.setVisibility(View.VISIBLE);
        iconok.setVisibility(View.VISIBLE);
        iconshift.setVisibility(View.VISIBLE);
        iconchoose.setVisibility(View.VISIBLE);
        icondelete.setVisibility(View.VISIBLE);
        iconplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WidthAndHeight = WidthAndHeight + 5;
                MPOI mpoi = LitePal.where("num = ?", Long.toString(QueriedIconPoiNum)).find(MPOI.class).get(0);
                IconHeight = IconHeight + 5;
                int[] whs = DataUtil.getWHForOrigin(mpoi.getImgPath(), IconHeight);
                IconWidth = whs[0];
                removeBufferIconBitmapForProgress();
                bufferIconBitmapMinus(mpoi);
                bufferIconBitmapPlus(mpoi);
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        iconminus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WidthAndHeight = WidthAndHeight - 5;
                MPOI mpoi = LitePal.where("num = ?", Long.toString(QueriedIconPoiNum)).find(MPOI.class).get(0);
                IconHeight = IconHeight - 5;
                int[] whs = DataUtil.getWHForOrigin(mpoi.getImgPath(), IconHeight);
                IconWidth = whs[0];
                removeBufferIconBitmapForProgress();
                bufferIconBitmapMinus(mpoi);
                bufferIconBitmapPlus(mpoi);
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        iconok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!IconShift) {
                    MPOI mpoi = new MPOI();
                    //mpoi.setWidth(WidthAndHeight);
                    //mpoi.setHeight(WidthAndHeight);
                    mpoi.setWidth(IconWidth);
                    mpoi.setHeight(IconHeight);
                    mpoi.updateAll("num = ?", Long.toString(QueriedIconPoiNum));
                    removeBufferIconBitmapForOK(LitePal.where("num = ?", Long.toString(QueriedIconPoiNum)).find(MPOI.class).get(0));
                    /*try {
                        plusIconBitmapNum(QueriedIconPoiNum);
                    }catch (Exception e){
                        Log.w(TAG, e.toString());
                    }*/
                    initIconBitmap1(LitePal.findAll(IconDataset.class));
                    resetQueriedIcon();
                    //pdfView.zoomWithAnimation(c_zoom);
                } else {
                    showQueriedWidgetForShift();
                    IconShift = false;
                    MPOI mpoi = new MPOI();
                    mpoi.setLat(centerPointLoc.x);
                    mpoi.setLng(centerPointLoc.y);
                    mpoi.updateAll("num = ?", Long.toString(QueriedIconPoiNum));
                    centerPoint.setVisibility(View.GONE);
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        iconshift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IconShift = true;
                if (IconShift) {
                    forbiddenQueriedWidgetForShift();
                    MPOI mpoi = LitePal.where("num = ?", Long.toString(QueriedIconPoiNum)).find(MPOI.class).get(0);
                    for (int i = 0; i < IconBitmaps.size(); ++i) {
                        if (mpoi.getHeight() != 80) {
                            if (IconBitmaps.get(i).getM_path().equals(mpoi.getImgPath() + "," + Integer.toString((int) mpoi.getHeight()))) {
                                centerPoint.setImageBitmap(IconBitmaps.get(i).getM_bm());
                                break;
                            }
                        } else {
                            if (IconBitmaps.get(i).getM_path().equals(mpoi.getImgPath())) {
                                centerPoint.setImageBitmap(IconBitmaps.get(i).getM_bm());
                                break;
                            }
                        }
                    }
                    centerPoint.setVisibility(View.VISIBLE);
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
        iconchoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopueWindowForIcon();
            }
        });
        icondelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainInterface.this);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LitePal.deleteAll(MPOI.class, "num = ? ", Long.toString(QueriedIconPoiNum));
                        resetQueriedIcon();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setMessage("确定要删除该标志吗?");
                builder.setTitle("提示");
                builder.show();
            }
        });
    }

    private void plusIconBitmapNum(long num) throws Exception {
        List<MPOI> mpois = LitePal.where("num = ?", Long.toString(num)).find(MPOI.class);
        if (mpois.size() != 0) {
            MPOI mpoi = mpois.get(0);
            for (int i = 0; i < IconBitmaps.size(); ++i) {
                if (IconBitmaps.get(i).getM_path().equals(mpoi.getImgPath() + "," + Integer.toString(WidthAndHeight)) && i >= iconBitmapNum) {
                    Log.w(TAG, "plusIconBitmapNum: " + i + ";" + iconBitmapNum);
                    iconBitmapNum++;
                    Log.w(TAG, "plusIconBitmapNum: " + i + ";" + iconBitmapNum);
                    break;
                }
            }
        } else throw new Exception("唯一标识符出错");
    }

    private void resetQueriedIcon() {
        QueriedIconPoiNum = -1;
        pdfView.zoomWithAnimation(c_zoom);
        forbiddenQueriedWidget();
        showCheckboxForQuery();
        showWidgetForQuery();
    }

    private void forbiddenQueriedWidget() {
        FloatingActionButton iconplus = (FloatingActionButton) findViewById(R.id.icon_plus);
        FloatingActionButton iconminus = (FloatingActionButton) findViewById(R.id.icon_minus);
        FloatingActionButton iconok = (FloatingActionButton) findViewById(R.id.icon_ok);
        FloatingActionButton iconshift = (FloatingActionButton) findViewById(R.id.icon_shift);
        FloatingActionButton iconchosse = (FloatingActionButton) findViewById(R.id.icon_choose);
        FloatingActionButton icondelete = (FloatingActionButton) findViewById(R.id.icon_delete);
        iconplus.setVisibility(View.GONE);
        iconminus.setVisibility(View.GONE);
        iconok.setVisibility(View.GONE);
        iconshift.setVisibility(View.GONE);
        iconchosse.setVisibility(View.GONE);
        icondelete.setVisibility(View.GONE);
    }

    private void showQueriedWidgetForShift() {
        FloatingActionButton iconplus = (FloatingActionButton) findViewById(R.id.icon_plus);
        FloatingActionButton iconminus = (FloatingActionButton) findViewById(R.id.icon_minus);
        FloatingActionButton iconshift = (FloatingActionButton) findViewById(R.id.icon_shift);
        FloatingActionButton iconchoose = (FloatingActionButton) findViewById(R.id.icon_choose);
        FloatingActionButton icondelete = (FloatingActionButton) findViewById(R.id.icon_delete);
        iconplus.setVisibility(View.VISIBLE);
        iconminus.setVisibility(View.VISIBLE);
        iconshift.setVisibility(View.VISIBLE);
        iconchoose.setVisibility(View.VISIBLE);
        icondelete.setVisibility(View.VISIBLE);
    }

    private void forbiddenQueriedWidgetForShift() {
        FloatingActionButton iconplus = (FloatingActionButton) findViewById(R.id.icon_plus);
        FloatingActionButton iconminus = (FloatingActionButton) findViewById(R.id.icon_minus);
        FloatingActionButton iconshift = (FloatingActionButton) findViewById(R.id.icon_shift);
        FloatingActionButton iconchoose = (FloatingActionButton) findViewById(R.id.icon_choose);
        FloatingActionButton icondelete = (FloatingActionButton) findViewById(R.id.icon_delete);
        iconplus.setVisibility(View.GONE);
        iconminus.setVisibility(View.GONE);
        iconshift.setVisibility(View.GONE);
        iconchoose.setVisibility(View.GONE);
        icondelete.setVisibility(View.GONE);
    }

    private void forbiddenCheckboxForQuery() {
        trailLayerBt.setVisibility(View.GONE);
        whiteBlankLayerBt.setVisibility(View.GONE);
        centerPointModeBt.setVisibility(View.GONE);
        poiLayerBt.setVisibility(View.GONE);
        type1_checkbox.setVisibility(View.GONE);
        type2_checkbox.setVisibility(View.GONE);
        type3_checkbox.setVisibility(View.GONE);
    }

    private void showCheckboxForQuery() {
        trailLayerBt.setVisibility(View.VISIBLE);
        whiteBlankLayerBt.setVisibility(View.VISIBLE);
        centerPointModeBt.setVisibility(View.VISIBLE);
        poiLayerBt.setVisibility(View.VISIBLE);
        type1_checkbox.setVisibility(View.VISIBLE);
        type2_checkbox.setVisibility(View.VISIBLE);
        type3_checkbox.setVisibility(View.VISIBLE);
    }

    List<com.esri.arcgisruntime.geometry.Point> mmpoints;

    private void initWidget() {
        initCheckbox();
        //中心点图标初始化
        centerPoint = (ImageView) findViewById(R.id.centerPoint);
        //初始化测量相关按钮
        cancel_messure_fab = (FloatingActionButton) findViewById(R.id.cancel_messure);
        cancel_messure_fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessureChanged = true;
                if (isDrawType != TuzhiEnum.LINE_DRAW_TYPE) {
                    ////测量
                    distanceSum = 0;
                    poinum_messure = 0;
                    messure_pts = "";
                    PointCollection pointCollection = new PointCollection(SpatialReference.create(4490));
                    for (int i = 0; i < distanceLatLngs.size(); ++i) {
                        com.esri.arcgisruntime.geometry.Point point = new com.esri.arcgisruntime.geometry.Point(distanceLatLngs.get(i).getLongitude(), distanceLatLngs.get(i).getLatitude(), SpatialReference.create(4490));
                        pointCollection.add(point);
                    }
                    Polyline polyline = new Polyline(pointCollection);
                    Polygon polygon = GeometryEngine.buffer(polyline, 0.0005);
                    mmpoints = new ArrayList<>();
                    int mnum = 0;
                    for (Iterator<com.esri.arcgisruntime.geometry.Point> iter = polygon.toPolyline().getParts().getPartsAsPoints().iterator(); iter.hasNext(); ) {
                        mmpoints.add(iter.next());
                        mnum++;
                    }
                    pdfView.zoomWithAnimation(c_zoom);
                    distancesLatLngs.add(distanceLatLngs);
                    distanceLatLngs = new ArrayList<>();
                } else {
                    ////////////////////
                    /////记录线要素

                    String lineFeature = drawLineFeature;
                    drawLineFeature = "";
                    LineFeatures.add(lineFeature);
                    PointCollection pointCollection = new PointCollection(SpatialReference.create(4490));
                    for (int i = 0; i < distanceLatLngs.size(); ++i) {
                        com.esri.arcgisruntime.geometry.Point point = new com.esri.arcgisruntime.geometry.Point(distanceLatLngs.get(i).getLongitude(), distanceLatLngs.get(i).getLatitude(), SpatialReference.create(4490));
                        pointCollection.add(point);
                    }
                    Polygon polygon = new Polygon(pointCollection);
                    Log.w(TAG, "onLongClick: " + GeometryEngine.areaGeodetic(polygon, new AreaUnit(AreaUnitId.SQUARE_KILOMETERS), GeodeticCurveType.GEODESIC));
                }
                ////////////////////////
                return true;
            }
        });
        cancel_messure_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                area = 0;
                CenterPtMessuredArea = new PointF();
                MessureChanged = true;
                if (isDrawType != TuzhiEnum.LINE_DRAW_TYPE) {
                    /////////////////
                    if (mmpoints != null)
                        mmpoints.clear();
                    distanceSum = 0;
                    distancesLatLngs.clear();
                    distanceLatLngs.clear();
                    if (showMode == TuzhiEnum.CENTERMODE) isQuery = true;
                    else isQuery = false;
                    centerPointModeBt.setVisibility(View.VISIBLE);
                    isMessure = false;
                    poinum_messure = 0;
                    messure_pts = "";
                    delete_messure_fab.setVisibility(View.GONE);
                    backpt_messure_fab.setVisibility(View.GONE);
                    cancel_messure_fab.setVisibility(View.GONE);
                    whiteBlank_fab.setVisibility(View.VISIBLE);
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("轨迹记录中");
                    } else toolbar.setTitle(pdfFileName);
                } else {
                    ////////////////////
                    /////记录线要素
                    if (!drawLineFeature.isEmpty()) {
                        String lineFeature = drawLineFeature;
                        drawLineFeature = "";
                        LineFeatures.add(lineFeature);
                    }
                    delete_messure_fab.setVisibility(View.GONE);
                    backpt_messure_fab.setVisibility(View.GONE);
                    cancel_messure_fab.setVisibility(View.GONE);
                    isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                    String time = String.valueOf(System.currentTimeMillis());
                    DMLine dmLine = new DMLine();
                    dmLine.setXh(Integer.toString(dmLines.size() + 1));
                    dmLine.setMapid(time);
                    dmLine.setDimingid(time);
                    dmLine.setMultiline(LineFeatures);
                    dmLine.save();
                    LineFeatures.clear();
                    dmLines = LitePal.findAll(DMLine.class);
                    GoDMLSinglePOIPage(time);
                }
                pdfView.zoomWithAnimation(c_zoom);
                ////////////////////////
            }
        });
        delete_messure_fab = (FloatingActionButton) findViewById(R.id.delete_messure);
        delete_messure_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessureChanged = true;
                if (isDrawType != TuzhiEnum.LINE_DRAW_TYPE) {
                    distanceLatLngs.clear();
                    int MessuredLinesSize = distancesLatLngs.size();
                    Log.w(TAG, "20191015test: " + MessuredLinesSize);
                    switch (MessuredLinesSize) {
                        case 0:
                            break;
                        default:
                            if(poinum_messure == 0)
                            {
                                distancesLatLngs.remove(MessuredLinesSize-1);
                                mmpoints.clear();
                            }
                            break;
                    }
                    distanceSum = 0;
                    messure_pts = "";
                    poinum_messure = 0;
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("正在测量(轨迹记录中)");
                    } else toolbar.setTitle("正在测量");
                } else {
                    ////////////////////
                    /////记录线要素
                    if (drawLineFeature.isEmpty() && LineFeatures.size() != 0) {
                        drawLineFeature = LineFeatures.get(LineFeatures.size() - 1);
                        LineFeatures.remove(LineFeatures.size() - 1);
                    } else
                        drawLineFeature = "";
                    ////////////////////////
                }
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        backpt_messure_fab = (FloatingActionButton) findViewById(R.id.backpts_messure);
        backpt_messure_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessureChanged = true;
                if (isDrawType != TuzhiEnum.LINE_DRAW_TYPE) {
                    ///////////////////////////
                    if (poinum_messure >= 1) {
                        distanceLatLngs.remove(distanceLatLngs.size() - 1);

                        if (poinum_messure > 1) {
                            messure_pts = messure_pts.substring(0, messure_pts.lastIndexOf(" "));
                            messure_pts = messure_pts.substring(0, messure_pts.lastIndexOf(" "));
                            poinum_messure--;
                        }
                        else
                        {
                            messure_pts = "";
                            poinum_messure = 0;
                        }
                        pdfView.zoomWithAnimation(c_zoom);
                    } else {
                        distanceLatLngs.clear();
                        int MessuredLinesSize = distancesLatLngs.size();
                        Log.w(TAG, "20191015test: " + MessuredLinesSize);
                        switch (MessuredLinesSize) {
                            case 0:
                                distanceSum = 0;
                                messure_pts = "";
                                poinum_messure = 0;
                                break;
                            default:
                                distanceLatLngs = distancesLatLngs.get(MessuredLinesSize-1);
                                distancesLatLngs.remove(MessuredLinesSize-1);
                                mmpoints.clear();
                                setMessure_ptsAndDistanceSum();
                                setPoinum_messure();
                                break;
                        }


                    }
                } else {
                    ////////////////////
                    /////记录线要素
                    if (drawLineFeature.isEmpty() && LineFeatures.size() != 0) {
                        drawLineFeature = LineFeatures.get(LineFeatures.size() - 1);
                        LineFeatures.remove(LineFeatures.size() - 1);
                    } else {
                        if (DataUtil.appearNumber(drawLineFeature, " ") > 0)
                            drawLineFeature = drawLineFeature.substring(0, drawLineFeature.lastIndexOf(" "));
                        else drawLineFeature = "";
                    }
                    ////////////////////////
                }
                pdfView.zoomWithAnimation(c_zoom);
            }
        });
        //初始化白板按钮
        whiteBlank_fab = (FloatingActionButton) findViewById(R.id.whiteBlank);
        whiteBlank_fab.setImageResource(R.drawable.ic_brush_black_24dp);
        whiteBlank_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpenWhiteBlank) {
                    isOpenWhiteBlank = true;
                    //whiteBlank_fab.setImageResource(R.drawable.ic_cancel_black_24dp);
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("正在进行白板绘图(轨迹记录中)");
                    } else toolbar.setTitle("正在进行白板绘图");
                    showPopueWindowForWhiteblank();
                    locHere_fab.setVisibility(View.GONE);
                    whiteBlank_fab.setVisibility(View.INVISIBLE);
                    isWhiteBlank = true;
                    whiteBlankLayerBt.setChecked(true);
                    pdfView.zoomWithAnimation(c_zoom);
                }
            }
        });
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
                if (isAutoTrans) {
                    isAutoTrans = false;
                    autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
                } else {
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
                String str = textView.getText().toString();
                Log.w(TAG, "onClick: " + str);
                if (!str.equals("点击位置在区域之外") && !str.equals("在这里显示坐标值")) {
                    if (isCoordinateType == TuzhiEnum.COORDINATE_DEFAULT_TYPE) {
                        //String[] strs = str.split(";");
                        //PointF pt = new PointF(Float.valueOf(strs[0]), Float.valueOf(strs[1]));
                        textView.setText(Integer.toString((int) locLatForTap) + "°" + Integer.toString((int) ((locLatForTap - (int) locLatForTap) * 60)) + "′" + Integer.toString((int) (((locLatForTap - (int) locLatForTap) * 60 - (int) ((locLatForTap - (int) locLatForTap) * 60)) * 60)) + "″;" + Integer.toString((int) locLongForTap) + "°" + Integer.toString((int) ((locLongForTap - (int) locLongForTap) * 60)) + "′" + Integer.toString((int) (((locLongForTap - (int) locLongForTap) * 60 - (int) ((locLongForTap - (int) locLongForTap) * 60)) * 60)) + "″");
                        isCoordinateType = TuzhiEnum.COORDINATE_BLH_TYPE;
                        locError(Integer.toString(textView.getHeight()));
                    } else if (isCoordinateType == TuzhiEnum.COORDINATE_BLH_TYPE) {
                        //String[] strs = str.split(";");
                        //locError(strs[0] + "还有: " + strs[1]);
                        //PointF pt = new PointF(Float.valueOf(strs[0].substring(0, strs[0].indexOf("°"))) + (Float.valueOf(strs[0].substring(strs[0].indexOf("°") + 1, strs[0].indexOf("′"))) / 60) + (Float.valueOf(strs[0].substring(strs[0].indexOf("′") + 1, strs[0].indexOf("″"))) / 3600), Float.valueOf(strs[1].substring(0, strs[1].indexOf("°"))) + (Float.valueOf(strs[1].substring(strs[1].indexOf("°") + 1, strs[1].indexOf("′"))) / 60) + (Float.valueOf(strs[1].substring(strs[1].indexOf("′") + 1, strs[1].indexOf("″"))) / 3600));
                        textView.setText(df.format(locLatForTap) + "; " + df.format(locLongForTap));
                        isCoordinateType = TuzhiEnum.COORDINATE_DEFAULT_TYPE;
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
                Toast.makeText(MainInterface.this, R.string.FinishCopy, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
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
                    Toast.makeText(MainInterface.this, R.string.TrailError, Toast.LENGTH_SHORT).show();
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
                Intent start_mService = new Intent(MainInterface.this, RecordTrail.class);
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
                    Intent stop_mService = new Intent(MainInterface.this, RecordTrail.class);
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
                    Toast.makeText(MainInterface.this, R.string.OpenTrailError, Toast.LENGTH_SHORT).show();
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
                if (esterEgg_dm) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainInterface.this);
                    dialog.setTitle("提示");
                    dialog.setMessage("需要添加什么要素?");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("点要素", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            poiLayerBt.setChecked(true);
                            showPOI = true;
                            //pdfView.resetZoomWithAnimation();
                            pdfView.zoomWithAnimation(c_zoom);
                            if (isDrawType == TuzhiEnum.POI_DRAW_TYPE) {
                                isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                                    toolbar.setTitle("正在记录轨迹");
                                } else toolbar.setTitle(pdfFileName);
                                if (showMode == TuzhiEnum.CENTERMODE) isQuery = true;
                                else isQuery = false;
                                pdfView.zoomWithAnimation(c_zoom);
                            } else {
                                isDrawType = TuzhiEnum.POI_DRAW_TYPE;
                                isQuery = false;
                                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                                    toolbar.setTitle("正在插放兴趣点(轨迹记录中)");
                                } else toolbar.setTitle("正在插放兴趣点");
                                isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
                            }
                        }
                    });
                    dialog.setNegativeButton("线要素(地名类型,需打开地名功能)", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backpt_messure_fab.setVisibility(View.VISIBLE);
                            cancel_messure_fab.setVisibility(View.VISIBLE);
                            delete_messure_fab.setVisibility(View.VISIBLE);
                            poiLayerBt.setChecked(false);
                            showPOI = false;
                            pdfView.zoomWithAnimation(c_zoom);
                            if (isDrawType == TuzhiEnum.LINE_DRAW_TYPE) {
                                isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                                    toolbar.setTitle("正在记录轨迹");
                                } else toolbar.setTitle(pdfFileName);
                                if (showMode == TuzhiEnum.CENTERMODE) isQuery = true;
                                else isQuery = false;
                                pdfView.zoomWithAnimation(c_zoom);
                            } else {
                                isDrawType = TuzhiEnum.LINE_DRAW_TYPE;
                                drawLineFeature = "";
                                LineFeatures.clear();
                                isQuery = false;
                                if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                                    toolbar.setTitle("正在插放兴趣点(轨迹记录中)");
                                } else toolbar.setTitle("正在插放兴趣点");
                                isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
                            }
                        }
                    });
                    dialog.show();
                } else {
                    poiLayerBt.setChecked(true);
                    showPOI = true;
                    //pdfView.resetZoomWithAnimation();
                    pdfView.zoomWithAnimation(c_zoom);
                    if (isDrawType == TuzhiEnum.POI_DRAW_TYPE) {
                        isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                        if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                            toolbar.setTitle("正在记录轨迹");
                        } else toolbar.setTitle(pdfFileName);
                        if (showMode == TuzhiEnum.CENTERMODE) isQuery = true;
                        else isQuery = false;
                        pdfView.zoomWithAnimation(c_zoom);
                    } else {
                        isDrawType = TuzhiEnum.POI_DRAW_TYPE;
                        isQuery = false;
                        if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                            toolbar.setTitle("正在插放兴趣点(轨迹记录中)");
                        } else toolbar.setTitle("正在插放兴趣点");
                        isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
                    }
                }

            }
        });
        query_poi_imgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                poiLayerBt.setChecked(true);
                showPOI = true;
                pdfView.zoomWithAnimation(c_zoom);
                if (!isQuery) {
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("正在查询(轨迹记录中)");
                    } else toolbar.setTitle("正在查询");
                    isQuery = true;
                    isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
                    isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
                } else {
                    isQuery = false;
                    if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                        toolbar.setTitle("正在记录轨迹");
                    } else toolbar.setTitle(pdfFileName);
                }
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
                isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
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
                clickLocHereBT();
            }
        });
        restoreZoom_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.restoreZoom);
        restoreZoom_fab.setImageResource(R.drawable.ic_autorenew);
        restoreZoom_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRestoreZoomBT();
            }
        });
        addTape_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addTape);
        addTape_fab.setImageResource(R.drawable.ic_sound);
        addTape_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //浮动按钮4 具体功能如下:
                try {
                    if (isPositionModuleEnable()) {
                        floatingActionsMenu.close(false);
                        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                        startActivityForResult(intent, EnumClass.REQUEST_CODE_TAPE);
                    } else
                        Toast.makeText(MainInterface.this, R.string.LocError, Toast.LENGTH_LONG).show();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MyApplication.getContext(), R.string.TakeTapeError, Toast.LENGTH_LONG).show();
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
    }

    private void setMessure_ptsAndDistanceSum(){
        int size = distanceLatLngs.size();
        distanceSum = 0;
        for (int i = 0; i < size; ++i){
            DistanceLatLng distanceLatLng = distanceLatLngs.get(i);
            if (i == 0)
            {
                messure_pts = Float.toString(distanceLatLng.getLatitude()) + " " + Float.toString(distanceLatLng.getLongitude());
            }
            else
            {
                messure_pts = messure_pts + " " + Float.toString(distanceLatLng.getLatitude()) + " " + Float.toString(distanceLatLng.getLongitude());
            }
            distanceSum += distanceLatLng.getDistance();
        }
    }

    private void setPoinum_messure(){
        int size = distanceLatLngs.size();

        poinum_messure = size;
    }

    private boolean isPositionModuleEnable(){
        return m_lat != 0 && m_long != 0;
    }

    private boolean isDeviceInThisMap(){
        return m_lat <= max_lat && m_lat >= min_lat && m_long >= min_long && m_long <= max_long;
    }

    private void locHereForOriginalZoom(){
        locHere_fab.setImageResource(R.drawable.ic_location_searching);
        pdfView.resetZoomWithAnimation();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                final PointF ppz = LatLng.getPixLocFromGeoL(new PointF((float) m_lat, (float) m_long), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
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
    }

    private void locHereForOtherZoom(){
        locHere_fab.setImageResource(R.drawable.ic_my_location);
        PointF ppz = LatLng.getPixLocFromGeoL(new PointF((float) m_lat, (float) m_long), current_pagewidth, current_pageheight, w, h, min_long, min_lat);
        ppz.x = ppz.x - 10;
        pdfView.zoomCenteredTo(8, ppz);
        pdfView.setPositionOffset(verx);
        isPos = true;
        floatingActionsMenu.close(false);
    }

    private void showPositionModuleError(){
        Toast.makeText(MyApplication.getContext(), R.string.LocError, Toast.LENGTH_LONG).show();
    }

    private void showDeviceNotInThisMapError(){
        Toast.makeText(MyApplication.getContext(), R.string.LocError, Toast.LENGTH_LONG).show();
    }

    private void clickLocHereBT(){
        if (isPositionModuleEnable()) {
            if (isDeviceInThisMap()) {
                if (pdfView.getZoom() != ORIGINAL_ZOOM) {
                    locHereForOriginalZoom();
                } else {
                    locHereForOtherZoom();
                }
            } else
                showDeviceNotInThisMapError();
        } else
            showPositionModuleError();
    }

    private void clickRestoreZoomBT(){
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
            if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
                toolbar.setTitle("正在记录轨迹");
            } else toolbar.setTitle(pdfFileName);
            if (!isFullLocation) {
                isFullLocation = true;
                if (location != null) {
                    updateView(location);
                } else
                    Toast.makeText(MainInterface.this, R.string.ResetError, Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(MainInterface.this, R.string.SpecialError + R.string.QLXWM, Toast.LENGTH_SHORT).show();
    }

    private void forbiddenCheckbox() {
        type1_checkbox = (CheckBox) findViewById(R.id.type1_poiLayer);
        type1_checkbox.setVisibility(View.GONE);
        type2_checkbox = (CheckBox) findViewById(R.id.type2_poiLayer);
        type2_checkbox.setVisibility(View.GONE);
        type3_checkbox = (CheckBox) findViewById(R.id.type3_poiLayer);
        type3_checkbox.setVisibility(View.GONE);
    }

    private void forbiddenWidget() {
        forbiddenCheckbox();
        ImageView imageView = (ImageView) findViewById(R.id.myscale);
        imageView.setVisibility(View.GONE);
        //中心点图标初始化
        centerPoint = (ImageView) findViewById(R.id.centerPoint);
        centerPoint.setVisibility(View.GONE);
        centerPointModeBt = (CheckBox) findViewById(R.id.centerPointMode);
        centerPointModeBt.setVisibility(View.GONE);
        //图层控制按钮初始化
        poiLayerBt = (CheckBox) findViewById(R.id.poiLayer);
        poiLayerBt.setVisibility(View.GONE);
        trailLayerBt = (CheckBox) findViewById(R.id.trailLayer);
        trailLayerBt.setVisibility(View.GONE);
        whiteBlankLayerBt = (CheckBox) findViewById(R.id.whiteBlankLayer);
        whiteBlankLayerBt.setVisibility(View.GONE);
        //初始化测量相关按钮
        cancel_messure_fab = (FloatingActionButton) findViewById(R.id.cancel_messure);
        cancel_messure_fab.setVisibility(View.GONE);
        delete_messure_fab = (FloatingActionButton) findViewById(R.id.delete_messure);
        delete_messure_fab.setVisibility(View.GONE);
        backpt_messure_fab = (FloatingActionButton) findViewById(R.id.backpts_messure);
        backpt_messure_fab.setVisibility(View.GONE);
        //初始化白板按钮
        whiteBlank_fab = (FloatingActionButton) findViewById(R.id.whiteBlank);
        whiteBlank_fab.setVisibility(View.GONE);
        //初始化比例尺信息
        scaleShow = (TextView) findViewById(R.id.scale);
        //获取传感器管理器系统服务
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolBar1);
        setSupportActionBar(toolbar);
        autoTrans_imgbt = (ImageButton) findViewById(R.id.trans);
        autoTrans_imgbt.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.txt);
        textView.setVisibility(View.GONE);
        trail_imgbt = (ImageButton) findViewById(R.id.trail);
        startTrail_imgbt = (ImageButton) findViewById(R.id.startTrail);
        endTrail_imgbt = (ImageButton) findViewById(R.id.endTrail);
        addPoi_imgbt = (ImageButton) findViewById(R.id.addPoi);
        query_poi_imgbt = (ImageButton) findViewById(R.id.query_poi);
        floatingActionsMenu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fam);
        floatingActionsMenu.setVisibility(View.GONE);
        trail_imgbt.setVisibility(View.GONE);
        startTrail_imgbt.setVisibility(View.GONE);
        endTrail_imgbt.setVisibility(View.GONE);
        addPoi_imgbt.setVisibility(View.GONE);
        query_poi_imgbt.setVisibility(View.GONE);
        addPhoto_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addPhoto);
        addPhoto_fab.setVisibility(View.GONE);
        locHere_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.locHere);
        locHere_fab.setVisibility(View.GONE);
        restoreZoom_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.restoreZoom);
        restoreZoom_fab.setVisibility(View.GONE);
        addTape_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.addTape);
        addTape_fab.setVisibility(View.GONE);
        messureDistance_fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.messureDistance);
        messureDistance_fab.setVisibility(View.GONE);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.MainInterfaceToolBar);
        linearLayout.setVisibility(View.GONE);
    }

    private void getWhiteBlankData() {
        List<Lines_WhiteBlank> lines = LitePal.where("ic = ?", ic).find(Lines_WhiteBlank.class);
        if (lines.size() >= 0) {
            for (Lines_WhiteBlank line : lines) {
                geometry_WhiteBlank geometry_whiteBlank = new geometry_WhiteBlank(line.getIc(), line.getLines(), line.getColor(), line.getMaxlat(), line.getMaxlng(), line.getMinlat(), line.getMinlng());
                geometry_whiteBlanks.add(geometry_whiteBlank);
            }
        }
    }

    public void getNormalBitmap() {
        ////////////////////////缓存Bitmap//////////////////////////////
        bts = new ArrayList<bt>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                bts.clear();
                List<POI> pois = LitePal.where("x <= " + String.valueOf(max_lat) + ";" + "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
                if (pois.size() > 0) {
                    for (POI poi : pois) {
                        List<MPHOTO> mphotos = LitePal.where("poic = ?", poi.getPoic()).find(MPHOTO.class);
                        //PointF pt2 = LatLng.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()));
                        //canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                        //locError(Boolean.toString(poi.getPath().isEmpty()));
                        //locError(Integer.toString(poi.getPath().length()));
                        //locError(poi.getPath());
                        if (poi.getPhotonum() != 0 && mphotos.size() != 0) {
                            locError("poic = " + poi.getPoic());
                            locError("需要显示的缩略图数量1 : " + Integer.toString(mphotos.size()));
                            String path = mphotos.get(0).getPath();
                            locError("需要显示的缩略图数量1 : " + path);
                            File file = new File(path);
                            if (file.exists()) {
                                Bitmap bitmap = DataUtil.getImageThumbnail(path, 100, 80);
                                if (mphotos.size() != 0) {
                                    int degree = DataUtil.getPicRotate(path);
                                    if (degree != 0) {
                                        Matrix m = new Matrix();
                                        m.setRotate(degree); // 旋转angle度
                                        Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                                    }
                                    Log.w(TAG, "imageUri: " + path);
                                    bt btt = new bt(bitmap, path);
                                    btt.setPoic(poi.getPoic());
                                    bts.add(btt);
                                }
                            } else {
                                Log.w(TAG, "imageUriWithWrongPath: " + path);
                                //Resources res = MyApplication.getContext().getResources();
                                //Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ic_info_black);
                                Drawable drawable = MyApplication.getContext().getResources().getDrawable(R.drawable.imgerror);
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                Bitmap bitmap = Bitmap.createBitmap(bd.getBitmap(), 0, 0, bd.getBitmap().getWidth(), bd.getBitmap().getHeight());
                                bitmap = ThumbnailUtils.extractThumbnail(bitmap, 80, 120,
                                        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                                bt btt = new bt(bitmap, path);
                                btt.setPoic("11");
                                bts.add(btt);
                            }
                        } else {
                            POI poi1 = new POI();
                            if (mphotos.size() != 0) poi1.setPhotonum(mphotos.size());
                            else poi1.setToDefault("photonum");
                            poi1.updateAll("poic = ?", poi.getPoic());
                        }
                    }
                }
                locError("需要显示的缩略图数量2 : " + Integer.toString(bts.size()));
                isCreateBitmap = true;
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pdfView.zoomWithAnimation(c_zoom);
                        }catch (Exception e){

                        }
                    }
                });*/
            }
        }).start();
        //////////////////////////////////////////////////////////////////
    }

    public void getBitmap1() {
        ////////////////////////缓存Bitmap//////////////////////////////
        bts1 = new ArrayList<bt>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                hasBitmap1 = false;
                bts1.clear();
                if (kmltests.size() > 0) {
                    for (int ii = 0; ii < kmltests.size(); ii++) {
                        List<plqzp> mphotos = LitePal.where("xh = ?", kmltests.get(ii).getXh()).find(plqzp.class);
                        //PointF pt2 = LatLng.getPixLocFromGeoL(new PointF(poi.getX(), poi.getY()));
                        //canvas.drawRect(new RectF(pt2.x - 5, pt2.y - 38, pt2.x + 5, pt2.y), paint2);
                        //locError(Boolean.toString(poi.getPath().isEmpty()));
                        //locError(Integer.toString(poi.getPath().length()));
                        //locError(poi.getPath());
                        String path = mphotos.get(0).getZp1();
                        File file = new File(path);
                        if (file.exists()) {
                            Bitmap bitmap = DataUtil.getImageThumbnail(path, 100, 80);
                            if (mphotos.size() != 0) {
                                int degree = DataUtil.getPicRotate(path);
                                if (degree != 0) {
                                    Matrix m = new Matrix();
                                    m.setRotate(degree); // 旋转angle度
                                    Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                                }
                                bt btt = new bt(bitmap, path);
                                bts1.add(btt);
                            }
                        } else {
                            //Resources res = MyApplication.getContext().getResources();
                            //Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ic_info_black);
                            Drawable drawable = MyApplication.getContext().getResources().getDrawable(R.drawable.imgerror);
                            BitmapDrawable bd = (BitmapDrawable) drawable;
                            Bitmap bitmap = Bitmap.createBitmap(bd.getBitmap(), 0, 0, bd.getBitmap().getWidth(), bd.getBitmap().getHeight());
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 80, 120,
                                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            bt btt = new bt(bitmap, path);
                            bts1.add(btt);
                        }

                    }
                }
                locError("需要显示的缩略图数量22 : " + Integer.toString(bts1.size()));
                hasBitmap1 = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pdfView.zoomWithAnimation(c_zoom);
                    }
                });
            }
        }).start();
        //////////////////////////////////////////////////////////////////
    }

    public void getDMBZBitmap() {
        ////////////////////////缓存Bitmap//////////////////////////////
        DMBZBts = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                hasDMBZBitmap = false;
                DMBZBts.clear();
                dmbzList = LitePal.findAll(DMBZ.class);
                Log.w(TAG, "dmbzList: " + dmbzList.size());
                if (dmbzList.size() > 0) {
                    for (int ii = 0; ii < dmbzList.size(); ii++) {
                        String path = dmbzList.get(ii).getIMGPATH();
                        try {
                            if (path != null && !path.isEmpty()) {
                                if (!path.substring(0, path.indexOf("|")).contains(Environment.getExternalStorageDirectory().toString())) {
                                    Log.w(TAG, "run: " + Environment.getExternalStorageDirectory() + "/地名标志照片/" + path.substring(0, path.indexOf("|")));
                                    path = Environment.getExternalStorageDirectory() + "/地名标志照片/" + path.substring(0, path.indexOf("|"));
                                } else {
                                    Log.w(TAG, "run: " + path.substring(0, path.indexOf("|")));
                                    path = path.substring(0, path.indexOf("|"));
                                }
                                File file = new File(path);
                                if (file.exists()) {
                                    Bitmap bitmap = DataUtil.getImageThumbnail(path, 100, 80);
                                    int degree = DataUtil.getPicRotate(path);
                                    if (degree != 0) {
                                        Matrix m = new Matrix();
                                        m.setRotate(degree); // 旋转angle度
                                        Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                                    }
                                    bt btt = new bt(bitmap, path);
                                    DMBZBts.add(btt);
                                } else {
                                    //Resources res = MyApplication.getContext().getResources();
                                    //Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ic_info_black);
                                    Drawable drawable = MyApplication.getContext().getResources().getDrawable(R.drawable.imgerror);
                                    BitmapDrawable bd = (BitmapDrawable) drawable;
                                    Bitmap bitmap = Bitmap.createBitmap(bd.getBitmap(), 0, 0, bd.getBitmap().getWidth(), bd.getBitmap().getHeight());
                                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, 80, 120,
                                            ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                                    bt btt = new bt(bitmap, path);
                                    DMBZBts.add(btt);
                                }
                            }
                        } catch (Exception e) {

                        }

                    }
                }
                hasDMBZBitmap = true;
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pdfView.zoomWithAnimation(c_zoom);
                        }catch (Exception e){

                        }
                    }
                });*/
            }
        }).start();
        //////////////////////////////////////////////////////////////////
    }

    public void getDMitmap() {
        ////////////////////////缓存Bitmap//////////////////////////////
        new Thread(new Runnable() {
            @Override
            public void run() {
                hasDMBitmap = false;
                DMBts.clear();
                dmPoints = LitePal.findAll(DMPoint.class);
                if (dmPoints.size() > 0) {
                    for (int ii = 0; ii < dmPoints.size(); ii++) {
                        String path = dmPoints.get(ii).getImgpath();
                        String id = dmPoints.get(ii).getMapid();
                        try {
                            if (path != null && !path.isEmpty()) {
                                Log.w(TAG, "run: lzylzylzy1");
                                if (path.contains("|")) {
                                    if (!path.substring(0, path.indexOf("|")).contains(Environment.getExternalStorageDirectory().toString())) {
                                        Log.w(TAG, "run: " + Environment.getExternalStorageDirectory() + "/盘龙区多媒体数据/照片/" + path.substring(0, path.indexOf("|")));
                                        path = Environment.getExternalStorageDirectory() + "/盘龙区多媒体数据/照片/" + path.substring(0, path.indexOf("|"));
                                    } else {
                                        Log.w(TAG, "run: " + path.substring(0, path.indexOf("|")));
                                        path = path.substring(0, path.indexOf("|"));
                                    }
                                } else {
                                    if (!path.contains(Environment.getExternalStorageDirectory().toString())) {
                                        Log.w(TAG, "run: " + Environment.getExternalStorageDirectory() + "/盘龙区多媒体数据/照片/" + path);
                                        path = Environment.getExternalStorageDirectory() + "/盘龙区多媒体数据/照片/" + path;
                                    }
                                }
                                File file = new File(path);
                                if (file.exists()) {
                                    Bitmap bitmap = DataUtil.getImageThumbnail(path, 100, 80);
                                    int degree = DataUtil.getPicRotate(path);
                                    if (degree != 0) {
                                        Matrix m = new Matrix();
                                        m.setRotate(degree); // 旋转angle度
                                        Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                                    }
                                    bt btt = new bt(bitmap, id);
                                    DMBts.add(btt);
                                } else {
                                    bt btt = new bt(Bitmap.createBitmap(100, 80, Bitmap.Config.ALPHA_8), id);
                                    DMBts.add(btt);
                                }
                            }
                            Log.w(TAG, "run: lzylzylzy2");
                        } catch (Exception e) {
                            Log.w(TAG, "run: 发生重要错误");
                        }

                    }
                }
                hasDMBitmap = true;
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pdfView.zoomWithAnimation(c_zoom);
                        }catch (Exception e){

                        }
                    }
                });*/
            }
        }).start();
        //////////////////////////////////////////////////////////////////
    }

    private void updateDB() {
        List<POI> pois = LitePal.where("x <= " + String.valueOf(max_lat) + ";" + "x >= " + String.valueOf(min_lat) + ";" + "y <= " + String.valueOf(max_long) + ";" + "y >= " + String.valueOf(min_long)).find(POI.class);
        int size = pois.size();
        for (int i = 0; i < size; ++i) {
            String poic = pois.get(i).getPoic();
            List<MPHOTO> mphotos = LitePal.where("poic = ?", poic).find(MPHOTO.class);
            List<MTAPE> mtapes = LitePal.where("poic = ?", poic).find(MTAPE.class);
            POI poi1 = new POI();
            if (mtapes.size() != 0) poi1.setTapenum(mtapes.size());
            else poi1.setToDefault("tapenum");
            if (mphotos.size() != 0) poi1.setPhotonum(mphotos.size());
            else poi1.setToDefault("photonum");
            poi1.updateAll("poic = ?", pois.get(0).getPoic());
            Log.w(TAG, "updateDB: " + poic);
        }
        Log.w(TAG, "updateDB: ");
    }

    private void receiveQueryForMap() {
        dmLines = LitePal.findAll(DMLine.class);
        SharedPreferences pref = getSharedPreferences("update_query_attr_to_map", MODE_PRIVATE);
        String poic = pref.getString("poic", "");
        SharedPreferences.Editor editor = getSharedPreferences("update_query_attr_to_map", MODE_PRIVATE).edit();
        editor.putString("poic", "");
        editor.apply();
        Log.w(TAG, "onResume: " + poic);
        if (!poic.isEmpty()) {
            if (poic.contains("POI")) {
                hasQueriedPoi = true;
                Cursor cursor = LitePal.findBySQL("select * from POI where poic = ?", poic);
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
                        queriedPoi = new mPOIobj(POIC, x, y, time, tapenum, photonum, name, description);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } else if (poic.contains("DMBZ")) {
                hasQueriedPoi = true;
                poic = poic.replace("DMBZ", "");
                //List<kmltest> kmltests = LitePal.where("xh = ?", poic).find(kmltest.class);
                List<DMBZ> dmbzList = LitePal.where("xh = ?", poic).find(DMBZ.class);
                queriedPoi = new mPOIobj(poic, dmbzList.get(0).getLat(), dmbzList.get(0).getLng(), "", 0, 0, "", "");
            } else if (poic.contains("DMP")) {
                hasQueriedPoi = true;
                poic = poic.replace("DMP", "");
                //List<kmltest> kmltests = LitePal.where("xh = ?", poic).find(kmltest.class);
                List<DMPoint> dmbzList = LitePal.where("mapid = ?", poic).find(DMPoint.class);
                queriedPoi = new mPOIobj(poic, dmbzList.get(0).getLat(), dmbzList.get(0).getLng(), "", 0, 0, "", "");
            } else if (poic.contains("DML")) {
                hasQueriedLine = true;
                poic = poic.replace("DML", "");
                //List<kmltest> kmltests = LitePal.where("xh = ?", poic).find(kmltest.class);
                List<DMLine> dmLines = LitePal.where("mapid = ?", poic).find(DMLine.class);
                queriedLine = dmLines.get(0);
            }
        }
    }

    private void resumeVariableAndSurface() {
        resumeVariable();
        resumeSurface();
    }

    private void resumeSurface() {
        //String title = toolbar.getTitle().toString();
        Log.w(TAG, "resumeSurface: " + title);
        //String title = pdfFileName;
        if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
            toolbar.setTitle(title + "(正在记录轨迹)");
        } else toolbar.setTitle(title);
        if (isOpenWhiteBlank){

        }else {

        }
    }

    private void resumeVariable() {
        if (showMode == TuzhiEnum.CENTERMODE) {
            centerPoint.setVisibility(View.VISIBLE);
            isQuery = true;
            locError("中心点模式");
            query_poi_imgbt.setVisibility(View.INVISIBLE);
        } else {
            locError("不是中心点模式");
            centerPoint.setVisibility(View.INVISIBLE);
            isQuery = false;
            query_poi_imgbt.setVisibility(View.VISIBLE);
        }
        MessureChanged = false;
        isDrawType = TuzhiEnum.NONE_DRAW_TYPE;
        isMessureType = TuzhiEnum.MESSURE_NONE_TYPE;
    }

    private void registerSensor() {
        //注册传感器监听器
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void getBitmap() {
        getNormalBitmap();
        getDMBZBitmap();
        getDMitmap();
    }

    private void takePhoto() {
        File file2 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/photo");
        if (!file2.exists() && !file2.isDirectory()) {
            file2.mkdirs();
        }
        long timenow = System.currentTimeMillis();
        File outputImage = new File(Environment.getExternalStorageDirectory() + "/TuZhi/photo", Long.toString(timenow) + ".jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (Build.VERSION.SDK_INT >= 24) {
                //locError(Environment.getExternalStorageDirectory() + "/maphoto/" + Long.toString(timenow) + ".jpg");
                imageUri = FileProvider.getUriForFile(MainInterface.this, "com.android.tuzhi.fileprovider", outputImage);

            } else imageUri = Uri.fromFile(outputImage);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, EnumClass.TAKE_PHOTO);
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }

    }

    //PDF页面变化监控
    @Override
    public void onPageChanged(int page, int pageCount) {
        //pageNumber = page;
        //setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String findTitle(String str) {
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    //获取当前屏幕的参数
    private void getScreenParameter() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;
        //screen_height = 2960;
        float density = dm.density;
        int screenWidth = (int) (screen_width / density);
        int screenHeight = (int) (screen_height / density);

        //Log.d(TAG, Float.toString(screen_width) + "^" + Float.toString(screen_height) + "^" + Float.toString(screenWidth) + "^" + Float.toString(screenHeight));
    }

    //开始记录轨迹
    private void initTrail() {
        if (isGPSEnabled()) {
            locError("开始绘制轨迹");
        } else locError("请打开GPS功能");
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

    public double getMetric() {
        DisplayMetrics metric = getResources().getDisplayMetrics();
        return metric.density;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);

        Log.w(TAG, "onCreate: ");
        int num = receiveInfo();
        if (num != -1) {
            doSpecificOperation();
            initVariable(num);
            initWidget();
            displayFromFile(uri);
            Log.w(TAG, "resumeSurface: " + toolbar.getTitle().toString());
        } else {
            showLeaflets();
        }
    }

    @Override
    protected void onResume() {
        Log.w(TAG, "onResume: ");
        if (FILE_TYPE == TuzhiEnum.FILE_FILE_TYPE) {
            Log.w(TAG, "onResume: isOK");
            receiveQueryForMap();
            getScreenParameter();
            resumeVariableAndSurface();
            getBitmap();
            registerSensor();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (FILE_TYPE == TuzhiEnum.FILE_FILE_TYPE) {
            sensorManager.unregisterListener(listener);
        }
        title = toolbar.getTitle().toString();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (FILE_TYPE == TuzhiEnum.FILE_FILE_TYPE) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.w(TAG, e.toString());
            }
        }
        /*if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE){
            popBackWindow("Destroy");
        }else super.onDestroy();*/
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (FILE_TYPE == TuzhiEnum.FILE_FILE_TYPE) {
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
                    menu.findItem(R.id.back).setVisible(true);
                    menu.findItem(R.id.queryPOI).setVisible(true);
                    menu.findItem(R.id.action_search).setVisible(false);
                    menu.findItem(R.id.queryLatLng).setVisible(true);
                    break;
                case SEARCH_DEMO:
                    toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                    menu.findItem(R.id.back).setVisible(true);
                    menu.findItem(R.id.queryPOI).setVisible(false);
                    menu.findItem(R.id.queryLatLng).setVisible(false);
                    menu.findItem(R.id.action_search).setVisible(true);
                    if (esterEgg_redline) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainInterface.this);
                        dialog.setTitle("提示");
                        dialog.setMessage("需要进行什么查询?");
                        dialog.setCancelable(false);
                        dialog.setPositiveButton("生态保护红线查询", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                queryMode = TuzhiEnum.RED_LINE_QUERY;
                            }
                        });
                        dialog.setNegativeButton("简单查询", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                queryMode = TuzhiEnum.POI_QUERY;
                            }
                        });
                        dialog.show();
                    } else queryMode = TuzhiEnum.POI_QUERY;
                    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                    searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
                    // Assumes current activity is the searchable activity
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
                    searchView.setSubmitButtonEnabled(true);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (queryMode == TuzhiEnum.RED_LINE_QUERY) {
                                try {
                                    String[] str = query.split(";");
                                    if (!str[0].contains("°")) {
                                        float[] ptss = new float[2];
                                        for (int i = 0; i < str.length; ++i) {
                                            ptss[i] = Float.valueOf(str[i]);
                                        }
                                        LatLng lt = new LatLng(ptss[0], ptss[1]);
                                        int sizee = patchsForLatLng.size();
                                        boolean In = false;
                                        for (int a = 0; a < sizee; ++a) {
                                            if (LatLng.PtInPolygon(lt, patchsForLatLng.get(a))) {
                                                In = true;
                                                break;
                                            }
                                        }
                                        if (In)
                                            Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.InRedLine), Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.NoInRedLine), Toast.LENGTH_LONG).show();
                                        return true;
                                    } else {
                                        float[] ptss = new float[2];
                                        for (int i = 0; i < str.length; ++i) {
                                            ptss[i] = (float) getDFromDFM(str[i]);
                                        }
                                        LatLng lt = new LatLng(ptss[0], ptss[1]);
                                        int sizee = patchsForLatLng.size();
                                        boolean In = false;
                                        for (int a = 0; a < sizee; ++a) {
                                            if (LatLng.PtInPolygon(lt, patchsForLatLng.get(a))) {
                                                In = true;
                                                break;
                                            }
                                        }
                                        if (In)
                                            Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.InRedLine), Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.NoInRedLine), Toast.LENGTH_LONG).show();
                                        return true;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(MainInterface.this, MainInterface.this.getResources().getText(R.string.RedLineError), Toast.LENGTH_LONG).show();
                                    return true;
                                }
                            } else {
                                showListPopupWindow(searchView, query);
                                //Toast.makeText(MainInterface.this, "该功能正在开发当中!", Toast.LENGTH_LONG).show();
                                return true;
                            }
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return true;
                        }
                    });
            }
        } else {
            menu.findItem(R.id.queryPOI).setVisible(false);
            menu.findItem(R.id.queryLatLng).setVisible(false);
            menu.findItem(R.id.action_search).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //加载当前菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.queryPOI).setVisible(true);
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
                /*Intent intent = new Intent(MainInterface.this, info_page.class);
                intent.putExtra("extra_data", WKT);
                startActivity(intent);*/
                break;
            case R.id.queryLatLng:
                isDrawTrail = TuzhiEnum.SEARCH_DEMO;
                invalidateOptionsMenu();
                break;
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

    //获取文件管理器的返回信息
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == EnumClass.REQUEST_CODE_PHOTO) {
            theNum = 0;
            final Uri uri = data.getData();
            Log.w(TAG, "onActivityResult: " + DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri));
            final float[] latandlong = new float[2];
            try {
                ExifInterface exifInterface = new ExifInterface(DataUtil.getRealPathFromUriForPhoto(this, uri));
                exifInterface.getLatLong(latandlong);
                locError("ADDPHOTO" + String.valueOf(latandlong[0]) + "%" + String.valueOf(latandlong[1]));
                if (latandlong[0] != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainInterface.this);
                    builder.setTitle("提示");
                    builder.setMessage("请选择你要添加的图层");
                    builder.setNeutralButton(strings[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!esterEgg_lm) AddPhoto(uri, latandlong, 0);
                        }
                    });
                    builder.setNegativeButton(strings[1], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!esterEgg_lm) AddPhoto(uri, latandlong, 1);
                            else {
                                dmbzList = LitePal.findAll(DMBZ.class);
                                int size = dmbzList.size();
                                DMBZ dmbz = new DMBZ();
                                dmbz.setIMGPATH(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri));
                                dmbz.setLat(latandlong[0]);
                                dmbz.setLng(latandlong[1]);
                                dmbz.setXH(String.valueOf(size + 1));
                                dmbz.setTime(simpleDateFormat1.format(new Date(System.currentTimeMillis())));
                                dmbz.save();
                                getDMBZBitmap();
                                pdfView.zoomWithAnimation(c_zoom);
                            }
                        }
                    });
                    builder.setPositiveButton(strings[2], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!esterEgg_lm) AddPhoto(uri, latandlong, 2);
                        }
                    });
                    builder.show();
                } else
                    Toast.makeText(MainInterface.this, R.string.LocError1, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (resultCode == RESULT_OK && requestCode == EnumClass.REQUEST_CODE_TAPE) {
            final Uri uri = data.getData();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainInterface.this);
            builder.setTitle("提示");
            builder.setMessage("请选择你要添加的图层");
            builder.setNeutralButton(strings[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!esterEgg_lm) AddTape(uri, 0);
                }
            });
            builder.setNegativeButton(strings[1], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!esterEgg_lm) AddTape(uri, 1);
                    else {
                        dmbzList = LitePal.findAll(DMBZ.class);
                        int size = dmbzList.size();
                        DMBZ dmbz = new DMBZ();
                        dmbz.setTAPEPATH(DataUtil.getRealPathFromUriForPhoto(MainInterface.this, uri));
                        dmbz.setLat((float) m_lat);
                        dmbz.setLng((float) m_long);
                        dmbz.setXH(String.valueOf(size + 1));
                        dmbz.setTime(simpleDateFormat1.format(new Date(System.currentTimeMillis())));
                        dmbz.save();
                        getDMBZBitmap();
                        pdfView.zoomWithAnimation(c_zoom);
                    }
                }
            });
            builder.setPositiveButton(strings[2], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!esterEgg_lm) AddTape(uri, 2);
                }
            });
            builder.show();
        }
        if (resultCode == RESULT_OK && requestCode == EnumClass.TAKE_PHOTO) {
            theNum = 0;
            final String imageuri;
            if (Build.VERSION.SDK_INT >= 24) {
                imageuri = DataUtil.getRealPath(imageUri.toString());
            } else {
                imageuri = imageUri.toString().substring(7);
            }
            File file = new File(imageuri);
            if (file.exists()) {
                locError("imageUri : " + imageuri.toString());
                final float[] latandlong = new float[2];
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), imageuri, "title", "description");
                    // 最后通知图库更新
                    MainInterface.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imageuri)));
                    ExifInterface exifInterface = new ExifInterface(imageuri);
                    exifInterface.getLatLong(latandlong);
                    locError("see here" + String.valueOf(latandlong[0]) + "%" + String.valueOf(latandlong[1]));
                    //List<POI> POIs = LitePal.where("ic = ?", ic).find(POI.class);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainInterface.this);
                    builder.setTitle("提示");
                    builder.setMessage("请选择你要添加的图层");
                    builder.setNeutralButton(strings[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!esterEgg_lm) AddTakePhoto(imageuri, latandlong, 0);
                        }
                    });
                    builder.setNegativeButton(strings[1], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!esterEgg_lm) AddTakePhoto(imageuri, latandlong, 1);
                            else {
                                dmbzList = LitePal.findAll(DMBZ.class);
                                int size = dmbzList.size();
                                DMBZ dmbz = new DMBZ();
                                dmbz.setLat(latandlong[0]);
                                dmbz.setLng(latandlong[1]);
                                dmbz.setIMGPATH(imageuri);
                                dmbz.setXH(String.valueOf(size + 1));
                                dmbz.setTime(simpleDateFormat1.format(new Date(System.currentTimeMillis())));
                                dmbz.save();
                                getDMBZBitmap();
                                pdfView.zoomWithAnimation(c_zoom);
                            }
                        }
                    });
                    builder.setPositiveButton(strings[2], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!esterEgg_lm) AddTakePhoto(imageuri, latandlong, 2);
                        }
                    });
                    builder.show();

                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
            } else {
                file.delete();
                Toast.makeText(MainInterface.this, R.string.TakePhotoError, Toast.LENGTH_LONG).show();
            }
            locError(imageUri.toString());
            //String imageuri = getRealPathFromUriForPhoto(this, imageUri);

        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (isDrawTrail == TuzhiEnum.TRAIL_DRAW_TYPE) {
            popBackWindow("Back");
        } else super.onBackPressed();
    }
}



