package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by 54286 on 2018/3/20.
 */

public class mPhotobjAdapter extends RecyclerView.Adapter<mPhotobjAdapter.ViewHolder>{
    private static final String TAG = "mPhotobjAdapter";
    private Context mContext;

    private List<mPhotobj> mPhotobjList;

    private mPhotobjAdapter.OnRecyclerItemLongListener mOnItemLong;

    private mPhotobjAdapter.OnRecyclerItemClickListener mOnItemClick;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        ImageView PhotoImage;
        TextView PhotoName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            PhotoImage = (ImageView) view.findViewById(R.id.photo_image);
            PhotoName = (TextView) view.findViewById(R.id.photo_txt);



        }
    }
    public mPhotobjAdapter(List<mPhotobj> mPhotobjList) {
        this.mPhotobjList = mPhotobjList;
    }

    @Override
    public mPhotobjAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false);
        final mPhotobjAdapter.ViewHolder holder = new mPhotobjAdapter.ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                mPhotobj poi = mPhotobjList.get(position);
                mOnItemClick.onItemClick(v, poi.getM_path(), position);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemLong != null){
                    int position = holder.getAdapterPosition();
                    mPhotobj mphotobj = mPhotobjList.get(position);
                    mOnItemLong.onItemLongClick(v, mphotobj.getM_path());
                    holder.cardView.setCardBackgroundColor(Color.GRAY);
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(mPhotobjAdapter.ViewHolder holder, int position) {
        mPhotobj mphoto = mPhotobjList.get(position);
        //holder.PhotoImage.setImageURI(Uri.parse(mphoto.getM_path()));
        File outputImage = new File(mphoto.getM_path());
        if (Build.VERSION.SDK_INT >= 24){
            //locError(Environment.getExternalStorageDirectory() + "/maphoto/" + Long.toString(timenow) + ".jpg");
            Uri imageUri = FileProvider.getUriForFile(mContext, "com.geopdfviewer.android.fileprovider", outputImage);
            Log.w(TAG, "onBindViewHolder: " + imageUri.toString());
            holder.PhotoImage.setImageURI(imageUri);
        }else {
            Uri imageUri = Uri.fromFile(outputImage);
            Log.w(TAG, "onBindViewHolder: " + imageUri.toString());
            holder.PhotoImage.setImageURI(imageUri);
        }
        String data;
        data = "图片名称: " + mphoto.getM_name() + "\n" + "时间: " + mphoto.getM_time();
        // + "\n" + "录制地址为: " +
        //mphoto.getM_X() + ", " + mphoto.getM_Y()
        holder.PhotoName.setText(data);
    }


    @Override
    public int getItemCount() {
        return mPhotobjList.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view,String path);
    }
    public void setOnItemLongClickListener(mPhotobjAdapter.OnRecyclerItemLongListener listener){
        //Log.w(TAG, "setOnItemLongClickListener: " );
        this.mOnItemLong =  listener;
    }
    public interface OnRecyclerItemClickListener{
        void onItemClick(View view,String path, int position);
    }
    public void setOnItemClickListener(OnRecyclerItemClickListener listener){
        this.mOnItemClick =  listener;
    }

    public static void sizeCompress(Bitmap bmp, File file) {
        // 尺寸压缩倍数,值越大，图片尺寸越小
        int ratio = 8;
        // 压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(bmp.getWidth() / ratio, bmp.getHeight() / ratio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, bmp.getWidth() / ratio, bmp.getHeight() / ratio);
        canvas.drawBitmap(bmp, null, rect, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
