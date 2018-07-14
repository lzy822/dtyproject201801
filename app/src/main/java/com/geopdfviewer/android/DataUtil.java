package com.geopdfviewer.android;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.icu.text.RelativeDateTimeFormatter;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {
    private static final String TAG = "DataUtil";
    public static Context mContext;

    //计算识别码
    public static String getPassword(String deviceId){
        //String password;
        //password = "l" + encryption(deviceId) + "ZY";

        return "l" + encryption(deviceId) + "ZY";
    }

    //计算识别码
    public static String getPassword1(String deviceId){
        //String password;
        //password = "l" + encryption(deviceId) + "ZY";
        SimpleDateFormat df = new SimpleDateFormat(MyApplication.getContext().getResources().getText(R.string.Date_1).toString());
        Date nowDate = new Date(System.currentTimeMillis());

        return encryption(deviceId) + encryption1(df.format(nowDate));
    }

    //编码
    public static String encryption(String password){
        password = password.replace("0", "q");
        password = password.replace("1", "R");
        password = password.replace("2", "V");
        password = password.replace("3", "z");
        password = password.replace("4", "T");
        password = password.replace("5", "b");
        password = password.replace("6", "L");
        password = password.replace("7", "s");
        password = password.replace("8", "W");
        password = password.replace("9", "F");
        password = password.replace("A", "d");
        password = password.replace("B", "o");
        password = password.replace("C", "O");
        password = password.replace("D", "n");
        password = password.replace("E", "v");
        password = password.replace("F", "C");
        return password;
    }

    //编码
    public static String encryption1(String password){
        password = password.replace("0", "a");
        password = password.replace("1", "Z");
        password = password.replace("2", "o");
        password = password.replace("3", "M");
        password = password.replace("4", "e");
        password = password.replace("5", "w");
        password = password.replace("6", "z");
        password = password.replace("7", "R");
        password = password.replace("8", "D");
        password = password.replace("9", "q");
        return password;
    }

    //反转授权码
    public static String reverseStr(String str){
        return str.substring(6, 10) + str.substring(0, 6) + str.substring(10);
    }

    //恢复反转授权码
    public static String reReverseStr(String str){
        return str.substring(4,10) + str.substring(0, 4) + str.substring(10);
    }

    //日期提取算法
    public static String getDateFromStr(String password){
        password = password.replace("a", "0");
        password = password.replace("Z", "1");
        password = password.replace("o", "2");
        password = password.replace("M", "3");
        password = password.replace("e", "4");
        password = password.replace("w", "5");
        password = password.replace("z", "6");
        password = password.replace("R", "7");
        password = password.replace("D", "8");
        password = password.replace("q", "9");
        password = password.substring(0, 4) + "年" + password.substring(4, 6) + "月" + password.substring(6, 8) + "日";
        return password;
    }

    //核对日期
    public static boolean verifyDate(String endDate){
        SimpleDateFormat df = new SimpleDateFormat(MyApplication.getContext().getResources().getText(R.string.Date).toString());
        Date nowDate = new Date(System.currentTimeMillis());
        Date endTimeDate = null;
        try {
            if (!endDate.isEmpty()){
                endTimeDate = df.parse(endDate);
            }
        }catch (ParseException e){
            Toast.makeText(MyApplication.getContext(), R.string.InputLicenseError + "_3", Toast.LENGTH_LONG).show();
        }
        if (nowDate.getTime() > endTimeDate.getTime()){
            return false;
        }else return true;
    }

    //日期加法
    public static String datePlus(String day, int days) {
        Log.w(TAG, "datePlus: " + day);
        SimpleDateFormat df = new SimpleDateFormat(MyApplication.getContext().getResources().getText(R.string.Date).toString());
        Date base = null;
        try {
            base = df.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DATE, days);
        String dateOK = df.format(cal.getTime());

        return dateOK;
    }

    //坐标飘移判断
    public static boolean isDrift(String LPTS){
        boolean isDrift = false;
        String[] LPTSStrings = LPTS.split(" ");
        //locError(Integer.toString(LPTSStrings.length));
        for (int i = 0; i < LPTSStrings.length; i++){
            //locError(LPTSStrings[i]);
            if (Float.valueOf(LPTSStrings[i]) != 0 && Float.valueOf(LPTSStrings[i]) != 1){
                isDrift = true;
                break;
            }
        }
        return isDrift;
    }

    //坐标拉伸算法
    public static String rubberCoordinate(String MediaBox, String BBox, String GPTS){
        String[] MediaBoxString = MediaBox.split(" ");
        String[] BBoxString = BBox.split(" ");
        String[] GPTSString = GPTS.split(" ");
        //将String 数组转换为 Float 数组
        float[] MediaBoxs = new float[MediaBoxString.length];
        float[] BBoxs = new float[BBoxString.length];
        float[] GPTSs = new float[GPTSString.length];
        for (int i = 0; i < MediaBoxString.length; i++) {
            MediaBoxs[i] = Float.valueOf(MediaBoxString[i]);
            //locError("MediaBoxs : " + MediaBoxs[i]);
        }
        for (int i = 0; i < BBoxString.length; i++) {
            BBoxs[i] = Float.valueOf(BBoxString[i]);
            //locError("BBoxs : " + Float.toString(BBoxs[i]));
        }
        //优化BBOX拉伸算法
        float del;
        /*if (BBoxs[0] > BBoxs[2]){
            del = BBoxs[0];
            BBoxs[0] = BBoxs[2];
            BBoxs[2] = del;
        }*/
        if (BBoxs[1] < BBoxs[3]){
            del = BBoxs[1];
            BBoxs[1] = BBoxs[3];
            BBoxs[3] = del;
        }
        //
        for (int i = 0; i < GPTSString.length; i++) {
            GPTSs[i] = Float.valueOf(GPTSString[i]);
        }

        if (Math.floor(MediaBoxs[2]) != Math.floor(BBoxs[2]) && Math.floor(MediaBoxs[3]) != Math.floor(BBoxs[3])) {
            PointF pt1_lb = new PointF(), pt1_lt = new PointF(), pt1_rt = new PointF(), pt1_rb = new PointF();
            PointF pt_lb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF(), pt_rb = new PointF();
            pt1_lb.x = BBoxs[0] / MediaBoxs[2];
            pt1_lb.y = BBoxs[3] / MediaBoxs[3];
            pt1_lt.x = BBoxs[0] / MediaBoxs[2];
            pt1_lt.y = BBoxs[1] / MediaBoxs[3];
            pt1_rt.x = BBoxs[2] / MediaBoxs[2];
            pt1_rt.y = BBoxs[1] / MediaBoxs[3];
            pt1_rb.x = BBoxs[2] / MediaBoxs[2];
            pt1_rb.y = BBoxs[3] / MediaBoxs[3];
            float lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            float long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
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
            GPTS = Float.toString(pt1_lb.x) + " " + Float.toString(pt1_lb.y) + " " + Float.toString(pt1_lt.x) + " " + Float.toString(pt1_lt.y) + " " + Float.toString(pt1_rt.x) + " " + Float.toString(pt1_rt.y) + " " + Float.toString(pt1_rb.x) + " " + Float.toString(pt1_rb.y);
            float delta_lat = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2, delta_long = ((pt_rb.y - pt_lb.y) + (pt_rt.y - pt_lt.y)) / 2;
            float delta_width = pt1_rb.x - pt1_lb.x, delta_height = pt1_lt.y - pt1_lb.y;
            pt_lb.x = pt_lb.x - (delta_lat / delta_height * (pt1_lb.y - 0));
            pt_lb.y = pt_lb.y - (delta_long / delta_width * (pt1_lb.x - 0));
            pt_lt.x = pt_lt.x - (delta_lat / delta_height * (pt1_lt.y - 1));
            pt_lt.y = pt_lt.y - (delta_long / delta_width * (pt1_lt.x - 0));
            pt_rb.x = pt_rb.x - (delta_lat / delta_height * (pt1_rb.y - 0));
            pt_rb.y = pt_rb.y - (delta_long / delta_width * (pt1_rb.x - 1));
            pt_rt.x = pt_rt.x - (delta_lat / delta_height * (pt1_rt.y - 1));
            pt_rt.y = pt_rt.y - (delta_long / delta_width * (pt1_rt.x - 1));
            /*m_center_x = ( pt_lb.x + pt_lt.x + pt_rb.x + pt_rt.x) / 4;
            m_center_y = ( pt_lb.y + pt_lt.y + pt_rb.y + pt_rt.y) / 4;*/
            //locError("GETGPTS: " + Double.toString(m_center_x));
            GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
        }else {
            /*m_center_x = ( GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            m_center_y = ( GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;*/
            //locError("GETGPTS: " + Double.toString(m_center_x));
        }
        return GPTS;

    }

    //获取GPTS值
    public static String getGPTS(String GPTS, String LPTS){
        //locError("看这里: " + " & LPTS " + LPTS);
        if (isDrift(LPTS) == true) {
            //locError("看这里: " + GPTS + " & LPTS " + LPTS);
            float lat_axis, long_axis;
            float lat_axis1, long_axis1;
            DecimalFormat df = new DecimalFormat("0.0");
            String[] GPTSStrings = GPTS.split(" ");
            String[] LPTSStrings = LPTS.split(" ");
            //将String 数组转换为 Float 数组
            float[] GPTSs = new float[GPTSStrings.length];
            float[] LPTSs = new float[LPTSStrings.length];
            for (int i = 0; i < LPTSStrings.length; i++) {
                LPTSs[i] = Float.valueOf(LPTSStrings[i]);
            }
            for (int i = 0; i < GPTSStrings.length; i++) {
                GPTSs[i] = Float.valueOf(GPTSStrings[i]);
            }
            //
            //构建两个矩形
            //构建经纬度矩形
            PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
            //PointF pt_lb1 = new PointF(), pt_rb1 = new PointF(), pt_lt1 = new PointF(), pt_rt1 = new PointF();
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
            //GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
            //locError(GPTS);
            //
            //构建LPTS 矩形
            //预处理LPTS
            for (int i = 0; i < LPTSs.length; i++){
                LPTSs[i] = Float.valueOf(df.format(LPTSs[i]));
            }
            //
            PointF pt_lb1 = new PointF(), pt_rb1 = new PointF(), pt_lt1 = new PointF(), pt_rt1 = new PointF();
            lat_axis1 = (LPTSs[0] + LPTSs[2] + LPTSs[4] + LPTSs[6]) / 4;
            long_axis1 = (LPTSs[1] + LPTSs[3] + LPTSs[5] + LPTSs[7]) / 4;
            for (int i = 0; i < LPTSs.length; i = i + 2){
                if (LPTSs[i] < lat_axis1) {
                    if (LPTSs[i + 1] < long_axis1){
                        pt_lb1.x = LPTSs[i];
                        pt_lb1.y = LPTSs[i + 1];
                    } else {
                        pt_rb1.x = LPTSs[i];
                        pt_rb1.y = LPTSs[i + 1];
                    }
                } else {
                    if (LPTSs[i + 1] < long_axis1){
                        pt_lt1.x = LPTSs[i];
                        pt_lt1.y = LPTSs[i + 1];
                    } else {
                        pt_rt1.x = LPTSs[i];
                        pt_rt1.y = LPTSs[i + 1];
                    }
                }
            }
            float delta_lat = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2, delta_long = ((pt_rb.y - pt_lb.y) + (pt_rt.y - pt_lt.y)) / 2;
            float delta_width = pt_rb1.y - pt_lb1.y, delta_height = pt_lt1.x - pt_lb1.x;
            pt_lb.x = pt_lb.x - (delta_lat / delta_height * (pt_lb1.x - 0));
            pt_lb.y = pt_lb.y - (delta_long / delta_width * (pt_lb1.y - 0));
            pt_lt.x = pt_lt.x - (delta_lat / delta_height * (pt_lt1.x - 1));
            pt_lt.y = pt_lt.y - (delta_long / delta_width * (pt_lt1.y - 0));
            pt_rb.x = pt_rb.x - (delta_lat / delta_height * (pt_rb1.x - 0));
            pt_rb.y = pt_rb.y - (delta_long / delta_width * (pt_rb1.y - 1));
            pt_rt.x = pt_rt.x - (delta_lat / delta_height * (pt_rt1.x - 1));
            pt_rt.y = pt_rt.y - (delta_long / delta_width * (pt_rt1.y - 1));
            GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
            //locError(GPTS);
            //
            //
        }
        //locError(Boolean.toString(isDrift));
        return GPTS;
    }

    //获取照片文件路径
    public static String getRealPathFromUriForPhoto(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //获取地图名称
    public static String findNameFromUri(Uri uri){
        int num;
        String str = "";
        num = appearNumber(uri.toString(), "/");
        try {
            String configPath = uri.toString();
            configPath = URLDecoder.decode(configPath, "utf-8");
            str = configPath;
            for (int i = 1; i <= num; i++){
                str = str.substring(str.indexOf("/") + 1);
            }
            str = str.substring(0, str.length() - 3);

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return str;

    }
    public static String findNamefromSample(String str){
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    //找到某字符在字符串中出现的次数
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    //获取File可使用路径
    public static String getRealPath(String filePath) {
        try {
            if (!filePath.contains("raw")) {
                String str = "content://com.android.tuzhi.fileprovider/external_files";
                String Dir = Environment.getExternalStorageDirectory().toString();
                filePath = Dir + filePath.substring(str.length());
            }else {
                filePath = filePath.substring(5);
                //locError("here");
                //locError(filePath);
            }
        }catch (Exception e){
            Log.w(TAG, e.toString());
        }

        return filePath;
    }

    //获取音频文件路径
    public static String getRealPathFromUriForAudio(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Audio.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //距离量测(输入参数为 两点的经纬度)
    public static double algorithm(double longitude1, double latitude1, double longitude2, double latitude2) {

        double Lat1 = rad(latitude1); // 纬度

        double Lat2 = rad(latitude2);

        double a = Lat1 - Lat2;//两点纬度之差

        double b = rad(longitude1) - rad(longitude2); //经度之差

        double s = 2 * Math.asin(Math

                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));//计算两点距离的公式

        s = s * 6378137.0;//弧长乘地球半径（半径为米）

        s = Math.round(s * 10000d) / 10000d;//精确距离的数值

        return s;

    }

    //将角度转化为弧度
    private static double rad(double d) {

        return d * Math.PI / 180.00; //角度转换成弧度

    }

    public static double[] getGPTS(String GPTS) {
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
        //w = ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2;
        //h = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2;
        double[] gpts = new double[]{(pt_lb.x + pt_rb.x) / 2, (pt_lt.x + pt_rt.x) / 2, (pt_lt.y + pt_lb.y) / 2, (pt_rt.y + pt_rb.y) / 2, ((pt_rt.y - pt_lt.y) + (pt_rb.y - pt_lb.y)) / 2, ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2};
        /*min_lat = (pt_lb.x + pt_rb.x) / 2;
        max_lat = (pt_lt.x + pt_rt.x) / 2;
        min_long = (pt_lt.y + pt_lb.y) / 2;
        max_long = (pt_rt.y + pt_rb.y) / 2;*/
        //locError(Double.toString(min_lat));
        //locError(Double.toString(max_lat));

        return gpts;
    }

    public static float[] getBox(String Box) {
        String[] BoxString = Box.split(" ");
        return new float[]{Float.valueOf(BoxString[0]), Float.valueOf(BoxString[1]), Float.valueOf(BoxString[2]), Float.valueOf(BoxString[3])};
    }

    //创建Thumbnails
    public static String getDtThumbnail(String fileName, String filePathMid, String filePathLast, int BitmapWidth, int BitmapHeight, int quality,  Activity activity){
        File file = new File(Environment.getExternalStorageDirectory() + filePathMid);
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        String outPath = Environment.getExternalStorageDirectory() + filePathMid + "/" + fileName + ".jpg";
        PdfiumCore pdfiumCore = new PdfiumCore(activity);
        int pageNum = 0;
        File m_pdf_file;
        try {
            m_pdf_file = new File(filePathLast);
            PdfDocument pdf = pdfiumCore.newDocument(ParcelFileDescriptor.open(m_pdf_file, ParcelFileDescriptor.MODE_READ_WRITE));
            pdfiumCore.openPage(pdf, pageNum);
            Bitmap bitmap = Bitmap.createBitmap(BitmapWidth, BitmapHeight, Bitmap.Config.RGB_565);
            pdfiumCore.renderPageBitmap(pdf, bitmap, pageNum, 0, 0, BitmapWidth, BitmapHeight);
            pdfiumCore.closeDocument(pdf);
            File of = new File(Environment.getExternalStorageDirectory() + filePathMid, fileName + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(of);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getContext(), R.string.CannotGetThumbnail, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return outPath;
    }

    //获取图片缩略图
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        //Log.w(TAG, "getImageThumbnail: " + Integer.toString(options.outWidth) + ";" + Integer.toString(options.outHeight) );
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }



    public static int getPicRotate(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static boolean PtInPolygon(LatLng point, List<LatLng> APoints) {
        int nCross = 0;
        for (int i = 0; i < APoints.size(); i++)   {
            LatLng p1 = APoints.get(i);
            LatLng p2 = APoints.get((i + 1) % APoints.size());
            // 求解 y=p.y 与 p1p2 的交点
            if ( p1.getLongitude() == p2.getLongitude())      // p1p2 与 y=p0.y平行
                continue;
            if ( point.getLongitude() <  Math.min(p1.getLongitude(), p2.getLongitude()))   // 交点在p1p2延长线上
                continue;
            if ( point.getLongitude() >= Math.max(p1.getLongitude(), p2.getLongitude()))   // 交点在p1p2延长线上
                continue;
            // 求交点的 X 坐标 --------------------------------------------------------------
            double x = (double)(point.getLongitude() - p1.getLongitude()) * (double)(p2.getLatitude() - p1.getLatitude()) / (double)(p2.getLongitude() - p1.getLongitude()) + p1.getLatitude();
            if ( x > point.getLatitude() )
                nCross++; // 只统计单边交点
        }
        // 单边交点为偶数，点在多边形之外 ---
        return (nCross % 2 == 1);
    }

    public static boolean isNumeric(String str) {
        // 该正则表达式可以匹配所有的数字 包括负数
        Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }

        Matcher isNum = pattern.matcher(bigStr); // matcher是全匹配
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean getKML(String filePath){
        File file = new File(filePath);
        InputStream in = null;
        int READ_TYPE;
        final int POI_TYPE = 0;
        final int NONE_TYPE = -1;
        int readingTable = 0, readingTd = 0;
        String coordinate = "";
        boolean readingTr = false;
        String title = "";
        String value = "";
        try {
            List<KeyAndValue> keyAndValues = new ArrayList<>();
            String line;
            in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            READ_TYPE = NONE_TYPE;
            while((line = bufferedReader.readLine()) != null) {
                if (line.contains("<table")) readingTable++;
                if (line.contains("</table")) readingTable--;
                if (readingTable == 0){
                    if (line.contains("<Point>")){
                        READ_TYPE = POI_TYPE;
                    }
                    /*if (line.contains("</Point>")){
                        READ_TYPE = NONE_TYPE;
                    }*/
                    if (READ_TYPE == POI_TYPE){
                        if (line.contains("<coordinates>")){
                            coordinate = line.substring(line.indexOf("<coordinates>") + 13, line.indexOf("</coordinates>"));
                            coordinate = coordinate.trim();
                            String[] coordinates = coordinate.split(",");
                            LatLng latLng = new LatLng(Float.valueOf(coordinates[0]), Float.valueOf(coordinates[1]));
                            READ_TYPE = NONE_TYPE;
                            kmltest kmltest = new kmltest();
                            DMBZ dmbz = new DMBZ();
                            int size = keyAndValues.size();
                            for (int i = 0; i < size; i++){
                                if (keyAndValues.get(i).getKey().equals("序号")) {
                                    kmltest.setXh(keyAndValues.get(i).getValue());
                                    dmbz.setXH(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("地名标准名称")) {
                                    kmltest.setDmbzmc(keyAndValues.get(i).getValue());
                                    dmbz.setBZMC(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("地名所在行政区_代码_")) {
                                    kmltest.setDmszxzqdm(keyAndValues.get(i).getValue());
                                    dmbz.setXZQDM(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("读音")) {
                                    kmltest.setDy(keyAndValues.get(i).getValue());
                                    dmbz.setDY(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("地名标志标准名称")) {
                                    kmltest.setDmbzbzmc(keyAndValues.get(i).getValue());
                                    dmbz.setBZMC(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("所在行政区")) {
                                    kmltest.setSzxzq(keyAndValues.get(i).getValue());
                                    dmbz.setXZQMC(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("设置单位")) {
                                    kmltest.setSzdw(keyAndValues.get(i).getValue());
                                    dmbz.setSZDW(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("生产厂家")) {
                                    kmltest.setSccj(keyAndValues.get(i).getValue());
                                    dmbz.setSCCJ(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("规格")) {
                                    kmltest.setGg(keyAndValues.get(i).getValue());
                                    dmbz.setGG(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("照片文件名")) {
                                    kmltest.setZp(keyAndValues.get(i).getValue());
                                    dmbz.setIMGPATH(keyAndValues.get(i).getValue().replace("JPG", "jpg"));
                                }
                            }
                            dmbz.setLat(Float.valueOf(coordinates[0]));
                            dmbz.setLng(Float.valueOf(coordinates[1]));
                            dmbz.save();
                            //kmltest.setLatLng(latLng);
                            plqyp plqyp1 = new plqyp();
                            plqyp1.setXh(kmltest.getXh());
                            if (!kmltest.getDy().isEmpty()) plqyp1.setYp(Environment.getExternalStorageDirectory() + "/地名标志录音/" + kmltest.getDy());
                            plqyp1.save();
                            String[] zps = new String[10];
                            String zpath = kmltest.getZp().replace("JPG", "jpg");
                            int jpgTime = appearNumber(zpath, ".jpg");
                            /*if (jpgTime == 2) {
                                zps[0] = kmltest.getZp().substring(0, (int) Math.floor(Float.valueOf(zpath.length() / 2)));
                                zps[1] = kmltest.getZp().substring((int) Math.ceil(Float.valueOf(zpath.length() / 2)) + 1, zpath.length());
                            }else if (jpgTime == 1) {
                                String str = kmltest.getZp();
                                zps[0] = str.substring(0, str.indexOf(".jpg") + 4);
                            }else if (jpgTime == 3) {
                                String str = kmltest.getZp();
                                zps[0] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[1] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[2] = str.substring(0, str.indexOf(".jpg") + 4);
                            }else if (jpgTime == 4) {
                                String str = kmltest.getZp();
                                zps[0] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[1] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[2] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[3] = str.substring(0, str.indexOf(".jpg") + 4);
                            }else if (jpgTime == 5){
                                String str = kmltest.getZp();
                                zps[0] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[1] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[2] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[3] = str.substring(0, str.indexOf(".jpg") + 4);
                                str = str.substring(0, str.indexOf(".jpg") + 5);
                                zps[4] = str.substring(0, str.indexOf(".jpg") + 4);
                            }*/
                            String str = kmltest.getZp();
                            for (int kk = 0; kk < jpgTime; kk++){
                                zps[kk] = str.substring(0, str.indexOf(".jpg") + 4);
                                if (kk != jpgTime - 1) str = str.substring(0, str.indexOf(".jpg") + 5);
                            }
                            plqzp plqzp1 = new plqzp();
                            plqzp1.setXh(kmltest.getXh());
                            if (jpgTime == 2) {
                                plqzp1.setZp1(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[0]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[1]);
                            }else if (jpgTime == 1) {
                                plqzp1.setZp1(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[0]);
                            }else if (jpgTime == 3){
                                plqzp1.setZp1(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[0]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[1]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[2]);
                            }else if (jpgTime == 4){
                                plqzp1.setZp1(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[0]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[1]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[2]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[3]);
                            }else {
                                plqzp1.setZp1(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[0]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[1]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[2]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[3]);
                                plqzp1.setZp2(Environment.getExternalStorageDirectory() + "/地名标志照片/" + zps[4]);
                            }
                            plqzp1.save();
                            kmltest.setLat(Float.valueOf(coordinates[1]));
                            kmltest.setLongi(Float.valueOf(coordinates[0]));
                            kmltest.save();
                        }
                    }
                }else if (readingTable == 2){
                    if (line.contains("<tr")){
                        readingTr = true;
                    }
                    if (readingTr){
                        if (line.contains("<td>")){
                            if (readingTd == 0){
                                title = line.substring(4, line.indexOf("</td>"));
                                Log.w(TAG, "title : " + title);
                            }else if (readingTd == 1){
                                value = line.substring(4, line.indexOf("</td>"));
                                Log.w(TAG, "value : " + value);
                                KeyAndValue keyAndValue = new KeyAndValue(title, value);
                                keyAndValues.add(keyAndValue);
                            }
                            readingTd++;
                        }
                        if (line.contains("</tr>")){
                            readingTr = false;
                            readingTd = 0;
                        }
                    }
                }
            }
            return true;
        }catch (Exception e){
            Log.w(TAG, "getKML: " + e.getLocalizedMessage());
            return false;
        }
    }

    public static boolean getKML1(String filePath){
        File file = new File(filePath);
        InputStream in = null;
        int READ_TYPE;
        final int POI_TYPE = 0;
        final int NONE_TYPE = -1;
        int readingTable = 0, readingTd = 0;
        String coordinate = "";
        boolean readingTr = false;
        String title = "";
        String value = "";
        try {
            List<KeyAndValue> keyAndValues = new ArrayList<>();
            String line;
            in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            READ_TYPE = NONE_TYPE;
            while((line = bufferedReader.readLine()) != null) {
                if (line.contains("<table")) readingTable++;
                if (line.contains("</table")) readingTable--;
                if (readingTable == 0){
                    if (line.contains("<Point>")){
                        READ_TYPE = POI_TYPE;
                    }
                    /*if (line.contains("</Point>")){
                        READ_TYPE = NONE_TYPE;
                    }*/
                    if (READ_TYPE == POI_TYPE){
                        if (line.contains("<coordinates>")){
                            coordinate = line.substring(line.indexOf("<coordinates>") + 13, line.indexOf("</coordinates>"));
                            coordinate = coordinate.trim();
                            String[] coordinates = coordinate.split(",");
                            LatLng latLng = new LatLng(Float.valueOf(coordinates[0]), Float.valueOf(coordinates[1]));
                            READ_TYPE = NONE_TYPE;
                            DMBZ dmbz = new DMBZ();
                            int size = keyAndValues.size();
                            for (int i = 0; i < size; i++){
                                if (keyAndValues.get(i).getKey().equals("序号")) {
                                    dmbz.setXH(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("地名标准名称")) {
                                    dmbz.setBZMC(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("地名所在行政区_代码_")) {
                                    dmbz.setXZQDM(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("读音")) {
                                    dmbz.setTAPEPATH(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("地名标志标准名称")) {
                                    dmbz.setBZMC(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("所在行政区")) {
                                    dmbz.setXZQMC(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("设置单位")) {
                                    dmbz.setSZDW(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("生产厂家")) {
                                    dmbz.setSCCJ(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("规格")) {
                                    dmbz.setGG(keyAndValues.get(i).getValue());
                                }
                                if (keyAndValues.get(i).getKey().equals("照片文件名")) {
                                    dmbz.setIMGPATH(keyAndValues.get(i).getValue().replace("JPG", "jpg"));
                                }
                            }
                            dmbz.setLat(Float.valueOf(coordinates[1]));
                            dmbz.setLng(Float.valueOf(coordinates[0]));
                            dmbz.save();
                            //kmltest.setLatLng(latLng);
                        }
                    }
                }else if (readingTable == 2){
                    if (line.contains("<tr")){
                        readingTr = true;
                    }
                    if (readingTr){
                        if (line.contains("<td>")){
                            if (readingTd == 0){
                                title = line.substring(4, line.indexOf("</td>"));
                                Log.w(TAG, "title : " + title);
                            }else if (readingTd == 1){
                                value = line.substring(4, line.indexOf("</td>"));
                                Log.w(TAG, "value : " + value);
                                KeyAndValue keyAndValue = new KeyAndValue(title, value);
                                keyAndValues.add(keyAndValue);
                            }
                            readingTd++;
                        }
                        if (line.contains("</tr>")){
                            readingTr = false;
                            readingTd = 0;
                        }
                    }
                }
            }
            return true;
        }catch (Exception e){
            Log.w(TAG, "getKML: " + e.getLocalizedMessage());
            return false;
        }
    }

    public static void addPhotoToDB(String path, String ic, String poic, String time){
        MPHOTO mphoto = new MPHOTO();
        mphoto.setPdfic(ic);
        mphoto.setPoic(poic);
        mphoto.setPath(path);
        mphoto.setTime(time);
        mphoto.save();
    }

    public static void addTapeToDB(String path, String ic, String poic, String time){
        MTAPE mtape = new MTAPE();
        mtape.setPath(path);
        mtape.setPdfic(ic);
        mtape.setPoic(poic);
        mtape.setTime(time);
        mtape.save();
    }

    public static void addPOI(String ic, String poic, String name, float x, float y, String time){
        String[] strings = MyApplication.getContext().getResources().getStringArray(R.array.Type);
        POI poi = new POI();
        poi.setIc(ic);
        if (name.contains("图片")) poi.setPhotonum(1);
        else if (name.contains("录音")) poi.setTapenum(1);
        poi.setPoic(poic);
        poi.setName(name);
        poi.setX(x);
        poi.setY(y);
        poi.setType(strings[0]);
        poi.setTime(time);
        poi.save();
    }

    public static void addPOI(String ic, String poic, String name, float x, float y, String time, int num){
        String[] strings = MyApplication.getContext().getResources().getStringArray(R.array.Type);
        POI poi = new POI();
        poi.setIc(ic);
        if (name.contains("图片")) poi.setPhotonum(1);
        else if (name.contains("录音")) poi.setTapenum(1);
        poi.setPoic(poic);
        poi.setName(name);
        poi.setX(x);
        poi.setY(y);
        poi.setType(strings[num]);
        poi.setTime(time);
        poi.save();
    }

    public static void makeKML(){
        List<File> files = new ArrayList<File>();
        //POI
        List<POI> pois = LitePal.findAll(POI.class);
        if (pois.size() > 0) {
            files.add(makePOIKML(pois));
        }
        //Trail
        //List<Trail> trails = LitePal.findAll(Trail.class);
        //if (trails.size() > 0) files.add(makeTrailKML(trails));
        //Lines_WhiteBlank
        //List<Lines_WhiteBlank> whiteBlanks = LitePal.findAll(Lines_WhiteBlank.class);
        //if (whiteBlanks.size() > 0) files.add(makeWhiteBlankKML(whiteBlanks));
    }

    public static String plusID(int num){
        String str = "";
        if (num >= 0 & num < 10) str = "0000" + String.valueOf(num);
        else if (num >= 10 & num < 100) str = "000" + String.valueOf(num);
        else if (num >= 100 & num < 1000) str = "00" + String.valueOf(num);
        else if (num >= 1000 & num < 10000) str = "0" + String.valueOf(num);
        else str = String.valueOf(num);
        return str;
    }

    public static StringBuffer makeKMLHead(StringBuffer sb, String str){
        sb = sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
        sb = sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").append("\n");
        sb = sb.append(" xsi:schemaLocation=\"http://www.opengis.net/kml/2.2 http://schemas.opengis.net/kml/2.2.0/ogckml22.xsd http://www.google.com/kml/ext/2.2 http://code.google.com/apis/kml/schema/kml22gx.xsd\">").append("\n");
        sb = sb.append("<Document id=\"" + str + "\">").append("\n");
        sb = sb.append("  ").append("<name>" + str + "</name>").append("\n");
        sb = sb.append("  ").append("<Snippet></Snippet>").append("\n");
        if (str.equals("WhiteBlank")) sb.append("  ").append("<description><![CDATA[界线]]></description>");
        sb = sb.append("  ").append("<Folder id=\"FeatureLayer0\">").append("\n");
        sb = sb.append("    ").append("<name>" + str + "</name>").append("\n");
        sb = sb.append("    ").append("<Snippet></Snippet>").append("\n");
        if (str.equals("WhiteBlank")) sb.append("    ").append("<description><![CDATA[界线]]></description>");
        return sb;
    }

    public static StringBuffer makeCDATAHead(StringBuffer sb){
        sb.append("      ").append("<description><![CDATA[<html xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:msxsl=\"urn:schemas-microsoft-com:xslt\">").append("\n");
        sb.append("\n");
        sb.append("<head>").append("\n");
        sb.append("\n");
        sb.append("<META http-equiv=\"Content-Type\" content=\"text/html\">").append("\n");
        sb.append("\n");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">").append("\n");
        sb.append("\n");
        sb.append("</head>").append("\n");
        sb.append("\n");
        sb.append("<body style=\"margin:0px 0px 0px 0px;overflow:auto;background:#FFFFFF;\">").append("\n");
        sb.append("\n");
        sb.append("<table style=\"font-family:Arial,Verdana,Times;font-size:12px;text-align:left;width:100%;border-collapse:collapse;padding:3px 3px 3px 3px\">").append("\n");
        sb.append("\n");
        sb.append("<tr style=\"text-align:center;font-weight:bold;background:#9CBCE2\">").append("\n");
        sb.append("\n");
        return sb;
    }

    public static StringBuffer makeCDATATail(StringBuffer sb){
        sb.append("</table>").append("\n");
        sb.append("\n");
        sb.append("</td>").append("\n");
        sb.append("\n");
        sb.append("</tr>").append("\n");
        sb.append("\n");
        sb.append("</table>").append("\n");
        sb.append("\n");
        sb.append("</body>").append("\n");
        sb.append("\n");
        sb.append("</html>").append("\n");
        sb.append("\n");
        sb.append("]]></description>").append("\n");
        return sb;
    }

    public static StringBuffer makeKMLTail(StringBuffer sb){
        sb.append("  ").append("</Folder>").append("\n");
        sb.append("  ").append("<Style id=\"IconStyle00\">").append("\n");
        sb.append("    ").append("<IconStyle>").append("\n");
        sb.append("      ").append("<Icon><href>Layer0_Symbol_2017ee40_0.png</href></Icon>").append("\n");
        sb.append("      ").append("<scale>0.250000</scale>").append("\n");
        sb.append("    ").append("</IconStyle>").append("\n");
        sb.append("    ").append("<LabelStyle>").append("\n");
        sb.append("      ").append("<color>00000000</color>").append("\n");
        sb.append("      ").append("<scale>0.000000</scale>").append("\n");
        sb.append("    ").append("</LabelStyle>").append("\n");
        sb.append("    ").append("<PolyStyle>").append("\n");
        sb.append("      ").append("<color>ff000000</color>").append("\n");
        sb.append("      ").append("<outline>0</outline>").append("\n");
        sb.append("    ").append("</PolyStyle>").append("\n");
        sb.append("  ").append("</Style>").append("\n");
        sb.append("</Document>").append("\n");
        sb.append("</kml>").append("\n");
        return sb;
    }

    public static StringBuffer makeKMLTailForLine(StringBuffer sb){
        sb.append("  ").append("</Folder>").append("\n");
        sb.append("  ").append("<Style id=\"LineStyle00\">").append("\n");
        sb.append("    ").append("<LabelStyle>").append("\n");
        sb.append("      ").append("<color>00000000</color>").append("\n");
        sb.append("      ").append("<scale>0.000000</scale>").append("\n");
        sb.append("    ").append("</LabelStyle>").append("\n");
        sb.append("    ").append("<LabelStyle>").append("\n");
        sb.append("      ").append("<color>00000000</color>").append("\n");
        sb.append("      ").append("<scale>0.000000</scale>").append("\n");
        sb.append("    ").append("</LabelStyle>").append("\n");
        sb.append("    ").append("<LineStyle>").append("\n");
        sb.append("      ").append("<color>ff005aad</color>").append("\n");
        sb.append("      ").append("<width>1.000000</width>").append("\n");
        sb.append("    ").append("</LineStyle>").append("\n");
        sb.append("  ").append("</Style>").append("\n");
        sb.append("</Document>").append("\n");
        sb.append("</kml>").append("\n");
        return sb;
    }

    public static File makePOIKML(final List<POI> pois){
        StringBuffer sb = new StringBuffer();
        int size_POI = pois.size();
        makeKMLHead(sb, "POI");
        for (int i = 0; i < size_POI; i++){
            sb.append("    ").append("<Placemark id=\"ID_").append(plusID(i)).append("\">").append("\n");
            sb.append("      ").append("<name>").append(pois.get(i).getPoic()).append("</name>").append("\n");
            sb.append("      ").append("<Snippet></Snippet>").append("\n");
            //属性表内容
            sb = makeCDATAHead(sb);
            sb.append("<td>").append(pois.get(i).getPoic()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            sb.append("<tr>").append("\n");
            sb.append("\n");
            sb.append("<td>").append("\n");
            sb.append("\n");
            sb.append("<table style=\"font-family:Arial,Verdana,Times;font-size:12px;text-align:left;width:100%;border-spacing:0px; padding:3px 3px 3px 3px\">").append("\n");
            sb.append("\n");
            //
            sb.append("<tr>").append("\n");
            sb.append("\n");
            sb.append("<td>").append("id").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getId()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("name").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getName()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("ic").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getIc()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("type").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getType()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("POIC").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getPoic()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("photoStr").append("</td>").append("\n");
            sb.append("\n");
            List<MPHOTO> mphotos = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MPHOTO.class);
            String photoStr = "";
            for (int j = 0; j < mphotos.size(); j++){
                if (j == 0){
                    photoStr = mphotos.get(j).getPath().substring(mphotos.get(j).getPath().lastIndexOf("/"), mphotos.get(j).getPath().length());
                }else photoStr = photoStr + "|" + mphotos.get(j).getPath().substring(mphotos.get(j).getPath().lastIndexOf("/") + 1, mphotos.get(j).getPath().length());
            }
            sb.append("<td>").append(photoStr).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("description").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getDescription()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("tapeStr").append("</td>").append("\n");
            sb.append("\n");
            List<MTAPE> mtapes = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MTAPE.class);
            String tapeStr = "";
            for (int j = 0; j < mtapes.size(); j++){
                if (j == 0){
                    tapeStr = mtapes.get(j).getPath().substring(mtapes.get(j).getPath().lastIndexOf("/"), mtapes.get(j).getPath().length());
                }else tapeStr = tapeStr + "|" + mtapes.get(j).getPath().substring(mtapes.get(j).getPath().lastIndexOf("/") + 1, mtapes.get(j).getPath().length());
            }
            sb.append("<td>").append(tapeStr).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("time").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getTime()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("x").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getX()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            //
            sb.append("<tr bgcolor=\"#D4E4F3\">").append("\n");
            sb.append("\n");
            sb.append("<td>").append("y").append("</td>").append("\n");
            sb.append("\n");
            sb.append("<td>").append(pois.get(i).getY()).append("</td>").append("\n");
            sb.append("\n");
            sb.append("</tr>").append("\n");
            sb.append("\n");
            //
            sb = makeCDATATail(sb);
            sb.append("      ").append("<styleUrl>#IconStyle00</styleUrl>").append("\n");
            sb.append("      ").append("<Point>").append("\n");
            sb.append("        ").append("<altitudeMode>clampToGround</altitudeMode>").append("\n");
            sb.append("        ").append("<coordinates>").append(" ").append(pois.get(i).getY()).append(",").append(pois.get(i).getX()).append(",").append(0).append("</coordinates>").append("\n");
            sb.append("      ").append("</Point>").append("\n");
            sb.append("    ").append("</Placemark>").append("\n");
            //
        }
        sb = makeKMLTail(sb);
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  outputPath + ".kml");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        }catch (IOException e){
            Log.w(TAG, e.toString());
        }
        return file1;
    }

    public static File makeTapeKML(final List<MTAPE> mtapes, List<File> files){
        StringBuffer sb = new StringBuffer();
        int size_mtape = mtapes.size();
        makeKMLHead(sb, "MTAPE");
        for (int i = 0; i < size_mtape; i++){
            sb.append("<id>").append(mtapes.get(i).getId()).append("</id>").append("\n");
            sb.append("<pdfic>").append(mtapes.get(i).getPdfic()).append("</pdfic>").append("\n");
            sb.append("<POIC>").append(mtapes.get(i).getPoic()).append("</POIC>").append("\n");
            String path = mtapes.get(i).getPath();
            sb.append("<path>").append(path).append("</path>").append("\n");
            files.add(new File(path));
            sb.append("<time>").append(mtapes.get(i).getTime()).append("</time>").append("\n");
        }
        sb.append("</MTAPE>").append("\n");
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  outputPath + ".dtdb");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        }catch (IOException e){
            Log.w(TAG, e.toString());
        }
        return file1;
    }

    public static File makePhotoKML(final List<MPHOTO> mphotos, List<File> files){
        StringBuffer sb = new StringBuffer();
        int size_mphoto = mphotos.size();
        makeKMLHead(sb, "MPHOTO");
        for (int i = 0; i < size_mphoto; i++){
            sb.append("<id>").append(mphotos.get(i).getId()).append("</id>").append("\n");
            sb.append("<pdfic>").append(mphotos.get(i).getPdfic()).append("</pdfic>").append("\n");
            sb.append("<POIC>").append(mphotos.get(i).getPoic()).append("</POIC>").append("\n");
            String path = mphotos.get(i).getPath();
            sb.append("<path>").append(path).append("</path>").append("\n");
            files.add(new File(path));
            sb.append("<time>").append(mphotos.get(i).getTime()).append("</time>").append("\n");
        }
        sb.append("</MPHOTO>").append("\n");
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  outputPath + ".dtdb");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        }catch (IOException e){
            Log.w(TAG, e.toString());
        }
        return file1;
    }

    public static File makeTrailKML(final List<Trail> trails){
        StringBuffer sb = new StringBuffer();
        int size_trail = trails.size();
        makeKMLHead(sb, "Trail");
        for (int i = 0; i < size_trail; i++){
            sb.append("<id>").append(trails.get(i).getId()).append("</id>").append("\n");
            sb.append("<ic>").append(trails.get(i).getIc()).append("</ic>").append("\n");
            sb.append("<name>").append(trails.get(i).getName()).append("</name>").append("\n");
            sb.append("<path>").append(trails.get(i).getPath()).append("</path>").append("\n");
            sb.append("<starttime>").append(trails.get(i).getStarttime()).append("</starttime>").append("\n");
            sb.append("<endtime>").append(trails.get(i).getEndtime()).append("</endtime>").append("\n");
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  outputPath + ".dtdb");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        }catch (IOException e){
            Log.w(TAG, e.toString());
        }
        return file1;
    }

    public static void makeWhiteBlankKML(){
        final List<Lines_WhiteBlank> whiteBlanks = LitePal.findAll(Lines_WhiteBlank.class);
        StringBuffer sb = new StringBuffer();
        int size_whiteBlanks = whiteBlanks.size();
        makeKMLHead(sb, "WhiteBlank");
        for (int i = 0; i < size_whiteBlanks; i++){
            sb.append("    ").append("<Placemark id=\"ID_").append(plusID(i)).append("\">").append("\n");
            sb.append("      ").append("<name>").append(whiteBlanks.get(i).getIc()).append("</name>").append("\n");
            sb.append("      ").append("<Snippet></Snippet>").append("\n");
            //属性表内容
            sb = makeCDATAHead(sb);
            sb = makeCDATATail(sb);
            sb.append("      ").append("<styleUrl>#LineStyle00</styleUrl>").append("\n");
            sb.append("      ").append("<MultiGeometry>").append("\n");
            sb.append("        ").append("<LineString>").append("\n");
            sb.append("          ").append("<extrude>0</extrude>").append("\n");
            sb.append("          ").append("<tessellate>1</tessellate><altitudeMode>clampToGround</altitudeMode>").append("\n");
            String[] lines_str = whiteBlanks.get(i).getLines().split(" ");
            float[] lats = new float[lines_str.length / 2];
            float[] lngs = new float[lines_str.length / 2];
            for (int k = 0; k < lines_str.length; k++){
                if (k == 0 || (k % 2 == 0)) {
                    lats[k / 2] = Float.valueOf(lines_str[k]);
                }
                else {
                    lngs[k / 2] = Float.valueOf(lines_str[k]);
                }
            }
            StringBuffer str = new StringBuffer();
            for (int k = 0; k < lngs.length; k++) {
                str.append(" ").append(Float.toString(lngs[k])).append(",").append(Float.toString(lats[k])).append(",").append("0");
            }
            sb.append("          ").append("<coordinates>").append(str).append("</coordinates>").append("\n");
            sb.append("        ").append("</LineString>").append("\n");
            sb.append("      ").append("</MultiGeometry>").append("\n");
            sb.append("    ").append("</Placemark>").append("\n");
            //
        }
        sb = makeKMLTailForLine(sb);
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()){
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output",  "白板" + outputPath + ".kml");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        }catch (IOException e){
            Log.w(TAG, e.toString());
        }
    }

    public static StringBuffer makeTxtHead(StringBuffer sb){
        sb = sb.append("ic").append(";");
        sb = sb.append("name").append(";");
        sb = sb.append("poic").append(";");
        sb = sb.append("photo").append(";");
        sb = sb.append("tape").append(";");
        sb = sb.append("description").append(";");
        sb = sb.append("time").append(";");
        sb = sb.append("type").append(";");
        sb = sb.append("x").append(";");
        sb = sb.append("y").append("\n");
        return sb;
    }

    public static StringBuffer makeTxtHead1(StringBuffer sb){
        sb = sb.append("XH").append(";");
        sb = sb.append("DY").append(";");
        sb = sb.append("MC").append(";");
        sb = sb.append("BZMC").append(";");
        sb = sb.append("XZQMC").append(";");
        sb = sb.append("XZQDM").append(";");
        sb = sb.append("SZDW").append(";");
        sb = sb.append("SCCJ").append(";");
        sb = sb.append("GG").append(";");
        sb = sb.append("x").append(";");
        sb = sb.append("y").append(";");
        sb = sb.append("IMGPATH").append("\n");
        return sb;
    }

    public static void makeTxt(String type){
        try {
            final List<POI> pois = LitePal.where("type = ?", type).find(POI.class);
            Log.w(TAG, "makeTxt: " + pois.size());
            StringBuffer sb = new StringBuffer();
            int size_POI = pois.size();
            sb = makeTxtHead(sb);
            for (int i = 0; i < size_POI; i++) {
                //属性表内容
                sb.append(pois.get(i).getIc()).append(";").append(pois.get(i).getName()).append(";").append(pois.get(i).getPoic()).append(";");
                List<MPHOTO> mphotos = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MPHOTO.class);
                String photoStr = "";
                for (int j = 0; j < mphotos.size(); j++) {
                    if (j == 0) {
                        photoStr = mphotos.get(j).getPath().substring(mphotos.get(j).getPath().lastIndexOf("/") + 1, mphotos.get(j).getPath().length());
                    } else
                        photoStr = photoStr + "|" + mphotos.get(j).getPath().substring(mphotos.get(j).getPath().lastIndexOf("/") + 1, mphotos.get(j).getPath().length());
                }
                photoStr = URLDecoder.decode(photoStr, "utf-8");
                sb.append(photoStr).append(";");
                List<MTAPE> mtapes = LitePal.where("poic = ?", pois.get(i).getPoic()).find(MTAPE.class);
                String tapeStr = "";
                for (int j = 0; j < mtapes.size(); j++) {
                    if (j == 0) {
                        tapeStr = mtapes.get(j).getPath().substring(mtapes.get(j).getPath().lastIndexOf("/") + 1, mtapes.get(j).getPath().length());
                    } else
                        tapeStr = tapeStr + "|" + mtapes.get(j).getPath().substring(mtapes.get(j).getPath().lastIndexOf("/") + 1, mtapes.get(j).getPath().length());
                }
                tapeStr = URLDecoder.decode(tapeStr, "utf-8");
                sb.append(tapeStr).append(";").append(pois.get(i).getDescription()).append(";").append(pois.get(i).getTime()).append(";").append(pois.get(i).getType()).append(";").append(pois.get(i).getY()).append(";").append(pois.get(i).getX()).append("\n");
            }
            makeFile(sb, type);
        }catch (UnsupportedEncodingException e){
            Log.w(TAG, e.toString());
        }
    }

    public static void makeTxt1(){
        try {
            final List<DMBZ> pois = LitePal.findAll(DMBZ.class);
            Log.w(TAG, "makeTxt: " + pois.size());
            StringBuffer sb = new StringBuffer();
            int size_POI = pois.size();
            sb = makeTxtHead1(sb);
            for (int i = 0; i < size_POI; i++) {
                //属性表内容
                sb.append(pois.get(i).getXH()).append(";").append(pois.get(i).getDY()).append(";").append(pois.get(i).getMC()).append(";").append(pois.get(i).getBZMC()).append(";").append(pois.get(i).getXZQMC()).append(";").append(pois.get(i).getXZQDM()).append(";").append(pois.get(i).getSZDW()).append(";").append(pois.get(i).getSCCJ()).append(";").append(pois.get(i).getGG()).append(";").append(pois.get(i).getIMGPATH()).append(";").append(pois.get(i).getLng()).append(";");
                sb.append(pois.get(i).getLat()).append("\n");
            }
            makeFile1(sb);
        }catch (Exception e){
            Log.w(TAG, e.toString());
        }
    }

    public static void makeFile(StringBuffer sb, String type){
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output", "DMBZ" + type + outputPath + ".txt");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        } catch (IOException e) {
            Log.w(TAG, e.toString());
        }
    }

    public static void makeFile1(StringBuffer sb){
        File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output");
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
        String outputPath = Long.toString(System.currentTimeMillis());
        File file1 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/" + "/Output", "DMBZ" + outputPath + ".txt");
        try {
            FileOutputStream of = new FileOutputStream(file1);
            of.write(sb.toString().getBytes());
            of.close();
        } catch (IOException e) {
            Log.w(TAG, e.toString());
        }
    }

    public static String[] bubbleSort(String[] arr) {
        int len = arr.length;
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < len - 1 - i; j++) {
                if (arr[j].toUpperCase().charAt(0) > arr[j + 1].toUpperCase().charAt(0)) {        // 相邻元素两两对比
                    String temp = arr[j+1];        // 元素交换
                    arr[j+1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        return arr;
    }

}
