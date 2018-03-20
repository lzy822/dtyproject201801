package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
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

public class mTapeobjAdapter extends RecyclerView.Adapter<mTapeobjAdapter.ViewHolder>{
    private Context mContext;

    private List<mTapeobj> mTapeobjList;

    private mTapeobjAdapter.OnRecyclerItemLongListener mOnItemLong;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //private OnRecyclerItemLongListener mOnItemLong = null;
        CardView cardView;
        ImageView TapeImage;
        TextView TapeName;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            TapeImage = (ImageView) view.findViewById(R.id.tape_image);
            TapeName = (TextView) view.findViewById(R.id.tape_txt);



        }
    }
    public mTapeobjAdapter(List<mTapeobj> mTapeobjList) {
        this.mTapeobjList = mTapeobjList;
    }

    @Override
    public mTapeobjAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.tape_item, parent, false);
        final mTapeobjAdapter.ViewHolder holder = new mTapeobjAdapter.ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                mTapeobj mtapeobj = mTapeobjList.get(position);
                MediaPlayer mediaPlayer = MediaPlayer.create(mContext, Uri.parse(mtapeobj.getM_path()));
                mediaPlayer.start();
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
                    mTapeobj tapeobj = mTapeobjList.get(position);
                    mOnItemLong.onItemLongClick(v, tapeobj.getM_path());
                    holder.cardView.setCardBackgroundColor(Color.GRAY);
                }
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(mTapeobjAdapter.ViewHolder holder, int position) {
        mTapeobj mtape = mTapeobjList.get(position);
        holder.TapeImage.setImageResource(R.drawable.ic_sound_b);
        String data;
        data = "音频名称: " + mtape.getM_name() + "\n" + "时间: " + mtape.getM_time();
        //+ "\n" + "录制地址为: " +
        //        mtape.getM_X() + ", " + mtape.getM_Y()
        holder.TapeName.setText(data);
    }


    @Override
    public int getItemCount() {
        return mTapeobjList.size();
    }



    public interface OnRecyclerItemLongListener{
        void onItemLongClick(View view,String path);
    }
    public void setOnItemLongClickListener(mTapeobjAdapter.OnRecyclerItemLongListener listener){
        //Log.w(TAG, "setOnItemLongClickListener: " );
        this.mOnItemLong =  listener;
    }
}
