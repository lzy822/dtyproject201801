package com.geopdfviewer.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.geopdfviewer.android.DataUtil.renamePath1;

public class JZActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener, OnDrawListener, OnTapListener {

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

    private TuzhiEnum isCoordinateType = TuzhiEnum.COORDINATE_DEFAULT_TYPE;

    PDFView pdfView;
    //声明中心点的位置坐标
    PointF centerPointLoc;
    //记录当前显示模式
    private TuzhiEnum showMode = TuzhiEnum.CENTERMODE;
    double w, h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jz);

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

    public void initWidget() {
        try {
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
            if (maps.get(i).getMaptype() == EnumClass.ROOTMAP)
            {
                currentMap = maps.get(i);
                break;
            }
        }
    }

    public void showMap(){
        try {
            manageInfo(currentMap.getGpts());
            displayFromFile(currentMap.getUri());
        }catch (NullPointerException e){
            displayFromFile("/storage/emulated/0/tencent/TIMfile_recv/cangyuan.dt");
        }
    }

    @TargetApi(21)
    public static void setStatusBarColor(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.BLACK);
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
        current_pageheight = pageHeight;
        current_pagewidth = pageWidth;
        viewer_height = pdfView.getHeight();
        viewer_width = pdfView.getWidth();
        getCurrentScreenLoc();
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
    private void manageInfo(String GPTS) {
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

    private void displayFromFile(String filePath) {
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
