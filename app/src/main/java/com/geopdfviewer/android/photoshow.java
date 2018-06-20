package com.geopdfviewer.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class photoshow extends AppCompatActivity {

    private static final String TAG = "photoshow";
    private String POIC;
    private List<mPhotobj> mPhotobjList = new ArrayList<>();
    private mPhotobjAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private String deletePath;
    private final static int REQUEST_CODE_PHOTO = 42;
    private final static int TAKE_PHOTO = 41;
    private int isLongClick = 1;
    Toolbar toolbar;
    Uri imageUri;
    private List<bt> bts;
    boolean isCreateBitmap = false;

    private void refreshCard(){
        mPhotobjList.clear();
        List<MPHOTO> mphotos = LitePal.where("poic = ?", POIC).find(MPHOTO.class);
        for (MPHOTO mphoto : mphotos){
            mPhotobj mphotobj = new mPhotobj(mphoto.getPoic(), mphoto.getPoic(), mphoto.getTime(), mphoto.getPath());
            mPhotobjList.add(mphotobj);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_photo);
        layoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new mPhotobjAdapter(mPhotobjList);
        adapter.setOnItemLongClickListener(new mPhotobjAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, String path, String time) {
                setTitle(photoshow.this.getResources().getText(R.string.IsLongClicking));
                //deletePath = path;
                deletePath = time;
                isLongClick = 0;
                invalidateOptionsMenu();
            }
        });
        adapter.setOnItemClickListener(new mPhotobjAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(View view, String path, int position, String time) {
                mPhotobjAdapter.ViewHolder holder = new mPhotobjAdapter.ViewHolder(view);
                if (isLongClick == 0){
                    if (holder.cardView.getCardBackgroundColor().getDefaultColor() != Color.GRAY){
                        holder.cardView.setCardBackgroundColor(Color.GRAY);
                        //deletePath = deletePath + "wslzy" + path;
                        deletePath = deletePath + "wslzy" + time;
                    }else {
                        holder.cardView.setCardBackgroundColor(Color.WHITE);
                        if (deletePath.contains("wslzy")) {
                            //String replace = "wslzy" + path;
                            String replace = "wslzy" + time;
                            deletePath = deletePath.replace(replace, "");
                            if (deletePath.length() == deletePath.replace(replace, "").length()){
                                //String replace1 = path + "wslzy";
                                String replace1 = time + "wslzy";
                                deletePath = deletePath.replace(replace1, "");
                            }
                        }else {
                            resetView();
                        }
                    }
                }else {
                    //Log.w(TAG, "onItemClick: " + path );
                    if (isCreateBitmap) showPopueWindowForPhoto(path);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        switch (isLongClick){
            case 1:
                toolbar.setBackgroundColor(Color.rgb(63, 81, 181));
                menu.findItem(R.id.deletepoi).setVisible(false);
                menu.findItem(R.id.restore_pois).setVisible(false);
                break;
            case 0:
                toolbar.setBackgroundColor(Color.rgb(233, 150, 122));
                menu.findItem(R.id.back_pois).setVisible(false);
                menu.findItem(R.id.deletepoi).setVisible(true);
                menu.findItem(R.id.restore_pois).setVisible(true);
                menu.findItem(R.id.add_pois).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoshow);
        bts = new ArrayList<bt>();
        /////////////////////////
        //声明ToolBar
        toolbar = (Toolbar) findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        setTitle(photoshow.this.getResources().getText(R.string.PhotoList));
        Intent intent = getIntent();
        POIC = intent.getStringExtra("POIC");
        new Thread(new Runnable() {
            @Override
            public void run() {
                bts.clear();
                List<MPHOTO> mphotos = LitePal.where("poic = ?", POIC).find(MPHOTO.class);
                int size = mphotos.size();
                for (int i = 0; i < size; i++) {
                    String path = mphotos.get(i).getPath();
                    File file = new File(path);
                    if (file.exists()) {
                        Bitmap bitmap = DataUtil.getImageThumbnail(path, 2048, 2048);
                        int degree = DataUtil.getPicRotate(path);
                        if (degree != 0) {
                            Matrix m = new Matrix();
                            m.setRotate(degree); // 旋转angle度
                            Log.w(TAG, "showPopueWindowForPhoto: " + degree);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                        }
                        bt btt = new bt(bitmap, path);
                        bts.add(btt);
                    }else {
                        /*Drawable drawable = MyApplication.getContext().getResources().getDrawable(R.drawable.imgerror);
                        BitmapDrawable bd = (BitmapDrawable) drawable;
                        Bitmap bitmap = bd.getBitmap();
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 80, 120,
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                        bt btt = new bt(bitmap, path);
                        bts.add(btt);*/
                    }
                }
                isCreateBitmap = true;
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.poiinfotoolbar, menu);
        menu.findItem(R.id.back_andupdate).setVisible(false);
        menu.findItem(R.id.query_poi_map).setVisible(false);
        return true;
    }

    private void resetView(){
        isLongClick = 1;
        setTitle(photoshow.this.getResources().getText(R.string.PhotoList));
        refreshCard();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back_pois:
                this.finish();
                //showPopueWindowForPhoto("/storage/emulated/0/DCIM/Camera/IMG_20180322_230831.jpg");
                break;
            case R.id.restore_pois:
                resetView();
                break;
            case R.id.deletepoi:
                isLongClick = 1;
                setTitle(photoshow.this.getResources().getText(R.string.PhotoList));
                invalidateOptionsMenu();
                //LitePal.deleteAll(MPHOTO.class, "POIC = ?", POIC, "path = ?", deletePath);
                parseSelectedPath();
                refreshCard();
                break;
            case R.id.add_pois:
                showPopueWindowForPhoto();
                //refreshCard();
                break;
            default:
                break;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            Uri uri = data.getData();
            List<POI> POIs = LitePal.where("poic = ?", POIC).find(POI.class);
            POI poi = new POI();
            long time = System.currentTimeMillis();
            poi.setPhotonum(POIs.get(0).getPhotonum() + 1);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(photoshow.this.getResources().getText(R.string.DateAndTime).toString());
            Date date = new Date(System.currentTimeMillis());
            poi.updateAll("poic = ?", POIC);
            MPHOTO mphoto = new MPHOTO();
            mphoto.setPdfic(POIs.get(0).getIc());
            mphoto.setPoic(POIC);
            //Log.w(TAG, "onActivityResult: " + uri.getPath() );
            //mphoto.setPath(getRealPath(uri.getPath()));
            mphoto.setPath(DataUtil.getRealPathFromUriForPhoto(this, uri));
            mphoto.setTime(simpleDateFormat.format(date));
            mphoto.save();
            refreshCard();
        }
        if (resultCode == RESULT_OK && requestCode == TAKE_PHOTO) {
            String imageuri;
            if (Build.VERSION.SDK_INT >= 24) {
                imageuri = DataUtil.getRealPath(imageUri.toString());
            }else {
                imageuri = imageUri.toString().substring(7);
            }
            File file = new File(imageuri);
            if (file.length() != 0) {
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), imageuri, "title", "description");
                    // 最后通知图库更新
                    photoshow.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imageuri)));
                }catch (IOException e){
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(photoshow.this.getResources().getText(R.string.DateAndTime).toString());
                Date date = new Date(System.currentTimeMillis());
                List<POI> POIs = LitePal.where("poic = ?", POIC).find(POI.class);
                POI poi = new POI();
                long time = System.currentTimeMillis();
                poi.setPhotonum(POIs.get(0).getPhotonum() + 1);
                poi.updateAll("poic = ?", POIC);
                MPHOTO mphoto = new MPHOTO();
                mphoto.setPoic(POIC);
                mphoto.setPath(imageuri);
                mphoto.setTime(simpleDateFormat.format(date));
                mphoto.save();
                refreshCard();
            }else {
                file.delete();
                Toast.makeText(photoshow.this, R.string.TakePhotoError, Toast.LENGTH_LONG).show();
            }
        }
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_CODE_PHOTO);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void parseSelectedPath(){
        if (deletePath.contains("wslzy")){
            String[] nums = deletePath.split("wslzy");
            Log.w(TAG, "parseSelectedPath: " + nums[0] );
            for (int i = 0; i < nums.length; i++){
                //LitePal.deleteAll(MPHOTO.class, "poic = ? and path = ?", POIC, nums[i]);
                LitePal.deleteAll(MPHOTO.class, "poic = ? and time = ?", POIC, nums[i]);
            }
        }else {
            //LitePal.deleteAll(MPHOTO.class, "poic = ? and path = ?", POIC, deletePath);
            LitePal.deleteAll(MPHOTO.class, "poic = ? and time = ?", POIC, deletePath);
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
                launchPicker();
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
            imageUri = FileProvider.getUriForFile(photoshow.this, "com.android.tuzhi.fileprovider", outputImage);

        }else imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void showPopueWindowForPhoto(String path){
        //final RelativeLayout linearLayout= (RelativeLayout) getLayoutInflater().inflate(R.layout.popupwindow_photo_show, null);
        View popView = View.inflate(this,R.layout.popupwindow_photo_show,null);
        final ImageView imageView1 = (ImageView) popView.findViewById(R.id.photoshow_all1);
        Log.w(TAG, "showPopueWindowForPhoto: " + path);
        //File outputImage = new File(path);
        //
        int size = bts.size();
        for (int i = 0; i < size; i++){
            if (path.equals(bts.get(i).getM_path())) imageView1.setImageBitmap(bts.get(i).getM_bm());
        }

        //获取屏幕宽高
        int weight = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels * 2 / 3;

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
        /*popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //imageView1.setVisibility(View.INVISIBLE);
                popupWindow.dismiss();
                return false;
            }
        });*/
        //popupWindow出现屏幕变为半透明
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        popupWindow.showAtLocation(popView, Gravity.BOTTOM,0,50);

    }

    private Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
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

}
