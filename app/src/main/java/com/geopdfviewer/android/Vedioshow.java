package com.geopdfviewer.android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.geopdfviewer.android.CameraUtils.RequestCode.FLAG_REQUEST_CAMERA_IMAGE;
import static com.geopdfviewer.android.EnumClass.TAKE_PHOTO;

public class Vedioshow extends AppCompatActivity {
    private static final String TAG = "Vedioshow";

    private ImageView iv;

    private VideoView videoView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedioshow);
        iv = (ImageView) findViewById(R.id.iv);
        videoView = (VideoView) findViewById(R.id.video);
    }

    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.btn_pick_image:
                // TODO 选取系统相册图片
                doPickImageFromSystem();
                break;
            case R.id.btn_open_camera_for_image:
                // TODO 打开相机拍照
                doOpenCameraForImage();
                break;
            case R.id.btn_open_camera_for_video:
                // TODO 打开相机录像
                doOpenCameraForVideo();
                break;
        }
    }

    private void doPickImageFromSystem() {
        pickVedio(this);
    }

    private void doOpenCameraForImage() {

        //CameraUtils.openCameraForImage(this);
        takePhoto();
    }

    private void doOpenCameraForVideo() {

        //CameraUtils.openCameraForVideo(this);
        takeVedio(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 118:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, Vedioshow.this.getResources().getText(R.string.PermissionError), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }else {
                            takeVedio();
                        }
                    }
                }
                break;
            case 119:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, Vedioshow.this.getResources().getText(R.string.PermissionError), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }else {
                            pickVedio();
                        }
                    }
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //当结果码为RESULT_OK时,表示用户有效
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CameraUtils.RequestCode.FLAG_REQUEST_SYSTEM_IMAGE:
                    /*String path = CameraUtils.getImagePathFromSystem(this, data);
                    if (path != null) {
                        System.out.println("选取到图片的回参" + path);
                        iv.setImageBitmap(BitmapFactory.decodeFile(path));   //这里在报open failed: EACCES (Permission denied)   已加权限和            android:requestLegacyExternalStorage="true"    还要获取动态权限  见上一步

                    }*/
                    Uri uri = data.getData();
                    ResultForPickVedio(uri);

                    String path = DataUtil.getRealPathFromUriForVedio(this, uri);
                    File file = new File(path);
                    /*try {
                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                        int degree = DataUtil.getPicRotate(path);
                        if (degree != 0) {
                            Matrix m = new Matrix();
                            m.setRotate(degree); // 旋转angle度
                            Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                        }
                        iv.setImageBitmap(bitmap);
                    }
                    catch (Exception e){

                    }*/
                    Uri uri1 = data.getData();
                    String uriString = uri1.toString();
                    /*try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(path);
                        Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        int degree = DataUtil.getPicRotate(path);
                        if (degree != 0) {
                            Matrix m = new Matrix();
                            m.setRotate(degree); // 旋转angle度
                            Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                        }
                        iv.setImageBitmap(bitmap);
                    }
                    catch (Exception e){

                    }
                    videoView.setVideoPath(uriString);//setVideoURI(Uri.parse(uriString));
                    videoView.start();
                    Toast.makeText(Vedioshow.this, data.getDataString(), Toast.LENGTH_LONG).show();*/
                    break;
                case FLAG_REQUEST_CAMERA_IMAGE:
                    //TODO 处理从相机返回的图片数据
                    Toast.makeText(Vedioshow.this, imageUri.toString(), Toast.LENGTH_LONG).show();
                    String imageuri;
                    if (Build.VERSION.SDK_INT >= 24) {
                        imageuri = DataUtil.getRealPath(imageUri.toString());
                    }else {
                        imageuri = imageUri.toString().substring(7);
                    }
                    videoView.setVideoPath(imageuri);//setVideoURI(Uri.parse(uriString));
                    videoView.start();
                    try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(imageuri);
                        Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    int degree = DataUtil.getPicRotate(imageuri);
                    if (degree != 0) {
                        Matrix m = new Matrix();
                        m.setRotate(degree); // 旋转angle度
                        Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                    }
                    iv.setImageBitmap(bitmap);
                }
                catch (Exception e){

                }
                    /*Log.w(TAG, "takePhoto: " + imageuri.toString());
                    file = new File(imageuri);
                    if (file.length() != 0) {
                        try {
                            MediaStore.Images.Media.insertImage(getContentResolver(), imageuri, "title", "description");
                            // 最后通知图库更新
                            Vedioshow.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imageuri)));
                        } catch (IOException e) {
                        }
                    }

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                        int degree = DataUtil.getPicRotate(imageuri);
                        if (degree != 0) {
                            Matrix m = new Matrix();
                            m.setRotate(degree); // 旋转angle度
                            Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                        }
                        iv.setImageBitmap(bitmap);
                    }
                    catch (Exception e){

                    }*/

                    /*if (data != null) {
                        Bitmap bm = data.getParcelableExtra("data");

                        iv.setImageBitmap(bm);
                    } else {
                        Bitmap bm = BitmapFactory.decodeFile(new File(imageUri));
                        iv.setImageBitmap(bm);
                    }*/

                    break;
                case CameraUtils.RequestCode.FLAG_REQUEST_CAMERA_VIDEO:
                    //TODO 处理从相机返回的视频数据
                    /*Uri uri1 = data.getData();
                    String uriString = uri1.toString();
                    videoView.setVideoURI(Uri.parse(uriString));
                    videoView.start();
                    Toast.makeText(Vedioshow.this, data.getDataString(), Toast.LENGTH_LONG).show();*/
                    break;
            }

        }
    }

    public void ResultForPickVedio(Uri uri){

        try {
            String path = DataUtil.getRealPathFromUriForVedio(this, uri);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            int degree = DataUtil.getPicRotate(path);
            if (degree != 0) {
                Matrix m = new Matrix();
                m.setRotate(degree); // 旋转angle度
                Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            }
            iv.setImageBitmap(bitmap);
            videoView.setVideoPath(path);//setVideoURI(Uri.parse(uriString));
            videoView.start();
            Toast.makeText(Vedioshow.this, path, Toast.LENGTH_LONG).show();
        }
        catch (Exception e){

        }
    }

    public static void pickVedioFromSystem(Activity activity)
    {
        verifyStoragePermissions(activity);   //在这里手动获取权限   我不知道是我真机MIUI12的问题还是所有android6上下  和android10上下都有

        /**
         * 参数一:打开系统相册的ACTION 参数二:返回数据的方式(从系统相册的数据库获取)
         */
    }

    private Uri imageUri = null;
    private void takePhoto(){
        File file2 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/photo");
        if (!file2.exists() && !file2.isDirectory()){
            file2.mkdirs();
        }
        long timenow = System.currentTimeMillis();
        File outputImage = new File(Environment.getExternalStorageDirectory() + "/TuZhi/photo", Long.toString(timenow) + ".jpg");
        try {
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24){
            //locError(Environment.getExternalStorageDirectory() + "/maphoto/" + Long.toString(timenow) + ".jpg");
            imageUri = FileProvider.getUriForFile(Vedioshow.this, "com.android.tuzhi.fileprovider", outputImage);

        }else imageUri = Uri.fromFile(outputImage);
        Log.w(TAG, "takePhoto: " + imageUri.toString());
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, FLAG_REQUEST_CAMERA_IMAGE);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        System.out.println("检测权限有没有"+permission);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            //动态获取权限
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static void verifyCameraPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);
        System.out.println("检测拍照权限有没有"+permission);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            //动态获取权限
            ActivityCompat.requestPermissions(activity,PERMISSIONS_CAMERA ,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public static void verifyVedioPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);
        System.out.println("检测拍照权限有没有"+permission);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            //动态获取权限
            ActivityCompat.requestPermissions(activity,PERMISSIONS_VIDEO ,
                    REQUEST_EXTERNAL_STORAGE);
        }
        else{

        }
    }

    public void pickVedio(Activity activity){

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(activity, permissions, 119);
        }else {
            pickVedio();
        }
    }

    private void pickVedio(){
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(intent,
                CameraUtils.RequestCode.FLAG_REQUEST_SYSTEM_IMAGE);
    }

    public void takeVedio(Activity activity){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(activity, permissions, 118);
        }else {
            takeVedio();
        }
    }

    private void takeVedio(){
        File file2 = new File(Environment.getExternalStorageDirectory() + "/TuZhi/vedio");
        if (!file2.exists() && !file2.isDirectory()){
            file2.mkdirs();
        }
        long timenow = System.currentTimeMillis();
        File outputImage = new File(Environment.getExternalStorageDirectory() + "/TuZhi/vedio", Long.toString(timenow) + ".mp4");
        try {
            if (outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24){
            //locError(Environment.getExternalStorageDirectory() + "/maphoto/" + Long.toString(timenow) + ".jpg");
            imageUri = FileProvider.getUriForFile(Vedioshow.this, "com.android.tuzhi.fileprovider", outputImage);

        }else imageUri = Uri.fromFile(outputImage);
        Log.w(TAG, "takeVedio: " + imageUri.toString());
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, FLAG_REQUEST_CAMERA_IMAGE);
    }

    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
    };

    private static String[] PERMISSIONS_VIDEO = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
}
