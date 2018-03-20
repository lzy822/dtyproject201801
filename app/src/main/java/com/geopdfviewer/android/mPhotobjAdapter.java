package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 54286 on 2018/3/20.
 */

public class mPhotobjAdapter extends RecyclerView.Adapter<mPhotobjAdapter.ViewHolder>{
    private Context mContext;

    private List<mPhotobj> mPhotobjList;

    private mPhotobjAdapter.OnRecyclerItemLongListener mOnItemLong;

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
                //Intent intent = new Intent(mContext, singlepoi.class);
                //intent.putExtra("POIC", poi.getM_POIC());
                //mContext.startActivity(intent);
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
        holder.PhotoImage.setImageURI(Uri.parse(mphoto.getM_path()));
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
}
